package mz.org.fgh.idartlite.dao.stock;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.dao.generic.IGenericDao;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.model.StockAjustment;
import mz.org.fgh.idartlite.model.inventory.Iventory;

public interface IStockAjustmentDao extends IGenericDao<StockAjustment, Integer> {


    List<StockAjustment> getAllOfInventory(Iventory iventory) throws SQLException;

    List<StockAjustment> getAllOfStock(Stock stock) throws SQLException;
}
