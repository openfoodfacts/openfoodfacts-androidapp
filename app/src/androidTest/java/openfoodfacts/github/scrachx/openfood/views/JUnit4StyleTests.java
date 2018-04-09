package openfoodfacts.github.scrachx.openfood.views;

import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Locale;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.views.splash.SplashActivity;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

/**
 * <p>Created by lgzaiti on 27/03/18 <br/>
 */
@RunWith(JUnit4.class)
public class JUnit4StyleTests {
    @Rule
    public LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void beforeAll() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());
    }

    @Before
    public void setUp() throws Exception {
//        final WelcomeActivity activity = activityRule.getActivity();
//        PrefManager prefManager = new PrefManager(activity);
//
//        prefManager.setFirstTimeLaunch(true);
    }

    @Test
    public void testTakeScreenshot() {
//        onView(withId(R.id.action_search)).check(matches(isDisplayed()));

        Screengrab.screenshot("beforeFabClick");

        onView(withId(R.id.action_search)).perform(click());
//        Locale current = activityRule.getActivity().getResources().getConfiguration().locale;
//        assertEquals("FR", current.getCountry());

//        try {
//            Thread.sleep(5000L);
//        } catch (InterruptedException e) {
//
//        }
        Screengrab.screenshot("afterFabClick");
    }

}
