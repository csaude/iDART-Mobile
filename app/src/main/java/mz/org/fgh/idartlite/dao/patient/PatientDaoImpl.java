package mz.org.fgh.idartlite.dao.patient;

import android.app.Application;
import android.util.Log;

import com.j256.ormlite.stmt.ColumnArg;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mz.org.fgh.idartlite.base.databasehelper.IdartLiteDataBaseHelper;
import mz.org.fgh.idartlite.dao.generic.GenericDaoImpl;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.Drug;
import mz.org.fgh.idartlite.model.Episode;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.model.patient.Patient;

public class PatientDaoImpl extends GenericDaoImpl<Patient, Integer> implements IPatientDao {

    public PatientDaoImpl(Class dataClass) throws SQLException {
        super(dataClass);
    }

    public PatientDaoImpl(ConnectionSource connectionSource, Class dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public PatientDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    public List<Patient> searchPatientByParamAndClinic(String param, Clinic clinic, long offset, long limit) throws SQLException {
        QueryBuilder<Patient, Integer> qb = queryBuilder();
        if (offset > 0 && limit > 0) qb.limit(limit).offset(offset);
        return qb.where()
                .like(Patient.COLUMN_NID, "%" + param + "%")
                .or().like(Patient.COLUMN_FIRST_NAME, "%" + param + "%")
                .or().like(Patient.COLUMN_LAST_NAME, "%" + param + "%")
                .and()
                .eq(Patient.COLUMN_CLINIC_ID, clinic.getId()).query();
    }

    @Override
    public int countNewPatientsByPeriod(Date start, Date end, String sanitaryUnit, Application application) throws SQLException {
        QueryBuilder<Episode, Integer> episodeQb = IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getEpisodeDao().queryBuilder();
        episodeQb.selectColumns(Episode.COLUMN_PATIENT_ID).where()
                .isNull(Episode.COLUMN_STOP_REASON)
                .and()
                .isNotNull(Episode.COLUMN_START_REASON)
                .and()
                .eq(Episode.COLUMN_SANITARY_UNIT, sanitaryUnit)
                .and()
                .ge(Episode.COLUMN_EPISODE_DATE, start)
                .and()
                .le(Episode.COLUMN_EPISODE_DATE, end);

        QueryBuilder<Patient, Integer> patientQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPatientDao().queryBuilder();
        patientQb.where().in(Patient.COLUMN_ID,episodeQb);

        System.out.println(patientQb.prepareStatementString());
        return (int) patientQb.countOf();
    }

    @Override
    public Patient getPatientByUuid(String uuid) throws SQLException {
        return queryBuilder().where().eq(Patient.COLUMN_UUID, uuid).queryForFirst();
    }

    @Override
    public Patient checkExistsPatientWithNID(String nid) throws SQLException {
        return queryBuilder().where().eq(Patient.COLUMN_NID,nid).queryForFirst();
    }

    @Override
    public List<Patient> searchPatientByNidOrNameOrSurname(String nid, String name, String surname, long offset, long limit) throws SQLException {
        QueryBuilder<Patient, Integer> qb = queryBuilder();
        if (offset > 0 && limit > 0) qb.limit(limit).offset(offset);
        return qb.where()
                .like(Patient.COLUMN_NID, "%" + nid + "%")
                .or().like(Patient.COLUMN_FIRST_NAME, "%" + name + "%")
                .or().like(Patient.COLUMN_LAST_NAME, "%" + surname+ "%").query();
    }

    @Override
    public List<Patient> getAllPatientsBetweenStartDateAndEndDate(Application application,Date start, Date end, long offset, long limit) throws SQLException {
        QueryBuilder<Episode, Integer> episodeQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getEpisodeDao().queryBuilder();
//        QueryBuilder<Episode, Integer> episodeQb1 =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getEpisodeDao().queryBuilder();
        if (offset > 0 && limit > 0) episodeQb.limit(limit).offset(offset);
        episodeQb.selectColumns(Episode.COLUMN_PATIENT_ID).where().eq(Episode.COLUMN_PATIENT_ID, new ColumnArg(Patient.TABLE_NAME, Patient.COLUMN_ID)).and().isNotNull(Episode.COLUMN_START_REASON).and()
                .isNull(Episode.COLUMN_STOP_REASON).and()
                .ge(Episode.COLUMN_EPISODE_DATE, start)
                .and()
                .le(Episode.COLUMN_EPISODE_DATE, end);

        QueryBuilder<Patient, Integer> patientQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getPatientDao().queryBuilder();
        patientQb.where().in(Patient.COLUMN_ID,episodeQb);
//        patientQb.join(episodeQb);
//        System.out.println(patientQb.prepareStatementString());

        return patientQb.query();



    }

    @Override
    public List<Patient> get(Application application, long offset, long limit) throws SQLException {
        QueryBuilder<Episode, Integer> episodeQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getEpisodeDao().queryBuilder();
        episodeQb.where().eq(Episode.COLUMN_PATIENT_ID, new ColumnArg(Patient.TABLE_NAME, Patient.COLUMN_ID)).and().isNotNull(Episode.COLUMN_STOP_REASON);

        QueryBuilder<Patient, Integer> qb = queryBuilder();
        qb.where().not().exists(episodeQb);
        qb.limit(limit).offset(offset);

        return qb.query();
    }

    public boolean checkIsEmpty(String param, Clinic clinic) throws SQLException {
       return  queryBuilder().where().like(Patient.COLUMN_NID, "%" + param + "%").or().like(Patient.COLUMN_FIRST_NAME, "%" + param + "%").or().like(Patient.COLUMN_LAST_NAME, "%" + param + "%").and().eq(Patient.COLUMN_CLINIC_ID, clinic.getId()).query().isEmpty();
    }
}
