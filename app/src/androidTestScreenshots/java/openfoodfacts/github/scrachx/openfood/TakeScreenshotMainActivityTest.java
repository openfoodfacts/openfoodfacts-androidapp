package openfoodfacts.github.scrachx.openfood;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.welcome.WelcomeActivity;

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4.class)
public class TakeScreenshotMainActivityTest extends AbstractScreenshotTest {
    @Rule
    public ScreenshotActivityTestRule<MainActivity> activityRule =
        new ScreenshotActivityTestRule<>(MainActivity.class);
    @Rule
    public ScreenshotActivityTestRule<WelcomeActivity> welcomeActivityRule =
        new ScreenshotActivityTestRule<>(WelcomeActivity.class);

    @Test
    public void testTakeScreenshotMainActivity() {
        welcomeActivityRule.setFirstTimeLaunched(true);
        startForAllLocales(welcomeActivityRule, activityRule);
    }
}
