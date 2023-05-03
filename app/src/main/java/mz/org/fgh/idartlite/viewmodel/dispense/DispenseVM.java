package mz.org.fgh.idartlite.viewmodel.dispense;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mz.org.fgh.idartlite.BR;
import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.base.activity.BaseActivity;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.base.viewModel.BaseViewModel;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.DiseaseType;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.DispensedDrug;
import mz.org.fgh.idartlite.model.Drug;
import mz.org.fgh.idartlite.model.Episode;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.PrescribedDrug;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.model.TherapeuticRegimen;
import mz.org.fgh.idartlite.model.patient.PatientAttribute;
import mz.org.fgh.idartlite.service.dispense.DispenseDrugService;
import mz.org.fgh.idartlite.service.dispense.DispenseService;
import mz.org.fgh.idartlite.service.dispense.IDispenseDrugService;
import mz.org.fgh.idartlite.service.dispense.IDispenseService;
import mz.org.fgh.idartlite.service.drug.DiseaseTypeService;
import mz.org.fgh.idartlite.service.drug.DrugService;
import mz.org.fgh.idartlite.service.drug.IDiseaseTypeService;
import mz.org.fgh.idartlite.service.drug.IDrugService;
import mz.org.fgh.idartlite.service.episode.EpisodeService;
import mz.org.fgh.idartlite.service.episode.IEpisodeService;
import mz.org.fgh.idartlite.service.patient.IPatientAttributeService;
import mz.org.fgh.idartlite.service.patient.PatientAttributeService;
import mz.org.fgh.idartlite.service.prescription.IPrescribedDrugService;
import mz.org.fgh.idartlite.service.prescription.IPrescriptionService;
import mz.org.fgh.idartlite.service.prescription.PrescribedDrugService;
import mz.org.fgh.idartlite.service.prescription.PrescriptionService;
import mz.org.fgh.idartlite.service.stock.IStockService;
import mz.org.fgh.idartlite.service.stock.StockService;
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.dispense.CreateDispenseActivity;
import mz.org.fgh.idartlite.view.patientPanel.DispenseFragment;
import mz.org.fgh.idartlite.view.patientPanel.PatientPanelActivity;
import mz.org.fgh.idartlite.viewmodel.prescription.PrescriptionVM;

public class DispenseVM extends BaseViewModel {

    private IDispenseService dispenseService;

    private IDiseaseTypeService diseaseService;

    private Dispense dispense;

    private boolean initialDataVisible;

    private boolean drugDataVisible;

    private IPrescriptionService prescriptionService;

    private IDispenseDrugService dispenseDrugService;

    private IDrugService drugService;

    private IStockService stockService;

    private IEpisodeService episodeService;

    private IPrescribedDrugService prescribedDrugService;
    private Episode episode;

    private IPatientAttributeService attributeService;

    protected IDiseaseTypeService diseaseTypeService;

    private static String selectedOption;

    public DispenseVM(@NonNull Application application) {
        super(application);

        this.dispense = new Dispense();
        dispenseService = new DispenseService(application, getCurrentUser());
        prescriptionService = new PrescriptionService(application, getCurrentUser());
        dispenseDrugService = new DispenseDrugService(application, getCurrentUser());
        diseaseService = new DiseaseTypeService(application, getCurrentUser());
        this.drugService = new DrugService(application, getCurrentUser());
        this.stockService = new StockService(application, getCurrentUser());
        this.episodeService = new EpisodeService(application, getCurrentUser());
        this.prescribedDrugService = new PrescribedDrugService(application, getCurrentUser());
        this.attributeService = new PatientAttributeService(application, getCurrentUser());
        this.diseaseTypeService = new DiseaseTypeService(application, getCurrentUser());
        this.setViewListRemoveButton(true);

    }

    @Override
    protected IBaseService initRelatedService() {
        return getServiceProvider().get(DispenseService.class);
    }

    @Override
    protected BaseModel initRecord() {
        return null;
    }

    @Override
    protected void initFormData() {

    }

    public List<Dispense> gatAllOfPatient(Patient patient) throws SQLException {
        return dispenseService.getAllOfPatient(patient);
    }

    public void deleteDispense(Dispense dispense) throws SQLException {
        this.dispenseService.deleteDispense(dispense);
    }

    public Dispense getDispense() {
        return this.dispense;
    }

    public void setDispense(Dispense dispense) {
        this.dispense = dispense;
    }

    @Bindable
    public boolean isInitialDataVisible() {
        return initialDataVisible;
    }

    public void setInitialDataVisible(boolean initialDataVisible) {
        this.initialDataVisible = initialDataVisible;
        notifyPropertyChanged(BR.initialDataVisible);
    }

    @Bindable
    public boolean isDrugDataVisible() {
        return drugDataVisible;
    }


    public void setDrugDataVisible(boolean drugDataVisible) {
        this.drugDataVisible = drugDataVisible;
        notifyPropertyChanged(BR.drugDataVisible);
    }

    public void create(Dispense dispense) throws SQLException {
        this.dispenseService.createDispense(dispense);
    }

    public Prescription getLastPatientPrescriptionByDiseaseType(Patient patient, DiseaseType diseaseType) throws SQLException {

        return this.prescriptionService.getLastPatientPrescriptionByDiseaseType(patient, diseaseType);
    }

    public Dispense getLastPatientDispense(Prescription prescription) throws SQLException {

        return this.dispenseService.getLastDispenseFromPrescription(prescription);
    }

    public List<Drug> getAllDrugs() throws SQLException {
        return drugService.getAll();
    }

    public List<Drug> getAllDrugsFromPrescritionRegimen() throws SQLException {
        return drugService.getAllByTherapeuticRegimen((TherapeuticRegimen) this.dispense.getPrescription().getTherapeuticRegimen());
    }

    public DiseaseType defaultDiseaseType() throws SQLException {
        return diseaseService.getDiseaseTypeByCode("TARV");
    }

    public List<Drug> getDrugsWithoutRectParanthesis(List<Drug> drugs) throws SQLException {
        return drugService.getDrugsWithoutRectParanthesis(drugs);
    }


    public List<Stock> getAllStocksByClinicAndDrug(Clinic clinic, Drug drug) {

        List<Stock> stocks = new ArrayList<>();
        try {
            stocks = this.stockService.getAllStocksByClinicAndDrug(clinic, drug);
        } catch (SQLException e) {
            e.printStackTrace();
            Utilities.displayAlertDialog(getRelatedActivity(), getRelatedActivity().getString(R.string.find_stock_list_error) + e.getLocalizedMessage()).show();
        }

        return stocks;
    }

    @Override
    public BaseActivity getRelatedActivity() {
        return super.getRelatedActivity();
    }

    public void save() {

        ((CreateDispenseActivity) getRelatedActivity()).loadFormData();

        try {
            this.dispense.getPrescription().getPatient().setAttributes(attributeService.getAllOfPatient(this.dispense.getPrescription().getPatient()));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        String validationErrors = this.patientHasEndingEpisode();


        if (!Utilities.stringHasValue(validationErrors)) {

            validationErrors = ((CreateDispenseActivity) getRelatedActivity()).validateDispenseDurationByPrescription();

            if (!Utilities.stringHasValue(validationErrors)) {

                validationErrors = ((CreateDispenseActivity) getRelatedActivity()).validateStockForSelectedDrugs();

                if (!Utilities.stringHasValue(validationErrors)) {

                    validationErrors = this.dispense.validate();

                    if (!Utilities.stringHasValue(validationErrors)) {
                        try {
                            if (dispense.getId() > 0) {
                                this.editDispenseAndRemovePrior();
                            } else {

                                String secondValidationErrors = this.dispenseOnDateBeforePickupDate();

                                if (!Utilities.stringHasValue(secondValidationErrors)) {

                                    if (dispense.getId() <= 0 && this.dispense.getPrescription().getPatient().isFaltosoOrAbandono()) {
                                        this.dispense.getPrescription().getPatient().setEpisodes(episodeService.getAllEpisodesByPatient(this.dispense.getPrescription().getPatient()));
                                        generateClosureEpisode(this.dispense.getPrescription().getPatient());
                                    } else this.episode = null;

                                    String patientNid = this.dispense.getPrescription().getPatient().getNid();
                                     this.dispenseService.saveOrUpdateDispense(dispense, mustValidateStock());
                                     saveClosureEpisodeForFaltosoOrAbandono();
                                    Utilities.displayAlertDialog(getRelatedActivity(), "Dispensa para o paciente " + patientNid + " efectuado com sucesso!", ((CreateDispenseActivity) getRelatedActivity())).show();
                                } else {
                                    Utilities.displayAlertDialog(getRelatedActivity(), secondValidationErrors).show();
                                }
                            }

                        } catch (SQLException e) {
                            e.printStackTrace();
                            Utilities.displayAlertDialog(getRelatedActivity(), getRelatedActivity().getString(R.string.save_error_msg) + e.getLocalizedMessage()).show();
                        }
                    } else {
                        Utilities.displayAlertDialog(getRelatedActivity(), validationErrors).show();
                    }
                } else {
                    Utilities.displayAlertDialog(getRelatedActivity(), validationErrors).show();
                }
            } else {
                Utilities.displayAlertDialog(getRelatedActivity(), validationErrors).show();
            }
        } else {
            Utilities.displayAlertDialog(getRelatedActivity(), validationErrors).show();
        }

    }

    private void saveClosureEpisodeForFaltosoOrAbandono() throws SQLException {
        if (this.episode != null && this.dispense.getPrescription().getPatient().isFaltosoOrAbandono()) {
            episodeService.save(this.episode);
        }
    }

    private boolean mustValidateStock() {
        if (this.dispense.getPrescription().getPatient().isFaltosoOrAbandono() && getCurrentClinic().isSanitaryUnit() && !getCurrentClinicSector().mustValidateStock()) return false;

        return true;
    }

    private void generateClosureEpisode(Patient patient) {
        this.episode = new Episode();
        this.episode.setEpisodeDate(DateUtilities.getCurrentDate());
        this.episode.setPatient(patient);
        this.episode.setSanitaryUnit(patient.getEpisodes().get(0).getSanitaryUnit());
        this.episode.setUsUuid(patient.getEpisodes().get(0).getUsUuid());
        this.episode.setStopReason(PatientAttribute.PATIENT_DISPENSATION_STATUS_FALTOSO);
        this.episode.setNotes(PatientAttribute.PATIENT_DISPENSATION_STATUS_FALTOSO + " Dispensa efectuada");
        this.episode.setSyncStatus(BaseModel.SYNC_SATUS_READY);
        this.episode.setUuid(UUID.randomUUID().toString());
    }

    public List<DispensedDrug> findDispensedDrugsByDispenseId(int id) throws SQLException {

        return this.dispenseDrugService.findDispensedDrugByDispenseId(id);
    }

    public List<Dispense> getAllDispensesByPrescription(Prescription prescription) throws SQLException {

        return this.dispenseService.getAllNotVoidedDispenseByPrescription(prescription);
    }

    public List<PrescribedDrug> getAllPrescribedDrugsByPrescription(Prescription prescription) throws SQLException {

        return this.prescribedDrugService.getAllByPrescription(prescription);
    }

    public void backToPreviusActivity() {
        Map<String, Object> params = new HashMap<>();
        params.put("patient", getDispense().getPrescription().getPatient());
        params.put("user", getCurrentUser());
        params.put("clinic", getCurrentClinic());
        params.put("requestedFragment", DispenseFragment.FRAGMENT_CODE_DISPENSE);
        getRelatedActivity().nextActivity(PatientPanelActivity.class, params);
    }

    @Override
    public void preInit() {

    }

    public String dispenseOnDateBeforePickupDate() {
        try {
            Prescription prescription = this.getLastPatientPrescriptionByDiseaseType(getDispense().getPrescription().getPatient(), getDispense().getPrescription().getDiseaseType());
            Dispense dispense = this.getLastPatientDispense(prescription);

            if (dispense != null) {
                if (DateUtilities.dateDiff(this.dispense.getPickupDate(), dispense.getNextPickupDate(), DateUtilities.DAY_FORMAT) < -2) {
                    return getRelatedActivity().getString(R.string.cant_dispense_patient_has_drugs);
                }
            }
        } catch (SQLException sq) {
            sq.printStackTrace();
        }

        return "";
    }

    public String dispenseCanBeEdited() throws SQLException {
        if (this.dispense.isSyncStatusSent(this.dispense.getSyncStatus()))
            return getRelatedActivity().getString(R.string.cant_edit_synced_dispense);
        return "";
    }

    public String checkDispenseRemoveConditions() {
        if (this.dispense.isSyncStatusSent(this.dispense.getSyncStatus()))
            return getRelatedActivity().getString(R.string.dispense_cant_be_removed_msg);
        return "";
    }

    public String patientHasEndingEpisode() {
        boolean hasEndingEpisode = false;
        try {
            hasEndingEpisode = this.episodeService.patientHasEndingEpisode(getDispense().getPrescription().getPatient());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (hasEndingEpisode) {
            return getRelatedActivity().getString(R.string.dispense_has_final_episode_cant_be_edit);
        }
        return "";
    }

    public boolean patientHasEpisodioFim(Patient patient) {
        try {
            return this.episodeService.patientHasEndingEpisode(patient);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

        public String dispenseCanBeReturned()  {
        if (this.dispense.isReturned())
            return getRelatedActivity().getString(R.string.cant_return_dispense);
        return "";
    }

    @Override
    public void doOnConfirmed() {
        if (getCurrentStep().isApplicationStepEdit()) {
            Dispense dispense = this.dispense;
            try {
                this.dispenseService.deleteDispense(dispense);
                dispense.setId(0);
                this.dispenseService.saveOrUpdateDispense(dispense, mustValidateStock());
                this.backToPreviusActivity();
            } catch (SQLException ex) {

            }
        }
    }

    @Override
    public void doOnDeny() {
        if (getCurrentStep().isApplicationStepEdit()) {
            getCurrentStep().changeToList();
            this.backToPreviusActivity();
        }
    }

    public void editDispenseAndRemovePrior() {
        getCurrentStep().changeToEdit();

        StringBuilder dispensedDrugsList = new StringBuilder();

        Dispense dispenseInEditMode = ((CreateDispenseActivity) getRelatedActivity()).getDispenseSelectedForEdit();
        List<DispensedDrug> dispensedDrugs = new ArrayList<>();
        try {
            dispensedDrugs = this.findDispensedDrugsByDispenseId(dispenseInEditMode.getId());
        } catch (SQLException ex) {
        }

        String dataLevantamento = "Data Levantamento: " + DateUtilities.formatToDDMMYYYY(dispenseInEditMode.getPickupDate(), "/");
        String duracao = "Duração: " + Utilities.parseSupplyToLabel(dispenseInEditMode.getSupply());
        String dataProximoLevantamento = "Data Próximo Levantamento: " + DateUtilities.formatToDDMMYYYY(dispenseInEditMode.getNextPickupDate(), "/");

        for (DispensedDrug dd : dispensedDrugs
        ) {
            Drug drug = dd.getStock().getDrug();
            dispensedDrugsList.append(drug.getDescription() + "\n");
        }

        String detalhesAviamento = getRelatedActivity().getString(R.string.dispense_drug_detail_list);
        String listaDeMedicamentosDispensados = getRelatedActivity().getString(R.string.dispensed_drug_list);
        String gostariaDeRemoverAdispensaAnterior = getRelatedActivity().getString(R.string.would_you_like_remove_prior_dispense);

        String editWillRemoveSelectedDispense = detalhesAviamento + "\n\n" + dataLevantamento +
                "\n" + duracao + "\n" + dataProximoLevantamento + "\n\n" + listaDeMedicamentosDispensados + "\n"
                + dispensedDrugsList + "\n" + gostariaDeRemoverAdispensaAnterior;

        Utilities.displayConfirmationDialog(getRelatedActivity(), editWillRemoveSelectedDispense, getRelatedActivity().getString(R.string.yes), getRelatedActivity().getString(R.string.no), DispenseVM.this).show();
    }

    public void changeDataViewStatus(View view){
        ((CreateDispenseActivity) getRelatedActivity()).changeFormSectionVisibility(view);
    }

    public String getPrescriptionDetailsLabel(){
       return "Prescricao:" +" "+dispense.getPrescription().getUiId();
    }

    public String getPrescriptionTimeLeft(){
        return "Mes(es) Restantes de Validade: "+dispense.getPrescription().getTimeLeftInMonths();
    }

    public DiseaseType getDiseaseTypeByCode (String code) {
        try {
            return diseaseTypeService.getDiseaseTypeByCode(code.toUpperCase());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}
