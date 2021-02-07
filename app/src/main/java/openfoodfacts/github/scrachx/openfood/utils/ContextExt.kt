package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.util.Log
import androidx.preference.PreferenceManager
import openfoodfacts.github.scrachx.openfood.features.PreferencesFragment
import kotlin.math.ceil

fun Context.isUserSet() = !getLoginPreferences().getString("user", null).isNullOrBlank()

fun Context.getLoginPreferences(mode: Int = 0): SharedPreferences =
        getSharedPreferences(PreferencesFragment.LOGIN_PREF, mode)

/**
 * Function which returns true if the battery level is low
 *
 * @param this@isBatteryLevelLow the context
 * @return true if battery is low or false if battery in not low
 */
fun Context.isBatteryLevelLow(): Boolean {
    val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus = registerReceiver(null, ifilter) ?: throw IllegalStateException("cannot get battery level")
    val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    val batteryPct = level / scale.toFloat() * 100
    Log.i("BATTERYSTATUS", batteryPct.toString())
    return ceil(batteryPct.toDouble()) <= 15
}

fun Context.isDisableImageLoad(defValue: Boolean = false) = PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean("disableImageLoad", defValue)

fun Context.isLowBatteryMode() = isDisableImageLoad() && isBatteryLevelLow()

fun Context.isFastAdditionMode(defValue: Boolean = false) = PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean("fastAdditionMode", defValue)

fun Context.dpsToPixel(dps: Int) = (dps * resources.displayMetrics.density + 0.5f).toInt()