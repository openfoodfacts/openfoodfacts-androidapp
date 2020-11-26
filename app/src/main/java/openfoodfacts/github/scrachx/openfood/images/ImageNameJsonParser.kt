package openfoodfacts.github.scrachx.openfood.images

import com.fasterxml.jackson.databind.JsonNode
import java.util.*

/**
 * Extract images informations form json.
 */
object ImageNameJsonParser {
    /**
     * @param rootNode json representing images entries given by api/v0/product/XXXX.json?fields=images
     */
    @JvmStatic
    fun extractImagesNameSortedByUploadTimeDesc(rootNode: JsonNode): List<String?> {
        // a json object referring to images
        val imagesNode = rootNode["product"]["images"]
        val namesWithTime = ArrayList<NameUploadedTimeKey>()
        if (imagesNode != null) {
            val images = imagesNode.fields()
            if (images != null) {
                // loop through all the image names and store them in a array list
                while (images.hasNext()) {
                    val image = images.next()
                    val imageName = image.key
                    // do not include images with contain nutrients, ingredients or other in their names
                    // as they are duplicate and do not load as well
                    if (!isNameAccepted(imageName)) {
                        continue
                    }
                    namesWithTime.add(NameUploadedTimeKey(imageName, image.value["uploaded_t"].asLong()))
                }
            }
        }
        return namesWithTime.sorted().map { it.name }
    }

    private fun isNameAccepted(namesString: String): Boolean {
        return (namesString.isNotBlank()
                && !namesString.contains("n")
                && !namesString.contains("f")
                && !namesString.contains("i")
                && !namesString.contains("o"))
    }

    private class NameUploadedTimeKey(
            val name: String?,
            private val timestamp: Long
    ) : Comparable<NameUploadedTimeKey> {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || javaClass != other.javaClass) {
                return false
            }
            val that = other as NameUploadedTimeKey
            if (timestamp != that.timestamp) {
                return false
            }
            return if (name != null) name == that.name else that.name == null
        }

        override fun toString(): String {
            return "NameUploadKey{name='$name', timestamp=$timestamp}"
        }

        override fun hashCode(): Int {
            var result = name?.hashCode() ?: 0
            result = 31 * result + (timestamp xor (timestamp ushr 32)).toInt()
            return result
        }

        /**
         * to be ordered from newer to older.
         */
        override fun compareTo(other: NameUploadedTimeKey): Int {
            val deltaInTime = other.timestamp - timestamp
            if (deltaInTime > 0) {
                return 1
            }
            return if (deltaInTime < 0) {
                -1
            } else name!!.compareTo(other.name!!)
        }
    }
}