package openfoodfacts.github.scrachx.openfood.utils

import android.graphics.Bitmap
import openfoodfacts.github.scrachx.openfood.camera.FrameMetadata
import java.nio.ByteBuffer

interface InputInfo {
    fun getBitmap(): Bitmap
}

class CameraInputInfo(
        private val frameByteBuffer: ByteBuffer,
        private val frameMetadata: FrameMetadata
) : InputInfo {

    private var bitmap: Bitmap? = null

    @Synchronized
    override fun getBitmap(): Bitmap {
        return bitmap ?: let {
            bitmap = CameraUtils.convertToBitmap(
                    frameByteBuffer, frameMetadata.width, frameMetadata.height, frameMetadata.rotation
            )
            bitmap!!
        }
    }
}

class BitmapInputInfo(private val bitmap: Bitmap) : InputInfo {
    override fun getBitmap(): Bitmap {
        return bitmap
    }
}
