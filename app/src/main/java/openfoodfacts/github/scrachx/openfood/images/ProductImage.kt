package openfoodfacts.github.scrachx.openfood.images

import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.ProductImageField.*
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import java.io.File

class ProductImage(
        code: String,
        field: ProductImageField,
        image: File,
        val language: String?
) {
    val code: RequestBody = RequestBody.create(OpenFoodAPIClient.MIME_TEXT, code)
    val field: RequestBody = RequestBody.create(OpenFoodAPIClient.MIME_TEXT, "${field}_$language")
    var imgFront: RequestBody? = null
    var imgIngredients: RequestBody? = null
    var imgNutrition: RequestBody? = null
    var imgPackaging: RequestBody? = null
    var imgOther: RequestBody? = null
    var filePath: String? = null
    val barcode: String?
    val imageField: ProductImageField

    constructor(code: String, field: ProductImageField, image: File) : this(code, field, image, getLanguage(OFFApplication._instance))

    companion object {
        fun createImageRequest(image: File): RequestBody = RequestBody.create(MediaType.parse("image/*"), image)
    }

    init {
        when (field) {
            FRONT -> imgFront = createImageRequest(image)
            INGREDIENTS -> imgIngredients = createImageRequest(image)
            NUTRITION -> imgNutrition = createImageRequest(image)
            PACKAGING -> imgPackaging = createImageRequest(image)
            OTHER -> imgOther = createImageRequest(image)
        }
        barcode = code
        imageField = field
    }
}