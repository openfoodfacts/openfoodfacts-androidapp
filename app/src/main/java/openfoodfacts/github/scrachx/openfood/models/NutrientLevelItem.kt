package openfoodfacts.github.scrachx.openfood.models

import androidx.annotation.DrawableRes

data class NutrientLevelItem(
    val category: String,
    val value: String,
    val label: String,
    @DrawableRes val icon: Int
)