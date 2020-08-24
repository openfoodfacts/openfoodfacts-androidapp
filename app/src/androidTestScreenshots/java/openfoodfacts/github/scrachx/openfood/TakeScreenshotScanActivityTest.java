package openfoodfacts.github.scrachx.openfood;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule;
import openfoodfacts.github.scrachx.openfood.views.scan.ContinuousScanActivity;

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4.class)
public class TakeScreenshotScanActivityTest extends AbstractScreenshotTest {
    public static final int MS_TO_WAIT_TO_DISPLAY_PRODUCT_IN_SCAN = 2000;
    @Rule
    public ScreenshotActivityTestRule<ContinuousScanActivity> activityRule =
        new ScreenshotActivityTestRule<>(ContinuousScanActivity.class);

    @Test
    public void testTakeScreenshotScanActivity() {
        activityRule.setAfterActivityLaunchedAction(screenshotActivityTestRule -> {
            try {
                screenshotActivityTestRule.runOnUiThread(() -> {
                    final String barcode = screenshotActivityTestRule
                        .getScreenshotParameter().getProductCodes().get(0);
                    screenshotActivityTestRule.getActivity()
                        .showProduct(barcode);
                });
                Thread.sleep(MS_TO_WAIT_TO_DISPLAY_PRODUCT_IN_SCAN);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        startForAllLocales(activityRule);
    }
}
