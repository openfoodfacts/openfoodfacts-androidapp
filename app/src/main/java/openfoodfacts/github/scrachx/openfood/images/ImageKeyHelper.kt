package openfoodfacts.github.scrachx.openfood.images

import android.os.Bundle
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.Barcode
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


private const val BASE_IMAGES_URL = BuildConfig.STATICURL + "/images/products"
fun getImageUrl(barcode: Barcode, imageName: String, size: String): String {
    val rawBarcode = barcode.raw

    val barcodePattern =
        if (rawBarcode.length <= 8) rawBarcode
        else buildString {
            append(rawBarcode.substring(0, 3))
            append("/")
            append(rawBarcode.substring(3, 7))
            append("/")
            append(rawBarcode.substring(7, 11))
            append("/")
            append(rawBarcode.substring(11))
        }

    return "$BASE_IMAGES_URL/$barcodePattern/$imageName.$size.jpg"
}

fun getImageUrl(barcode: Barcode, imageName: String, size: Int) =
    getImageUrl(barcode, imageName, size.toString())

const val IMAGE_URL = "imageurl"
const val IMAGE_FILE = "imagefile"
const val IMAGE_TYPE = "imageType"
const val PRODUCT_BARCODE = "code"
const val PRODUCT = "product"
const val LANGUAGE = "language"
const val IMAGE_STRING_ID = "id"
const val IMG_ID = "imgid"
const val IMAGE_EDIT_SIZE = 400
