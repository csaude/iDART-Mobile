package mz.org.fgh.idartlite.service.prescription;

import android.app.Application;

import com.google.gson.internal.LinkedTreeMap;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.service.BaseService;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.DispensedDrug;
import mz.org.fgh.idartlite.model.Patient;
import mz.org.fgh.idartlite.model.PrescribedDrug;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.model.TherapeuticLine;
import mz.org.fgh.idartlite.model.TherapeuticRegimen;
import mz.org.fgh.idartlite.model.User;
import mz.org.fgh.idartlite.service.dispense.DispenseTypeService;
import mz.org.fgh.idartlite.service.drug.TherapeuthicLineService;
import mz.org.fgh.idartlite.service.drug.TherapheuticRegimenService;
import mz.org.fgh.idartlite.util.DateUtilities;

import static java.util.Objects.requireNonNull;
import static mz.org.fgh.idartlite.util.DateUtilities.getSqlDateFromString;

public class PrescriptionService extends BaseService<Prescription> implements IPrescriptionService {

    protected TherapheuticRegimenService therapeuticRegimenService;
    protected TherapeuthicLineService therapeuticLineService;
    protected DispenseTypeService dispenseTypeService;
    protected PrescribedDrugService prescribedDrugService;

    public PrescriptionService(Application application, User currUser) {
        super(application, currUser);
        this.therapeuticRegimenService = new TherapheuticRegimenService(getApp(), currUser);
        this.therapeuticLineService = new TherapeuthicLineService(getApp(), currUser);
        this.dispenseTypeService = new DispenseTypeService(getApp(), currUser);
        this.prescribedDrugService = new PrescribedDrugService(getApp(), currUser);

    }

    public PrescriptionService(Application application) {
        super(application);
    }

    @Override
    public void save(Prescription record) throws SQLException {

    }

    @Override
    public void update(Prescription record) throws SQLException {

    }

    public List<Prescription> getAllPrescriptionsByPatient(Patient patient) throws SQLException {
        return getDataBaseHelper().getPrescriptionDao().getAllByPatient(patient);
    }


    public void createPrescription(Prescription prescription) throws SQLException {
        getDataBaseHelper().getPrescriptionDao().create(prescription);
        if (prescription.getPrescribedDrugs() != null)
            savePriscribedDrus(prescription.getPrescribedDrugs());
    }


    public void savePriscribedDrus(List<PrescribedDrug> prescribedDrugs) throws SQLException {
        for (PrescribedDrug prescribedDrug : prescribedDrugs) {
            getDataBaseHelper().getPrescribedDrugDao().create(prescribedDrug);
        }
    }

    public void updatePrescription(Prescription prescription) throws SQLException {
        getDataBaseHelper().getPrescriptionDao().update(prescription);

        DeleteBuilder<PrescribedDrug, Integer> deleteBuilder = getDataBaseHelper().getPrescribedDrugDao().deleteBuilder();
        deleteBuilder.where().eq(PrescribedDrug.COLUMN_PRESCRIPTION, prescription.getId());
        deleteBuilder.delete();

        getDataBaseHelper().getPrescribedDrugDao().create(prescription.getPrescribedDrugs());
    }

    public void updatePrescriptionEntity(Prescription prescription) throws SQLException {
        getDataBaseHelper().getPrescriptionDao().update(prescription);
    }

    public void closePrescription(Prescription prescription) throws SQLException {
        getDataBaseHelper().getPrescriptionDao().closePrescription(prescription);
    }

    public void deletePrescription(Prescription prescription) throws SQLException {
        getDataBaseHelper().getPrescriptionDao().delete(prescription);
    }

    public Prescription getLastPatientPrescription(Patient patient) throws SQLException {

        return getDataBaseHelper().getPrescriptionDao().getLastPatientPrescription(patient);
    }

    public void saveLastPrescriptionFromRest(LinkedTreeMap<String, Object> patient, Patient localPatient) {

        try {

            Prescription prescription = getLastPatientPrescription(localPatient);

            if (prescription != null) {
                if ((int) DateUtilities.dateDiff(requireNonNull(getSqlDateFromString(requireNonNull(patient.get("prescriptiondate")).toString(), "yyyy-MM-dd'T'HH:mm:ss")), prescription.getPrescriptionDate(), DateUtilities.DAY_FORMAT) > 0)
                    buildPrescription(patient,localPatient);
                }else {
                buildPrescription(patient,localPatient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean checkIfPatientHasPrescriptions(Patient patient) throws SQLException {
        return getDataBaseHelper().getPrescriptionDao().checkIfPatientHasPrescriptions(patient);
    }

    @Override
    public Prescription getLastClosedPrescriptionByPatient(Patient patient) throws SQLException {
        return getDataBaseHelper().getPrescriptionDao().getLastClosedPrescriptionByPatient(patient);
    }

    @Override
    public List<Prescription> getAllPrescriptionToRemoveByDate(Date dateToRemove) throws SQLException {
        return getDataBaseHelper().getPrescriptionDao().getAllPrescriptionToRemoveByDate(dateToRemove);
    }

    @Override
    public void deletePrescriptionAndPrescribedDrugs(Prescription prescription) throws SQLException {

        List<PrescribedDrug> prescribedDrugs = this.prescribedDrugService.getAllByPrescription(prescription);
        this.prescribedDrugService.deletePrescribedDrugs(prescribedDrugs);
        getDataBaseHelper().getPrescriptionDao().delete(prescription);

    }

    @Override
    public List<Prescription> getAllPrescriptionToRemoveByDateAndPatient(Patient patient, Date dateToRemove) throws SQLException {
        return getDataBaseHelper().getPrescriptionDao().getAllPrescriptionToRemoveByDateAndPatient(patient, dateToRemove);
    }


    public void buildPrescription (LinkedTreeMap<String, Object> patient, Patient localPatient) throws SQLException {
        Prescription prescription = new Prescription();
        TherapeuticRegimen therapeuticRegimen = therapeuticRegimenService.getTherapeuticRegimenByDescription(requireNonNull(patient.get("regimenome")).toString());
        TherapeuticLine therapeuticLine = therapeuticLineService.getTherapeuticLineByCode(requireNonNull(patient.get("linhanome")).toString());

        prescription.setPrescriptionDate(getSqlDateFromString(requireNonNull(patient.get("prescriptiondate")).toString(), "yyyy-MM-dd'T'HH:mm:ss"));
        if (patient.get("prescriptionenddate") != null)
            prescription.setExpiryDate(getSqlDateFromString(requireNonNull(patient.get("prescriptionenddate")).toString(), "yyyy-MM-dd'T'HH:mm:ss"));
        prescription.setPatient(localPatient);
        prescription.setPrescriptionSeq("0001");
        prescription.setSupply(Math.round(Float.parseFloat(requireNonNull(patient.get("duration")).toString())));
        prescription.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
        prescription.setTherapeuticLine(therapeuticLine);
        prescription.setTherapeuticRegimen(therapeuticRegimen);
        prescription.setUrgentNotes(requireNonNull(patient.get("motivocriacaoespecial")).toString());
        prescription.setUrgentPrescription(requireNonNull(patient.get("prescricaoespecial")).toString());
        prescription.setUuid(UUID.randomUUID().toString());

        if ((int) Float.parseFloat(requireNonNull(patient.get("dispensasemestral")).toString()) > 0)
            prescription.setDispenseType(dispenseTypeService.getDispenseTypeByDescription("Dispensa Semestral (DS)"));
        else if ((int) Float.parseFloat(requireNonNull(patient.get("dispensatrimestral")).toString()) > 0)
            prescription.setDispenseType(dispenseTypeService.getDispenseTypeByDescription("Dispensa Trimestral (DT)"));
        else
            prescription.setDispenseType(dispenseTypeService.getDispenseTypeByDescription("Dispensa Mensal (DM)"));
        createPrescription(prescription);

        prescribedDrugService.savePrescribedDrug(prescription, requireNonNull(patient.get("jsonprescribeddrugs")).toString());
    }

}
