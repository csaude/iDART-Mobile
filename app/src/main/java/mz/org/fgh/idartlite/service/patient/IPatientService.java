package mz.org.fgh.idartlite.service.patient;

import android.app.Application;

import com.google.gson.internal.LinkedTreeMap;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.listener.rest.RestResponseListener;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.patient.Patient;


public interface IPatientService extends IBaseService<Patient> {

    public List<Patient> searchPatientByParamAndClinic(String param, Clinic clinic, long offset, long limit) throws SQLException ;

    public List<Patient> getALLPatient() throws  SQLException ;

    public void  savePatient(Patient patient) throws SQLException ;

    int countNewPatientsByPeriod(Date start, Date end, String sanitaryUnit) throws SQLException;

    public Patient getPatientByUuid(String uuid) throws SQLException ;

    public boolean checkPatient(LinkedTreeMap<String, Object> patient) ;

    public Patient saveOnPatient(LinkedTreeMap<String, Object> patient) throws  Exception;

    public void  updatePatient(Patient patient) throws SQLException ;

    public Patient checkExistsPatientWithNID(String nid) throws SQLException ;

    public List<Patient> getPatientsBetweenStartDateAndEndDate(Application application, Date start, Date end , long offset, long limit) throws SQLException;

    public List<Patient> searchPatientByNidOrNameOrSurname(String nid, String name, String surname, long offset, long limit, RestResponseListener listener) throws SQLException;

    List<String> getSanitaryUnitsWithRecordsOnPeriod(Date start, Date end) throws SQLException;

    public Patient updateOnPatientViaRest(LinkedTreeMap<String, Object> patient) throws Exception;

    void changePatienToFaltosoOrAbandono(Patient patient) throws SQLException;

    void savePatientAndDetails(Patient patient) throws SQLException;

}
