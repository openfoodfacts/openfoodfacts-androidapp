package openfoodfacts.github.scrachx.openfood.images

import android.os.Bundle
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import org.apache.commons.lang.StringUtils
import org.jetbrains.annotations.Contract

object ImageKeyHelper {
    const val IMAGE_URL = "imageurl"
    const val IMAGE_FILE = "imagefile"
    const val IMAGE_TYPE = "imageType"
    const val PRODUCT_BARCODE = "code"
    const val PRODUCT = "product"
    const val LANGUAGE = "language"
    const val IMAGE_STRING_ID = "id"
    const val IMG_ID = "imgid"
    const val IMAGE_EDIT_SIZE = "400"
    const val IMAGE_EDIT_SIZE_FILE = ".$IMAGE_EDIT_SIZE"
    fun getImageStringKey(field: ProductImageField, product: Product): String {
        return getImageStringKey(field, product.lang)
    }

    @JvmStatic
    fun getImageStringKey(field: ProductImageField, language: String): String {
        return "${field}_$language"
    }

    @JvmStatic
    fun getLanguageCodeFromUrl(field: ProductImageField?, url: String?): String? {
        return if (url.isNullOrBlank() || field == null) null
        else
            StringUtils.substringBefore(StringUtils.substringAfterLast(url, field.toString() + "_"), ".")
    }

    fun createImageBundle(imageType: ProductImageField?, product: Product?, language: String?, imageUrl: String?): Bundle {
        val bundle = Bundle()
        bundle.putString(IMAGE_URL, imageUrl)
        if (product != null) {
            bundle.putSerializable(PRODUCT, product)
            bundle.putSerializable(IMAGE_TYPE, imageType)
            bundle.putString(LANGUAGE, language)
        }
        return bundle
    }

    @JvmStatic
    @Contract(pure = true)
    fun getResourceIdForEditAction(field: ProductImageField) = when (field) {
        ProductImageField.FRONT -> R.string.edit_front_image
        ProductImageField.NUTRITION -> R.string.edit_nutrition_image
        ProductImageField.PACKAGING -> R.string.edit_packaging_image
        ProductImageField.INGREDIENTS -> R.string.edit_ingredients_image
        else -> R.string.edit_other_image
    }

    @JvmStatic
    @Contract(pure = true)
    fun getResourceId(field: ProductImageField) = when (field) {
        ProductImageField.FRONT -> R.string.front_short_picture
        ProductImageField.NUTRITION -> R.string.nutrition_facts
        ProductImageField.INGREDIENTS -> R.string.ingredients
        ProductImageField.PACKAGING -> R.string.packaging
        else -> R.string.other_picture
    }

    @JvmStatic
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
        return "$baseUrlString$barcodePattern/$imageName$size.jpg"
    }
}