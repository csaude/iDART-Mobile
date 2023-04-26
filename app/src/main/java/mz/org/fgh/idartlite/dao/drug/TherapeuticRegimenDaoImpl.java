package mz.org.fgh.idartlite.dao.drug;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import mz.org.fgh.idartlite.dao.generic.GenericDaoImpl;
import mz.org.fgh.idartlite.model.DiseaseType;
import mz.org.fgh.idartlite.model.TherapeuticLine;
import mz.org.fgh.idartlite.model.TherapeuticRegimen;

import java.sql.SQLException;
import java.util.List;

public class  TherapeuticRegimenDaoImpl extends GenericDaoImpl<TherapeuticRegimen, Integer> implements ITherapeuticRegimenDao {

    public TherapeuticRegimenDaoImpl(Class dataClass) throws SQLException {
        super(dataClass);
    }

    public TherapeuticRegimenDaoImpl(ConnectionSource connectionSource, Class dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public TherapeuticRegimenDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    @Override
    public List<TherapeuticRegimen> getAll() throws SQLException {
        return queryForAll();
    }

    @Override
    public TherapeuticRegimen getTherapeuticRegimenByDescription(String description) throws SQLException {
        return queryBuilder().where().eq(TherapeuticRegimen.COLUMN_DESCRIPTION, description).queryForFirst();
    }

    @Override
    public TherapeuticRegimen getTherapeuticRegimenByCode(String code) throws SQLException {
        return queryBuilder().where().eq(TherapeuticRegimen.COLUMN_REGIMEN_CODE, code).queryForFirst();
    }

    @Override
    public List<TherapeuticRegimen> getAllByDiseaseType(DiseaseType diseaseType) throws SQLException {
        return queryBuilder().where().eq(TherapeuticRegimen.COLUMN_DISEASE_TYPE_ID , diseaseType.getId()).query();
    }
}
