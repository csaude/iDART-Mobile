package mz.org.fgh.idartlite.viewmodel.stock;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.BR;
import mz.org.fgh.idartlite.base.activity.BaseActivity;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.base.viewModel.SearchVM;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.searchparams.AbstractSearchParams;
import mz.org.fgh.idartlite.service.stock.IStockService;
import mz.org.fgh.idartlite.service.stock.StockService;
import mz.org.fgh.idartlite.view.stock.panel.StockEntranceActivity;
import mz.org.fgh.idartlite.view.stock.panel.StockEntranceFragment;

public class StockEntranceVM extends SearchVM<Stock> {

    private Clinic clinic;
    private Stock stock;
    private IStockService stockService;
    private boolean initialDataVisible;
    private boolean drugDataVisible;

    private StockEntranceFragment entranceFragment;


    public StockEntranceVM(@NonNull Application application) {
        super(application);
        stockService = new StockService(getApplication(), getCurrentUser());
        stock = new Stock();
    }

    @Override
    protected IBaseService initRelatedService() {
        return null;
    }

    @Override
    protected void doOnNoRecordFound() {
    }

    @Override
    protected BaseModel initRecord() {
        return null;
    }


    @Override
    protected void initFormData() {

    }

    private List<Stock> getStockByClinic(Clinic clinic, long offset, long limit) throws SQLException {
        return stockService.getStockByClinic(clinic, offset, limit);
    }

    public List<Stock> getStockByOrderNumber(String orderNumber, Clinic clinic) throws SQLException {
        return stockService.getStockByOrderNumber(orderNumber, clinic);
    }

    public void deleteStock(Stock stock) throws SQLException {
        this.stockService.deleteStock(stock);
    }

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public BaseActivity getRelatedActivity() {
        return super.getRelatedActivity();
    }

    @Override
    public void preInit() {

    }

    @Bindable
    public boolean isInitialDataVisible() {
        return initialDataVisible;
    }

    public void setInitialDataVisible(boolean initialDataVisible) {
        this.initialDataVisible = initialDataVisible;
        notifyPropertyChanged(BR.initialDataVisible);
    }

    @Bindable
    public boolean isDrugDataVisible() {
        return drugDataVisible;
    }


    public void setDrugDataVisible(boolean drugDataVisible) {
        this.drugDataVisible = drugDataVisible;
        notifyPropertyChanged(BR.drugDataVisible);
    }

    public StockEntranceFragment getEntranceFragment() {
        return entranceFragment;
    }

    public void setEntranceFragment(StockEntranceFragment entranceFragment) {
        this.entranceFragment = entranceFragment;
    }

    public void initNewStock(){
        this.stock = new Stock();
    }

    @Override
    public List<Stock> doSearch(long offset, long limit) throws SQLException {
        return getStockByClinic(getCurrentClinic(), offset, limit);
    }

    @Override
    public void displaySearchResults() {
        getEntranceFragment().displaySearchResults();
    }

    @Override
    public AbstractSearchParams<Stock> initSearchParams() {
        return null;
    }

    public void changeInitialDataViewStatus(View view){
        ((StockEntranceActivity) getRelatedActivity()).changeFormSectionVisibility(view);
    }
}
