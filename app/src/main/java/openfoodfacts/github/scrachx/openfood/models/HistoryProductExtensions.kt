@file:JvmName("HistoryProductExtensions")

package openfoodfacts.github.scrachx.openfood.models

import java.util.*

fun HistoryProduct.getProductBrandsQuantityDetails(): String {
    return StringBuilder()
            .apply {
                if (!brands.isNullOrEmpty()) {
                    append(brands.split(",").first().trim { it <= ' ' }.capitalize(Locale.ROOT))
                }
                if (!quantity.isNullOrEmpty()) {
                    append(" - ")
                    append(quantity)
                }
            }
            .toString()
}
