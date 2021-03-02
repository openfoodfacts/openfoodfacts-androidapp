@file:Suppress("DEPRECATION")

package openfoodfacts.github.scrachx.openfood.camera

import android.hardware.Camera
import com.google.android.gms.common.images.Size

/**
 * Stores a preview size and a corresponding same-aspect-ratio picture size. To avoid distorted
 * preview images on some devices, the picture size must be set to a size that is the same aspect
 * ratio as the preview size or the preview may end up being distorted. If the picture size is null,
 * then there is no picture size with the same aspect ratio as the preview size.
 */

data class CameraSizePair(val previewSize: Camera.Size, val pictureSize: Camera.Size?) {

    val preview: Size = Size(previewSize.width, previewSize.height)
    val picture: Size? = pictureSize?.let { Size(it.width, it.height) }

}
