package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import androidx.core.content.edit
import com.google.zxing.BarcodeFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.CameraState
import openfoodfacts.github.scrachx.openfood.utils.getAppPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScannerPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val cameraPrefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    private val appPrefs by lazy { context.getAppPreferences() }

    fun saveAutoFocusPref(value: Boolean) {
        cameraPrefs.edit {
            putBoolean(SETTING_FOCUS, value)
        }
    }

    fun getAutoFocusPref() = cameraPrefs.getBoolean(SETTING_FOCUS, true)

    fun saveFlashPref(value: Boolean) {
        cameraPrefs.edit {
            putBoolean(SETTING_FLASH, value)
        }
    }

    fun getFlashPref() = cameraPrefs.getBoolean(SETTING_FLASH, false)

    fun saveCameraPref(camera: CameraState) {
        cameraPrefs.edit {
            putInt(SETTING_STATE, camera.value)
        }
    }

    fun getCameraPref() = CameraState.fromInt(cameraPrefs.getInt(SETTING_STATE, CameraState.Back.value))

    fun saveRingPref(value: Boolean) {
        cameraPrefs.edit {
            putBoolean(SETTING_RING, value)
        }
    }

    fun getRingPref() = cameraPrefs.getBoolean(SETTING_RING, false)

    fun isMlScannerEnabled(): Boolean {
        return BuildConfig.USE_MLKIT && appPrefs.getBoolean(context.getString(R.string.pref_scanner_mlkit_key), false)
    }

    companion object {
        private const val PREFS_NAME = "camera"
        private const val SETTING_RING = "ring"
        private const val SETTING_FLASH = "flash"
        private const val SETTING_FOCUS = "focus"
        private const val SETTING_STATE = "cameraState"
        val BARCODE_FORMATS = listOf(
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.EAN_13,
            BarcodeFormat.EAN_8,
            BarcodeFormat.RSS_14,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.CODE_128,
            BarcodeFormat.ITF
        )
    }
}