package openfoodfacts.github.scrachx.openfood.images

import com.fasterxml.jackson.databind.JsonNode

/**
 * @receiver json representing images entries given by api/v0/product/XXXX.json?fields=images
 */
internal fun JsonNode.extractImagesNameSortedByUploadTimeDesc(): List<String> {
    // a json object referring to images
    return this["product"]["images"]?.fields()
        ?.asSequence()
        ?.toList().orEmpty()
        .mapNotNull { (imageName, value) ->
            // do not include images with contain nutrients, ingredients or other in their names
            // as they are duplicate and do not load as well
            if (!isNameOk(imageName)) null
            else NameTimestampKey(imageName, value["uploaded_t"].asLong())
        }
        .sorted()
        .map { it.name }
}

internal fun isNameOk(name: String) =
    name.isNotBlank() && !Regex("[nfio]").containsMatchIn(name)

internal data class NameTimestampKey(
    val name: String,
    private val timestamp: Long
) : Comparable<NameTimestampKey> {

    /**
     * to be ordered from newer to older.
     */
    override fun compareTo(other: NameTimestampKey) = (other.timestamp - timestamp).let {
        when {
            it > 0 -> 1
            it < 0 -> -1
            else -> name.compareTo(other.name)
        }
    }
}