package mz.org.fgh.idartlite.service.prescription;

import com.google.gson.internal.LinkedTreeMap;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.model.DiseaseType;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.PrescribedDrug;
import mz.org.fgh.idartlite.model.Prescription;


public interface IPrescriptionService extends IBaseService<Prescription> {

    public List<Prescription> getAllPrescriptionsByPatient(Patient patient) throws SQLException ;

    public void createPrescription(Prescription prescription) throws SQLException ;

    public void savePriscribedDrus(List<PrescribedDrug> prescribedDrugs) throws SQLException ;

    public void updatePrescription(Prescription prescription) throws SQLException ;

    public void updatePrescriptionEntity(Prescription prescription) throws SQLException ;

    public void closePrescription(Prescription prescription) throws SQLException ;

    public void deletePrescription(Prescription prescription) throws SQLException ;

    public Prescription getLastPatientPrescription(Patient patient) throws SQLException ;

    public Prescription getLastPatientPrescriptionByDiseaseType(Patient patient, DiseaseType diseaseType) throws SQLException;

    public void saveLastPrescriptionFromRest(LinkedTreeMap<String, Object> patient, Patient localPatient) ;

    public boolean checkIfPatientHasPrescriptions(Patient patient) throws SQLException;

    public boolean checkIfPatientHasPrescriptionsWithDiseaseType(Patient patient, DiseaseType diseaseType) throws SQLException ;

    public Prescription getLastClosedPrescriptionByPatient(Patient patient) throws SQLException;

    public List<Prescription> getAllPrescriptionToRemoveByDate(Date dateToRemove) throws SQLException;

    public void deletePrescriptionAndPrescribedDrugs(Prescription prescription) throws SQLException;

    public List<Prescription> getAllPrescriptionToRemoveByDateAndPatientAndDiseaseType(Patient patient,DiseaseType diseaseType, Date dateToRemove) throws SQLException;

}
