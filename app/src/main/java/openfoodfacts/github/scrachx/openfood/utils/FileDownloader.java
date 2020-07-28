package openfoodfacts.github.scrachx.openfood.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;

/**
 * File Downloader class which is used to download a file and
 * write response to the disk.
 */
public class FileDownloader {
    private FileDownloader() {
        // utility class
    }

    /**
     * Downloads a file from the given fileUrl.
     * If file is found write to disk and then return it via {@link Maybe}.
     * <p>
     * Network operations are done via {@link Schedulers#io()}
     * </p>
     * <p>
     * To use the result for UI updated remember to <em>OBSERVE ON {@link AndroidSchedulers#mainThread()}</em>
     * </p>
     *
     * @param context
     * @param fileUrl provides the URL of the file to download.
     * @return {@link Maybe}
     */
    public static Maybe<File> download(@NonNull Context context, @NonNull String fileUrl) {
        return CommonApiManager.getInstance()
            .getProductsApi()
            .downloadFileSingle(fileUrl)
            .flatMapMaybe(responseBody -> {
                if (responseBody != null) {
                    Log.d(FileDownloader.class.getSimpleName(), "server contacted and has file");
                    File writtenToDisk = writeResponseBodyToDiskSync(context, responseBody, fileUrl);
                    if (writtenToDisk != null) {
                        Log.d(FileDownloader.class.getSimpleName(), "file download was a success " + writtenToDisk);
                        return Maybe.just(writtenToDisk);
                    } else {
                        return Maybe.empty();
                    }
                } else {
                    Log.d(FileDownloader.class.getSimpleName(), "server contact failed");
                    return Maybe.empty();
                }
            })
            .doOnError(throwable -> Log.e(FileDownloader.class.getSimpleName(), "error"))
            .subscribeOn(Schedulers.io()); // IO operation -> Schedulers.io()
    }

    /**
     * A method to write the response body to disk.
     *
     * @param context: {@link Context} of the class.
     * @param body: {@link ResponseBody} from the call.
     * @param url: url of the downloaded file.
     * @return {@link File} that has been written to the disk.
     */
    private static File writeResponseBodyToDiskSync(Context context, ResponseBody body, String url) {
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
