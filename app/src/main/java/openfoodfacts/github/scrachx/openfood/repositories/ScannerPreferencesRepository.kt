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

    var autoFocusPref
        get() = cameraPrefs.getBoolean(SETTING_FOCUS, true)
        set(value) = cameraPrefs.edit { putBoolean(SETTING_FOCUS, value) }

    var flashPref
        get() = cameraPrefs.getBoolean(SETTING_FLASH, false)
        set(value) = cameraPrefs.edit { putBoolean(SETTING_FLASH, value) }

    var cameraPref
        get() = CameraState.fromInt(cameraPrefs.getInt(SETTING_STATE, CameraState.Back.value))
        set(value) = cameraPrefs.edit { putInt(SETTING_STATE, value.value) }

    var ringPref
        get() = cameraPrefs.getBoolean(SETTING_RING, false)
        set(value) = cameraPrefs.edit { putBoolean(SETTING_RING, value) }

    val mlScannerEnabled: Boolean
        get() = BuildConfig.USE_MLKIT && appPrefs.getBoolean(context.getString(R.string.pref_scanner_mlkit_key), false)

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