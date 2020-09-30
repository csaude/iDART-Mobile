package mz.org.fgh.idartlite.TaskSchedule.restTaskSchedule;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import mz.org.fgh.idartlite.TaskSchedule.workScheduler.RestGetConfigWorkerScheduler;
import mz.org.fgh.idartlite.TaskSchedule.workScheduler.RestGetPatientDataWorkerScheduler;

public class ExecuteGetWorkerScheduler {

    private static final String TAG = "ExecuteWorkerScheduler";
    public static final int JOB_ID = 1000;
    private Context context;

    public ExecuteGetWorkerScheduler(Context context) {

        this.context = context;
    }

    public void initConfigTaskWork() {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresCharging(true)
                .build();

        PeriodicWorkRequest periodicConfigDataWorkRequest = new PeriodicWorkRequest.Builder(RestGetConfigWorkerScheduler.class, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag("TASK ID " + JOB_ID)
                .build();

       WorkManager.getInstance(context).enqueue(periodicConfigDataWorkRequest);
    }

    public void initDataTaskWork() {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresCharging(true)
                .build();

        PeriodicWorkRequest periodicPatientDataWorkRequest = new PeriodicWorkRequest.Builder(RestGetPatientDataWorkerScheduler.class, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag("TASK ID " + JOB_ID)
                .build();

        WorkManager.getInstance(context).enqueue(periodicPatientDataWorkRequest);
    }

}