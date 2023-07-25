package mz.org.fgh.idartlite.workSchedule.work.post;

import static mz.org.fgh.idartlite.base.application.IdartLiteApplication.CHANNEL_2_ID;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.base.work.BaseWorker;
import mz.org.fgh.idartlite.model.ClinicInformation;
import mz.org.fgh.idartlite.rest.service.ClinicInfo.RestClinicInfoService;
import mz.org.fgh.idartlite.util.Utilities;

public class RestPostClinicInformationWorkerScheduler extends BaseWorker<ClinicInformation> {

    public RestPostClinicInformationWorkerScheduler(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    protected void doOnStart() {
        try {
            issueNotification(CHANNEL_2_ID, "Sincronização de Informação farmaceuica Iniciada", true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doOnFinish() {
        try {
            issueNotification(CHANNEL_2_ID, "Sincronização de Informação farmaceuica  Terminada", false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doSave(List<ClinicInformation> recs) {

    }


    @Override
    public void doOnlineSearch(long offset, long limit) throws SQLException {
        try {
            RestClinicInfoService.restPostAllClinicInfo(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doAfterSearch(String flag, List<ClinicInformation> recs) throws SQLException {
        if (Utilities.listHasElements(recs)) {
            for (ClinicInformation clinicInformation : recs) {
                if (Utilities.stringHasValue(clinicInformation.getUuid())) this.newRecsQty ++;
                else this.updatedRecsQty++;
            }
            this.offset = this.offset + RECORDS_PER_SEARCH;
        } else {
            changeStatusToFinished();
            doOnFinish();
        }
    }



}
