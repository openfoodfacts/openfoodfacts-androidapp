package openfoodfacts.github.scrachx.openfood.utils

import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.utils.SearchType

object SearchTypeUrls {
    private val URLS = mapOf(
            SearchType.ALLERGEN to "${BuildConfig.OFWEBSITE}allergens/",
            SearchType.EMB to "${BuildConfig.OFWEBSITE}packager-code/",
            SearchType.TRACE to "${BuildConfig.OFWEBSITE}trace/"
    )

    fun getUrl(type: SearchType) = URLS[type]
}