package openfoodfacts.github.scrachx.openfood.features.scan

import android.view.ViewStub
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import openfoodfacts.github.scrachx.openfood.BuildConfig

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
    init {
        error("Could not create ${this::class.simpleName} in ${BuildConfig.BUILD_TYPE} build type.")
    }

    override fun onResume() = Unit
    override fun stopCameraPreview() = Unit
    override fun startCameraPreview() = Unit
    override fun updateFlashSetting(flashActive: Boolean) = Unit
    override fun updateFocusModeSetting(autoFocusActive: Boolean) = Unit
    override fun toggleCamera(requestedCameraId: Int) = Unit
}
