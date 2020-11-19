package mz.org.fgh.idartlite.viewmodel.stock;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mz.org.fgh.idartlite.BR;
import mz.org.fgh.idartlite.adapter.recyclerview.listable.Listble;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.viewModel.BaseViewModel;
import mz.org.fgh.idartlite.model.DestroyedDrug;
import mz.org.fgh.idartlite.model.Drug;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.service.drug.DrugService;
import mz.org.fgh.idartlite.service.stock.DestroyedStockDrugService;
import mz.org.fgh.idartlite.service.stock.StockService;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.stock.destroy.DestroyStockActivity;

public class DestroyStockVM extends BaseViewModel {

    private Drug selectedDrug;

    private List<Drug> drugs;

    private List<Listble> stockList;

    public DestroyStockVM(@NonNull Application application) {
        super(application);
    }

    @Override
    protected BaseModel initRecord() {
        return new DestroyedDrug();
    }

    @Override
    protected Class<DestroyedStockDrugService> getRecordServiceClass() {
        return DestroyedStockDrugService.class;
    }


    @Override
    protected void initFormData() {
        try {
            this.drugs = getAllDrugs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected List<Drug> getAllDrugs() throws SQLException {
        return ((DrugService)baseServiceFactory.get(DrugService.class)).getAll();
    }

    @Override
    public void save() {
        try {
            List<DestroyedDrug> stocksToDestroy = new ArrayList<>();

            String errorMsg = getRelatedRecord().isValid(getRelatedActivity());

            if (Utilities.stringHasValue(errorMsg)){
                Utilities.displayAlertDialog(getRelatedActivity(), errorMsg).show();
            }
            else {
                for (Listble listble : getRelatedActivity().getStockToDestroy()) {
                    if (((DestroyedDrug) listble).getQtyToModify() > 0) {
                        stocksToDestroy.add(((DestroyedDrug) listble));
                        stocksToDestroy.get(stocksToDestroy.size() - 1).setNotes(getRelatedRecord().getNotes());
                        stocksToDestroy.get(stocksToDestroy.size() - 1).setDate(getRelatedRecord().getDate());
                    }
                }

                if (!Utilities.listHasElements(stocksToDestroy)) {
                    Utilities.displayAlertDialog(getRelatedActivity(), "Não indicou nenhuma quantidade de stock para destruir.").show();
                }else {
                    getRecordService().saveAll(stocksToDestroy);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DestroyedStockDrugService getRecordService() {
        return (DestroyedStockDrugService) super.getRecordService();
    }

    @Override
    public DestroyedDrug getRelatedRecord() {
        return (DestroyedDrug) super.getRelatedRecord();
    }

    @Bindable
    public Drug getSelectedDrug() {
        return selectedDrug;
    }

    public List<Drug> getDrugs() {
        return drugs;
    }

    public void setDestryedDrugDate(Date date){
        getRelatedRecord().setDate(date);
        notifyPropertyChanged(BR.destryedDrugDate);
    }

    @Override
    public DestroyStockActivity getRelatedActivity() {
        return (DestroyStockActivity) super.getRelatedActivity();
    }

    @Bindable
    public Date getDestryedDrugDate(){
        return getRelatedRecord().getDate();
    }

    public void changeDataViewStatus(View view){
        getRelatedActivity().changeFormSectionVisibility(view);
    }

    public void setSelectedDrug(Drug selectedDrug) {
        this.selectedDrug = selectedDrug;
    }


    public void addSelectedDrug(){
        if (stockList == null) stockList = new ArrayList<>();

        try {
            List<Stock> drugStocks = ((StockService)baseServiceFactory.get(StockService.class)).getAll(selectedDrug);

            if (Utilities.listHasElements(drugStocks)) {
                for (Stock stock : drugStocks) {
                    DestroyedDrug destroyedDrug = initNewDestroiedDrug(stock);

                    if (!stockList.contains(destroyedDrug)) {

                        destroyedDrug.setListType(Listble.STOCK_DESTROY_LISTING);
                        stockList.add(destroyedDrug);

                        notifyPropertyChanged(BR.selectedDrug);
                    } else {
                        Utilities.displayAlertDialog(getRelatedActivity(), "Ja se encontra a visualizar todo o stock do medicamento seleccionado").show();
                    }
                }
                getRelatedActivity().displaySelectedDrugs();
            }else {
                Utilities.displayAlertDialog(getRelatedActivity(), "Medicamento seleccionado não possui stock.").show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private DestroyedDrug initNewDestroiedDrug(Stock stock) {
        return new DestroyedDrug(stock);
    }

    @Bindable
    public String getNotes() {
        return getRelatedRecord().getNotes();
    }

    public void setNotes(String notes) {
        getRelatedRecord().setNotes(notes);
        notifyPropertyChanged(BR.notes);
    }

    public List<Listble> getStockList() {
        return stockList;
    }
}
