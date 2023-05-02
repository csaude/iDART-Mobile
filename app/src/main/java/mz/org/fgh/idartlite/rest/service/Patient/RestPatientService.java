package mz.org.fgh.idartlite.rest.service.Patient;

import static mz.org.fgh.idartlite.savelogs.PathConstant.LOGGER_FILE_PATH;
import static mz.org.fgh.idartlite.util.DateUtilities.getSqlDateFromString;

import android.app.Application;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import mz.org.fgh.idartlite.base.application.IdartLiteApplication;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.rest.BaseRestService;
import mz.org.fgh.idartlite.base.service.BaseService;
import mz.org.fgh.idartlite.listener.rest.RestResponseListener;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.ClinicSector;
import mz.org.fgh.idartlite.model.Episode;
import mz.org.fgh.idartlite.model.Province;
import mz.org.fgh.idartlite.model.SyncMobilePatient;
import mz.org.fgh.idartlite.model.SyncTempPatient;
import mz.org.fgh.idartlite.model.User;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.patient.PatientAttribute;
import mz.org.fgh.idartlite.rest.helper.RESTServiceHandler;
import mz.org.fgh.idartlite.rest.service.ClinicInfo.RestClinicInfoService;
import mz.org.fgh.idartlite.savelogs.SaveLogsInStorage;
import mz.org.fgh.idartlite.service.clinic.ClinicSectorService;
import mz.org.fgh.idartlite.service.clinic.ClinicService;
import mz.org.fgh.idartlite.service.clinic.IClinicService;
import mz.org.fgh.idartlite.service.episode.EpisodeService;
import mz.org.fgh.idartlite.service.episode.IEpisodeService;
import mz.org.fgh.idartlite.service.patient.IPatientService;
import mz.org.fgh.idartlite.service.patient.PatientService;
import mz.org.fgh.idartlite.service.prescription.IPrescriptionService;
import mz.org.fgh.idartlite.service.prescription.PrescriptionService;
import mz.org.fgh.idartlite.service.territory.IProvinceService;
import mz.org.fgh.idartlite.service.territory.ProvinceService;
import mz.org.fgh.idartlite.util.Utilities;

public class RestPatientService extends BaseRestService {

    private static final String TAG = "RestPatientService";
    private static String url = null;
    private static IClinicService clinicService;
    private static ClinicSectorService clinicSectorService;
    private static IEpisodeService episodeService;
    private static IPatientService patientService;
    private static IProvinceService provinceService;
    private static IPrescriptionService prescriptionService;


    public RestPatientService(Application application, User currentUser) {
        super(application, currentUser);

        clinicService = new ClinicService(getApplication(), null);

        patientService = new PatientService(getApplication(), null);

        prescriptionService = new PrescriptionService(getApplication());
    }

    public static void getAllPatient(RestResponseListener listener, long offset, long limit, boolean isFullLoad) {
        SaveLogsInStorage log = SaveLogsInStorage.getSaveLoggerInstance(IdartLiteApplication.getInstance().getApplicationContext(), LOGGER_FILE_PATH);
        try {
            clinicService = new ClinicService(getApp(), null);
            clinicSectorService = new ClinicSectorService(getApp(), null);
            Clinic clinic = clinicService.getAllClinics().get(0);
            Clinic finalClinic = clinicService.getAllClinics().get(0);
            ArrayList<ClinicSector> clinicSectorList = (ArrayList<ClinicSector>) clinicSectorService.getClinicSectorsByClinic(finalClinic);

            if (clinicSectorList.isEmpty()) {
                url = BaseRestService.baseUrl + "/sync_temp_patients?clinicuuid=eq." + clinic.getUuid() +
                        "&exclusaopaciente=eq.false";
                if (!isFullLoad) url += "&syncstatus=in.(\"P\",\"U\")";
                url += "&uuidopenmrs=not.in.(null,\"NA\")" +
                        "&offset=" + offset +
                        "&limit=" + limit;
            } else {
                ClinicSector clinicSector = clinicSectorList.get(0);
                if (!clinicSector.getClinicSectorType().getDescription().contains("Provedor")) {
                    url = BaseRestService.baseUrl + "/sync_temp_patients?clinicuuid=eq." + clinicSector.getUuid() +
                            "&exclusaopaciente=eq.false&syncstatus=eq.P&uuidopenmrs=not.in.(null,\"NA\")" +
                            "&offset=" + offset +
                            "&limit=" + limit;
                }
                // Carregar paciente alocados ao Provedor iDART Modbile v2.4.0
            }

            patientService = new PatientService(getApp(), null);


            List<Patient> newPatients = new ArrayList<>();

            if (RESTServiceHandler.getServerStatus(BaseRestService.baseUrl)) {
                getRestServiceExecutor().execute(() -> {

                    RESTServiceHandler handler = new RESTServiceHandler();
                    handler.addHeader("Content-Type", "Application/json");

                    handler.objectRequest(url, Request.Method.GET, null, Object[].class, new Response.Listener<Object[]>() {
                        @Override
                        public void onResponse(Object[] patients) {
                            Log.d("Response", String.valueOf(patients.length));

                            if (patients.length > 0) {

                                for (Object patient : patients) {
                                    Log.i(TAG, "onResponse: " + patient);
                                    Patient patientObj;
                                    try {
                                        LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) patient;
                                        if (!patientService.checkPatient(itemresult)) {
                                            patientObj = patientService.saveOnPatient(itemresult);
                                            newPatients.add(new Patient(itemresult.get("uuidopenmrs").toString()));
                                        } else {
                                            patientObj = patientService.updateOnPatientViaRest(itemresult);
                                            newPatients.add(new Patient());
                                            Log.i(TAG, "onResponse: " + patient + " Ja Existe");
                                        }
                                        restPatchSyncStatus(patientObj, BaseModel.SYNC_SATUS_SENT);
                                        RestClinicInfoService.getRestLastClinicInfo(patientObj);
                                    } catch (Exception e) {
                                        log.saveErrorLogs(TAG, "Ocorreu um erro ao Buscar Pacientes: [ Patient: " + patient + "]: \n " + e);
                                    } finally {
                                        continue;
                                    }
                                }

                                listener.doOnResponse(BaseRestService.REQUEST_SUCESS, newPatients);
                                //listener.onResponse(BaseRestService.REQUEST_SUCESS, result);
                            } else {
                                listener.doOnResponse(REQUEST_NO_DATA, null);
                                Log.w(TAG, "Response Sem Info." + patients.length);
                            }
                        }

                    }, error -> {
                        listener.doOnResponse(generateErrorMsg(error), null);
                        Log.d(TAG, "onErrorResponse: Erro no GET :" + generateErrorMsg(error));
                    });
                });

            } else {
                Log.e(TAG, "Response Servidor Offline");
            }
        } catch (Exception e) {
            log.saveErrorLogs(TAG, "Ocorreu um erro ao Buscar Pacientes: " + e);
        }
    }

    public static void restPostPatientSector(Patient patient) throws SQLException {

        String url = BaseRestService.baseUrl + "/sync_mobile_patient";

        clinicService = new ClinicService(BaseService.getApp(), null);
        clinicSectorService = new ClinicSectorService(getApp(), null);
        episodeService = new EpisodeService(getApp(), null);

        if (patient != null) {


            try {
                Clinic finalClinic = clinicService.getAllClinics().get(0);
                ClinicSector clinicSector = (ClinicSector) clinicSectorService.getClinicSectorsByClinic(finalClinic).get(0);
                Episode episode = episodeService.getAllEpisodesByPatient(patient).get(0);

                if (episode.getSyncStatus().equalsIgnoreCase(BaseModel.SYNC_SATUS_READY))
                    getRestServiceExecutor().execute(() -> {

                        RESTServiceHandler handler = new RESTServiceHandler();

                        try {

                            SyncMobilePatient syncPatient = convertSyncPatient(patient, episode, clinicSector, finalClinic);

                            Gson g = new Gson();
                            String restObject = g.toJson(syncPatient);

                            handler.addHeader("Content-Type", "application/json");
                            JSONObject jsonObject = new JSONObject(restObject);
                            handler.objectRequest(url, Request.Method.POST, jsonObject, Object[].class, new Response.Listener<TreeMap<String, Object>>() {

                                @Override
                                public void onResponse(TreeMap<String, Object> response) {
                                    Log.d(TAG, "onResponse: Paciente enviado : " + response);

                                    try {
                                        episode.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
                                        episodeService.udpateEpisode(episode);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d(TAG, "onErrorResponse: Erro no POST :" + generateErrorMsg(error));
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void restGetPatientByNidOrNameOrSurname(String nid, String name, String surname,long offset, long limit, RestResponseListener listener) {
        final boolean finished = false;

        clinicService = new ClinicService(getApp(), null);

        patientService = new PatientService(getApp(), null);
        provinceService = new ProvinceService(getApp(), null);
        prescriptionService = new PrescriptionService(getApp(), null);

        List<Patient> newPatients = new ArrayList<>();
        Clinic clinic = null;
        Map<String, Province> provinceMap = new HashMap<>();
        try {
            clinic = clinicService.getAllClinics().get(0);
            provinceMap = provinceService.getProvincesInMap();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (RESTServiceHandler.getServerStatus(BaseRestService.baseUrl)) {

            Clinic finalClinic = clinic;
            Map<String, Province> finalProvinceMap = provinceMap;
            getRestServiceExecutor().execute(() -> {
                String url = BaseRestService.baseUrl + "/sync_temp_patients?or=(patientid.like.*" + nid + "*" + ",firstnames.like.*" + name + "*" + ",lastname.like.*" + surname + "*)&offset=" + offset +
                        "&limit=" + limit;


                RESTServiceHandler handler = new RESTServiceHandler();
                handler.addHeader("Content-Type", "Application/json");

                handler.objectRequest(url, Request.Method.GET, null, Object[].class, new Response.Listener<Object[]>() {
                    @Override
                    public void onResponse(Object[] patients) {
                        Log.d("Response", String.valueOf(patients.length));
                        if (patients.length > 0) {
                            for (Object patient : patients) {
                                Log.i(TAG, "onResponse: " + patient);
                                Patient patientObj;
                                LinkedTreeMap<String, Object> patientRest = (LinkedTreeMap<String, Object>) patient;
                                if (patientRest.get("uuidopenmrs") !="" && patientRest.get("uuidopenmrs") != null ) {
                                Patient localPatient = new Patient();
                                String address1 = patientRest.get("address1") == null ? "" : patientRest.get("address1").toString();
                                String address2 = patientRest.get("address2") == null ? "" : patientRest.get("address2").toString();
                                String address3 = patientRest.get("address3") == null ? "" : patientRest.get("address3").toString();
                                String concatAdrees = getFullAdreess(Objects.requireNonNull(address1),
                                        Objects.requireNonNull(address2),
                                        Objects.requireNonNull(address3));

                                localPatient.setProvince(finalProvinceMap.get(Objects.requireNonNull(patientRest.get("province")).toString()));
                                localPatient.setAddress(concatAdrees);
                                localPatient.setBirthDate(getSqlDateFromString(Objects.requireNonNull(patientRest.get("dateofbirth")).toString(), "yyyy-MM-dd'T'HH:mm:ss"));
                                localPatient.setClinic(finalClinic);
                                localPatient.setFirstName(Objects.requireNonNull(patientRest.get("firstnames")).toString());
                                localPatient.setLastName(Objects.requireNonNull(patientRest.get("lastname")).toString());
                                localPatient.setGender(Objects.requireNonNull(patientRest.get("sex")).toString());
                                localPatient.setNid(Objects.requireNonNull(patientRest.get("patientid")).toString());
                                localPatient.setPhone(Objects.requireNonNull(patientRest.get("cellphone")).toString());
                                localPatient.setUuid(Objects.requireNonNull(patientRest.get("uuidopenmrs")).toString());
                                if ((patientRest.get("datainiciotarv") != null)) {
                                    localPatient.setStartARVDate(getSqlDateFromString(Objects.requireNonNull(patientRest.get("datainiciotarv")).toString(), "dd MMM yyyy"));
                                }

                                        localPatient.setAttributes(new ArrayList<>());
                                        localPatient.getAttributes().add(PatientAttribute.fastCreateFaltoso(localPatient));

                                        Episode episode = new Episode();
                                        episode.setEpisodeDate(getSqlDateFromString(Objects.requireNonNull(patientRest.get("prescriptiondate")).toString(), "yyyy-MM-dd'T'HH:mm:ss"));
                                        episode.setPatient(localPatient);
                                        episode.setSanitaryUnit(Objects.requireNonNull(patientRest.get("mainclinicname")).toString());
                                        episode.setUsUuid(Objects.requireNonNull(patientRest.get("mainclinicuuid")).toString());
                                        episode.setStartReason(PatientAttribute.PATIENT_DISPENSATION_STATUS_FALTOSO);
                                        episode.setNotes(PatientAttribute.PATIENT_DISPENSATION_STATUS_FALTOSO);
                                        episode.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
                                        episode.setUuid(UUID.randomUUID().toString());
                                        localPatient.setEpisodes(new ArrayList<>());
                                        localPatient.getEpisodes().add(episode);
                                    prescriptionService.saveLastPrescriptionFromRest(patientRest, localPatient);
                                localPatient.addAttribute(PatientAttribute.fastCreateByCode(Objects.requireNonNull(patientRest.get("estadopaciente")).toString(), localPatient));
                                newPatients.add(localPatient);
                            }
                            }

                        } else {
                            listener.doOnResponse(REQUEST_NO_DATA, null);
                            Log.w(TAG, "Response Sem Info." + patients.length);
                        }
                        listener.doOnResponse(BaseRestService.REQUEST_SUCESS, newPatients);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Response", generateErrorMsg(error));
                    }
                });
            });
            //return newPatients;
        } else {
            Log.e(TAG, "Response Servidor Offline");

        }
    }

    public static void restSearchPatientFaltosoOrAbandonoByNidOrNameOrSurname(String nid, String name, String surname, long offset, long limit, RestResponseListener listener) {


        final boolean finished = false;

        clinicService = new ClinicService(getApp(), null);

        patientService = new PatientService(getApp(), null);
        provinceService = new ProvinceService(getApp(), null);
        prescriptionService = new PrescriptionService(getApp());

        List<Patient> newPatients = new ArrayList<>();
        Clinic clinic = null;
        Map<String, Province> provinceMap = new HashMap<>();
        try {
            clinic = clinicService.getAllClinics().get(0);
            provinceMap = provinceService.getProvincesInMap();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (RESTServiceHandler.getServerStatus(BaseRestService.baseUrl)) {

            Clinic finalClinic = clinic;
            Map<String, Province> finalProvinceMap = provinceMap;
            getRestServiceExecutor().execute(() -> {
                String url;
                url = BaseRestService.baseUrl + "/sync_temp_patients?or=(patientid.like.*" + nid + "*" +
                        ",firstnames.like.*" + name + "*" +
                        ",lastname.like.*" + surname + "*)" +
                        "&and=(estadopaciente.in.(\"Faltoso\",\"Abandono\")" +
                        ",exclusaopaciente.is.false)" +
                        "&mainclinicuuid=eq." + finalClinic.getUuid() +
                        "&offset=" + offset +
                        "&limit=" + limit;

                Log.e(TAG, url);
                RESTServiceHandler handler = new RESTServiceHandler();
                handler.addHeader("Content-Type", "Application/json");

                handler.objectRequest(url, Request.Method.GET, null, Object[].class, new Response.Listener<Object[]>() {
                    @Override
                    public void onResponse(Object[] patients) {
                        Log.d("Response", String.valueOf(patients.length));

                        if (patients.length > 0) {
                            for (Object patient : patients) {
                                Log.i(TAG, "onResponse: " + patient);

                                LinkedTreeMap<String, Object> patientRest = (LinkedTreeMap<String, Object>) patient;
                                Patient localPatient = new Patient();

                                String concatAdrees = getFullAdreess(Objects.requireNonNull(patientRest.get("address1")).toString(),
                                        Objects.requireNonNull(patientRest.get("address2")).toString(),
                                        Objects.requireNonNull(patientRest.get("address3")).toString());

                                localPatient.setProvince(finalProvinceMap.get(Objects.requireNonNull(patientRest.get("province")).toString()));
                                localPatient.setAddress(concatAdrees);
                                if ((patientRest.get("dateofbirth") != null)) {
                                    localPatient.setBirthDate(getSqlDateFromString(Objects.requireNonNull(patientRest.get("dateofbirth")).toString(), "yyyy-MM-dd'T'HH:mm:ss"));
                                }
                                localPatient.setClinic(finalClinic);
                                localPatient.setFirstName(Objects.requireNonNull(patientRest.get("firstnames")).toString());
                                localPatient.setLastName(Objects.requireNonNull(patientRest.get("lastname")).toString());
                                localPatient.setGender(Objects.requireNonNull(patientRest.get("sex")).toString());
                                localPatient.setNid(Objects.requireNonNull(patientRest.get("patientid")).toString());
                                localPatient.setPhone(Objects.requireNonNull(patientRest.get("cellphone")).toString());
                                localPatient.setUuid(Objects.requireNonNull(patientRest.get("uuidopenmrs")).toString());
                                if ((patientRest.get("datainiciotarv") != null)) {
                                    localPatient.setStartARVDate(getSqlDateFromString(Objects.requireNonNull(patientRest.get("datainiciotarv")).toString(), "dd MMM yyyy"));
                                }
                                //Falsoso ou abandono
                                if (true) {
                                    localPatient.setAttributes(new ArrayList<>());
                                    localPatient.getAttributes().add(PatientAttribute.fastCreateFaltoso(localPatient));

                                    Episode episode = new Episode();
                                    episode.setEpisodeDate(getSqlDateFromString(Objects.requireNonNull(patientRest.get("prescriptiondate")).toString(), "yyyy-MM-dd'T'HH:mm:ss"));
                                    episode.setPatient(localPatient);
                                    episode.setSanitaryUnit(Objects.requireNonNull(patientRest.get("mainclinicname")).toString());
                                    episode.setUsUuid(Objects.requireNonNull(patientRest.get("mainclinicuuid")).toString());
                                    episode.setStartReason(PatientAttribute.PATIENT_DISPENSATION_STATUS_FALTOSO);
                                    episode.setNotes(PatientAttribute.PATIENT_DISPENSATION_STATUS_FALTOSO);
                                    episode.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
                                    episode.setUuid(UUID.randomUUID().toString());
                                    localPatient.setEpisodes(new ArrayList<>());
                                    localPatient.getEpisodes().add(episode);
                                }
                                prescriptionService.saveLastPrescriptionFromRest(patientRest, localPatient);
                                newPatients.add(localPatient);
                            }
                            listener.doOnResponse(BaseRestService.REQUEST_SUCESS, newPatients);
                        } else {
                            Log.w(TAG, "Response Sem Info." + patients.length);
                            listener.doOnResponse(REQUEST_NO_DATA, null);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.doOnResponse(generateErrorMsg(error), null);
                        Log.e("Response", generateErrorMsg(error));
                    }
                });
            });
        } else {
            Log.e(TAG, "Response Servidor Offline");

        }
    }

    private void buildEpisode(LinkedTreeMap<String, Object> patient, Patient localPatient, Date episodeDate) {
        Episode episode = new Episode();
        episode.setEpisodeDate(episodeDate);
        episode.setPatient(localPatient);
        episode.setSanitaryUnit(Objects.requireNonNull(patient.get("mainclinicname")).toString());
        episode.setUsUuid(Objects.requireNonNull(patient.get("mainclinicuuid")).toString());
        episode.setStartReason("Referido De");
        episode.setNotes("Referido De");
        episode.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
        episode.setUuid(UUID.randomUUID().toString());
        localPatient.setEpisodes(new ArrayList<>());
        localPatient.getEpisodes().add(episode);
    }

    private static String getFullAdreess(String address1, String address2, String address3) {
        return address1 + " " + address2 + " " + address3;
    }


    private static SyncMobilePatient convertSyncPatient(Patient patient, Episode episode, ClinicSector clinicSector, Clinic clinic) {

        SyncMobilePatient syncMobilePatient = new SyncMobilePatient();

        syncMobilePatient.setAddress1(patient.getDistrict() != null ? patient.getDistrict().getName() : " ");
        syncMobilePatient.setAddress2(patient.getAddress());
        syncMobilePatient.setAddress3("");
        syncMobilePatient.setCellphone(patient.getPhone());
        syncMobilePatient.setDateofbirth(patient.getBirthDate());
        syncMobilePatient.setNextofkinname("");
        syncMobilePatient.setNextofkinphone("");
        syncMobilePatient.setFirstnames(patient.getFirstName());
        syncMobilePatient.setHomephone("");
        syncMobilePatient.setLastname(patient.getLastName());
        syncMobilePatient.setPatientid(patient.getNid());
        syncMobilePatient.setProvince(patient.getProvince() != null ? patient.getProvince().getName() : " ");
        if (patient.getGender().startsWith("F"))
            syncMobilePatient.setSex('F');
        else
            syncMobilePatient.setSex('M');
        syncMobilePatient.setWorkphone("");
        syncMobilePatient.setRace("");
        syncMobilePatient.setUuidopenmrs("");
        //    syncMobilePatient.setSex('F');
        syncMobilePatient.setSyncstatus(BaseModel.SYNC_SATUS_SENT);
        syncMobilePatient.setSyncuuid(UUID.randomUUID().toString());
        syncMobilePatient.setClinicsectoruuid(clinicSector.getUuid());
        syncMobilePatient.setClinicuuid(clinic.getUuid());
        syncMobilePatient.setArvstartdate(patient.getStartARVDate());
        syncMobilePatient.setEnrolldate(episode.getEpisodeDate());

        return syncMobilePatient;

    }

    public static void restPatchPatientFaltosoOrAbandono(Patient patient) throws SQLException {

        String url = BaseRestService.baseUrl + "/sync_temp_patients?uuidopenmrs=eq." + patient.getUuid();

        episodeService = new EpisodeService(getApp());

        if (patient != null) {


            try {
                Episode episode = episodeService.getLatestByPatient(patient);

                if (episode.getSyncStatus().equalsIgnoreCase(BaseModel.SYNC_SATUS_READY))
                    getRestServiceExecutor().execute(() -> {

                        RESTServiceHandler handler = new RESTServiceHandler();

                        try {

                            SyncTempPatient syncPatient = convertSyncTempPatient(patient, BaseModel.SYNC_SATUS_UPDATED, true);

                            Gson g = new Gson();
                            String restObject = g.toJson(syncPatient);

                            handler.addHeader("Content-Type", "application/json");
                            JSONObject jsonObject = new JSONObject(restObject);
                            handler.objectRequest(url, Request.Method.PATCH, jsonObject, Object[].class, (Response.Listener<TreeMap<String, Object>>) response -> {
                                Log.d(TAG, "onResponse: Paciente enviado : " + response);

                                try {
                                    episode.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
                                    episodeService.udpateEpisode(episode);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }, error -> Log.d(TAG, "onErrorResponse: Erro no POST :" + generateErrorMsg(error)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void restPatchSyncStatus(Patient patient, String syncStatus) {

        String url = BaseRestService.baseUrl + "/sync_temp_patients?uuidopenmrs=eq." + patient.getUuid();

        episodeService = new EpisodeService(getApp());

        if (patient != null) {


            try {
                Episode episode = episodeService.getLatestByPatient(patient);

                getRestServiceExecutor().execute(() -> {

                    RESTServiceHandler handler = new RESTServiceHandler();

                    try {

                        SyncTempPatient syncPatient = convertSyncTempPatient(patient, syncStatus, false);

                        Gson g = new Gson();
                        String restObject = g.toJson(syncPatient);

                        handler.addHeader("Content-Type", "application/json");
                        JSONObject jsonObject = new JSONObject(restObject);
                        handler.objectRequest(url, Request.Method.PATCH, jsonObject, Object[].class, (Response.Listener<TreeMap<String, Object>>) response -> {
                            Log.d(TAG, "onResponse: Paciente actualizado : " + response);

                        }, error -> Log.d(TAG, "onErrorResponse: Erro no POST :" + generateErrorMsg(error)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static SyncTempPatient convertSyncTempPatient(Patient patient, String syncStatus, boolean isFaltoso) {
        SyncTempPatient syncTempPatient = new SyncTempPatient();
        syncTempPatient.setSyncstatus(syncStatus.charAt(0));
        if (isFaltoso) syncTempPatient.setExclusaopaciente(true);
        return syncTempPatient;
    }

    public static void checkLoadingStatus() {
        clinicService = new ClinicService(BaseService.getApp(), null);
        clinicSectorService = new ClinicSectorService(getApp(), null);
        episodeService = new EpisodeService(getApp(), null);


        try {
            Clinic finalClinic = clinicService.getAllClinics().get(0);
            List<ClinicSector> clinicSectorList = clinicSectorService.getClinicSectorsByClinic(finalClinic);
            ClinicSector clinicSector = !clinicSectorList.isEmpty() ? clinicSectorList.get(0) : null;
            if (clinicSector != null && clinicSector.getClinicSectorType().getCode().contains("PROVEDOR")) {
                String url = BaseRestService.baseUrl + "/sync_temp_check_loading?mainclinicuuid=eq." + finalClinic.getUuid();

                RESTServiceHandler handler = new RESTServiceHandler();
                handler.addHeader("Content-Type", "Application/json");
                handler.objectRequest(url, Request.Method.GET, null, Object[].class, new Response.Listener<Object[]>() {
                    @Override
                    public void onResponse(Object[] loadingStatusList) {

                        if (loadingStatusList.length > 0) {
                            for (Object loading : loadingStatusList) {
                                Log.i(TAG, "onResponse: " + loading);
                                try {
                                    LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) loading;
                                    Boolean isLoading = itemresult.get("isloading") != null ? Boolean.valueOf(itemresult.get("isloading").toString()) : null;
                                    if (isLoading != null) {
                                        if (isLoading) {
                                            Utilities.issueNotification(NotificationManagerCompat.from(IdartLiteApplication.getInstance().getApplicationContext()), IdartLiteApplication.getInstance().getApplicationContext(), "O carregamento de pacientes ainda está em curso.", IdartLiteApplication.CHANNEL_1_ID, false, ThreadLocalRandom.current().nextInt());

                                        } else {
                                            Utilities.issueNotification(NotificationManagerCompat.from(IdartLiteApplication.getInstance().getApplicationContext()), IdartLiteApplication.getInstance().getApplicationContext(), "O carregamento de pacientes Terminado.", IdartLiteApplication.CHANNEL_1_ID, false, ThreadLocalRandom.current().nextInt());

                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    continue;
                                }
                            }
                        } else
                            Log.w(TAG, "Response Sem Info." + loadingStatusList.length);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Response", BaseRestService.generateErrorMsg(error));
                    }
                });

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
