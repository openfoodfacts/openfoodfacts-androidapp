package openfoodfacts.github.scrachx.openfood.jobs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.rxjava3.RxWorker;
import androidx.work.WorkerParameters;

import io.reactivex.rxjava3.core.Single;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;

import static androidx.work.ListenableWorker.Result.failure;
import static androidx.work.ListenableWorker.Result.success;

public class SavedProductUploadWorker extends RxWorker {
    /**
     * @param appContext The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public SavedProductUploadWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Single<Result> createWork() {
        return new OpenFoodAPIClient(getApplicationContext())
            .uploadOfflineImages(getApplicationContext())
            .toSingleDefault(success())
            .onErrorReturnItem(failure());
    }
}
