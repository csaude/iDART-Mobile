package mz.org.fgh.idartlite.viewmodel.splash;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

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
import mz.org.fgh.idartlite.service.stock.IStockAlertService;
import mz.org.fgh.idartlite.service.stock.StockAlertService;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.login.LoginActivity;
import mz.org.fgh.idartlite.view.splash.SplashActivity;

public class SplashVM extends BaseViewModel implements RestResponseListener<Clinic> {


    private List<AppSettings> appSettings;
    private IStockAlertService stockAlertService;
    private WifiManager wifiManager;

    //private WorkerScheduleExecutor workerScheduleExecutor;

    public SplashVM(@NonNull Application application) {
        super(application);
    }

    @Override
    protected IBaseService initRelatedService() {
        stockAlertService = new StockAlertService(getApplication());
        return new SplashService(getApplication());
    }

    @Override
    protected BaseModel initRecord() {
        return null;
    }

    @Override
    protected void initFormData() {

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
        wifiManager = (WifiManager) getRelatedActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getRelatedActivity().getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();
        }
        /*
        try {
            this.appSettings = settingsService.getAll();
            scheduleSyncWorks();
            //getFirstData();
        } catch (SQLException e) {
            e.printStackTrace();
        }

         */
    }

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
    }

    private void scheduleSyncWorks() {
        this.initWorkScheduleExecutor(getRelatedActivity().getApplicationContext(), null, this.appSettings);
        workerScheduleExecutor.initConfigTaskWork();
        workerScheduleExecutor.initStockTaskWork();
        workerScheduleExecutor.initDataTaskWork();
        workerScheduleExecutor.initPatchStockDataTaskWork();
        workerScheduleExecutor.initPostPatientDataTaskWork();
        workerScheduleExecutor.initPostStockDataTaskWork();
        workerScheduleExecutor.initPatientDispenseTaskWork();
        workerScheduleExecutor.initEpisodeTaskWork();
        workerScheduleExecutor.initPostNewPatientDataTaskWork();
        workerScheduleExecutor.initStockAlertTaskWork();
        workerScheduleExecutor.initPatientUSDispenseTaskWork();
        workerScheduleExecutor.initClinicInformationTaskWork();
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

        List<AppSettings> settings = new ArrayList<>();

        settings.add(AppSettings.generateUrlSetting(s));
        settings.add(AppSettings.generateDataSyncSetting(AppSettings.DEFAULT_DATA_SYNC_PERIOD_SETTING));
        settings.add(AppSettings.generateMetadataSyncSetting(AppSettings.DEFAULT_METADATA_SYNC_PERIOD_SETTING));
        settings.add(AppSettings.generateDataRemotionSetting(AppSettings.DEFAULT_DATA_REMOTION_PERIOD));

        try {
            getRelatedService().saveAppSettings(settings);

            BaseRestService.setBaseUrl(getRelatedService().getCentralServerSettings().getValue());

            this.appSettings = settingsService.getAll();
            scheduleSyncWorks();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        syncApp();
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

        this.initWorkScheduleExecutor(getApplication(), null, this.appSettings);
        //this.workerScheduleExecutor.getWorkManager().cancelAllWorkByTag("ONE_TIME_PATIENT_ID" + WorkerScheduleExecutor.ONE_TIME_REQUEST_JOB_ID);
        //this.workerScheduleExecutor.getWorkManager().cancelAllWorkByTag("ONE_TIME_STOCK_ID" + WorkerScheduleExecutor.ONE_TIME_REQUEST_JOB_ID);
        //this.workerScheduleExecutor.getWorkManager().cancelAllWorkByTag("ONE_TIME_STOCK_ALERT_ID" + WorkerScheduleExecutor.ONE_TIME_REQUEST_JOB_ID);
        try {
            this.systemSettings = getRelatedService().getAllSettings();
            if (!Utilities.listHasElements(stockAlertService.getAll())) {
                //this.workerScheduleExecutor.runOneStockAlert();
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
    public void doOnRestSucessResponseObjects(String flag, List<Clinic> objects) {
        Map<String, Object> params = new HashMap<>();
        params.put("clinicList", objects);
        getRelatedActivity().nextActivityFinishingCurrent(LoginActivity.class, params);
    }

    @Override
    public void doOnConfirmed() {
        super.doOnConfirmed();

        if (appHasNoUsersOnDB()){
            exitApp();
        }else {
            getRelatedActivity().nextActivityFinishingCurrent(LoginActivity.class);
        }
    }

    public void exitApp() {
        getRelatedActivity().finishAffinity();
        System.exit(0);
    }
}
