package openfoodfacts.github.scrachx.openfood.features.scan

import android.util.Log
import android.view.ViewStub
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.camera.CameraSource
import openfoodfacts.github.scrachx.openfood.camera.CameraSourcePreview
import openfoodfacts.github.scrachx.openfood.camera.GraphicOverlay
import openfoodfacts.github.scrachx.openfood.camera.WorkflowModel
import openfoodfacts.github.scrachx.openfood.scanner.BarcodeProcessor
import java.io.IOException

class MlKitCameraView(private val activity: AppCompatActivity) {

    companion object {
        private const val LOG_TAG = "MlKitCameraView"
    }

    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var promptChip: Chip? = null

    private var cameraSource: CameraSource? = null
    private var workflowModel: WorkflowModel? = null

    var onOverlayClickListener: (() -> Unit)? = null
    var barcodeScannedCallback: ((String) -> Unit)? = null

    fun attach(viewStub: ViewStub, cameraState: Int, flashActive: Boolean, autoFocusActive: Boolean) {
        viewStub.layoutResource = R.layout.view_camera_source_preview
        preview = (viewStub.inflate() as CameraSourcePreview).apply {
            graphicOverlay = findViewById(R.id.camera_preview_graphic_overlay)
            promptChip = findViewById(R.id.bottom_prompt_chip)
        }
        setupGraphicOverlay(cameraState, flashActive, autoFocusActive)
        setUpWorkflowModel()
    }

    fun detach() {
        stopCameraPreview()
        cameraSource?.release()
        cameraSource = null
    }

    fun onResume() {
        workflowModel?.markCameraFrozen()
        cameraSource?.setFrameProcessor(BarcodeProcessor(graphicOverlay!!, workflowModel!!))
        workflowModel?.setWorkflowState(WorkflowState.DETECTING)

    }

    fun startCameraPreview() {
        val workflowModel = this.workflowModel ?: return
        val cameraSource = this.cameraSource ?: return

        if (!workflowModel.isCameraLive) {
            try {
                workflowModel.markCameraLive()
                preview?.start(cameraSource)
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Failed to start camera preview!", e)
                cameraSource.release()
                this.cameraSource = null
            }
        }
    }

    fun stopCameraPreview() {
        workflowModel?.let {
            if (it.isCameraLive) {
                it.markCameraFrozen()
                preview?.stop()
            }
        }
    }

    fun updateFlashSetting(flashActive: Boolean) {
        cameraSource?.updateFlashMode(flashActive)
    }

    fun updateFocusModeSetting(autoFocusActive: Boolean) {
        cameraSource?.setFocusMode(autoFocusActive)
    }

    fun toggleCamera() {
        stopCameraPreview()
        cameraSource?.switchCamera()
        startCameraPreview()
    }

    fun updateWorkflowState(state: WorkflowState) {
        workflowModel?.setWorkflowState(state)
    }

    private fun setupGraphicOverlay(cameraState: Int, flashActive: Boolean, autoFocusActive: Boolean) {
        graphicOverlay?.let {
            cameraSource = CameraSource(it).apply {
                requestedCameraId = cameraState
                requestedFlashState = flashActive
                requestedFocusState = autoFocusActive
            }
            it.setOnClickListener {
                onOverlayClickListener?.invoke()
                workflowModel?.setWorkflowState(WorkflowState.DETECTING)
                startCameraPreview()
            }
        }
    }

    private fun setUpWorkflowModel() {
        workflowModel = ViewModelProvider(activity)[WorkflowModel::class.java]

        // Observes the workflow state changes, if happens, update the overlay view indicators and
        // camera preview state.
        workflowModel!!.workflowState.observe(activity, Observer { workflowState ->
            if (workflowState == null) {
                return@Observer
            }

            when (workflowState) {
                WorkflowState.DETECTING -> {
                    promptChip?.isVisible = true
                    promptChip?.setText(R.string.prompt_point_at_a_barcode)
                    startCameraPreview()
                }
                WorkflowState.CONFIRMING -> {
                    promptChip?.isVisible = true
                    promptChip?.setText(R.string.prompt_move_camera_closer)
                    startCameraPreview()
                }
                WorkflowState.DETECTED -> stopCameraPreview()
                else -> promptChip?.isVisible = false
            }

        })

        workflowModel?.detectedBarcode?.observe(activity) { barcode ->
            barcode?.rawValue?.let {
                barcodeScannedCallback?.invoke(it)
                Log.i(LOG_TAG, "barcode =" + barcode.rawValue)
            }
        }
    }
}
