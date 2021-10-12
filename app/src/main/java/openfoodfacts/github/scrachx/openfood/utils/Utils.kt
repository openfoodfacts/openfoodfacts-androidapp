/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.children
import androidx.work.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Callback
import com.squareup.picasso.RequestCreator
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.jobs.ImagesUploaderWorker
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit
import java.io.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.TimeUnit
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity.Companion.start as startSearch

private const val LOG_TAG_COMPRESS = "COMPRESS_IMAGE"

object Utils {
    private const val UPLOAD_JOB_TAG = "upload_saved_product_job"
    private var isUploadJobInitialised = false
    const val HEADER_USER_AGENT_SCAN = "Scan"
    const val HEADER_USER_AGENT_SEARCH = "Search"
    const val NO_DRAWABLE_RESOURCE = 0
    const val FORCE_REFRESH_TAXONOMIES = "force_refresh_taxonomies"


    @JvmStatic
    fun compressImage(fileUrl: String): String? {
        val decodedBitmap = decodeFile(File(fileUrl))
        if (decodedBitmap == null) {
            Log.e(LOG_TAG_COMPRESS, "$fileUrl not found")
            return null
        }
        val smallFileFront = File(fileUrl.replace(".png", "_small.png"))
        try {
            FileOutputStream(smallFileFront).use { decodedBitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } catch (e: IOException) {
            Log.e(LOG_TAG_COMPRESS, e.message, e)
        }
        return smallFileFront.toString()
    }

    /**
     * Schedules job to download when network is available
     */
    @Synchronized
    fun scheduleProductUploadJob(context: Context) {
        if (isUploadJobInitialised) return

        val periodicity = TimeUnit.MINUTES.toSeconds(30).toInt()
        val uploadWorkRequest = OneTimeWorkRequest.Builder(ImagesUploaderWorker::class.java)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build())
            .setInitialDelay(periodicity.toLong(), TimeUnit.SECONDS).build()
        WorkManager.getInstance(context).enqueueUniqueWork(UPLOAD_JOB_TAG, ExistingWorkPolicy.KEEP, uploadWorkRequest)

        isUploadJobInitialised = true
    }

    fun makeOrGetPictureDirectory(context: Context): File {
        // determine the profile directory
        var dir = context.filesDir
        if (isExternalStorageWritable()) {
            dir = context.getExternalFilesDir(null)
        }
        val picDir = File(dir, "Pictures")
        if (picDir.exists()) {
            return picDir
        }
        // creates the directory if not present yet
        val mkdir = picDir.mkdirs()
        if (!mkdir) {
            Log.e(Utils::class.simpleName, "Can create dir $picDir")
        }
        return picDir
    }

    fun isExternalStorageWritable() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    fun getOutputPicUri(context: Context): Uri =
        File(makeOrGetPictureDirectory(context), "${System.currentTimeMillis()}.jpg").toUri()

    /**
     * Function to open ContinuousScanActivity to facilitate scanning
     *
     * @param activity
     */
    fun scan(activity: Activity) {
        when {
            checkSelfPermission(activity, Manifest.permission.CAMERA) == PERMISSION_GRANTED -> {
                activity.startActivity(Intent(activity, ContinuousScanActivity::class.java))
            }
            shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) -> {
                MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.action_about)
                    .setMessage(R.string.permission_camera)
                    .setPositiveButton(android.R.string.ok) { d, _ ->
                        requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.CAMERA),
                            MY_PERMISSIONS_REQUEST_CAMERA
                        )
                        d.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
                    .show()
            }
            else -> {
                requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
            }
        }

    }

    @JvmStatic
    fun firstNotEmpty(vararg args: String?) = args.firstOrNull { it != null && it.isNotEmpty() }
}

fun isAllGranted(grantResults: IntArray) =
    grantResults.isNotEmpty() && grantResults.none { it != PERMISSION_GRANTED }

fun buildSignInDialog(
    context: Context,
    onPositive: (DialogInterface, Int) -> Unit = { d, _ -> d.dismiss() },
    onNegative: (DialogInterface, Int) -> Unit = { d, _ -> d.dismiss() }
): MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
    .setTitle(R.string.sign_in_to_edit)
    .setPositiveButton(R.string.txtSignIn) { d, i -> onPositive(d, i) }
    .setNegativeButton(R.string.dialog_cancel) { d, i -> onNegative(d, i) }


/**
 * @param type Type of call (Search or Scan)
 * @return Returns the header to be put in network call
 */
fun getUserAgent(type: String) = "${getUserAgent()} $type"
fun getUserAgent() = "${BuildConfig.APP_NAME} Official Android App ${BuildConfig.VERSION_NAME}"

/**
 * Decodes image and scales it to reduce memory consumption
 */
private fun decodeFile(f: File): Bitmap? {
    try {
        // Decode image size
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(FileInputStream(f), null, o)

        // The new size we want to scale to
        // Find the correct scale value. It should be the power of 2.
        var scale = 1
        while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
            o.outHeight / scale / 2 >= REQUIRED_SIZE
        ) {
            scale *= 2
        }

        // Decode with inSampleSize
        val o2 = BitmapFactory.Options().apply {
            inSampleSize = scale
        }
        return BitmapFactory.decodeStream(FileInputStream(f), null, o2)
    } catch (e: FileNotFoundException) {
        Log.e(LOG_TAG, "Error while decoding file $f", e)
    }
    return null
}

private const val REQUIRED_SIZE = 1200
const val MY_PERMISSIONS_REQUEST_CAMERA = 1
const val MY_PERMISSIONS_REQUEST_STORAGE = 2


private val LOG_TAG = Utils::class.simpleName!!

fun <T : View?> ViewGroup.getViewsByType(typeClass: Class<T>): List<T> {
    val result = mutableListOf<T>()
    children.forEach { view ->
        if (view is ViewGroup) {
            result += view.getViewsByType(typeClass)
        }
        if (typeClass.isInstance(view)) {
            result += typeClass.cast(view)
        }
    }
    return result
}

/**
 * @param barcode
 * @return true if valid according to [EAN13CheckDigit.EAN13_CHECK_DIGIT]
 * and if the barcode doesn't start will 977/978/979 (Book barcode)
 */
fun isBarcodeValid(barcode: String?): Boolean {
    // For debug only: the barcode '1' is used for test:
    if (ApiFields.Defaults.DEBUG_BARCODE == barcode) return true
    return barcode != null
            && EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(barcode)
            && barcode.length > 3
            && (!barcode.substring(0, 3).contains("977")
            || !barcode.substring(0, 3).contains("978")
            || !barcode.substring(0, 3).contains("979"))
}

/**
 * Check if the device has a camera installed.
 *
 * @return true if installed, false otherwise.
 */
fun isHardwareCameraInstalled(context: Context) = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)


fun getSearchLinkText(
    text: String,
    type: SearchType,
    activityToStart: Activity
): CharSequence {
    val clickable = object : ClickableSpan() {
        override fun onClick(view: View) = startSearch(activityToStart, type, text)
    }
    return buildSpannedString {
        inSpans(clickable) { append(text) }
    }
}


internal fun RequestCreator.into(target: ImageView, onSuccess: () -> Unit) {
    return into(target, object : Callback {
        override fun onSuccess() = onSuccess()
        override fun onError(e: Exception) = throw e
    })
}


fun @receiver:ColorInt Int.darken(ratio: Float) = ColorUtils.blendARGB(this, Color.BLACK, ratio)
fun @receiver:ColorInt Int.lighten(ratio: Float) = ColorUtils.blendARGB(this, Color.WHITE, ratio)

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

/**
 * Check if a certain application is installed on a device.
 *
 * @param context the applications context.
 * @param packageName the package name that you want to check.
 * @return true if the application is installed, false otherwise.
 */
fun isApplicationInstalled(context: Context, packageName: String) = try {
    // Check if the package name exists, if exception is thrown, package name does not
    // exist.
    context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    true
} catch (e: PackageManager.NameNotFoundException) {
    false
}

/**
 * Returns the NOVA group explanation given the group
 */
fun getNovaGroupExplanation(novaGroup: String, context: Context) = when (novaGroup) {
    "1" -> context.resources.getString(R.string.nova_grp1_msg)
    "2" -> context.resources.getString(R.string.nova_grp2_msg)
    "3" -> context.resources.getString(R.string.nova_grp3_msg)
    "4" -> context.resources.getString(R.string.nova_grp4_msg)
    else -> null
}

fun Context.getBitmapFromDrawable(@DrawableRes drawableId: Int): Bitmap? {
    val drawable = AppCompatResources.getDrawable(this, drawableId) ?: return null
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun getRoundNumber(measurement: Measurement, locale: Locale = Locale.getDefault()) =
    getRoundNumber(measurement.value, locale)

fun getRoundNumber(value: Double, locale: Locale = Locale.getDefault()) =
    getRoundNumber(value.toString(), locale)

fun getRoundNumber(value: Float, locale: Locale = Locale.getDefault()) =
    getRoundNumber(value.toString(), locale)

/**
 * Return a round float value **with 2 decimals**
 *
 * **BE CAREFUL:** THE METHOD DOESN'T CHECK THE NUMBER AS A NUMBER.
 *
 * @param value float value
 * @return round value **with 2 decimals** or 0 if the value is empty or equals to 0
 */
fun getRoundNumber(value: CharSequence, locale: Locale = Locale.getDefault()) = when {
    value.isEmpty() -> "?"
    value == "0" -> value
    else -> value.toString().toDoubleOrNull()
        ?.let { DecimalFormat("##.##", DecimalFormatSymbols(locale)).format(it) }
        ?: "?"
}