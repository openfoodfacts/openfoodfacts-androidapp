package openfoodfacts.github.scrachx.openfood.jobs;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.*;

/**
* Download file from internet using file url
* and save it on disk
* */

public class FileDownloader {
    private Context context;

    public interface FileReceiver {
        void fileDownloaded(File file);
    }

    public FileDownloader(Context context) {
        this.context = context;
    }

    /**
    * Downloads a file from internet using the file Url
    *
    *  @param fileUrl Url of the file to be downloaded
    *  @param callback object of class FileReciever
    * */

    public void download(String fileUrl, FileReceiver callback) {
        OpenFoodAPIService client = CommonApiManager.getInstance().getOpenFoodApiService();
        final Call<ResponseBody> responseBodyCall = client.downloadFile(fileUrl);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(FileDownloader.class.getSimpleName(), "server contacted and has file");
                    File writtenToDisk = writeResponseBodyToDisk(context, response.body(), fileUrl);
                    if (writtenToDisk != null) {
                        callback.fileDownloaded(writtenToDisk);
                    }
                    Log.d(FileDownloader.class.getSimpleName(), "file download was a success " + writtenToDisk);
                } else {
                    Log.d(FileDownloader.class.getSimpleName(), "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(FileDownloader.class.getSimpleName(), "error");
            }
        });
    }

    /**
    * Creates space in disk for file downloaded from download function above
     * @param context context
     * @param body network response for recieved while downloading file from internet
     * @param url url of the file which is downloaded from internet
     * */

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
            Log.w(FileDownloader.class.getSimpleName(), "writeResponseBodyToDisk",e);
            return null;
        }
    }
}
