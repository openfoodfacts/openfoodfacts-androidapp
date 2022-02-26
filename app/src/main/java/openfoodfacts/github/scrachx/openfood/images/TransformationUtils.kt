package openfoodfacts.github.scrachx.openfood.images

import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import androidx.core.graphics.toRect
import androidx.core.graphics.toRectF
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.utils.getAsFloat
import openfoodfacts.github.scrachx.openfood.utils.getAsInt

/**
 * Get the specified key from the map of sizes
 * @param key the key
 * @return NO_VALUE if can't parse the size
 */
fun Map<String, Map<String, *>>.getDimension(key: String): Int {
    val value = (this[IMAGE_EDIT_SIZE] ?: error(""))[key] ?: return NO_VALUE
    return if (value is Number) {
        value.toInt()
    } else value.toString().toInt()
}

fun ImageTransformation.applyToMap(imgMap: MutableMap<String, String>) {
    imgMap[ANGLE] = rotationInDegree.toString()
    cropRectangle?.let {
        imgMap[LEFT] = it.left.toString()
        imgMap[RIGHT] = it.right.toString()
        imgMap[TOP] = it.top.toString()
        imgMap[BOTTOM] = it.bottom.toString()
    }
}

fun getInitialServerTransformation(
        product: Product,
        productImageField: ProductImageField?,
        language: String?
): ImageTransformation {
    val imageKey = getImageStringKey(productImageField!!, language!!)
    val imageDetails = product.getImageDetails(imageKey) ?: return ImageTransformation()

    val initImageId = imageDetails[IMG_ID] as String?
    if (initImageId.isNullOrBlank()) return ImageTransformation()

    return ImageTransformation().apply {
        imageId = initImageId
        imageUrl = getImageUrl(product.barcode, initImageId, IMAGE_EDIT_SIZE_FILE)
        rotationInDegree = getImageRotation(imageDetails)

        getImageCropRect(imageDetails)?.let { cropRectangle = it.toRect() }
    }
}

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

private fun applyRotationOnCropRectangle(
        product: Product,
        productImageField: ProductImageField,
        language: String,
        res: ImageTransformation,
        invert: Boolean
) {
    // if a crop and a rotation is done, we should rotate the cropped rectangle
    val imageKey = getImageStringKey(productImageField, language)
    val imageDetails = product.getImageDetails(imageKey)!!
    val initImageId = imageDetails[IMG_ID] as String
    val imageDetailsInitImage = product.getImageDetails(initImageId) ?: return
    val sizesMap = imageDetailsInitImage["sizes"] as Map<String, Map<String, *>>?
    try {
        val initCrop = res.cropRectangle?.toRectF()
        val height = sizesMap!!.getDimension("h")
        val width = sizesMap.getDimension("w")
        if (height != NO_VALUE && width != NO_VALUE) {
            val rotationToApply = res.rotationInDegree
            //we will rotate the whole image to have the top left values
            var wholeImage = RectF(0F, 0F, width.toFloat(), height.toFloat())
            val m = Matrix()
            if (invert) {
                m.setRotate(rotationToApply.toFloat())
                m.mapRect(wholeImage)
                //the whole image whith the final width/height
                wholeImage = RectF(0F, 0F, wholeImage.width(), wholeImage.height())
                m.reset()
            }
            //now wholeImage and initCrop are in the same dimension as in the server.
            //to revert the off crop to the initial image without rotation
            m.setRotate(if (invert) -(rotationToApply.toFloat()) else rotationToApply.toFloat())
            m.mapRect(initCrop)
            m.mapRect(wholeImage)
            m.reset()
            //we translate the crop rectangle to the origin
            m.setTranslate(-wholeImage.left, -wholeImage.top)
            m.mapRect(initCrop)
            res.cropRectangle = initCrop?.toRect()
        }
    } catch (e: Exception) {
        Log.e(LOG_TAG, "Can't process image for product ${product.code}", e)
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
): ImageTransformation {
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

/**
 * @param product the product
 * @param productImageField the type of the image
 * @param language the language
 * @return the image transformation containing the initial url and the transformation (rotation/crop) for screen
 */
fun toServerTransformation(
        screenTransformation: ImageTransformation,
        product: Product,
        productImageField: ProductImageField,
        language: String
): ImageTransformation {
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

const val NO_VALUE = -1
const val LEFT = "x1"
const val RIGHT = "x2"
const val TOP = "y1"
const val BOTTOM = "y2"
const val ANGLE = "angle"

private val LOG_TAG = ImageTransformation::class.simpleName
