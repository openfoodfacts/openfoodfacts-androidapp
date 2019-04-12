package openfoodfacts.github.scrachx.openfood;

import android.Manifest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.util.Log;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import tools.fastlane.screengrab.FalconScreenshotStrategy;
import tools.fastlane.screengrab.Screengrab;

import java.util.Arrays;
import java.util.Locale;

/**
 * Take screenshots...
 */
@RunWith(JUnit4.class)
public class TakeScreenshotMainActivityTest {
    private static final String LOG_TAG = TakeScreenshotMainActivityTest.class.getSimpleName();

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CHANGE_CONFIGURATION);
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class, false, false);
    private FileWritingScreenshotCallback callback = new FileWritingScreenshotCallback();
    private static Locale initLocale;

    public static Iterable<Locale> getLocales() {
        return Arrays.asList(Locale.FRENCH, Locale.ENGLISH, Locale.ITALY);
    }

    @Test
    public void testTakeScreenshot() {
        for (Locale locale : getLocales()) {
            testTakeScreenshot(locale);
        }
    }

    public void testTakeScreenshot(Locale locale) {
        Log.d(LOG_TAG, "Start screenshots for locale "+locale);
        LocaleHelper.setLocale(locale);
        activityRule.launchActivity(null);
        Screengrab.screenshot("mainactivity", new FalconScreenshotStrategy(activityRule.getActivity()), callback);
        activityRule.finishActivity();
    }

    @AfterClass
    public static void resetLanguage() {
        LocaleHelper.setLocale(initLocale);
    }

    @BeforeClass
    public static void initLanguage() {
        initLocale = LocaleHelper.getLocale();
    }
}
