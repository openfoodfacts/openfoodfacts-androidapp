package openfoodfacts.github.scrachx.openfood.images

import android.graphics.Rect
import org.apache.commons.lang3.builder.ToStringBuilder

class ImageTransformation(
        var rotationInDegree: Int = 0,
        var cropRectangle: Rect? = null
) {
    var imageUrl: String? = null
    var imageId: String? = null

    override fun toString() = ToStringBuilder(this)
            .append(rotationInDegree)
            .append(cropRectangle)
            .append(imageUrl)
            .toString()


    override fun hashCode(): Int {
        var result = rotationInDegree
        result = 31 * result + if (cropRectangle != null) cropRectangle.hashCode() else 0
        result = 31 * result + if (imageUrl != null) imageUrl.hashCode() else 0
        return result
    }

    fun isEmpty() = imageUrl.isNullOrBlank()
    fun isNotEmpty(): Boolean = !isEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as ImageTransformation
        if (rotationInDegree != that.rotationInDegree) {
            return false
        }
        if (if (cropRectangle != null) cropRectangle != that.cropRectangle else that.cropRectangle != null) {
            return false
        }
        return if (imageUrl != null) imageUrl == that.imageUrl else that.imageUrl == null
    }

}

