package openfoodfacts.github.scrachx.openfood.models

data class SaveItem(
        var title: String? = null,
        val fieldsCompleted: Int = 0,
        var url: String? = null,
        var barcode: String? = null,
        var weight: String? = null,
        var brand: String? = null
)
