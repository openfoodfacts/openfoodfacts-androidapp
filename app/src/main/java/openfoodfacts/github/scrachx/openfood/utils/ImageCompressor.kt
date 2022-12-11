package openfoodfacts.github.scrachx.openfood.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

internal object ImageCompressor {
    const val REQUIRED_SIZE = 1200

    @JvmStatic
    fun compress(fileUrl: String): String? {
        val decodedBitmap = decodeFile(File(fileUrl))
        if (decodedBitmap == null) {
            logcat(LogPriority.ERROR) { "$fileUrl not found" }
            return null
        }
        val smallFileFront = File(fileUrl.replace(".png", "_small.png"))
        try {
            FileOutputStream(smallFileFront).use {
                decodedBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        } catch (e: IOException) {
            logcat(LogPriority.ERROR) { e.asLog() }
        }
        return smallFileFront.toString()
    }

    /**
     * Decodes image and scales it to reduce memory consumption
     */
    fun decodeFile(file: File): Bitmap? {
        try {
            // Decode image size
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(FileInputStream(file), null, options)

            // The new size we want to scale to
            // Find the correct scale value. It should be the power of 2.
            var scale = 1
            while (options.outWidth / scale / 2 >= REQUIRED_SIZE &&
                options.outHeight / scale / 2 >= REQUIRED_SIZE
            ) {
                scale *= 2
            }

            // Decode with inSampleSize
            val o2 = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            return BitmapFactory.decodeStream(FileInputStream(file), null, o2)
        } catch (e: FileNotFoundException) {
            logcat(LogPriority.ERROR) { "Error while decoding file $file: " + e.asLog() }
            return null
        }
    }
}