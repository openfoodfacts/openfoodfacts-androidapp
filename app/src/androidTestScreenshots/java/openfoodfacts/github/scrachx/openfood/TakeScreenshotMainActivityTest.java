package openfoodfacts.github.scrachx.openfood;

import android.content.res.Configuration;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import junit.framework.Assert;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.PrefManager;
import openfoodfacts.github.scrachx.openfood.views.WelcomeActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4.class)
public class TakeScreenshotMainActivityTest extends AbstractScreenshotTest {
    @Rule
    public ScreenshotActivityTestRule<MainActivity> activityRule = new ScreenshotActivityTestRule<>(MainActivity.class);
    //    public ScreenshotActivityTestRule<SplashActivity> splashActivityRule = new ScreenshotActivityTestRule<>(SplashActivity.class);
    public ScreenshotActivityTestRule<WelcomeActivity> welcomeActivityRule = new ScreenshotActivityTestRule<>(WelcomeActivity.class);

    @Test
    public void testTakeScreenshotMainActivity() {
        startForAllLocales(activityRule);
    }

    @Test
    public void testTakeScreenshotWelcomeActivity() {
        //Impossible to change the language...
        welcomeActivityRule.setFirstTimeLaunched(true);
        startForAllLocales(welcomeActivityRule);
    }
}
