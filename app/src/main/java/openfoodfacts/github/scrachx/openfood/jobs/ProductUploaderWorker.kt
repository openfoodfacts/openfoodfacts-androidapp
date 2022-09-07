package openfoodfacts.github.scrachx.openfood.jobs

import android.content.Context
import android.content.SharedPreferences
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
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

        fun scheduleProductUpload(context: Context, preferences: SharedPreferences) {

            val uploadDataWorkRequest = buildUploadRequest(
                false,
                Constraints {
                    setRequiredNetworkType(NetworkType.CONNECTED)
                },
            )

            val uploadIfMobile = preferences.getBoolean(context.getString(R.string.pref_enable_mobile_data_key), true)
            val uploadPicturesWorkRequest = buildUploadRequest(
                true,
                Constraints {
                    setRequiredNetworkType(if (uploadIfMobile) NetworkType.CONNECTED else NetworkType.UNMETERED)
                },
            )

            WorkManager.getInstance(context)
                .beginUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, uploadDataWorkRequest)
                .then(uploadPicturesWorkRequest)
                .enqueue()
        }

        private fun buildUploadRequest(
            includeImages: Boolean,
            constPics: Constraints,
        ): OneTimeWorkRequest {
            return OneTimeWorkRequest<ProductUploaderWorker> {
                setInputData(buildData { putBoolean(KEY_INCLUDE_IMAGES, includeImages) })
                setConstraints(constPics)
            }
        }
    }
}

