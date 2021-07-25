package openfoodfacts.github.scrachx.openfood.images

import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.ProductImageField.*
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import java.io.File

class ProductImage(
        val barcode: String,
        val imageField: ProductImageField,
        imageBytes: ByteArray,
        val language: String?
) {

    constructor(code: String, field: ProductImageField, image: File, language: String?) :
            this(code, field, image.readBytes(), language)

    val barcodeBody: RequestBody = RequestBody.create(OpenFoodAPIClient.MIME_TEXT, barcode)
    val fieldBody: RequestBody = RequestBody.create(OpenFoodAPIClient.MIME_TEXT, "${imageField}_$language")

    var imgFront: RequestBody? = null
    var imgIngredients: RequestBody? = null
    var imgNutrition: RequestBody? = null
    var imgPackaging: RequestBody? = null
    var imgOther: RequestBody? = null

    var filePath: String? = null

    init {
        when (imageField) {
            FRONT -> imgFront = createImageRequest(imageBytes)
            INGREDIENTS -> imgIngredients = createImageRequest(imageBytes)
            NUTRITION -> imgNutrition = createImageRequest(imageBytes)
            PACKAGING -> imgPackaging = createImageRequest(imageBytes)
            OTHER -> imgOther = createImageRequest(imageBytes)
        }
    }

    companion object {
        fun createImageRequest(image: File): RequestBody = RequestBody.create(MediaType.parse("image/*"), image)
        fun createImageRequest(bytes: ByteArray): RequestBody = RequestBody.create(MediaType.parse("image/*"), bytes)
    }

}
