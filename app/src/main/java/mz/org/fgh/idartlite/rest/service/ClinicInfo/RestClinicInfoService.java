package mz.org.fgh.idartlite.rest.service.ClinicInfo;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.rest.BaseRestService;
import mz.org.fgh.idartlite.base.rest.ServiceWatcher;
import mz.org.fgh.idartlite.listener.rest.RestResponseListener;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.ClinicInformation;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.Episode;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.model.SyncClinicInformation;
import mz.org.fgh.idartlite.model.User;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.rest.helper.RESTServiceHandler;
import mz.org.fgh.idartlite.service.clinic.ClinicService;
import mz.org.fgh.idartlite.service.clinic.IClinicService;
import mz.org.fgh.idartlite.service.clinicInfo.ClinicInfoService;
import mz.org.fgh.idartlite.service.clinicInfo.IClinicInfoService;
import mz.org.fgh.idartlite.service.dispense.DispenseDrugService;
import mz.org.fgh.idartlite.service.dispense.DispenseService;
import mz.org.fgh.idartlite.service.dispense.DispenseTypeService;
import mz.org.fgh.idartlite.service.drug.TherapeuthicLineService;
import mz.org.fgh.idartlite.service.drug.TherapheuticRegimenService;
import mz.org.fgh.idartlite.service.episode.EpisodeService;
import mz.org.fgh.idartlite.service.episode.IEpisodeService;
import mz.org.fgh.idartlite.service.prescription.PrescriptionService;
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.Utilities;

public class RestClinicInfoService extends BaseRestService {

    private static final String TAG = "RestClinicInfoService";

    private static IClinicService clinicService;
    private static IClinicInfoService episodeService;
    private static IEpisodeService realEpisodeService;
    private static List<ClinicInformation> clinicInformationsList;
    private static List<Episode> episodeList;


    public RestClinicInfoService(Application application, User currentUser) {
        super(application, currentUser);
        clinicService = new ClinicService(application, currentUser);
    }

    public static void restPostClinicInfo(RestResponseListener listener,ClinicInformation clinicInformation) {

        String url = BaseRestService.baseUrl + "/sync_temp_clinic_information";

        ClinicInfoService clinicInfoService = new ClinicInfoService(getApp(), null);
        getRestServiceExecutor().execute(() -> {
            RESTServiceHandler handler = new RESTServiceHandler();


            SyncClinicInformation syncClinicInformation = setSyncClinicInfo(clinicInformation);
            Gson g = new Gson();
            String restObject = g.toJson(syncClinicInformation);

            handler.addHeader("Content-Type", "application/json");
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(restObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            handler.objectRequest(url, Request.Method.POST, jsonObject, Object.class, new Response.Listener<Object>() {

                @Override
                public void onResponse(Object response) {
                    Log.d(TAG, "onResponse: Informacao Clinica enviada" + response);
                    try {
                        clinicInformation.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
                        clinicInfoService.updateClinicInfo(clinicInformation);
                        listener.doOnResponse(BaseRestService.REQUEST_SUCESS, null);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    listener.doOnResponse(REQUEST_ERROR, null);
                    Log.d(TAG, "onErrorResponse: Erro no POST :" + generateErrorMsg(error));
                }
            });


        });
    }

    public static void getRestLastClinicInfo(Patient patient) throws SQLException {

        ClinicInfoService clinicInfoService = new ClinicInfoService(getApp(), null);

        clinicInformationsList = clinicInfoService.getAllClinicInfosByPatient(patient);

        realEpisodeService = new EpisodeService(getApp(), null);

        episodeList = realEpisodeService.getAllEpisodesByPatient(patient);

        if (Utilities.listHasElements(episodeList)) {
        Episode episode = episodeList.get(episodeList.size() - 1);


        if (episode != null) {

            String url = BaseRestService.baseUrl + "/sync_temp_clinic_information?patientuuid=eq." + patient.getUuid() + "&order=registerdate.desc";

            try {
                getRestServiceExecutor().execute(() -> {

                    RESTServiceHandler handler = new RESTServiceHandler();

                    try {
                        handler.addHeader("Content-Type", "application/json");
                        handler.objectRequest(url, Request.Method.GET, null, Object[].class, new Response.Listener<Object[]>() {

                            @SuppressLint("SuspiciousIndentation")
                            @Override
                            public void onResponse(Object[] clinicInformList) {
                                try {
                                    if (clinicInformList.length > 0) {
                                        for (Object clinicInfo : clinicInformList) {
                                            Log.d(TAG, "onResponse: clinicInformation " + clinicInfo);

                                            ClinicInformation newClinicInformation = setRestNewClinicInformation(clinicInfo, patient);
                                            ClinicInformation lastClinicInformation = clinicInfoService.getLastPatientClinicInformation(patient);

                                            if(lastClinicInformation != null && newClinicInformation != null) {
                                                if ((int) DateUtilities.dateDiff(newClinicInformation.getRegisterDate(), lastClinicInformation.getRegisterDate(), DateUtilities.DAY_FORMAT) > 0) {
                                                    clinicInfoService.createClinicInfo(newClinicInformation);
                                                } else {
                                                    break;
                                                }
                                            }else {
                                                if(newClinicInformation != null)
                                                    clinicInfoService.createClinicInfo(newClinicInformation);
                                            }
                                        }
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
    }

    public static void restPostAllClinicInfo(RestResponseListener listener) throws SQLException {
        ClinicInfoService clinicInfoService = new ClinicInfoService(getApp(), null);
        List<ClinicInformation> clinicInformationList= null;
            clinicInformationList = clinicInfoService.getAllClinicInfoByStatus(BaseModel.SYNC_SATUS_READY);

        if(clinicInformationList != null && clinicInformationList.size() > 0) {
            for (ClinicInformation clinicInformation : clinicInformationList) {
                RestClinicInfoService.restPostClinicInfo(listener, clinicInformation);
            }
            listener.doOnResponse(BaseRestService.REQUEST_SUCESS, clinicInformationList);
        } else {
            listener.doOnResponse(BaseRestService.REQUEST_NO_DATA, null);
        }

    }


    private static SyncClinicInformation setSyncClinicInfo(ClinicInformation clinicInformation) {


        //episodeService = new EpisodeService(getApp(), null);
        SyncClinicInformation syncClinicInformation = new SyncClinicInformation();

        syncClinicInformation.setRegisterdate(clinicInformation.getRegisterDate());
        syncClinicInformation.setWeight(clinicInformation.getWeight());
        syncClinicInformation.setHeight(clinicInformation.getHeight());
        syncClinicInformation.setImc(clinicInformation.getImc());
        syncClinicInformation.setSystole(clinicInformation.getSystole());
        syncClinicInformation.setDistort(clinicInformation.getDistort());
        syncClinicInformation.setIstreatmenttb(clinicInformation.isTreatmentTB());
        syncClinicInformation.setIstreatmenttpi(clinicInformation.isTreatmentTPI());
        syncClinicInformation.setIscough(clinicInformation.isCough());
        syncClinicInformation.setIsfever(clinicInformation.isFever());
        syncClinicInformation.setIslostweight(clinicInformation.isLostWeight());
        syncClinicInformation.setIssweating(clinicInformation.isSweating());
        syncClinicInformation.setHasparenttbtreatment(clinicInformation.isHasParentTBTreatment());
        syncClinicInformation.setIsreferedtoustb(clinicInformation.isReferedToUsTB());
        syncClinicInformation.setHaspatientcamecorrectdate(clinicInformation.isHasPatientCameCorrectDate());
        syncClinicInformation.setLatedays(clinicInformation.getLateDays());
        syncClinicInformation.setPatientforgotmedicine(clinicInformation.isPatientForgotMedicine());
        syncClinicInformation.setDayswithoutmedicine(clinicInformation.getDaysWithoutMedicine());
        syncClinicInformation.setLatemotives(clinicInformation.getLateMotives());
        syncClinicInformation.setAdversereactionofmedicine(clinicInformation.isAdverseReactionOfMedicine());
        syncClinicInformation.setAdversereaction(clinicInformation.getAdverseReaction());
        syncClinicInformation.setIsreferedtousram(clinicInformation.isReferedToUsRAM());
        syncClinicInformation.setPatientuuid(clinicInformation.getPatient().getUuid());
        syncClinicInformation.setUuid(clinicInformation.getUuid());
        syncClinicInformation.setSyncstatus('S');
        //  syncClinicInformation.setId((int)Math.floor(Math.random()*(max-min+1)+min));
        syncClinicInformation.setHasfatigueortiredneslasttwoweeks(clinicInformation.isHasFatigueOrTirednesLastTwoWeeks());
        syncClinicInformation.setHashadmenstruationlasttwomonths(clinicInformation.isHasHadMenstruationLastTwoMonths());
        syncClinicInformation.setIspregnant(clinicInformation.isPregnant());

        if (clinicInformation.getStartTreatmentDate() != null) {
            syncClinicInformation.setStarttreatmentdate(clinicInformation.getStartTreatmentDate());
        }


        return syncClinicInformation;

    }

    private static ClinicInformation setRestNewClinicInformation(Object clinicInformationObject, Patient patient) throws SQLException {

        LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) (Object) clinicInformationObject;

        ClinicInformation clinicInformation = new ClinicInformation();

        int weigth = (int) Float.parseFloat(itemresult.get("weight").toString());
        int height = (int) Float.parseFloat(itemresult.get("height").toString());
        String imc = (String) itemresult.get("imc");
        int distort = (int) Float.parseFloat(itemresult.get("distort").toString());
        int systole = (int) Float.parseFloat(itemresult.get("systole").toString());
        boolean istreatmenttpi = (boolean) itemresult.get("istreatmenttpi");
        boolean istreatmenttb = (boolean) itemresult.get("istreatmenttb");
        boolean iscough = (boolean) itemresult.get("iscough");
        boolean isfever = (boolean) itemresult.get("isfever");
        boolean islostweight = (boolean) itemresult.get("islostweight");
        boolean issweating = (boolean) itemresult.get("issweating");
        boolean hasfatigueortiredneslasttwoweeks = (boolean) itemresult.get("hasfatigueortiredneslasttwoweeks");
        boolean hasparenttbtreatment = (boolean) itemresult.get("hasparenttbtreatment");
        boolean isreferedtoustb = (boolean) itemresult.get("isreferedtoustb");
        boolean haspatientcamecorrectdate = (boolean) itemresult.get("haspatientcamecorrectdate");
        int latedays = (int) Float.parseFloat(itemresult.get("latedays").toString());
        boolean patientforgotmedicine = (boolean) itemresult.get("patientforgotmedicine");
        int dayswithoutmedicine = (int) Float.parseFloat(itemresult.get("dayswithoutmedicine").toString());
        String latemotives = (String) itemresult.get("latemotives");
        boolean adversereactionofmedicine = (boolean) itemresult.get("adversereactionofmedicine");
        String adversereaction = (String) itemresult.get("adversereaction");
        boolean isreferedtousram = (boolean) itemresult.get("isreferedtousram");
        boolean ispregnant = (boolean) itemresult.get("ispregnant");
        boolean hashadmenstruationlasttwomonths = (boolean) itemresult.get("hashadmenstruationlasttwomonths");
        String patientuuid = (String) itemresult.get("patientuuid");
        String syncstatus = (String) itemresult.get("syncstatus");
//        String usuuid = (Integer) itemresult.get("usuuid");
//        String uuid = (Integer) itemresult.get("uuid");
//        String clinicuuid = (Integer) itemresult.get("clinicuuid");
    try {
        clinicInformation.setRegisterDate(DateUtilities.createDate(itemresult.get("registerdate").toString(), "yyyy-MM-dd"));
        clinicInformation.setStartTreatmentDate(itemresult.get("starttreatmentdate") != null ? DateUtilities.createDate(itemresult.get("starttreatmentdate").toString(), "yyyy-MM-dd") : null);
        clinicInformation.setWeight(weigth);
        clinicInformation.setHeight(height);
        clinicInformation.setImc(imc);
        clinicInformation.setDistort(distort);
        clinicInformation.setSystole(systole);
        clinicInformation.setTreatmentTPI(istreatmenttpi);
        clinicInformation.setTreatmentTB(istreatmenttb);
        clinicInformation.setCough(iscough);
        clinicInformation.setFever(isfever);
        clinicInformation.setLostWeight(islostweight);
        clinicInformation.setSweating(issweating);
        clinicInformation.setHasFatigueOrTirednesLastTwoWeeks(hasfatigueortiredneslasttwoweeks);
        clinicInformation.setHasParentTBTreatment(hasparenttbtreatment);
        clinicInformation.setReferedToUsTB(isreferedtoustb);
        clinicInformation.setHasPatientCameCorrectDate(haspatientcamecorrectdate);
        clinicInformation.setLateDays(latedays);
        clinicInformation.setPatientForgotMedicine(patientforgotmedicine);
        clinicInformation.setDaysWithoutMedicine(dayswithoutmedicine);
        clinicInformation.setLateMotives(latemotives);
        clinicInformation.setAdverseReactionOfMedicine(adversereactionofmedicine);
        clinicInformation.setAdverseReaction(adversereaction);
        clinicInformation.setReferedToUsRAM(isreferedtousram);
        clinicInformation.setPregnant(ispregnant);
        clinicInformation.setHasHadMenstruationLastTwoMonths(hashadmenstruationlasttwomonths);
        clinicInformation.setPatient(patient);
        clinicInformation.setUuid(patientuuid);
        clinicInformation.setSyncStatus(syncstatus);
    }catch (Exception e){
        e.printStackTrace();
    }
        return clinicInformation;

    }


}
