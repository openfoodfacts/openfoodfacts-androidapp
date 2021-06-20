package openfoodfacts.github.scrachx.openfood.analytics

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SentryAnalytics @Inject constructor(
        @ApplicationContext private val context: Context,
        private val sharedPreferences: SharedPreferences,
) {

    // isCrashReportingEnabled is not dynamic, as sentry can not be enabled / disabled, so it takes the value at startup, and changes will only be taken into account after an app restart
    private val isCrashReportingEnabled by lazy {
        sharedPreferences.getBoolean(context.getString(R.string.pref_crash_reporting_key), true)
    }

    init {
        if (isCrashReportingEnabled) {
            SentryAndroid.init(context)
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
