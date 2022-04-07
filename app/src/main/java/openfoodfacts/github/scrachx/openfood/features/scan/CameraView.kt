package openfoodfacts.github.scrachx.openfood.features.scan

import androidx.appcompat.app.AppCompatActivity

abstract class CameraView(
    protected val activity: AppCompatActivity,
    var barcodeScannedCallback: ((String?) -> Unit)? = null
) {
    var onOverlayClickListener: (() -> Unit)? = null

    abstract fun attach(cameraState: Int, flashActive: Boolean, autoFocusActive: Boolean)
    abstract fun detach()

    abstract fun startCameraPreview()
    abstract fun stopCameraPreview()
    abstract fun onResume()

    abstract fun toggleCamera(cameraState: Int)
    abstract fun updateFlashSetting(flashActive: Boolean)
    abstract fun updateFocusModeSetting(autoFocusActive: Boolean)

    open fun updateWorkflowState(state: WorkflowState) {
        // Default impl
    }

    open fun playBeepSound() {
        // Default impl
    }

}