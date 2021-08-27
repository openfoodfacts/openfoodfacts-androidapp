package openfoodfacts.github.scrachx.openfood.utils

import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder

inline fun <reified T : ListenableWorker> buildOneTimeWorkRequest(
    builderAction: OneTimeWorkRequest.Builder.() -> Unit = {}
): OneTimeWorkRequest {
    return OneTimeWorkRequestBuilder<T>().apply(builderAction).build()
}