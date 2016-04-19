package openfoodfacts.github.scrachx.openfood.utils;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import java.util.Locale;

public class Utils {

    public static String getUriByCurrentLanguage() {
        String url;
        if (Locale.getDefault().getLanguage().contains("fr")){
            url = "http://fr.openfoodfacts.org/";
        } else {
            url = "http://world.openfoodfacts.org/";
        }
        return url;
    }

    public static String getUriProductByCurrentLanguage() {
        String url;
        if (Locale.getDefault().getLanguage().contains("fr")){
            url = "http://fr.openfoodfacts.org/produit/";
        } else {
            url = "http://world.openfoodfacts.org/product/";
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

}