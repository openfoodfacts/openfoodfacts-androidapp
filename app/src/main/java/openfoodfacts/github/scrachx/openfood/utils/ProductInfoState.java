
package org.openfoodfacts.scanner.utils;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static org.openfoodfacts.scanner.utils.ProductInfoState.EMPTY;
import static org.openfoodfacts.scanner.utils.ProductInfoState.LOADING;

/**
 * Created by Lobster on 10.03.18.
 */

@Retention(SOURCE)
@StringDef({
        LOADING,
        EMPTY
})
public @interface ProductInfoState {
    String LOADING = "loading";
    String EMPTY = "empty";
}