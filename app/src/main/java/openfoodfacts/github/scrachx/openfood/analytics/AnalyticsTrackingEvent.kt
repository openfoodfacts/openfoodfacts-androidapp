package openfoodfacts.github.scrachx.openfood.analytics

import android.app.Activity
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

fun Activity.startTrackEvent(event: AnalyticsEvent): AnalyticsTrackingEvent {
    return AnalyticsTrackingEvent(event = event)
}

data class AnalyticsTrackingEvent(
    val startDate: Long = System.currentTimeMillis(),
    val event: AnalyticsEvent
) {

    fun computeDurationInSeconds(): Float =
        (System.currentTimeMillis() - startDate).milliseconds.inWholeSeconds.toFloat()

}