package openfoodfacts.github.scrachx.openfood

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidTest
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule
import openfoodfacts.github.scrachx.openfood.test.ScreenshotParameter
import openfoodfacts.github.scrachx.openfood.test.getFilteredParameters
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import java.util.*

/**
 * Take screenshots...buil
 */
//@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
abstract class AbstractScreenshotTest {


    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CHANGE_CONFIGURATION
    )

    private fun startScreenshotActivityTestRules(
            screenshotParameter: ScreenshotParameter,
            activityRules: List<ScreenshotActivityTestRule<out Activity?>>,
            intents: List<Intent?>
    ) {
        changeLocale(screenshotParameter)
        activityRules.forEach { activityRule ->
            intents.forEach { intent ->
                activityRule.finishActivity()
                intent?.getStringExtra(ACTION_NAME)?.let { title -> activityRule.name = title }
                activityRule.screenshotParameter = screenshotParameter
                activityRule.launchActivity(intent)
            }

        }
    }

    private fun changeLocale(parameter: ScreenshotParameter) {
        Log.d(LOG_TAG, "Change parameters to $parameter")
        LocaleHelper.setContextLanguage(ApplicationProvider.getApplicationContext(), parameter.locale)
    }

    protected fun startForAllLocales(
            filter: (ScreenshotParameter) -> List<Intent?> = { listOf(null) },
            rules: List<ScreenshotActivityTestRule<out Activity?>>
    ) {
        getFilteredParameters().forEach {
            startScreenshotActivityTestRules(it, rules, filter(it))
        }
    }

    companion object {
        const val ACTION_NAME = "actionName"
        private val LOG_TAG = AbstractScreenshotTest::class.java.simpleName
        private lateinit var initLocale: Locale

        @BeforeClass
        fun initLanguage() {
            initLocale = LocaleHelper.getLocaleFromContext()
        }

        @AfterClass
        fun resetLanguage() {
            LocaleHelper.setLanguageInPrefs(initLocale)
        }
    }
}
