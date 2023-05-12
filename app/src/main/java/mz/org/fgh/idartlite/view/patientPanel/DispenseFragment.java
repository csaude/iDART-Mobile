package mz.org.fgh.idartlite.view.patientPanel;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.adapter.recyclerview.dispense.DispenseAdapter;
import mz.org.fgh.idartlite.base.fragment.GenericFragment;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.viewModel.BaseViewModel;
import mz.org.fgh.idartlite.common.ApplicationStep;
import mz.org.fgh.idartlite.databinding.FragmentDispenseBinding;
import mz.org.fgh.idartlite.listener.dialog.IDialogListener;
import mz.org.fgh.idartlite.listener.dialog.IDiseaseTypeDialogListener;
import mz.org.fgh.idartlite.listener.dialog.IListbleDialogListener;
import mz.org.fgh.idartlite.listener.recyclerView.ClickListener;
import mz.org.fgh.idartlite.model.ClinicSector;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.dispense.CreateDispenseActivity;
import mz.org.fgh.idartlite.view.dispense.ReturnDispenseActivity;
import mz.org.fgh.idartlite.viewmodel.dispense.DispenseVM;
import mz.org.fgh.idartlite.viewmodel.prescription.PrescriptionVM;

public class DispenseFragment extends GenericFragment implements IListbleDialogListener, IDiseaseTypeDialogListener, IDialogListener {

    public static final String FRAGMENT_CODE_DISPENSE = "DispenseFragment";
    int dispensePosition;
    private RecyclerView rcvDispences;
    private List<Dispense> dispenseList;

    private FragmentDispenseBinding fragmentDispenseBinding;
    private DispenseAdapter dispenseAdapter;
    private List<Prescription> prescriptionList;
    private static String selectedOption;

    private Prescription lastPrescriptionByDiseaseType;


    public DispenseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentDispenseBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_dispense, container, false);
        return fragmentDispenseBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.rcvDispences = fragmentDispenseBinding.rcvDispenses;

        this.dispenseList = new ArrayList<>();
        Map<Integer,String > map1 = new HashMap<>();
        map1.put(1, "TARV");
        map1.put(2, "TPT");
        map1.put(3, "Prep");

        try {
            this.dispenseList = getRelatedViewModel().gatAllOfPatient(getSelectedPatient());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (Utilities.listHasElements(this.dispenseList)) {
            this.dispenseAdapter = new DispenseAdapter(rcvDispences, this.dispenseList, getMyActivity());
            displayDataOnRecyclerView(rcvDispences, dispenseAdapter, getContext());
        }


        fragmentDispenseBinding.newDispense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (getRelatedViewModel().getCurrentClinicSector() != null || getRelatedViewModel().getCurrentClinic().getPharmacyType().getDescription() == "Unidade Sanitária" )   {
                    Utilities.displayShowDiseaseTypeDialog(DispenseFragment.this.getContext(),  map1.values().toArray(new String[map1.values().size()]), getActivity().getString(R.string.yes), getActivity().getString(R.string.no), DispenseFragment.this, DispenseFragment.this, true).show();
                } else {
                    selectedOption = "TARV";
                   genericNewDispense();

                }
                }
        });

        rcvDispences.addOnItemTouchListener(
                new ClickListener(
                        getContext(), rcvDispences, new ClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        displayPopupMenu(view, position);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        displayPopupMenu(view, position);
                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
                ));

    }



    private void displayPopupMenu(View view, int position) {
        getRelatedViewModel().setDispense(dispenseList.get(position));
        getRelatedViewModel().getDispense().setListPosition(position);

        PopupMenu popup = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(DispenseFragment.this::onMenuItemClick);
        inflater.inflate(R.menu.edit_remove_menu, popup.getMenu());
        popup.getMenu().getItem(0).setVisible(true);
        popup.show();
    }

    public boolean onMenuItemClick(MenuItem item) {

        String editErrors = getRelatedViewModel().patientHasEndingEpisode();

        switch (item.getItemId()) {
            case R.id.edit:
                try {
                    if (Utilities.stringHasValue(editErrors)) {
                        Utilities.displayAlertDialog(DispenseFragment.this.getContext(), editErrors).show();
                    } else {

                        editErrors = getRelatedViewModel().dispenseCanBeEdited();
                        if (Utilities.stringHasValue(editErrors)) {
                            Utilities.displayAlertDialog(DispenseFragment.this.getContext(), editErrors).show();
                        } else {
                            Map<String, Object> params = new HashMap<>();
                            params.put("patient", getMyActivity().getPatient());
                            params.put("user", getCurrentUser());
                            params.put("clinic", getMyActivity().getCurrentClinic());
                            params.put("dispense", getRelatedViewModel().getDispense());
                            params.put("step", ApplicationStep.STEP_EDIT);
                            params.put("comingFromPrescription", false);
                            params.put("clinicSector", getClinicSector());
                            nextActivity(CreateDispenseActivity.class, params);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.remove:
                //Utilities.displayDeleteConfirmationDialogFromList(DispenseFragment.this.getContext(), DispenseFragment.this.getString(R.string.list_item_delete_msg), dispensePosition, DispenseFragment.this).show();
            //    this.removeDispenseConfirmation();
              //  return true;
                if (Utilities.stringHasValue(editErrors)) {
                    Utilities.displayAlertDialog(DispenseFragment.this.getContext(), editErrors).show();
                }
                else {
                   String errorMsg1 = getRelatedViewModel().checkDispenseRemoveConditions();
                    if (Utilities.stringHasValue(errorMsg1)) {
                        Utilities.displayAlertDialog(DispenseFragment.this.getContext(), errorMsg1).show();
                    }
                    else {

                        Map<String, Object> newParams = new HashMap<>();
                        newParams.put("patient", getMyActivity().getPatient());
                        newParams.put("user", getCurrentUser());
                        newParams.put("clinic", getMyActivity().getCurrentClinic());
                        newParams.put("dispense", getRelatedViewModel().getDispense());
                        newParams.put("step", ApplicationStep.STEP_REMOVE);
                        newParams.put("positionRemoved", dispensePosition);
                        newParams.put("clinicSector", getClinicSector());
                        nextActivity(ReturnDispenseActivity.class, newParams);

                    }
                }
                return true;
            case R.id.viewDetails:
                Map<String, Object> params = new HashMap<>();
                params.put("patient", getMyActivity().getPatient());
                params.put("user", getCurrentUser());
                params.put("clinic", getMyActivity().getCurrentClinic());
                params.put("dispense", getRelatedViewModel().getDispense());
                params.put("step", ApplicationStep.STEP_DISPLAY);
                params.put("comingFromPrescription", false);
                params.put("clinicSector", getClinicSector());
                nextActivity(CreateDispenseActivity.class, params);
                return true;

            case R.id.refazerFrascos:

                if (Utilities.stringHasValue(editErrors)) {
                    Utilities.displayAlertDialog(DispenseFragment.this.getContext(), editErrors).show();
                }
                else {
                    String returnedErrors = getRelatedViewModel().dispenseCanBeReturned();
                    if (Utilities.stringHasValue(returnedErrors)) {
                        Utilities.displayAlertDialog(DispenseFragment.this.getContext(), returnedErrors).show();
                    } else {
                        Map<String, Object> params1 = new HashMap<>();
                        params1.put("patient", getMyActivity().getPatient());
                        params1.put("user", getCurrentUser());
                        params1.put("clinic", getMyActivity().getCurrentClinic());
                        params1.put("dispense", getRelatedViewModel().getDispense());
                        params1.put("clinicSector", getClinicSector());
                        nextActivity(ReturnDispenseActivity.class, params1);

                    }
                }
                return true;
            default:
                return false;
        }

    }

    @Override
    public void remove(int position) {

        String errorMsg = getRelatedViewModel().patientHasEndingEpisode();

        if (!Utilities.stringHasValue(errorMsg)) {

            errorMsg = getRelatedViewModel().checkDispenseRemoveConditions();

            if (!Utilities.stringHasValue(errorMsg)) {

                try {
                    dispenseList.remove(getRelatedViewModel().getDispense());

                    rcvDispences.getAdapter().notifyItemRemoved(position);
                    rcvDispences.removeViewAt(position);
                    rcvDispences.getAdapter().notifyItemRangeChanged(position, rcvDispences.getAdapter().getItemCount());

                    getRelatedViewModel().deleteDispense(getRelatedViewModel().getDispense());
                } catch (SQLException e) {
                    Utilities.displayAlertDialog(DispenseFragment.this.getContext(), getString(R.string.record_sucessfuly_removed)).show();
                }

            } else {
                Utilities.displayAlertDialog(DispenseFragment.this.getContext(), errorMsg).show();
            }
        } else {
            Utilities.displayAlertDialog(DispenseFragment.this.getContext(), errorMsg).show();
        }
    }

    @Override
    public void remove(BaseModel baseModel) {

    }


    public PatientPanelActivity getMyActivity() {
        return (PatientPanelActivity) getActivity();
    }

    @Override
    public BaseViewModel initViewModel() {
        return new ViewModelProvider(this).get(DispenseVM.class);
    }

    @Override
    public DispenseVM getRelatedViewModel() {
        return (DispenseVM) super.getRelatedViewModel();
    }

    private Patient getSelectedPatient() {
        return getMyActivity().getPatient();
    }

    public ClinicSector getClinicSector() {
        return getMyActivity().getClinicSector();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.rcvDispences = fragmentDispenseBinding.rcvDispenses;

        try {
            this.dispenseList = getRelatedViewModel().gatAllOfPatient(getSelectedPatient());


        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (Utilities.listHasElements(dispenseList)) {
            dispenseAdapter = new DispenseAdapter(rcvDispences, this.dispenseList, getMyActivity());
            displayDataOnRecyclerView(rcvDispences, dispenseAdapter, getContext());
        }
        else
           if(getMyActivity().getPositionRemoved() !=null) {
               dispenseAdapter = new DispenseAdapter(rcvDispences, this.dispenseList, getMyActivity());
                dispenseAdapter.notifyItemRangeRemoved(0,dispenseList.size());

           }
    }

    public List<Dispense> getDispenseList() {
        return dispenseList;
    }

    public void setDispenseList(List<Dispense> dispenseList) {
        this.dispenseList = dispenseList;
    }

    private void genericNewDispense() {
        Patient patient = getMyActivity().getPatient();
        List<Dispense> dispensesList = getDispenseList();
        if (!getRelatedViewModel().patientHasEpisodioFim(patient)) {
            Intent intent = new Intent(getContext(), CreateDispenseActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user", getCurrentUser());
            bundle.putSerializable("clinic", getMyActivity().getCurrentClinic());
            bundle.putSerializable("patient", patient);
            bundle.putSerializable("dispenses", (Serializable) dispensesList);
            bundle.putSerializable("prescription", lastPrescriptionByDiseaseType);
            bundle.putSerializable("step", ApplicationStep.STEP_CREATE);
            bundle.putSerializable("comingFromPrescription", false);
            bundle.putSerializable("clinicSector", getClinicSector());
            intent.putExtras(bundle);
            startActivity(intent);
        } else {
            Utilities.displayAlertDialog(DispenseFragment.this.getContext(), getActivity().getString(R.string.patient_has_final_episode_cant_create_dispense)).show();
        }
    }

    @Override
    public void doOnConfirmedDiseaseType(String[] diseaseTypes, int selectedDiseaseType)  {

            selectedOption = diseaseTypes[selectedDiseaseType];
        try {
            lastPrescriptionByDiseaseType =  this.getRelatedViewModel().getLastPatientPrescriptionByDiseaseType( getMyActivity().getPatient(),getRelatedViewModel().getDiseaseTypeByCode(selectedOption));
            if (lastPrescriptionByDiseaseType == null) {
                Utilities.displayAlertDialog(DispenseFragment.this.getContext(), getActivity().getString(R.string.patient_doenst_have_prescription_disease_type)).show();
            } else {
                genericNewDispense();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void doOnConfirmed() {

    }

    @Override
    public void doOnDeny() {

    }

   /* public void removeDispenseConfirmation() {

        StringBuilder dispensedDrugsList = new StringBuilder();

        String dataLevantamento = "Data Levantamento: " + DateUtilities.formatToDDMMYYYY(getRelatedViewModel().getDispense().getPickupDate(), "/");
        String duracao = "Duração: " + Utilities.parseSupplyToLabel(getRelatedViewModel().getDispense().getSupply());
        String dataProximoLevantamento = "Data Próximo Levantamento: " + DateUtilities.formatToDDMMYYYY(getRelatedViewModel().getDispense().getNextPickupDate(), "/");

        List<DispensedDrug> dispensedDrugs = new ArrayList<>();
        try {
            dispensedDrugs = getRelatedViewModel().findDispensedDrugsByDispenseId(getRelatedViewModel().getDispense().getId());
        } catch (SQLException ex) {
        }

        for (DispensedDrug dd : dispensedDrugs
        ) {
            Drug drug = dd.getStock().getDrug();
            dispensedDrugsList.append(drug.getDescription() + "\n");
        }

        String detalhesAviamento = DispenseFragment.this.getString(R.string.remove_dispense_drug_detail_list);
        String listaDeMedicamentosDispensados = DispenseFragment.this.getString(R.string.dispensed_drug_list);
        String gostariaDeRemoverAdispensaAnterior = DispenseFragment.this.getString(R.string.would_you_like_remove_dispense);

        String removeDispenseConfirmationMessage = detalhesAviamento + "\n\n" + dataLevantamento +
                "\n" + duracao + "\n" + dataProximoLevantamento + "\n\n" + listaDeMedicamentosDispensados + "\n"
                + dispensedDrugsList + "\n" + gostariaDeRemoverAdispensaAnterior;

        Utilities.displayDeleteConfirmationDialogFromList(DispenseFragment.this.getContext(), removeDispenseConfirmationMessage, dispensePosition, DispenseFragment.this).show();
    }*/

}