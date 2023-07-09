package mz.org.fgh.idartlite.view.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.adapter.spinner.listable.ListableSpinnerAdapter;
import mz.org.fgh.idartlite.base.activity.BaseActivity;
import mz.org.fgh.idartlite.base.viewModel.BaseViewModel;
import mz.org.fgh.idartlite.databinding.ActivityLoginBinding;
import mz.org.fgh.idartlite.listener.dialog.IDialogListener;
import mz.org.fgh.idartlite.listener.rest.RestResponseListener;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.ClinicSectorType;
import mz.org.fgh.idartlite.model.Province;
import mz.org.fgh.idartlite.rest.service.User.RestUserService;
import mz.org.fgh.idartlite.util.SecurePreferences;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.patientPanel.AddNewPatientActivity;
import mz.org.fgh.idartlite.viewmodel.login.LoginVM;

public class LoginActivity extends BaseActivity implements RestResponseListener<Clinic> {
    //Data binding variable
    private ActivityLoginBinding activityLoginBinding;

    // Vars for bindings
    private ListableSpinnerAdapter clinicSectorAdapter;
    private ListableSpinnerAdapter clinicSectorTypeAdapter;

    private List<Clinic> clinicList;
    private static final String LOG_USR_NAME = "log_name";

    private static final String LOG_USR_PASS = "log_pass";

    private static final String LOG_SHARED_FILE_NAME = "log_cred";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        activityLoginBinding.setViewModel(getRelatedViewModel());
        activityLoginBinding.executePendingBindings();

        changeViewToNormalMode();

        clinicList = new ArrayList<Clinic>();
        Intent intent = this.getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                clinicList.add(0, new Clinic());
                clinicList.addAll((List<Clinic>) bundle.getSerializable("clinicList"));
            }
        }

        if (Utilities.listHasElements(clinicList)) {
            loadClinicAdapters();
        }

        getSharedPreferencesData();
        Utilities.checkPermissionsToViewPdf(this);
        populateSctorTypes();
        Utilities.displayImportantMessage(this,"Ao acessar este sistema," +
                " você está prestes a visualizar informações altamente confidenciais de utentes." +
                " É sua responsabilidade protegê-las adequadamente e usá-las somente para os fins autorizados. A privacidade dos utentes é essencial para nossa missão.","Aviso de Confidencialidade").show();
    }

    public void loadClinicAdapters() {
        ListableSpinnerAdapter adapter = new ListableSpinnerAdapter(this, R.layout.simple_auto_complete_item, clinicList);
        activityLoginBinding.setClinicAdapter(adapter);
    }

    // Popular as clinicSectorTypes
    private void populateSctorTypes() {
        try {
            List<ClinicSectorType> clinicSectorTypeArrayList = new ArrayList<>();
            clinicSectorTypeArrayList.addAll(getRelatedViewModel().getAllClinicSectorTypes());

            clinicSectorTypeAdapter = new ListableSpinnerAdapter(this, R.layout.simple_auto_complete_item, clinicSectorTypeArrayList);
            activityLoginBinding.clinicSectorType.setAdapter(clinicSectorTypeAdapter);
            activityLoginBinding.setClinicSectorAdapter(clinicSectorAdapter);
        } catch (Exception e) {
            Utilities.displayAlertDialog(LoginActivity.this, getString(R.string.error_loading_form_data) + e.getMessage());
            e.printStackTrace();
        }
    }

    public void savingSharedPreferences() {
        if (getRelatedViewModel().isRemeberMe()) {

            SecurePreferences preferences = new SecurePreferences(getApplicationContext(), LOG_SHARED_FILE_NAME, true);

            preferences.put(LOG_USR_NAME, getCurrentUser().getUserName());
            preferences.put(LOG_USR_PASS, getCurrentUser().getPassword());
        }
    }

    private void getSharedPreferencesData() {
        SecurePreferences sp = new SecurePreferences(getApplicationContext(), LOG_SHARED_FILE_NAME, true);

        if (sp.containsKey(LOG_USR_NAME)) {
            String user = sp.getString(LOG_USR_NAME);
            this.getRelatedViewModel().setUserName(user);
        }
        if (sp.containsKey(LOG_USR_PASS)) {
            String pass = sp.getString(LOG_USR_PASS);
            this.getRelatedViewModel().setUserPassword(pass);
        }
    }

    public void loadClinicSectorAdapter() {

        clinicSectorAdapter = new ListableSpinnerAdapter(this, R.layout.simple_auto_complete_item, getRelatedViewModel().getClinicSectorsList());
        activityLoginBinding.setClinicSectorAdapter(clinicSectorAdapter);
    }

    public void loadClinicSectorTypeAdapter() {

        clinicSectorTypeAdapter = new ListableSpinnerAdapter(this, R.layout.simple_auto_complete_item, getRelatedViewModel().getClinicSectorTypeList());
        activityLoginBinding.setClinicSectorTypeAdapter(clinicSectorTypeAdapter);
    }

    public void changeViewToAuthenticatingMode() {
        getRelatedViewModel().setAuthenticating(true);
    }

    public void changeViewToNormalMode() {
        getRelatedViewModel().setAuthenticating(false);
    }

    @Override
    public LoginVM getRelatedViewModel() {
        return (LoginVM) super.getRelatedViewModel();
    }

    @Override
    public BaseViewModel initViewModel() {
        return new ViewModelProvider(this).get(LoginVM.class);
    }


    @Override
    public void doOnRestSucessResponse(String flag) {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        changeViewToNormalMode();

        if (Utilities.stringHasValue(flag)) {
            if (flag.equals(RestUserService.auth)) {
                try {
                    getRelatedViewModel().saveUserSettingsAndProcced();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Utilities.displayAlertDialog(LoginActivity.this, "Ocorreu um erro ao guardar as configurações do utilizador.").show();
                }
            } else {
                Utilities.displayAlertDialog(LoginActivity.this, "Os dados do utilizador são inválidos").show();
            }
        }
    }

    public ActivityLoginBinding getActivityLoginBinding() {
        return activityLoginBinding;
    }

    @Override
    public void doOnRestErrorResponse(String errormsg) {
    }

    @Override
    public void doOnRestSucessResponseObjects(String flag, List<Clinic> objects) {

    }

}