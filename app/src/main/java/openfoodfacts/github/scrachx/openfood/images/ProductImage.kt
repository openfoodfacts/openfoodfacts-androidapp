package openfoodfacts.github.scrachx.openfood.images

import openfoodfacts.github.scrachx.openfood.models.ImageType
import openfoodfacts.github.scrachx.openfood.utils.asImageRequest
import openfoodfacts.github.scrachx.openfood.utils.asRequestBody
import java.io.File

class ProductImage(
    val barcode: String,
    val imageField: ImageType,
    imageBytes: ByteArray,
    val language: String?
) {

    constructor(
        code: String,
        field: ImageType,
        image: File,
        language: String?
    ) : this(code, field, image.readBytes(), language)

    constructor(
        code: String,
        field: ImageType,
        image: File,
        language: String?,
        filePath: String?
    ) : this(code, field, image.readBytes(), language) {
        this.filePath = filePath
    }

    val barcodeBody = barcode.asRequestBody()
    val fieldBody = "${imageField}_$language".asRequestBody()
    var body = imageBytes.asImageRequest()
    var filePath: String? = null

}
