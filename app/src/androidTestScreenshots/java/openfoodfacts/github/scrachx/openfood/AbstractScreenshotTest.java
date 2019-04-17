package openfoodfacts.github.scrachx.openfood;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.rule.GrantPermissionRule;
import android.util.Log;
import openfoodfacts.github.scrachx.openfood.fragments.PreferencesFragment;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotParameter;
import openfoodfacts.github.scrachx.openfood.test.ScreenshotsLocaleProvider;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collection;
import java.util.Locale;

/**
 * Take screenshots...
 */
@RunWith(JUnit4.class)
public abstract class AbstractScreenshotTest {
    public static final String ACTION_NAME = "actionName";
    private static final String LOG_TAG = AbstractScreenshotTest.class.getSimpleName();
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CHANGE_CONFIGURATION);
    private static Locale initLocale;
    private static String initCountry;
    ScreenshotsLocaleProvider localeProvider = new ScreenshotsLocaleProvider();

    protected void startScreenshotActivityTestRules(ScreenshotParameter screenshotParameter, ScreenshotActivityTestRule... activityRules) {
        changeLocale(screenshotParameter);
        for (ScreenshotActivityTestRule activityRule : activityRules) {
            activityRule.finishActivity();
            activityRule.setScreenshotParameter(screenshotParameter);
            activityRule.launchActivity(null);
        }
    }

    protected void startScreenshotActivityTestRules(ScreenshotParameter screenshotParameter, ScreenshotActivityTestRule activityRule,
                                                    Collection<Intent> intents) {
        changeLocale(screenshotParameter);
        for (Intent intent : intents) {
            String title = intent.getStringExtra(ACTION_NAME);
            if (title != null) {
                activityRule.setName(title);
            }
            activityRule.setActivityIntent(intent);
            activityRule.setScreenshotParameter(screenshotParameter);
            activityRule.launchActivity(null);
        }
    }

    protected void changeLocale(ScreenshotParameter parameter) {
        Log.d(LOG_TAG, "Change parameters to " + parameter);
        LocaleHelper.setLocale(parameter.getLocale());
        final String countryName = parameter.getCountryTag();
        setCountyInPrefs(countryName);
    }

    private static void setCountyInPrefs(String countryName) {
        SharedPreferences settings = OFFApplication.getInstance().getSharedPreferences("prefs", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PreferencesFragment.USER_COUNTRY_PREFERENCE_KEY, countryName);
        editor.apply();
    }

    public void startForAllLocales(ScreenshotActivityTestRule... activityRule) {
        for (ScreenshotParameter screenshotParameter : localeProvider.getParameters()) {
            startScreenshotActivityTestRules(screenshotParameter, activityRule);
        }
    }

    @AfterClass
    public static void resetLanguage() {
        LocaleHelper.setLocale(initLocale);
        if (initCountry != null) {
            setCountyInPrefs(initCountry);
        }
    }

    @BeforeClass
    public static void initLanguage() {
        initLocale = LocaleHelper.getLocale();
        initCountry = getCountryInPrefs();
    }

    public static String getCountryInPrefs() {
        SharedPreferences settings = OFFApplication.getInstance().getSharedPreferences("prefs", 0);
        return settings.getString(PreferencesFragment.USER_COUNTRY_PREFERENCE_KEY, null);
    }
}
