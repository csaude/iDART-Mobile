package mz.org.fgh.idartlite.service.episode;

import android.app.Application;

import com.google.gson.internal.LinkedTreeMap;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.service.BaseService;
import mz.org.fgh.idartlite.model.Episode;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.User;
import mz.org.fgh.idartlite.service.clinic.ClinicService;
import mz.org.fgh.idartlite.service.patient.PatientService;
import mz.org.fgh.idartlite.util.DateUtilities;

import static mz.org.fgh.idartlite.util.DateUtilities.getSqlDateFromString;

public class EpisodeService extends BaseService<Episode> implements IEpisodeService{

    protected PatientService patientService;
    protected ClinicService clinicService;

    public EpisodeService(Application application, User currUser) {
        super(application, currUser);
       // this.patientService = new PatientService(getApp(), currUser);
    }

    public EpisodeService(Application application) {
        super(application);
    }

    @Override
    public void save(Episode record) throws SQLException {

    }

    @Override
    public void update(Episode record) throws SQLException {

    }

    public List<Episode> getAllEpisodesByPatient(Patient patient) throws SQLException{
        return getDataBaseHelper().getEpisodeDao().getAllByPatient(patient);
    }

    public Episode getLatestByPatient(Patient patient) throws SQLException{
        return getDataBaseHelper().getEpisodeDao().getLatestByPatient(patient);
    }

    public Episode findEpisodeWithStopReasonByPatient(Patient patient) throws SQLException {
        return getDataBaseHelper().getEpisodeDao().findEpisodeWithStopReasonByPatient(patient);
    }


    public void createEpisode(Episode episode) throws SQLException {
        getDataBaseHelper().getEpisodeDao().create(episode);
    }
    public void udpateEpisode(Episode episode) throws SQLException {
        getDataBaseHelper().getEpisodeDao().update(episode);
    }
    public void deleteEpisode(Episode episode) throws SQLException {
        getDataBaseHelper().getEpisodeDao().delete(episode);
    }

    public void saveEpisodeFromRest(LinkedTreeMap<String, Object> patient, Patient localPatient) {

        try {

            Date dataEpisodio = getSqlDateFromString(Objects.requireNonNull(patient.get("prescriptiondate")).toString(), "yyyy-MM-dd'T'HH:mm:ss");

            Episode episode = getLatestByPatient(localPatient);

            if(episode != null){
                assert dataEpisodio != null;
                if(episode.getStartReason() == null)
                    episode.setStartReason("");

                if((int) DateUtilities.dateDiff(dataEpisodio, episode.getEpisodeDate(), DateUtilities.DAY_FORMAT) > 0 && !episode.getStartReason().equalsIgnoreCase("Referido De")) {
                    buildEpisode(patient,localPatient,dataEpisodio);
                }else{
                    episode.setEpisodeDate(dataEpisodio);
                    episode.setPatient(localPatient);
                    episode.setSanitaryUnit(Objects.requireNonNull(patient.get("mainclinicname")).toString());
                    episode.setUsUuid(Objects.requireNonNull(patient.get("mainclinicuuid")).toString());
                    episode.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
                    udpateEpisode(episode);
                }
            }else{
                buildEpisode(patient, localPatient, dataEpisodio);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean patientHasEndingEpisode(Patient patient){
        Episode episode = null;
        try {
            episode=this.getLatestByPatient(patient);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(episode.getStopReason()!=null){
          return true;
        }
        else {
            return false;
        }

    }

    public List<Episode> getAllEpisodeByStatus(String status) throws SQLException {

     return   getDataBaseHelper().getEpisodeDao().getAllEpisodeByStatus(status);
    }


    public void saveOnEpisodeEnding(LinkedTreeMap<String, Object> episode) {

        Episode localEpisode = new Episode();
        try {

            this.patientService = new PatientService(getApp(), null);
            this.clinicService = new ClinicService(getApp(), null);
            Patient localPatient = patientService.getPatientByUuid(Objects.requireNonNull(episode.get("patientuuid")).toString());

            Episode lastEpisode = getLatestByPatient(localPatient);
            Date inputEpisodeData = getSqlDateFromString(Objects.requireNonNull(episode.get("startdate")).toString(), "yyyy-MM-dd'T'HH:mm:ss");

            if((int) DateUtilities.dateDiff(inputEpisodeData, lastEpisode.getEpisodeDate(), DateUtilities.DAY_FORMAT) > 0) {

                localEpisode.setPatient(localPatient);
                localEpisode.setSanitaryUnit(getLatestByPatient(localPatient).getSanitaryUnit());
                localEpisode.setUsUuid(Objects.requireNonNull(episode.get("usuuid")).toString());

                if (Objects.requireNonNull(episode.get("startreason") != null)) {
                    localEpisode.setEpisodeDate(inputEpisodeData);
                    if (episode.get("startnotes") != null)
                        localEpisode.setNotes(episode.get("startnotes").toString());
                } else {
                    localEpisode.setEpisodeDate(getSqlDateFromString(Objects.requireNonNull(episode.get("stopdate")).toString(), "yyyy-MM-dd'T'HH:mm:ss"));
                    if (episode.get("stopnotes") != null)
                        localEpisode.setNotes(Objects.requireNonNull(episode.get("stopnotes")).toString());
                }
                localEpisode.setStopReason("Referido para mesma US");

                localEpisode.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
                localEpisode.setUuid(UUID.randomUUID().toString());
                createEpisode(localEpisode);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkEpisodeExists(LinkedTreeMap<String, Object> episode){
       Patient localPatient=null;
        this.patientService = new PatientService(getApp(), null);
        try {
             localPatient = patientService.getPatientByUuid(Objects.requireNonNull(episode.get("patientuuid")).toString());
        }
     catch (SQLException e) {
        e.printStackTrace();
    }
     return patientHasEndingEpisode(localPatient);

    }

    @Override
    public List<Episode> getAllStartEpisodesBetweenStartDateAndEndDate(Date start, Date end,long limit,long offset) throws SQLException {
        return getDataBaseHelper().getEpisodeDao().getAllStartEpisodesBetweenStartDateAndEndDate(start,end,limit,offset);
    }

    @Override
    public List<Episode> getAllStartEpisodesBetweenStartDateAndEndDate(Date start, Date end) throws SQLException {
        return getDataBaseHelper().getEpisodeDao().getAllStartEpisodesBetweenStartDateAndEndDate(start,end);
    }

    public void buildEpisode(LinkedTreeMap<String, Object> patient, Patient localPatient, Date dataEpisodio) throws SQLException {
       Episode episode = new Episode();
                episode.setEpisodeDate(dataEpisodio);
                episode.setPatient(localPatient);
                episode.setSanitaryUnit(Objects.requireNonNull(patient.get("mainclinicname")).toString());
                episode.setUsUuid(Objects.requireNonNull(patient.get("mainclinicuuid")).toString());
                episode.setStartReason("Referido De");
                episode.setNotes("Referido De");
                episode.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
                episode.setUuid(UUID.randomUUID().toString());
                createEpisode(episode);

    }
}
