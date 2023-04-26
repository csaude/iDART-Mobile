package mz.org.fgh.idartlite.dao.drug;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.dao.generic.IGenericDao;
import mz.org.fgh.idartlite.model.DiseaseType;
import mz.org.fgh.idartlite.model.TherapeuticRegimen;

public interface ITherapeuticRegimenDao extends IGenericDao<TherapeuticRegimen, Integer> {

    public List<TherapeuticRegimen> getAll() throws SQLException;

    public TherapeuticRegimen getTherapeuticRegimenByDescription(String description) throws SQLException ;

    public TherapeuticRegimen getTherapeuticRegimenByCode(String code) throws SQLException ;

    public List<TherapeuticRegimen> getAllByDiseaseType(DiseaseType diseaseType) throws SQLException;
}
