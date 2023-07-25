package mz.org.fgh.idartlite.rest.service;

import android.app.Application;
import android.app.Notification;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.base.application.IdartLiteApplication;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.rest.BaseRestService;
import mz.org.fgh.idartlite.base.rest.ServiceWatcher;
import mz.org.fgh.idartlite.listener.rest.RestResponseListener;
import mz.org.fgh.idartlite.model.ClinicInformation;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.Episode;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.model.User;
import mz.org.fgh.idartlite.model.patient.PatientAttribute;
import mz.org.fgh.idartlite.rest.service.ClinicInfo.RestClinicInfoService;
import mz.org.fgh.idartlite.rest.service.Disease.RestDiseaseTypeService;
import mz.org.fgh.idartlite.rest.service.Dispense.RestDispenseService;
import mz.org.fgh.idartlite.rest.service.Dispense.RestDispenseTypeService;
import mz.org.fgh.idartlite.rest.service.Drug.RestDrugService;
import mz.org.fgh.idartlite.rest.service.Episode.RestEpisodeService;
import mz.org.fgh.idartlite.rest.service.Form.RestFormService;
import mz.org.fgh.idartlite.rest.service.Patient.RestPatientService;
import mz.org.fgh.idartlite.rest.service.Regimen.RestTherapeuticRegimenService;
import mz.org.fgh.idartlite.rest.service.Stock.RestStockService;
import mz.org.fgh.idartlite.rest.service.TherapeuticLine.RestTherapeuticLineService;
import mz.org.fgh.idartlite.rest.service.clinic.RestPharmacyTypeService;
import mz.org.fgh.idartlite.savelogs.SaveLogsInStorage;
import mz.org.fgh.idartlite.service.clinic.ClinicSectorService;
import mz.org.fgh.idartlite.service.clinic.ClinicService;
import mz.org.fgh.idartlite.service.clinic.IClinicSectorService;
import mz.org.fgh.idartlite.service.clinic.IClinicService;
import mz.org.fgh.idartlite.service.clinicInfo.ClinicInfoService;
import mz.org.fgh.idartlite.service.clinicInfo.IClinicInfoService;
import mz.org.fgh.idartlite.service.dispense.DispenseService;
import mz.org.fgh.idartlite.service.dispense.IDispenseService;
import mz.org.fgh.idartlite.service.episode.EpisodeService;
import mz.org.fgh.idartlite.service.episode.IEpisodeService;
import mz.org.fgh.idartlite.service.patient.IPatientService;
import mz.org.fgh.idartlite.service.patient.PatientService;
import mz.org.fgh.idartlite.service.stock.IStockService;
import mz.org.fgh.idartlite.service.stock.StockService;
import mz.org.fgh.idartlite.service.territory.CountryService;
import mz.org.fgh.idartlite.service.territory.ICountryService;
import mz.org.fgh.idartlite.util.Utilities;

import static mz.org.fgh.idartlite.base.application.IdartLiteApplication.CHANNEL_1_ID;
import static mz.org.fgh.idartlite.base.application.IdartLiteApplication.CHANNEL_2_ID;
import static mz.org.fgh.idartlite.savelogs.PathConstant.LOGGER_FILE_PATH;

public class RestRunDataForTestService extends BaseRestService implements RestResponseListener {

    private List<ServiceWatcher> serviceWatcherList;
    private static final String TAG = "RestRunDataForTestService";

    private NotificationManagerCompat notificationManager;

    public RestRunDataForTestService(Application application, User currentUser) {
        super(application, currentUser);

        notificationManager = NotificationManagerCompat.from(getApp());


      /*  try{
          List<ClinicSector>  clinicSectors= clinicSectorService.getClinicSectorsByClinic(clinicService.getAllClinics().get(0));

            if(clinicSectors.isEmpty()){
                ClinicSector clinicSector=new ClinicSector();
                clinicSector.setClinicId(clinicService.getAllClinics().get(0).getId());
                clinicSector.setCode("001");
                clinicSector.setPhone("84464422");
                clinicSector.setSectorName("Paragem Unic HIV");
                clinicSector.setUuid(Utilities.getNewUUID().toString());

                clinicSectorService.saveClinicSector(clinicSector);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
    }

    public void runDataSync() {
        SaveLogsInStorage log = SaveLogsInStorage.getSaveLoggerInstance(IdartLiteApplication.getInstance().getApplicationContext(), LOGGER_FILE_PATH);
        IDispenseService dispenseService = new DispenseService(application, currentUser);
        IStockService stockService = new StockService(application, currentUser);
        IEpisodeService episodeService = new EpisodeService(application, currentUser);
        ICountryService countryService = new CountryService(application, currentUser);

        IClinicService clinicService = new ClinicService(application, currentUser);
        IPatientService patientService = new PatientService(application, currentUser);
        IClinicSectorService clinicSectorService = new ClinicSectorService(application, currentUser);
        IClinicInfoService clinicInfoService = new ClinicInfoService(application, currentUser);
        List<Stock> stockList;
        List<Dispense> dispenseList;
        List<Patient> patientList;
        Episode episode;

        RestTerritoryService.restGetAllCountries();
        RestTerritoryService.restGetAllProvinces();
        RestTerritoryService.restGetAllDistricts();

        RestPharmacyTypeService.restGetAllPharmacyType();
        RestFormService.restGetAllForms();
        RestDrugService.restGetAllDrugs();
        RestDiseaseTypeService.restGetAllDiseaseType();
        RestDispenseTypeService.restGetAllDispenseType();
        RestTherapeuticRegimenService.restGetAllTherapeuticRegimen();
        RestTherapeuticLineService.restGetAllTherapeuticLine();
        //RestPatientService.restGetAllPatient();
        RestEpisodeService.restGetAllReadyEpisodes();
        RestEpisodeService.restGetAllEpisodes();

       /* try {
            RestStockService.restGetStock(clinicService.getAllClinics().get(0));
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        try {
            stockList = stockService.getStockByStatus(BaseModel.SYNC_SATUS_READY);
            if (stockList != null)
                if (stockList.size() > 0) {
                    serviceWatcherList.get(serviceWatcherList.size() - 1).setServiceAsRunning();

                    for (Stock stock : stockList) {
                        RestStockService.restPostStock(stock, serviceWatcherList.get(serviceWatcherList.size() - 1), RestRunDataForTestService.this);
                    }
                }
        } catch (SQLException e) {
            log.saveErrorLogs(TAG, "Ocorreu um erro ao Buscar Pacientes: " + e);
        }

        try {
            stockList = stockService.getStockByStatus(BaseModel.SYNC_SATUS_UPDATED);
            if (stockList != null)
                if (stockList.size() > 0) {
                    serviceWatcherList.get(serviceWatcherList.size() - 1).setServiceAsRunning();

                    for (Stock stock : stockList) {
                        RestStockService.restGetAndPatchStockLevel(stock, serviceWatcherList.get(serviceWatcherList.size() - 1), RestRunDataForTestService.this);
                    }
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            dispenseList = dispenseService.getAllDispensesByStatus(BaseModel.SYNC_SATUS_READY);
            if (dispenseList != null)
                if (dispenseList.size() > 0) {
                    for (Dispense dispense : dispenseList) {
                        try {
                            RestDispenseService.restPostDispense(dispense);
                        } catch (Exception e) {
                            //Do Nothing continue
                        }
                    }
                }
        } catch (SQLException e) {
            e.printStackTrace();
            //continue();
        }

        try {
            List<Episode> episodeList = episodeService.getAllEpisodeByStatusAndDispenseStatus(BaseModel.SYNC_SATUS_READY, PatientAttribute.PATIENT_DISPENSATION_NORMAL);
            if (episodeList != null && episodeList.size() > 0) {
                for (Episode episode1 : episodeList) {
                    RestEpisodeService.restPostEpisode(episode1);
                    RestPatientService.restPostPatientSector(episode1.getPatient());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            patientList = patientService.getALLPatient();
            if (patientList != null)
                if (patientList.size() > 0) {
                    for (Patient patient : patientList) {
                        episode = episodeService.findEpisodeWithStopReasonByPatient(patient);
                        if (episode == null) {
                            RestDispenseService.restGetLastDispense(patient);
                            RestClinicInfoService.getRestLastClinicInfo(patient);
                        }
                    }
                }
        } catch (Exception e) {
            log.saveErrorLogs(TAG, "Ocorreu um erro ao Buscar Pacientes: \n " + e);
        }

        try {

            List<ClinicInformation> clinicInformationList = clinicInfoService.getAllClinicInfoByStatus(BaseModel.SYNC_SATUS_READY);
            if (clinicInformationList != null && clinicInformationList.size() > 0) {
                for (ClinicInformation clinicInformation : clinicInformationList) {
                    RestClinicInfoService.restPostClinicInfo(null,clinicInformation);
                    //    RestPatientService.restPostPatientSector(clinicInformation.getPatient());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private void dysplayDownloadResultNotification() {
        String contentText = null;

        for (ServiceWatcher watcher : serviceWatcherList) {
            if (!watcher.isUploadService() && Utilities.stringHasValue(watcher.getUpdates()))
                contentText = Utilities.stringHasValue(contentText) ? contentText + '\n' + watcher.getUpdates() : watcher.getUpdates();
        }

        if (!Utilities.stringHasValue(contentText))
            contentText = "Não foram encontrados dados novos para sincronizar.";

        Notification builder = new NotificationCompat.Builder(getApp(), CHANNEL_1_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("iDART MOBILE")
                .setContentText(contentText)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .build();

        notificationManager.notify(1, builder);
    }

    private boolean hasRunningDownloadService() {
        for (ServiceWatcher watcher : this.serviceWatcherList) {
            if (!watcher.isUploadService() && watcher.isRunning()) return true;
        }
        return false;
    }

    public void runMetaDataSync() {

    }

    @Override
    public void doOnRestSucessResponse(String flag) {

    }

    @Override
    public void doOnRestErrorResponse(String errormsg) {

    }

    @Override
    public void doOnRestSucessResponseObjects(String flag, List objects) {

    }

    private boolean hasUploadRunning() {
        for (ServiceWatcher watcher : this.serviceWatcherList) {
            if (watcher.isUploadService() && watcher.isRunning()) return true;
        }
        return false;
    }

    private void dysplayUploadResultNotification() {
        String contentText = null;

        for (ServiceWatcher watcher : serviceWatcherList) {
            if (watcher.isUploadService() && Utilities.stringHasValue(watcher.getUpdates()))
                contentText = Utilities.stringHasValue(contentText) ? contentText + '\n' + watcher.getUpdates() : watcher.getUpdates();
        }

        if (!Utilities.stringHasValue(contentText)) contentText = "Não foram enviados dados.";

        Notification builder = new NotificationCompat.Builder(getApp(), CHANNEL_2_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("iDART MOBILE UPLOAD")
                .setContentText(contentText)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .build();

        notificationManager.notify(1, builder);
    }
}
