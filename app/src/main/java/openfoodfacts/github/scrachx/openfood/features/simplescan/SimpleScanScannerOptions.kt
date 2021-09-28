package openfoodfacts.github.scrachx.openfood.features.simplescan

import openfoodfacts.github.scrachx.openfood.models.CameraState

data class SimpleScanScannerOptions(
    val mlScannerEnabled: Boolean,
    val cameraState: CameraState,
    val autoFocusEnabled: Boolean,
    val flashEnabled: Boolean
)