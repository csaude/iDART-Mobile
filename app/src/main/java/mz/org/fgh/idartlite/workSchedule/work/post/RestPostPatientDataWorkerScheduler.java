package mz.org.fgh.idartlite.workSchedule.work.post;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.rest.BaseRestService;
import mz.org.fgh.idartlite.model.ClinicInformation;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.Episode;
import mz.org.fgh.idartlite.model.patient.PatientAttribute;
import mz.org.fgh.idartlite.rest.helper.RESTServiceHandler;
import mz.org.fgh.idartlite.rest.service.ClinicInfo.RestClinicInfoService;
import mz.org.fgh.idartlite.rest.service.Dispense.RestDispenseService;
import mz.org.fgh.idartlite.rest.service.Episode.RestEpisodeService;
import mz.org.fgh.idartlite.service.clinicInfo.ClinicInfoService;
import mz.org.fgh.idartlite.service.dispense.DispenseService;
import mz.org.fgh.idartlite.service.episode.EpisodeService;

public class RestPostPatientDataWorkerScheduler extends Worker {

    private static final String TAG = "RestPostWorkerScheduler";
    private List<Dispense> dispenseList;
    
    protected DispenseService dispenseService;

    protected EpisodeService episodeService;

    protected ClinicInfoService clinicInfoService;
    
    public RestPostPatientDataWorkerScheduler(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            dispenseService = new DispenseService(BaseRestService.getApp(),null);
            episodeService = new EpisodeService(BaseRestService.getApp(),null);
            clinicInfoService = new ClinicInfoService(BaseRestService.getApp(),null);
            if (RESTServiceHandler.getServerStatus(BaseRestService.baseUrl)) {
                Log.d(TAG, "doWork: Sync Patient Data");
                dispenseList = dispenseService.getAllDispensesByStatus(BaseModel.SYNC_SATUS_READY);
                if(dispenseList != null)
                if(dispenseList.size() > 0) {
                    for (Dispense dispense : dispenseList) {
                        RestDispenseService.restPostDispense(dispense);
                    }
                }else {Log.d(TAG, "doWork: Sem Dispensas para syncronizar");}

            List<Episode> episodeList = episodeService.getAllEpisodeByStatusAndDispenseStatus(BaseModel.SYNC_SATUS_READY, PatientAttribute.PATIENT_DISPENSATION_NORMAL);
            if(episodeList != null && episodeList.size() > 0) {
                for (Episode episode : episodeList) {
                    RestEpisodeService.restPostEpisode(episode);
                }
            }
                else {
                    Log.d(TAG, "doWork: Sem Episodios para syncronizar");
                }

                List<ClinicInformation> clinicInformationList=clinicInfoService.getAllClinicInfoByStatus(BaseModel.SYNC_SATUS_READY);
                if(clinicInformationList != null && clinicInformationList.size() > 0) {
                    for (ClinicInformation clinicInformation : clinicInformationList) {
                        RestClinicInfoService.restPostClinicInfo(null,clinicInformation);
                    }
                }
                else {
                    Log.d(TAG, "doWork: Sem Informacao Clinica para syncronizar");
                }
            }
            else {
                Log.e(TAG, "Response Servidor Offline");
                return Result.failure();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }

        return Result.success();
    }
}
