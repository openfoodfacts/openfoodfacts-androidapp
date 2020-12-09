package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import androidx.annotation.DrawableRes
import io.reactivex.Single
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.models.Units
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient

fun OfflineSavedProduct.toState(context: Context): Single<ProductState> {
    return OpenFoodAPIClient(context).getProductStateFull(barcode)
}

fun OfflineSavedProduct.toOnlineProduct(context: Context): Single<Product> {
    return toState(context).map { obj: ProductState -> obj.product }
}

fun Product.isPerServingInLiter() = servingSize?.contains(Units.UNIT_LITER, true)

@DrawableRes
fun Product?.getCO2Drawable(): Int {
    if (this == null) return Utils.NO_DRAWABLE_RESOURCE

    val tags = environmentImpactLevelTags
    if (tags.isNullOrEmpty()) return Utils.NO_DRAWABLE_RESOURCE

    return when (tags[0].replace("\"", "")) {
        "en:high" -> R.drawable.ic_co2_high_24dp
        "en:low" -> R.drawable.ic_co2_low_24dp
        "en:medium" -> R.drawable.ic_co2_medium_24dp
        else -> Utils.NO_DRAWABLE_RESOURCE
    }
}

@DrawableRes
fun Product?.getNutriScoreSmallDrawable() = when {
    this == null -> getNutriScoreSmallDrawable(null as String?)
    // Prefer the global tag to the FR tag
    getNutritionGradeTag() != null -> getNutriScoreSmallDrawable(getNutritionGradeTag())
    else -> getNutriScoreSmallDrawable(nutritionGradeFr)
}

@DrawableRes
fun Product?.getEcoscoreDrawable() = when (this?.ecoscore) {
    "a" -> R.drawable.ic_ecoscore_a
    "b" -> R.drawable.ic_ecoscore_b
    "c" -> R.drawable.ic_ecoscore_c
    "d" -> R.drawable.ic_ecoscore_d
    "e" -> R.drawable.ic_ecoscore_e
    else -> Utils.NO_DRAWABLE_RESOURCE
}

@DrawableRes
fun Product?.getNutriScoreDrawable() = getNutriScoreDrawable(this?.nutritionGradeFr)
fun Product?.getImageGradeDrawable(context: Context) = getImageGradeDrawable(context, this?.nutritionGradeFr)

/**
 * Returns the NOVA group graphic asset given the group
 */
@DrawableRes
fun Product?.getNovaGroupDrawable() = getNovaGroupDrawable(this?.novaGroups)