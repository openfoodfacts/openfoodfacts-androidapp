package openfoodfacts.github.scrachx.openfood.jobs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;

public class SavedProductUploadWork extends ListenableWorker {
    /**
     * @param appContext The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public SavedProductUploadWork(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        Context context = getApplicationContext();
        OpenFoodAPIClient apiClient = new OpenFoodAPIClient(context);
        return apiClient.uploadOfflineImages(context);
    }

}
