package openfoodfacts.github.scrachx.openfood.features.scan

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import openfoodfacts.github.scrachx.openfood.repositories.ScannerPreferencesRepository

class ZXCameraView(
    activity: AppCompatActivity,
    private val decoratedView: DecoratedBarcodeView,
) : CameraView(activity) {

    private val beepManager by lazy { BeepManager(activity) }

    override fun attach(cameraState: Int, flashActive: Boolean, autoFocusActive: Boolean) {
        decoratedView.isVisible = true
        decoratedView.visibility = View.VISIBLE

        decoratedView.barcodeView.decoderFactory = DefaultDecoderFactory(ScannerPreferencesRepository.BARCODE_FORMATS)
        decoratedView.setStatusText(null)

        decoratedView.barcodeView.cameraSettings.run {
            requestedCameraId = cameraState
            isAutoFocusEnabled = autoFocusActive
        }

        decoratedView.setOnClickListener { onOverlayClickListener?.invoke() }


        // Start continuous scanner
        decoratedView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                beepManager.playBeepSound()
                barcodeScannedCallback?.invoke(result?.text)
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) = Unit
        })
    }

    override fun onResume() {
        decoratedView.resume()
    }

    override fun updateFlashSetting(flashActive: Boolean) {
        if (flashActive) decoratedView.setTorchOn()
        else decoratedView.setTorchOff()
    }

    override fun toggleCamera(cameraState: Int) {
        val settings = decoratedView.barcodeView.cameraSettings
        if (decoratedView.barcodeView.isPreviewActive) {
            decoratedView.pause()
        }
        settings.requestedCameraId = cameraState
        decoratedView.barcodeView.cameraSettings = settings
        decoratedView.resume()
    }

    override fun updateFocusModeSetting(autoFocusActive: Boolean) {
        if (decoratedView.barcodeView.isPreviewActive) {
            decoratedView.pause()
        }
        val settings = decoratedView.barcodeView.cameraSettings.apply {
            isAutoFocusEnabled = autoFocusActive
        }
        decoratedView.barcodeView.cameraSettings = settings
        decoratedView.resume()
    }

    override fun startCameraPreview() {
        decoratedView.resume()
    }

    override fun stopCameraPreview() {
        decoratedView.pause()
    }

    override fun playBeepSound() {
        beepManager.playBeepSound()
    }

    override fun detach() = Unit


    override fun updateWorkflowState(state: WorkflowState) = Unit


}