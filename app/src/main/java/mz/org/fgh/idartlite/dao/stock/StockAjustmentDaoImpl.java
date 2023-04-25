package mz.org.fgh.idartlite.dao.stock;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.dao.generic.GenericDaoImpl;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.model.StockAjustment;
import mz.org.fgh.idartlite.model.inventory.Iventory;

public class StockAjustmentDaoImpl extends GenericDaoImpl<StockAjustment, Integer> implements IStockAjustmentDao {

    public StockAjustmentDaoImpl(Class dataClass) throws SQLException {
        super(dataClass);
    }

    public StockAjustmentDaoImpl(ConnectionSource connectionSource, Class dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public StockAjustmentDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    @Override
    public List<StockAjustment> getAllOfInventory(Iventory iventory) throws SQLException {
        return queryBuilder().where().eq(StockAjustment.COLUMN_IVENTORY_ID, iventory.getId()).query();
    }

    @Override
    public List<StockAjustment> getAllOfStock(Stock stock) throws SQLException {
        return queryBuilder().orderBy(StockAjustment.COLUMN_DATE,false).where().eq(StockAjustment.COLUMN_STOCK_ID, stock.getId()).query();
    }
}
