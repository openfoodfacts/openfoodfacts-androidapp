package openfoodfacts.github.scrachx.openfood.features.scan

import android.view.ViewStub
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

/**
 * When on FDroid this class is just a stub.
 */
class MLKitCameraView(
    activity: AppCompatActivity,
    viewStub: ViewStub,
) : CameraView<FrameLayout>(
    activity,
    viewStub,
    0
) {

    override fun attach(cameraState: Int, flashActive: Boolean, autoFocusActive: Boolean) {
        super.attach(cameraState, flashActive, autoFocusActive)
        viewStub.isVisible = false
    }

    override fun detach() = Unit
    override fun onResume() = Unit
    override fun stopCameraPreview() = Unit
    override fun startCameraPreview() = Unit
    override fun updateFlashSetting(flashActive: Boolean) = Unit
    override fun updateFocusModeSetting(autoFocusActive: Boolean) = Unit
    override fun toggleCamera(requestedCameraId: Int) = Unit
}
