package openfoodfacts.github.scrachx.openfood;

import android.support.test.annotation.UiThreadTest;
import android.support.test.runner.AndroidJUnit4;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4.class)
public class TakeScreenshotMainActivityTest extends AbstractScreenshotTest {
    @Rule
    public ScreenshotActivityTestRule<MainActivity> activityRule = new ScreenshotActivityTestRule<>(MainActivity.class);

    @Test
    public void testTakeScreenshot() {
        startForAllLocales(activityRule);
    }
}
