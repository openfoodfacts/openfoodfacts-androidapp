package openfoodfacts.github.scrachx.openfood.features.simplescan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.models.CameraState
import openfoodfacts.github.scrachx.openfood.repositories.ScannerPreferencesRepository
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import javax.inject.Inject

@HiltViewModel
class SimpleScanViewModel @Inject constructor(
    private val scannerPrefsRepository: ScannerPreferencesRepository,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    private val _sideEffectsFlow = MutableSharedFlow<SideEffect>()
    val sideEffectsFlow = _sideEffectsFlow.asSharedFlow()

    private val _scannerOptionsFlow = MutableStateFlow(
        SimpleScanScannerOptions(
            mlScannerEnabled = scannerPrefsRepository.mlScannerEnabled,
            cameraState = scannerPrefsRepository.cameraPref,
            autoFocusEnabled = scannerPrefsRepository.autoFocusPref,
            flashEnabled = scannerPrefsRepository.flashPref
        )
    )
    val scannerOptionsFlow = _scannerOptionsFlow.asStateFlow()

    fun changeCameraAutoFocus() {
        val newValue = !_scannerOptionsFlow.value.autoFocusEnabled
        scannerPrefsRepository.autoFocusPref = newValue
        _scannerOptionsFlow.value = _scannerOptionsFlow.value.copy(
            autoFocusEnabled = newValue
        )
    }

    fun changeCameraFlash() {
        val newValue = !_scannerOptionsFlow.value.flashEnabled
        scannerPrefsRepository.flashPref = newValue
        _scannerOptionsFlow.value = _scannerOptionsFlow.value.copy(
            flashEnabled = newValue
        )
    }

    fun changeCameraState() {
        val newValue = when (_scannerOptionsFlow.value.cameraState) {
            CameraState.Front -> CameraState.Back
            CameraState.Back -> CameraState.Front
        }
        scannerPrefsRepository.cameraPref = newValue
        _scannerOptionsFlow.value = _scannerOptionsFlow.value.copy(
            cameraState = newValue
        )
    }

    fun barcodeDetected(barcode: String) {
        viewModelScope.launch(dispatchers.Default) {
            _sideEffectsFlow.emit(SideEffect.BarcodeDetected(barcode))
        }
    }

    fun troubleScanningPressed() {
        viewModelScope.launch(dispatchers.Default) {
            _sideEffectsFlow.emit(SideEffect.ScanTrouble)
        }
    }

    sealed class SideEffect {
        data class BarcodeDetected(val barcode: String) : SideEffect()
        object ScanTrouble : SideEffect()
    }
}
