package openfoodfacts.github.scrachx.openfood.jobs

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Single
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient

class SavedProductUploadWorker
/**
 * @param appContext The application [Context]
 * @param workerParams Parameters to setup the internal state of this worker
 */
(appContext: Context, workerParams: WorkerParameters) : RxWorker(appContext, workerParams) {
    override fun createWork(): Single<Result> {
        return OpenFoodAPIClient(applicationContext)
                .uploadOfflineImages()
                .toSingleDefault(Result.success())
                .onErrorReturnItem(Result.failure())
    }
}