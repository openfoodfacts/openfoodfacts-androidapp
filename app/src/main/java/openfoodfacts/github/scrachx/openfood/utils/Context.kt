package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import openfoodfacts.github.scrachx.openfood.R
import java.io.File
import java.io.IOException


private const val LOG_TAG = "ContextExt"

/**
 * @return true if the device supports the [PowerManager] API and is
 * set to power save mode. False otherwise.
 */
fun Context.isPowerSaveMode(): Boolean {
    val powerManager = getSystemService<PowerManager>() ?: return false
    return powerManager.isPowerSaveMode
}

fun Context.shouldLoadImages() = !isImageLoadingDisabled() && !isPowerSaveMode()

fun Context.isImageLoadingDisabled(): Boolean {
    val preferences = getDefaultSharedPreferences(this)
    val key = getString(R.string.pref_low_battery_key)
    return preferences.getBoolean(key, false)
}

fun Context.isFastAdditionMode(defValue: Boolean = false): Boolean {
    val preferences = getDefaultSharedPreferences(this)
    val key = getString(R.string.pref_fast_addition_key)
    return preferences.getBoolean(key, defValue)
}

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