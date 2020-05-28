package openfoodfacts.github.scrachx.openfood.jobs;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.services.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * File Downloader class which is used to download a file and
 * write response to the disk.
 */
public class FileDownloader {
    private final Context context;

    public interface FileReceiver {
        void onFileDownloaded(File file);
    }

    /**
     * Constructor of the class used to initialize the objects.
     *
     * @param context : {@link Context} to be set, cannot be null.
     */
    public FileDownloader(Context context) {
        this.context = context;
    }

    /**
     * A method to download the file using fileUrl and to callback
     * the FileReceiver interface method fileDownloaded.
     *
     * @param fileUrl provides the URL of the file to download.
     * @param callback is called if the file is downloaded with success, cannot be null.
     */
    public void download(@NonNull String fileUrl, @NonNull FileReceiver callback) {
        OpenFoodAPIService client = CommonApiManager.getInstance().getOpenFoodApiService();
        final Call<ResponseBody> responseBodyCall = client.downloadFile(fileUrl);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(FileDownloader.class.getSimpleName(), "server contacted and has file");
                    File writtenToDisk = writeResponseBodyToDisk(context, response.body(), fileUrl);
                    if (writtenToDisk != null) {
                        callback.onFileDownloaded(writtenToDisk);
                    }
                    Log.d(FileDownloader.class.getSimpleName(), "file download was a success " + writtenToDisk);
                } else {
                    Log.d(FileDownloader.class.getSimpleName(), "server contact failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(FileDownloader.class.getSimpleName(), "error");
            }
        });
    }

    /**
     * A method to write the response body to disk.
     *
     * @param context: {@link Context} of the class.
     * @param body: {@link ResponseBody} from the call.
     * @param url: url of the downloaded file.
     * @return {@link File} that has been written to the disk.
     */
    private File writeResponseBodyToDisk(Context context, ResponseBody body, String url) {
        final Uri decode = Uri.parse(url);
        File res = new File(Utils.makeOrGetPictureDirectory(context), System.currentTimeMillis() + "-" + decode.getLastPathSegment());
        try {
            InputStream inputStream = null;
            try (OutputStream outputStream = new FileOutputStream(res)) {
                byte[] fileReader = new byte[4096];
                inputStream = body.byteStream();

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                }

                outputStream.flush();

                return res;
            } catch (IOException e) {
                return null;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            Log.w(FileDownloader.class.getSimpleName(), "writeResponseBodyToDisk", e);
            return null;
        }
    }
}
