package openfoodfacts.github.scrachx.openfood.features.scan

import android.view.ViewStub
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.repositories.ScannerPreferencesRepository

class ZXCameraView(
    activity: AppCompatActivity,
    viewStub: ViewStub,
) : CameraView<DecoratedBarcodeView>(
    activity,
    viewStub,
    R.layout.view_camera_zx_preview
) {
    private val beepManager by lazy { BeepManager(activity) }

    override fun attach(cameraState: Int, flashActive: Boolean, autoFocusActive: Boolean) {
        super.attach(cameraState, flashActive, autoFocusActive)

        view.apply {
            isVisible = true

            barcodeView.decoderFactory = DefaultDecoderFactory(ScannerPreferencesRepository.BARCODE_FORMATS)
            setStatusText(null)

            barcodeView.cameraSettings.run {
                requestedCameraId = cameraState
                isAutoFocusEnabled = autoFocusActive
            }

            setOnClickListener { onOverlayClickListener?.invoke() }

            // Start continuous scanner
            decodeContinuous(object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult?) {
                    beepManager.playBeepSound()
                    barcodeScannedCallback?.invoke(result?.text)
                }

                override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) = Unit
            })
        }
    }

    override fun onResume() {
        view.resume()
    }

    override fun updateFlashSetting(flashActive: Boolean) {
        if (flashActive) {
            view.setTorchOn()
        } else {
            view.setTorchOff()
        }
    }

    override fun toggleCamera(requestedCameraId: Int) {
        if (view.barcodeView.isPreviewActive) {
            view.pause()
        }
        val settings = view.barcodeView.cameraSettings.apply {
            this.requestedCameraId = requestedCameraId
        }
        view.barcodeView.cameraSettings = settings
        view.resume()
    }

    override fun updateFocusModeSetting(autoFocusActive: Boolean) {
        if (view.barcodeView.isPreviewActive) {
            view.pause()
        }
        val settings = view.barcodeView.cameraSettings.apply {
            isAutoFocusEnabled = autoFocusActive
        }
        view.barcodeView.cameraSettings = settings
        view.resume()
    }

    override fun startCameraPreview() {
        view.resume()
    }

    override fun stopCameraPreview() {
        view.pause()
    }

    override fun playBeepSound() {
        beepManager.playBeepSound()
    }


    override fun updateWorkflowState(state: WorkflowState) = Unit


}