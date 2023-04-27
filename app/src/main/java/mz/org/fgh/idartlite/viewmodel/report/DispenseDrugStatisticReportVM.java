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

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.base.viewModel.SearchVM;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.DispensedDrug;
import mz.org.fgh.idartlite.model.Drug;
import mz.org.fgh.idartlite.rest.service.Dispense.RestDispenseService;
import mz.org.fgh.idartlite.searchparams.AbstractSearchParams;
import mz.org.fgh.idartlite.searchparams.DispenseSearchParams;
import mz.org.fgh.idartlite.service.dispense.DispenseDrugService;
import mz.org.fgh.idartlite.service.dispense.DispenseService;
import mz.org.fgh.idartlite.service.dispense.IDispenseDrugService;
import mz.org.fgh.idartlite.service.dispense.IDispenseService;
import mz.org.fgh.idartlite.service.drug.ITherapheuticRegimenService;
import mz.org.fgh.idartlite.service.drug.TherapheuticRegimenService;
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.reports.DispenseDrugGraphStatisticReportActivity;
import mz.org.fgh.idartlite.view.reports.DispenseDrugStatisticReportActivity;

public class DispenseDrugStatisticReportVM extends SearchVM<Dispense> {


    private IDispenseService dispenseService;

    private IDispenseDrugService dispenseDrugService;

    private ITherapheuticRegimenService therapheuticRegimenService;

    public DispenseDrugStatisticReportVM(@NonNull Application application) {
        super(application);
        dispenseService = new DispenseService(application, getCurrentUser());
        therapheuticRegimenService = new TherapheuticRegimenService(application, getCurrentUser());
        dispenseDrugService = new DispenseDrugService(application, getCurrentUser());
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

        ArrayList list = new ArrayList();

        List<Dispense> dispenseList = dispenseService.getDispensesBetweenStartDateAndEndDateWithLimit(startDate, endDate,offset,limit);


        List<DispensedDrug> dispensedDrugs = dispenseDrugService.getDispensedDrugsByDispenses(dispenseList);


        Map<Drug,List<DispensedDrug>> drugsMap=new HashMap<>();


        for (int i=0;i<dispensedDrugs.size();i++){

            Drug    value = dispensedDrugs.get(i).getStock().getDrug();

            List<DispensedDrug> al = drugsMap.get(value);

            if (al == null)
            {
                drugsMap.put(value, (List<DispensedDrug>) (al = new ArrayList<DispensedDrug>()));
            }

            al.add(dispensedDrugs.get(i));
        }

        for (Drug drug: drugsMap.keySet()) {


            List<DispensedDrug> dispenseDrugs= drugsMap.get(drug);
            int quantityDispensed=0;
            for (DispensedDrug dispenseD:
                    dispenseDrugs) {

                quantityDispensed+=dispenseD.getQuantitySupplied();

            }

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("nomeMedicamento", drug.getDescription());
            map.put("totalFrascosDispensados", quantityDispensed);

            list.add(map);
        }


        return list;
    }

    @Override
    public String validateBeforeSearch() {
        if (getSearchParams().getStartdate() == null || getSearchParams().getStartdate() == null) {
            return getRelatedActivity().getString(R.string.start_end_date_is_mandatory);
        } else if (DateUtilities.dateDiff(getSearchParams().getEndDate(), getSearchParams().getStartdate(), DateUtilities.DAY_FORMAT) < 0) {
            return "A data inicio deve ser menor que a data fim.";
        } else if ((int) (DateUtilities.dateDiff(DateUtilities.getCurrentDate(),getSearchParams().getStartdate(), DateUtilities.DAY_FORMAT)) < 0) {
            return "A data inicio deve ser menor ou igual que a data corrente.";
        }

        return null;
    }

    @Override
    public void createPdfDocument() {
        try {
            ((DispenseDrugStatisticReportActivity)getRelatedActivity()).createPdfDocument();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doOnNoRecordFound() {
        Utilities.displayAlertDialog(getRelatedActivity(), "Não foram encontrados resultados para a sua pesquisa").show();
    }


    public List doSearch(long offset, long limit) throws SQLException {
        return getDispensesByDates(getSearchParams().getStartdate(), getSearchParams().getEndDate(), offset, limit);
    }

    @Override
    public void doOnlineSearch(long offset, long limit) throws SQLException {
        if (this instanceof DispenseDrugGraphStatisticReportVM) getLoadingDialog().startLoadingDialog();
        super.doOnlineSearch(offset, limit);
        boolean isUsDispenses = false;
        RestDispenseService.restGetAllDispenseByPeriod(getSearchParams().getStartdate(), getSearchParams().getEndDate(), getCurrentClinic().getUuid() ,offset,limit, this, isUsDispenses);
    }

    @Override
    public List doBeforeDisplay(List<Dispense> objects) {
        ArrayList list = new ArrayList();

        List<DispensedDrug> dispensedDrugs = new ArrayList<>();

        for (Dispense dispense : objects) {
            dispensedDrugs.addAll(dispense.getDispensedDrugs());
        }

        Map<Drug,List<DispensedDrug>> drugsMap=new HashMap<>();


        for (int i=0;i<dispensedDrugs.size();i++){

            Drug    value = dispensedDrugs.get(i).getStock().getDrug();

            List<DispensedDrug> al = drugsMap.get(value);

            if (al == null)
            {
                drugsMap.put(value, (List<DispensedDrug>) (al = new ArrayList<DispensedDrug>()));
            }

            al.add(dispensedDrugs.get(i));
        }

        for (Drug drug: drugsMap.keySet()) {


            List<DispensedDrug> dispenseDrugs= drugsMap.get(drug);
            int quantityDispensed=0;
            for (DispensedDrug dispenseD:
                    dispenseDrugs) {

                quantityDispensed+=dispenseD.getQuantitySupplied();

            }

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("nomeMedicamento", drug.getDescription());
            map.put("totalFrascosDispensados", quantityDispensed);

            list.add(map);
        }


        return list;
    }

    @Override
    public void displaySearchResults() {
        Utilities.hideSoftKeyboard(getRelatedActivity());

        if (this instanceof  DispenseDrugGraphStatisticReportVM) {
            ((DispenseDrugGraphStatisticReportActivity) getRelatedActivity()).displaySearchResult();
        } else
            ((DispenseDrugStatisticReportActivity)getRelatedActivity()).displaySearchResult();

    }

    @Bindable
    public Clinic getClinic() {
        return getCurrentClinic();
    }

    @Override
    public AbstractSearchParams<Dispense> initSearchParams() {
        return new DispenseSearchParams();
    }

    @Override
    public void preInit() {}

    @Override
    public DispenseSearchParams getSearchParams() {
        return (DispenseSearchParams) super.getSearchParams();
    }
}
