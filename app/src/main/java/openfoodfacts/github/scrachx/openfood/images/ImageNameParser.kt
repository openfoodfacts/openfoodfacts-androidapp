package openfoodfacts.github.scrachx.openfood.images

import com.fasterxml.jackson.databind.JsonNode
import openfoodfacts.github.scrachx.openfood.models.ValueAndTimestamp


object ImageNameParser {
    /**
     * @return a sorted by timestamp [List] of [ValueAndTimestamp] that have the
     * image name as key.
     *
     * The list is sorted in descending order (first newer images).
     */
    internal fun extractImageNames(jsonNode: JsonNode): List<ValueAndTimestamp<String>> {
        return jsonNode["product"]["images"]?.fields()
            ?.asSequence()
            ?.toList().orEmpty()
            .filter {
                // Do not include images with contain nutrients,
                // ingredients or other in their names. They are duplicates and
                // sometimes they do not have the `uploaded_t` field and make
                // the mapping throw a NPE
                isValidImageName(it.key)
            }
            .map { ValueAndTimestamp(it.value["uploaded_t"].asLong(), it.key) }
            .sortedByTimestampDescending()

    }

    internal fun isValidImageName(name: String) =
        name.isNotBlank() && !Regex("[nfio]").containsMatchIn(name)

    private fun <T> List<ValueAndTimestamp<T>>.sortedByTimestampDescending() =
        sortedByDescending { it.timestamp }
}



