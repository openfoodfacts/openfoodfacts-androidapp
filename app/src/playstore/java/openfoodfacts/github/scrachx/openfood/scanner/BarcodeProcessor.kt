package openfoodfacts.github.scrachx.openfood.scanner

import android.util.Log
import androidx.annotation.MainThread
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import openfoodfacts.github.scrachx.openfood.camera.CameraReticleAnimator
import openfoodfacts.github.scrachx.openfood.camera.FrameProcessorBase
import openfoodfacts.github.scrachx.openfood.camera.GraphicOverlay
import openfoodfacts.github.scrachx.openfood.camera.WorkflowModel
import openfoodfacts.github.scrachx.openfood.features.scan.WorkflowState
import openfoodfacts.github.scrachx.openfood.utils.CameraUtils
import java.io.IOException

/**
 * A processor to run the barcode detector.
 */
class BarcodeProcessor(graphicOverlay: GraphicOverlay, private val workflowModel: WorkflowModel) :
        FrameProcessorBase<List<Barcode>>() {

    private val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_CODE_93,
                    Barcode.FORMAT_CODE_128
            ).build()

    private val scanner = BarcodeScanning.getClient(options)
    private val cameraReticleAnimator: CameraReticleAnimator = CameraReticleAnimator(graphicOverlay)

    override fun detectInImage(image: InputImage): Task<List<Barcode>> =
            scanner.process(image)

    @MainThread
    override fun onSuccess(
            results: List<Barcode>,
            graphicOverlay: GraphicOverlay
    ) {

        if (!workflowModel.isCameraLive) return

        Log.d(LOG_TAG, "Barcode result size: ${results.size}")

        // Picks the barcode, if exists, that covers the center of graphic overlay.

        val barcodeInCenter = results.firstOrNull { barcode ->
            val boundingBox = barcode.boundingBox ?: return@firstOrNull false
            val box = graphicOverlay.translateRect(boundingBox)
            box.contains(graphicOverlay.width / 2f, graphicOverlay.height / 2f)
        }

        graphicOverlay.clear()
        if (barcodeInCenter == null) {
            cameraReticleAnimator.start()
            graphicOverlay.add(BarcodeReticleGraphic(graphicOverlay, cameraReticleAnimator))
            workflowModel.setWorkflowState(WorkflowState.DETECTING)
        } else {
            cameraReticleAnimator.cancel()
            val sizeProgress = CameraUtils.getProgressToMeetBarcodeSizeRequirement(graphicOverlay, barcodeInCenter)
            if (sizeProgress < 1) {
                // Barcode in the camera view is too small, so prompt user to move camera closer.
                graphicOverlay.add(BarcodeConfirmingGraphic(graphicOverlay, barcodeInCenter))
                workflowModel.setWorkflowState(WorkflowState.CONFIRMING)
            } else {
                // Barcode size in the camera view is sufficient.
                workflowModel.setWorkflowState(WorkflowState.DETECTED)
                workflowModel.detectedBarcode.setValue(barcodeInCenter)
            }
        }
        graphicOverlay.invalidate()
    }

    override fun onFailure(e: Exception) {
        Log.e(LOG_TAG, "Barcode detection failed!", e)
    }

    override fun stop() {
        super.stop()
        try {
            scanner.close()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Failed to close barcode detector!", e)
        }
    }

    companion object {
        private const val LOG_TAG = "BarcodeProcessor"
    }

}
