package openfoodfacts.github.scrachx.openfood.utils;

import android.os.Build;

public class CompatibiltyUtils {

    private CompatibiltyUtils(){

    }


    public static boolean isOnScrollChangeListenerAvailable(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
