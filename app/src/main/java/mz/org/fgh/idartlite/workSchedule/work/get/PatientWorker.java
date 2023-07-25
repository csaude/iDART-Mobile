package mz.org.fgh.idartlite.workSchedule.work.get;

import static mz.org.fgh.idartlite.base.application.IdartLiteApplication.CHANNEL_1_ID;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.base.rest.BaseRestService;
import mz.org.fgh.idartlite.base.work.BaseWorker;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.rest.service.Patient.RestPatientService;
import mz.org.fgh.idartlite.util.Utilities;

public class PatientWorker extends BaseWorker<Patient> {

    public static final String UPDATED_RECS = "UPDATED_RECS";
    public static final String CREATED_RECS = "CREATED_RECS";

    public PatientWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    protected void doOnStart() {
        try {
            issueNotification(CHANNEL_1_ID, "Sincronização de Pacientes e Informação Farmacêutica Iniciada", true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doOnlineSearch(long offset, long limit) throws SQLException {
        boolean isFullLoad = getInputData().getBoolean("isFullLoad", false);
        RestPatientService.getAllPatient(this, offset, limit, isFullLoad);
    }

    @Override
    protected void doOnFinish() {
        try {
            String msg = "Sincronização de Pacientes e Informação Farmacêutica Terminada, Não Foram Encontrados Novos registos";
            if (hasNewRescs()) {
                msg = "Sincronização de Pacientes e Informação farmacêutica Terminada. \n";

                if (getNewRecsQty() > 0) {
                    msg += "Tem " + getNewRecsQty() ;
                    msg = msg + (getNewRecsQty() > 1 ? " novos pacientes\n" : " novo paciente\n");
                }
                if (getUpdatedRecsQty() > 0) {
                    msg += "Actualizado(s) " + getUpdatedRecsQty();
                    msg = msg + (getUpdatedRecsQty() > 1 ? " pacientes\n" : " paciente\n");
                }
            }
            issueNotification(CHANNEL_1_ID, msg, false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doAfterSearch(String flag, List<Patient> recs) throws SQLException {
        if (Utilities.listHasElements(recs) || flag.equals(BaseRestService.REQUEST_SUCESS)) {
            for (Patient patient : recs) {
                if (Utilities.stringHasValue(patient.getUuid())) this.newRecsQty ++;
                else this.updatedRecsQty++;
            }
            this.offset = this.offset + RECORDS_PER_SEARCH;
            doSave(recs);
            fullLoadRecords();
        } else {
            changeStatusToFinished();
            doOnFinish();
        }
    }

    @Override
    protected void doSave(List<Patient> recs) {

    }
}
