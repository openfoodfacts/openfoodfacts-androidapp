package openfoodfacts.github.scrachx.openfood;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import openfoodfacts.github.scrachx.openfood.app.OFFApplication;
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule;
import openfoodfacts.github.scrachx.openfood.utils.SearchInfo;

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4.class)
public class TakeScreenshotIncompleteProductsTest extends AbstractScreenshotTest {
    @Rule
    public ScreenshotActivityTestRule<ProductSearchActivity> incompleteRule = new ScreenshotActivityTestRule<>(ProductSearchActivity.class, "incompleteProducts",
        createSearchIntent(SearchInfo.emptySearchInfo()));

    @NonNull
    private static Intent createSearchIntent(SearchInfo searchInfo) {
        Intent intent = new Intent(OFFApplication.getInstance(), ProductSearchActivity.class);
        intent.putExtra(ProductSearchActivity.SEARCH_INFO, searchInfo);
        return intent;
    }

    @Test
    public void testTakeScreenshot() {
        startForAllLocales(incompleteRule);
    }
}
