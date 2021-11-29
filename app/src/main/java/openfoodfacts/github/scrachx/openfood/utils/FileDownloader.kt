package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.Utils.makeOrGetPictureDirectory
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * File Downloader class which is used to download a file and
 * write response to the disk.
 */
class FileDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productsAPI: ProductsAPI,
    private val coroutineDispatchers: CoroutineDispatchers,
) {

    companion object {
        private const val LOG_TAG = "FileDownloader"
    }

    /**
     * Downloads a file from the given fileUrl and stores it on disk.
     *
     * @param fileUrl provides the URL of the file to download
     * @return [Uri] in case of success and null otherwise
     */
    suspend fun download(fileUrl: String): Uri? {
        val file = withContext(coroutineDispatchers.io()) {
            val result = runCatching {
                productsAPI.downloadFile(fileUrl)
            }
            result.fold(
                onSuccess = { response ->
                    writeResponseBodyToDisk(response, fileUrl)
                },
                onFailure = {
                    Log.e(LOG_TAG, "error", it)
                    null
                }
            )
        }
        return withContext(coroutineDispatchers.main()) {
            file?.toUri()
        }
    }


    /**
     * A method to write the response body to disk.
     *
     * @param body: [ResponseBody] from the call.
     * @param request: url of the downloaded file.
     * @return [File] that has been written to the disk or null in case of failure
     */
    private fun writeResponseBodyToDisk(body: ResponseBody, request: String): File? {
        val requestUri = request.toUri()
        val writtenFile = File(makeOrGetPictureDirectory(context), "${System.currentTimeMillis()}-${requestUri.lastPathSegment}")
        return try {
            body.byteStream().use { input ->
                writtenFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            writtenFile
        } catch (e: IOException) {
            Log.w(LOG_TAG, "Could not write file $writtenFile to disk.", e)
            null
        }
    }
}
