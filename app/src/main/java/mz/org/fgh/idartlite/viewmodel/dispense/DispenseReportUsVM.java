package mz.org.fgh.idartlite.viewmodel.dispense;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;

import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.base.viewModel.SearchVM;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.rest.service.Dispense.RestDispenseService;
import mz.org.fgh.idartlite.searchparams.AbstractSearchParams;
import mz.org.fgh.idartlite.searchparams.DispenseSearchParams;
import mz.org.fgh.idartlite.service.dispense.DispenseService;
import mz.org.fgh.idartlite.service.dispense.IDispenseService;
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.reports.DispenseReportUsActivity;

public class DispenseReportUsVM extends SearchVM<Dispense> {
    private IDispenseService dispenseService;

    public DispenseReportUsVM(@NonNull Application application) {
        super(application);
        dispenseService = new DispenseService(application, getCurrentUser());
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

    public List<Dispense> getDispensesByDates(Date startDate, Date endDate, long offset, long limit) throws SQLException {
        return dispenseService.getDispensesUsBetweenStartDateAndEndDateWithLimit(startDate, endDate,offset,limit);
    }

    @Override
    public void createPdfDocument() {
        try {
            this.getRelatedActivity().createPdfDocument();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
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


    public List<Dispense> doSearch(long offset, long limit) throws SQLException {
        return getDispensesByDates(getSearchParams().getStartdate(), getSearchParams().getEndDate(),offset,limit);
    }

    @Override
    public void doOnlineSearch(long offset, long limit) throws SQLException {
        super.doOnlineSearch(offset, limit);
        RestDispenseService.restGetAllDispenseByPeriod(getSearchParams().getStartdate(), getSearchParams().getEndDate(), getCurrentClinic().getUuid() ,offset,limit, this);
    }

    @Override
    public String validateBeforeSearch() {
        if (getSearchParams().getStartdate() == null || getSearchParams().getEndDate() == null ){
            return "Por favor indicar o período por analisar!";
        }else
        if (DateUtilities.dateDiff(getSearchParams().getEndDate(), getSearchParams().getStartdate(), DateUtilities.DAY_FORMAT) < 0){
            return "A data inicio deve ser menor que a data fim.";
        }else
        if ((int) (DateUtilities.dateDiff(DateUtilities.getCurrentDate(), getSearchParams().getStartdate(), DateUtilities.DAY_FORMAT)) < 0){
            return "A data inicio deve ser menor que a data corrente.";
        }
        else
        if ((int) DateUtilities.dateDiff(DateUtilities.getCurrentDate(), getSearchParams().getEndDate(), DateUtilities.DAY_FORMAT) < 0){
            return "A data fim deve ser menor que a data corrente.";
        }
        return null;
    }

    @Override
    public void displaySearchResults() {
        Utilities.hideSoftKeyboard(getRelatedActivity());

        getRelatedActivity().displaySearchResult();
    }

    @Override
    public AbstractSearchParams<Dispense> initSearchParams() {
        return new DispenseSearchParams();
    }

    @Override
    public DispenseSearchParams getSearchParams() {
        return (DispenseSearchParams) super.getSearchParams();
    }

    public DispenseReportUsActivity getRelatedActivity() {
        return (DispenseReportUsActivity) super.getRelatedActivity();
    }

    @Override
    public void preInit() {

    }

    @Bindable
    public Clinic getClinic(){
        return getCurrentClinic();
    }
}
