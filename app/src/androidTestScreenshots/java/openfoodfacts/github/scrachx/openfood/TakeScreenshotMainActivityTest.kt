package openfoodfacts.github.scrachx.openfood

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import openfoodfacts.github.scrachx.openfood.features.MainActivity
import openfoodfacts.github.scrachx.openfood.features.welcome.WelcomeActivity
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TakeScreenshotMainActivityTest : AbstractScreenshotTest() {

    @Rule
    var activityRule = ScreenshotActivityTestRule(MainActivity::class.java, context = context, localeManager = localeManager)

    @Rule
    var welcomeActivityRule = ScreenshotActivityTestRule(WelcomeActivity::class.java, context = context, localeManager = localeManager)

    @Test
    fun testTakeScreenshotMainActivity() {
        welcomeActivityRule.firstTimeLaunched = true
        startForAllLocales(rules = listOf(welcomeActivityRule, activityRule), context = context)
    }
}
