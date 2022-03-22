package openfoodfacts.github.scrachx.openfood.utils

import androidx.annotation.StringRes
import openfoodfacts.github.scrachx.openfood.R

enum class SortType(@StringRes val stringRes: Int) {
    NONE(0),
    TIME(R.string.by_time),
    BARCODE(R.string.by_barcode),
    BRAND(R.string.by_brand),
    GRADE(R.string.by_nutrition_grade),
    TITLE(R.string.by_title),
}