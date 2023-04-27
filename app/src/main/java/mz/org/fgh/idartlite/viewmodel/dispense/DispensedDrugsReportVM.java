package mz.org.fgh.idartlite.viewmodel.dispense;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mz.org.fgh.idartlite.BR;
import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.base.viewModel.BaseViewModel;
import mz.org.fgh.idartlite.listener.rest.RestResponseListener;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.rest.service.Dispense.RestDispenseService;
import mz.org.fgh.idartlite.service.dispense.DispenseService;
import mz.org.fgh.idartlite.service.dispense.IDispenseService;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.reports.DispensedDrugsReportActivity;

public class DispensedDrugsReportVM extends BaseViewModel implements RestResponseListener<Dispense> {
    private IDispenseService dispenseService;
    private boolean onlineSearch;

    private double totalAviamentos;

    private Map<String, Double> searchResults;

    public DispensedDrugsReportVM(@NonNull Application application) {
        super(application);
        dispenseService = new DispenseService(application);
    }

    @Override
    protected IBaseService initRelatedService() {
        return null;
    }

    @Override
    protected BaseModel initRecord() {
        return null;
    }

    @Override
    protected void initFormData() {

    }

    @Override
    public void preInit() {

    }

    public void doSearch(Date start, Date end) throws SQLException {
        getLoadingDialog().startLoadingDialog();
        if (!onlineSearch) {
            List<Dispense> dispenseLis = dispenseService.getDispensesBetweenStartDateAndEndDate(start, end);
            doAfterSearch(dispenseLis);
        }else {

            doOnlineSearch(start, end);
        }
    }

    private void doOnlineSearch(Date start, Date end) {
        boolean isUsDispenses = false;
        try {
            RestDispenseService.restGetAllDispenseByPeriod(start, end, getCurrentClinic().getUuid() ,0,0, this, isUsDispenses);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void doAfterSearch(List<Dispense> dispenseLis) {
        Map<String, Integer> map = new HashMap<>();

        Map<String, Double> percentageMap = new HashMap<>();
        if (Utilities.listHasElements(dispenseLis)){
            totalAviamentos = dispenseLis.size();

            for (Dispense dispense : dispenseLis){
                if (map.containsKey(dispense.getPrescription().getTherapeuticRegimen().getDescription())){
                    int qty = map.get(dispense.getPrescription().getTherapeuticRegimen().getDescription());
                    map.remove(dispense.getPrescription().getTherapeuticRegimen().getDescription());
                    map.put(dispense.getPrescription().getTherapeuticRegimen().getDescription(), qty+1);
                }else {
                    map.put(dispense.getPrescription().getTherapeuticRegimen().getDescription(), 1);
                }
            }

            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                percentageMap.put(entry.getKey()+" ["+entry.getValue()+"]-["+((100*entry.getValue())/totalAviamentos)+"%]", ((100*entry.getValue())/totalAviamentos));
            }
        }
        this.searchResults = percentageMap;

        getLoadingDialog().dismisDialog();

        getRelatedActivity().generateGraph();
    }

    @Override
    public DispensedDrugsReportActivity getRelatedActivity() {
        return (DispensedDrugsReportActivity) super.getRelatedActivity();
    }

    @Override
    public void doOnResponse(String flag, List<Dispense> objects) {
        doAfterSearch(objects);
    }

    public boolean isOnlineSearch() {
        return onlineSearch;
    }

    public void setOnlineSearch(boolean onlineSearch) {
        this.onlineSearch = onlineSearch;
    }

    public void changeReportSearchMode(String searchType) {
        if (searchType.equals(getApplication().getString(R.string.local))) {
            this.setOnlineSearch(false);
        }
        else {
            setOnlineSearch(true);
        }
    }

    public double getTotalAviamentos(){
        return this.totalAviamentos;
    }

    @Override
    public void doOnRestSucessResponse(String flag) {

    }

    @Override
    public void doOnRestErrorResponse(String errormsg) {

    }

    @Override
    public void doOnRestSucessResponseObjects(String flag, List<Dispense> objects) {

    }


    public Map<String, Double> getSearchResults() {
        return this.searchResults;
    }
}
