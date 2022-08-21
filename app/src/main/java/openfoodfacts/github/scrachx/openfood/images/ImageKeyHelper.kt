package openfoodfacts.github.scrachx.openfood.images

import android.os.Bundle
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT
import openfoodfacts.github.scrachx.openfood.models.ProductImageField.INGREDIENTS
import openfoodfacts.github.scrachx.openfood.models.ProductImageField.NUTRITION
import openfoodfacts.github.scrachx.openfood.models.ProductImageField.PACKAGING
import org.jetbrains.annotations.Contract

@Contract(pure = true)
fun getResourceId(field: ProductImageField) = when (field) {
    FRONT -> R.string.front_short_picture
    NUTRITION -> R.string.nutrition_facts
    INGREDIENTS -> R.string.ingredients
    PACKAGING -> R.string.packaging
    else -> R.string.other_picture
}

fun Product.getImageStringKey(field: ProductImageField) = getImageStringKey(field, lang)

fun getImageStringKey(field: ProductImageField, language: String) = "${field}_$language"

fun getLanguageCodeFromUrl(field: ProductImageField?, url: String?): String? {
    return if (url.isNullOrBlank() || field == null) null
    else url.substringAfterLast("${field}_").substringBefore(".")
}

fun createImageBundle(
    imageType: ProductImageField?,
    product: Product?,
    language: String?,
    imageUrl: String,
) = Bundle().apply {
    putString(IMAGE_URL, imageUrl)
    if (product != null) {
        putSerializable(PRODUCT, product)
        putSerializable(IMAGE_TYPE, imageType)
        putString(LANGUAGE, language)
    }
}

@Contract(pure = true)
fun getResourceIdForEditAction(field: ProductImageField) = when (field) {
    FRONT -> R.string.edit_front_image
    NUTRITION -> R.string.edit_nutrition_image
    PACKAGING -> R.string.edit_packaging_image
    INGREDIENTS -> R.string.edit_ingredients_image
    else -> R.string.edit_other_image
}

fun getImageUrl(barcode: String, imageName: String, size: Int) =
    getImageUrl(barcode, imageName, size.toString())

fun getImageUrl(barcode: String, imageName: String, size: String): String {
    val baseUrlString = BuildConfig.STATICURL + "/images/products/"
    var barcodePattern = barcode
    if (barcodePattern.length > 8) {
        barcodePattern = StringBuilder(barcode).let {
            it.insert(3, "/")
            it.insert(7, "/")
            it.insert(11, "/")
            it.toString()
        }

    }
    return "$baseUrlString$barcodePattern/$imageName.$size.jpg"
}

const val IMAGE_URL = "imageurl"
const val IMAGE_FILE = "imagefile"
const val IMAGE_TYPE = "imageType"
const val PRODUCT_BARCODE = "code"
const val PRODUCT = "product"
const val LANGUAGE = "language"
const val IMAGE_STRING_ID = "id"
const val IMG_ID = "imgid"
const val IMAGE_EDIT_SIZE = 400
