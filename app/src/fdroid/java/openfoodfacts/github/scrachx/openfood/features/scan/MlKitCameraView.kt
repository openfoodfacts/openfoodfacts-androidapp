package openfoodfacts.github.scrachx.openfood.features.scan

import android.view.ViewStub
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

@Suppress("UNUSED_PARAMETER", "unused")
class MlKitCameraView(private val activity: AppCompatActivity) {

    var onOverlayClickListener: (() -> Unit)? = null
    var barcodeScannedCallback: ((String) -> Unit)? = null

    fun attach(viewStubProxy: ViewStub, cameraState: Int, flashActive: Boolean, autoFocusActive: Boolean) {
        viewStubProxy.isVisible = false
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
