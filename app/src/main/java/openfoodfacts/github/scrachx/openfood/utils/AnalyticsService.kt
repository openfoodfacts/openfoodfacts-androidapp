package openfoodfacts.github.scrachx.openfood.utils

import io.sentry.Scope
import io.sentry.Sentry
import openfoodfacts.github.scrachx.openfood.BuildConfig

object AnalyticsService {
    @JvmStatic
    fun init() {
        Sentry.configureScope { scope: Scope -> scope.setTag("flavor", BuildConfig.FLAVOR) }
    }

    fun log(key: String, value: String) {
        Sentry.setTag(key, value)
    }

    fun record(exception: Exception?) {
        Sentry.captureException(exception!!)
    }
}