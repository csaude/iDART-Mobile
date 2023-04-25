package mz.org.fgh.idartlite.service.stock;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.model.StockAjustment;
import mz.org.fgh.idartlite.model.inventory.Iventory;

public interface IStockAjustmentService extends IBaseService<StockAjustment> {
    List<StockAjustment> getAllOfInventory(Iventory iventory) throws SQLException;

    void saveOrUpdateMany(List<StockAjustment> ajustmentList) throws SQLException;

    List<StockAjustment> getAllOfStock(Stock stock) throws SQLException;

    void deleteStockAjustments(List<StockAjustment> ajustmentList) throws SQLException;
}
