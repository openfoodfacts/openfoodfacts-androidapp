package openfoodfacts.github.scrachx.openfood.jobs

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import javax.inject.Inject

/**
 * @param appContext The application [Context]
 * @param workerParams Parameters to setup the internal state of this worker
 */
@HiltWorker
class ImagesUploaderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : RxWorker(appContext, workerParams) {
    @Inject
    lateinit var client: OpenFoodAPIClient

    override fun createWork() = client
        .uploadOfflineImages()
        .toSingleDefault(Result.success())
        .onErrorReturnItem(Result.failure())
}