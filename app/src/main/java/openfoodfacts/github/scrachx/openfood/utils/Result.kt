package openfoodfacts.github.scrachx.openfood.utils

import androidx.work.ListenableWorker

fun <T> Result<T>.toWorkResult(): ListenableWorker.Result = fold(
    onSuccess = { ListenableWorker.Result.success() },
    onFailure = { ListenableWorker.Result.failure() },
)