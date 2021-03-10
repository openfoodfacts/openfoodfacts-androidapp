package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager
import openfoodfacts.github.scrachx.openfood.utils.Utils.makeOrGetPictureDirectory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * File Downloader class which is used to download a file and
 * write response to the disk.
 */
object FileDownloader {
    private val LOG_TAG = FileDownloader::class.simpleName

    /**
     * Downloads a file from the given fileUrl.
     * If file is found write to disk and then return it via [Maybe].
     *
     * Network operations are done via [Schedulers.io]
     *
     * To use the result for UI updated remember to *OBSERVE ON MAIN THREAD*
     *
     * @param context
     * @param fileUrl provides the URL of the file to download.
     * @return [Maybe]
     */
    fun download(context: Context, fileUrl: String) = CommonApiManager.productsApi
            .downloadFile(fileUrl)
            .flatMapMaybe { responseBody ->
                Log.d(LOG_TAG, "server contacted and has file")
                val writtenToDisk = writeResponseBodyToDiskSync(context, responseBody, fileUrl)
                if (writtenToDisk != null) {
                    Log.d(LOG_TAG, "file download was a success $writtenToDisk")
                    Maybe.just(writtenToDisk)
                } else {
                    Maybe.empty()
                }
            }
            .doOnError { Log.e(LOG_TAG, "error", it) }
            .subscribeOn(Schedulers.io()) // IO operation -> Schedulers.io()

    /**
     * A method to write the response body to disk.
     *
     * @param context: [Context] of the class.
     * @param body: [ResponseBody] from the call.
     * @param request: url of the downloaded file.
     * @return [File] that has been written to the disk.
     */
    private fun writeResponseBodyToDiskSync(context: Context, body: ResponseBody, request: String): File? {
        val requestUri = request.toUri()
        val writtenFile = File(makeOrGetPictureDirectory(context), "${System.currentTimeMillis()}-${requestUri.lastPathSegment}")
        try {
            body.byteStream().use { inputStream ->
                FileOutputStream(writtenFile).use { outputStream ->
                    inputStream.copyTo(outputStream, 4 * 1024)
                    outputStream.flush()
                    return writtenFile
                }
            }
        } catch (e: IOException) {
            Log.w(LOG_TAG, "Could not write file $writtenFile to disk.", e)
            return null
        }
    }
}