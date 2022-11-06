package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import java.io.File
import java.io.IOException
import kotlin.math.ceil

private const val LOG_TAG = "ContextExt"

/**
 * Function which returns true if the battery level is low
 *
 * @return true if battery is low or false if battery in not low
 */
fun Context.isBatteryLevelLow(percent: Int = 15): Boolean {
    val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus = registerReceiver(null, filter) ?: throw IllegalStateException("cannot get battery level")

    val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

    val batteryPct = level.toFloat() / scale * 100
    Log.i("BATTERYSTATUS", batteryPct.toString())
    return ceil(batteryPct) <= percent
}

fun Context.isLowBatteryMode() = isDisableImageLoad() && isBatteryLevelLow()

fun Context.isDisableImageLoad(defValue: Boolean = false) = getDefaultSharedPreferences(this)
    .getBoolean("disableImageLoad", defValue)

fun Context.isFastAdditionMode(defValue: Boolean = false) = getDefaultSharedPreferences(this)
    .getBoolean("fastAdditionMode", defValue)

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
    // Prefer external storage, photos are not sensible.
    // From android docs:
    // > If you need to store sensitive data only temporarily,
    // > you should use the app's designated cache directory
    // > within internal storage to save the data
    val cacheDir = if (Utils.isExternalStorageWritable()) externalCacheDir else cacheDir

    val picDir = File(cacheDir, "EasyImage")
    if (picDir.exists()) {
        check(picDir.isDirectory) { "Path '$picDir' is not a directory." }
    } else {
        if (picDir.mkdirs()) Log.i(LOG_TAG, "Directory '${picDir.absolutePath}' created.")
        else throw IOException("Couldn't create directory '${picDir.absolutePath}'.")
    }
    return picDir
}

/**
 * Check if the user is connected to a network. This can be any network.
 *
 * @return `true` if connected or connecting;
 *
 * `false` otherwise.
 *
 */
@Suppress("DEPRECATION")
fun Context.isNetworkConnected(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val capability = cm.getNetworkCapabilities(cm.activeNetwork)
        capability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    } else {
        cm.activeNetworkInfo?.isConnectedOrConnecting ?: false
    }
}