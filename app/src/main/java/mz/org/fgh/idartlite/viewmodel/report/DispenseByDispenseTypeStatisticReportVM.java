package mz.org.fgh.idartlite.viewmodel.report;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mz.org.fgh.idartlite.BR;
import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.base.viewModel.SearchVM;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.TherapeuticRegimen;
import mz.org.fgh.idartlite.rest.service.Dispense.RestDispenseService;
import mz.org.fgh.idartlite.searchparams.AbstractSearchParams;
import mz.org.fgh.idartlite.service.dispense.DispenseService;
import mz.org.fgh.idartlite.service.dispense.IDispenseService;
import mz.org.fgh.idartlite.service.drug.ITherapheuticRegimenService;
import mz.org.fgh.idartlite.service.drug.TherapheuticRegimenService;
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.reports.DispenseByDispenseTypeReportActivity;
import mz.org.fgh.idartlite.view.reports.PatientsAwaitingStatisticsActivity;

public class DispenseByDispenseTypeStatisticReportVM extends SearchVM<Dispense> {


    private IDispenseService dispenseService;

    private ITherapheuticRegimenService therapheuticRegimenService;

    private String startDate;

    private String endDate;


    public DispenseByDispenseTypeStatisticReportVM(@NonNull Application application) {
        super(application);
        dispenseService = new DispenseService(application, getCurrentUser());
        therapheuticRegimenService = new TherapheuticRegimenService(application, getCurrentUser());
        setFullLoad(true);
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

    public List getDispensesByDates(Date startDate, Date endDate, long offset, long limit) throws SQLException {
        List<Dispense> dispenseList = dispenseService.getDispensesBetweenStartDateAndEndDateWithLimit(startDate, endDate,offset,limit);
        return doBeforeDisplay(dispenseList);
    }

    @Override
    public String validateBeforeSearch() {
        if (!Utilities.stringHasValue(startDate) || !Utilities.stringHasValue(endDate)) {
            return getRelatedActivity().getString(R.string.start_end_date_is_mandatory);
        } else if (DateUtilities.dateDiff(DateUtilities.createDate(endDate, DateUtilities.DATE_FORMAT), DateUtilities.createDate(startDate, DateUtilities.DATE_FORMAT), DateUtilities.DAY_FORMAT) < 0) {
            return "A data inicio deve ser menor que a data fim.";
        } else if ((int) (DateUtilities.dateDiff(DateUtilities.getCurrentDate(), DateUtilities.createDate(startDate, DateUtilities.DATE_FORMAT), DateUtilities.DAY_FORMAT)) < 0) {
            return "A data inicio deve ser menor ou igual que a data corrente.";
        } else return null;
    }

    @Override
    public void createPdfDocument() {
        try {
            this.getRelatedActivity().createPdfDocument();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doOnNoRecordFound() {
        if (!isOnlineSearch()) {
            if (getAllDisplyedRecords().size() > 0) {
                getRelatedActivity().generatePdfButton(true);
            } else {
                Utilities.displayAlertDialog(getRelatedActivity(), "Não foram encontrados resultados para a sua pesquisa").show();
                getRelatedActivity().generatePdfButton(false);
            }
        } else {
            Utilities.displayAlertDialog(getRelatedActivity(), onlineRequestError).show();
            getRelatedActivity().generatePdfButton(false);
        }
    }


    public List doSearch(long offset, long limit) throws SQLException {
        return getDispensesByDates(DateUtilities.createDate(startDate, DateUtilities.DATE_FORMAT), DateUtilities.createDateWithTime(endDate,DateUtilities.END_DAY_TIME, DateUtilities.DATE_TIME_FORMAT), offset, limit);
    }

    @Override
    public void doOnlineSearch(long offset, long limit) throws SQLException {
        super.doOnlineSearch(offset, limit);
        boolean isUsDispenses = false;
        RestDispenseService.restGetAllDispenseByPeriod(DateUtilities.createDate(startDate, DateUtilities.DATE_FORMAT), DateUtilities.createDateWithTime(endDate,DateUtilities.END_DAY_TIME, DateUtilities.DATE_TIME_FORMAT), getCurrentClinic().getUuid() ,offset,limit, this, isUsDispenses);

    }

    @Override
    public void displaySearchResults() {

        Utilities.hideSoftKeyboard(getRelatedActivity());

        getRelatedActivity().displaySearchResult();
    }

    @Override
    public List<?> doBeforeDisplay(List<Dispense> objects) {
        ArrayList list = new ArrayList();


        List<Dispense> dispenseList = objects;

        List<TherapeuticRegimen> therapeuticRegimenList = null;
        try {
            therapeuticRegimenList = therapheuticRegimenService.getAll();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        for (TherapeuticRegimen regimen : therapeuticRegimenList) {

            int totalDM = 0;
            int totalDT = 0;
            int totalDS = 0;
            int totalOutros = 0;

            for (Dispense dispense : dispenseList) {

                if (dispense.getPrescription().getTherapeuticRegimen().getCode().equals(regimen.getCode())) {

                    if (dispense.getPrescription().getDispenseType() != null) {
                        if (dispense.getPrescription().getDispenseType().getDescription().contains("DM"))
                            totalDM += 1;
                        else if (dispense.getPrescription().getDispenseType().getDescription().contains("DT"))
                            totalDT += 1;
                        else if (dispense.getPrescription().getDispenseType().getDescription().contains("DS"))
                            totalDS += 1;
                    }else{
                        totalOutros += 1;
                    }
                }

            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("regimeTerapeutico", regimen.getDescription());
            map.put("totalGeralDM", totalDM);
            map.put("totalGeralDT", totalDT);
            map.put("totalGeralDS", totalDS);
            map.put("totalGeral", totalDM + totalDT + totalDS + totalOutros);

            if ((totalDM + totalDT + totalDS) > 0)
                list.add(map);
        }
        return list;
    }

    @Override
    public AbstractSearchParams<Dispense> initSearchParams() {
        return null;
    }

    public DispenseByDispenseTypeReportActivity getRelatedActivity() {
        return (DispenseByDispenseTypeReportActivity) super.getRelatedActivity();
    }

    @Override
    public void preInit() {

    }

    @Bindable
    public Clinic getClinic() {
        return getCurrentClinic();
    }

    @Bindable
    public String getSearchParam() {
        return startDate;
    }

    public void setSearchParam(String searchParam) {
        this.startDate = searchParam;
        notifyPropertyChanged(BR.searchParam);
    }

    @Bindable
    public String getSearchParam2() {
        return endDate;
    }

    public void setSearchParam2(String searchParam) {
        this.endDate = searchParam;
        notifyPropertyChanged(BR.searchParam);
    }

}
