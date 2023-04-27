package mz.org.fgh.idartlite.dao.stock;

import android.app.Application;

import com.j256.ormlite.stmt.ColumnArg;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import mz.org.fgh.idartlite.base.databasehelper.IdartLiteDataBaseHelper;
import mz.org.fgh.idartlite.dao.generic.GenericDaoImpl;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.Drug;
import mz.org.fgh.idartlite.model.ReferedStockMoviment;
import mz.org.fgh.idartlite.model.Stock;

public class StockDaoImpl extends GenericDaoImpl<Stock, Integer> implements IStockDao {

    public StockDaoImpl(Class dataClass) throws SQLException {
        super(dataClass);
    }

    public StockDaoImpl(ConnectionSource connectionSource, Class dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public StockDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    @Override
    public List<Stock> getAllOfClinic(Application application, Clinic clinic, long offset, long limit) throws SQLException {
        QueryBuilder<ReferedStockMoviment, Integer> referedQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getReferedStockMovimentDao().queryBuilder();
        referedQb.where().eq(ReferedStockMoviment.COLUMN_STOCK_ID, new ColumnArg(Stock.TABLE_NAME, Stock.COLUMN_ID));

        QueryBuilder<Stock, Integer> stockQb =  IdartLiteDataBaseHelper.getInstance(application.getApplicationContext()).getStockDao().queryBuilder();
        if (offset > 0 && limit > 0) stockQb.offset(offset).limit(limit);
        stockQb.orderBy(Stock.COLUMN_DATE_RECEIVED, false).groupBy(Stock.COLUMN_ORDER_NUMBER);
        stockQb.where().eq(Stock.COLUMN_CLINIC, clinic.getId()).and().not().exists(referedQb);

        return stockQb.query();
    }

    @Override
    public List<Stock> getStockByOrderNumber(String orderNumber, Clinic clinic) throws SQLException {
        return queryBuilder().where().eq(Stock.COLUMN_CLINIC, clinic.getId()).and().eq(Stock.COLUMN_ORDER_NUMBER, orderNumber).query();
    }

    @Override
    public boolean checkStockExist(String orderNumber, Clinic clinic) throws SQLException {
        return queryBuilder().where().eq(Stock.COLUMN_CLINIC, clinic.getId()).and().like(Stock.COLUMN_ORDER_NUMBER, "%" + orderNumber + "%").query().isEmpty();
    }

    @Override
    public List<Stock> getAllStocksByClinicAndDrug(Clinic clinic, Drug drug) throws SQLException {
        return queryBuilder().where().eq(Stock.COLUMN_CLINIC,clinic.getId()).and().eq(Stock.COLUMN_DRUG, drug.getId()).query();
    }

    @Override
    public List<Stock> getAllStocksByDrug(Drug drug) throws SQLException {
        for(Stock stock : queryBuilder()
                .where()
                .eq(Stock.COLUMN_DRUG, drug.getId())
                .and()
                .eq(Drug.COLUMN_DISEASE_TYPE, "TARV")
                .query()){
            System.out.println(stock);
        }
        return queryBuilder().where().eq(Stock.COLUMN_DRUG, drug.getId()).query();
    }
//    public List<Stock> getAllStocksByDrug(Drug drug) throws SQLException {
//        for(Stock stock : queryBuilder().where().eq(Stock.COLUMN_DRUG, drug.getId()).query()){
//            System.out.println(stock);
//        }
//        return queryBuilder().where().eq(Stock.COLUMN_DRUG, drug.getId()).query();
//    }

    @Override
    public List<Stock> getAll(Drug drug) throws SQLException {
        QueryBuilder qb = queryBuilder();
        qb.where().eq(Stock.COLUMN_DRUG, drug.getId());
        qb.orderBy(Stock.COLUMN_EXPIRY_DATE, true);
        return qb.query();
    }

    @Override
    public Stock getByBatchNumber(Stock stock) throws SQLException {
        return queryBuilder().where().eq(Stock.COLUMN_BATCH_NUMBER, stock.getBatchNumber()).queryForFirst();
    }

    @Override
    public List<Stock> getStockListByDates(Drug drug, Date startDate, Date endDate) throws SQLException {
        return queryBuilder().where().eq(Stock.COLUMN_DRUG, drug.getId()).and().gt(Stock.COLUMN_DATE_RECEIVED,startDate).and().lt(Stock.COLUMN_DATE_RECEIVED,endDate).query();
    }
}
