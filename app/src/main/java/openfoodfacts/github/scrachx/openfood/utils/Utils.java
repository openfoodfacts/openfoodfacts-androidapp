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

package openfoodfacts.github.scrachx.openfood.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.jobs.SavedProductUploadWorker;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.views.LoginActivity;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
import openfoodfacts.github.scrachx.openfood.views.scan.ContinuousScanActivity;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class Utils {
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int RW_TIMEOUT = 30000;
    public static final String SPACE = " ";
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    public static final int MY_PERMISSIONS_REQUEST_STORAGE = 2;
    private static final String UPLOAD_JOB_TAG = "upload_saved_product_job";
    private static boolean isUploadJobInitialised;
    public static final String HEADER_USER_AGENT_SCAN = "Scan";
    public static final String HEADER_USER_AGENT_SEARCH = "Search";
    public static final int NO_DRAWABLE_RESOURCE = 0;
    public static final String FORCE_REFRESH_TAXONOMIES = "force_refresh_taxonomies";

    private Utils() {
        // Utility class
    }

    /**
     * Returns a CharSequence that concatenates the specified array of CharSequence
     * objects and then applies a list of zero or more tags to the entire range.
     *
     * @param content an array of character sequences to apply a style to
     * @param tags the styled span objects to apply to the content
     *     such as android.text.style.StyleSpan
     */
    private static String apply(CharSequence[] content, Object... tags) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        openTags(text, tags);
        for (CharSequence item : content) {
            text.append(item);
        }
        closeTags(text, tags);
        return text.toString();
    }

    /**
     * Iterates over an array of tags and applies them to the beginning of the specified
     * Spannable object so that future text appended to the text will have the styling
     * applied to it. Do not call this method directly.
     */
    private static void openTags(Spannable text, Object[] tags) {
        for (Object tag : tags) {
            text.setSpan(tag, 0, 0, Spanned.SPAN_MARK_MARK);
        }
    }

    /**
     * "Closes" the specified tags on a Spannable by updating the spans to be
     * endpoint-exclusive so that future text appended to the end will not take
     * on the same styling. Do not call this method directly.
     */
    private static void closeTags(Spannable text, Object[] tags) {
        int len = text.length();
        for (Object tag : tags) {
            if (len > 0) {
                text.setSpan(tag, 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                text.removeSpan(tag);
            }
        }
    }

    /**
     * Returns a CharSequence that applies boldface to the concatenation
     * of the specified CharSequence objects.
     */
    public static String bold(CharSequence... content) {
        return apply(content, new StyleSpan(Typeface.BOLD));
    }

    public static void hideKeyboard(@NonNull Activity activity) {
        final View view = activity.getCurrentFocus();
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String compressImage(String fileUrl) {
        Bitmap decodedBitmap = decodeFile(new File(fileUrl));
        if (decodedBitmap == null) {
            Log.e("COMPRESS_IMAGE", fileUrl + " not found");
            return null;
        }

        File smallFileFront = new File(fileUrl.replace(".png", "_small.png"));

        try (OutputStream fOutFront = new FileOutputStream(smallFileFront)) {
            decodedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOutFront);
        } catch (IOException e) {
            Log.e("COMPRESS_IMAGE", e.getMessage(), e);
        }
        return smallFileFront.toString();
    }

    public static int getColor(Context context, int id) {
        return ContextCompat.getColor(context, id);
    }

    /**
     * Decodes image and scales it to reduce memory consumption
     */
    @Nullable
    private static Bitmap decodeFile(@NonNull File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 1200;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            Log.e(Utils.class.getSimpleName(), "decodeFile " + f, e);
        }
        return null;
    }

    /**
     * Check if a certain application is installed on a device.
     *
     * @param context the applications context.
     * @param packageName the package name that you want to check.
     * @return true if the application is installed, false otherwise.
     */
    public static boolean isApplicationInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            // Check if the package name exists, if exception is thrown, package name does not
            // exist.
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    
    /**
     * Returns the Nutri-Score graphic asset given the grade
     */
    @DrawableRes
    public static int getImageGrade(@Nullable String grade) {

        if (grade == null) {
            return NO_DRAWABLE_RESOURCE;
        }

        switch (grade.toLowerCase(Locale.getDefault())) {
            case "a":
                return R.drawable.ic_nutriscore_a;
            case "b":
                return R.drawable.ic_nutriscore_b;
            case "c":
                return R.drawable.ic_nutriscore_c;
            case "d":
                return R.drawable.ic_nutriscore_d;
            case "e":
                return R.drawable.ic_nutriscore_e;
            default:
                return NO_DRAWABLE_RESOURCE;
        }
    }

    public static int getImageGrade(@Nullable Product product) {
        return getImageGrade(product == null ? null : product.getNutritionGradeFr());
    }

    @Nullable
    public static Drawable getImageGradeDrawable(@NonNull Context context, @Nullable String grade) {

        int gradeID = getImageGrade(grade);
        if (gradeID == NO_DRAWABLE_RESOURCE) {
            return null;
        }
        return VectorDrawableCompat.create(context.getResources(), gradeID, null);
    }

    @Nullable
    public static Drawable getImageGradeDrawable(@NonNull Context context, @Nullable Product product) {
        return getImageGradeDrawable(context, product == null ? null : product.getNutritionGradeFr());
    }

    /**
     * Returns the NOVA group explanation given the group
     */
    public static String getNovaGroupExplanation(@Nullable String novaGroup, @NonNull Context context) {

        if (novaGroup == null) {
            return "";
        }

        switch (novaGroup) {
            case "1":
                return context.getResources().getString(R.string.nova_grp1_msg);
            case "2":
                return context.getResources().getString(R.string.nova_grp2_msg);
            case "3":
                return context.getResources().getString(R.string.nova_grp3_msg);
            case "4":
                return context.getResources().getString(R.string.nova_grp4_msg);
            default:
                return "";
        }
    }

    public static <T extends View> List<T> getViewsByType(ViewGroup root, Class<T> typeClass) {
        final ArrayList<T> result = new ArrayList<>();
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                result.addAll(getViewsByType((ViewGroup) child, typeClass));
            }

            if (typeClass.isInstance(child)) {
                result.add(typeClass.cast(child));
            }
        }
        return result;
    }
    
    /**
     * Returns the NOVA group graphic asset given the group
     */
    public static int getNovaGroupDrawable(@Nullable Product product) {
        return getNovaGroupDrawable(product == null ? null : product.getNovaGroups());
    }

    @DrawableRes
    public static int getNovaGroupDrawable(@Nullable String novaGroup) {

        if (novaGroup == null) {
            return NO_DRAWABLE_RESOURCE;
        }

        switch (novaGroup) {
            case "1":
                return R.drawable.ic_nova_group_1;
            case "2":
                return R.drawable.ic_nova_group_2;
            case "3":
                return R.drawable.ic_nova_group_3;
            case "4":
                return R.drawable.ic_nova_group_4;
            default:
                return NO_DRAWABLE_RESOURCE;
        }
    }

    public static int getSmallImageGrade(Product product) {
        if (product == null) {
            return getSmallImageGrade((String) null);
        }
        // Prefer the global tag to the FR tag
        if (product.getNutritionGradeTag() != null) {
            return getSmallImageGrade(product.getNutritionGradeTag());
        } else {
            return getSmallImageGrade(product.getNutritionGradeFr());
        }
    }

    public static int getImageEnvironmentImpact(Product product) {
        if (product == null) {
            return NO_DRAWABLE_RESOURCE;
        }
        List<String> tags = product.getEnvironmentImpactLevelTags();
        if (CollectionUtils.isEmpty(tags)) {
            return NO_DRAWABLE_RESOURCE;
        }
        String tag = tags.get(0).replace("\"", "");
        switch (tag) {
            case "en:high":
                return R.drawable.ic_co2_high_24dp;
            case "en:low":
                return R.drawable.ic_co2_low_24dp;
            case "en:medium":
                return R.drawable.ic_co2_medium_24dp;
            default:
                return NO_DRAWABLE_RESOURCE;
        }
    }

    public static int getSmallImageGrade(@Nullable String grade) {
        int drawable = NO_DRAWABLE_RESOURCE;

        if (grade == null) {
            return drawable;
        }

        switch (grade.toLowerCase(Locale.getDefault())) {
            case "a":
                drawable = R.drawable.ic_nutriscore_small_a;
                break;
            case "b":
                drawable = R.drawable.ic_nutriscore_small_b;
                break;
            case "c":
                drawable = R.drawable.ic_nutriscore_small_c;
                break;
            case "d":
                drawable = R.drawable.ic_nutriscore_small_d;
                break;
            case "e":
                drawable = R.drawable.ic_nutriscore_small_e;
                break;
            default:
                break;
        }

        return drawable;
    }

    public static Bitmap getBitmapFromDrawable(@NonNull Context context, @DrawableRes int drawableId) {
        Drawable drawable = AppCompatResources.getDrawable(context, drawableId);
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable
            .getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Return a round float value <b>with 2 decimals</b>
     * <b>BE CAREFUL:</b> THE METHOD DOESN'T CHECK THE NUMBER AS A NUMBER.
     *
     * @param value float value
     * @return round value <b>with 2 decimals</b> or 0 if the value is empty or equals to 0
     */
    public static String getRoundNumber(String value) {
        if ("0".equals(value)) {
            return value;
        }

        if (TextUtils.isEmpty(value)) {
            return "?";
        }

        String[] strings = value.split("\\.");
        if (strings.length == 1 || (strings.length == 2 && strings[1].length() <= 2)) {
            return value;
        }

        return String.format(Locale.getDefault(), "%.2f", Double.valueOf(value));
    }

    /**
     * @see Utils#getRoundNumber(String)
     */
    public static String getRoundNumber(float value) {
        return getRoundNumber(Float.toString(value));
    }

    public static DaoSession getDaoSession() {
        return OFFApplication.getDaoSession();
    }

    /**
     * Check if the device has a camera installed.
     *
     * @return true if installed, false otherwise.
     */
    public static boolean isHardwareCameraInstalled(@NonNull Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * Schedules job to download when network is available
     */
    public static synchronized void scheduleProductUploadJob(Context context) {
        if (isUploadJobInitialised) {
            return;
        }
        final int periodicity = (int) TimeUnit.MINUTES.toSeconds(30);

        OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(SavedProductUploadWorker.class)
            .setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
            )
            .setInitialDelay(periodicity, TimeUnit.SECONDS).build();
        WorkManager.getInstance(context).enqueueUniqueWork(UPLOAD_JOB_TAG, ExistingWorkPolicy.KEEP, uploadWorkRequest);
        isUploadJobInitialised = true;
    }

    public static OkHttpClient httpClientBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(RW_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(RW_TIMEOUT, TimeUnit.MILLISECONDS)
            .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS));

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        } else {
            builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC));
        }
        return builder.build();
    }

    public static boolean isUserLoggedIn(@NonNull Context context) {
        final SharedPreferences settings = context.getSharedPreferences("login", 0);
        final String login = settings.getString("user", "");
        return StringUtils.isNotEmpty(login);
    }

    /**
     * Check if the user is connected to a network. This can be any network.
     *
     * @param context of the application.
     * @return true if connected or connecting. False otherwise.
     */
    public static boolean isNetworkConnected(@NonNull Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) {
            return false;
        }
        return activeNetwork.isConnectedOrConnecting();
    }

    @NonNull
    public static File makeOrGetPictureDirectory(@NonNull Context context) {
        // determine the profile directory
        File dir = context.getFilesDir();

        if (isExternalStorageWritable()) {
            dir = context.getExternalFilesDir(null);
        }
        File picDir = new File(dir, "Pictures");
        if (picDir.exists()) {
            return picDir;
        }
        // creates the directory if not present yet
        final boolean mkdir = picDir.mkdirs();
        if (!mkdir) {
            Log.e(Utils.class.getSimpleName(), "Can create dir " + picDir);
        }

        return picDir;
    }

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static Uri getOutputPicUri(Context context) {
        return Uri.fromFile(new File(Utils.makeOrGetPictureDirectory(context), System.currentTimeMillis() + ".jpg"));
    }

    @NonNull
    public static CharSequence getClickableText(String text, String urlParameter, SearchType type, Activity activity, CustomTabsIntent customTabsIntent) {
        ClickableSpan clickableSpan;
        String url = SearchTypeUrls.getUrl(type);

        if (url == null) {
            clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    ProductBrowsingListActivity.start(activity, text, type);
                }
            };
        } else {
            Uri uri = Uri.parse(url + urlParameter);
            clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View textView) {
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent, uri, new WebViewFallback());
                }
            };
        }

        SpannableString spannableText = new SpannableString(text);
        spannableText.setSpan(clickableSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableText;
    }

    /**
     * Function which returns true if the battery level is low
     *
     * @param context the context
     * @return true if battery is low or false if battery in not low
     */
    public static boolean isBatteryLevelLow(@NonNull Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        if (batteryStatus == null) {
            throw new IllegalStateException("cannot get battery level");
        }

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = (level / (float) scale) * 100;
        Log.i("BATTERYSTATUS", String.valueOf(batteryPct));

        return Math.ceil(batteryPct) <= 15;
    }

    public static boolean isDisableImageLoad(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("disableImageLoad", false);
    }

    /**
     * Function to open ContinuousScanActivity to facilitate scanning
     *
     * @param activity
     */
    public static void scan(@NonNull Activity activity) {

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest
                .permission.CAMERA)) {
                new MaterialDialog.Builder(activity)
                    .title(R.string.action_about)
                    .content(R.string.permission_camera)
                    .neutralText(R.string.txtOk)
                    .show().setOnDismissListener(dialogInterface -> ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    Utils.MY_PERMISSIONS_REQUEST_CAMERA));
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest
                    .permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
            Intent intent = new Intent(activity, ContinuousScanActivity.class);
            activity.startActivity(intent);
        }
    }

    /**
     * @param context The context
     * @return Returns the version name of the app
     */
    @NonNull
    public static String getVersionName(@Nullable Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            Log.e(Utils.class.getSimpleName(), "getVersionName", e);
            return "(version unknown)";
        }
    }

    /**
     * @param type Type of call (Search or Scan)
     * @return Returns the header to be put in network call
     */
    @NonNull
    public static String getUserAgent(@NonNull String type) {
        return getUserAgent() + " " + type;
    }

    @NonNull
    public static String getUserAgent() {
        return BuildConfig.APP_NAME + " Official Android App " + BuildConfig.VERSION_NAME;
    }

    /**
     * @param response Takes a string
     * @return Returns a Json object
     */
    @Nullable
    public static JSONObject createJsonObject(@NonNull String response) {
        try {
            return new JSONObject(response);
        } catch (JSONException e) {
            Log.e(Utils.class.getSimpleName(), "createJsonObject", e);
            return null;
        }
    }

    @Contract(pure = true)
    @Nullable
    @SafeVarargs
    public static <T> T firstNotNull(@Nullable T... args) {
        if (args != null) {
            for (T arg : args) {
                if (arg != null) {
                    return arg;
                }
            }
        }
        return null;
    }

    @Nullable
    public static String firstNotEmpty(String... args) {
        for (String arg : args) {
            if (arg != null && arg.length() > 0) {
                return arg;
            }
        }
        return null;
    }

    @NonNull
    public static String getModifierNonDefault(String modifier) {
        return modifier.equals(Modifier.DEFAULT_MODIFIER) ? "" : modifier;
    }

    public static int dpsToPixel(int dps, @Nullable Activity activity) {
        if (activity == null) {
            return 0;
        }
        final float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    /**
     * Ask to login before editing product
     */
    public static void startLoginToEditAnd(int requestCode, @Nullable Activity activity) {
        if (activity == null) {
            return;
        }
        new MaterialDialog.Builder(activity)
            .title(R.string.sign_in_to_edit)
            .positiveText(R.string.txtSignIn)
            .negativeText(R.string.dialog_cancel)
            .onPositive((dialog, which) -> {
                Intent intent = new Intent(activity, LoginActivity.class);

                activity.startActivityForResult(intent, requestCode);
                dialog.dismiss();
            })
            .onNegative((dialog, which) -> dialog.dismiss())
            .build().show();
    }

    public static boolean isAllGranted(@NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        for (int result : grantResults) {
            if (result != PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllGranted(@NonNull Map<String, Boolean> grantResults) {
        return grantResults.containsValue(false);
    }
}

