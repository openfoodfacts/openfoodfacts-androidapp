package openfoodfacts.github.scrachx.openfood.utils;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.util.HashMap;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static openfoodfacts.github.scrachx.openfood.utils.ClickableType.ALLERGEN;
import static openfoodfacts.github.scrachx.openfood.utils.ClickableType.CATEGORY;
import static openfoodfacts.github.scrachx.openfood.utils.ClickableType.EMB;
import static openfoodfacts.github.scrachx.openfood.utils.ClickableType.LABEL;
import static openfoodfacts.github.scrachx.openfood.utils.ClickableType.STORE;
import static openfoodfacts.github.scrachx.openfood.utils.ClickableType.TRACE;

/**
 * Created by Lobster on 10.03.18.
 */

@Retention(SOURCE)
@IntDef({
        EMB,
        CATEGORY,
        LABEL,
        ALLERGEN,
        TRACE,
        STORE
})
public @interface ClickableType {

    int EMB = 1;
    int CATEGORY = 2;
    int LABEL = 3;
    int ALLERGEN = 4;
    int TRACE = 5;
    int STORE = 6;

    HashMap<Integer, String> URLS = new HashMap<Integer, String>() {{
        put(CATEGORY, "https://world.openfoodfacts.org/category/");
        put(LABEL, "https://world.openfoodfacts.org/label/");
        put(EMB, "https://world.openfoodfacts.org/packager-code/");
        put(ALLERGEN, "https://world.openfoodfacts.org/allergen/");
        put(TRACE, "https://world.openfoodfacts.org/trace/");
        put(STORE, "https://world.openfoodfacts.org/store/");
    }};

}
