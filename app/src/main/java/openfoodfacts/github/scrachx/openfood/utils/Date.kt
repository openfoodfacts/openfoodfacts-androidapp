package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import openfoodfacts.github.scrachx.openfood.R
import java.util.*
import java.util.concurrent.TimeUnit

fun Date.durationToNowFormatted(context: Context): String {
    val duration = Date().time - time
    val seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
    val hours = TimeUnit.MILLISECONDS.toHours(duration)
    val days = TimeUnit.MILLISECONDS.toDays(duration)
    return when {
        seconds < 60 -> context.resources.getQuantityString(R.plurals.seconds, seconds.toInt(), seconds.toInt())
        minutes < 60 -> context.resources.getQuantityString(R.plurals.minutes, minutes.toInt(), minutes.toInt())
        hours < 24 -> context.resources.getQuantityString(R.plurals.hours, hours.toInt(), hours.toInt())
        else -> context.resources.getQuantityString(R.plurals.days, days.toInt(), days.toInt())
    }
}
