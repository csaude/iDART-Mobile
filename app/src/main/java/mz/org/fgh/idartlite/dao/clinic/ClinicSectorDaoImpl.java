package mz.org.fgh.idartlite.dao.clinic;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.dao.generic.GenericDaoImpl;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.ClinicSector;
import mz.org.fgh.idartlite.model.ClinicSectorType;

public class ClinicSectorDaoImpl extends GenericDaoImpl<ClinicSector, Integer> implements IClinicSectorDao {

    public ClinicSectorDaoImpl(Class dataClass) throws SQLException {
        super(dataClass);
    }

    public ClinicSectorDaoImpl(ConnectionSource connectionSource, Class dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public ClinicSectorDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }


    @Override
    public List<ClinicSector> getClinicSectorsByClinic(Clinic clinic) throws SQLException {
        return queryBuilder().where().eq(ClinicSector.COLUMN_CLINIC_ID, clinic.getId()).query();
    }

    @Override
    public ClinicSector getClinicSector() throws SQLException {
       return queryBuilder().queryForFirst();
    }

    @Override
    public List<ClinicSector> getClinicSectorsByTYpe(ClinicSectorType clinicSectorType) throws SQLException {
        return queryBuilder().where().eq(ClinicSector.COLUMN_CLINIC_SECTOR_TYPE_ID, clinicSectorType.getId()).query();
    }


}
