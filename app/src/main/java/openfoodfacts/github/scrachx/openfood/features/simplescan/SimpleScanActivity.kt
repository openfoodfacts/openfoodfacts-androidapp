@file:Suppress("DEPRECATION")

package openfoodfacts.github.scrachx.openfood.features.simplescan

import android.content.DialogInterface
import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivitySimpleScanBinding
import openfoodfacts.github.scrachx.openfood.features.scan.MlKitCameraView
import openfoodfacts.github.scrachx.openfood.features.simplescan.SimpleScanActivityContract.Companion.KEY_SCANNED_BARCODE
import openfoodfacts.github.scrachx.openfood.models.CameraState
import openfoodfacts.github.scrachx.openfood.repositories.ScannerPreferencesRepository
import java.util.concurrent.atomic.AtomicBoolean

@AndroidEntryPoint
class SimpleScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySimpleScanBinding
    private val viewModel: SimpleScanViewModel by viewModels()

    private val mlKitView by lazy { MlKitCameraView(this) }
    private val scannerInitialized = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        binding.scanFlashBtn.setOnClickListener {
            viewModel.changeCameraFlash()
        }
        binding.scanFlipCameraBtn.setOnClickListener {
            viewModel.changeCameraState()
        }
        binding.scanChangeFocusBtn.setOnClickListener {
            viewModel.changeCameraAutoFocus()
        }
        binding.troubleScanningBtn.setOnClickListener {
            viewModel.troubleScanningPressed()
        }

        lifecycleScope.launch {
            viewModel.scannerOptionsFlow
                .flowWithLifecycle(lifecycle)
                .collect { options ->
                    Log.d("SimpleScanActivity", "options: $options")
                    if (!scannerInitialized.getAndSet(true)) {
                        setupBarcodeScanner(options)
                    }
                    applyScannerOptions(options)
                }
        }

        lifecycleScope.launch {
            viewModel.sideEffectsFlow
                .flowWithLifecycle(lifecycle)
                .collect { sideEffect ->
                    Log.d("SimpleScanActivity", "sideEffect: $sideEffect")
                    when (sideEffect) {
                        is SimpleScanViewModel.SideEffect.ScanTrouble -> {
                            stopScanning()
                            showManualInputDialog()
                        }
                        is SimpleScanViewModel.SideEffect.BarcodeDetected -> {
                            val intent = Intent().putExtra(KEY_SCANNED_BARCODE, sideEffect.barcode)
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        startScanning()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // status bar will remain visible if user presses home and then reopens the activity
        // hence hiding status bar again
        hideSystemUI()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
    }

    private fun hideSystemUI() {
        WindowInsetsControllerCompat(window, binding.root).hide(WindowInsetsCompat.Type.statusBars())
        actionBar?.hide()
    }

    private fun applyScannerOptions(options: SimpleScanScannerOptions) {
        // camera state
        if (options.mlScannerEnabled) {
            mlKitView.toggleCamera()
        } else {
            val cameraId = when (options.cameraState) {
                CameraState.Back -> Camera.CameraInfo.CAMERA_FACING_BACK
                CameraState.Front -> Camera.CameraInfo.CAMERA_FACING_FRONT
            }
            with(binding.scanBarcodeView) {
                pause()
                val newSettings = barcodeView.cameraSettings.apply {
                    requestedCameraId = cameraId
                }
                barcodeView.cameraSettings = newSettings
                resume()
            }
        }

        // flash
        val flashIconRes = if (options.flashEnabled) {
            R.drawable.ic_flash_off_white_24dp
        } else {
            R.drawable.ic_flash_on_white_24dp
        }
        binding.scanFlashBtn.setImageResource(flashIconRes)

        if (options.mlScannerEnabled) {
            mlKitView.updateFlashSetting(options.flashEnabled)
        } else {
            if (options.flashEnabled) {
                binding.scanBarcodeView.setTorchOn()
            } else {
                binding.scanBarcodeView.setTorchOff()
            }
        }

        // autofocus
        val focusIconRes = if (options.autoFocusEnabled) {
            R.drawable.ic_baseline_camera_focus_on_24
        } else {
            R.drawable.ic_baseline_camera_focus_off_24
        }
        binding.scanChangeFocusBtn.setImageResource(focusIconRes)
        if (options.mlScannerEnabled) {
            mlKitView.updateFocusModeSetting(options.autoFocusEnabled)
        } else {
            with(binding.scanBarcodeView) {
                pause()
                val newSettings = barcodeView.cameraSettings.apply {
                    isAutoFocusEnabled = options.autoFocusEnabled
                }
                barcodeView.cameraSettings = newSettings
                resume()
            }
        }
    }

    private fun setupBarcodeScanner(options: SimpleScanScannerOptions) {
        binding.scanBarcodeView.isVisible = !options.mlScannerEnabled

        if (options.mlScannerEnabled) {
            mlKitView.attach(binding.scanMlView, options.cameraState.value, options.flashEnabled, options.autoFocusEnabled)
            mlKitView.barcodeScannedCallback = {
                viewModel.barcodeDetected(it)
            }
        } else {
            with(binding.scanBarcodeView) {
                barcodeView.decoderFactory = DefaultDecoderFactory(ScannerPreferencesRepository.BARCODE_FORMATS)
                setStatusText(null)
                barcodeView.cameraSettings.requestedCameraId = options.cameraState.value
                barcodeView.cameraSettings.isAutoFocusEnabled = options.autoFocusEnabled

                decodeContinuous(object : BarcodeCallback {
                    override fun barcodeResult(result: BarcodeResult?) {
                        result?.text?.let {
                            viewModel.barcodeDetected(it)
                        }
                    }

                    override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) = Unit
                })
            }
        }
    }

    private fun stopScanning() {
        if (viewModel.scannerOptionsFlow.value.mlScannerEnabled) {
            mlKitView.stopCameraPreview()
        } else {
            binding.scanBarcodeView.pause()
        }
    }

    private fun startScanning() {
        if (viewModel.scannerOptionsFlow.value.mlScannerEnabled) {
            mlKitView.onResume()
            mlKitView.startCameraPreview()
        } else {
            binding.scanBarcodeView.resume()
        }
    }

    private fun showManualInputDialog() {
        val inputEditText = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val view = FrameLayout(this).apply {
            val margin = resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin)
            setPadding(margin, margin / 2, margin, margin / 2)
            addView(inputEditText)
        }
        val dialog = MaterialAlertDialogBuilder(this@SimpleScanActivity)
            .setTitle(R.string.trouble_scanning)
            .setMessage(R.string.enter_barcode)
            .setView(view)
            .setPositiveButton(R.string.ok_button, null)
            .setNegativeButton(R.string.cancel_button) { _, _ ->
                startScanning()
            }
            .show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            inputEditText.text?.toString()?.let {
                viewModel.barcodeDetected(it)
            }

        }
    }
}
