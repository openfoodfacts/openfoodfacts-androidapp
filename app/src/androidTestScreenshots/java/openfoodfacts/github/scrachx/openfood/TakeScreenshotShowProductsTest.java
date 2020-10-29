package openfoodfacts.github.scrachx.openfood;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductState;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotParameter;
import openfoodfacts.github.scrachx.openfood.features.HistoryScanActivity;
import openfoodfacts.github.scrachx.openfood.features.OFFApplication;
import openfoodfacts.github.scrachx.openfood.features.product.ProductActivity;

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4.class)
public class TakeScreenshotShowProductsTest extends AbstractScreenshotTest {
    @Rule
    public ScreenshotActivityTestRule<HistoryScanActivity> activityHistoryRule =
        new ScreenshotActivityTestRule<>(HistoryScanActivity.class);
    @Rule
    public ScreenshotActivityTestRule<ProductActivity> activityShowProductRule =
        new ScreenshotActivityTestRule<>(ProductActivity.class);

    private static Intent createProductIntent(String productCode) {
        Intent intent = new Intent(OFFApplication.getInstance(), ProductActivity.class);
        ProductState st = new ProductState();
        Product pd = new Product();
        pd.setCode(productCode);
        st.setProduct(pd);
        intent.putExtra("state", st);
        intent.putExtra(ACTION_NAME, ProductActivity.class.getSimpleName() + "-" + productCode);
        return intent;
    }

    @Test
    public void testTakeScreenshot() {
        for (ScreenshotParameter screenshotParameter : localeProvider.getFilteredParameters()) {
            List<Intent> intents = new ArrayList<>();
            for (String product : screenshotParameter.getProductCodes()) {
                intents.add(createProductIntent(product));
            }
            startScreenshotActivityTestRule(screenshotParameter, activityShowProductRule, intents);
            startScreenshotActivityTestRule(screenshotParameter, activityHistoryRule);
        }
    }
}
