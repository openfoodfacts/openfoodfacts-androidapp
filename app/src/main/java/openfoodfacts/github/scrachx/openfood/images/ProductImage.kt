package openfoodfacts.github.scrachx.openfood.images

import android.content.Context
import android.net.Uri
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.models.Barcode
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.utils.MediaTypes.MIME_TEXT
import java.io.File


data class ProductImage(
    val barcode: Barcode,
    val field: ProductImageField,
    val language: String?,
    val bytes: ByteArray,
    val filePath: String? = null,
) {

    @Deprecated("Use the constructor with the filePath parameter instead")
    constructor(
        code: String,
        field: ProductImageField,
        imageFile: File,
        language: String?,
    ) : this(
        barcode = Barcode(code),
        field = field,
        language = language,
        bytes = imageFile.readBytes(),
        filePath = imageFile.absolutePath,
    )

    constructor(
        code: String,
        field: ProductImageField,
        imageUri: Uri,
        language: String?,
        context: Context,
    ) : this(
        barcode = Barcode(code),
        field = field,
        language = language,
        filePath = imageUri.path,
        bytes = context.contentResolver
            .openInputStream(imageUri)!!.use { it.readBytes() },
    )

    fun getBarcodeBody(): RequestBody = RequestBody.create(MIME_TEXT, barcode.raw)
    fun getFieldBody(): RequestBody = RequestBody.create(MIME_TEXT, "${field}_$language")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductImage

        if (barcode != other.barcode) return false
        if (field != other.field) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (language != other.language) return false
        if (filePath != other.filePath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = barcode.hashCode()
        result = 31 * result + field.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (filePath?.hashCode() ?: 0)
        return result
    }
}
