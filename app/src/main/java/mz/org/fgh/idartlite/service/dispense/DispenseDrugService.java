package mz.org.fgh.idartlite.service.dispense;

import android.app.Application;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.base.service.BaseService;
import mz.org.fgh.idartlite.model.DispensedDrug;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.model.User;

public class DispenseDrugService extends BaseService<DispensedDrug> implements IDispenseDrugService {

    public DispenseDrugService(Application application, User currUser) {
        super(application, currUser);
    }

    public DispenseDrugService(Application application) {
        super(application);
    }

    @Override
    public void save(DispensedDrug record) throws SQLException {

    }

    @Override
    public void update(DispensedDrug relatedRecord) throws SQLException {

    }

    public void createDispensedDrug(DispensedDrug dispenseDrug) throws SQLException {
        getDataBaseHelper().getDispensedDrugDao().create(dispenseDrug);
    }

    public List<DispensedDrug> findDispensedDrugByDispenseId(int id) throws SQLException {

        return getDataBaseHelper().getDispensedDrugDao().findDispensedDrugByDispenseId(id);
    }


    public boolean checkStockIsDispensedDrug(Stock stock) throws SQLException {
        return getDataBaseHelper().getDispensedDrugDao().checkStockIsDispensedDrug(stock);
    }

}