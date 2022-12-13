package openfoodfacts.github.scrachx.openfood.utils

import okhttp3.MediaType
import okhttp3.RequestBody

object MediaTypes {
    val MIME_IMAGE = "image/*".toMediaType()
    val MIME_TEXT: MediaType = "text/plain".toMediaType()
}

internal fun String.toRequestBody(mediaType: MediaType = MediaTypes.MIME_TEXT) =
    RequestBody.create(mediaType, this)

internal fun ByteArray.toRequestBody(mediaType: MediaType) =
    RequestBody.create(mediaType, this)

internal fun String.toMediaTypeOrNull() = MediaType.parse(this)
internal fun String.toMediaType() = toMediaTypeOrNull()!!