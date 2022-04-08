package openfoodfacts.github.scrachx.openfood.features.simplescan

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import openfoodfacts.github.scrachx.openfood.features.simplescan.SimpleScanViewModel.SideEffect
import openfoodfacts.github.scrachx.openfood.models.CameraState
import openfoodfacts.github.scrachx.openfood.repositories.ScannerPreferencesRepository
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchersTest
import openfoodfacts.github.scrachx.openfood.utils.InstantTaskExecutorExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.*

@ExtendWith(InstantTaskExecutorExtension::class)
@ExperimentalCoroutinesApi
class SimpleScanViewModelTest {

    private val prefsRepository = mock<ScannerPreferencesRepository>()
    private val dispatchers = CoroutineDispatchersTest()

    private lateinit var viewModel: SimpleScanViewModel

    @BeforeEach
    fun setup() {
        whenever(prefsRepository.getAutoFocusPref()).doReturn(true)
        whenever(prefsRepository.getFlashPref()).doReturn(true)
        whenever(prefsRepository.isMlScannerEnabled()).doReturn(false)
        whenever(prefsRepository.getCameraPref()).doReturn(CameraState.Back)

        viewModel = SimpleScanViewModel(prefsRepository, dispatchers)
    }

    @Test
    fun onInit_shouldEmitDefaultCameraOptions() = runBlockingTest {
        // GIVEN
        val flowItems = mutableListOf<SimpleScanScannerOptions>()
        val job = launch {
            viewModel.scannerOptionsFlow.toList(flowItems)
        }

        // THEN
        assertThat(flowItems.size).isEqualTo(1)
        assertThat(flowItems[0].autoFocusEnabled).isTrue()
        assertThat(flowItems[0].flashEnabled).isTrue()
        assertThat(flowItems[0].mlScannerEnabled).isFalse()
        assertThat(flowItems[0].cameraState).isEqualTo(CameraState.Back)
        job.cancel()
    }

    @Test
    fun changeCameraAutoFocus_shouldChangeAutoFocus() = runBlockingTest {
        // GIVEN
        val flowItems = mutableListOf<SimpleScanScannerOptions>()
        val job = launch {
            viewModel.scannerOptionsFlow.toList(flowItems)
        }

        // WHEN
        viewModel.changeCameraAutoFocus()

        // THEN
        verify(prefsRepository).saveAutoFocusPref(eq(false))
        assertThat(flowItems.size).isEqualTo(2)
        assertThat(flowItems[1].autoFocusEnabled).isFalse()
        job.cancel()
    }

    @Test
    fun changeCameraFlash_shouldChangeFlash() = runBlockingTest {
        // GIVEN
        val flowItems = mutableListOf<SimpleScanScannerOptions>()
        val job = launch {
            viewModel.scannerOptionsFlow.toList(flowItems)
        }

        // WHEN
        viewModel.changeCameraFlash()

        // THEN
        verify(prefsRepository).saveFlashPref(eq(false))
        assertThat(flowItems.size).isEqualTo(2)
        assertThat(flowItems[1].flashEnabled).isFalse()
        job.cancel()
    }

    @Test
    fun changeCameraState_shouldCameraState() = runBlockingTest {
        // GIVEN
        val flowItems = mutableListOf<SimpleScanScannerOptions>()
        val job = launch {
            viewModel.scannerOptionsFlow.toList(flowItems)
        }

        // WHEN
        viewModel.changeCameraState()

        // THEN
        verify(prefsRepository).saveCameraPref(eq(CameraState.Front))
        assertThat(flowItems.size).isEqualTo(2)
        assertThat(flowItems[1].cameraState).isEqualTo(CameraState.Front)
        job.cancel()
    }

    @Test
    fun barcodeDetected_shouldEmitRightEffect() = runBlockingTest {
        // GIVEN
        val givenBarcode = "qwerty"
        val flowItems = mutableListOf<SideEffect>()
        val job = launch {
            viewModel.sideEffectsFlow.toList(flowItems)
        }

        // WHEN
        viewModel.barcodeDetected(givenBarcode)

        // THEN
        assertThat(flowItems.size).isEqualTo(1)
        assertThat((flowItems[0] as SideEffect.BarcodeDetected).barcode).isEqualTo(givenBarcode)
        job.cancel()
    }

    @Test
    fun troubleScanningPressed_shouldEmitRightEffect() = runBlockingTest {
        // GIVEN
        val flowItems = mutableListOf<SideEffect>()
        val job = launch {
            viewModel.sideEffectsFlow.toList(flowItems)
        }

        // WHEN
        viewModel.troubleScanningPressed()

        // THEN
        assertThat(flowItems.size).isEqualTo(1)
        assertThat(flowItems[0] is SideEffect.ScanTrouble).isTrue()
        job.cancel()
    }
}
