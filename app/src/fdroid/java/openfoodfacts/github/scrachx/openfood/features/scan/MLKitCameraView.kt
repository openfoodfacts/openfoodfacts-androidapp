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
    null
) {

    override fun onResume() = Unit
    override fun stopCameraPreview() = Unit
    override fun startCameraPreview() = Unit
    override fun updateFlashSetting(flashActive: Boolean) = Unit
    override fun updateFocusModeSetting(autoFocusActive: Boolean) = Unit
    override fun toggleCamera(requestedCameraId: Int) = Unit
}
