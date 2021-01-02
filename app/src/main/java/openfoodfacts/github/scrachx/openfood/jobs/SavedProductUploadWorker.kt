package openfoodfacts.github.scrachx.openfood.jobs

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient

/**
 * @param appContext The application [Context]
 * @param workerParams Parameters to setup the internal state of this worker
 */
class SavedProductUploadWorker(appContext: Context, workerParams: WorkerParameters) : RxWorker(appContext, workerParams) {
    override fun createWork() = OpenFoodAPIClient(applicationContext)
            .uploadOfflineImages()
            .toSingleDefault(Result.success())
            .onErrorReturnItem(Result.failure())
}