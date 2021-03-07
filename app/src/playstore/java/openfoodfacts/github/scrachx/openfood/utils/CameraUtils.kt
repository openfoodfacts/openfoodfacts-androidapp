@file:Suppress("DEPRECATION")

package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.hardware.Camera
import android.util.Log
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.common.InputImage
import openfoodfacts.github.scrachx.openfood.camera.CameraSizePair
import openfoodfacts.github.scrachx.openfood.camera.GraphicOverlay
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.abs



object CameraUtils {

    private const val LOG_TAG = "CameraUtils"

    /**
     * If the absolute difference between aspect ratios is less than this tolerance, they are
     * considered to be the same aspect ratio.
     */
    const val ASPECT_RATIO_TOLERANCE = 0.01f

    /**
     * Check if the  camera is in portrait mode.
     *
     * @return true if installed, false otherwise.
     */
    fun isPortraitMode(context: Context): Boolean =
            context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    /**
     * Generates a list of acceptable preview sizes. Preview sizes are not acceptable if there is not
     * a corresponding picture size of the same aspect ratio. If there is a corresponding picture size
     * of the same aspect ratio, the picture size is paired up with the preview size.
     *
     *
     * This is necessary because even if we don't use still pictures, the still picture size must
     * be set to a size that is the same aspect ratio as the preview size we choose. Otherwise, the
     * preview images may be distorted on some devices.
     */
    fun generateValidPreviewSizeList(camera: Camera): List<CameraSizePair> {
        val parameters = camera.parameters
        val supportedPreviewSizes = parameters.supportedPreviewSizes
        val supportedPictureSizes = parameters.supportedPictureSizes
        val validPreviewSizes = ArrayList<CameraSizePair>()
        for (previewSize in supportedPreviewSizes) {
            val previewAspectRatio = previewSize.width.toFloat() / previewSize.height.toFloat()

            // By looping through the picture sizes in order, we favor the higher resolutions.
            // We choose the highest resolution in order to support taking the full resolution
            // picture later.
            for (pictureSize in supportedPictureSizes) {
                val pictureAspectRatio = pictureSize.width.toFloat() / pictureSize.height.toFloat()
                if (abs(previewAspectRatio - pictureAspectRatio) < ASPECT_RATIO_TOLERANCE) {
                    validPreviewSizes.add(CameraSizePair(previewSize, pictureSize))
                    break
                }
            }
        }
        // If there are no picture sizes with the same aspect ratio as any preview sizes, allow all of
        // the preview sizes and hope that the camera can handle it.  Probably unlikely, but we still
        // account for it.
        if (validPreviewSizes.isEmpty()) {
            Log.w(LOG_TAG, "No preview sizes have a corresponding same-aspect-ratio picture size.")
            supportedPreviewSizes.forEach { previewSize ->
                // The null picture size will let us know that we shouldn't set a picture size.
                validPreviewSizes += CameraSizePair(previewSize, null)
            }
        }

        return validPreviewSizes
    }


    /** Convert NV21 format byte buffer to bitmap. */
    fun convertToBitmap(data: ByteBuffer, width: Int, height: Int, rotationDegrees: Int): Bitmap? {
        data.rewind()
        val imageInBuffer = ByteArray(data.limit())
        data.get(imageInBuffer, 0, imageInBuffer.size)
        try {
            val image = YuvImage(
                    imageInBuffer, InputImage.IMAGE_FORMAT_NV21, width, height, null
            )
            val stream = ByteArrayOutputStream()
            image.compressToJpeg(Rect(0, 0, width, height), 80, stream)
            val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
            stream.close()

            // Rotate the image back to straight.
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error: " + e.message)
        }
        return null
    }

    fun getProgressToMeetBarcodeSizeRequirement(overlay: GraphicOverlay, barcode: Barcode): Float {
        val reticleBoxWidth = getBarcodeReticleBox(overlay).width()
        val barcodeWidth = overlay.translateX(barcode.boundingBox?.width()?.toFloat() ?: 0f)
        val requiredWidth = reticleBoxWidth * 50/100
        return (barcodeWidth / requiredWidth).coerceAtMost(1f)
    }

    fun getBarcodeReticleBox(overlay: GraphicOverlay): RectF {
        val overlayWidth = overlay.width.toFloat()
        val overlayHeight = overlay.height.toFloat()
        val boxWidth = overlayWidth * 80 / 100
        val boxHeight = overlayHeight * 40 / 100
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        return RectF(cx - boxWidth / 2, cy - boxHeight / 2, cx + boxWidth / 2, cy + boxHeight / 2)
    }

}