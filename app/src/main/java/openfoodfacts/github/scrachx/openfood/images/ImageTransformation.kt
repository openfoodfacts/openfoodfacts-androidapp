package openfoodfacts.github.scrachx.openfood.images

import android.graphics.Rect

data class ImageTransformation(
    val rotationInDegree: Int = 0,
    val cropRectangle: Rect? = null,
    var imageUrl: String? = null,
    var imageId: String? = null,
) {
    fun isUrlEmpty() = imageUrl.isNullOrBlank()

    fun toMap(): Map<String, String> = buildMap {
        this[ANGLE] = rotationInDegree.toString()

        if (cropRectangle != null) {
            this[LEFT] = cropRectangle.left.toString()
            this[RIGHT] = cropRectangle.right.toString()
            this[TOP] = cropRectangle.top.toString()
            this[BOTTOM] = cropRectangle.bottom.toString()
        }
    }
}
