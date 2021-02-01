package mz.org.fgh.idartlite.viewmodel.splash;

import android.app.Application;

import androidx.annotation.NonNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.rest.BaseRestService;
import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.base.viewModel.BaseViewModel;
import mz.org.fgh.idartlite.listener.rest.RestResponseListener;
import mz.org.fgh.idartlite.model.AppSettings;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.DiseaseType;
import mz.org.fgh.idartlite.model.Drug;
import mz.org.fgh.idartlite.model.PharmacyType;
import mz.org.fgh.idartlite.rest.helper.RESTServiceHandler;
import mz.org.fgh.idartlite.service.splash.ISplashService;
import mz.org.fgh.idartlite.service.splash.SplashService;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.login.LoginActivity;
import mz.org.fgh.idartlite.view.splash.SplashActivity;

public class SplashVM extends BaseViewModel implements RestResponseListener<Clinic> {

    private List<Clinic> clinicList;


    public SplashVM(@NonNull Application application) {
        super(application);
    }

    @Override
    protected IBaseService initRelatedService() {
        return new SplashService(getApplication());
    }

    @Override
    protected BaseModel initRecord() {
        return null;
    }

    @Override
    protected void initFormData() {
        clinicList = new ArrayList<Clinic>();

        try {
            this.systemSettings = getRelatedService().getAllSettings();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SplashActivity getRelatedActivity() {
        return (SplashActivity) super.getRelatedActivity();
    }

    @Override
    public ISplashService getRelatedService() {
        return (ISplashService) super.getRelatedService();
    }

    @Override
    public void preInit() {



    }

    public void requestConfiguration() {
        getRelatedActivity().requestCentralServerSettings();
    }

    public boolean appHasNoUsersOnDB(){
        return getRelatedService().appHasNoUsersOnDB();
    }
    public List<PharmacyType> getAllPharmacyTypes(){
        return getRelatedService().getAllPharmacyTypes();
    }

    public List<DiseaseType> getAllDiseaseTypes() throws SQLException {
        return getRelatedService().getAllDiseaseTypes();
    }

    public List<Drug> getAllDrugs() throws SQLException {
        return getRelatedService().getAllDrugs();
    }

    public void saveSettings(String s) {

        if (RESTServiceHandler.getServerStatus(s)) {

            List<AppSettings> settings = new ArrayList<>();

            settings.add(AppSettings.generateUrlSetting(s));
            settings.add(AppSettings.generateDataSyncSetting(AppSettings.DEFAULT_DATA_SYNC_PERIOD_SETTING));
            settings.add(AppSettings.generateMetadataSyncSetting(AppSettings.DEFAULT_METADATA_SYNC_PERIOD_SETTING));
            settings.add(AppSettings.generateDataRemotionSetting(AppSettings.DEFAULT_DATA_REMOTION_PERIOD));

            try {
                getRelatedService().saveAppSettings(settings);

                BaseRestService.setBaseUrl(getRelatedService().getCentralServerSettings().getValue());

            } catch (SQLException e) {
                e.printStackTrace();
            }


            syncApp();

        }else {
            Utilities.displayAlertDialog(getRelatedActivity(), "A aplicação não conseguiu ligar-se ao servidor central, por favor verifique a configuração informada ou a configuração de rede do dispositivo.").show();
        }
    }


    public void syncApp(){
        if (RESTServiceHandler.getServerStatus(BaseRestService.baseUrl)) {
            getRelatedService().syncApp(SplashVM.this);
        }else {
            String errorMsg = "";

            if (getRelatedService().appHasNoUsersOnDB()){
                errorMsg = getRelatedActivity().getString(R.string.error_msg_server_offline);
            }else {
                errorMsg = getRelatedActivity().getString(R.string.error_msg_server_offline_records_wont_be_sync);
            }
            Utilities.displayAlertDialog(getRelatedActivity(), errorMsg, SplashVM.this).show();
        }
    }

    @Override
    public void doOnRestSucessResponse(String flag) {
        if (Utilities.stringHasValue(flag)){
            long timeOut = 60000;
            long requestTime = 0;
            while (!Utilities.listHasElements(getAllPharmacyTypes())){
                try {
                    Thread.sleep(2000);
                    requestTime += 2000;
                    if (requestTime >= timeOut){
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void doOnRestErrorResponse(String errormsg) {
        getRelatedActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utilities.displayAlertDialog(getRelatedActivity(), errormsg).show();
            }
        });
    }

    @Override
    public void doOnRestSucessResponseObject(String flag, Clinic object) {

    }

    @Override
    public void doOnRestSucessResponseObjects(String flag, List<Clinic> objects) {
        Map<String, Object> params = new HashMap<>();
        params.put("clinicList", objects);
        getRelatedActivity().nextActivityFinishingCurrent(LoginActivity.class, params);
    }

    @Override
    public void doOnConfirmed() {
        super.doOnConfirmed();

        if (appHasNoUsersOnDB()){
            getRelatedActivity().finishAffinity();
            System.exit(0);
        }else {
            getRelatedActivity().nextActivityFinishingCurrent(LoginActivity.class);
        }
    }
}
