package openfoodfacts.github.scrachx.openfood.utils;

import java.util.HashMap;

import openfoodfacts.github.scrachx.openfood.BuildConfig;

import static openfoodfacts.github.scrachx.openfood.utils.SearchType.ALLERGEN;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.EMB;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.TRACE;

public class SearchTypeUrls {
    private static final HashMap<String, String> URLS = new HashMap<>();

    private SearchTypeUrls() {

    }

    static {
        URLS.put(ALLERGEN, BuildConfig.OFWEBSITE + "allergens/");
        URLS.put(EMB, BuildConfig.OFWEBSITE + "packager-code/");
        URLS.put(TRACE, BuildConfig.OFWEBSITE + "trace/");
    }

    public static String getUrl(@SearchType String type) {
        return URLS.get(type);
    }
}
