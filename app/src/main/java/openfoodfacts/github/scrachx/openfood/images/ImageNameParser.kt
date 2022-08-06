package openfoodfacts.github.scrachx.openfood.images

import com.fasterxml.jackson.databind.JsonNode


object ImageNamesParser {
    internal fun extractImageNames(jsonNode: JsonNode): List<TimestampedKey<String>> {
        return jsonNode["product"]["images"]?.fields()
            ?.asSequence()
            ?.toList().orEmpty()
            .map { TimestampedKey(it.value["uploaded_t"].asLong(), it.key) }
            .filter {
                // do not include images with contain nutrients, ingredients or other in their names
                // as they are duplicate and do not load as well
                isNameOk(it.key)
            }
    }

    fun isNameOk(name: String) =
        name.isNotBlank() && !Regex("[nfio]").containsMatchIn(name)
}

internal data class TimestampedKey<T>(
    val timestamp: Long,
    val key: T,
)


internal fun <T> List<TimestampedKey<T>>.sortedByTimestampDescending() =
    sortedByDescending { it.timestamp }

