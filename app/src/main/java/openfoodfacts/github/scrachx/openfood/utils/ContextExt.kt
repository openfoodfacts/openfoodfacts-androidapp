package openfoodfacts.github.scrachx.openfood.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.util.Log
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import openfoodfacts.github.scrachx.openfood.features.PreferencesFragment
import kotlin.math.ceil

private const val LOG_TAG = "ContextExt"

fun Context.isUserSet() = !getLoginPreferences().getString("user", null).isNullOrBlank()

fun Context.getLoginPreferences(mode: Int = 0): SharedPreferences =
    getSharedPreferences(PreferencesFragment.LOGIN_PREF, mode)

/**
 * Function which returns true if the battery level is low
 *
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

fun Context.dpsToPixel(dps: Int) = dps.toPx(this)

fun Number.toPx(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    context.resources.displayMetrics
).toInt()

/**
 * @return Returns the version name of the app
 */
fun Context.getVersionName(): String = try {
    packageManager.getPackageInfo(packageName, 0).versionName
} catch (e: PackageManager.NameNotFoundException) {
    Log.e(LOG_TAG, "getVersionName", e)
    "(version unknown)"
} catch (e: NullPointerException) {
    Log.e(LOG_TAG, "getVersionName", e)
    "(version unknown)"
}

fun Context.isHardwareCameraInstalled() = isHardwareCameraInstalled(this)

fun Activity.hideKeyboard() {
    val view = currentFocus ?: return
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(view.windowToken, 0)
}