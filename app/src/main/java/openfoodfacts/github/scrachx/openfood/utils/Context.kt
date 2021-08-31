package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.util.Log
import androidx.preference.PreferenceManager
import java.io.File
import kotlin.math.ceil

private const val LOG_TAG = "ContextExt"

/**
 * Function which returns true if the battery level is low
 *
 * @return true if battery is low or false if battery in not low
 */
fun Context.isBatteryLevelLow(percent: Int = 15): Boolean {
    val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus = registerReceiver(null, ifilter) ?: throw IllegalStateException("cannot get battery level")

    val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

    val batteryPct = level / scale.toFloat() * 100
    Log.i("BATTERYSTATUS", batteryPct.toString())
    return ceil(batteryPct.toDouble()) <= percent
}

fun Context.isDisableImageLoad(defValue: Boolean = false) = PreferenceManager.getDefaultSharedPreferences(this)
    .getBoolean("disableImageLoad", defValue)

fun Context.isLowBatteryMode() = isDisableImageLoad() && isBatteryLevelLow()

fun Context.isFastAdditionMode(defValue: Boolean = false) = PreferenceManager.getDefaultSharedPreferences(this)
    .getBoolean("fastAdditionMode", defValue)

fun Context.dpsToPixel(dps: Int) = dps.toPx(this)

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
fun Context.clearCameraCache() {
    (getCameraCacheLocation().listFiles() ?: return).forEach {
        if (it.delete()) Log.i(LOG_TAG, "Deleted cached photo '${it.absolutePath}'.")
        else Log.i(LOG_TAG, "Couldn't delete cached photo '${it.absolutePath}'.")
    }
}

fun Context.getCameraCacheLocation(): File {
    var cacheDir = cacheDir
    if (Utils.isExternalStorageWritable()) {
        cacheDir = externalCacheDir
    }
    val picDir = File(cacheDir, "EasyImage")
    if (!picDir.exists()) {
        if (picDir.mkdirs()) Log.i(LOG_TAG, "Directory '${picDir.absolutePath}' created.")
        else Log.i(LOG_TAG, "Couldn't create directory '${picDir.absolutePath}'.")
    }
    return picDir
}