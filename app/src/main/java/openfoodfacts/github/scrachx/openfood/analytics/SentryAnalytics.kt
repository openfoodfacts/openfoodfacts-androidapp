package openfoodfacts.github.scrachx.openfood.analytics

import androidx.preference.PreferenceManager
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication


object SentryAnalytics {

    // isCrashReportingEnabled is not dynamic, as sentry can not be enabled / disabled, so it takes the value at startup, and changes will only be taken into account after an app restart
    private val isCrashReportingEnabled by lazy {
        PreferenceManager.getDefaultSharedPreferences(OFFApplication._instance)
                .getBoolean(OFFApplication._instance.getString(R.string.pref_crash_reporting_key), true)
    }

    fun init() {
        if (isCrashReportingEnabled) {
            SentryAndroid.init(OFFApplication._instance)
            Sentry.configureScope { scope ->
                scope.setTag("flavor", BuildConfig.FLAVOR)
            }
        }
    }

    fun setBarcode(barcode: String) {
        setTag("barcode", barcode)
    }

    fun setTag(key: String, value: String) {
        if (isCrashReportingEnabled) {
            Sentry.setTag(key, value)
        }
    }

    fun record(exception: Throwable) {
        if (isCrashReportingEnabled) {
            Sentry.captureException(exception)
        }
    }
}
