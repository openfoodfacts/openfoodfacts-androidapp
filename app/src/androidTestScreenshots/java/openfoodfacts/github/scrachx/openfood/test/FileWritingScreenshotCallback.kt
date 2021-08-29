package openfoodfacts.github.scrachx.openfood.test

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.PNG
import android.os.Environment
import android.util.Log
import openfoodfacts.github.scrachx.openfood.BuildConfig
import tools.fastlane.screengrab.ScreenshotCallback
import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.attribute.PosixFilePermissions.asFileAttribute
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.*

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
                screenshotFile.outputStream().buffered().use { fos ->
                    screenshot.compress(PNG, 100, fos)
                }
            } finally {
                screenshot.recycle()
            }
            Log.d(LOG_TAG, "Captured screenshot at '${screenshotFile.name}'")
        } catch (exception: Exception) {
            throw RuntimeException("Unable to capture screenshot.", exception)
        }
    }

    private fun getScreenshotFile(dir: Path, name: String): Path =
        dir.resolve("${name}_${dateFormat.format(Date())}.png")

    private fun getFilesDirectory(): Path {
        if (!isExternalStorageWritable()) {
            Log.e(LOG_TAG, "Can't write to external storage check your installation")
            throw IOException("Can't write to external storage")
        }
        val targetDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toPath()
            .resolve(BuildConfig.FLAVOR + "Screenshots")

        val internalDir = targetDirectory.resolve(screenshotParameter.locale.country)
        val directory = initializeDirectory(internalDir)

        return if (directory == null) {
            throw IOException("Unable to get a screenshot storage directory")
        } else {
            Log.d(LOG_TAG, "Using screenshot storage directory: ${directory.absolutePathString()}")
            directory
        }
    }

    companion object {
        private val LOG_TAG = FileWritingScreenshotCallback::class.simpleName
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")

        private fun initializeDirectory(dir: Path): Path? {
            try {
                createPathTo(dir)
                if (dir.isDirectory() && dir.isWritable()) return dir
            } catch (exception: IOException) {
                Log.e(LOG_TAG, "Failed to initialize directory: ${dir.absolutePathString()}", exception)
            }
            return null
        }

        /* Checks if external storage is available for read and write */
        private fun isExternalStorageWritable() =
            Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

        private fun createPathTo(dir: Path) {
            dir.createDirectories(asFileAttribute(PosixFilePermissions.fromString("rwxr-xr--")))
        }
    }
}