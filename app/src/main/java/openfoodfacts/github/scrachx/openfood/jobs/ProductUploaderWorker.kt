package openfoodfacts.github.scrachx.openfood.jobs

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.utils.OfflineProductService
import openfoodfacts.github.scrachx.openfood.utils.buildConstraints
import openfoodfacts.github.scrachx.openfood.utils.buildData
import openfoodfacts.github.scrachx.openfood.utils.buildOneTimeWorkRequest
import javax.inject.Inject

@HiltWorker
class ProductUploaderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var offlineProductService: OfflineProductService

    override suspend fun doWork(): Result {
        val includeImages = inputData.getBoolean(KEY_INCLUDE_IMAGES, false)
        Log.d(WORK_TAG, "[START] (includeImages=$includeImages)")

        val shouldRetry = offlineProductService.uploadAll(includeImages)

        return if (shouldRetry) {
            Log.d(WORK_TAG, "[RETRY]")
            Result.retry()
        } else {
            Log.d(WORK_TAG, "[SUCCESS]")
            Result.success()
        }
    }

    companion object {
        private const val WORK_TAG = "OFFLINE_WORKER_TAG"
        const val KEY_INCLUDE_IMAGES = "includeImages"

        fun scheduleProductUpload(context: Context, pref: SharedPreferences) {

            val constData = buildConstraints {
                setRequiredNetworkType(NetworkType.CONNECTED)
            }
            val uploadDataWorkRequest = buildUploadRequest(constData, false)

            val constPics = buildConstraints {
                val uploadIfMobile = pref.getBoolean(context.getString(R.string.pref_enable_mobile_data_key), true)

                setRequiredNetworkType(if (uploadIfMobile) NetworkType.CONNECTED else NetworkType.UNMETERED)
            }
            val uploadPicturesWorkRequest = buildUploadRequest(constPics, true)

            WorkManager.getInstance(context)
                .beginUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, uploadDataWorkRequest)
                .then(uploadPicturesWorkRequest)
                .enqueue()
        }

        private fun buildUploadRequest(constPics: Constraints, includeImages: Boolean) = buildOneTimeWorkRequest<ProductUploaderWorker> {
            setInputData(buildData { putBoolean(KEY_INCLUDE_IMAGES, includeImages) })
            setConstraints(constPics)
        }

    }
}

