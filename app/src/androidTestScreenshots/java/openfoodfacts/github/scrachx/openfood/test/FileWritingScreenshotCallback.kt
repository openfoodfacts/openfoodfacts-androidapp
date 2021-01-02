package openfoodfacts.github.scrachx.openfood.test

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.os.Environment
import android.util.Log
import openfoodfacts.github.scrachx.openfood.BuildConfig
import tools.fastlane.screengrab.ScreenshotCallback
import tools.fastlane.screengrab.file.Chmod
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

//
/**
 * Write screennshot files.
 */
class FileWritingScreenshotCallback internal constructor(
        private val screenshotParameter: ScreenshotParameter
) : ScreenshotCallback {

    override fun screenshotCaptured(screenshotName: String, screenshot: Bitmap) {
        try {
            val screenshotDirectory = getFilesDirectory()
            val screenshotFile = getScreenshotFile(
                    screenshotDirectory,
                    "${screenshotParameter.locale.country}_${screenshotParameter.locale.language}-$screenshotName"
            )
            try {
                BufferedOutputStream(FileOutputStream(screenshotFile)).use { fos ->
                    screenshot.compress(CompressFormat.PNG, 100, fos)
                    Chmod.chmodPlusR(screenshotFile)
                }
            } finally {
                screenshot.recycle()
            }
            Log.d(LOG_TAG, """Captured screenshot "${screenshotFile.name}"""")
        } catch (exception: Exception) {
            throw RuntimeException("Unable to capture screenshot.", exception)
        }
    }

    private fun getScreenshotFile(screenshotDirectory: File, screenshotName: String) =
            File(screenshotDirectory, "${screenshotName}_${dateFormat.format(Date())}.png")

    @Throws(IOException::class)
    private fun getFilesDirectory(): File {
        if (!isExternalStorageWritable()) {
            Log.e(LOG_TAG, "Can't write to external storage check your installation")
            throw IOException("Can't write to external storage")
        }
        val targetDirectory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                BuildConfig.FLAVOR + "Screenshots"
        )
        val internalDir = File(targetDirectory, screenshotParameter.locale.country)
        val directory = initializeDirectory(internalDir)
        return if (directory == null) {
            throw IOException("Unable to get a screenshot storage directory")
        } else {
            Log.d(LOG_TAG, "Using screenshot storage directory: " + directory.absolutePath)
            directory
        }
    }

    companion object {
        private val LOG_TAG = FileWritingScreenshotCallback::class.simpleName
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
        private fun initializeDirectory(dir: File): File? {
            try {
                createPathTo(dir)
                if (dir.isDirectory && dir.canWrite()) return dir
            } catch (exception: IOException) {
                Log.e(LOG_TAG, "Failed to initialize directory: " + dir.absolutePath, exception)
            }
            return null
        }

        /* Checks if external storage is available for read and write */
        private fun isExternalStorageWritable() =
                Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

        @Throws(IOException::class)
        private fun createPathTo(dir: File) {
            if (!dir.exists() && !dir.mkdirs()) {
                throw IOException("Unable to create output dir: " + dir.absolutePath)
            } else {
                Chmod.chmodPlusRWX(dir)
            }
        }
    }
}