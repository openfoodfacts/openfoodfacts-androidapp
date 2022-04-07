package openfoodfacts.github.scrachx.openfood.utils

import android.view.ViewStub
import androidx.appcompat.app.AppCompatActivity
import openfoodfacts.github.scrachx.openfood.features.scan.CameraView
import openfoodfacts.github.scrachx.openfood.features.scan.MLKitCameraView
import openfoodfacts.github.scrachx.openfood.features.scan.ZXCameraView
import openfoodfacts.github.scrachx.openfood.repositories.ScannerPreferencesRepository

fun ScannerPreferencesRepository.buildCameraView(activity: AppCompatActivity, viewStub: ViewStub): CameraView<*> {
    return when {
        // Prefer ml kit with fallback
        mlScannerEnabled -> MLKitCameraView(activity, viewStub)
        else -> ZXCameraView(activity, viewStub)
    }
}