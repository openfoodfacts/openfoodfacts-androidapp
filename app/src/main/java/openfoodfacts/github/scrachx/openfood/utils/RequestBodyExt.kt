package openfoodfacts.github.scrachx.openfood.utils

import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.models.Fields
import java.io.File

val MIME_TEXT: MediaType = MediaType.get("text/plain")
private val MIME_IMG: MediaType = MediaType.get("image/*")

fun String.asRequestBody(mediaType: MediaType = MIME_TEXT): RequestBody = RequestBody.create(mediaType, this)

fun File.asRequestBody(mediaType: MediaType): RequestBody = RequestBody.create(mediaType, this)
fun File.asImageRequest() = asRequestBody(MIME_IMG)

fun ByteArray.asRequestBody(mediaType: MediaType): RequestBody = RequestBody.create(mediaType, this)
fun ByteArray.asImageRequest() = asRequestBody(MIME_IMG)

fun Fields.asRequestBody(): Map<String, RequestBody> {
    val map = mutableMapOf<String, RequestBody>()

    for ((key, value) in this) {
        if (value == null) continue
        map[key] = value.asRequestBody()
    }

    return map
}