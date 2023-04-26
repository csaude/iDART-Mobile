package mz.org.fgh.idartlite.view.prescription;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.adapter.recyclerview.listable.Listble;
import mz.org.fgh.idartlite.adapter.recyclerview.listable.ListbleRecycleViewAdapter;
import mz.org.fgh.idartlite.adapter.spinner.listable.ListableSpinnerAdapter;
import mz.org.fgh.idartlite.base.activity.BaseActivity;
import mz.org.fgh.idartlite.base.viewModel.BaseViewModel;
import mz.org.fgh.idartlite.common.ApplicationStep;
import mz.org.fgh.idartlite.databinding.ActivityPrescriptionBinding;
import mz.org.fgh.idartlite.listener.dialog.IDialogListener;
import mz.org.fgh.idartlite.listener.dialog.IDiseaseTypeDialogListener;
import mz.org.fgh.idartlite.model.DiseaseType;
import mz.org.fgh.idartlite.model.DispenseType;
import mz.org.fgh.idartlite.model.Drug;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.PrescribedDrug;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.model.TherapeuticLine;
import mz.org.fgh.idartlite.model.TherapeuticRegimen;
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.about.AboutActivity;
import mz.org.fgh.idartlite.view.dispense.CreateDispenseActivity;
import mz.org.fgh.idartlite.view.patientPanel.PatientPanelActivity;
import mz.org.fgh.idartlite.view.patientPanel.PrescriptionFragment;
import mz.org.fgh.idartlite.viewmodel.prescription.PrescriptionVM;

public class PrescriptionActivity extends BaseActivity implements IDialogListener {

    private ActivityPrescriptionBinding prescriptionBinding;

    private RecyclerView rcvSelectedDrugs;

    private ListbleRecycleViewAdapter listbleRecycleViewAdapter;
    private ListableSpinnerAdapter drugArrayAdapter;
    private ListableSpinnerAdapter dispenseTypeArrayAdapter;
    private ListableSpinnerAdapter lineArrayAdapter;
    private ListableSpinnerAdapter regimenArrayAdapter;
    private ListableSpinnerAdapter motiveAdapter;
    private ListableSpinnerAdapter durationAdapter;
    private ListableSpinnerAdapter prophylaxyFollowUpAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prescriptionBinding = DataBindingUtil.setContentView(this, R.layout.activity_prescription);

        rcvSelectedDrugs = prescriptionBinding.rcvSelectedDrugs;

        prescriptionBinding.drugsDataLyt.setVisibility(View.GONE);
        prescriptionBinding.urgentLyt.setVisibility(View.GONE);
        prescriptionBinding.ibtnUrgent.animate().setDuration(200).rotation(180);
        prescriptionBinding.ibtnDrugs.animate().setDuration(200).rotation(180);

        getRelatedViewModel().setInitialDataVisible(true);
        getRelatedViewModel().setDrugDataVisible(false);

        Intent intent = this.getIntent();
        if(intent != null){
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                getRelatedViewModel().getPrescription().setPatient((Patient) bundle.getSerializable("patient"));
                if (getRelatedViewModel().getSelectedOption() != null) {
                    getRelatedViewModel().getPrescription().setDiseaseType(getRelatedViewModel().getDiseaseTypeByCode(getRelatedViewModel().getSelectedOption()));
                }
                if (getRelatedViewModel().getPrescription().getPatient() == null){
                    getRelatedViewModel().setPrescription((Prescription) bundle.getSerializable("prescription"));
                    if (getRelatedViewModel().getPrescription() == null) {
                        throw new RuntimeException(getString(R.string.no_patient_or_prescription));
                    }else {
                        try {
                            getRelatedViewModel().loadPrescribedDrugsOfPrescription();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            Utilities.displayAlertDialog(PrescriptionActivity.this, getString(R.string.error_on_loading_data)+e.getLocalizedMessage()).show();
                        }
                    }
                }

            }else {
                throw new RuntimeException(getString(R.string.no_patient_or_prescription));
            }
        }


        if (getApplicationStep().isApplicationstepCreate() || getApplicationStep().isApplicationStepEdit()){
            getRelatedViewModel().setViewListRemoveButton(true);
        }
        if(getApplicationStep().isApplicationStepEdit())
        {
            this.getRelatedViewModel().checkInEditIfPrescriptionMustBeSpecial();
        }

        if (getApplicationStep().isApplicationstepCreate() && !getRelatedViewModel().checkIfPatientHasPrescriptionsByDiseaseType()){
            try {


                getRelatedViewModel().checkIfMustBeUrgentPrescription();

                getRelatedViewModel().loadLastPatientPrescription();
                getRelatedViewModel().getPrescription().setPrescriptionDate(DateUtilities.getCurrentDate());
                getRelatedViewModel().getPrescription().setExpiryDate(null);
            } catch (SQLException e) {
                e.printStackTrace();
                Utilities.displayAlertDialog(PrescriptionActivity.this, getString(R.string.error_on_loading_data)+e.getLocalizedMessage()).show();
            }
        }

        prescriptionBinding.setViewModel(getRelatedViewModel());

        getRelatedViewModel().loadDrugs();

        populateForm();

        if(!getRelatedViewModel().checkIfPatientHasPrescriptionsByDiseaseType()){
            loadSelectedPrescriptionToForm();


        }
        if (!getRelatedViewModel().getPrescription().getDiseaseType().getCode().equalsIgnoreCase("TARV")) {
            this.prescriptionBinding.lblLinhaTerap.setVisibility(View.GONE);
            this.prescriptionBinding.spnLine.setVisibility(View.GONE);
            this.prescriptionBinding.lblProphyFollow.setVisibility(View.VISIBLE);
            this.prescriptionBinding.spnProphyFollow.setVisibility(View.VISIBLE);
        }
        changeMotiveSpinnerStatus(getRelatedViewModel().getPrescription().isUrgent());


        prescriptionBinding.prescriptionDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mYear, mMonth, mDay;

                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(PrescriptionActivity.this, new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                getRelatedViewModel().setPrescriptionDate(DateUtilities.createDate(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year, DateUtilities.DATE_FORMAT));
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        prescriptionBinding.autCmpDrugs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                getRelatedViewModel().setSelectedDrug((Listble) adapterView.getItemAtPosition(pos));
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (getApplicationStep().isApplicationStepDisplay()){
            disableAllSpinners();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadSelectedPrescriptionToForm() {
        if (getRelatedViewModel().getSelectedDrugs() == null) getRelatedViewModel().setSelectedDrugs(new ArrayList<>());

        int i = 1;
        for (PrescribedDrug prescribedDrug : getRelatedViewModel().getPrescription().getPrescribedDrugs()){
            Drug d = prescribedDrug.getDrug();
            d.setListPosition(i);
            d.setListType(Listble.PRESCRIPTION_DRUG_LISTING);
            i++;
            getRelatedViewModel().getSelectedDrugs().add(d);
        }
        displaySelectedDrugs();
    }

    public void populateDrugs(List<Drug> drugs){
        drugArrayAdapter = new ListableSpinnerAdapter(this, R.layout.simple_auto_complete_item, drugs);
        prescriptionBinding.autCmpDrugs.setAdapter(drugArrayAdapter);
        prescriptionBinding.autCmpDrugs.setThreshold(1);
    }

    private void populateForm() {
        try {
            List<TherapeuticRegimen> therapeuticRegimenList = new ArrayList<>();
            therapeuticRegimenList.add(new TherapeuticRegimen());
            therapeuticRegimenList.addAll(getRelatedViewModel().getAllTherapeuticRegimenByDiseaseType(this.getRelatedViewModel().getPrescription().getDiseaseType()));

            List<TherapeuticLine> therapeuticLines = new ArrayList<>();
            therapeuticLines.add(new TherapeuticLine());
            therapeuticLines.addAll(getRelatedViewModel().getAllTherapeuticLines());

            List<DispenseType> dispenseTypes = new ArrayList<>();
            dispenseTypes.add(new DispenseType());
            dispenseTypes.addAll(getRelatedViewModel().getAllDispenseTypes());

            dispenseTypeArrayAdapter = new ListableSpinnerAdapter(this, R.layout.simple_auto_complete_item, dispenseTypes);
            prescriptionBinding.spnDispenseType.setAdapter(dispenseTypeArrayAdapter);
            prescriptionBinding.setDispenseTypeAdapter(dispenseTypeArrayAdapter);

            lineArrayAdapter = new ListableSpinnerAdapter(this, R.layout.simple_auto_complete_item, therapeuticLines);
            prescriptionBinding.spnLine.setAdapter(lineArrayAdapter);
            prescriptionBinding.setLineAdapter(lineArrayAdapter);

            regimenArrayAdapter = new ListableSpinnerAdapter(this, R.layout.simple_auto_complete_item, therapeuticRegimenList);
            prescriptionBinding.spnRegime.setAdapter(regimenArrayAdapter);
            prescriptionBinding.setRegimenAdapter(regimenArrayAdapter);

            durationAdapter = new ListableSpinnerAdapter(this, R.layout.simple_auto_complete_item, getRelatedViewModel().getDurations());
            prescriptionBinding.spnDuration.setAdapter(durationAdapter);
            prescriptionBinding.setDurationAdapter(durationAdapter);

            motiveAdapter = new ListableSpinnerAdapter(this, R.layout.simple_auto_complete_item, getRelatedViewModel().getMotives());
            prescriptionBinding.spnReson.setAdapter(motiveAdapter);
            prescriptionBinding.setMotiveAdapter(motiveAdapter);

            prophylaxyFollowUpAdapter = new ListableSpinnerAdapter(this, R.layout.simple_auto_complete_item, getRelatedViewModel().getProphylaxyFollowUp());
            prescriptionBinding.spnProphyFollow.setAdapter(prophylaxyFollowUpAdapter);
            prescriptionBinding.setProphylaxyFollowUpAdapter(prophylaxyFollowUpAdapter);


        } catch (SQLException e) {
            Utilities.displayAlertDialog(PrescriptionActivity.this, getString(R.string.error_loading_form_data)+e.getMessage());
            e.printStackTrace();
        }
    }

    public void changeFormSectionVisibility(View view) {
        if (view.equals(prescriptionBinding.initialData)){
            if (prescriptionBinding.initialDataLyt.getVisibility() == View.VISIBLE){
                switchLayout();
                prescriptionBinding.ibtnInitialData.animate().setDuration(200).rotation(180);
                Utilities.collapse(prescriptionBinding.initialDataLyt);
            }else {
                switchLayout();
                prescriptionBinding.ibtnInitialData.animate().setDuration(200).rotation(0);
                Utilities.expand(prescriptionBinding.initialDataLyt);
            }
        }else if (view.equals(prescriptionBinding.txvDrugs)){
            if (prescriptionBinding.drugsDataLyt.getVisibility() == View.VISIBLE){
                switchLayout();
                prescriptionBinding.ibtnDrugs.animate().setDuration(200).rotation(180);
                Utilities.collapse(prescriptionBinding.drugsDataLyt);
            }else {
                prescriptionBinding.ibtnDrugs.animate().setDuration(200).rotation(0);
                Utilities.expand(prescriptionBinding.drugsDataLyt);
                switchLayout();
            }
        }else if (view.equals(prescriptionBinding.txvUrgent)){
            if (prescriptionBinding.urgentLyt.getVisibility() == View.VISIBLE){
                prescriptionBinding.ibtnUrgent.animate().setDuration(200).rotation(180);
                Utilities.collapse(prescriptionBinding.urgentLyt);
                switchLayout();
            }else {
                prescriptionBinding.ibtnUrgent.animate().setDuration(200).rotation(0);
                Utilities.expand(prescriptionBinding.urgentLyt);
                switchLayout();
            }
        }
    }

    private void switchLayout(){
        getRelatedViewModel().setInitialDataVisible(!getRelatedViewModel().isInitialDataVisible());
        getRelatedViewModel().setDrugDataVisible(!getRelatedViewModel().isDrugDataVisible());
    }

    @Override
    public BaseViewModel initViewModel() {
        return new ViewModelProvider(this).get(PrescriptionVM.class);
    }

    @Override
    public PrescriptionVM getRelatedViewModel() {
        return (PrescriptionVM) super.getRelatedViewModel();
    }

    public void displaySelectedDrugs(){
        if (listbleRecycleViewAdapter != null) {
            listbleRecycleViewAdapter.notifyDataSetChanged();
        }else {
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            rcvSelectedDrugs.setLayoutManager(mLayoutManager);
            rcvSelectedDrugs.setItemAnimator(new DefaultItemAnimator());
            rcvSelectedDrugs.addItemDecoration(new DividerItemDecoration(getApplicationContext(), 0));

            listbleRecycleViewAdapter = new ListbleRecycleViewAdapter(rcvSelectedDrugs, getRelatedViewModel().getSelectedDrugs(), this);
            rcvSelectedDrugs.setAdapter(listbleRecycleViewAdapter);
        }
    }

    public void doAfterPrescriptionSave(){
        Map<String, Object> params = new HashMap<>();
        params.put("prescription", getRelatedViewModel().getPrescription());
        params.put("user", getCurrentUser());
        params.put("clinic", getCurrentClinic());
        params.put("step", ApplicationStep.STEP_CREATE);
        params.put("comingFromPrescription", true);
        params.put("clinicSector", getClinicSector());
        nextActivityFinishingCurrent(CreateDispenseActivity.class,params);
    }

    @Override
    public void doOnConfirmed() {
        if (getApplicationStep().isApplicationStepSave()) {
            doAfterPrescriptionSave();
        }else {
            doOnDeny();
        }
    }

    @Override
    public void doOnDeny() {
        Map<String, Object> params = new HashMap<>();
        params.put("patient", getRelatedViewModel().getPrescription().getPatient());
        params.put("user", getCurrentUser());
        params.put("clinic", getCurrentClinic());
        params.put("requestedFragment", PrescriptionFragment.FRAGMENT_CODE_PRESCRIPTION);
        nextActivityFinishingCurrent(PatientPanelActivity.class,params);
    }

    public void changeMotiveSpinnerStatus(boolean b) {
        prescriptionBinding.spnReson.setEnabled(b);
    }

    private void changeAllSpinnersStatus(boolean status){
        prescriptionBinding.spnDispenseType.setEnabled(status);
        prescriptionBinding.spnLine.setEnabled(status);
        prescriptionBinding.spnRegime.setEnabled(status);
        prescriptionBinding.autCmpDrugs.setEnabled(status);
        prescriptionBinding.spnDuration.setEnabled(status);
        prescriptionBinding.spnReson.setEnabled(status);
        prescriptionBinding.spnProphyFollow.setEnabled(status);
    }

    public ActivityPrescriptionBinding getPrescriptionBinding() {
        return prescriptionBinding;
    }

    public void disableAllSpinners(){
        changeAllSpinnersStatus(false);
    }

    public void enableAllSpinners(){
        changeAllSpinnersStatus(true);
    }



}