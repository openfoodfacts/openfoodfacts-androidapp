package openfoodfacts.github.scrachx.openfood.jobs;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import openfoodfacts.github.scrachx.openfood.utils.OfflineProductService;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

public class OfflineProductWorker extends Worker {
    private static final String WORK_TAG = "OFFLINE_WORKER_TAG";

    public OfflineProductWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        boolean includeImages = getInputData().getBoolean("includeImages", false);
        Log.d(WORK_TAG, "[START] doWork with includeImages: " + includeImages);
        boolean shouldRetry = OfflineProductService.sharedInstance().uploadAll(includeImages);
        if (shouldRetry) {
            Log.d(WORK_TAG, "[RETRY] doWork with includeImages: " + includeImages);
            return Result.retry();
        }
        Log.d(WORK_TAG, "[SUCCESS] doWork with includeImages: " + includeImages);
        return Result.success();
    }

    private static Data inputData(boolean includeImages) {
        return new Data.Builder()
            .putBoolean("includeImages", includeImages)
            .build();
    }

    public static void addWork() {
        OneTimeWorkRequest uploadDataWorkRequest = new OneTimeWorkRequest
            .Builder(OfflineProductWorker.class)
            .setInputData(inputData(false))
            .setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .build();

        Constraints.Builder constraints = new Constraints.Builder();
        if (PreferenceManager.getDefaultSharedPreferences(OFFApplication.getInstance()).getBoolean("enableMobileDataUpload", true)) {
            constraints.setRequiredNetworkType(NetworkType.CONNECTED);
        } else {
            constraints.setRequiredNetworkType(NetworkType.UNMETERED);
        }

        OneTimeWorkRequest uploadPicturesWorkRequest = new OneTimeWorkRequest
            .Builder(OfflineProductWorker.class)
            .setInputData(inputData(true))
            .setConstraints(constraints.build())
            .build();

        WorkManager.getInstance(OFFApplication.getInstance())
            .beginUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, uploadDataWorkRequest)
            .then(uploadPicturesWorkRequest)
            .enqueue();
    }
}
