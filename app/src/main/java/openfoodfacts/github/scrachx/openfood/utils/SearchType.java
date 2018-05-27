package org.openfoodfacts.scanner.utils;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.util.HashMap;

import org.openfoodfacts.scanner.BuildConfig;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static org.openfoodfacts.scanner.utils.SearchType.ADDITIVE;
import static org.openfoodfacts.scanner.utils.SearchType.ALLERGEN;
import static org.openfoodfacts.scanner.utils.SearchType.BRAND;
import static org.openfoodfacts.scanner.utils.SearchType.CATEGORY;
import static org.openfoodfacts.scanner.utils.SearchType.CONTRIBUTOR;
import static org.openfoodfacts.scanner.utils.SearchType.COUNTRY;
import static org.openfoodfacts.scanner.utils.SearchType.EMB;
import static org.openfoodfacts.scanner.utils.SearchType.INCOMPLETE_PRODUCT;
import static org.openfoodfacts.scanner.utils.SearchType.LABEL;
import static org.openfoodfacts.scanner.utils.SearchType.PACKAGING;
import static org.openfoodfacts.scanner.utils.SearchType.SEARCH;
import static org.openfoodfacts.scanner.utils.SearchType.STATE;
import static org.openfoodfacts.scanner.utils.SearchType.STORE;
import static org.openfoodfacts.scanner.utils.SearchType.TRACE;

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
        INCOMPLETE_PRODUCT
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


    HashMap<String, String> URLS = new HashMap<String, String>() {{
        put(ALLERGEN, BuildConfig.OFWEBSITE + "allergen/");
        put(EMB, BuildConfig.OFWEBSITE + "packager-code/");
        put(TRACE, BuildConfig.OFWEBSITE + "trace/");
    }};

}
