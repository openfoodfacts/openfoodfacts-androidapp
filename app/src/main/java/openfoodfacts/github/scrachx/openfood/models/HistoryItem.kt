package openfoodfacts.github.scrachx.openfood.models

import java.util.*

data class HistoryItem(
        var title: String?,
        var brands: String?,
        var url: String?,
        internal var barcode: String,
        var time: Date?,
        var quantity: String?,
        var nutritionGrade: String?
)