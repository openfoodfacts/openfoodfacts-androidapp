package openfoodfacts.github.scrachx.openfood.utils;

import openfoodfacts.github.scrachx.openfood.BuildConfig;

import java.util.HashMap;

import static openfoodfacts.github.scrachx.openfood.utils.SearchType.*;

public class SearchTypeUrls {
   private static final HashMap<String, String> URLS = new HashMap<>();

    static {
        URLS.put(ALLERGEN, BuildConfig.OFWEBSITE + "allergens/");
        URLS.put(EMB, BuildConfig.OFWEBSITE + "packager-code/");
        URLS.put(TRACE, BuildConfig.OFWEBSITE + "trace/");
    }


    public static String  getUrl( @SearchType String type){
        return URLS.get(type);
    }
}
