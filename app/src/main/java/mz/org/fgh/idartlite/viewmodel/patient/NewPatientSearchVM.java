package mz.org.fgh.idartlite.viewmodel.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;

import java.sql.SQLException;
import java.util.List;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.base.viewModel.SearchVM;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.Episode;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.rest.service.Patient.RestPatientService;
import mz.org.fgh.idartlite.searchparams.AbstractSearchParams;
import mz.org.fgh.idartlite.searchparams.PatientSearchParams;
import mz.org.fgh.idartlite.service.episode.EpisodeService;
import mz.org.fgh.idartlite.service.episode.IEpisodeService;
import mz.org.fgh.idartlite.service.patient.IPatientService;
import mz.org.fgh.idartlite.service.patient.PatientService;
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.patientSearch.NewPatientSearchActivity;

public class NewPatientSearchVM extends SearchVM<Patient> {

    private Patient patient;
    private IPatientService patientService;
    private IEpisodeService episodeService;

    public NewPatientSearchVM(@NonNull Application application) {
        super(application);

        patientService = (PatientService) getServiceProvider().get(PatientService.class);
        episodeService = new EpisodeService(application,getCurrentUser());

    }

    @Override
    protected IBaseService initRelatedService() {
        return null;
    }

    @Override
    protected BaseModel initRecord() {
        return null;
    }

    @Override
    protected void initFormData() {

    }

    public List<Patient> searchPatient(String nid,String name,String surname, long offset, long limit) throws SQLException {
        return patientService.searchPatientByNidOrNameOrSurname( nid, name, surname, offset, limit,this.getRelatedActivity());
    }

    public void createPatientAndEpisodeIfNotExists(Patient patient) throws SQLException {
   //    Patient localPatient= patientService.checkExistsPatientWithNID(patient.getNid());
       if(!checkIfPatientExists(patient)) {
           patientService.savePatient(patient);
           Episode episode= this.initEpisode(patient);
           this.createEpisode(episode);
       }
    }

    public boolean checkIfPatientExists(Patient patient) throws SQLException {
        Patient localPatient= patientService.checkExistsPatientWithNID(patient.getNid());
        if(localPatient==null) {
            return false;
        }else {
            return true;
        }
    }


    public Episode initEpisode(Patient patient){

        Episode episode=new Episode();
        episode.setPatient(patient);
        episode.setStartReason("Referido De");
        episode.setUsUuid(getCurrentClinic().getUuid());
        episode.setSanitaryUnit(getCurrentClinic().getDescription());
        episode.setUuid(Utilities.getNewUUID().toString());
        episode.setSyncStatus("R");
        episode.setEpisodeDate(DateUtilities.getCurrentDate());
      return  episode;
    }


    public void createEpisode(Episode episode) throws SQLException {
        episodeService.createEpisode(episode);
    }

    public void loadPatientEpisodes() throws SQLException {
        this.patient.setEpisodes(episodeService.getAllEpisodesByPatient(this.patient));
        
        if (!Utilities.listHasElements(this.patient.getEpisodes())){
            throw new RuntimeException(getRelatedActivity().getString(R.string.no_episode_found));
        }
    }

    @Override
    public void initSearch(){
        if(!Utilities.stringHasValue(getSearchParams().getNid()) && !Utilities.stringHasValue(getSearchParams().getName()) && !Utilities.stringHasValue(getSearchParams().getSurName())) {
            Utilities.displayAlertDialog(getRelatedActivity(),getRelatedActivity().getString(R.string.nid_or_name_is_mandatory)).show();
        }else {
            super.initSearch();
        }
    }

    @Override
    public void doOnlineSearch(long offset, long limit) throws SQLException {
        //Utilities.displayAlertDialog(getRelatedActivity(),getRelatedActivity().getString(R.string.no_search_results)).show();

       // if(getRelatedActivity().)
      //  Utilities.displayConfirmationDialog(getRelatedActivity(), getRelatedActivity().getString(R.string.would_like_create_patient), getRelatedActivity().getString(R.string.yes), getRelatedActivity().getString(R.string.no), ((NewPatientSearchActivity)getRelatedActivity())).show();

      RestPatientService.restGetPatientByNidOrNameOrSurname(getSearchParams().getNid(), getSearchParams().getName(), getSearchParams().getSurName(), offset, limit,this.getRelatedActivity());


    //    displaySearchResults();
    }

    @Override
    protected void doOnNoRecordFound() {

    }

    @Override
    public void displaySearchResults() {
        Utilities.hideSoftKeyboard(getRelatedActivity());

        getRelatedActivity().displaySearchResult();
    }

    @Override
    public PatientSearchParams getSearchParams() {
        return (PatientSearchParams) super.getSearchParams();
    }

    @Override
    public AbstractSearchParams<Patient> initSearchParams() {
        return new PatientSearchParams();
    }

    @Override
    public List<Patient> doSearch(long offset, long limit) throws SQLException {

        return searchPatient(getSearchParams().getNid()!=null? getSearchParams().getNid().trim():null ,getSearchParams().getName()!=null? getSearchParams().getName().trim():null,getSearchParams().getSurName() !=null? getSearchParams().getSurName().trim():null, offset, limit);
    }

    public List<Patient> getAllPatient() throws SQLException {
        return patientService.getALLPatient();
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @Override
    public NewPatientSearchActivity getRelatedActivity(){
        return (NewPatientSearchActivity) super.getRelatedActivity();
    }

    @Override
    public void preInit() {

    }


    @Bindable
    public Clinic getClinic(){
        return getCurrentClinic();
    }

    @Bindable
    public String getSearchNid() {
        return getSearchParams().getNid();
    }

    public void setSearchNid(String searchNid) {
        getSearchParams().setNid(searchNid);
    }

    @Bindable
    public String getSearchName() {
        return getSearchParams().getName();
    }

    public void setSearchName(String searchName) {
        getSearchParams().setName(searchName);
    }

    @Bindable
    public String getSearchSurname() {
        return getSearchParams().getSurName();
    }

    public void setSearchSurname(String searchSurname) {
        getSearchParams().setSurName(searchSurname);
    }
}
