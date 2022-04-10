package openfoodfacts.github.scrachx.openfood.models

import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import java.util.*

fun HistoryProduct(
    barcode: String,
    title: String?,
    imageUrl: String?,
    details: Map<String, String?>
) = HistoryProduct(
    title,
    details[ApiFields.Keys.BRANDS],
    imageUrl,
    barcode,
    details[ApiFields.Keys.QUANTITY],
    details[ApiFields.Keys.NUTRITION_GRADE_FR],
    details[ApiFields.Keys.ECOSCORE],
    details[ApiFields.Keys.NOVA_GROUPS]
)

fun HistoryProduct(product: OfflineSavedProduct) = HistoryProduct(
    barcode = product.barcode,
    title = product.name,
    imageUrl = product.imageFrontLocalUrl,
    details = product.productDetails
)

fun OfflineSavedProduct.toHistoryProduct() = HistoryProduct(this)

private fun HistoryProduct(
    product: Product,
    language: String
) = HistoryProduct(
    product.productName,
    product.brands,
    product.getImageSmallUrl(language),
    product.code,
    product.quantity,
    product.nutritionGradeFr,
    product.ecoscore,
    product.novaGroups
)

fun Product.toHistoryProduct(language: String) = HistoryProduct(this, language)

fun HistoryProduct.getProductBrandsQuantityDetails(): String {
    return buildString {
        if (!brands.isNullOrEmpty()) {
            val firstBrand = brands.split(",").first()
            append(firstBrand.trim { it <= ' ' }.replaceFirstChar { it.titlecase(Locale.ROOT) })
        }
        if (!quantity.isNullOrEmpty()) {
            append(" - ")
            append(quantity)
        }
    }
}