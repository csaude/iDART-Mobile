package mz.org.fgh.idartlite.adapter.recyclerview.report;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.adapter.recyclerview.generic.AbstractRecycleViewAdapter;
import mz.org.fgh.idartlite.databinding.ContentDispensesNonSyncReportBinding;
import mz.org.fgh.idartlite.databinding.ContentDispensesReportBinding;
import mz.org.fgh.idartlite.databinding.ItemLoadingBinding;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.util.Utilities;

public class DispenseNonSyncReportAdapter extends AbstractRecycleViewAdapter<Dispense> {


    public DispenseNonSyncReportAdapter(RecyclerView recyclerView, List<Dispense> dispenseList, Activity activity) {
        super(recyclerView, dispenseList, activity);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemLoadingBinding itemLoadingBinding;

        if (viewType == VIEW_TYPE_ITEM) {
            ContentDispensesNonSyncReportBinding  contentDispensesNonSyncReportBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.content_dispenses_non_sync_report, parent, false);
            return new DispenseViewHolder(contentDispensesNonSyncReportBinding);
        }
        else if (viewType == VIEW_TYPE_LOADING) {
            itemLoadingBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_loading, parent, false);
            return new LoadingViewHolder(itemLoadingBinding);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof DispenseViewHolder){
            Dispense dispense = (Dispense) records.get(position);
            ((DispenseViewHolder) viewHolder).contentDispensesReportBinding.setDispense(dispense);
        }else
        if (viewHolder instanceof LoadingViewHolder){
            showLoadingView((LoadingViewHolder) viewHolder, position);
        }
        ((DispenseViewHolder) viewHolder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

             //   toggleLayout(true,v,((DispenseViewHolder) viewHolder).contentDispensesReportBinding.linearDetails);

                if(((DispenseViewHolder) viewHolder).contentDispensesReportBinding.linearDetails.getVisibility()==View.VISIBLE){
                    ((DispenseViewHolder) viewHolder).contentDispensesReportBinding.linearDetails.setVisibility(View.GONE);
                }
                else  if(((DispenseViewHolder) viewHolder).contentDispensesReportBinding.linearDetails.getVisibility()==View.GONE){
                    ((DispenseViewHolder) viewHolder).contentDispensesReportBinding.linearDetails.setVisibility(View.VISIBLE);
                }


               /* boolean show = toggleLayout(!personList.get(i).isExpanded(), v, holder.bi.layoutExpand);
                personList.get(i).setExpanded(show);*/
            }
        });


    }

    /*private boolean toggleLayout(boolean isExpanded, View v, LinearLayout layoutExpand) {
        Utilities.toggleArrow(v, isExpanded);
        return isExpanded;

    }*/


    private boolean toggleLayout(boolean isExpanded, View v, LinearLayout layoutExpand) {
       // Utilities.toggleArrow(v, isExpanded);
        if (isExpanded) {
            Utilities.expand(layoutExpand);
        } else {
            Utilities.collapse(layoutExpand);
        }
        return isExpanded;

    }

    public class DispenseViewHolder extends RecyclerView.ViewHolder{

        private ContentDispensesNonSyncReportBinding contentDispensesReportBinding;

        public DispenseViewHolder(@NonNull ContentDispensesNonSyncReportBinding contentDispensesReportBinding) {
            super(contentDispensesReportBinding.getRoot());
            this.contentDispensesReportBinding = contentDispensesReportBinding;
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        ItemLoadingBinding itemLoadingBinding;

        public LoadingViewHolder(@NonNull ItemLoadingBinding itemLoadingBinding) {
            super(itemLoadingBinding.getRoot());
            this.itemLoadingBinding = itemLoadingBinding;
        }
    }

    protected void showLoadingView(DispenseNonSyncReportAdapter.LoadingViewHolder viewHolder, int position) {}
}
