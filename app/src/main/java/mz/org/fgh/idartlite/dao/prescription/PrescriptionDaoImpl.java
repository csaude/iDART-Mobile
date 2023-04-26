package mz.org.fgh.idartlite.dao.prescription;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.dao.generic.GenericDaoImpl;
import mz.org.fgh.idartlite.model.DiseaseType;
import mz.org.fgh.idartlite.model.Episode;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.util.DateUtilities;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class PrescriptionDaoImpl extends GenericDaoImpl<Prescription, Integer> implements IPrescriptionDao {

    public PrescriptionDaoImpl(Class dataClass) throws SQLException {
        super(dataClass);
    }

    public PrescriptionDaoImpl(ConnectionSource connectionSource, Class dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public PrescriptionDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    @Override
    public List<Prescription> getAllByPatient(Patient patient) throws SQLException {
        return queryBuilder().orderBy(Prescription.COLUMN_PRESCRIPTION_DATE, false).where().eq(Episode.COLUMN_PATIENT_ID, patient.getId()).query();
    }

    @Override
    public Prescription getLastPatientPrescription(Patient patient) throws SQLException {

        QueryBuilder<Prescription, Integer> prescriptionQb = queryBuilder();

        prescriptionQb.where().eq(Prescription.COLUMN_PATIENT_ID, patient.getId());

        return prescriptionQb.orderBy(Prescription.COLUMN_PRESCRIPTION_DATE, false).queryForFirst();

    }

    @Override
    public Prescription getLastPatientPrescriptionByDiseaseType(Patient patient, DiseaseType diseaseType) throws SQLException {
        QueryBuilder<Prescription, Integer> prescriptionQb = queryBuilder();

        prescriptionQb.where().eq(Prescription.COLUMN_PATIENT_ID, patient.getId()).and().eq(Prescription.COLUMN_DISEASE_TYPE_ID, diseaseType.getId());

        return prescriptionQb.orderBy(Prescription.COLUMN_PRESCRIPTION_DATE, false).queryForFirst();
    }

    @Override
    public Prescription getLastClosedPrescriptionByPatient(Patient patient) throws SQLException {
        QueryBuilder<Prescription, Integer> prescriptionQb = queryBuilder();

        prescriptionQb.where().eq(Prescription.COLUMN_PATIENT_ID, patient.getId()).and().isNotNull(Prescription.COLUMN_EXPIRY_DATE);

        return prescriptionQb.orderBy("id", false).queryForFirst();

    }

    @Override
    public void closePrescription(Prescription prescription) throws SQLException {
        prescription.setExpiryDate(DateUtilities.getCurrentDate());
        update(prescription);
    }

    @Override
    public boolean checkIfPatientHasPrescriptions(Patient patient) throws SQLException  {
        return queryBuilder().where().eq(Episode.COLUMN_PATIENT_ID, patient.getId()).query().isEmpty();
    }

    @Override
    public boolean checkIfPatientHasPrescriptionsWithDiseaseType(Patient patient, DiseaseType diseaseType) throws SQLException {
        return queryBuilder().where().eq(Episode.COLUMN_PATIENT_ID, patient.getId()).and().eq(Prescription.COLUMN_DISEASE_TYPE_ID, diseaseType.getId()).query().isEmpty();
    }

    @Override
    public List<Prescription> getAllPrescriptionToRemoveByDate(Date dateToRemove) throws SQLException {
        return queryBuilder().where().le(Prescription.COLUMN_PRESCRIPTION_DATE, dateToRemove).and().eq(Prescription.COLUMN_SYNC_STATUS, BaseModel.SYNC_SATUS_SENT).or().eq(Prescription.COLUMN_SYNC_STATUS, BaseModel.SYNC_SATUS_UPDATED).query();
    }

    @Override
    public List<Prescription> getAllPrescriptionToRemoveByDateAndPatientAndDiseaseType(Patient patient, DiseaseType diseaseType, Date dateToRemove) throws SQLException {
        return queryBuilder().orderBy(Prescription.COLUMN_PRESCRIPTION_DATE, false).where().eq(Prescription.COLUMN_PATIENT_ID, patient.getId())
                .and().le(Prescription.COLUMN_PRESCRIPTION_DATE, dateToRemove).and().ne(Prescription.COLUMN_SYNC_STATUS, BaseModel.SYNC_SATUS_READY).and().eq(Prescription.COLUMN_DISEASE_TYPE_ID, diseaseType.getId()).query();
    }

}
