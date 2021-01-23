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
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.work.*
import com.afollestad.materialdialogs.MaterialDialog
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.features.LoginActivity
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity.Companion.start
import openfoodfacts.github.scrachx.openfood.jobs.SavedProductUploadWorker
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.utils.SearchTypeUrls.getUrl
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit
import java.io.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.TimeUnit

private const val LOG_TAG_COMPRESS = "COMPRESS_IMAGE"

object Utils {
    private const val UPLOAD_JOB_TAG = "upload_saved_product_job"
    private var isUploadJobInitialised = false
    const val HEADER_USER_AGENT_SCAN = "Scan"
    const val HEADER_USER_AGENT_SEARCH = "Search"
    const val NO_DRAWABLE_RESOURCE = 0
    const val FORCE_REFRESH_TAXONOMIES = "force_refresh_taxonomies"

    fun italic(vararg content: CharSequence) = apply(content, StyleSpan(Typeface.ITALIC))

    fun boldItalic(vararg content: CharSequence) = apply(content, StyleSpan(Typeface.BOLD_ITALIC))

    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus ?: return
        (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(view.windowToken, 0)
    }

    @JvmStatic
    fun compressImage(fileUrl: String): String? {
        val decodedBitmap = decodeFile(File(fileUrl))
        if (decodedBitmap == null) {
            Log.e(LOG_TAG_COMPRESS, "$fileUrl not found")
            return null
        }
        val smallFileFront = File(fileUrl.replace(".png", "_small.png"))
        try {
            FileOutputStream(smallFileFront).use { stream -> decodedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream) }
        } catch (e: IOException) {
            Log.e(LOG_TAG_COMPRESS, e.message, e)
        }
        return smallFileFront.toString()
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

    /**
     * Return a round float value **with 2 decimals**
     *
     * **BE CAREFUL:** THE METHOD DOESN'T CHECK THE NUMBER AS A NUMBER.
     *
     * @param value float value
     * @return round value **with 2 decimals** or 0 if the value is empty or equals to 0
     */
    fun getRoundNumber(value: String, locale: Locale = Locale.getDefault()) = when {
        value.isEmpty() -> "?"
        value == "0" -> value
        else -> value.toDoubleOrNull()
                ?.let { DecimalFormat("##.##", DecimalFormatSymbols(locale)).format(it) }
                ?: "?"
    }

    /**
     * @see Utils.getRoundNumber
     */
    fun getRoundNumber(value: Float, locale: Locale = Locale.getDefault()) = getRoundNumber(value.toString(), locale)
    fun getRoundNumber(value: Double, locale: Locale = Locale.getDefault()) = getRoundNumber(value.toString(), locale)

    val daoSession get() = OFFApplication.daoSession

    /**
     * Schedules job to download when network is available
     */
    @Synchronized
    fun scheduleProductUploadJob(context: Context) {
        if (isUploadJobInitialised) return

        val periodicity = TimeUnit.MINUTES.toSeconds(30).toInt()
        val uploadWorkRequest = OneTimeWorkRequest.Builder(SavedProductUploadWorker::class.java)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build())
                .setInitialDelay(periodicity.toLong(), TimeUnit.SECONDS).build()
        WorkManager.getInstance(context).enqueueUniqueWork(UPLOAD_JOB_TAG, ExistingWorkPolicy.KEEP, uploadWorkRequest)

        isUploadJobInitialised = true
    }

    private fun defaultHttpBuilder(): OkHttpClient.Builder {
        // Our servers don't support TLS 1.3 therefore we need to create custom connectionSpec
        // with the correct ciphers to support network requests successfully on Android 7
        val connectionSpecModernTLS = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                .build()
        val builder = OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(RW_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                .writeTimeout(RW_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                .connectionSpecs(listOf(connectionSpecModernTLS, ConnectionSpec.COMPATIBLE_TLS))
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        } else {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        }
        return builder
    }

    val defaultHttpClient: OkHttpClient by lazy { defaultHttpBuilder().build() }

    fun buildCachedHttpClient(context: Context): OkHttpClient {
        val maxSize: Long = 50 * 1024 * 1024
        return defaultHttpBuilder()
                .cache(Cache(File(context.cacheDir, "http-cache"), maxSize))
                .build()
    }

    fun picassoBuilder(context: Context): Picasso = Picasso.Builder(context)
            .downloader(OkHttp3Downloader(buildCachedHttpClient(context)))
            .build()

    /**
     * Check if the user is connected to a network. This can be any network.
     *
     * @param context of the application.
     * @return true if connected or connecting. False otherwise.
     */
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo ?: return false
        return activeNetwork.isConnectedOrConnecting
    }

    @JvmStatic
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

    fun isExternalStorageWritable() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    @JvmStatic
    fun getOutputPicUri(context: Context): Uri =
            Uri.fromFile(File(makeOrGetPictureDirectory(context), "${System.currentTimeMillis()}.jpg"))

    fun getClickableText(
            text: String,
            urlParameter: String,
            type: SearchType,
            activity: Activity,
            customTabsIntent: CustomTabsIntent
    ): CharSequence {
        val url = getUrl(type)

        val clickableSpan = if (url == null) object : ClickableSpan() {
            override fun onClick(view: View) = start(activity, type, text)

        } else object : ClickableSpan() {
            override fun onClick(textView: View) = CustomTabActivityHelper.openCustomTab(
                    activity,
                    customTabsIntent,
                    Uri.parse(url + urlParameter),
                    WebViewFallback()
            )
        }
        return SpannableString(text).apply {
            setSpan(clickableSpan, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    /**
     * Function to open ContinuousScanActivity to facilitate scanning
     *
     * @param activity
     */
    fun scan(activity: Activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
                MaterialDialog.Builder(activity)
                        .title(R.string.action_about)
                        .content(R.string.permission_camera)
                        .neutralText(R.string.txtOk)
                        .show().setOnDismissListener {
                            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA),
                                    MY_PERMISSIONS_REQUEST_CAMERA)
                        }
            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
            }
        } else {
            val intent = Intent(activity, ContinuousScanActivity::class.java)
            activity.startActivity(intent)
        }
    }

    @JvmStatic
    fun firstNotEmpty(vararg args: String?) = args.firstOrNull { it != null && it.isNotEmpty() }

}

fun isAllGranted(grantResults: Map<String?, Boolean?>) = !grantResults.containsValue(false)

fun isAllGranted(grantResults: IntArray) =
        grantResults.isNotEmpty() && grantResults.none { it != PackageManager.PERMISSION_GRANTED }

/**
 * Ask to login before editing product
 */
fun startLoginToEditAnd(requestCode: Int, activity: Activity?) {
    if (activity == null) return
    MaterialDialog.Builder(activity)
            .title(R.string.sign_in_to_edit)
            .positiveText(R.string.txtSignIn)
            .negativeText(R.string.dialog_cancel)
            .onPositive { dialog, _ ->
                val intent = Intent(activity, LoginActivity::class.java)
                activity.startActivityForResult(intent, requestCode)
                dialog.dismiss()
            }
            .onNegative { dialog, _ -> dialog.dismiss() }
            .build().show()
}

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
                o.outHeight / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2
        }

        // Decode with inSampleSize
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        return BitmapFactory.decodeStream(FileInputStream(f), null, o2)
    } catch (e: FileNotFoundException) {
        Log.e(Utils::class.simpleName, "Error while decoding file $f", e)
    }
    return null
}

private const val REQUIRED_SIZE = 1200
private const val CONNECTION_TIMEOUT = 5000
private const val RW_TIMEOUT = 30000
const val SPACE = " "
const val MY_PERMISSIONS_REQUEST_CAMERA = 1
const val MY_PERMISSIONS_REQUEST_STORAGE = 2

/**
 * Returns a CharSequence that concatenates the specified array of CharSequence
 * objects and then applies a list of zero or more tags to the entire range.
 *
 * @param content an array of character sequences to apply a style to
 * @param tags the styled span objects to apply to the content
 * such as android.text.style.StyleSpan
 */
private fun apply(content: Array<out CharSequence>, vararg tags: StyleSpan) = SpannableStringBuilder().let {
    openTags(it, tags)
    content.forEach { item -> it.append(item) }
    closeTags(it, tags)
    it.toString()
}

/**
 * Iterates over an array of tags and applies them to the beginning of the specified
 * Spannable object so that future text appended to the text will have the styling
 * applied to it. Do not call this method directly.
 */
private fun openTags(text: Spannable, tags: Array<out StyleSpan>) {
    tags.forEach { text.setSpan(it, 0, 0, Spanned.SPAN_MARK_MARK) }
}

/**
 * "Closes" the specified tags on a Spannable by updating the spans to be
 * endpoint-exclusive so that future text appended to the end will not take
 * on the same styling. Do not call this method directly.
 */
private fun closeTags(text: Spannable, tags: Array<out StyleSpan>) {
    tags.forEach {
        if (text.length > 0) {
            text.setSpan(it, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            text.removeSpan(it)
        }
    }
}

/**
 * Returns a CharSequence that applies boldface to the concatenation
 * of the specified CharSequence objects.
 */
fun bold(vararg content: CharSequence) = apply(content, StyleSpan(Typeface.BOLD))

fun getModifierNonDefault(modifier: String) = if (modifier != DEFAULT_MODIFIER) modifier else ""

fun dpsToPixel(dps: Int, context: Context) = (dps * context.resources.displayMetrics.density + 0.5f).toInt()

private val LOG_TAG = Utils::class.simpleName!!

/**
 * @param context The context
 * @return Returns the version name of the app
 */
fun getVersionName(context: Context): String = try {
    val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    pInfo.versionName
} catch (e: PackageManager.NameNotFoundException) {
    Log.e(LOG_TAG, "getVersionName", e)
    "(version unknown)"
} catch (e: NullPointerException) {
    Log.e(LOG_TAG, "getVersionName", e)
    "(version unknown)"
}

fun <T : View?> ViewGroup.getViewsByType(typeClass: Class<T>): List<T> {
    val result = mutableListOf<T>()
    children.forEach { child ->
        if (child is ViewGroup) {
            result.addAll(child.getViewsByType(typeClass))
        }
        if (typeClass.isInstance(child)) {
            result += typeClass.cast(child)
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
fun isHardwareCameraInstalled(context: Context) = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
