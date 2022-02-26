package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.Utils.NO_DRAWABLE_RESOURCE
import java.util.*

suspend fun OfflineSavedProduct.toState(client: ProductRepository): ProductState = client.getProductStateFull(barcode.asBarcode())

suspend fun OfflineSavedProduct.toOnlineProduct(client: ProductRepository) = toState(client).product

fun Product.isPerServingInLiter() = servingSize?.contains(MeasurementUnit.UNIT_LITER.sym, true)

fun SearchProduct.getProductBrandsQuantityDetails() = StringBuilder().apply {
    brands?.takeIf { it.isNotEmpty() }?.let { brandStr ->
        append(brandStr.split(",").first().trim { it <= ' ' }.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() })
    }
    if (!quantity.isNullOrEmpty()) {
        append(" - ")
        append(quantity)
    }
}.toString()

suspend fun SearchProduct.toProduct(client: ProductRepository): Product? = client.getProductStateFull(barcode).product

@DrawableRes
private fun getResourceFromEcoscore(ecoscore: String?) = when (ecoscore?.lowercase(Locale.ROOT)) {
    "a" -> R.drawable.ic_ecoscore_a
    "b" -> R.drawable.ic_ecoscore_b
    "c" -> R.drawable.ic_ecoscore_c
    "d" -> R.drawable.ic_ecoscore_d
    "e" -> R.drawable.ic_ecoscore_e
    else -> R.drawable.ic_ecoscore_unknown
}

@DrawableRes
fun SearchProduct?.getEcoscoreResource() = getResourceFromEcoscore(this?.ecoscore)

@DrawableRes
fun HistoryProduct?.getEcoscoreResource() = getResourceFromEcoscore(this?.ecoscore)


@DrawableRes
private fun getResourceFromNutriScore(
    nutriscore: String?,
    vertical: Boolean,
) = when (nutriscore?.lowercase(Locale.ROOT)) {
    "a" -> if (vertical) R.drawable.ic_nutriscore_vertical_border_a else R.drawable.ic_nutriscore_a
    "b" -> if (vertical) R.drawable.ic_nutriscore_vertical_border_b else R.drawable.ic_nutriscore_b
    "c" -> if (vertical) R.drawable.ic_nutriscore_vertical_border_c else R.drawable.ic_nutriscore_c
    "d" -> if (vertical) R.drawable.ic_nutriscore_vertical_border_d else R.drawable.ic_nutriscore_d
    "e" -> if (vertical) R.drawable.ic_nutriscore_vertical_border_e else R.drawable.ic_nutriscore_e
    else -> if (vertical) NO_DRAWABLE_RESOURCE else R.drawable.ic_nutriscore_unknown
}

@DrawableRes
fun SearchProduct?.getNutriScoreResource(vertical: Boolean = false) = getResourceFromNutriScore(this?.nutritionGradeFr, vertical)

@DrawableRes
fun HistoryProduct.getNutriScoreResource(vertical: Boolean = false) = getResourceFromNutriScore(nutritionGrade, vertical)

fun Product?.getImageGradeDrawable(context: Context, vertical: Boolean = false): Drawable? {
    val gradeID = this.getNutriScoreResource(vertical)
    return if (gradeID == NO_DRAWABLE_RESOURCE) null
    else VectorDrawableCompat.create(context.resources, gradeID, context.theme)
}

@DrawableRes
private fun getResourceFromNova(novaGroup: String?) = when (novaGroup) {
    "1" -> R.drawable.ic_nova_group_1
    "2" -> R.drawable.ic_nova_group_2
    "3" -> R.drawable.ic_nova_group_3
    "4" -> R.drawable.ic_nova_group_4
    else -> R.drawable.ic_nova_group_unknown
}

/**
 * Returns the NOVA group graphic asset given the group
 */
@DrawableRes
fun SearchProduct?.getNovaGroupResource() = getResourceFromNova(this?.novaGroups)

@DrawableRes
fun HistoryProduct?.getNovaGroupResource() = getResourceFromNova(this?.novaGroup)

internal fun Product.isProductIncomplete() = this.let {
    it.imageFrontUrl == null
            || it.imageFrontUrl == ""
            || it.quantity == null
            || it.quantity == ""
            || it.productName == null
            || it.productName == ""
            || it.brands == null
            || it.brands == ""
            || it.ingredientsText == null
            || it.ingredientsText == ""
}

