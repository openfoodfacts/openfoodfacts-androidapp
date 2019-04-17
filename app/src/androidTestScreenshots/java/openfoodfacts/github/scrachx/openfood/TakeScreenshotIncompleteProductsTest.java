package openfoodfacts.github.scrachx.openfood;

import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule;
import openfoodfacts.github.scrachx.openfood.utils.SearchInfo;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4.class)
public class TakeScreenshotIncompleteProductsTest extends AbstractScreenshotTest {
    @Rule
    public ScreenshotActivityTestRule<ProductBrowsingListActivity> incompleteRule = new ScreenshotActivityTestRule<>(ProductBrowsingListActivity.class, "incompleteProducts",
        createSearchIntent(SearchInfo.emptySearchInfo()));

    private static Intent createSearchIntent(SearchInfo searchInfo) {
        Intent intent = new Intent(OFFApplication.getInstance(), ProductBrowsingListActivity.class);
        intent.putExtra(ProductBrowsingListActivity.SEARCH_INFO, searchInfo);
        return intent;
    }

    @Test
    public void testTakeScreenshot() {
        startForAllLocales(incompleteRule);
    }
}
