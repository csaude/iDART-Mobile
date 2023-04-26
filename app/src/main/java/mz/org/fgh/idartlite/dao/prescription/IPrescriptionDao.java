package mz.org.fgh.idartlite.dao.prescription;

import mz.org.fgh.idartlite.dao.generic.IGenericDao;
import mz.org.fgh.idartlite.model.DiseaseType;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.Prescription;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface IPrescriptionDao extends IGenericDao<Prescription, Integer> {

    public List<Prescription> getAllByPatient(Patient patient) throws SQLException;

    public Prescription getLastPatientPrescription(Patient patient) throws SQLException;

    public Prescription getLastPatientPrescriptionByDiseaseType(Patient patient, DiseaseType diseaseType) throws SQLException;

    public Prescription getLastClosedPrescriptionByPatient(Patient patient) throws SQLException;

    public void closePrescription(Prescription prescription) throws SQLException;

    public boolean checkIfPatientHasPrescriptions(Patient patient) throws SQLException ;

    public boolean checkIfPatientHasPrescriptionsWithDiseaseType(Patient patient, DiseaseType diseaseType) throws SQLException ;

    public List<Prescription> getAllPrescriptionToRemoveByDate(Date dateToRemove)throws SQLException;

    public List<Prescription> getAllPrescriptionToRemoveByDateAndPatientAndDiseaseType(Patient patient,DiseaseType diseaseType,Date dateToRemove) throws SQLException;
}
