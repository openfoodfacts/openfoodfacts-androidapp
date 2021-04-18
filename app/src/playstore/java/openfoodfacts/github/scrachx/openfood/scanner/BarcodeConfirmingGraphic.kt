package openfoodfacts.github.scrachx.openfood.scanner

import android.graphics.Canvas
import android.graphics.Path
import com.google.mlkit.vision.barcode.Barcode
import openfoodfacts.github.scrachx.openfood.camera.GraphicOverlay
import openfoodfacts.github.scrachx.openfood.utils.CameraUtils

/**
 * Guides user to move camera closer to confirm the detected barcode.
 */
internal class BarcodeConfirmingGraphic(overlay: GraphicOverlay, private val barcode: Barcode) :
    BarcodeGraphicBase(overlay) {

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // Draws a highlighted path to indicate the current progress to meet size requirement.
        val sizeProgress = CameraUtils.getProgressToMeetBarcodeSizeRequirement(overlay, barcode)
        val path = Path()
        if (sizeProgress > 0.95f) {
            // To have a completed path with all corners rounded.
            path.moveTo(boxRect.left, boxRect.top)
            path.lineTo(boxRect.right, boxRect.top)
            path.lineTo(boxRect.right, boxRect.bottom)
            path.lineTo(boxRect.left, boxRect.bottom)
            path.close()
        } else {
            path.moveTo(boxRect.left, boxRect.top + boxRect.height() * sizeProgress)
            path.lineTo(boxRect.left, boxRect.top)
            path.lineTo(boxRect.left + boxRect.width() * sizeProgress, boxRect.top)

            path.moveTo(boxRect.right, boxRect.bottom - boxRect.height() * sizeProgress)
            path.lineTo(boxRect.right, boxRect.bottom)
            path.lineTo(boxRect.right - boxRect.width() * sizeProgress, boxRect.bottom)
        }
        canvas.drawPath(path, pathPaint)
    }
}
