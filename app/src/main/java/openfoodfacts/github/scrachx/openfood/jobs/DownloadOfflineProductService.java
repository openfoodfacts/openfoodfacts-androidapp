package openfoodfacts.github.scrachx.openfood.jobs;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadOfflineProductService extends IntentService {

    private static final String TAG = "DOPService";
    public static final String IDENTIFIER = "INTENT_IDENTIFIER_FOR_BRAODCASTING";
    public static final String DOWNLOAD_PROGRESS_UPDATE_KEY = "PROGRESS_UPDATE_FOR_DOWNLOADING";
    public static boolean isDownloadOfflineProductServiceRunning = false;
    SharedPreferences settings;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;

    DownloadZipFileTask downloadZipFileTask;

    Intent broadcastIntent;

    public DownloadOfflineProductService() {
        super("DownloadOfflineProductService");
        broadcastIntent = new Intent(IDENTIFIER);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        isDownloadOfflineProductServiceRunning = true;
        settings = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelId = "channel";
            CharSequence channelName = getString(R.string.notification_channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        builder = new NotificationCompat.Builder(this, "channel");

        builder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.txtDownloading))
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher);

        doTask();
    }

    private void doTask() {

        //notify downloading is started
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.txtDownloading))
                .setProgress(100, 0, true)
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher);

        notificationManager.notify(7, builder.build());

        //downloading data
        OpenFoodAPIClient client = new OpenFoodAPIClient(this);
        String fileURL = "http://fr.openfoodfacts.org/data/offline/fr.openfoodfacts.org.products.small.zip";
        client.getAPIService().downloadFileWithDynamicUrlSync(fileURL)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            //downloading completed .. notify processing is started!
                            Log.d(TAG, "server contacted and has file");
                            downloadZipFileTask = new DownloadZipFileTask();
                            downloadZipFileTask.execute(response.body());

                        } else {
                            //download error .. notify and handle actions
                            isDownloadOfflineProductServiceRunning = false;
                            Log.d(TAG, "server contact failed");
                            Intent intent = new Intent(DownloadOfflineProductService.this, MainActivity.class);
                            intent.putExtra("from_download_service", "retry");
                            PendingIntent pendingIntent = PendingIntent.getActivity(DownloadOfflineProductService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            builder.setContentText(getString(R.string.txtConnectionFailed))
                                    .setOngoing(false)
                                    .addAction(R.mipmap.ic_launcher, getString(R.string.txtRetry), pendingIntent);
                            notificationManager.notify(7, builder.build());
                            broadcastIntent.putExtra(DOWNLOAD_PROGRESS_UPDATE_KEY, -1);
                            sendBroadcast(broadcastIntent);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        //download error .. notify and handle actions
                        isDownloadOfflineProductServiceRunning = false;
                        Log.d(TAG, "server contact failed");
                        Intent intent = new Intent(DownloadOfflineProductService.this, MainActivity.class);
                        intent.putExtra("from_download_service", "retry");
                        PendingIntent pendingIntent = PendingIntent.getActivity(DownloadOfflineProductService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        builder.setContentText(getString(R.string.txtConnectionFailed))
                                .setOngoing(false)
                                .addAction(R.mipmap.ic_launcher, getString(R.string.txtRetry), pendingIntent);
                        notificationManager.notify(7, builder.build());
                        broadcastIntent.putExtra(DOWNLOAD_PROGRESS_UPDATE_KEY, -1);
                        sendBroadcast(broadcastIntent);
                    }
                });
    }

    private void saveToDisk(ResponseBody body, String filename) {
        try {

            File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(destinationFile);
                byte data[] = new byte[4096];
                int count;
                int progress = 0;
                long fileSize = body.contentLength();
                Log.d(TAG, "File Size=" + fileSize);
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                    progress += count;
                    Pair<Integer, Long> pairs = new Pair<>(progress, fileSize);
                    downloadZipFileTask.doProgress(pairs);
                    Log.d(TAG, "Progress: " + progress + "/" + fileSize + " >>>> " + (float) progress / fileSize);
                }

                outputStream.flush();

                Log.d(TAG, destinationFile.getParent());
                Pair<Integer, Long> pairs = new Pair<>(100, 100L);
                downloadZipFileTask.doProgress(pairs);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Pair<Integer, Long> pairs = new Pair<>(-1, Long.valueOf(-1));
                downloadZipFileTask.doProgress(pairs);
                Log.d(TAG, "Failed to save the file!");
                return;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to save the file!");
            return;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadZipFileTask extends AsyncTask<ResponseBody, Pair<Integer, Long>, String> {

        int prevProgress = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(ResponseBody... urls) {
            //Copy you logic to calculate progress and call
            saveToDisk(urls[0], "fr.openfoodfacts.org.products.small.zip");
            return null;
        }

        protected void onProgressUpdate(Pair<Integer, Long>... progress) {

            Log.d(TAG, progress[0].second + " ");

            if (progress[0].first == 100) {
                settings.edit().putBoolean("is_data_downloaded", true).apply();
                Intent intent = new Intent(DownloadOfflineProductService.this, MainActivity.class);
                intent.putExtra("from_download_service", "start_extraction");
                PendingIntent pendingIntent = PendingIntent.getActivity(DownloadOfflineProductService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentText(getString(R.string.txtDownloaded))
                        .addAction(R.mipmap.ic_launcher, getString(R.string.txtExtractSave), pendingIntent)
                        .setOngoing(false)
                        .setProgress(0, 0, false);
                notificationManager.notify(7, builder.build());
                //sending updates to fragment
                broadcastIntent.putExtra(DOWNLOAD_PROGRESS_UPDATE_KEY, 100);
                sendBroadcast(broadcastIntent);
            }

            if (progress[0].second > 0) {
                int currentProgress = (int) ((double) progress[0].first / (double) progress[0].second * 100);
                if (currentProgress > prevProgress) {
                    builder.setProgress(100, currentProgress, false)
                            .setOngoing(true);
                    notificationManager.notify(7, builder.build());
                    //sending updates to fragment
                    Log.d("aaaaaaaaaaaaa", currentProgress + "");
                    broadcastIntent.putExtra(DOWNLOAD_PROGRESS_UPDATE_KEY, currentProgress);
                    sendBroadcast(broadcastIntent);
                    prevProgress = currentProgress;
                }
            }

            if (progress[0].first == -1) {
                Intent intent = new Intent(DownloadOfflineProductService.this, MainActivity.class);
                intent.putExtra("from_download_service", "retry");
                PendingIntent pendingIntent = PendingIntent.getActivity(DownloadOfflineProductService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentText(getString(R.string.txtConnectionFailed))
                        .setOngoing(false)
                        .addAction(R.mipmap.ic_launcher, getString(R.string.txtRetry), pendingIntent)
                        .setProgress(0, 0, false);
                notificationManager.notify(7, builder.build());
                //sending updates to fragment
                broadcastIntent.putExtra(DOWNLOAD_PROGRESS_UPDATE_KEY, -1);
                sendBroadcast(broadcastIntent);
            }

        }

        public void doProgress(Pair<Integer, Long> progressDetails) {
            publishProgress(progressDetails);
        }

        @Override
        protected void onPostExecute(String result) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("is_data_downloaded", true);
            editor.apply();
            isDownloadOfflineProductServiceRunning = false;
        }
    }


}