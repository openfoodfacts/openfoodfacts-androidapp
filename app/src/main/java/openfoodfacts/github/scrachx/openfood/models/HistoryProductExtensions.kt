@file:JvmName("HistoryProductExtensions")

package openfoodfacts.github.scrachx.openfood.models

import java.util.*

fun HistoryProduct.getProductBrandsQuantityDetails(): String {
    return StringBuilder()
            .apply {
                if (!brands.isNullOrEmpty()) {
                    append(brands.split(",").first().trim { it <= ' ' }.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() })
                }
                if (!quantity.isNullOrEmpty()) {
                    append(" - ")
                    append(quantity)
                }
            }
            .toString()
}
