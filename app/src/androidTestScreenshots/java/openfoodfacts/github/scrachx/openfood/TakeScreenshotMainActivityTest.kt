package openfoodfacts.github.scrachx.openfood

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import openfoodfacts.github.scrachx.openfood.features.MainActivity
import openfoodfacts.github.scrachx.openfood.features.welcome.WelcomeActivity
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * Take screenshots...
 */
//@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TakeScreenshotMainActivityTest : AbstractScreenshotTest() {


    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    @ApplicationContext
    lateinit var context: Context


    @get:Rule
    var activityRule = ScreenshotActivityTestRule(MainActivity::class.java, context = context)

    @get:Rule
    var welcomeActivityRule = ScreenshotActivityTestRule(WelcomeActivity::class.java, context = context)

    @Test
    fun testTakeScreenshotMainActivity() {
        welcomeActivityRule.firstTimeLaunched = true
        startForAllLocales(rules = listOf(welcomeActivityRule, activityRule), context = context)
    }
}
