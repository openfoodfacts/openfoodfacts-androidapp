package openfoodfacts.github.scrachx.openfood.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public class Utils {

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    public static final int MY_PERMISSIONS_REQUEST_STORAGE= 2;
    private static final String OPEN_FOOD_FACTS_WORLD_URL = "http://world.openfoodfacts.org/";
    private static final String HTTP_FR_OPENFOODFACTS_ORG = "http://fr.openfoodfacts.org/";

    public static String getUriByCurrentLanguage() {
        String url;
        if (Locale.getDefault().getLanguage().contains("fr")){
            url = HTTP_FR_OPENFOODFACTS_ORG;
        } else {
            url = OPEN_FOOD_FACTS_WORLD_URL;
        }
        return url;
    }

    public static String getUriProductByCurrentLanguage() {
        String url;
        if (Locale.getDefault().getLanguage().contains("fr")){
            url = HTTP_FR_OPENFOODFACTS_ORG + "produit/";
        } else {
            url = OPEN_FOOD_FACTS_WORLD_URL + "product/";
        }
        return url;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
            final int REQUIRED_SIZE = 300;

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
     * @param context the applications context.
     * @param packageName the package name that you want to check.
     *
     * @return true if the application is installed, false otherwise.
     */
    public static boolean isApplicationInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            // Check if the package name exists, if exception is thrown, package name does not exist.
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}