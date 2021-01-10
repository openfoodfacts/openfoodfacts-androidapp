package openfoodfacts.github.scrachx.openfood.images

import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import java.io.File

class ProductImage(code: String, field: ProductImageField, image: File, val language: String?) {
    val code: RequestBody = RequestBody.create(MediaType.parse(OpenFoodAPIClient.MIME_TEXT), code)
    val field: RequestBody = RequestBody.create(MediaType.parse(OpenFoodAPIClient.MIME_TEXT), "${field}_$language")
    var imgFront: RequestBody? = null
    var imgIngredients: RequestBody? = null
    var imgNutrition: RequestBody? = null
    var imgPackaging: RequestBody? = null
    var imgOther: RequestBody? = null
    var filePath: String? = null
    val barcode: String?
    val imageField: ProductImageField

    constructor(code: String, field: ProductImageField, image: File) : this(code, field, image, getLanguage(OFFApplication.instance))

    companion object {
        fun createImageRequest(image: File): RequestBody = RequestBody.create(MediaType.parse("image/*"), image)
    }

    init {
        when (field) {
            ProductImageField.FRONT -> {
                imgFront = createImageRequest(image)
                imgIngredients = null
                imgNutrition = null
                imgPackaging = null
                imgOther = null
            }
            ProductImageField.INGREDIENTS -> {
                imgIngredients = createImageRequest(image)
                imgFront = null
                imgNutrition = null
                imgPackaging = null
                imgOther = null
            }
            ProductImageField.NUTRITION -> {
                imgNutrition = createImageRequest(image)
                imgFront = null
                imgIngredients = null
                imgPackaging = null
                imgOther = null
            }
            ProductImageField.PACKAGING -> {
                imgNutrition = null
                imgFront = null
                imgIngredients = null
                imgPackaging = createImageRequest(image)
                imgOther = null
            }
            ProductImageField.OTHER -> {
                imgOther = createImageRequest(image)
                imgNutrition = null
                imgFront = null
                imgIngredients = null
                imgPackaging = null
            }
        }
        barcode = code
        imageField = field
    }
}