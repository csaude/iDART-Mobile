package mz.org.fgh.idartlite.service.patient;

import android.app.Application;
import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import mz.org.fgh.idartlite.base.service.BaseService;
import mz.org.fgh.idartlite.dao.patient.IPatientDao;
import mz.org.fgh.idartlite.listener.rest.RestResponseListener;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.Episode;
import mz.org.fgh.idartlite.model.patient.PatientAttribute;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.User;
import mz.org.fgh.idartlite.service.clinic.ClinicService;
import mz.org.fgh.idartlite.service.episode.EpisodeService;
import mz.org.fgh.idartlite.service.prescription.PrescriptionService;
import mz.org.fgh.idartlite.util.Utilities;

import static mz.org.fgh.idartlite.util.DateUtilities.getSqlDateFromString;

public class PatientService extends BaseService<Patient> implements IPatientService {

    private IPatientDao patientDao;
    protected ClinicService clinicService;
    protected PrescriptionService prescriptionService;
    protected EpisodeService episodeService;
    private IPatientAttributeService attributeService;

    public PatientService(Application application, User currentUser) {
        super(application, currentUser);
        init();
    }

    private void init() {
        try {
            patientDao = getDataBaseHelper().getPatientDao();
            this.prescriptionService = new PrescriptionService(application, currentUser);
            this.episodeService = new EpisodeService(application, currentUser);
            this.clinicService = new ClinicService(application, currentUser);
            this.attributeService = new PatientAttributeService(application, currentUser);


        } catch (SQLException sql) {
            Log.i("erro ", sql.getMessage());
        }
    }

    public PatientService(Application application) {
        super(application);
        init();
    }

    @Override
    public void save(Patient record) throws SQLException {
        savePatient(record);
    }

    @Override
    public void update(Patient record) throws SQLException {

    }

    public List<Patient> searchPatientByParamAndClinic(String param, Clinic clinic, long offset, long limit) throws SQLException {
        return patientDao.searchPatientByParamAndClinic(param, clinic, offset, limit);
    }

    public List<Patient> getALLPatient() throws SQLException {
        return patientDao.queryForAll();
    }

    public List<Patient> get(long offset, long limit) throws SQLException {
        return patientDao.get(application, offset, limit);
    }

    public void savePatient(Patient patient) throws SQLException {
        patientDao.create(patient);
        if (Utilities.listHasElements(patient.getAttributes())) {
            for (PatientAttribute attribute : patient.getAttributes()) {
                attributeService.save(attribute);
            }
        }
    }

    @Override
    public int countNewPatientsByPeriod(Date start, Date end, String sanitaryUnit) throws SQLException {
        return patientDao.countNewPatientsByPeriod(start, end, sanitaryUnit, getApplication());
    }

    public Patient getPatientByUuid(String uuid) throws SQLException {

        Patient typeList = patientDao.getPatientByUuid(uuid);

        if (typeList != null) return typeList;

        return null;
    }

    public boolean checkPatient(LinkedTreeMap<String, Object> patient) {

        boolean result = false;

        try {
            Patient localPatient = getPatientByUuid((Objects.requireNonNull(patient.get("uuidopenmrs")).toString()));

            if (localPatient != null)
                result = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public Patient saveOnPatient(LinkedTreeMap<String, Object> patient) throws Exception {

        Patient localPatient = setPatientFromRest(patient);
        savePatient(localPatient);
        episodeService.saveEpisodeFromRest(patient, localPatient);
        prescriptionService.saveLastPrescriptionFromRest(patient, localPatient);

        return localPatient;
    }


    @Override
    public void updatePatient(Patient patient) throws SQLException {
        this.getDataBaseHelper().getPatientDao().update(patient);
    }

    @Override
    public Patient checkExistsPatientWithNID(String nid) throws SQLException {
        return getDataBaseHelper().getPatientDao().checkExistsPatientWithNID(nid);
    }

    @Override
    public List<Patient> getPatientsBetweenStartDateAndEndDate(Application application, Date start, Date end, long offset, long limit) throws SQLException {

      /* episodeService = new EpisodeService(application, currentUser);
         List<Episode> startEpisodes=episodeService.getAllStartEpisodesBetweenStartDateAndEndDate(start,end,offset,limit);

         List<Patient> patients=new ArrayList<>();
        for (Episode episode:
             startEpisodes) {
            patients.add(episode.getPatient());

        }
        return  patients;*/

        return patientDao.getAllPatientsBetweenStartDateAndEndDate(application, start, end, offset, limit);
    }

    @Override
    public List<Patient> searchPatientByNidOrNameOrSurname(String nid, String name, String surname, long offset, long limit, RestResponseListener listener) throws SQLException {

        List<Patient> patients = new ArrayList<>();
        patients = patientDao.searchPatientByNidOrNameOrSurname(nid, name, surname, offset, limit);

      /*  if(patients.isEmpty()){
            List<Patient> patients1 = RestPatientService.restGetPatientByNidOrNameOrSurname(nid, name, surname, this);
*/
        //   }
        return patients;
    }

    @Override
    public List<String> getSanitaryUnitsWithRecordsOnPeriod(Date start, Date end) throws SQLException {
        List<String> usLits = new ArrayList<>();

        List<Episode> episodes = episodeService.getAllStartEpisodesBetweenStartDateAndEndDate(start, end);

        if (!Utilities.listHasElements(episodes)) return null;

        for (Episode episode : episodes) {
            if (!usLits.contains(episode.getSanitaryUnit())) usLits.add(episode.getSanitaryUnit());
        }
        return usLits;
    }

    @Override
    public Patient updateOnPatientViaRest(LinkedTreeMap<String, Object> patient) throws Exception {
        Patient localPatient = setPatientFromRest(patient);
        updatePatient(localPatient);
        episodeService.saveEpisodeFromRest(patient, localPatient);
        return localPatient;
    }

    @Override
    public void changePatienToFaltosoOrAbandono(Patient patient) throws SQLException {
        PatientAttribute attribute = attributeService.getByAttributeOfPatient(PatientAttribute.PATIENT_DISPENSATION_STATUS, patient);
        attribute.setValue(PatientAttribute.PATIENT_DISPENSATION_STATUS_FALTOSO);
        attributeService.update(attribute);
    }

    @Override
    public void savePatientAndDetails(Patient patient) throws SQLException {
        save(patient);
        if (Utilities.listHasElements(patient.getEpisodes())) {
            for (Episode episode : patient.getEpisodes()) {
                episodeService.save(episode);
            }
        }
        prescriptionService.save(patient.getPrescriptionList().get(0));
    }


    private String getFullAdreess(String address1, String address2, String address3) {
        return address1 + " " + address2 + " " + address3;
    }


    public Patient setPatientFromRest(LinkedTreeMap<String, Object> patient) {

        Patient localPatient = null;
        try {
            localPatient = getPatientByUuid(Objects.requireNonNull(patient.get("uuidopenmrs")).toString());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        if (localPatient == null) {
            localPatient = new Patient();
        }


        try {

            //Clinic clinic = clinicService.getClinicByUuid(Objects.requireNonNull(patient.get("clinicuuid")).toString());
            Clinic clinic = clinicService.getAllClinics().get(0);

            String concatAdrees = getFullAdreess(Objects.requireNonNull(patient.get("address1")).toString(),
                    Objects.requireNonNull(patient.get("address2")).toString(),
                    Objects.requireNonNull(patient.get("address3")).toString());

            localPatient.setAddress(concatAdrees);
            localPatient.setBirthDate(getSqlDateFromString(Objects.requireNonNull(patient.get("dateofbirth")).toString(), "yyyy-MM-dd'T'HH:mm:ss"));
            localPatient.setClinic(clinic);
            localPatient.setFirstName(Objects.requireNonNull(patient.get("firstnames")).toString());
            localPatient.setLastName(Objects.requireNonNull(patient.get("lastname")).toString());
            localPatient.setGender(Objects.requireNonNull(patient.get("sex")).toString());
            localPatient.setNid(Objects.requireNonNull(patient.get("patientid")).toString());
            localPatient.setPhone(Objects.requireNonNull(patient.get("cellphone")).toString());
            localPatient.setUuid(Objects.requireNonNull(patient.get("uuidopenmrs")).toString());

            if (patient.get("datainiciotarv") != null)
                if (!patient.get("datainiciotarv").toString().equalsIgnoreCase("null") &&
                        !patient.get("datainiciotarv").toString().equalsIgnoreCase("NA") &&
                        !patient.get("datainiciotarv").toString().equalsIgnoreCase("N/A"))
                    localPatient.setStartARVDate(getSqlDateFromString(Objects.requireNonNull(patient.get("datainiciotarv")).toString(), "dd MMM yyyy"));


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return localPatient;
    }

}
