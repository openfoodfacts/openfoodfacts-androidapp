@file:Suppress("DEPRECATION")

package openfoodfacts.github.scrachx.openfood.features.simplescan

import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivitySimpleScanBinding
import openfoodfacts.github.scrachx.openfood.features.scan.CameraView
import openfoodfacts.github.scrachx.openfood.features.simplescan.SimpleScanActivityContract.Companion.KEY_SCANNED_BARCODE
import openfoodfacts.github.scrachx.openfood.features.simplescan.SimpleScanViewModel.SideEffect.BarcodeDetected
import openfoodfacts.github.scrachx.openfood.features.simplescan.SimpleScanViewModel.SideEffect.ScanTrouble
import openfoodfacts.github.scrachx.openfood.models.CameraState
import java.util.concurrent.atomic.AtomicBoolean

@AndroidEntryPoint
class SimpleScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySimpleScanBinding
    private val viewModel: SimpleScanViewModel by viewModels()

    private lateinit var cameraView: CameraView<*>
    private val scannerInitialized = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        binding.scanFlashBtn.setOnClickListener { viewModel.changeCameraFlash() }
        binding.scanFlipCameraBtn.setOnClickListener { viewModel.changeCameraState() }
        binding.scanChangeFocusBtn.setOnClickListener { viewModel.changeCameraAutoFocus() }
        binding.troubleScanningBtn.setOnClickListener { viewModel.troubleScanningPressed() }

        viewModel.scannerOptionsFlow
            .flowWithLifecycle(lifecycle)
            .onEach(this::updateScanner)
            .launchIn(lifecycleScope)


        viewModel.sideEffectsFlow
            .flowWithLifecycle(lifecycle)
            .onEach(this::onSideEffect)
            .launchIn(lifecycleScope)
    }

    private fun onSideEffect(sideEffect: SimpleScanViewModel.SideEffect) {
        logcat { "sideEffect: $sideEffect" }
        when (sideEffect) {
            is ScanTrouble -> {
                stopScanning()
                showManualInputDialog()
            }
            is BarcodeDetected -> {
                val intent = Intent().putExtra(KEY_SCANNED_BARCODE, sideEffect.barcode)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startScanning()
    }

    override fun onPause() {
        super.onPause()
        stopScanning()
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

    private fun updateScanner(options: SimpleScanScannerOptions) {
        logcat { "options: $options" }
        // If not initialized yet, initialize the scanner
        if (!scannerInitialized.getAndSet(true)) {
            setupBarcodeScanner(options)
        }
        applyScannerOptions(options)
    }

    private fun applyScannerOptions(options: SimpleScanScannerOptions) {
        cameraView.toggleCamera(
            when (options.cameraState) {
                CameraState.Back -> Camera.CameraInfo.CAMERA_FACING_BACK
                CameraState.Front -> Camera.CameraInfo.CAMERA_FACING_FRONT
            }
        )

        // flash
        updateFlashStatus(options.flashEnabled)

        // autofocus
        updateAutofocusStatus(options.autoFocusEnabled)
    }

    private fun updateAutofocusStatus(autoFocusEnabled: Boolean) {
        val focusIconRes = CameraView.getAutofocusRes(autoFocusEnabled)

        binding.scanChangeFocusBtn.setImageResource(focusIconRes)
        cameraView.updateFocusModeSetting(autoFocusEnabled)
    }


    private fun updateFlashStatus(flashEnabled: Boolean) {
        val flashIconRes = CameraView.getFlashRes(flashEnabled)
        binding.scanFlashBtn.setImageResource(flashIconRes)
        cameraView.updateFlashSetting(flashEnabled)
    }

    private fun setupBarcodeScanner(options: SimpleScanScannerOptions) {
        // camera state
        cameraView = CameraView.of(this, binding.scanMlView, options.mlScannerEnabled)

        cameraView.attach(options.cameraState.value, options.flashEnabled, options.autoFocusEnabled)
        cameraView.barcodeScannedCallback = { barcode ->
            barcode?.let { viewModel.barcodeDetected(it) }
        }
    }

    private fun stopScanning() {
        updateFlashStatus(flashEnabled = false)

        cameraView.stopCameraPreview()
    }

    private fun startScanning() {
        cameraView.onResume()
        cameraView.startCameraPreview()
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
        MaterialAlertDialogBuilder(this@SimpleScanActivity)
            .setTitle(R.string.trouble_scanning)
            .setMessage(R.string.enter_barcode)
            .setView(view)
            .setPositiveButton(R.string.ok_button) { _, _ ->
                inputEditText.text?.toString()?.let {
                    viewModel.barcodeDetected(it)
                }
            }
            .setNegativeButton(R.string.cancel_button) { _, _ ->
                startScanning()
            }
            .show()
    }
}
