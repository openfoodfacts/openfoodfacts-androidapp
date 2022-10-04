package openfoodfacts.github.scrachx.openfood.utils

import androidx.work.ListenableWorker

fun <T> Result<T>.toWorkResult(): ListenableWorker.Result =
    if (isSuccess) ListenableWorker.Result.success()
    else ListenableWorker.Result.failure()
