package openfoodfacts.github.scrachx.openfood.features.scan

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.ViewStubProxy

@Suppress("UNUSED_PARAMETER", "unused")
class MlKitCameraView(private val activity: AppCompatActivity) {

    var onOverlayClickListener: (() -> Unit)? = null
    var barcodeScannedCallback: ((String) -> Unit)? = null

    fun attach(viewStubProxy: ViewStubProxy, cameraState: Int, flashActive: Boolean, autoFocusActive: Boolean) {
        viewStubProxy.viewStub?.isVisible = false
    }

    fun detach() {
        //No-op
    }

    fun onResume() {
        //No-op
    }

    fun stopCameraPreview() {
        //No-op
    }

    fun startCameraPreview() {
        //No-op
    }

    fun updateFlashSetting(flashActive: Boolean) {
        //No-op
    }

    fun updateFocusModeSetting(autoFocusActive: Boolean) {
        //No-op
    }

    fun toggleCamera() {
        //No-op
    }

    fun updateWorkflowState(state: WorkflowState) {
        //No-op
    }
}
