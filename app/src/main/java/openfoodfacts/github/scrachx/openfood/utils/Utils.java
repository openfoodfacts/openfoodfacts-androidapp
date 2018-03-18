package openfoodfacts.github.scrachx.openfood.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.jobs.SavedProductUploadJob;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;

import static android.text.TextUtils.isEmpty;

public class Utils {

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    public static final int MY_PERMISSIONS_REQUEST_STORAGE = 2;
    public static final String UPLOAD_JOB_TAG = "upload_saved_product_job";
    public static boolean isUploadJobInitialised;

    /**
     * Returns a CharSequence that concatenates the specified array of CharSequence
     * objects and then applies a list of zero or more tags to the entire range.
     *
     * @param content an array of character sequences to apply a style to
     * @param tags    the styled span objects to apply to the content
     *                such as android.text.style.StyleSpan
     */
    private static CharSequence apply(CharSequence[] content, Object... tags) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        openTags(text, tags);
        for (CharSequence item : content) {
            text.append(item);
        }
        closeTags(text, tags);
        return text;
    }

    /**
     * Iterates over an array of tags and applies them to the beginning of the specified
     * Spannable object so that future text appended to the text will have the styling
     * applied to it. Do not call this method directly.
     */
    private static void openTags(Spannable text, Object[] tags) {
        for (Object tag : tags) {
            text.setSpan(tag, 0, 0, Spannable.SPAN_MARK_MARK);
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
    public static CharSequence bold(CharSequence... content) {
        return apply(content, new StyleSpan(Typeface.BOLD));
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static String compressImage(String url) {
        File fileFront = new File(url);
        Bitmap bt = decodeFile(fileFront);
        if (bt == null) {
            Log.e("COMPRESS_IMAGE", url + " not found");
            return null;
        }

        File smallFileFront = new File(url.replace(".png", "_small.png"));
        OutputStream fOutFront = null;
        try {
            fOutFront = new FileOutputStream(smallFileFront);
            bt.compress(Bitmap.CompressFormat.PNG, 100, fOutFront);
        } catch (IOException e) {
            Log.e("COMPRESS_IMAGE", e.getMessage(), e);
        } finally {
            if (fOutFront != null) {
                try {
                    fOutFront.flush();
                    fOutFront.close();
                } catch (IOException e) {
                    // nothing to do
                }
            }
        }
        return smallFileFront.toString();
    }

    public static int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    // Decodes image and scales it to reduce memory consumption
    public static Bitmap decodeFile(File f) {
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
        }
        return null;
    }

    /**
     * Check if a certain application is installed on a device.
     *
     * @param context     the applications context.
     * @param packageName the package name that you want to check.
     * @return true if the application is installed, false otherwise.
     */

    public static boolean isApplicationInstalled(Context context, String packageName) {
        //private boolean isApplicationInstalled(Context context, String packageName) {
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

    public static int getImageGrade(String grade) {
        int drawable;

        if (grade == null) {
            return R.drawable.ic_error;
        }

        switch (grade.toLowerCase(Locale.getDefault())) {
            case "a":
                drawable = R.drawable.nnc_a;
                break;
            case "b":
                drawable = R.drawable.nnc_b;
                break;
            case "c":
                drawable = R.drawable.nnc_c;
                break;
            case "d":
                drawable = R.drawable.nnc_d;
                break;
            case "e":
                drawable = R.drawable.nnc_e;
                break;
            default:
                drawable = R.drawable.ic_error;
                break;
        }

        return drawable;
    }

    public static int getSmallImageGrade(String grade) {
        int drawable;

        if (grade == null) {
            return R.drawable.ic_error;
        }

        switch (grade.toLowerCase(Locale.getDefault())) {
            case "a":
                drawable = R.drawable.nnc_small_a;
                break;
            case "b":
                drawable = R.drawable.nnc_small_b;
                break;
            case "c":
                drawable = R.drawable.nnc_small_c;
                break;
            case "d":
                drawable = R.drawable.nnc_small_d;
                break;
            case "e":
                drawable = R.drawable.nnc_small_e;
                break;
            default:
                drawable = R.drawable.ic_error;
                break;
        }

        return drawable;
    }

    public static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId) {
        Drawable drawable = AppCompatResources.getDrawable(context, drawableId);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable
                .getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Return a round float value with 2 decimals
     *
     * @param value float value
     * @return round value or 0 if the value is empty or equals to 0
     */
    public static String getRoundNumber(String value) {
        if ("0".equals(value)) {
            return value;
        }

        if (isEmpty(value)) {
            return "?";
        }

        String[] strings = value.split("\\.");
        if (strings.length == 1 || (strings.length == 2 && strings[1].length() <= 2)) {
            return value;
        }

        return String.format(Locale.getDefault(), "%.2f", Double.valueOf(value));
    }

    public static DaoSession getAppDaoSession(Activity activity) {
        return ((OFFApplication) activity.getApplication()).getDaoSession();
    }


    public static DaoSession getDaoSession(Context context) {
        return OFFApplication.daoSession;
    }

    /**
     * Check if the device has a camera installed.
     *
     * @return true if installed, false otherwise.
     */
    public static boolean isHardwareCameraInstalled(Context context) {
        try {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                return true;
            }
        } catch (NullPointerException e) {
            if (BuildConfig.DEBUG) Log.i(context.getClass().getSimpleName(), e.toString());
            return false;
        }
        return false;
    }

    /**
     * Schedules job to download when network is available
     */

    synchronized public static void scheduleProductUploadJob(Context context) {
        if (isUploadJobInitialised) return;
        final int periodicity = (int) TimeUnit.MINUTES.toSeconds(30);
        final int toleranceInterval = (int) TimeUnit.MINUTES.toSeconds(5);
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);
        Job uploadJob = jobDispatcher.newJobBuilder()
                .setService(SavedProductUploadJob.class)
                .setTag(UPLOAD_JOB_TAG)
                .setConstraints(Constraint.ON_UNMETERED_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(false)
                .setTrigger(Trigger.executionWindow(periodicity, periodicity + toleranceInterval))
                .setReplaceCurrent(false)
                .build();
        jobDispatcher.schedule(uploadJob);
        isUploadJobInitialised = true;
    }

    public static OkHttpClient HttpClientBuilder() {
        OkHttpClient httpClient;
        if (Build.VERSION.SDK_INT == 24) {
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .cipherSuites(CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                    .build();

            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(5000, TimeUnit.MILLISECONDS)
                    .readTimeout(30000, TimeUnit.MILLISECONDS)
                    .writeTimeout(30000, TimeUnit.MILLISECONDS)
                    .connectionSpecs(Collections.singletonList(spec))
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();
        } else {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(5000, TimeUnit.MILLISECONDS)
                    .readTimeout(30000, TimeUnit.MILLISECONDS)
                    .writeTimeout(30000, TimeUnit.MILLISECONDS)
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();
        }
        return httpClient;
    }

    /**
     * Check if airplane mode is turned on on the device.
     *
     * @param context of the application.
     * @return true if airplane mode is active.
     */
    @SuppressWarnings("depreciation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAirplaneModeActive(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    /**
     * Check if the user is connected to a network. This can be any network.
     *
     * @param context of the application.
     * @return true if connected or connecting. False otherwise.
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Check if the user is connected to a mobile network.
     *
     * @param context of the application.
     * @return true if connected to mobile data.
     */
    public static boolean isConnectedToMobileData(Context context) {
        return getNetworkType(context).equals("Mobile");
    }

    /**
     * Get the type of network that the user is connected to.
     *
     * @param context of the application.
     * @return the type of network that is connected.
     */
    private static String getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_ETHERNET:
                    return "Ethernet";
                case ConnectivityManager.TYPE_MOBILE:
                    return "Mobile";
                case ConnectivityManager.TYPE_VPN:
                    return "VPN";
                case ConnectivityManager.TYPE_WIFI:
                    return "WiFi";
                case ConnectivityManager.TYPE_WIMAX:
                    return "WiMax";
            }
        }

        return "Other";
    }

    public static CharSequence getClickableText(String text, String urlParameter, @SearchType String type, Activity activity, CustomTabsIntent customTabsIntent) {
        ClickableSpan clickableSpan;
        String url = SearchType.URLS.get(type);

        if (url == null) {
            clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    ProductBrowsingListActivity.startActivity(activity, text, type);
                }
            };
        } else {
            Uri uri = Uri.parse(url + urlParameter);
            clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent, uri, new WebViewFallback());
                }
            };
        }

        SpannableString spannableText = new SpannableString(text);
        spannableText.setSpan(clickableSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableText;

    }
}
