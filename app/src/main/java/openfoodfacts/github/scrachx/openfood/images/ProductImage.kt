package openfoodfacts.github.scrachx.openfood.images

import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.models.ImageType
import openfoodfacts.github.scrachx.openfood.models.ImageType.*
import openfoodfacts.github.scrachx.openfood.utils.asImageRequest
import openfoodfacts.github.scrachx.openfood.utils.asRequestBody
import java.io.File

class ProductImage(
    val barcode: String,
    val imageField: ImageType,
    imageBytes: ByteArray,
    val language: String?
) {

    constructor(code: String, field: ImageType, image: File, language: String?) :
            this(code, field, image.readBytes(), language)

    val barcodeBody = barcode.asRequestBody()
    val fieldBody = "${imageField}_$language".asRequestBody()

    var imgFront: RequestBody? = null
    var imgIngredients: RequestBody? = null
    var imgNutrition: RequestBody? = null
    var imgPackaging: RequestBody? = null
    var imgOther: RequestBody? = null

    var filePath: String? = null

    init {
        when (imageField) {
            FRONT -> imgFront = imageBytes.asImageRequest()
            INGREDIENTS -> imgIngredients = imageBytes.asImageRequest()
            NUTRITION -> imgNutrition = imageBytes.asImageRequest()
            PACKAGING -> imgPackaging = imageBytes.asImageRequest()
            OTHER -> imgOther = imageBytes.asImageRequest()
        }
    }

}
