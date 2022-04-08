package openfoodfacts.github.scrachx.openfood.features.scan

import android.view.ViewStub
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

abstract class CameraView<T : FrameLayout>(
    protected val activity: AppCompatActivity,
    protected val viewStub: ViewStub,
    @LayoutRes private val resId: Int,
    var barcodeScannedCallback: ((String?) -> Unit)? = null
) {
    var onOverlayClickListener: (() -> Unit)? = null

    protected lateinit var view: T

    @Suppress("UNCHECKED_CAST")
    @CallSuper
    open fun attach(cameraState: Int, flashActive: Boolean, autoFocusActive: Boolean) {
        viewStub.isVisible = true
        viewStub.layoutResource = resId
        view = viewStub.inflate() as T
    }

    abstract fun detach()

    abstract fun startCameraPreview()
    abstract fun stopCameraPreview()
    abstract fun onResume()

    abstract fun toggleCamera(requestedCameraId: Int)
    abstract fun updateFlashSetting(flashActive: Boolean)
    abstract fun updateFocusModeSetting(autoFocusActive: Boolean)

    open fun updateWorkflowState(state: WorkflowState) {
        // Default impl
    }

    open fun playBeepSound() {
        // Default impl
    }

}