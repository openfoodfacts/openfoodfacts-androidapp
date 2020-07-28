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
import androidx.work.rxjava3.RxWorker;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import io.reactivex.rxjava3.core.Single;
import openfoodfacts.github.scrachx.openfood.utils.OfflineProductService;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

public class OfflineProductWorker extends RxWorker {
    private static final String WORK_TAG = "OFFLINE_WORKER_TAG";
    public static final String KEY_INCLUDE_IMAGES = "includeImages";

    public OfflineProductWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private static Data inputData(boolean includeImages) {
        return new Data.Builder()
            .putBoolean(KEY_INCLUDE_IMAGES, includeImages)
            .build();
    }

    public static void scheduleSync() {
        Constraints.Builder constPics = new Constraints.Builder();
        if (PreferenceManager.getDefaultSharedPreferences(OFFApplication.getInstance()).getBoolean("enableMobileDataUpload", true)) {
            constPics.setRequiredNetworkType(NetworkType.CONNECTED);
        } else {
            constPics.setRequiredNetworkType(NetworkType.UNMETERED);
        }

        Constraints constData = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

        OneTimeWorkRequest uploadDataWorkRequest = new OneTimeWorkRequest
            .Builder(OfflineProductWorker.class)
            .setInputData(inputData(false))
            .setConstraints(constData)
            .build();

        OneTimeWorkRequest uploadPicturesWorkRequest = new OneTimeWorkRequest
            .Builder(OfflineProductWorker.class)
            .setInputData(inputData(true))
            .setConstraints(constPics.build())
            .build();

        WorkManager.getInstance(OFFApplication.getInstance())
            .beginUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, uploadDataWorkRequest)
            .then(uploadPicturesWorkRequest)
            .enqueue();
    }

    @NonNull
    @Override
    public Single<Result> createWork() {
        boolean includeImages = getInputData().getBoolean(KEY_INCLUDE_IMAGES, false);
        Log.d(WORK_TAG, "[START] doWork with includeImages: " + includeImages);
        return OfflineProductService.sharedInstance().uploadAll(includeImages)
            .map(shouldRetry -> {
                if (shouldRetry) {
                    Log.d(WORK_TAG, "[RETRY] doWork with includeImages: " + includeImages);
                    return Result.retry();
                } else {
                    Log.d(WORK_TAG, "[SUCCESS] doWork with includeImages: " + includeImages);
                    return Result.success();
                }
            });
    }
}
