package openfoodfacts.github.scrachx.openfood.models

import androidx.annotation.IntegerRes

data class NutrientLevelItem(
        val category: String,
        val value: String,
        val label: String,
        @IntegerRes val icon: Int
)