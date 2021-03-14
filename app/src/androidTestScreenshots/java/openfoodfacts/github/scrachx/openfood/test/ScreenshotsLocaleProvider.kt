package openfoodfacts.github.scrachx.openfood.test

fun getFilteredParameters(predicate: (ScreenshotParameter?) -> Boolean = { true }) =
        ScreenshotParametersProvider.defaults.filter(predicate)