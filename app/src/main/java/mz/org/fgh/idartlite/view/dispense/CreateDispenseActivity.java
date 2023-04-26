package mz.org.fgh.idartlite.view.dispense;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;

import androidx.annotation.RequiresApi;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.adapter.recyclerview.listable.Listble;
import mz.org.fgh.idartlite.adapter.recyclerview.listable.ListbleRecycleViewAdapter;
import mz.org.fgh.idartlite.adapter.spinner.listable.ListableSpinnerAdapter;
import mz.org.fgh.idartlite.base.activity.BaseActivity;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.viewModel.BaseViewModel;
import mz.org.fgh.idartlite.databinding.ActivityCreateDispenseBinding;
import mz.org.fgh.idartlite.listener.dialog.IDialogListener;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.DispensedDrug;
import mz.org.fgh.idartlite.model.Drug;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.PrescribedDrug;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.SimpleValue;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.patientPanel.DispenseFragment;
import mz.org.fgh.idartlite.view.patientPanel.PatientPanelActivity;
import mz.org.fgh.idartlite.viewmodel.dispense.DispenseVM;

public class CreateDispenseActivity extends BaseActivity implements IDialogListener {

    private ActivityCreateDispenseBinding activityCreateDispenseBinding;

    private List<Listble> selectedDrugs;

    private RecyclerView rcvSelectedDrugs;

    private ListbleRecycleViewAdapter listbleRecycleViewAdapter;

    private Patient patient;

    private Prescription prescription;

    private ArrayAdapter<SimpleValue> valorSimplesArrayAdapter;

    private Drug selectedDrug;

    private Dispense dispenseSelectedForEdit;

    private List<Dispense> dispenseList;

    private boolean comingFromPrescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityCreateDispenseBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_dispense);

        rcvSelectedDrugs = activityCreateDispenseBinding.rcvSelectedDrugs;

        activityCreateDispenseBinding.drugsDataLyt.setVisibility(View.GONE);
        getRelatedViewModel().setInitialDataVisible(true);
        getRelatedViewModel().setDrugDataVisible(false);

        Intent intent = this.getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                getRelatedViewModel().getDispense().setPrescription((Prescription) bundle.getSerializable("prescription"));
                activityCreateDispenseBinding.setViewModel(getRelatedViewModel());

                this.prescription = getRelatedViewModel().getDispense().getPrescription();
                comingFromPrescription= (boolean) bundle.getSerializable("comingFromPrescription");

                if (prescription == null) {

                    this.setPatient((Patient) bundle.getSerializable("patient"));

                    try {
                        this.prescription = getRelatedViewModel().getLastPatientPrescriptionByDiseaseType(this.getPatient(),this.prescription.getDiseaseType());

                        dispenseList = (List<Dispense>) bundle.getSerializable("dispenses");

                        // prescription.setDispenses(dispenseList);
                        this.prescription.setDispenses(getRelatedViewModel().getAllDispensesByPrescription(this.prescription));

                        getRelatedViewModel().getDispense().setPrescription((this.prescription));


                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                if (bundle.getSerializable("dispense") != null) {
                    getRelatedViewModel().setDispense((Dispense) bundle.getSerializable("dispense"));
                    this.setDispenseSelectedForEdit(getRelatedViewModel().getDispense());
                }
            }
        }

        populateForm();

        loadSelectedDispenseToForm();

        if (getApplicationStep().isApplicationstepCreate()) {
            this.loadPrescribedDrugsOfLastPatientPrescription();
            getRelatedViewModel().getDispense().setPickupDate(DateUtilities.getCurrentDate());
            getRelatedViewModel().getDispense().setNextPickupDate(null);
        }

        if (getApplicationStep().isApplicationStepDisplay())
            getRelatedViewModel().setViewListRemoveButton(false);

        activityCreateDispenseBinding.dispenseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mYear, mMonth, mDay;

                final Calendar c = Calendar.getInstance();
                c.setTime(DateUtilities.createDate(activityCreateDispenseBinding.dispenseDate.getText().toString(),DateUtilities.DATE_FORMAT));
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateDispenseActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        activityCreateDispenseBinding.dispenseDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                        int duracaoDoAviamento = ((SimpleValue) activityCreateDispenseBinding.spnDuration.getSelectedItem()).getId();

                        String nextPickUpDate = calculateNextPickUpDate(duracaoDoAviamento);
                        if (duracaoDoAviamento != 0)
                            activityCreateDispenseBinding.nextPickupDate.setText(nextPickUpDate);
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        activityCreateDispenseBinding.dispenseDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    int mYear, mMonth, mDay;

                    final Calendar c = Calendar.getInstance();
                    c.setTime(DateUtilities.createDate(activityCreateDispenseBinding.dispenseDate.getText().toString(),DateUtilities.DATE_FORMAT));
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(CreateDispenseActivity.this, new DatePickerDialog.OnDateSetListener() {

                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                            activityCreateDispenseBinding.dispenseDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                            int duracaoDoAviamento = ((SimpleValue) activityCreateDispenseBinding.spnDuration.getSelectedItem()).getId();

                            String nextPickUpDate = calculateNextPickUpDate(duracaoDoAviamento);
                            if (duracaoDoAviamento != 0)
                                activityCreateDispenseBinding.nextPickupDate.setText(nextPickUpDate);
                        }
                    }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                }
            }
        });

        activityCreateDispenseBinding.nextPickupDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mYear, mMonth, mDay;

                Calendar c = Calendar.getInstance();
                c.setTime(DateUtilities.createDate(activityCreateDispenseBinding.nextPickupDate.getText().toString(),DateUtilities.DATE_FORMAT));
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateDispenseActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        activityCreateDispenseBinding.nextPickupDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        activityCreateDispenseBinding.nextPickupDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    int mYear, mMonth, mDay;

                    Calendar c = Calendar.getInstance();
                    c.setTime(DateUtilities.createDate(activityCreateDispenseBinding.nextPickupDate.getText().toString(),DateUtilities.DATE_FORMAT));
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(CreateDispenseActivity.this, new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                            activityCreateDispenseBinding.nextPickupDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                        }
                    }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                }
            }
        });

        activityCreateDispenseBinding.spnDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int duracaoDoAviamento = ((SimpleValue) adapterView.getItemAtPosition(i)).getId();

                String nextPickUpDate = calculateNextPickUpDate(duracaoDoAviamento);

                if (duracaoDoAviamento != 0)
                    activityCreateDispenseBinding.nextPickupDate.setText(nextPickUpDate);

                if (duracaoDoAviamento != 0)
                    setDispenseQuantityForEachSelectedDrug();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        activityCreateDispenseBinding.imvAddSelectedDrug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedDrugs == null) selectedDrugs = new ArrayList<>();

                if (activityCreateDispenseBinding.autCmpDrugs.getText().toString().length() != 0) {
                    Listble listble = selectedDrug;

                    Drug drug = (Drug) listble;

                    if (drug != null) {
                        final int qtdADispensar = getQuantidadeADispensar(drug);
                        drug.setQuantity(qtdADispensar);
                        listble = drug;

                        if (!existsStockToDispense(drug)) {
                            Utilities.displayAlertDialog(CreateDispenseActivity.this, getString(R.string.stock_menor)).show();
                            return;
                        }
                        if (!selectedDrugs.contains(listble)) {
                            listble.setListPosition(selectedDrugs.size() + 1);
                            selectedDrug.setListType(Listble.DISPENSE_DRUG_LISTING);
                            selectedDrugs.add(listble);
                            Collections.sort(selectedDrugs);

                            displaySelectedDrugs();
                            activityCreateDispenseBinding.autCmpDrugs.setText("");
                        } else {

                            Utilities.displayAlertDialog(CreateDispenseActivity.this, getString(R.string.drug_data_duplication_msg)).show();
                        }

                    }else{
                        Utilities.displayAlertDialog(CreateDispenseActivity.this,"O medicamento não existe. Por favor, seleccione um medicamento para adicionar à lista.").show();
                    }
                }
            }
        });

        activityCreateDispenseBinding.autCmpDrugs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                //this is the way to find selected object/item
                selectedDrug = (Drug) adapterView.getItemAtPosition(pos);
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

        if (getRelatedViewModel().getDispense().getPrescription().getTimeLeftInMonths() == 0) {
            activityCreateDispenseBinding.prescriptionLabel.setTextColor(Color.RED);
            activityCreateDispenseBinding.prescriptionLabelTimeLeft.setTextColor(Color.RED);
        }

        //   activityCreateDispenseBinding.spnDuration.setSelection(valorSimplesArrayAdapter.getPosition(SimpleValue.fastCreate(getRelatedViewModel().getDispense().getSupply())));

        if (getApplicationStep().isApplicationStepDisplay()) {
            disableAllSpinners();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String calculateNextPickUpDate(int duracaoDoAviamento) {
        String pickUpdate = activityCreateDispenseBinding.dispenseDate.getText().toString();

        int daysToAdd = 0;

        if (duracaoDoAviamento == 2) {
            daysToAdd = Dispense.DURATION_TWO_WEEKS;
        } else if (duracaoDoAviamento == 4) {
            daysToAdd = Dispense.DURATION_ONE_MONTH;
        } else if (duracaoDoAviamento == 8) {
            daysToAdd = Dispense.DURATION_TWO_MONTHS;
        } else if (duracaoDoAviamento == 12) {
            daysToAdd = Dispense.DURATION_THREE_MONTHS;
        } else if (duracaoDoAviamento == 16) {
            daysToAdd = Dispense.DURATION_FOUR_MONTHS;
        } else if (duracaoDoAviamento == 20) {
            daysToAdd = Dispense.DURATION_FIVE_MONTHS;
        } else if (duracaoDoAviamento == 24) {
            daysToAdd = Dispense.DURATION_SIX_MONTHS;
        }
        String nextPickUpDate = "";

        if (pickUpdate.length() != 0)
            nextPickUpDate = DateUtilities.getDateAfterAddingDaysToGivenDate(pickUpdate, daysToAdd);

        int isWeekend = DateUtilities.isSaturdayOrSunday(nextPickUpDate);

        if (isWeekend == 6) {
            nextPickUpDate = DateUtilities.getDateAfterAddingDaysToGivenDate(nextPickUpDate, 2);
        } else if (isWeekend == 7) {
            nextPickUpDate = DateUtilities.getDateAfterAddingDaysToGivenDate(nextPickUpDate, 1);
        }

        return nextPickUpDate;

    }

    private void setDispenseQuantityForEachSelectedDrug() {
        List<Listble> tempSelectedDrugs = new ArrayList<>();

        tempSelectedDrugs.addAll(this.selectedDrugs);

        this.selectedDrugs = new ArrayList<>();

        int i = 1;
        int qtdADispensar;
        for (Listble selected : tempSelectedDrugs
        ) {
            Drug drug = (Drug) selected;
            qtdADispensar = getQuantidadeADispensar(drug);
            drug.setQuantity(qtdADispensar);
            drug.setListPosition(i);
            i++;
            this.selectedDrugs.add(drug);
            Collections.sort(this.selectedDrugs);
        }
        displaySelectedDrugs();
    }

    private void loadPrescribedDrugsOfLastPatientPrescription() {

        List<PrescribedDrug> prescribedDrugs = this.getPrescribedDrugsByPrescription();
        int i = 1;
        for (PrescribedDrug pd : prescribedDrugs
        ) {
            Drug drug = pd.getDrug();
            drug.setListPosition(i);
            drug.setListType(Listble.DISPENSE_DRUG_LISTING);
            i++;
            selectedDrugs.add(drug);
            Collections.sort(selectedDrugs);
        }
        displaySelectedDrugs();
    }

    private void loadSelectedDispenseToForm() {

        if (getRelatedViewModel().getDispense().getSupply() != 0)
            activityCreateDispenseBinding.spnDuration.setSelection(valorSimplesArrayAdapter.getPosition(SimpleValue.fastCreate(getRelatedViewModel().getDispense().getSupply())));


        if (getRelatedViewModel().getDispense() != null) {

            if (this.selectedDrugs == null) selectedDrugs = new ArrayList<>();

            List<DispensedDrug> dispensedDrugs = new ArrayList<>();

            try {
                dispensedDrugs = getRelatedViewModel().findDispensedDrugsByDispenseId(getRelatedViewModel().getDispense().getId());
            } catch (SQLException ex) {
            }

            int i = 1;
            for (DispensedDrug dispensedDrugDrug : dispensedDrugs) {
                Drug d = dispensedDrugDrug.getStock().getDrug();
                d.setListPosition(i);
                d.setListType(Listble.DISPENSE_DRUG_LISTING);
                i++;
                selectedDrugs.add(d);
            }
            displaySelectedDrugs();

        }
    }

    private void populateForm() {

        try {

            List<Drug> drugs = new ArrayList<>();
            drugs.addAll(getRelatedViewModel().getDrugsWithoutRectParanthesis(getRelatedViewModel().getAllDrugsFromPrescritionRegimen()));

            List<Drug> drugsToDisplay = new ArrayList<>();

            for (Drug drug :
                    drugs) {
                if (this.verifyIfDrugHasAtLeastOneStock(drug))
                    drugsToDisplay.add(drug);
            }


            ArrayAdapter<Drug> drugArrayAdapter = new ListableSpinnerAdapter(this, R.layout.simple_auto_complete_item, drugsToDisplay);
            activityCreateDispenseBinding.autCmpDrugs.setThreshold(1);
            activityCreateDispenseBinding.autCmpDrugs.setAdapter(drugArrayAdapter);

            List<SimpleValue> durations = new ArrayList<>();
            durations.add(new SimpleValue());
            durations.add(SimpleValue.fastCreate(2, Prescription.DURATION_TWO_WEEKS));
            durations.add(SimpleValue.fastCreate(4, Prescription.DURATION_ONE_MONTH));
            durations.add(SimpleValue.fastCreate(8, Prescription.DURATION_TWO_MONTHS));
            durations.add(SimpleValue.fastCreate(12, Prescription.DURATION_THREE_MONTHS));
            durations.add(SimpleValue.fastCreate(16, Prescription.DURATION_FOUR_MONTHS));
            durations.add(SimpleValue.fastCreate(20, Prescription.DURATION_FIVE_MONTHS));
            durations.add(SimpleValue.fastCreate(24, Prescription.DURATION_SIX_MONTHS));

            valorSimplesArrayAdapter = new ArrayAdapter<SimpleValue>(getApplicationContext(), android.R.layout.simple_spinner_item, durations);
            valorSimplesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            activityCreateDispenseBinding.spnDuration.setAdapter(valorSimplesArrayAdapter);

                if (getRelatedViewModel().getDispense().getPrescription().getDispenseType().getDescription().contains("DT")) {
                    SimpleValue s = SimpleValue.fastCreate(12, Prescription.DURATION_THREE_MONTHS);
                    activityCreateDispenseBinding.spnDuration.setSelection(valorSimplesArrayAdapter.getPosition(s));
                } else if (getRelatedViewModel().getDispense().getPrescription().getDispenseType().getDescription().contains("DS")) {
                    SimpleValue s = SimpleValue.fastCreate(24, Prescription.DURATION_SIX_MONTHS);
                    activityCreateDispenseBinding.spnDuration.setSelection(valorSimplesArrayAdapter.getPosition(s));
                } else {
                    SimpleValue s = SimpleValue.fastCreate(4, Prescription.DURATION_ONE_MONTH);
                    activityCreateDispenseBinding.spnDuration.setSelection(valorSimplesArrayAdapter.getPosition(s));
                }

        } catch (SQLException e) {
            Utilities.displayAlertDialog(CreateDispenseActivity.this, getString(R.string.error_loading_form_data) + e.getMessage());
            e.printStackTrace();
        }
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public void changeFormSectionVisibility(View view) {
        if (view.equals(activityCreateDispenseBinding.initialData)) {
            if (activityCreateDispenseBinding.initialDataLyt.getVisibility() == View.VISIBLE) {
                switchLayout();
                activityCreateDispenseBinding.ibtnInitialData.animate().setDuration(200).rotation(180);
                Utilities.collapse(activityCreateDispenseBinding.initialDataLyt);
            } else {
                switchLayout();
                activityCreateDispenseBinding.ibtnInitialData.animate().setDuration(200).rotation(0);
                Utilities.expand(activityCreateDispenseBinding.initialDataLyt);
            }
        } else if (view.equals(activityCreateDispenseBinding.txvDrugs)) {
            if (activityCreateDispenseBinding.drugsDataLyt.getVisibility() == View.VISIBLE) {
                switchLayout();
                activityCreateDispenseBinding.ibtnDrugs.animate().setDuration(200).rotation(180);
                Utilities.collapse(activityCreateDispenseBinding.drugsDataLyt);
            } else {
                activityCreateDispenseBinding.ibtnDrugs.animate().setDuration(200).rotation(0);
                Utilities.expand(activityCreateDispenseBinding.drugsDataLyt);
                switchLayout();
            }
        }
    }

    private void switchLayout() {
        getRelatedViewModel().setInitialDataVisible(!getRelatedViewModel().isInitialDataVisible());
        getRelatedViewModel().setDrugDataVisible(!getRelatedViewModel().isDrugDataVisible());
    }

    @Override
    public BaseViewModel initViewModel() {
        return new ViewModelProvider(this).get(DispenseVM.class);
    }

    @Override
    public DispenseVM getRelatedViewModel() {
        return (DispenseVM) super.getRelatedViewModel();
    }

    private void displaySelectedDrugs() {

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rcvSelectedDrugs.setLayoutManager(mLayoutManager);
        rcvSelectedDrugs.setItemAnimator(new DefaultItemAnimator());
        rcvSelectedDrugs.addItemDecoration(new DividerItemDecoration(getApplicationContext(), 0));

        listbleRecycleViewAdapter = new ListbleRecycleViewAdapter(rcvSelectedDrugs, this.selectedDrugs, this);
        rcvSelectedDrugs.setAdapter(listbleRecycleViewAdapter);
    }

    public void loadFormData() {
        if (activityCreateDispenseBinding.dispenseDate.getText().length() != 0) {
            getRelatedViewModel().getDispense().setPickupDate(DateUtilities.createDate(activityCreateDispenseBinding.dispenseDate.getText().toString(), DateUtilities.DATE_FORMAT));
        }
        if (activityCreateDispenseBinding.nextPickupDate.getText().length() != 0) {
            getRelatedViewModel().getDispense().setNextPickupDate(DateUtilities.createDate(activityCreateDispenseBinding.nextPickupDate.getText().toString(), DateUtilities.DATE_FORMAT));
        }
        getRelatedViewModel().getDispense().setSupply(((SimpleValue) activityCreateDispenseBinding.spnDuration.getSelectedItem()).getId());
        getRelatedViewModel().getDispense().setUuid(Utilities.getNewUUID().toString());
        getRelatedViewModel().getDispense().setSyncStatus(BaseModel.SYNC_SATUS_READY);

        List<DispensedDrug> dispensedDrugs = new ArrayList<>();

        for (Listble drug : this.selectedDrugs) {

            dispensedDrugs.add(initNewDispensedDrug((Drug) drug));
        }

        getRelatedViewModel().getDispense().setDispensedDrugs(dispensedDrugs);

    }

    private DispensedDrug initNewDispensedDrug(Drug drug) {

        final int quantitySupplied = this.getQuantidadeADispensar(drug);

        List<Stock> stocks = getRelatedViewModel().getAllStocksByClinicAndDrug(getCurrentClinic(), drug);

        DispensedDrug dispensedDrug = new DispensedDrug();
        dispensedDrug.setDrug(drug);

        for (Stock stock :
                stocks) {

            int stockQuantityMoviment = stock.getStockMoviment();

            if (quantitySupplied <= stockQuantityMoviment) {
                dispensedDrug.setQuantitySupplied(quantitySupplied);
                dispensedDrug.setStock(stock);
                dispensedDrug.setSyncStatus(BaseModel.SYNC_SATUS_READY);
                break;
            }
        }
        return dispensedDrug;
    }

    @Override
    public void doOnConfirmed() {
        if(comingFromPrescription){
            Map<String, Object> params = new HashMap<>();
            params.put("patient", getRelatedViewModel().getDispense().getPrescription().getPatient());
            params.put("user", getCurrentUser());
            params.put("clinic", getCurrentClinic());
            params.put("requestedFragment", DispenseFragment.FRAGMENT_CODE_DISPENSE);
            nextActivity(PatientPanelActivity.class, params);
        }
        else {
            finish();
        }
    }

    @Override
    public void doOnDeny() {

    }

    private boolean existsStockToDispense(Drug drug) {
        if (getCurrentClinic().isSanitaryUnit() && !getClinicSector().mustValidateStock()) return true;

        final int qtdADispensar = this.getQuantidadeADispensar(drug);
        int stockQuantityMoviment = 0;

        List<Stock> stocks = getRelatedViewModel().getAllStocksByClinicAndDrug(getCurrentClinic(), drug);
        for (Stock stock : stocks) {
            stockQuantityMoviment += stock.getStockMoviment();
        }

        if (qtdADispensar > stockQuantityMoviment) {
            return false;
        }
        return true;
    }

    private boolean verifyIfDrugHasAtLeastOneStock(Drug drug) {

        return getRelatedViewModel().getAllStocksByClinicAndDrug(getCurrentClinic(), drug).size() > 0;

    }

    private int getQuantidadeADispensar(Drug drug) {

        int supply = ((SimpleValue) activityCreateDispenseBinding.spnDuration.getSelectedItem()).getId();
        int qtdAdispensar = 0;
        int supplyPerDugPackSize = (int) drug.getPackSize() / 7;

        if (drug.getPackSize() == 60)
            supplyPerDugPackSize = supplyPerDugPackSize / 2;

        if (supplyPerDugPackSize >= supply) {
            qtdAdispensar = 1;
        } else {
            if (supplyPerDugPackSize * 2 == supply) {
                qtdAdispensar = 2;
            } else if (supplyPerDugPackSize * 2 < supply)
                qtdAdispensar = (supply / 4);
        }

        return qtdAdispensar;
    }

    public String validateDispenseDurationByPrescription() {

        String prescriptionExpireDate = DateUtilities.formatToDDMMYYYY(this.prescription.getExpiryDate());

        if (prescriptionExpireDate != null) {
            return "A última prescrição " +
                    "do paciente não pode ser dispensada, porque já atingiu o limite de levantamentos. VALIDADE DA PRESCRIÇÃO: " + prescriptionExpireDate;
        } else {

            int prescriptionSupply = this.prescription.getSupply();

            try {

                List<Dispense> prescriptionDispenses = getRelatedViewModel().getAllDispensesByPrescription(this.prescription);

                int totalOfDispenseSupplies = 0;
                int remainingSupplyWeeks;
                int currentDispenseSupply = getRelatedViewModel().getDispense().getSupply();

                for (Dispense dispense : prescriptionDispenses
                ) {

                    if(dispense.getId()!=getRelatedViewModel().getDispense().getId()) {
                        totalOfDispenseSupplies = totalOfDispenseSupplies + dispense.getSupply();
                    }
                    }

                remainingSupplyWeeks = prescriptionSupply - totalOfDispenseSupplies;
                totalOfDispenseSupplies = totalOfDispenseSupplies + currentDispenseSupply;
                if (currentDispenseSupply > remainingSupplyWeeks)
                    return "Não pode dispensar medicamentos por um periodo maior que a validade restante da prescrição.\n VALIDADE RESTANTE DA PRESCRIÇÃO: " + Utilities.parseSupplyToLabel(remainingSupplyWeeks);
                if (currentDispenseSupply > prescriptionSupply)
                    return "Não pode dispensar medicamentos por um periodo maior que a validade da prescrição. \n VALIDADE DA PRESCRIÇÃO: " + Utilities.parseSupplyToLabel(prescriptionSupply);
                else if (totalOfDispenseSupplies > prescriptionSupply)
                    return "A dispensa só pode ser efectuada para " + Utilities.parseSupplyToLabel(remainingSupplyWeeks);

                if (totalOfDispenseSupplies == prescriptionSupply)
                    getRelatedViewModel().getDispense().getPrescription().setExpiryDate(getRelatedViewModel().getDispense().getPickupDate());

            } catch (SQLException ex) {

            }
        }
        return "";
    }

    public String validateStockForSelectedDrugs() {

        StringBuilder emptyStockDrugs = new StringBuilder();

        if (getRelatedViewModel().getDispense().getPrescription().getPatient().isFaltosoOrAbandono() && getCurrentClinic().isSanitaryUnit() && !getClinicSector().mustValidateStock()) return emptyStockDrugs.toString();

        for (Listble lt : this.selectedDrugs) {
            Drug drug = (Drug) lt;
            if (!existsStockToDispense(drug)) {
                emptyStockDrugs.append(drug.getDescription() + "\n");
            }
        }
        if (emptyStockDrugs.toString().trim().length() != 0)
            return "Os medicamentos abaixo listados não possuem stock suficiente para a quantidade a dispensar:\n\n" + emptyStockDrugs;
        else
            return emptyStockDrugs.toString();
    }

    private List<PrescribedDrug> getPrescribedDrugsByPrescription() {

        List<PrescribedDrug> prescribedDrugs = new ArrayList<>();
        try {
            prescribedDrugs = getRelatedViewModel().getAllPrescribedDrugsByPrescription(this.prescription);
        } catch (SQLException ignored) {
        }
        return prescribedDrugs;
    }

    private void changeAllSpinnersStatus(boolean status) {
        activityCreateDispenseBinding.spnDuration.setEnabled(status);
        activityCreateDispenseBinding.autCmpDrugs.setEnabled(status);
    }

    public void disableAllSpinners() {
        changeAllSpinnersStatus(false);
    }

    public Dispense getDispenseSelectedForEdit() {
        return dispenseSelectedForEdit;
    }

    public void setDispenseSelectedForEdit(Dispense dispenseSelectedForEdit) {
        this.dispenseSelectedForEdit = new Dispense();
        this.dispenseSelectedForEdit.setNextPickupDate(dispenseSelectedForEdit.getNextPickupDate());
        this.dispenseSelectedForEdit.setSupply(dispenseSelectedForEdit.getSupply());
        this.dispenseSelectedForEdit.setPickupDate(dispenseSelectedForEdit.getPickupDate());
        this.dispenseSelectedForEdit.setId(dispenseSelectedForEdit.getId());
        this.dispenseSelectedForEdit.setSyncStatus(dispenseSelectedForEdit.getSyncStatus());
        this.dispenseSelectedForEdit.setDispensedDrugs(dispenseSelectedForEdit.getDispensedDrugs());
        this.dispenseSelectedForEdit.setUuid(dispenseSelectedForEdit.getUuid());
        this.dispenseSelectedForEdit.setPrescription(dispenseSelectedForEdit.getPrescription());
    }
}