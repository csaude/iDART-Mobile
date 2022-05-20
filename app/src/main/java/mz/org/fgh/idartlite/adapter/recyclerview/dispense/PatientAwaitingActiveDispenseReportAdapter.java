package mz.org.fgh.idartlite.adapter.recyclerview.dispense;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.adapter.recyclerview.generic.AbstractRecycleViewAdapter;
import mz.org.fgh.idartlite.databinding.ContentAwaitingAbsentPatientsReportBinding;
import mz.org.fgh.idartlite.databinding.ContentAwaitingActivePatientsReportBinding;
import mz.org.fgh.idartlite.databinding.ItemLoadingBinding;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.patient.Patient;

public class PatientAwaitingActiveDispenseReportAdapter extends AbstractRecycleViewAdapter<Dispense> {


    private Activity activity;
    private List<Dispense> dispenseList;
    private boolean awaitingReport;

    public PatientAwaitingActiveDispenseReportAdapter(RecyclerView recyclerView, List<Dispense> dispenseList, Activity activity, boolean awaitingReport) {
        super(recyclerView, dispenseList, activity);
        this.awaitingReport=awaitingReport;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        ItemLoadingBinding itemLoadingBinding;


        if (viewType == VIEW_TYPE_ITEM) {
            ContentAwaitingActivePatientsReportBinding contentAwaitingPatientsReportBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.content_awaiting_active_patients_report, parent, false);

            if(awaitingReport) {
                contentAwaitingPatientsReportBinding.linearAwaitingDetails.setVisibility(View.VISIBLE);
            }else {
                contentAwaitingPatientsReportBinding.linearAbsentDetails.setVisibility(View.VISIBLE);
            }

            return new PatientAwaitingActiveDispenseReportAdapter.DispenseViewHolder(contentAwaitingPatientsReportBinding);
        }  else if (viewType == VIEW_TYPE_LOADING) {
            itemLoadingBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_loading, parent, false);
            return new PatientAwaitingActiveDispenseReportAdapter.LoadingViewHolder(itemLoadingBinding);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof PatientAwaitingActiveDispenseReportAdapter.DispenseViewHolder){
            Dispense dispense = (Dispense) records.get(position);
            ((PatientAwaitingActiveDispenseReportAdapter.DispenseViewHolder) viewHolder).contentAwaitingPatientsReportBinding.setDispense(dispense);
        }else
        if (viewHolder instanceof PatientAwaitingActiveDispenseReportAdapter.LoadingViewHolder){
            showLoadingView((PatientAwaitingActiveDispenseReportAdapter.LoadingViewHolder) viewHolder, position);
        }
    }


    public class DispenseViewHolder extends RecyclerView.ViewHolder{

        private ContentAwaitingActivePatientsReportBinding contentAwaitingPatientsReportBinding;


        public DispenseViewHolder(@NonNull ContentAwaitingActivePatientsReportBinding contentAwaitingPatientsReportBinding) {
            super(contentAwaitingPatientsReportBinding.getRoot());
            this.contentAwaitingPatientsReportBinding = contentAwaitingPatientsReportBinding;
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {

        ItemLoadingBinding itemLoadingBinding;

        public LoadingViewHolder(@NonNull ItemLoadingBinding itemLoadingBinding) {
            super(itemLoadingBinding.getRoot());
            this.itemLoadingBinding = itemLoadingBinding;
        }
    }

    protected void showLoadingView(PatientAwaitingActiveDispenseReportAdapter.LoadingViewHolder viewHolder, int position) {}
}
