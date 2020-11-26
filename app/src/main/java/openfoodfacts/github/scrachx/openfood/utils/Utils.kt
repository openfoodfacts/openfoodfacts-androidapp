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
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Environment
import android.text.*
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
import androidx.preference.PreferenceManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import androidx.work.*
import com.afollestad.materialdialogs.MaterialDialog
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
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
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.utils.SearchTypeUrls.getUrl
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang.StringUtils
import org.jetbrains.annotations.Contract
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

object Utils {
    private const val CONNECTION_TIMEOUT = 5000
    private const val RW_TIMEOUT = 30000
    const val SPACE = " "
    const val MY_PERMISSIONS_REQUEST_CAMERA = 1
    const val MY_PERMISSIONS_REQUEST_STORAGE = 2
    private const val UPLOAD_JOB_TAG = "upload_saved_product_job"
    private var isUploadJobInitialised = false
    const val HEADER_USER_AGENT_SCAN = "Scan"
    const val HEADER_USER_AGENT_SEARCH = "Search"
    const val NO_DRAWABLE_RESOURCE = 0
    const val FORCE_REFRESH_TAXONOMIES = "force_refresh_taxonomies"

    /**
     * Returns a CharSequence that concatenates the specified array of CharSequence
     * objects and then applies a list of zero or more tags to the entire range.
     *
     * @param content an array of character sequences to apply a style to
     * @param tags the styled span objects to apply to the content
     * such as android.text.style.StyleSpan
     */
    private fun apply(content: Array<out CharSequence>, vararg tags: Any): String {
        return SpannableStringBuilder().let {
            openTags(it, tags)
            content.forEach { item -> it.append(item) }
            closeTags(it, tags)
            toString()
        }
    }

    /**
     * Iterates over an array of tags and applies them to the beginning of the specified
     * Spannable object so that future text appended to the text will have the styling
     * applied to it. Do not call this method directly.
     */
    private fun openTags(text: Spannable, tags: Array<out Any>) {
        for (tag in tags) {
            text.setSpan(tag, 0, 0, Spanned.SPAN_MARK_MARK)
        }
    }

    /**
     * "Closes" the specified tags on a Spannable by updating the spans to be
     * endpoint-exclusive so that future text appended to the end will not take
     * on the same styling. Do not call this method directly.
     */
    private fun closeTags(text: Spannable, tags: Array<out Any>) {
        tags.forEach { tag ->
            if (text.length > 0) {
                text.setSpan(tag, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                text.removeSpan(tag)
            }
        }
    }

    /**
     * Returns a CharSequence that applies boldface to the concatenation
     * of the specified CharSequence objects.
     */
    fun bold(vararg content: CharSequence): String {
        return apply(content, StyleSpan(Typeface.BOLD))
    }

    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus ?: return
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun compressImage(fileUrl: String): String? {
        val decodedBitmap = decodeFile(File(fileUrl))
        if (decodedBitmap == null) {
            Log.e("COMPRESS_IMAGE", "$fileUrl not found")
            return null
        }
        val smallFileFront = File(fileUrl.replace(".png", "_small.png"))
        try {
            FileOutputStream(smallFileFront).use { fOutFront -> decodedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOutFront) }
        } catch (e: IOException) {
            Log.e("COMPRESS_IMAGE", e.message, e)
        }
        return smallFileFront.toString()
    }

    fun getColor(context: Context?, id: Int): Int {
        return ContextCompat.getColor(context!!, id)
    }

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
            val REQUIRED_SIZE = 1200

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

    /**
     * Check if a certain application is installed on a device.
     *
     * @param context the applications context.
     * @param packageName the package name that you want to check.
     * @return true if the application is installed, false otherwise.
     */
    fun isApplicationInstalled(context: Context, packageName: String?): Boolean {
        val pm = context.packageManager
        return try {
            // Check if the package name exists, if exception is thrown, package name does not
            // exist.
            pm.getPackageInfo(packageName!!, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Returns the Nutri-Score graphic asset given the grade
     */
    @DrawableRes
    fun getImageGrade(grade: String?): Int {
        return if (grade == null) {
            NO_DRAWABLE_RESOURCE
        } else when (grade.toLowerCase(Locale.getDefault())) {
            "a" -> R.drawable.ic_nutriscore_a
            "b" -> R.drawable.ic_nutriscore_b
            "c" -> R.drawable.ic_nutriscore_c
            "d" -> R.drawable.ic_nutriscore_d
            "e" -> R.drawable.ic_nutriscore_e
            else -> NO_DRAWABLE_RESOURCE
        }
    }

    fun getImageGrade(product: Product?): Int {
        return getImageGrade(product?.nutritionGradeFr)
    }

    fun getImageGradeDrawable(context: Context, grade: String?): Drawable? {
        val gradeID = getImageGrade(grade)
        return if (gradeID == NO_DRAWABLE_RESOURCE) {
            null
        } else VectorDrawableCompat.create(context.resources, gradeID, null)
    }

    fun getImageGradeDrawable(context: Context, product: Product?): Drawable? {
        return getImageGradeDrawable(context, product?.nutritionGradeFr)
    }

    /**
     * Returns the NOVA group explanation given the group
     */
    fun getNovaGroupExplanation(novaGroup: String?, context: Context): String {
        return if (novaGroup == null) {
            ""
        } else when (novaGroup) {
            "1" -> context.resources.getString(R.string.nova_grp1_msg)
            "2" -> context.resources.getString(R.string.nova_grp2_msg)
            "3" -> context.resources.getString(R.string.nova_grp3_msg)
            "4" -> context.resources.getString(R.string.nova_grp4_msg)
            else -> ""
        }
    }

    @JvmStatic
    fun <T : View?> getViewsByType(root: ViewGroup, typeClass: Class<T>): List<T> {
        val result = ArrayList<T>()
        val childCount = root.childCount
        for (i in 0 until childCount) {
            val child = root.getChildAt(i)
            if (child is ViewGroup) {
                result.addAll(getViewsByType(child, typeClass))
            }
            if (typeClass.isInstance(child)) {
                result.add(typeClass.cast(child))
            }
        }
        return result
    }

    /**
     * Returns the NOVA group graphic asset given the group
     */
    fun getNovaGroupDrawable(product: Product?): Int {
        return getNovaGroupDrawable(product?.novaGroups)
    }

    @DrawableRes
    fun getNovaGroupDrawable(novaGroup: String?): Int {
        return if (novaGroup == null) {
            NO_DRAWABLE_RESOURCE
        } else when (novaGroup) {
            "1" -> R.drawable.ic_nova_group_1
            "2" -> R.drawable.ic_nova_group_2
            "3" -> R.drawable.ic_nova_group_3
            "4" -> R.drawable.ic_nova_group_4
            else -> NO_DRAWABLE_RESOURCE
        }
    }

    @JvmStatic
    fun getSmallImageGrade(product: Product?): Int {
        if (product == null) {
            return getSmallImageGrade(null as String?)
        }
        // Prefer the global tag to the FR tag
        return if (product.nutritionGradeTag != null) {
            getSmallImageGrade(product.nutritionGradeTag)
        } else {
            getSmallImageGrade(product.nutritionGradeFr)
        }
    }

    @DrawableRes
    fun getImageEnvironmentImpact(product: Product?): Int {
        if (product == null) {
            return NO_DRAWABLE_RESOURCE
        }
        val tags = product.environmentImpactLevelTags
        if (CollectionUtils.isEmpty(tags)) {
            return NO_DRAWABLE_RESOURCE
        }
        return when (tags[0]!!.replace("\"", "")) {
            "en:high" -> R.drawable.ic_co2_high_24dp
            "en:low" -> R.drawable.ic_co2_low_24dp
            "en:medium" -> R.drawable.ic_co2_medium_24dp
            else -> NO_DRAWABLE_RESOURCE
        }
    }

    fun getImageEcoscore(product: Product?): Int {
        if (product == null) {
            return NO_DRAWABLE_RESOURCE
        }
        val ecoscore = product.ecoscore ?: return NO_DRAWABLE_RESOURCE
        return when (ecoscore) {
            "a" -> R.drawable.ic_ecoscore_a
            "b" -> R.drawable.ic_ecoscore_b
            "c" -> R.drawable.ic_ecoscore_c
            "d" -> R.drawable.ic_ecoscore_d
            "e" -> R.drawable.ic_ecoscore_e
            else -> NO_DRAWABLE_RESOURCE
        }
    }

    fun getSmallImageGrade(grade: String?): Int {
        return if (grade == null) {
            NO_DRAWABLE_RESOURCE
        } else when (grade.toLowerCase(Locale.getDefault())) {
            "a" -> R.drawable.ic_nutriscore_small_a
            "b" -> R.drawable.ic_nutriscore_small_b
            "c" -> R.drawable.ic_nutriscore_small_c
            "d" -> R.drawable.ic_nutriscore_small_d
            "e" -> R.drawable.ic_nutriscore_small_e
            else -> NO_DRAWABLE_RESOURCE
        }
    }

    @JvmStatic
    fun getBitmapFromDrawable(context: Context, @DrawableRes drawableId: Int): Bitmap? {
        val drawable = AppCompatResources.getDrawable(context, drawableId) ?: return null
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable
                .intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Return a round float value **with 2 decimals**
     * **BE CAREFUL:** THE METHOD DOESN'T CHECK THE NUMBER AS A NUMBER.
     *
     * @param value float value
     * @return round value **with 2 decimals** or 0 if the value is empty or equals to 0
     */
    fun getRoundNumber(value: String): String {
        if ("0" == value) {
            return value
        }
        if (TextUtils.isEmpty(value)) {
            return "?"
        }
        val strings = value.split("\\.").toTypedArray()
        return if (strings.size == 1 || strings.size == 2 && strings[1].length <= 2) {
            value
        } else String.format(Locale.getDefault(), "%.2f", java.lang.Double.valueOf(value))
    }

    /**
     * @see Utils.getRoundNumber
     */
    fun getRoundNumber(value: Float): String {
        return getRoundNumber(value.toString())
    }

    @get:JvmStatic
    val daoSession: DaoSession
        get() = OFFApplication.getDaoSession()

    /**
     * Check if the device has a camera installed.
     *
     * @return true if installed, false otherwise.
     */
    fun isHardwareCameraInstalled(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    /**
     * Schedules job to download when network is available
     */
    @Synchronized
    fun scheduleProductUploadJob(context: Context?) {
        if (isUploadJobInitialised) {
            return
        }
        val periodicity = TimeUnit.MINUTES.toSeconds(30).toInt()
        val uploadWorkRequest = OneTimeWorkRequest.Builder(SavedProductUploadWorker::class.java)
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                )
                .setInitialDelay(periodicity.toLong(), TimeUnit.SECONDS).build()
        WorkManager.getInstance(context!!).enqueueUniqueWork(UPLOAD_JOB_TAG, ExistingWorkPolicy.KEEP, uploadWorkRequest)
        isUploadJobInitialised = true
    }

    @JvmStatic
    fun httpClientBuilder(): OkHttpClient {
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
        return builder.build()
    }

    @JvmStatic
    fun picassoBuilder(context: Context?): Picasso {
        return Picasso.Builder(context!!)
                .downloader(OkHttp3Downloader(httpClientBuilder()))
                .build()
    }

    fun isUserLoggedIn(context: Context): Boolean {
        val settings = context.getSharedPreferences("login", 0)
        val login = settings.getString("user", "")
        return StringUtils.isNotEmpty(login)
    }

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
        if (isExternalStorageWritable) {
            dir = context.getExternalFilesDir(null)
        }
        val picDir = File(dir, "Pictures")
        if (picDir.exists()) {
            return picDir
        }
        // creates the directory if not present yet
        val mkdir = picDir.mkdirs()
        if (!mkdir) {
            Log.e(Utils::class.java.simpleName, "Can create dir $picDir")
        }
        return picDir
    }

    val isExternalStorageWritable: Boolean
        get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    @JvmStatic
    fun getOutputPicUri(context: Context): Uri {
        return Uri.fromFile(File(makeOrGetPictureDirectory(context), System.currentTimeMillis().toString() + ".jpg"))
    }

    fun getClickableText(text: String, urlParameter: String, type: SearchType?, activity: Activity?, customTabsIntent: CustomTabsIntent?): CharSequence {
        val clickableSpan: ClickableSpan
        val url = getUrl(type!!)
        clickableSpan = if (url == null) {
            object : ClickableSpan() {
                override fun onClick(view: View) {
                    start(activity!!, text, type)
                }
            }
        } else {
            val uri = Uri.parse(url + urlParameter)
            object : ClickableSpan() {
                override fun onClick(textView: View) {
                    CustomTabActivityHelper.openCustomTab(activity!!, customTabsIntent!!, uri, WebViewFallback())
                }
            }
        }
        val spannableText = SpannableString(text)
        spannableText.setSpan(clickableSpan, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableText
    }

    /**
     * Function which returns true if the battery level is low
     *
     * @param context the context
     * @return true if battery is low or false if battery in not low
     */
    fun isBatteryLevelLow(context: Context): Boolean {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter) ?: throw IllegalStateException("cannot get battery level")
        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = level / scale.toFloat() * 100
        Log.i("BATTERYSTATUS", batteryPct.toString())
        return ceil(batteryPct.toDouble()) <= 15
    }

    fun isDisableImageLoad(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("disableImageLoad", false)
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

    /**
     * @param context The context
     * @return Returns the version name of the app
     */
    @JvmStatic

    fun getVersionName(context: Context?): String {
        return try {
            val pInfo = context!!.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(Utils::class.java.simpleName, "getVersionName", e)
            "(version unknown)"
        } catch (e: NullPointerException) {
            Log.e(Utils::class.java.simpleName, "getVersionName", e)
            "(version unknown)"
        }
    }

    /**
     * @param type Type of call (Search or Scan)
     * @return Returns the header to be put in network call
     */
    @JvmStatic
    fun getUserAgent(type: String): String {
        return "${getUserAgent()} $type"
    }

    fun getUserAgent(): String = BuildConfig.APP_NAME + " Official Android App " + BuildConfig.VERSION_NAME

    /**
     * @param response Takes a string
     * @return Returns a Json object
     */
    fun createJsonObject(response: String): JSONObject? {
        return try {
            JSONObject(response)
        } catch (e: JSONException) {
            Log.e(Utils::class.java.simpleName, "createJsonObject", e)
            null
        }
    }

    @Contract(pure = true)
    @SafeVarargs
    fun <T> firstNotNull(vararg args: T?): T? {
        for (arg in args) {
            if (arg != null) {
                return arg
            }
        }
        return null
    }

    fun firstNotEmpty(vararg args: String?): String? {
        return args.firstOrNull { it != null && it.isNotEmpty() }
    }

    fun getModifierNonDefault(modifier: String): String {
        return if (modifier == Modifier.DEFAULT_MODIFIER) "" else modifier
    }

    @JvmStatic
    fun dpsToPixel(dps: Int, activity: Activity?): Int {
        if (activity == null) {
            return 0
        }
        val scale = activity.resources.displayMetrics.density
        return (dps * scale + 0.5f).toInt()
    }

    /**
     * Ask to login before editing product
     */
    fun startLoginToEditAnd(requestCode: Int, activity: Activity?) {
        if (activity == null) {
            return
        }
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

    @JvmStatic
    fun isAllGranted(grantResults: IntArray): Boolean {
        if (grantResults.isEmpty()) {
            return false
        }
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun isAllGranted(grantResults: Map<String?, Boolean?>): Boolean {
        return grantResults.containsValue(false)
    }
}