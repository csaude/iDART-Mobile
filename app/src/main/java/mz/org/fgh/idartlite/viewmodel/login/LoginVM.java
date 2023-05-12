package mz.org.fgh.idartlite.viewmodel.login;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mz.org.fgh.idartlite.BR;
import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.adapter.recyclerview.listable.Listble;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.base.viewModel.BaseViewModel;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.ClinicSector;
import mz.org.fgh.idartlite.model.ClinicSectorType;
import mz.org.fgh.idartlite.rest.service.User.RestUserService;
import mz.org.fgh.idartlite.service.clinic.ClinicSectorService;
import mz.org.fgh.idartlite.service.clinic.ClinicSectorTypeService;
import mz.org.fgh.idartlite.service.clinic.ClinicService;
import mz.org.fgh.idartlite.service.clinic.IClinicSectorService;
import mz.org.fgh.idartlite.service.clinic.IClinicSectorTypeService;
import mz.org.fgh.idartlite.service.clinic.IClinicService;
import mz.org.fgh.idartlite.service.user.IUserService;
import mz.org.fgh.idartlite.service.user.UserService;
import mz.org.fgh.idartlite.util.SimpleValue;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.home.IDartHomeActivity;
import mz.org.fgh.idartlite.view.login.LoginActivity;
import mz.org.fgh.idartlite.view.splash.SecondSplashActivity;

public class LoginVM extends BaseViewModel {
    private RestUserService restUserService;
    private IUserService userService;
    private IClinicService clinicService;

    private IClinicSectorService clinicSectorService;

    //JNM 12.01.2022
    private IClinicSectorTypeService clinicSectorTypeService;

    private Clinic selectedClinic;

    private ClinicSector selectedClinicSector;

    private ClinicSectorType selectedClinicSectorType;

    //JNM 12.01.2022
    private List<ClinicSectorType> clinicSectorTypes = new ArrayList<>();

    public List<ClinicSectorType> getClinicSectorTypeList() {
        return clinicSectorTypes;
    }

    public List<ClinicSector> getAllClinicSectorsByType(ClinicSectorType clinicSectorType) throws SQLException {
        return clinicSectorService.getClinicSectorsByType(clinicSectorType);
    }

    public List<ClinicSectorType> getAllClinicSectorTypes() throws SQLException {
        return clinicSectorTypeService.getAllClinicSectorType();
    }


    private List<Clinic> clinicList;

    private List<ClinicSector> clinicSectorsList = new ArrayList<>();

    private boolean authenticating;

    private boolean remeberMe;

    private boolean sanitaryUnit;

    public LoginVM(@NonNull Application application) {

        super(application);
    }

    @Override
    protected IBaseService initRelatedService() {

        userService = new UserService(getApplication(), getCurrentUser());
        restUserService = new RestUserService(getApplication(), getCurrentUser());
        clinicService = new ClinicService(getApplication(), getCurrentUser());
        clinicSectorService = new ClinicSectorService(getApplication(), getCurrentUser());
        //JNM 12.01.2022
        clinicSectorTypeService = new ClinicSectorTypeService(getApplication(), getCurrentUser());

        return userService;
    }

    @Override
    protected BaseModel initRecord() {
        return null;
    }

    @Override
    protected void initFormData() {
        try {
            this.clinicList = clinicService.getAllClinics();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUserName(String userName) {
        this.currentUser.setUserName(userName);
        notifyPropertyChanged(BR.userName);
    }

    @Bindable
    public String getUserName() {
        return this.currentUser.getUserName();
    }

    @Bindable
    public String getUserPassword() {
        return this.currentUser.getPassword();
    }

    public void setUserPassword(String password) {
        this.currentUser.setPassword(password);
        notifyPropertyChanged(BR.userPassword);
    }

    @Bindable
    public Clinic getClinic() {
        return getCurrentClinic();
    }

    public void setClinic(Clinic c) {
        setCurrentClinic(c);
        notifyPropertyChanged(BR.clinic);
    }

    public void saveLogingUser() throws SQLException {
        this.currentUser.setClinic(clinicService.getAllClinics().get(0));
        userService.saveUser(getRelatedActivity().getCurrentUser());
    }

    public void saveUserClinic() throws SQLException {
        clinicService.saveClinic(getCurrentClinic());
    }

    public void saveUserClinicSector() throws SQLException {
        ClinicSector clinicSectore = (ClinicSector) getSelectedClinicSector();
        clinicSectore.setClinic(clinicService.getAllClinics().get(0));
        clinicSectorService.saveClinicSector(clinicSectore);
        setCurrentClinicSector(clinicSectorService.getClinicSector());
    }

    public void login() {
        getRelatedActivity().changeViewToAuthenticatingMode();

        if ((appHasUsersOnDB() && getCurrentClinic() == null) || (appHasUsersOnDB() && getCurrentClinic().getUuid() == null)) {
            getRelatedActivity().changeViewToNormalMode();
            Utilities.displayAlertDialog(getRelatedActivity(), getRelatedActivity().getString(R.string.pharmacy_required)).show();
            return;
        }

        try {

            String loginErrors = getCurrentUser().isValid(getRelatedActivity());

            if (!Utilities.stringHasValue(loginErrors)) {
                getCurrentUser().setUserName(getUserName().trim());
                if (userService.checkIfUsertableIsEmpty()) {
                    runRestUserAccess();

                } else {
                    if (!userService.login(getCurrentUser())) {
                        if (Utilities.listHasElements(clinicList)) {
                            setCurrentClinic(clinicList.get(0));
                            if (currentClinic.getPharmacyType().isUS()) {
                                setCurrentClinicSector(clinicSectorService.getClinicSector());
                            }

                            getRelatedActivity().changeViewToNormalMode();
                            moveToHome();
                            this.getRelatedActivity().savingSharedPreferences();
                        } else {
                            getRelatedActivity().changeViewToNormalMode();
                            Utilities.displayAlertDialog(getRelatedActivity(), getRelatedActivity().getString(R.string.pharmacy_config_not_found)).show();
                        }
                    } else {
                        if (getCurrentClinic() == null) setCurrentClinic(clinicList.get(0));
                        runRestUserAccess();
                    }
                }
            } else {
                getRelatedActivity().changeViewToNormalMode();

                Utilities.displayAlertDialog(getRelatedActivity(), loginErrors).show();

            }
        } catch (SQLException e) {
            getRelatedActivity().changeViewToNormalMode();
            Utilities.displayAlertDialog(getRelatedActivity(), "Ocorreu um erro ao verificar a configuração de utilizadores, " + e.getLocalizedMessage()).show();
            Log.i("INFO DB", "Erro ao fazer Login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void runRestUserAccess() {
        restUserService.getUserAuthentication(getCurrentClinic().getUuid(), getRelatedActivity().getCurrentUser().getUserName(), getRelatedActivity().getCurrentUser().getPassword(), getRelatedActivity());
    }

    public boolean appHasUsersOnDB() {
        boolean usersOnDB = false;
        try {
            usersOnDB = userService.checkIfUsertableIsEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
            Utilities.displayAlertDialog(getRelatedActivity(), getRelatedActivity().getString(R.string.error_verifying)).show();
        }
        return usersOnDB;
    }

    @Override
    public LoginActivity getRelatedActivity() {
        return (LoginActivity) super.getRelatedActivity();
    }

    @Override
    public void preInit() {

    }

    public void saveUserSettingsAndProcced() throws SQLException {
        saveUserClinic();
        saveLogingUser();
        if (isSanitaryUnit()) {
            saveUserClinicSector();
        }
        moveToSecondSplsh();
    }

    public void moveToSecondSplsh() {
        Map<String, Object> params = new HashMap<>();
        params.put("user", getCurrentUser());
        if (getCurrentClinicSector() != null) params.put("clinicSector", getCurrentClinicSector());
        params.put("clinic", getCurrentClinic());
        getRelatedActivity().nextActivity(SecondSplashActivity.class, params);
    }

    private void moveToHome() {
        Map<String, Object> params = new HashMap<>();
        params.put("user", getCurrentUser());
        if (getCurrentClinicSector() != null) {
            params.put("clinicSector", getCurrentClinicSector());
        }
        params.put("clinic", getCurrentClinic());
        getRelatedActivity().nextActivity(IDartHomeActivity.class, params);
    }

    @Bindable
    public Listble getSelectedClinic() {
        return selectedClinic;
    }

    public void setSelectedClinic(Listble selectedClinic) {
        this.selectedClinic = (Clinic) selectedClinic;
        setCurrentClinic(this.selectedClinic);

        // clinicSectorsList.clear();
        // getClinicSectorsList().add(new ClinicSector());
        if (((Clinic) selectedClinic).getClinicName() != null && verifySanitaryUnit()) {

            // Fazer o bind de todas clinicSectorType aqui (Trata-se de uma US)
            List<ClinicSectorType> clinicSectorTypes = new ArrayList<>();
            clinicSectorTypes.clear();
            try {
                for (ClinicSectorType cst : getAllClinicSectorTypes()) {
                    System.out.println("MABJ: "+cst);
                    clinicSectorTypes.add(cst);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            getClinicSectorTypeList().add(new ClinicSectorType());
            getClinicSectorTypeList().clear();
            getClinicSectorTypeList().addAll(clinicSectorTypes);
            getRelatedActivity().loadClinicSectorTypeAdapter();

            // getClinicSectorsList().addAll(((Clinic) selectedClinic).getClinicSectorList());
            // getRelatedActivity().loadClinicSectorAdapter();

        }
        notifyPropertyChanged(BR.selectedClinic);
        // notifyPropertyChanged(BR.selectedClinicSector);
        notifyPropertyChanged(BR.sanitaryUnit);

        notifyPropertyChanged(BR.selectedClinicSectorType);

    }


    @Bindable
    public Listble getSelectedClinicSectorType() {
        return selectedClinicSectorType;
    }


    public void setSelectedClinicSectorType(Listble selectedClinicSectorType) {

        this.selectedClinicSectorType = (ClinicSectorType) selectedClinicSectorType;
        setCurrentClinicSectorType(this.selectedClinicSectorType);

        clinicSectorsList.clear();
        // getClinicSectorsList().add(new ClinicSector());

        List<ClinicSector> clinicSectorList = new ArrayList<>();

        Clinic currClinic = (Clinic) this.currentClinic;

        clinicSectorList.clear();

        if (Utilities.listHasElements(currClinic.getClinicSectorList())) {
            int i = 0;
            for (ClinicSector clinicSector : currClinic.getClinicSectorList()) {
                if (clinicSector.getClinicSectorType().equals(this.currentClinicSectorType)) {
                    clinicSector.setClinicSectorType(this.selectedClinicSectorType);
                    clinicSectorList.add(clinicSector);
                    i++;
                }
            }
        }

        getClinicSectorsList().clear();
        getClinicSectorsList().add(new ClinicSector());
        getClinicSectorsList().addAll(clinicSectorList);
        getRelatedActivity().loadClinicSectorAdapter();

        //JNM 17.01.2022
        notifyPropertyChanged(BR.selectedClinicSectorType);
        notifyPropertyChanged(BR.selectedClinicSector);
        notifyPropertyChanged(BR.selectedClinic);
        notifyPropertyChanged(BR.clinicSectorType);

    }

    @Bindable
    public Listble getSelectedClinicSector() {
        return selectedClinicSector;
    }

    public void setSelectedClinicSector(Listble selectedClinicSector) {
        this.selectedClinicSector = (ClinicSector) selectedClinicSector;
        notifyPropertyChanged(BR.selectedClinicSector);
    }

    @Bindable
    public List<ClinicSector> getClinicSectorsList() {
        return clinicSectorsList;
    }

    public void setClinicSectorsList(List<ClinicSector> clinicSectorsList) {
        this.clinicSectorsList = clinicSectorsList;
        notifyPropertyChanged(BR.clinicSectorsList);
    }

    @Bindable
    public boolean isAuthenticating() {
        return authenticating;
    }

    public void setAuthenticating(boolean authenticating) {
        this.authenticating = authenticating;
        notifyChange();
    }

    @Bindable
    public boolean isRemeberMe() {
        return remeberMe;
    }

    public void setRemeberMe(boolean remeberMe) {
        this.remeberMe = remeberMe;
        notifyPropertyChanged(BR.remeberMe);
    }

    public void changeRemeberMeStatus() {
        setRemeberMe(!isRemeberMe());
    }

    @Bindable
    public boolean isSanitaryUnit() {
        return sanitaryUnit;
    }

    public void setSanitaryUnit(boolean sanitaryUnit) {
        this.sanitaryUnit = sanitaryUnit;
        notifyPropertyChanged(BR.sanitaryUnit);
    }

    public boolean verifySanitaryUnit() {

        Clinic clinic = (Clinic) getSelectedClinic();
        if (clinic != null) {
            if (clinic.getPharmacyType().getDescription().contains("Unidade Sanit"))
                return sanitaryUnit = true;
        }

        return sanitaryUnit = false;

    }
}