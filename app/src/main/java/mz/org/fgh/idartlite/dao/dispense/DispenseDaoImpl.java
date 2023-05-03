package mz.org.fgh.idartlite.dao.dispense;

import android.app.Application;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.j256.ormlite.stmt.ColumnArg;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import mz.org.fgh.idartlite.base.databasehelper.IdartLiteDataBaseHelper;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.dao.generic.GenericDaoImpl;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.DispensedDrug;
import mz.org.fgh.idartlite.model.Episode;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.model.TherapeuticLine;
import mz.org.fgh.idartlite.util.DateUtilities;

public class DispenseDaoImpl extends GenericDaoImpl<Dispense, Integer> implements IDispenseDao {

    public DispenseDaoImpl(Class dataClass) throws SQLException {
        super(dataClass);
    }

    public DispenseDaoImpl(ConnectionSource connectionSource, Class dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public DispenseDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    @Override
    public List<Dispense> getAllNotVoidedByPrescription(Prescription prescription) throws SQLException {
        return queryBuilder().where().eq(Dispense.COLUMN_PRESCRIPTION, prescription.getId()).and().eq(Dispense.COLUMN_VOIDED,false).query();
    }

    @Override
    public long countAllOfPrescription(Prescription prescription) throws SQLException {
        return queryBuilder().where().eq(Dispense.COLUMN_PRESCRIPTION, prescription.getId()).and().eq(Dispense.COLUMN_VOIDED,false).countOf();
    }

    public List<Dispense> getAllOfPatient(Application application, Patient patient) throws SQLException {

        QueryBuilder<Prescription, Integer> prescriptionQb = IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPrescriptionDao().queryBuilder();
        prescriptionQb.where().eq(Prescription.COLUMN_PATIENT_ID, patient.getId());

        QueryBuilder<TherapeuticLine, Integer> therapeuticLineQb = IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getTherapeuticLineDao().queryBuilder();
        prescriptionQb.leftJoin(therapeuticLineQb);

        QueryBuilder<Dispense, Integer> dispenseQb =   IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getDispenseDao().queryBuilder();
        dispenseQb.join(prescriptionQb).where().eq(Dispense.COLUMN_VOIDED,false);

        List<Dispense> dispenses = dispenseQb.orderBy(Dispense.COLUMN_NEXT_PICKUP_DATE, false).query();

        List<Dispense> resList = new ArrayList<>();
        for (Dispense dispense : dispenses) {
            if(dispense.getPrescription().getDiseaseType().getCode().equalsIgnoreCase("TARV"))
                resList.add(dispense);
        }
        return resList;
    }

    @Override
    public Dispense getLastDispensePrescription(Prescription prescription) throws SQLException {

        List<Dispense> dispenseList = null;
        QueryBuilder<Dispense, Integer> dispenseQb = queryBuilder();

        dispenseQb.where().eq(Dispense.COLUMN_PRESCRIPTION, prescription.getId()).and().eq(Dispense.COLUMN_VOIDED,false);

        dispenseList = dispenseQb.orderBy(Dispense.COLUMN_PICKUP_DATE, false).limit(1L).query();

        if (dispenseList.size() != 0)
            return dispenseList.get(0);

        return null;

    }

    @Override
    public List<Dispense> getDispensesBetweenStartDateAndEndDateWithLimit(Application application,Date startDate, Date endDate, long offset, long limit) throws SQLException {
        QueryBuilder<Dispense, Integer> qb = queryBuilder();
        QueryBuilder<DispensedDrug, Integer> dispensedDrugQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getDispensedDrugDao().queryBuilder();
        dispensedDrugQb.where().gt(DispensedDrug.COLUMN_QUANTITY_SUPPLIED,0).and().lt(DispensedDrug.COLUMN_QUANTITY_SUPPLIED,12);

        qb.join(dispensedDrugQb);
        if (limit > 0 && offset > 0) qb.limit(limit).offset(offset);
         qb.where().ge(Dispense.COLUMN_PICKUP_DATE, startDate)
                .and()
                .le(Dispense.COLUMN_PICKUP_DATE, endDate).and().eq(Dispense.COLUMN_VOIDED,false);


        List<Dispense> resList = new ArrayList<>();
        for (Dispense dispense : qb.query()) {
            if(dispense.getPrescription().getDiseaseType().getCode().equalsIgnoreCase("TARV"))
                resList.add(dispense);
        }
        return resList;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public List<Dispense> getDispensesUsBetweenStartDateAndEndDateWithLimit(Application application, Date startDate, Date endDate, long offset, long limit) throws SQLException {
        QueryBuilder<Dispense, Integer> qb = queryBuilder();
        QueryBuilder<DispensedDrug, Integer> dispensedDrugQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getDispensedDrugDao().queryBuilder();

        qb.join(dispensedDrugQb);
        if (limit > 0 && offset > 0) qb.limit(limit).offset(offset);
        qb.where().ge(Dispense.COLUMN_PICKUP_DATE, startDate)
                .and()
                .le(Dispense.COLUMN_PICKUP_DATE, endDate).and().eq(Dispense.COLUMN_VOIDED,false);

        List<Dispense> resList = new ArrayList<>();
        for (Dispense dispense : qb.query()) {
            LocalDate ldPickUpDate = Instant.ofEpochMilli(dispense.getPickupDate().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate ldPirescriptionDate = Instant.ofEpochMilli(dispense.getPrescription().getPrescriptionDate().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            if(ChronoUnit.DAYS.between(ldPickUpDate, ldPirescriptionDate) < 7 && dispense.getPrescription().getPrescriptionSeq().length() > 4){// Dispensas da US
                if(dispense.getPrescription().getDiseaseType().getCode().equalsIgnoreCase("TARV"))
                    resList.add(dispense);
            }
        }
        return resList;
    }

//    public List<Dispense> getDispensesUsBetweenStartDateAndEndDateWithLimit(Application application, Date startDate, Date endDate, long offset, long limit) throws SQLException {
//        QueryBuilder<Dispense, Integer> qb = queryBuilder();
//        QueryBuilder<Prescription, Integer> prescriptionQb = IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPrescriptionDao().queryBuilder();
//        String rawWhere = "LENGTH(" + Prescription.COLUMN_PRESCRIPTION_SEQ + ") > 4";
//        prescriptionQb.where().raw(rawWhere);
//
//        qb.join(prescriptionQb);
//        QueryBuilder<DispensedDrug, Integer> dispensedDrugQb = IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getDispensedDrugDao().queryBuilder();
//        dispensedDrugQb.where().gt(DispensedDrug.COLUMN_QUANTITY_SUPPLIED, 0).and().lt(DispensedDrug.COLUMN_QUANTITY_SUPPLIED, 12);
//
//        qb.join(dispensedDrugQb);
//        if (limit > 0 && offset > 0) qb.limit(limit).offset(offset);
//        qb.where().ge(Dispense.COLUMN_PICKUP_DATE, startDate)
//                .and()
//                .le(Dispense.COLUMN_PICKUP_DATE, endDate)
//                .and()
//                .eq(Dispense.COLUMN_VOIDED, false);
//
//        System.out.println(qb.prepareStatementString());
//        return qb.query();
//    }


//    public List<Dispense> getDispensesUsBetweenStartDateAndEndDateWithLimit(Application application,Date startDate, Date endDate, long offset, long limit) throws SQLException {
//        // Tamanho de prescriptionID.tamanho > 10 [US-001]
//        QueryBuilder<Dispense, Integer> qb = queryBuilder();
//        QueryBuilder<DispensedDrug, Integer> dispensedDrugQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getDispensedDrugDao().queryBuilder();
//        dispensedDrugQb.where().gt(DispensedDrug.COLUMN_QUANTITY_SUPPLIED,0).and().lt(DispensedDrug.COLUMN_QUANTITY_SUPPLIED,12);
//
//        qb.join(dispensedDrugQb);
//        if (limit > 0 && offset > 0) qb.limit(limit).offset(offset);
//        qb.where().ge(Dispense.COLUMN_PICKUP_DATE, startDate)
//                .and()
//                .le(Dispense.COLUMN_PICKUP_DATE, endDate).and().eq(Dispense.COLUMN_VOIDED,false);
//        List<Dispense> resList = new ArrayList<>();
//        for (Dispense dispense : qb.query()) {
//            if (dispense.getPrescription().getPrescriptionSeq().length() > 7) resList.add(dispense);
//        }
//        return resList;
//    }

    @Override
    public List<Dispense> getDispensesNonSyncBetweenStartDateAndEndDateWithLimit(Date startDate, Date endDate, long offset, long limit) throws SQLException {
        QueryBuilder<Dispense, Integer> qb = queryBuilder();
        if (limit > 0 && offset > 0)  qb.limit(limit).offset(offset);
         qb.where().ge(Dispense.COLUMN_PICKUP_DATE, startDate)
                .and()
                .le(Dispense.COLUMN_PICKUP_DATE, endDate).and().eq(Dispense.COLUMN_VOIDED,false).and().eq(Dispense.COLUMN_SYNC_STATUS, Dispense.SYNC_SATUS_READY);
        List<Dispense> resList = new ArrayList<>();
        for(Dispense dispense : qb.query()) {
            if(dispense.getPrescription().getDiseaseType().getCode().equalsIgnoreCase("TARV"))
                resList.add(dispense);
        }
        return resList;
    }

    @Override
    public List<Dispense> getDispensesBetweenStartDateAndEndDate(Application application,Date startDate, Date endDate) throws SQLException {
        QueryBuilder<Dispense, Integer> qb = queryBuilder();
        QueryBuilder<DispensedDrug, Integer> dispensedDrugQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getDispensedDrugDao().queryBuilder();
        dispensedDrugQb.where().gt(DispensedDrug.COLUMN_QUANTITY_SUPPLIED,0).and().lt(DispensedDrug.COLUMN_QUANTITY_SUPPLIED,12);

        qb.join(dispensedDrugQb);
        qb.where().ge(Dispense.COLUMN_PICKUP_DATE, startDate)
                .and()
                .le(Dispense.COLUMN_PICKUP_DATE, endDate).and().eq(Dispense.COLUMN_VOIDED,false);

        return qb.query();
    }

    @Override
    public List<Dispense> getAllDispensesByStatusAndNotVoided(String status) throws SQLException {
        return queryBuilder().where().eq(Dispense.COLUMN_SYNC_STATUS, status).and().eq(Dispense.COLUMN_VOIDED,false).query();
    }

    @Override
    public List<Dispense> getDispensesBetweenNextPickppDateStartDateAndEndDateWithLimit(Date startDate, Date endDate, long offset, long limit) throws SQLException {
        return null;
    }

    @Override
    public List<Dispense> getDispensesBetweenNextPickppDateStartDateAndEndDateWithLimit(Application application,Date startDate, Date endDate, long offset, long limit) throws SQLException {

        QueryBuilder<Prescription, Integer> prescriptionQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPrescriptionDao().queryBuilder();
        QueryBuilder<Episode, Integer> episodeQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getEpisodeDao().queryBuilder();

        episodeQb.where().isNotNull(Episode.COLUMN_STOP_REASON);
        QueryBuilder<Patient, Integer> patientQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPatientDao().queryBuilder();
        patientQb.where().eq(Patient.COLUMN_ID, new ColumnArg(Patient.TABLE_NAME, Patient.COLUMN_ID)).and().not().in(Patient.COLUMN_ID,episodeQb.selectRaw("patient_id"));
        QueryBuilder<Dispense, Integer> dispenseQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getDispenseDao().queryBuilder();

        prescriptionQb.groupBy(Prescription.COLUMN_PATIENT_ID).join(patientQb);
        dispenseQb.join(prescriptionQb);
        dispenseQb.orderBy(Dispense.COLUMN_NEXT_PICKUP_DATE,true);
        if (limit > 0 && offset > 0) dispenseQb.limit(limit).offset(offset);
        dispenseQb.where().ge(Dispense.COLUMN_NEXT_PICKUP_DATE, startDate)
                .and()
                .le(Dispense.COLUMN_NEXT_PICKUP_DATE, endDate).and().eq(Dispense.COLUMN_VOIDED,false);
        List<Dispense> resList = new ArrayList<>();
        for(Dispense dispense : dispenseQb.query()) {
            if(dispense.getPrescription().getDiseaseType().getCode().equalsIgnoreCase("TARV"))
            resList.add(dispense);
        }
        return resList;
    }

    public List<Dispense> getAbsentPatientsBetweenNextPickppDateStartDateAndEndDateWithLimit(Application application,Date startDate, Date endDate, long offset, long limit) throws SQLException {

        QueryBuilder<Prescription, Integer> prescriptionQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPrescriptionDao().queryBuilder();
        QueryBuilder<Episode, Integer> episodeQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getEpisodeDao().queryBuilder();

        episodeQb.where().isNotNull(Episode.COLUMN_STOP_REASON);
        QueryBuilder<Patient, Integer> patientQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPatientDao().queryBuilder().setAlias("patientOuter");
        patientQb.where().not().in(Patient.COLUMN_ID,episodeQb.selectRaw("patient_id"));

        QueryBuilder<Dispense, Integer> outerDispenseQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getDispenseDao().queryBuilder().setAlias("outerDispense");
        QueryBuilder<Dispense, Integer> dispenseQb1 =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getDispenseDao().queryBuilder();
        QueryBuilder<Dispense, Integer> dispenseQb2 =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getDispenseDao().queryBuilder();
        QueryBuilder<Prescription, Integer> prescriptionQb1 =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPrescriptionDao().queryBuilder();
        QueryBuilder<Patient, Integer> patientInnerQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPatientDao().queryBuilder().setAlias("patientInner");
        QueryBuilder<Prescription, Integer> prescriptionInnerQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPrescriptionDao().queryBuilder();
        QueryBuilder<Patient, Integer> patientInnerQb2 =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPatientDao().queryBuilder();


        prescriptionQb.groupBy(Prescription.COLUMN_PATIENT_ID).join(patientQb);
        prescriptionInnerQb.groupBy(Prescription.COLUMN_PATIENT_ID).join(patientInnerQb2);
        outerDispenseQb.join(prescriptionQb);
        dispenseQb1.join(prescriptionInnerQb);
        outerDispenseQb.orderBy(Dispense.COLUMN_NEXT_PICKUP_DATE,true);
        prescriptionQb1.join(patientInnerQb);
        dispenseQb2.join(prescriptionQb1);
        dispenseQb2.where().raw(" patientInner.id=patientOuter.id AND dispense.pickup_date >= outerDispense.next_pickup_date AND dispense.pickup_date <= outerDispense.next_pickup_date +1");
        dispenseQb1.where().raw("patient_id = patientOuter.id");
        if (limit > 0 && offset > 0) outerDispenseQb.limit(limit).offset(offset);
        outerDispenseQb.where().ge(Dispense.COLUMN_NEXT_PICKUP_DATE, startDate)
                .and()
                .le(Dispense.COLUMN_NEXT_PICKUP_DATE, endDate).and().in(Dispense.COLUMN_NEXT_PICKUP_DATE,dispenseQb1.selectRaw("max(next_pickup_date)")).and().eq(Dispense.COLUMN_VOIDED,false).and().not().exists(dispenseQb2);
        List<Dispense> resList = new ArrayList<>();
        for (Dispense dispense : outerDispenseQb.query()) {
            if(dispense.getPrescription().getDiseaseType().getCode().equalsIgnoreCase("TARV"))
                resList.add(dispense);
        }
        return resList;
    }

    public List<Dispense> getActivePatientsBetweenNextPickppDateStartDateAndEndDateWithLimit(Application application,Date startDate, Date endDate, long offset, long limit) throws SQLException {
        String days = "0";
        QueryBuilder<Episode, Integer> episodeQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getEpisodeDao().queryBuilder();
        QueryBuilder<Patient, Integer> patientQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPatientDao().queryBuilder();
        QueryBuilder<Prescription, Integer> prescriptionQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPrescriptionDao().queryBuilder();
        QueryBuilder<Dispense, Integer> dispenseQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getDispenseDao().queryBuilder().setAlias("dispenses");

        episodeQb.where().isNotNull(Episode.COLUMN_STOP_REASON);
        dispenseQb.join(prescriptionQb);
        prescriptionQb.groupBy(Prescription.COLUMN_PATIENT_ID).join(patientQb);
        patientQb.where().eq(Patient.COLUMN_ID, new ColumnArg(Patient.TABLE_NAME, Patient.COLUMN_ID)).and().not().in(Patient.COLUMN_ID,episodeQb.selectRaw("patient_id"));

        dispenseQb.orderBy(Dispense.COLUMN_NEXT_PICKUP_DATE,true);

        if (limit > 0 && offset > 0) dispenseQb.limit(limit).offset(offset);
        dispenseQb.where().raw("Date(dispenses.next_pickup_date, \'+3 days\') >= '"+DateUtilities.formatToYYYYMMDD(endDate)+"'");

        List<Dispense> resList = new ArrayList<>();
        for (Dispense dispense : dispenseQb.query()) {
            if(dispense.getPrescription().getDiseaseType().getCode().equalsIgnoreCase("TARV"))
                resList.add(dispense);
        }
        return resList;
    }



    @Override
    public List<Dispense> getAllDispensesToRemoveByDates(Date dateToRemove) throws SQLException {
        return queryBuilder().where()
                .le(Dispense.COLUMN_PICKUP_DATE, dateToRemove).and().eq(Dispense.COLUMN_SYNC_STATUS, BaseModel.SYNC_SATUS_SENT).or().eq(Dispense.COLUMN_SYNC_STATUS, BaseModel.SYNC_SATUS_UPDATED).query();

    }

    @Override
    public List<Dispense> getAllByPrescription(Prescription prescription) throws SQLException {
        return queryBuilder().orderBy(Dispense.COLUMN_PICKUP_DATE, false).where().eq(Dispense.COLUMN_PRESCRIPTION, prescription.getId()).query();
    }
}