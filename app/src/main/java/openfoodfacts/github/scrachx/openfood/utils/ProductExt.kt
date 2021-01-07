package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.Units
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.Utils.NO_DRAWABLE_RESOURCE
import java.util.*

fun OfflineSavedProduct.toState(context: Context) = OpenFoodAPIClient(context).getProductStateFull(barcode)

fun OfflineSavedProduct.toOnlineProduct(context: Context) = toState(context).map { it.product }

fun Product.isPerServingInLiter() = servingSize?.contains(Units.UNIT_LITER, true)

@DrawableRes
fun Product?.getCO2Resource(): Int {
    if (this == null) return NO_DRAWABLE_RESOURCE

    val tags = environmentImpactLevelTags
    if (tags.isNullOrEmpty()) return NO_DRAWABLE_RESOURCE

    return when (tags[0].replace("\"", "")) {
        "en:high" -> R.drawable.ic_co2_high_24dp
        "en:low" -> R.drawable.ic_co2_low_24dp
        "en:medium" -> R.drawable.ic_co2_medium_24dp
        else -> NO_DRAWABLE_RESOURCE
    }
}

@DrawableRes
fun Product?.getEcoscoreResource() = when (this?.ecoscore?.toLowerCase(Locale.ROOT)) {
    "a" -> R.drawable.ic_ecoscore_a
    "b" -> R.drawable.ic_ecoscore_b
    "c" -> R.drawable.ic_ecoscore_c
    "d" -> R.drawable.ic_ecoscore_d
    "e" -> R.drawable.ic_ecoscore_e
    else -> R.drawable.ic_ecoscore_unknown
}

@DrawableRes
fun Product?.getNutriScoreResource(vertical: Boolean = false) = when (this?.nutritionGradeFr?.toLowerCase(Locale.ROOT)) {
    "a" -> if (vertical) R.drawable.ic_nutriscore_vertical_border_a else R.drawable.ic_nutriscore_a
    "b" -> if (vertical) R.drawable.ic_nutriscore_vertical_border_b else R.drawable.ic_nutriscore_b
    "c" -> if (vertical) R.drawable.ic_nutriscore_vertical_border_c else R.drawable.ic_nutriscore_c
    "d" -> if (vertical) R.drawable.ic_nutriscore_vertical_border_d else R.drawable.ic_nutriscore_d
    "e" -> if (vertical) R.drawable.ic_nutriscore_vertical_border_e else R.drawable.ic_nutriscore_e
    else -> if (vertical) NO_DRAWABLE_RESOURCE else R.drawable.ic_nutriscore_unknown
}

fun Product?.getImageGradeDrawable(context: Context, vertical: Boolean = false): Drawable? {
    val gradeID = this.getNutriScoreResource(vertical)
    return if (gradeID == NO_DRAWABLE_RESOURCE) null
    else VectorDrawableCompat.create(context.resources, gradeID, context.theme)
}

/**
 * Returns the NOVA group graphic asset given the group
 */
@DrawableRes
fun Product?.getNovaGroupResource() = when (this?.novaGroups) {
    "1" -> R.drawable.ic_nova_group_1
    "2" -> R.drawable.ic_nova_group_2
    "3" -> R.drawable.ic_nova_group_3
    "4" -> R.drawable.ic_nova_group_4
    else -> R.drawable.ic_nova_group_unknown
}