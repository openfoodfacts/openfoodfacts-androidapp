package openfoodfacts.github.scrachx.openfood.images

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.utils.getAsFloat
import openfoodfacts.github.scrachx.openfood.utils.getAsInt
import org.apache.commons.lang3.builder.ToStringBuilder
import kotlin.math.roundToInt

class ImageTransformationUtils {
    /**
     * image rotation in degree
     */
    var rotationInDegree = 0
        private set
    var cropRectangle: Rect? = null
        private set
    var imageUrl: String? = null
        private set
    var imageId: String? = null
        private set

    private constructor()
    constructor(rotationInDegree: Int, cropRectangle: Rect?) {
        this.rotationInDegree = rotationInDegree
        this.cropRectangle = cropRectangle
    }

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
        val that = other as ImageTransformationUtils
        if (rotationInDegree != that.rotationInDegree) {
            return false
        }
        if (if (cropRectangle != null) cropRectangle != that.cropRectangle else that.cropRectangle != null) {
            return false
        }
        return if (imageUrl != null) imageUrl == that.imageUrl else that.imageUrl == null
    }

    companion object {
        private const val NO_VALUE = -1
        private const val LEFT = "x1"
        private const val RIGHT = "x2"
        private const val TOP = "y1"
        private const val BOTTOM = "y2"
        private const val ANGLE = "angle"

        fun addTransformToMap(newServerTransformation: ImageTransformationUtils, imgMap: MutableMap<String, String>) {
            imgMap[ANGLE] = newServerTransformation.rotationInDegree.toString()
            val cropRectangle = newServerTransformation.cropRectangle
            if (cropRectangle != null) {
                imgMap[LEFT] = cropRectangle.left.toString()
                imgMap[RIGHT] = cropRectangle.right.toString()
                imgMap[TOP] = cropRectangle.top.toString()
                imgMap[BOTTOM] = cropRectangle.bottom.toString()
            }
        }

        /**
         * @param product the product
         * @param productImageField the type of the image
         * @param language the language
         * @return the image transformation containing the initial url and the transformation (rotation/crop) for screen
         */
        fun getScreenTransformation(
                product: Product,
                productImageField: ProductImageField,
                language: String
        ): ImageTransformationUtils {
            val res = getInitialServerTransformation(product, productImageField, language)
            if (res.isEmpty()) return res

            // if we want to perform a rotation + a crop, we have to rotate the crop area.
            // Open Food Facts applies the crop on the rotated image and the Android library applies the crop before the rotation... so we should
            // transform the crop from Open Food Facts to the Android library version.
            if (res.cropRectangle != null && res.rotationInDegree != 0) {
                applyRotationOnCropRectangle(product, productImageField, language, res, true)
            }
            return res
        }

        private fun applyRotationOnCropRectangle(
                product: Product,
                productImageField: ProductImageField,
                language: String,
                res: ImageTransformationUtils,
                inverse: Boolean
        ) {
            // if a crop and a rotation is done, we should rotate the cropped rectangle
            val imageKey = getImageStringKey(productImageField, language)
            val imageDetails = product.getImageDetails(imageKey)!!
            val initImageId = imageDetails[IMG_ID] as String
            val imageDetailsInitImage = product.getImageDetails(initImageId)
            if (imageDetailsInitImage != null) {
                val sizes = imageDetailsInitImage["sizes"] as Map<String, Map<String, *>>?
                try {
                    val initCrop = toRectF(res.cropRectangle)
                    val height = sizes!!.getDimension("h")
                    val width = sizes.getDimension("w")
                    if (height != NO_VALUE && width != NO_VALUE) {
                        val rotationToApply = res.rotationInDegree
                        //we will rotate the whole image to have the top left values
                        var wholeImage = RectF(0F, 0F, width.toFloat(), height.toFloat())
                        val m = Matrix()
                        if (inverse) {
                            m.setRotate(rotationToApply.toFloat())
                            m.mapRect(wholeImage)
                            //the whole image whith the final width/height
                            wholeImage = RectF(0F, 0F, wholeImage.width(), wholeImage.height())
                            m.reset()
                        }
                        //now wholeImage and initCrop are in the same dimension as in the server.
                        //to revert the off crop to the initial image without rotation
                        m.setRotate(if (inverse) -(rotationToApply.toFloat()) else rotationToApply.toFloat())
                        m.mapRect(initCrop)
                        m.mapRect(wholeImage)
                        m.reset()
                        //we translate the crop rectangle to the origin
                        m.setTranslate(-wholeImage.left, -wholeImage.top)
                        m.mapRect(initCrop)
                        res.cropRectangle = toRect(initCrop)
                    }
                } catch (e: Exception) {
                    Log.e(ImageTransformationUtils::class.simpleName, "Can't process image for product ${product.code}", e)
                }
            }
        }

        fun getInitialServerTransformation(
                product: Product,
                productImageField: ProductImageField?,
                language: String?
        ): ImageTransformationUtils {
            val imageKey = getImageStringKey(productImageField!!, language!!)
            val imageDetails = product.getImageDetails(imageKey) ?: return ImageTransformationUtils()

            val initImageId = imageDetails[IMG_ID] as String?
            if (initImageId.isNullOrBlank()) return ImageTransformationUtils()

            return ImageTransformationUtils().apply {
                imageId = initImageId
                imageUrl = getImageUrl(product.code, initImageId, IMAGE_EDIT_SIZE_FILE)
                rotationInDegree = getImageRotation(imageDetails)
                val initCrop = getImageCropRect(imageDetails)
                if (initCrop != null) {
                    cropRectangle = toRect(initCrop)
                }
            }
        }

        /**
         * @param product the product
         * @param productImageField the type of the image
         * @param language the language
         * @return the image transformation containing the initial url and the transformation (rotation/crop) for screen
         */
        fun toServerTransformation(
                screenTransformation: ImageTransformationUtils,
                product: Product,
                productImageField: ProductImageField,
                language: String
        ): ImageTransformationUtils {
            val res = getInitialServerTransformation(product, productImageField, language)
            if (res.isEmpty()) {
                return res
            }
            res.rotationInDegree = screenTransformation.rotationInDegree
            res.cropRectangle = screenTransformation.cropRectangle
            if (res.cropRectangle != null && res.rotationInDegree != 0) {
                applyRotationOnCropRectangle(product, productImageField, language, res, false)
            }
            return res
        }

        /**
         * Get the specified key from the map of sizes
         * @param key the key
         * @return NO_VALUE if can't parse the size
         */
        private fun Map<String, Map<String, *>>.getDimension(key: String): Int {
            val value = (this[IMAGE_EDIT_SIZE] ?: error(""))[key] ?: return NO_VALUE
            return if (value is Number) {
                value.toInt()
            } else value.toString().toInt()
        }

        private fun toRect(init: RectF?) =
                if (init == null) null
                else Rect(
                        init.left.roundToInt(),
                        init.top.roundToInt(),
                        init.right.roundToInt(),
                        init.bottom.roundToInt()
                )

        private fun toRectF(init: Rect?) =
                if (init == null) null
                else RectF(
                        init.left.toFloat(),
                        init.top.toFloat(),
                        init.right.toFloat(),
                        init.bottom.toFloat()
                )

        /**
         * @param imgDetails
         * @return the angle in degree from the map.
         */
        private fun getImageRotation(imgDetails: Map<String, *>) = getAsInt(imgDetails, ANGLE, 0)

        private fun getImageCropRect(imgDetails: Map<String, *>): RectF? {
            val x1 = getAsFloat(imgDetails, LEFT, Float.NaN)
            val x2 = getAsFloat(imgDetails, RIGHT, Float.NaN)
            val y1 = getAsFloat(imgDetails, TOP, Float.NaN)
            val y2 = getAsFloat(imgDetails, BOTTOM, Float.NaN)
            return if (x1.isNaN() || x2.isNaN() || y1.isNaN() || y2.isNaN() || x2 <= x1 || y2 <= y1) null
            else RectF(x1, y1, x2, y2)
        }
    }
}