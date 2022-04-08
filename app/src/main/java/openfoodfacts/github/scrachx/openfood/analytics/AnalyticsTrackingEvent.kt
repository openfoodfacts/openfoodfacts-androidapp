package openfoodfacts.github.scrachx.openfood.analytics

import android.app.Activity
import java.util.concurrent.TimeUnit

fun Activity.startTrackEvent(event : AnalyticsEvent): AnalyticsTrackingEvent {
    return AnalyticsTrackingEvent(event)
}

data class AnalyticsTrackingEvent(
    val startDate: Long,
    val event: AnalyticsEvent
) {

    constructor(event: AnalyticsEvent) : this(System.currentTimeMillis(), event)

    fun computeDurationInSeconds() =
        TimeUnit.MILLISECONDS.toSeconds(
            System.currentTimeMillis() - startDate
        ).toFloat()

}