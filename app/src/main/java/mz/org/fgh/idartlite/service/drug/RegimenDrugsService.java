package mz.org.fgh.idartlite.service.drug;

import android.app.Application;

import com.google.gson.internal.LinkedTreeMap;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

import mz.org.fgh.idartlite.base.service.BaseService;
import mz.org.fgh.idartlite.model.Drug;
import mz.org.fgh.idartlite.model.RegimenDrug;
import mz.org.fgh.idartlite.model.TherapeuticRegimen;
import mz.org.fgh.idartlite.model.User;
import mz.org.fgh.idartlite.util.Utilities;

public class RegimenDrugsService extends BaseService<RegimenDrug> implements IRegimenDrugsService{

    private static final String TAG = "RegimenDrugsService";

    protected DrugService drugService = new DrugService(getApplication(), null);

    public RegimenDrugsService(Application application, User currentUser) {
        super(application, currentUser);
    }

    public RegimenDrugsService(Application application) {
        super(application);
    }

    @Override
    public void save(RegimenDrug record) throws SQLException {

    }

    @Override
    public void update(RegimenDrug record) throws SQLException {

    }

    public void createRegimenDrug(RegimenDrug regimenDrug) throws SQLException {
        getDataBaseHelper().getRegimenDrugDao().create(regimenDrug);
    }

    public void saveRegimenDrug(TherapeuticRegimen regimen, ArrayList drugs) {

        if (drugs.size() > 0) {
            for (Object drug : drugs) {
                try {
                    LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) drug;

                    if (Utilities.stringHasValue((String) itemresult.get("atccode_id"))
                            && (((String) itemresult.get("atccode_id")).length() > 2) && ((Boolean) itemresult.get("active") == true)) {

                        Drug localDrug = drugService.getDrugByFNMCode(Objects.requireNonNull(itemresult.get("atccode_id")).toString());

                        RegimenDrug regimenDrug = new RegimenDrug();
                        regimenDrug.setTherapeuticRegimen(regimen);
                        regimenDrug.setDrug(localDrug);
                        createRegimenDrug(regimenDrug);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    continue;
                }
            }
        }
    }
}
