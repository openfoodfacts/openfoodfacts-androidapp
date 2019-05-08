package openfoodfacts.github.scrachx.openfood;

import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotParameter;
import openfoodfacts.github.scrachx.openfood.views.HistoryScanActivity;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.product.ProductActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4.class)
public class TakeScreenshotShowProductsTest extends AbstractScreenshotTest {
    @Rule
    public ScreenshotActivityTestRule<ProductActivity> activityShowProductRule = new ScreenshotActivityTestRule<>(ProductActivity.class);
    @Rule
    public ScreenshotActivityTestRule<HistoryScanActivity> historyTestRule = new ScreenshotActivityTestRule<>(HistoryScanActivity.class);

    private static Intent createProductIntent(String productCode) {
        Intent intent = new Intent(OFFApplication.getInstance(), ProductActivity.class);
        State st = new State();
        Product pd = new Product();
        pd.setCode(productCode);
        st.setProduct(pd);
        intent.putExtra("state", st);
        intent.putExtra(ACTION_NAME, ProductActivity.class.getSimpleName() + "-" + productCode);
        return intent;
    }

    @Test
    public void testTakeScreenshot() {
        for (ScreenshotParameter screenshotParameter : localeProvider.getParameters()) {
            List<Intent> intents = new ArrayList<>();
            for (String product : screenshotParameter.getProductCodes()) {
                intents.add(createProductIntent(product));
            }
            startScreenshotActivityTestRules(screenshotParameter, activityShowProductRule, intents);
            startScreenshotActivityTestRules(screenshotParameter, historyTestRule);
        }
    }
}
