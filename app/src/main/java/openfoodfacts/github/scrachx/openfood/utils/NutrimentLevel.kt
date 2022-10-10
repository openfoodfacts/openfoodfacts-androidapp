package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.NutrimentLevel

/**
 * Returns the description of this [NutrimentLevel]
 */
fun NutrimentLevel.getDesc(context: Context): String =
    context.getString(getDescRes())

@DrawableRes
fun NutrimentLevel.getImgRes(): Int = when (this) {
    NutrimentLevel.MODERATE -> R.drawable.moderate
    NutrimentLevel.LOW -> R.drawable.low
    NutrimentLevel.HIGH -> R.drawable.high
}

@StringRes
fun NutrimentLevel.getDescRes(): Int = when (this) {
    NutrimentLevel.LOW -> R.string.txtNutritionLevelLow
    NutrimentLevel.MODERATE -> R.string.txtNutritionLevelModerate
    NutrimentLevel.HIGH -> R.string.txtNutritionLevelHigh
}