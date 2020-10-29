package openfoodfacts.github.scrachx.openfood;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.app.OFFApplication;
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity;
import openfoodfacts.github.scrachx.openfood.features.scanhistory.ScanHistoryActivity;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductState;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotParameter;

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4.class)
public class TakeScreenshotShowProductsTest extends AbstractScreenshotTest {
    @Rule
    public ScreenshotActivityTestRule<ScanHistoryActivity> activityHistoryRule =
        new ScreenshotActivityTestRule<>(ScanHistoryActivity.class);
    @Rule
    public ScreenshotActivityTestRule<ProductViewActivity> activityShowProductRule =
        new ScreenshotActivityTestRule<>(ProductViewActivity.class);

    @NonNull
    private static Intent createProductIntent(String productCode) {
        Intent intent = new Intent(OFFApplication.getInstance(), ProductViewActivity.class);
        ProductState st = new ProductState();
        Product pd = new Product();
        pd.setCode(productCode);
        st.setProduct(pd);
        intent.putExtra("state", st);
        intent.putExtra(ACTION_NAME, ProductViewActivity.class.getSimpleName() + "-" + productCode);
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
