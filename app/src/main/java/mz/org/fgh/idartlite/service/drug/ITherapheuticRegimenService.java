package mz.org.fgh.idartlite.service.drug;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.model.DiseaseType;
import mz.org.fgh.idartlite.model.TherapeuticRegimen;


public interface ITherapheuticRegimenService extends IBaseService<TherapeuticRegimen> {

    public void createTherapheuticRegimen(TherapeuticRegimen therapeuticRegimen) throws SQLException;

    public List<TherapeuticRegimen> getAll() throws SQLException ;

    public TherapeuticRegimen getTherapeuticRegimenByDescription(String description) throws SQLException ;

    public TherapeuticRegimen getTherapeuticRegimenByCode(String code) throws SQLException ;

    public boolean checkRegimen(Object regimen) ;

    public void saveRegimen(Object regimen);

    public List<TherapeuticRegimen> getAllTherapeuticRegimenByDiseaseType(DiseaseType diseaseType) throws SQLException;

}
