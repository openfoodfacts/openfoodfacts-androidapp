package openfoodfacts.github.scrachx.openfood;

import android.support.test.runner.AndroidJUnit4;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4.class)
public class TakeScreenshotMainActivityTest extends AbstractScreenshotTest {
    @Rule
    public ScreenshotActivityTestRule<MainActivity> activityRule = new ScreenshotActivityTestRule<>(MainActivity.class);

    @Test
    public void testTakeScreenshotMainActivity() {
        startForAllLocales(activityRule);
    }
}
