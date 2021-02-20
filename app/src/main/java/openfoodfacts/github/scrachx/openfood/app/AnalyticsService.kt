package openfoodfacts.github.scrachx.openfood.app

import io.sentry.Sentry
import openfoodfacts.github.scrachx.openfood.BuildConfig

object AnalyticsService {
    fun init() = Sentry.configureScope { it.setTag("flavor", BuildConfig.FLAVOR) }

    fun setBarcode(barcode: String) = setTag("barcode", barcode)

    fun setTag(key: String, value: String) = Sentry.setTag(key, value)

    fun record(exception: Throwable) = Sentry.captureException(exception)
}
