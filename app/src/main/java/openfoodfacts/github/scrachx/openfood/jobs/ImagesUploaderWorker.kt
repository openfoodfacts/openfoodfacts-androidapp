package openfoodfacts.github.scrachx.openfood.jobs

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.Constraints
import openfoodfacts.github.scrachx.openfood.utils.OneTimeWorkRequest
import openfoodfacts.github.scrachx.openfood.utils.toWorkResult
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

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

    companion object {
        private var isUploadJobInitialised = false
        private val UPLOAD_JOB_PERIODICITY = 30.minutes
        private const val UPLOAD_JOB_TAG = "upload_saved_product_job"


        @Synchronized
        fun scheduleProductUploadJob(context: Context) {
            if (isUploadJobInitialised) return

            val uploadWorkRequest = OneTimeWorkRequest<ImagesUploaderWorker> {
                setConstraints(Constraints(fun Constraints.Builder.() {
                    setRequiredNetworkType(NetworkType.UNMETERED)
                }))
                setInitialDelay(UPLOAD_JOB_PERIODICITY.toJavaDuration())
            }

            WorkManager.getInstance(context).enqueueUniqueWork(
                UPLOAD_JOB_TAG,
                ExistingWorkPolicy.KEEP,
                uploadWorkRequest
            )

            isUploadJobInitialised = true
        }
    }


}
