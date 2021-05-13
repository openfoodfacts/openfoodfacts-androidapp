package openfoodfacts.github.scrachx.openfood.jobs

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.Single
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.utils.OfflineProductService
import javax.inject.Inject

@HiltWorker
class ProductUploaderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

    @Inject
    lateinit var offlineProductService: OfflineProductService

    override fun createWork() = Single.fromCallable { inputData.getBoolean(KEY_INCLUDE_IMAGES, false) }
        .doOnSuccess { Log.d(WORK_TAG, "[START] (includeImages=$it)") }
        .flatMap { includeImages -> offlineProductService.uploadAll(includeImages) }
        .map { shouldRetry ->
            if (shouldRetry) {
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
        private fun inputData(includeImages: Boolean) = Data.Builder()
            .putBoolean(KEY_INCLUDE_IMAGES, includeImages)
            .build()

        fun scheduleProductUpload(context: Context, sharedPreferences: SharedPreferences) {

            val constData = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val uploadDataWorkRequest = OneTimeWorkRequest.Builder(ProductUploaderWorker::class.java)
                .setInputData(inputData(false))
                .setConstraints(constData)
                .build()

            val constPics = Constraints.Builder()
                .setRequiredNetworkType(
                    if (sharedPreferences.getBoolean(context.getString(R.string.pref_enable_mobile_data_key), true))
                        NetworkType.CONNECTED
                    else
                        NetworkType.UNMETERED
                )
            val uploadPicturesWorkRequest = OneTimeWorkRequest.Builder(ProductUploaderWorker::class.java)
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
