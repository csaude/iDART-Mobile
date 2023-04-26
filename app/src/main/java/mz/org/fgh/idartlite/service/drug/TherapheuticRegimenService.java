package mz.org.fgh.idartlite.service.drug;

import android.app.Application;

import com.google.gson.internal.LinkedTreeMap;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mz.org.fgh.idartlite.base.service.BaseService;
import mz.org.fgh.idartlite.model.DiseaseType;
import mz.org.fgh.idartlite.model.TherapeuticRegimen;
import mz.org.fgh.idartlite.model.User;
import mz.org.fgh.idartlite.util.Utilities;

public class TherapheuticRegimenService extends BaseService<TherapeuticRegimen> implements  ITherapheuticRegimenService{

    private RegimenDrugsService regimenDrugsService = new RegimenDrugsService(getApplication(),null);
    private DiseaseTypeService diseaseTypeService = new DiseaseTypeService(getApplication(),null);

    public TherapheuticRegimenService(Application application, User currUser) {
        super(application, currUser);
    }

    public TherapheuticRegimenService(Application application) {
        super(application);
    }

    @Override
    public void save(TherapeuticRegimen record) throws SQLException {

    }

    @Override
    public void update(TherapeuticRegimen record) throws SQLException {

    }

    public void createTherapheuticRegimen(TherapeuticRegimen therapeuticRegimen) throws SQLException {
        getDataBaseHelper().getTherapeuticRegimenDao().create(therapeuticRegimen);
    }

    public List<TherapeuticRegimen> getAll() throws SQLException {
        return getDataBaseHelper().getTherapeuticRegimenDao().getAll();
    }

    public TherapeuticRegimen getTherapeuticRegimenByDescription(String description) throws SQLException {

        TherapeuticRegimen typeList = getDataBaseHelper().getTherapeuticRegimenDao().getTherapeuticRegimenByDescription(description);

        if (typeList != null) return typeList;

        return null;
    }

    public TherapeuticRegimen getTherapeuticRegimenByCode(String code) throws SQLException {

        TherapeuticRegimen typeList = getDataBaseHelper().getTherapeuticRegimenDao().getTherapeuticRegimenByCode(code);
        if (typeList != null) return typeList;

        return null;
    }

    public boolean checkRegimen(Object regimen) {

        boolean result = false;

        LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) regimen;

        try {

            TherapeuticRegimen localRegimen = getTherapeuticRegimenByCode(Objects.requireNonNull(itemresult.get("codigoregime")).toString());

            if(localRegimen != null)
                result = true;

        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public void saveRegimen(Object regimen){

        TherapeuticRegimen localRegimen = new TherapeuticRegimen();
        try {
            LinkedTreeMap<String, Object> itemresult = (LinkedTreeMap<String, Object>) regimen;
            String diseaseTypeCode = "";
            localRegimen.setRestId((int) Float.parseFloat(Objects.requireNonNull(itemresult.get("regimeid")).toString()));
            localRegimen.setRegimenCode(Objects.requireNonNull(itemresult.get("codigoregime")).toString());
            localRegimen.setDescription(Objects.requireNonNull(itemresult.get("regimeesquema")).toString());
             if (Objects.requireNonNull(itemresult.get("tipodoenca")).toString().equalsIgnoreCase("TB")) {
                 diseaseTypeCode = "TPT" ;
             } else {
                 diseaseTypeCode =  Objects.requireNonNull(itemresult.get("tipodoenca")).toString();
             }

            localRegimen.setDiseaseType(diseaseTypeService.getDiseaseTypeByCode(diseaseTypeCode));
            createTherapheuticRegimen(localRegimen);

            if (Utilities.listHasElements((ArrayList<?>) itemresult.get("drug")))
            regimenDrugsService.saveRegimenDrug(localRegimen, (ArrayList) Objects.requireNonNull(itemresult.get("drug")));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<TherapeuticRegimen> getAllTherapeuticRegimenByDiseaseType(DiseaseType diseaseType) throws SQLException {
        return getDataBaseHelper().getTherapeuticRegimenDao().getAllByDiseaseType(diseaseType);
    }

}
