package openfoodfacts.github.scrachx.openfood.utils

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Observable

fun <T> Observable<T>.subscribeLifecycle(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
    RxLifecycleHandler(lifecycleOwner, this, observer)
}


/**
 *  Logs all lifecycle events of the [io.reactivex.Observable].
 */
@Deprecated("Should NOT be used in code at all, only for temporary debugging purposes.")
fun <T> Observable<T>.log(streamName: String, message: String? = null): Observable<T> {
    val msg = if (message == null) "" else " $message"
    return this
        .doOnNext { Log.d(streamName, "$msg: $it") }
        .doOnError { Log.w(streamName, "$msg [Error]: $it: ${it.message}") }
        .doOnComplete { Log.w(streamName, "$msg [Complete]") }
        .doOnSubscribe { Log.w(streamName, "$msg [Subscribed]") }
        .doOnDispose { Log.w(streamName, "$msg [Disposed]") }
}
