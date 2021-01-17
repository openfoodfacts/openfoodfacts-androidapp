package openfoodfacts.github.scrachx.openfood.jobs

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.*
import io.reactivex.Single
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.utils.OfflineProductService

class OfflineProductWorker(context: Context, workerParams: WorkerParameters) : RxWorker(context, workerParams) {
    override fun createWork(): Single<Result> {
        val includeImages = inputData.getBoolean(KEY_INCLUDE_IMAGES, false)
        Log.d(WORK_TAG, "[START] doWork with includeImages: $includeImages")
        return OfflineProductService.uploadAll(includeImages).map { shouldRetry ->
            if (shouldRetry) {
                Log.d(WORK_TAG, "[RETRY] doWork with includeImages: $includeImages")
                Result.retry()
            } else {
                Log.d(WORK_TAG, "[SUCCESS] doWork with includeImages: $includeImages")
                Result.success()
            }
        }
    }

    companion object {
        private const val WORK_TAG = "OFFLINE_WORKER_TAG"
        const val KEY_INCLUDE_IMAGES = "includeImages"
        private fun inputData(includeImages: Boolean) = Data.Builder()
                .putBoolean(KEY_INCLUDE_IMAGES, includeImages)
                .build()

        @JvmStatic
        fun scheduleSync() {
            val constPics = Constraints.Builder()
            val context = OFFApplication.instance
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_enable_mobile_data_key), true)) {
                constPics.setRequiredNetworkType(NetworkType.CONNECTED)
            } else {
                constPics.setRequiredNetworkType(NetworkType.UNMETERED)
            }
            val constData = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            val uploadDataWorkRequest = OneTimeWorkRequest.Builder(OfflineProductWorker::class.java)
                    .setInputData(inputData(false))
                    .setConstraints(constData)
                    .build()
            val uploadPicturesWorkRequest = OneTimeWorkRequest.Builder(OfflineProductWorker::class.java)
                    .setInputData(inputData(true))
                    .setConstraints(constPics.build())
                    .build()
            WorkManager.getInstance(context)
                    .beginUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, uploadDataWorkRequest)
                    .then(uploadPicturesWorkRequest)
                    .enqueue()
        }
    }
}