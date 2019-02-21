package openfoodfacts.github.scrachx.openfood.utils;

import android.support.annotation.StringDef;
import openfoodfacts.github.scrachx.openfood.BuildConfig;

import java.lang.annotation.Retention;
import java.util.HashMap;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.*;

/**
 * Created by Lobster on 10.03.18.
 */

@Retention(SOURCE)
@StringDef({
        ADDITIVE,
        ALLERGEN,
        BRAND,
        CATEGORY,
        COUNTRY,
        EMB,
        LABEL,
        PACKAGING,
        SEARCH,
        STORE,
        TRACE,
        CONTRIBUTOR,
        STATE,
        INCOMPLETE_PRODUCT,
        ORIGIN,
        MANUFACTURING_PLACE
})
public @interface SearchType {

    String ADDITIVE = "additive";
    String ALLERGEN = "allergen";
    String BRAND = "brand";
    String CATEGORY = "category";
    String COUNTRY = "country";
    String EMB = "emb";
    String LABEL = "label";
    String PACKAGING = "packaging";
    String SEARCH = "search";
    String STORE = "store";
    String TRACE = "trace";
    String CONTRIBUTOR = "contributor";
    String INCOMPLETE_PRODUCT = "incomplete_product";
    String STATE = "state";
    String ORIGIN = "origin";
    String MANUFACTURING_PLACE = "manufacturing-place";

    HashMap<String, String> URLS = new HashMap<String, String>() {{
        put(ALLERGEN, BuildConfig.OFWEBSITE + "allergens/");
        put(EMB, BuildConfig.OFWEBSITE + "packager-code/");
        put(TRACE, BuildConfig.OFWEBSITE + "trace/");
    }};

}
