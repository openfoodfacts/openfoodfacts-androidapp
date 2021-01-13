package openfoodfacts.github.scrachx.openfood

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule
import openfoodfacts.github.scrachx.openfood.test.ScreenshotParameter
import openfoodfacts.github.scrachx.openfood.test.ScreenshotsLocaleProvider
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.runner.RunWith
import java.util.*

/**
 * Take screenshots...buil
 */
@RunWith(AndroidJUnit4::class)
abstract class AbstractScreenshotTest {

    @Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CHANGE_CONFIGURATION
    )

    @SafeVarargs
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

    private fun changeLocale(parameter: ScreenshotParameter, context: Context = OFFApplication.instance) {
        Log.d(LOG_TAG, "Change parameters to $parameter")
        LocaleHelper.setContextLanguage(context, parameter.locale)
    }

    protected fun startForAllLocales(
            filter: (ScreenshotParameter) -> List<Intent?> = { listOf(null) },
            rules: List<ScreenshotActivityTestRule<out Activity?>>
    ) {
        ScreenshotsLocaleProvider.getFilteredParameters().forEach {
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