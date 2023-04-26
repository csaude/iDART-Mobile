package mz.org.fgh.idartlite.view.patientPanel;

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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.adapter.recyclerview.prescription.PrescriptionAdapter;
import mz.org.fgh.idartlite.base.fragment.GenericFragment;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.viewModel.BaseViewModel;
import mz.org.fgh.idartlite.common.ApplicationStep;
import mz.org.fgh.idartlite.databinding.PrescriptionFragmentBinding;
import mz.org.fgh.idartlite.listener.dialog.IListbleDialogListener;
import mz.org.fgh.idartlite.listener.recyclerView.ClickListener;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.prescription.PrescriptionActivity;
import mz.org.fgh.idartlite.viewmodel.prescription.PrescriptionVM;

public class PrescriptionFragment extends GenericFragment implements IListbleDialogListener {

    public static final String FRAGMENT_CODE_PRESCRIPTION = "PrescriptionFragment";

    private RecyclerView rcvPrescriptions;
    private List<Prescription> prescriptionList;

    private PrescriptionFragmentBinding prescriptionFragmentBinding;
    private PrescriptionAdapter prescriptionAdapter;

    public PrescriptionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        prescriptionFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.prescription_fragment, container,false);
        return prescriptionFragmentBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.rcvPrescriptions = prescriptionFragmentBinding.rcvPrescriptions;


        prescriptionFragmentBinding.setViewModel(getRelatedViewModel());

        try {
            this.prescriptionList = getRelatedViewModel().gatAllOfPatient(getSelectedPatient());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (Utilities.listHasElements(prescriptionList)) {
            prescriptionAdapter = new PrescriptionAdapter(rcvPrescriptions, this.prescriptionList, getMyActivity());
            displayDataOnRecyclerView(rcvPrescriptions, prescriptionAdapter, getContext());
        }


        rcvPrescriptions.addOnItemTouchListener(
                new ClickListener(getContext(), rcvPrescriptions, new ClickListener.OnItemClickListener() {
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

    public void startPrescriptionActivity(){
        if (getMyActivity().getPatient().hasEndEpisode()) {
            Utilities.displayAlertDialog(PrescriptionFragment.this.getContext(), getString(R.string.cant_edit_patient_data)).show();
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("patient", getMyActivity().getPatient());
            params.put("user", getCurrentUser());
            params.put("clinic", getMyActivity().getCurrentClinic());
            params.put("step", ApplicationStep.STEP_CREATE);
            params.put("clinicSector", getMyActivity().getClinicSector());
            // params.put("diseaseType" , selec)
            nextActivityFinishingCurrent(PrescriptionActivity.class, params);
        }
    }

    private void displayPopupMenu(View view, int position) {
        getRelatedViewModel().setPrescription(prescriptionList.get(position));
        getRelatedViewModel().getPrescription().setListPosition(position);

        PopupMenu popup = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(PrescriptionFragment.this::onMenuItemClick);
        inflater.inflate(R.menu.edit_remove_menu, popup.getMenu());
        popup.show();
    }

    public boolean onMenuItemClick(MenuItem item){
        switch (item.getItemId()){
            case R.id.edit:
                if (getRelatedViewModel().getPrescription().getPatient().hasEndEpisode()){
                    Utilities.displayAlertDialog(PrescriptionFragment.this.getContext(),getString(R.string.cant_edit_patient_data)).show();
                }else {
                    try {
                        String editErrors = getRelatedViewModel().prescriptionCanBeEdited();

                        if (Utilities.stringHasValue(editErrors)) {
                            Utilities.displayAlertDialog(PrescriptionFragment.this.getContext(), editErrors).show();
                        } else {
                            Map<String, Object> params = new HashMap<>();
                            params.put("prescription", getRelatedViewModel().getPrescription());
                            params.put("user", getRelatedViewModel().getCurrentUser());
                            params.put("clinic", getMyActivity().getCurrentClinic());
                            params.put("step", ApplicationStep.STEP_EDIT);
                            params.put("clinicSector", getMyActivity().getClinicSector());
                            nextActivity(PrescriptionActivity.class, params);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            case R.id.remove:
                if (getRelatedViewModel().getPrescription().getPatient().hasEndEpisode()){
                    Utilities.displayAlertDialog(PrescriptionFragment.this.getContext(),getString(R.string.cant_edit_patient_data)).show();
                }else {
                    Utilities.displayDeleteConfirmationDialogFromList(PrescriptionFragment.this.getContext(), getString(R.string.list_item_delete_msg), getRelatedViewModel().getPrescription().getListPosition(), PrescriptionFragment.this).show();
                }
                return true;

            case R.id.viewDetails:
                Map<String, Object> params = new HashMap<>();
                params.put("prescription", getRelatedViewModel().getPrescription());
                params.put("user", getRelatedViewModel().getCurrentUser());
                params.put("clinic", getMyActivity().getCurrentClinic());
                params.put("step", ApplicationStep.STEP_DISPLAY);
                params.put("clinicSector", getMyActivity().getClinicSector());
                nextActivity(PrescriptionActivity.class, params);
            default:
                return false;
        }

    }

    @Override
    public void remove(int position) {

        String errorMsg = getRelatedViewModel().checkPrescriptionRemoveConditions();

        if(!Utilities.stringHasValue(errorMsg)){

            try {
                getRelatedViewModel().deletePrescription(prescriptionList.get(position));

                prescriptionList.remove(getRelatedViewModel().getPrescription());

                rcvPrescriptions.getAdapter().notifyItemRemoved(position);
                rcvPrescriptions.removeViewAt(position);
                rcvPrescriptions.getAdapter().notifyItemRangeChanged(position, rcvPrescriptions.getAdapter().getItemCount());

                Utilities.displayAlertDialog(PrescriptionFragment.this.getContext(), getString(R.string.record_sucessfuly_removed)).show();

            } catch (SQLException e) {
                Utilities.displayAlertDialog(PrescriptionFragment.this.getContext(), getString(R.string.error_removing_record)+ " "+e.getLocalizedMessage()).show();
            }
        }
        else {
            Utilities.displayAlertDialog(PrescriptionFragment.this.getContext(),errorMsg).show();
        }

    }

    @Override
    public void remove(BaseModel baseModel) {

    }

    public PatientPanelActivity getMyActivity(){
        return (PatientPanelActivity) getActivity();
    }

    @Override
    public BaseViewModel initViewModel() {
        return new ViewModelProvider(this).get(PrescriptionVM.class);
    }

    @Override
    public PrescriptionVM getRelatedViewModel() {
        return (PrescriptionVM) super.getRelatedViewModel();
    }

    private Patient getSelectedPatient(){
        return getMyActivity().getPatient();
    }

}