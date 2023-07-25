package mz.org.fgh.idartlite.workSchedule.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.service.BaseService;
import mz.org.fgh.idartlite.base.viewModel.BaseViewModel;
import mz.org.fgh.idartlite.model.ClinicInformation;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.Episode;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.model.patient.PatientAttribute;
import mz.org.fgh.idartlite.rest.service.ClinicInfo.RestClinicInfoService;
import mz.org.fgh.idartlite.rest.service.Dispense.RestDispenseService;
import mz.org.fgh.idartlite.rest.service.Episode.RestEpisodeService;
import mz.org.fgh.idartlite.rest.service.Patient.RestPatientService;
import mz.org.fgh.idartlite.rest.service.Stock.RestStockService;
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
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.SecurePreferences;

public class DataSyncWorker extends Worker {

    public static final String TAG = "data_upload_job";
    private static final String PROGRESS = "PROGRESS";
    private static final long DELAY = 6000;
    private static final String SETTINGS_PREF = "settings_pref";
    private static final String APP_LAST_SYNC_DATE = "app_last_sync";

    public DataSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        IDispenseService dispenseService = new DispenseService(BaseService.getApp(), null);
        IStockService stockService = new StockService(BaseService.getApp(), null);
        IEpisodeService episodeService = new EpisodeService(BaseService.getApp(), null);
        ICountryService countryService = new CountryService(BaseService.getApp(), null);

        IClinicService clinicService = new ClinicService(BaseService.getApp(), null);
        IPatientService patientService = new PatientService(BaseService.getApp(), null);
        IClinicSectorService clinicSectorService = new ClinicSectorService(BaseService.getApp(), null);
        IClinicInfoService clinicInfoService = new ClinicInfoService(BaseService.getApp(), null);
        BaseViewModel baseViewModel = null;

        List<Stock> stockList;
        List<Dispense> dispenseList;
        List<Patient> patientList;
        Episode episode;

        try {
            RestEpisodeService.restGetAllReadyEpisodes();
            RestEpisodeService.restGetAllEpisodes();

           try {
                stockList = stockService.getStockByStatus(BaseModel.SYNC_SATUS_READY);
                if (stockList != null)
                    if (stockList.size() > 0) {

                        for (Stock stock : stockList) {
                            RestStockService.restPostStock(stock);
                        }
                    }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                stockList = stockService.getStockByStatus(BaseModel.SYNC_SATUS_UPDATED);
                if (stockList != null)
                    if (stockList.size() > 0) {

                        for (Stock stock : stockList) {
                            RestStockService.restGetAndPatchStockLevel(stock);
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
                            RestDispenseService.restPostDispense(dispense);
                        }
                    }
            } catch (SQLException e) {
                e.printStackTrace();
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

           /* try {
                patientList = patientService.getALLPatient();
                if (patientList != null)
                    if (patientList.size() > 0) {
                        for (Patient patient : patientList) {
                            episode = episodeService.findEpisodeWithStopReasonByPatient(patient);
                            if (episode == null)
                                RestDispenseService.restGetLastDispense(patient);
                                RestClinicInfoService.getRestLastClinicInfo(patient);
                        }
                    }
            } catch (SQLException e) {
                e.printStackTrace();
            }*/

            try {

                List<ClinicInformation> clinicInformationList = clinicInfoService.getAllClinicInfoByStatus(BaseModel.SYNC_SATUS_READY);
                if (clinicInformationList != null && clinicInformationList.size() > 0) {
                    for (ClinicInformation clinicInformation : clinicInformationList) {
                        RestClinicInfoService.restPostClinicInfo(null,clinicInformation);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }

        SecurePreferences settingsPreferences = new SecurePreferences(BaseService.getApp(), SETTINGS_PREF,  true);
        settingsPreferences.put(APP_LAST_SYNC_DATE, DateUtilities.formatToDDMMYYYY_HHMISS(DateUtilities.getCurrentDate()));

        //Data outputData = new Data.Builder().putString(UPLOAD_MESSAGE_STATUS, watcher.getUpdates()).build();

        return Result.success();
    }
}
