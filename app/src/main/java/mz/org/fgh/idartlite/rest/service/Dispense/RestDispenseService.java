package mz.org.fgh.idartlite.rest.service.Dispense;

import static mz.org.fgh.idartlite.savelogs.PathConstant.LOGGER_FILE_PATH;

import android.app.Application;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

import mz.org.fgh.idartlite.base.application.IdartLiteApplication;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.rest.BaseRestService;
import mz.org.fgh.idartlite.listener.rest.RestResponseListener;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.ClinicSector;
import mz.org.fgh.idartlite.model.DiseaseType;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.DispensedDrug;
import mz.org.fgh.idartlite.model.Drug;
import mz.org.fgh.idartlite.model.Episode;
import mz.org.fgh.idartlite.model.PrescribedDrug;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.model.SyncDispense;
import mz.org.fgh.idartlite.model.User;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.rest.helper.RESTServiceHandler;
import mz.org.fgh.idartlite.savelogs.SaveLogsInStorage;
import mz.org.fgh.idartlite.service.clinic.ClinicSectorService;
import mz.org.fgh.idartlite.service.clinic.ClinicService;
import mz.org.fgh.idartlite.service.clinic.IClinicService;
import mz.org.fgh.idartlite.service.dispense.DispenseDrugService;
import mz.org.fgh.idartlite.service.dispense.DispenseService;
import mz.org.fgh.idartlite.service.dispense.DispenseTypeService;
import mz.org.fgh.idartlite.service.dispense.IDispenseDrugService;
import mz.org.fgh.idartlite.service.dispense.IDispenseService;
import mz.org.fgh.idartlite.service.drug.DiseaseTypeService;
import mz.org.fgh.idartlite.service.drug.DrugService;
import mz.org.fgh.idartlite.service.drug.IDrugService;
import mz.org.fgh.idartlite.service.drug.TherapeuthicLineService;
import mz.org.fgh.idartlite.service.drug.TherapheuticRegimenService;
import mz.org.fgh.idartlite.service.episode.EpisodeService;
import mz.org.fgh.idartlite.service.episode.IEpisodeService;
import mz.org.fgh.idartlite.service.patient.IPatientService;
import mz.org.fgh.idartlite.service.patient.PatientService;
import mz.org.fgh.idartlite.service.prescription.IPrescriptionService;
import mz.org.fgh.idartlite.service.prescription.PrescribedDrugService;
import mz.org.fgh.idartlite.service.prescription.PrescriptionService;
import mz.org.fgh.idartlite.service.stock.IStockService;
import mz.org.fgh.idartlite.service.stock.StockService;
import mz.org.fgh.idartlite.service.user.UserService;
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.Utilities;

public class RestDispenseService extends BaseRestService {

    private static final String TAG = "RestDispenseService";
    private static IDispenseService dispenseService;
    private static IDispenseDrugService dispenseDrugService;
    private static IEpisodeService episodeService;
    private static IPrescriptionService prescriptionService;
    private static IPatientService patientService;
    private static List<DispensedDrug> dispensedDrugList;
    private static List<Episode> episodeList;
    private static ClinicService clinicService;
    private static UserService userService;
    private static ClinicSectorService clinicSectorService;

    private static String url = null;


    public RestDispenseService(Application application, User currentUser) {
        super(application, currentUser);
        dispenseService = new DispenseService(application, currentUser);
        prescriptionService = new PrescriptionService(application, currentUser);
        userService = new UserService(application, currentUser);
    }


    public static void restGetLastDispense(Patient patient) throws SQLException {
        restGetLastDispense(null, patient);
    }

    public static void restGetAllLastDispense(RestResponseListener listener, long offset, long limit) throws SQLException {

        SaveLogsInStorage log = SaveLogsInStorage.getSaveLoggerInstance(IdartLiteApplication.getInstance().getApplicationContext(), LOGGER_FILE_PATH);
        dispenseService = new DispenseService(getApp(), null);
        dispenseDrugService = new DispenseDrugService(getApp(), null);
        prescriptionService = new PrescriptionService(getApp(), null);
        episodeService = new EpisodeService(getApp(), null);
        patientService = new PatientService(getApp());
        clinicService = new ClinicService(getApp(), null);
        clinicSectorService = new ClinicSectorService(getApp(), null);
        Clinic clinic = clinicService.getAllClinics().get(0);

        ArrayList<ClinicSector> clinicSectorList = (ArrayList<ClinicSector>) clinicSectorService.getClinicSectorsByClinic(clinic);
        if (clinicSectorList.isEmpty()) {
             url = BaseRestService.baseUrl + "/patient_last_sync_tmp_dispense_vw?clinicuuid=eq." + clinic.getUuid() + "&offset=" + offset + "&limit=" + limit;
        } else {
             url = BaseRestService.baseUrl + "/patient_last_sync_tmp_dispense_vw?clinicuuid=eq." + clinicSectorList.get(0).getUuid() + "&offset=" + offset + "&limit=" + limit;
        }

      //  String url = BaseRestService.baseUrl + "/patient_last_sync_tmp_dispense_vw?clinicuuid=eq." + "045ee9e5-d1f6-4429-bb5d-ac05f97a8d62" + "&offset=" + offset + "&limit=" + "2246";
        try {
            getRestServiceExecutor().execute(() -> {

                RESTServiceHandler handler = new RESTServiceHandler();

                try {
                    handler.addHeader("Content-Type", "application/json");
                    handler.objectRequest(url, Request.Method.GET, null, Object[].class, (Response.Listener<Object[]>) dispenses -> {
                        try {
                            if (dispenses.length > 0) {
                                List<Dispense> dispenseList = new ArrayList<>();
                                for (Object dispense : dispenses) {
                                    Log.d(TAG, "onResponse: Dispensa " + dispense);

                                    LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) (Object) dispense;

                                    Patient patient = patientService.getPatientByUuid(itemresult.get("uuidopenmrs").toString());
                                    if (patient != null) {
                                        patient.setEpisodes(episodeService.getAllEpisodesByPatient(patient));

                                        if (patient.hasEpisode() && !patient.hasEndEpisode()) {
                                            Prescription newPrescription = getPrescroptionRest(dispense, patient);
                                            Prescription lastPrescription = prescriptionService.getLastPatientPrescriptionByDiseaseType(patient, newPrescription.getDiseaseType());

                                            if (lastPrescription != null) {
                                                if ((int) DateUtilities.dateDiff(newPrescription.getPrescriptionDate(), lastPrescription.getPrescriptionDate(), DateUtilities.DAY_FORMAT) > 0) {
                                                    prescriptionService.createPrescription(newPrescription);
                                                    savePrescribedDrugOnRest(dispense, newPrescription);
                                                    lastPrescription.setExpiryDate(newPrescription.getPrescriptionDate());
                                                    prescriptionService.updatePrescriptionEntity(lastPrescription);
                                                } else if ((int) DateUtilities.dateDiff(newPrescription.getPrescriptionDate(), lastPrescription.getPrescriptionDate(), DateUtilities.DAY_FORMAT) == 0) {
                                                    newPrescription = lastPrescription;
                                                } else {
                                                    break;
                                                }
                                            } else {
                                                if (newPrescription.getDiseaseType().getCode() != "TARV") {
                                                    prescriptionService.createPrescription(newPrescription);
                                                    savePrescribedDrugOnRest(dispense, newPrescription);
                                                }
                                            }

                                            Dispense newDispense = getDispenseOnRest(dispense, newPrescription);
                                            Dispense lastDispense = dispenseService.getLastDispenseFromPrescription(newPrescription);

                                            if (lastDispense != null) {
                                                if ((int) DateUtilities.dateDiff(newDispense.getPickupDate(), lastDispense.getPickupDate(), DateUtilities.DAY_FORMAT) > 0) {
                                                    dispenseService.createDispense(newDispense);
                                                    saveDispensedOnRest(dispense, newDispense);
                                                } // else {
                                                   // break;
                                                // }
                                            } else {
                                                dispenseService.createDispense(newDispense);
                                                saveDispensedOnRest(dispense, newDispense);
                                            }
                                            dispenseList.add(newDispense);

                                        }
                                    }
                                }
                                if (listener != null)
                                    listener.doOnResponse(BaseRestService.REQUEST_SUCESS, dispenseList);
                            } else {
                                listener.doOnResponse(REQUEST_NO_DATA, null);
                                Log.w(TAG, "Response Sem Info." + dispenses.length);
                            }
                        } catch (Exception e) {
                            log.saveErrorLogs(TAG, "Ocorreu um erro Inesperado: " + e);
                            Log.d(TAG, "onErrorResponse 1: Erro no GET :" + e.getMessage());
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            listener.doOnResponse(generateErrorMsg(error), null);
                            Log.d(TAG, "onErrorResponse: Erro no GET :" + generateErrorMsg(error));
                        }
                    });
                } catch (Exception e) {
                    log.saveErrorLogs(TAG, "Ocorreu um erro Inesperado: " + e);
                    Log.d(TAG, "onErrorResponse 2: Erro no GET :" + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.saveErrorLogs(TAG, "Ocorreu um erro Inesperado: " + e);
            Log.d(TAG, "onErrorResponse 3: Erro no GET :" + e.getMessage());
        }
    }

    public static void restGetLastDispense(RestResponseListener listener, Patient patient) throws SQLException {

        dispenseService = new DispenseService(getApp(), null);
        dispenseDrugService = new DispenseDrugService(getApp(), null);
        prescriptionService = new PrescriptionService(getApp(), null);
        episodeService = new EpisodeService(getApp(), null);

        episodeList = episodeService.getAllEpisodesByPatient(patient);

        Episode episode = episodeList.get(episodeList.size() - 1);

        if (episode != null) {

            String url = BaseRestService.baseUrl + "/sync_temp_dispense?uuidopenmrs=eq." + patient.getUuid() + "&mainclinicuuid=eq." + episode.getUsUuid() + "&order=pickupdate.desc";

            try {
                getRestServiceExecutor().execute(() -> {

                    RESTServiceHandler handler = new RESTServiceHandler();

                    try {
                        handler.addHeader("Content-Type", "application/json");
                        handler.objectRequest(url, Request.Method.GET, null, Object[].class, new Response.Listener<Object[]>() {

                            @Override
                            public void onResponse(Object[] dispenses) {
                                try {
                                    if (dispenses.length > 0) {
                                        List<Dispense> dispenseList = new ArrayList<>();
                                        for (Object dispense : dispenses) {
                                            Log.d(TAG, "onResponse: Dispensa " + dispense);

                                            Prescription newPrescription = getPrescroptionRest(dispense, patient);
                                            Prescription lastPrescription = prescriptionService.getLastPatientPrescriptionByDiseaseType(patient, newPrescription.getDiseaseType());

                                            if ((int) DateUtilities.dateDiff(newPrescription.getPrescriptionDate(), lastPrescription.getPrescriptionDate(), DateUtilities.DAY_FORMAT) > 0) {
                                                prescriptionService.createPrescription(newPrescription);
                                                savePrescribedDrugOnRest(dispense, newPrescription);
                                                lastPrescription.setExpiryDate(newPrescription.getPrescriptionDate());
                                                prescriptionService.updatePrescriptionEntity(lastPrescription);
                                            } else if ((int) DateUtilities.dateDiff(newPrescription.getPrescriptionDate(), lastPrescription.getPrescriptionDate(), DateUtilities.DAY_FORMAT) == 0) {
                                                newPrescription = lastPrescription;
                                            } else {
                                                break;
                                            }

                                            Dispense newDispense = getDispenseOnRest(dispense, newPrescription);
                                            Dispense lastDispense = dispenseService.getLastDispenseFromPrescription(newPrescription);

                                            if (lastDispense != null) {
                                                if ((int) DateUtilities.dateDiff(newDispense.getPickupDate(), lastDispense.getPickupDate(), DateUtilities.DAY_FORMAT) > 0) {
                                                    dispenseService.createDispense(newDispense);
                                                    saveDispensedOnRest(dispense, newDispense);
                                                } else {
                                                    break;
                                                }
                                            } else {
                                                dispenseService.createDispense(newDispense);
                                                saveDispensedOnRest(dispense, newDispense);
                                            }
                                            dispenseList.add(newDispense);
                                        }
                                        if (listener != null)
                                            listener.doOnRestSucessResponse(BaseRestService.REQUEST_SUCESS);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "onErrorResponse: Erro no GET :" + generateErrorMsg(error));
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void restPostDispense(Dispense dispense) {

        String url = BaseRestService.baseUrl + "/sync_temp_dispense";

        dispenseService = new DispenseService(getApp(), null);
        dispenseDrugService = new DispenseDrugService(getApp(), null);
        prescriptionService = new PrescriptionService(getApp(), null);

        try {
            getRestServiceExecutor().execute(() -> {

                RESTServiceHandler handler = new RESTServiceHandler();

                try {
                    dispensedDrugList = dispenseDrugService.findDispensedDrugByDispenseId(dispense.getId());

                    for (DispensedDrug dispensedDrug : dispensedDrugList) {

                        SyncDispense syncDispense = setSyncDispense(dispense, dispensedDrug);

                        Gson g = new Gson();
                        String restObject = g.toJson(syncDispense);

                        handler.addHeader("Content-Type", "application/json");
                        JSONObject jsonObject = new JSONObject(restObject);

                        handler.objectRequest(url, Request.Method.POST, jsonObject, Object.class, new Response.Listener<Object>() {

                            @Override
                            public void onResponse(Object response) {
                                Log.d(TAG, "onResponse: Dispensa enviada" + response);
                                try {

                                    Prescription prescription = dispense.getPrescription();

                                    prescription.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
                                    prescriptionService.updatePrescriptionEntity(prescription);
                                    dispense.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
                                    dispenseService.udpateDispense(dispense);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "onErrorResponse: Erro no POST :" + generateErrorMsg(error));
                            }
                        });
                    }
                } catch (SQLException | JSONException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Prescription getPrescroptionRest(Object dispense, Patient patient) throws SQLException {

        LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) (Object) dispense;
        DispenseTypeService dispenseTypeService = new DispenseTypeService(getApp(), null);
        DiseaseTypeService diseaseTypeService = new DiseaseTypeService(getApp(), null);
        TherapheuticRegimenService therapheuticRegimenService = new TherapheuticRegimenService(getApp(), null);
        TherapeuthicLineService therapeuthicLineService = new TherapeuthicLineService(getApp(), null);
        Prescription prescription = new Prescription();

        if (itemresult.get("dispensatrimestral").toString().contains("1"))
            prescription.setDispenseType(dispenseTypeService.getDispenseTypeByDescription("Dispensa Trimestral (DT)"));
        else if (itemresult.get("dispensasemestral").toString().contains("1"))
            prescription.setDispenseType(dispenseTypeService.getDispenseTypeByDescription("Dispensa Semestral (DS)"));
        else
            prescription.setDispenseType(dispenseTypeService.getDispenseTypeByDescription("Dispensa Mensal (DM)"));
        prescription.setUuid(UUID.randomUUID().toString());
        if (itemresult.get("prescricaoespecial").toString().contains("F") || itemresult.get("prescricaoespecial").toString().contains("f")) {
            prescription.setUrgentPrescription(Prescription.URGENT_PRESCRIPTION);
            prescription.setUrgentNotes(itemresult.get("motivocriacaoespecial").toString());
        } else {
            prescription.setUrgentPrescription(Prescription.NOT_URGENT_PRESCRIPTION);
            prescription.setUrgentNotes("");
        }
        prescription.setTherapeuticRegimen(therapheuticRegimenService.getTherapeuticRegimenByDescription(itemresult.get("regimenome").toString()));
         if (itemresult.get("linhanome") != null ) {
             prescription.setTherapeuticLine(therapeuthicLineService.getTherapeuticLineByCode(itemresult.get("linhanome").toString()));
         } else {
             prescription.setProphylaxyFollowUp(itemresult.get("reasonforupdate").toString());
         }


        prescription.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
        prescription.setSupply((int) Float.parseFloat(itemresult.get("duration").toString()));
        prescription.setPrescriptionSeq(itemresult.get("prescriptionid").toString());
        prescription.setPatient(patient);
        if (itemresult.get("expirydate") != null)
            prescription.setExpiryDate(DateUtilities.createDate(itemresult.get("expirydate").toString(), "yyyy-MM-dd"));
        prescription.setPrescriptionDate(DateUtilities.createDate(itemresult.get("date").toString(), "yyyy-MM-dd"));
        if (itemresult.get("tipodoenca") == null) {
            prescription.setDiseaseType(diseaseTypeService.getDiseaseTypeByCode("TARV"));
        } else {
            prescription.setDiseaseType(diseaseTypeService.getDiseaseTypeByCode(Objects.requireNonNull(itemresult.get("tipodoenca")).toString()));
        }


        return prescription;

    }

    private static Dispense getDispenseOnRest(Object dispense, Prescription prescription) {
        LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) (Object) dispense;
        Dispense localDispense = new Dispense();

        localDispense.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
        localDispense.setNextPickupDate(DateUtilities.getUtilDateFromString(itemresult.get("dateexpectedstring").toString(), "dd MMM yyyy"));
        localDispense.setPickupDate(DateUtilities.createDate(itemresult.get("pickupdate").toString(), "yyyy-MM-dd"));
        localDispense.setSupply((int) Float.parseFloat(itemresult.get("weekssupply").toString()));
        localDispense.setPrescription(prescription);
        localDispense.setUuid(UUID.randomUUID().toString());

        return localDispense;
    }

    private static void saveDispensedOnRest(Object dispense, Dispense dispensed) throws SQLException {
        LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) (Object) dispense;
        IDrugService drugService = new DrugService(getApp(), null);
        IDispenseDrugService dispenseDrugService = new DispenseDrugService(getApp(), null);
        IStockService stockService = new StockService(getApp(), null);
        IClinicService clinicService = new ClinicService(getApp(), null);
        DispensedDrug dispensedDrug = new DispensedDrug();

        Clinic clinic = clinicService.getAllClinics().get(0);
        Drug localDrug = drugService.getDrugByDescription(itemresult.get("drugname").toString());
        String inHand = itemresult.get("qtyinhand").toString();

        if (!inHand.isEmpty())
            inHand = inHand.replace('(', ' ').replace(')', ' ').replaceAll("\\s+", "");
        else
            inHand = "0";

        if (localDrug != null) {

            List<Stock> stockList = stockService.getAllStocksByClinicAndDrug(clinic, localDrug);

            dispensedDrug.setDispense(dispensed);
            int quantity = Integer.parseInt(inHand);
            if (quantity > 0 && quantity < 12) {
                dispensedDrug.setQuantitySupplied(quantity);
            } else {
                dispensedDrug.setQuantitySupplied(0);
            }
            if (!stockList.isEmpty())
                dispensedDrug.setStock(stockList.get(0));
            dispensedDrug.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
            dispensedDrug.setDrug(localDrug);
            dispenseDrugService.createDispensedDrug(dispensedDrug);
        }

    }

    private static void savePrescribedDrugOnRest(Object dispense, Prescription prescription) throws SQLException {
        LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) (Object) dispense;
        DrugService drugService = new DrugService(getApp(), null);
        PrescribedDrugService prescribedDrugService = new PrescribedDrugService(getApp(), null);
        PrescribedDrug prescribedDrug = new PrescribedDrug();

        Drug localDrug = drugService.getDrugByDescription(itemresult.get("drugname").toString());

        if (localDrug != null) {
            prescribedDrug.setPrescription(prescription);
            prescribedDrug.setDrug(localDrug);
            prescribedDrugService.createPrescribedDrug(prescribedDrug);
        }
    }

    private static SyncDispense setSyncDispense(Dispense dispense, DispensedDrug dispensedDrug) {

        episodeService = new EpisodeService(getApp(), null);
        userService = new UserService(getApp(), null);
        clinicService = new ClinicService(getApp(),null);
        SyncDispense syncDispense = new SyncDispense();
        ArrayList<ClinicSector> clinicSectorList = new ArrayList<>();
        int qtySupplied = 0;

        if (dispensedDrug.getQuantitySupplied() == 0) {
            qtySupplied = ((dispense.getSupply() / 4) * 30) / dispensedDrug.getDrug().getPackSize();
        } else {
            qtySupplied = dispensedDrug.getQuantitySupplied();
        }

        try {
            Episode episode = episodeService.getAllEpisodesByPatient(dispense.getPrescription().getPatient()).get(0);
            Clinic clinic = clinicService.getAllClinics().get(0);
            clinicSectorList = (ArrayList<ClinicSector>) clinicSectorService.getClinicSectorsByClinic(clinic);

            syncDispense.setDate(dispense.getPrescription().getPrescriptionDate());
            syncDispense.setClinicalstage(0);
            syncDispense.setCurrent('T');
             if (dispense.getPrescription().getDiseaseType().getCode() == "TARV") {
                 syncDispense.setReasonforupdate("Manter");
             } else {
                 syncDispense.setReasonforupdate(dispense.getPrescription().getProphylaxyFollowUp());
             }

            syncDispense.setDuration(dispense.getPrescription().getSupply());
            syncDispense.setDoctor(1);
            syncDispense.setEnddate(null);
            syncDispense.setRegimenome(dispense.getPrescription().getTherapeuticRegimen().getDescription());
            if (dispense.getPrescription().getTherapeuticLine() != null) {
                syncDispense.setLinhanome(dispense.getPrescription().getTherapeuticLine().getDescription());
            }
            syncDispense.setNotes("Mobile Pharmacy");
            syncDispense.setMotivomudanca("");
            syncDispense.setWeight(null);
            syncDispense.setAf(Character.toUpperCase('f'));
            syncDispense.setCa(Character.toUpperCase('f'));
            syncDispense.setCcr(Character.toUpperCase('f'));
            syncDispense.setPpe(Character.toUpperCase('f'));
            syncDispense.setPtv(Character.toUpperCase('f'));
            syncDispense.setTb(Character.toUpperCase('f'));
            syncDispense.setFr(Character.toUpperCase('f'));
            syncDispense.setGaac(Character.toUpperCase('f'));
            syncDispense.setSaaj(Character.toUpperCase('f'));
            syncDispense.setTpc(Character.toUpperCase('f'));
            syncDispense.setTpi(Character.toUpperCase('f'));
            syncDispense.setPatient(dispense.getPrescription().getPatient().getId());

            if (dispense.getPrescription().getDispenseType().getDescription().contains("DT")) {
                syncDispense.setDispensatrimestral(1);
                syncDispense.setTipodt("Manuntecao");
            } else {
                syncDispense.setDispensatrimestral(0);
                syncDispense.setTipodt(null);
            }
            if (dispense.getPrescription().getDispenseType().getDescription().contains("DS")) {
                syncDispense.setDispensasemestral(1);
                syncDispense.setTipods("Manuntecao");
            } else {
                syncDispense.setDispensasemestral(0);
                syncDispense.setTipods(null);
            }
            if (dispense.getPrescription().getDiseaseType().getCode() == "TARV") {
                syncDispense.setDrugtypes("ARV");
            } else {
                syncDispense.setDrugtypes(dispense.getPrescription().getDiseaseType().getCode());
            }
            syncDispense.setDatainicionoutroservico(null);
            syncDispense.setModified('T');
            syncDispense.setPatientid(dispense.getPrescription().getPatient().getNid());
            syncDispense.setPatientfirstname(dispense.getPrescription().getPatient().getFirstName());
            syncDispense.setPatientlastname(dispense.getPrescription().getPatient().getLastName());
            syncDispense.setPickupdate(dispense.getPickupDate());
            syncDispense.setQtyinhand("(" + qtySupplied + ")");
            syncDispense.setQtyinlastbatch("(" + dispensedDrug.getQuantitySupplied() + ")");
            syncDispense.setSummaryqtyinhand("(" + dispensedDrug.getQuantitySupplied() + ")");
            syncDispense.setTimesperday(1);
            syncDispense.setWeekssupply(dispense.getPrescription().getSupply());
            syncDispense.setExpirydate(dispense.getNextPickupDate());

            syncDispense.setDateexpectedstring(DateUtilities.getStringDateFromDate(dispense.getNextPickupDate(), "dd MMM yyyy"));
            syncDispense.setDrugname(dispensedDrug.getDrug().getDescription());
            syncDispense.setDispensedate(dispense.getPickupDate());

            syncDispense.setMainclinic(0);
            syncDispense.setMainclinicname(episode.getSanitaryUnit());
            syncDispense.setMainclinicuuid(episode.getUsUuid());

            syncDispense.setSyncstatus('P');
            syncDispense.setPrescriptionid(String.valueOf(dispense.getPrescription().getPrescriptionSeq()));

            syncDispense.setDurationsentence("");
            syncDispense.setDc(Character.toUpperCase('T'));
            syncDispense.setPrep(Character.toUpperCase('f'));
            syncDispense.setCe(Character.toUpperCase('f'));
            syncDispense.setCpn(Character.toUpperCase('f'));
            syncDispense.setClinicuuid(clinicSectorList.isEmpty()? clinic.getUuid(): clinicSectorList.get(0).getUuid());
            syncDispense.setTipodoenca(dispense.getPrescription().getDiseaseType().getCode());

            List<User> users = userService.getAllUsers();

            syncDispense.setUsername(!users.isEmpty() ? users.get(0).getUserName() : StringUtils.EMPTY);
            if (!Prescription.URGENT_PRESCRIPTION.equalsIgnoreCase(dispense.getPrescription().getUrgentPrescription())) {
                syncDispense.setPrescricaoespecial(Character.toUpperCase('f'));
                syncDispense.setMotivocriacaoespecial("");
            } else {
                syncDispense.setPrescricaoespecial(Character.toUpperCase('T'));
                syncDispense.setMotivocriacaoespecial(dispense.getPrescription().getUrgentNotes());
            }

            syncDispense.setUuidopenmrs(dispense.getPrescription().getPatient().getUuid());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return syncDispense;

    }

    public static void restGetAllDispenseByPatient(Patient patient, RestResponseListener listener) throws SQLException {

        episodeService = new EpisodeService(getApp(), null);

        episodeList = episodeService.getAllEpisodesByPatient(patient);

        Episode episode = episodeList.get(episodeList.size() - 1);

        if (episode != null) {

            String url = BaseRestService.baseUrl + "/sync_temp_dispense?uuidopenmrs=eq." + patient.getUuid() + "&mainclinicuuid=eq." + episode.getUsUuid() + "&order=pickupdate.desc";

            try {
                getRestServiceExecutor().execute(() -> {

                    RESTServiceHandler handler = new RESTServiceHandler();

                    try {
                        handler.addHeader("Content-Type", "application/json");
                        handler.objectRequest(url, Request.Method.GET, null, Object[].class, new Response.Listener<Object[]>() {

                            @Override
                            public void onResponse(Object[] dispenses) {
                                try {
                                    List<Dispense> dispenseList = new ArrayList<>();
                                    if (dispenses.length > 0) {
                                        for (Object dispense : dispenses) {
                                            Log.d(TAG, "onResponse: Dispensa " + dispense);
                                            Prescription newPrescription = getPrescroptionRest(dispense, patient);
                                            Dispense d = getDispenseOnRest(dispense, newPrescription);
                                            loadDispensedDrugFromRest(dispense, d);
                                            dispenseList.add(d);
                                        }
                                        listener.doOnResponse(BaseRestService.REQUEST_SUCESS, dispenseList);
                                    } else listener.doOnResponse(REQUEST_NO_DATA, null);
                                } catch (Exception e) {
                                    listener.doOnResponse(e.getMessage(), null);
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                listener.doOnResponse(generateErrorMsg(error), null);
                                Log.d(TAG, "onErrorResponse: Erro no GET :" + generateErrorMsg(error));
                            }
                        });
                    } catch (Exception e) {
                        listener.doOnResponse(e.getMessage(), null);
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void restGetAllDispenseByPeriod(Date startDate, Date endDate, String clinicUUID, long offset, long limit, RestResponseListener listener, boolean isUsDispense) throws SQLException {
        String url;

        if (limit > 0) {
            if(isUsDispense){
                url = BaseRestService.baseUrl + "/sync_temp_dispense_vw?pickupdate=gte." + DateUtilities.formatToYYYYMMDD(startDate) +
                        "&pickupdate=lte." + DateUtilities.formatToYYYYMMDD(endDate) +
                        "&clinicuuid=eq." + clinicUUID +
                        "&prescriptionid=like._____%" +
                        "&offset=" + offset +
                        "&limit=" + limit +
                        "&order=pickupdate.desc";
            } else {
                url = BaseRestService.baseUrl + "/sync_temp_dispense_vw?pickupdate=gte." + DateUtilities.formatToYYYYMMDD(startDate) +
                        "&pickupdate=lte." + DateUtilities.formatToYYYYMMDD(endDate) +
                        "&clinicuuid=eq." + clinicUUID +
                        "&qtyinhand=neq.(30)" +
                        "&qtyinhand=neq.(90)" +
                        "&offset=" + offset +
                        "&limit=" + limit +
                        "&order=pickupdate.desc";
            }
        } else {
            if(isUsDispense){
                url = BaseRestService.baseUrl + "/sync_temp_dispense_vw?pickupdate=gte." + DateUtilities.formatToYYYYMMDD(startDate) +
                        "&pickupdate=lte." + DateUtilities.formatToYYYYMMDD(endDate) +
                        "&clinicuuid=eq." + clinicUUID +
                        "&prescriptionid=like._____%" +
                        "&order=pickupdate.desc";
            }else{
                url = BaseRestService.baseUrl + "/sync_temp_dispense_vw?pickupdate=gte." + DateUtilities.formatToYYYYMMDD(startDate) +
                        "&pickupdate=lte." + DateUtilities.formatToYYYYMMDD(endDate) +
                        "&clinicuuid=eq." + clinicUUID +
                        "&qtyinhand=neq.(30)" +
                        "&qtyinhand=neq.(90)" +
                        "&order=pickupdate.desc";
            }

        }


        Log.d(TAG, "URL " + url);
        try {
            getRestServiceExecutor().execute(() -> {

                RESTServiceHandler handler = new RESTServiceHandler();

                try {
                    handler.addHeader("Content-Type", "application/json");
                    handler.objectRequest(url, Request.Method.GET, null, Object[].class, new Response.Listener<Object[]>() {

                        @Override
                        public void onResponse(Object[] dispenses) {
                            try {
                                List<Dispense> dispenseList = new ArrayList<>();
                                if (dispenses.length > 0) {
                                    for (Object dispense : dispenses) {
//                                        Log.d(TAG, "onResponse: Dispensa " + dispense);
                                        System.out.println("dispense: "+dispense);
                                        Patient patient = getPatient(dispense);
                                        Prescription newPrescription = getPrescroptionRest(dispense, patient);
                                        Dispense d = getDispenseOnRest(dispense, newPrescription);
                                        loadDispensedDrugFromRest(dispense, d);
                                        dispenseList.add(d);
                                    }
                                    listener.doOnResponse(BaseRestService.REQUEST_SUCESS, dispenseList);
                                } else listener.doOnResponse(REQUEST_NO_DATA, null);
                            } catch (Exception e) {
                                listener.doOnResponse(e.getMessage(), null);
                                e.printStackTrace();
                            }
                        }
                    }, error -> {
                        listener.doOnResponse(generateErrorMsg(error), null);
                        Log.d(TAG, "onErrorResponse: Erro no GET :" + generateErrorMsg(error));
                    });
                } catch (Exception e) {
                    listener.doOnResponse(e.getMessage(), null);
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void restGetAllLastUSDispense(RestResponseListener listener, long offset, long limit) throws SQLException {

        dispenseService = new DispenseService(getApp(), null);
        dispenseDrugService = new DispenseDrugService(getApp(), null);
        prescriptionService = new PrescriptionService(getApp(), null);
        episodeService = new EpisodeService(getApp(), null);
        patientService = new PatientService(getApp());
        clinicService = new ClinicService(getApp(), null);

        Clinic clinic = clinicService.getAllClinics().get(0);
        String url = BaseRestService.baseUrl + "/patient_us_sync_tmp_dispense_vw?clinicuuid=eq." + clinic.getUuid() + "&syncstatus=eq." + 'N' + "&offset=" + offset + "&limit=" + limit;

        try {
            getRestServiceExecutor().execute(() -> {

                RESTServiceHandler handler = new RESTServiceHandler();

                try {
                    handler.addHeader("Content-Type", "application/json");
                    handler.objectRequest(url, Request.Method.GET, null, Object[].class, (Response.Listener<Object[]>) dispenses -> {
                        try {
                            if (dispenses.length > 0) {
                                List<Dispense> dispenseList = new ArrayList<>();
                                for (Object dispense : dispenses) {
                                    Log.d(TAG, "onResponse: Dispensa " + dispense);


                                    LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) (Object) dispense;

                                    Patient patient = patientService.getPatientByUuid(itemresult.get("uuidopenmrs").toString());
                                    if (patient != null) {
                                        patient.setEpisodes(episodeService.getAllEpisodesByPatient(patient));

                                        if (patient.hasEpisode() && !patient.hasEndEpisode()) {
                                            Prescription newPrescription = getPrescroptionRest(dispense, patient);
                                            Prescription lastPrescription = prescriptionService.getLastPatientPrescriptionByDiseaseType(patient, newPrescription.getDiseaseType());

                                            if (lastPrescription != null) {
                                                if ((int) DateUtilities.dateDiff(newPrescription.getPrescriptionDate(), lastPrescription.getPrescriptionDate(), DateUtilities.DAY_FORMAT) > 0) {
                                                    prescriptionService.createPrescription(newPrescription);
                                                    savePrescribedDrugOnRest(dispense, newPrescription);
                                                    lastPrescription.setExpiryDate(newPrescription.getPrescriptionDate());
                                                    prescriptionService.updatePrescriptionEntity(lastPrescription);
                                                } else if ((int) DateUtilities.dateDiff(newPrescription.getPrescriptionDate(), lastPrescription.getPrescriptionDate(), DateUtilities.DAY_FORMAT) == 0) {
                                                    newPrescription = lastPrescription;
                                                } else {
                                                    break;
                                                }
                                            }

                                            Dispense newDispense = getDispenseOnRest(dispense, newPrescription);
                                            Dispense lastDispense = dispenseService.getLastDispenseFromPrescription(newPrescription);

                                            if (lastDispense != null) {
                                                if ((int) DateUtilities.dateDiff(newDispense.getPickupDate(), lastDispense.getPickupDate(), DateUtilities.DAY_FORMAT) > 0) {
                                                    dispenseService.createDispense(newDispense);
                                                    saveDispensedOnRest(dispense, newDispense);
                                                } else {
                                                    break;
                                                }
                                            } else {
                                                dispenseService.createDispense(newDispense);
                                                saveDispensedOnRest(dispense, newDispense);
                                            }
                                            restPatchSyncStatus(String.valueOf(Double.valueOf(itemresult.get("id").toString()).intValue()), 'S');
                                            dispenseList.add(newDispense);

                                        }
                                    }
                                }
                                if (listener != null)
                                    listener.doOnResponse(BaseRestService.REQUEST_SUCESS, dispenseList);
                            } else {
                                listener.doOnResponse(REQUEST_NO_DATA, null);
                                Log.w(TAG, "Response Sem Info." + dispenses.length);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            listener.doOnResponse(generateErrorMsg(error), null);
                            Log.d(TAG, "onErrorResponse: Erro no GET :" + generateErrorMsg(error));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void restPatchSyncStatus(String syncTempDispenseId, Character syncStatus) {

        String url = BaseRestService.baseUrl + "/sync_temp_dispense?id=eq." + syncTempDispenseId;

        episodeService = new EpisodeService(getApp());


        if (syncTempDispenseId != null) {


            try {
                SyncDispense syncDispense = new SyncDispense();
                syncDispense.setSyncstatus(syncStatus);

                getRestServiceExecutor().execute(() -> {

                    RESTServiceHandler handler = new RESTServiceHandler();
                    try {
                        Gson g = new Gson();
                        String restObject = g.toJson(syncDispense);

                        handler.addHeader("Content-Type", "application/json");
                        JSONObject jsonObject = new JSONObject(restObject);
                        handler.objectRequest(url, Request.Method.PATCH, jsonObject, Object[].class, (Response.Listener<TreeMap<String, Object>>) response -> {
                            Log.d(TAG, "onResponse: Dispensa actualizada : " + response);

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

    private static void loadDispensedDrugFromRest(Object restDispense, Dispense dispense) throws SQLException {
        LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) (Object) restDispense;
        DrugService drugService = new DrugService(getApp(), null);
        DispensedDrug dispensedDrug = new DispensedDrug();

        Drug localDrug = drugService.getDrugByDescription(itemresult.get("drugname").toString());

        if (localDrug != null) {
            dispensedDrug.setDispense(dispense);
            dispensedDrug.setStock(new Stock());
            dispensedDrug.setDrug(localDrug);
            dispensedDrug.getStock().setDrug(localDrug);
            dispensedDrug.setQuantitySupplied(Integer.valueOf(itemresult.get("qtyinhand").toString().substring(1, 2)));
            if (!Utilities.listHasElements(dispense.getDispensedDrugs()))
                dispense.setDispensedDrugs(new ArrayList<>());
            dispense.getDispensedDrugs().add(dispensedDrug);
        }
    }

    private static Patient getPatient(Object dispense) throws SQLException {
        LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) (Object) dispense;
        PatientService patientService = new PatientService(getApp(), null);
        episodeService = new EpisodeService(getApp(), null);
        Patient patient = new Patient();
        if(itemresult.get("uuidopenmrs").toString().equalsIgnoreCase("3c3a032f-58ef-4632-92f7-4047f15773dc") || itemresult.get("uuidopenmrs").toString().equalsIgnoreCase("e605e933-6066-40ba-b841-d7e64fe7eaad") || itemresult.get("uuidopenmrs").toString().equalsIgnoreCase("13ffd84e-c69e-4881-a30a-32e957f83292")){
            patient = patientService.getPatientByUuid("486044ee-b4df-4b99-8514-e49349d41dba");
        } else {
            patient = patientService.getPatientByUuid(itemresult.get("uuidopenmrs").toString());
        }
        System.out.println(patient);
        List<Episode> episodeList = episodeService.getAllEpisodesByPatient(patient);
        patient.getEpisodes1().add(episodeList.get(0));
        return patient;
    }

}