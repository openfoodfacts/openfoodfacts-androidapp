package openfoodfacts.github.scrachx.openfood.jobs

import android.content.Context
import android.content.SharedPreferences
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import logcat.LogPriority
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.repositories.OfflineProductRepository
import openfoodfacts.github.scrachx.openfood.utils.Constraints
import openfoodfacts.github.scrachx.openfood.utils.OneTimeWorkRequest
import openfoodfacts.github.scrachx.openfood.utils.buildData
import javax.inject.Inject

@HiltWorker
class ProductUploaderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var offlineProductRepository: OfflineProductRepository

    override suspend fun doWork(): Result {
        val includeImages = inputData.getBoolean(KEY_INCLUDE_IMAGES, false)
        logcat(WORK_TAG, LogPriority.DEBUG) { "[START] (includeImages=$includeImages)" }

        val shouldRetry = offlineProductRepository.uploadAll(includeImages)

        return if (shouldRetry) {
            logcat(WORK_TAG, LogPriority.DEBUG) { "[RETRY]" }
            Result.retry()
        } else {
            logcat(WORK_TAG, LogPriority.DEBUG) { "[SUCCESS]" }
            Result.success()
        }
    }

    companion object {
        private const val WORK_TAG = "OFFLINE_WORKER_TAG"
        const val KEY_INCLUDE_IMAGES = "includeImages"

        fun scheduleProductUpload(context: Context, pref: SharedPreferences) {

            val uploadDataWorkRequest = buildUploadRequest(
                Constraints {
                    setRequiredNetworkType(NetworkType.CONNECTED)
                },
                false,
            )

            val uploadIfMobile = pref.getBoolean(context.getString(R.string.pref_enable_mobile_data_key), true)
            val uploadPicturesWorkRequest = buildUploadRequest(
                Constraints {
                    setRequiredNetworkType(if (uploadIfMobile) NetworkType.CONNECTED else NetworkType.UNMETERED)
                },
                true,
            )

            WorkManager.getInstance(context)
                .beginUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, uploadDataWorkRequest)
                .then(uploadPicturesWorkRequest)
                .enqueue()
        }

        private fun buildUploadRequest(constPics: Constraints, includeImages: Boolean): OneTimeWorkRequest {
            return OneTimeWorkRequest<ProductUploaderWorker> {
                setInputData(buildData { putBoolean(KEY_INCLUDE_IMAGES, includeImages) })
                setConstraints(constPics)
            }
        }
    }
}

