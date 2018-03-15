package openfoodfacts.github.scrachx.openfood.utils;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.util.HashMap;

import openfoodfacts.github.scrachx.openfood.BuildConfig;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.ADDITIVE;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.ALLERGEN;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.BRAND;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.CATEGORY;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.CONTRIBUTOR;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.COUNTRY;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.EMB;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.LABEL;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.PACKAGING;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.SEARCH;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.STORE;
import static openfoodfacts.github.scrachx.openfood.utils.SearchType.TRACE;

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


    HashMap<String, String> URLS = new HashMap<String, String>() {{
        put(ALLERGEN, BuildConfig.OFWEBSITE + "allergen/");
        put(EMB, BuildConfig.OFWEBSITE + "packager-code/");
        put(TRACE, BuildConfig.OFWEBSITE + "trace/");
    }};

}
