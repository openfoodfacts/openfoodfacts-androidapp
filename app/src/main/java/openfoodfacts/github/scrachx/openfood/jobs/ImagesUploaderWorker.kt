package openfoodfacts.github.scrachx.openfood.jobs

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import javax.inject.Inject

/**
 * @param appContext The application [Context]
 * @param workerParams Parameters to setup the internal state of this worker
 */
@HiltWorker
class ImagesUploaderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    @Inject
    lateinit var productRepository: ProductRepository

    override suspend fun doWork(): Result {
        return productRepository
            .runCatching { uploadOfflineProductsImages() }
            .toWorkResult()
    }

    private fun <T> kotlin.Result<T>.toWorkResult() = fold(
        onSuccess = { Result.success() },
        onFailure = { Result.failure() },
    )
}
