package openfoodfacts.github.scrachx.openfood

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import openfoodfacts.github.scrachx.openfood.features.MainActivity
import openfoodfacts.github.scrachx.openfood.features.welcome.WelcomeActivity
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Take screenshots...
 */
//@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TakeScreenshotMainActivityTest : AbstractScreenshotTest() {


    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @ApplicationContext
    lateinit var context: Context


    @Rule @JvmField
    var activityRule = ScreenshotActivityTestRule(MainActivity::class.java, context = context)

    @Rule @JvmField
    var welcomeActivityRule = ScreenshotActivityTestRule(WelcomeActivity::class.java, context = context)

    @Test
    fun testTakeScreenshotMainActivity() {
        welcomeActivityRule.firstTimeLaunched = true
        startForAllLocales(rules = listOf(welcomeActivityRule, activityRule), context = context)
    }
}
