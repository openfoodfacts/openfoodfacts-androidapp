package openfoodfacts.github.scrachx.openfood.features.scan

import android.view.ViewStub
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import openfoodfacts.github.scrachx.openfood.R

abstract class CameraView<T : FrameLayout>(
    protected val activity: AppCompatActivity,
    private val viewStub: ViewStub,
    @LayoutRes private val resId: Int?,
) {
    var barcodeScannedCallback: ((String?) -> Unit)? = null
    var onOverlayClickListener: (() -> Unit)? = null

    protected lateinit var view: T

    @Suppress("UNCHECKED_CAST")
    @CallSuper
    open fun attach(cameraState: Int, flashActive: Boolean, autoFocusActive: Boolean) {
        // For stubs
        if (resId == null) {
            viewStub.isVisible = false
            return
        }

        viewStub.isVisible = true
        viewStub.layoutResource = resId
        view = viewStub.inflate() as T
    }

    @CallSuper
    open fun detach() {
        // Default impl
    }

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

    companion object {
        fun of(activity: AppCompatActivity, viewStub: ViewStub, mlScannerEnabled: Boolean): CameraView<*> {
            return if (mlScannerEnabled) {
                // Prefer ml kit with fallback
                MLKitCameraView(activity, viewStub)
            } else {
                ZXCameraView(activity, viewStub)
            }
        }

        @DrawableRes
        fun getFlashRes(active: Boolean): Int {
            return if (active) {
                R.drawable.ic_flash_on_white_24dp
            } else {
                R.drawable.ic_flash_off_white_24dp
            }
        }

        @DrawableRes
        fun getAutofocusRes(enabled: Boolean): Int {
            return if (enabled) {
                R.drawable.ic_baseline_camera_focus_on_24
            } else {
                R.drawable.ic_baseline_camera_focus_off_24
            }
        }
    }

}