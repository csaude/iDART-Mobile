package mz.org.fgh.idartlite.service.stock;

import android.app.Application;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.base.service.BaseService;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.model.StockAjustment;
import mz.org.fgh.idartlite.model.User;
import mz.org.fgh.idartlite.model.inventory.Iventory;

public class StockAjustementService extends BaseService<StockAjustment> implements IStockAjustmentService {

    public StockAjustementService(Application application, User currentUser) {
        super(application, currentUser);
    }

    public StockAjustementService(Application application) {
        super(application);
    }

    @Override
    public void save(StockAjustment record) throws SQLException {
        super.save(record);
        getDataBaseHelper().getStockAjustmentDao().create(record);
    }

    @Override
    public void update(StockAjustment record) throws SQLException {
        super.update(record);
        getDataBaseHelper().getStockAjustmentDao().update(record);
    }

    @Override
    public void delete(StockAjustment record) throws SQLException {
        super.delete(record);

        getDataBaseHelper().getStockAjustmentDao().delete(record);
    }

    @Override
    public List<StockAjustment> getAllOfInventory(Iventory iventory) throws SQLException {
        return getDataBaseHelper().getStockAjustmentDao().getAllOfInventory(iventory);
    }

    @Override
    public void saveOrUpdateMany(List<StockAjustment> ajustmentList) throws SQLException {

        for (StockAjustment ajustment : ajustmentList){
            if (ajustment.getId() > 0){
                update(ajustment);
            }else {
                save(ajustment);
            }
        }
    }

    @Override
    public List<StockAjustment> getAllOfStock(Stock stock) throws SQLException {
        return getDataBaseHelper().getStockAjustmentDao().getAllOfStock(stock);
    }

    @Override
    public void deleteStockAjustments(List<StockAjustment> ajustmentList) throws SQLException {
         getDataBaseHelper().getStockAjustmentDao().delete(ajustmentList);
    }

}
