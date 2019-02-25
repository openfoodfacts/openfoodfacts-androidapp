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

import com.opencsv.CSVReader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.OfflineProduct;
import openfoodfacts.github.scrachx.openfood.models.OfflineProductDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
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
    String url;
    int index;

    public DownloadOfflineProductService() {
        super("DownloadOfflineProductService");
        broadcastIntent = new Intent(IDENTIFIER);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        url = intent.getStringExtra("url");
        index = intent.getIntExtra("index", -1);
        isDownloadOfflineProductServiceRunning = true;
        settings = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        //configuring notifications
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

//        checking for which process to start
        if (!settings.getBoolean(url + "download", false)) {
//            start downloading
            doTask();
        } else {
//            start extraction
            extract();
        }
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
        String fileURL = getString(R.string.website) + getString(R.string.offline_excerpt_url) + url;
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

    //    doInBackground of DownloadZipFileTask
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

    private void extract() {
        unzip(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + url), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());

    }

    public void unzip(File file, String _location) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ZipInputStream zin = new ZipInputStream(inputStream);

            byte b[] = new byte[1024];
            String string = "";

            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v("Decompress", "Unzipping " + ze.getName());

                if (ze.isDirectory()) {
                    _dirChecker(_location, ze.getName());
                } else {

                    if (!new File(_location).isDirectory()) {
                        new File(_location).mkdirs();
                    }
                    FileOutputStream fout = new FileOutputStream(_location + File.separator + ze.getName());
                    string = ze.getName();
                    BufferedInputStream in = new BufferedInputStream(zin);
                    BufferedOutputStream out = new BufferedOutputStream(fout);
                    int n;
                    while ((n = in.read(b, 0, 1024)) >= 0) {
                        out.write(b, 0, n);
                    }
                    zin.closeEntry();
                    out.close();
                }

            }
            builder.setContentText(getString(R.string.txtExtracted))
                    .setProgress(0, 0, false)
                    .setOngoing(false);
            notificationManager.notify(7, builder.build());

            new saveCSVToDb(new File(_location + File.separator + string), settings, DownloadOfflineProductService.this).execute();
            zin.close();
        } catch (Exception e) {
            Log.e("Decompress", "unzip", e);
            Intent intent = new Intent(DownloadOfflineProductService.this, MainActivity.class);
            intent.putExtra("from_extract_service", "retry");
            PendingIntent pendingIntent = PendingIntent.getActivity(DownloadOfflineProductService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentText(getString(R.string.txtError))
                    .setProgress(0, 0, false)
                    .addAction(R.mipmap.ic_launcher, getString(R.string.txtRetry), pendingIntent)
                    .setOngoing(false);
            notificationManager.notify(7, builder.build());
            broadcastIntent.putExtra(DOWNLOAD_PROGRESS_UPDATE_KEY, -1);
            broadcastIntent.putExtra("index", index);
            sendBroadcast(broadcastIntent);
        }

    }

    private void _dirChecker(String _location, String dir) {
        File f = new File(_location + dir);

        if (!f.isDirectory()) {
            f.mkdirs();
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
            saveToDisk(urls[0], url);
            return null;
        }

        protected void onProgressUpdate(Pair<Integer, Long>... progress) {

            Log.d(TAG, progress[0].second + " ");

            if (progress[0].first == 100) {
                Intent intent = new Intent(DownloadOfflineProductService.this, MainActivity.class);
                intent.putExtra("from_download_service", "start_extraction");
                builder.setContentText(getString(R.string.txtDownloaded))
                        .setOngoing(false)
                        .setProgress(0, 0, false);
                notificationManager.notify(7, builder.build());
                //sending updates to fragment
                broadcastIntent.putExtra(DOWNLOAD_PROGRESS_UPDATE_KEY, 200);
                broadcastIntent.putExtra("index", index);
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
                    broadcastIntent.putExtra("index", index);
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
                broadcastIntent.putExtra("index", index);
                sendBroadcast(broadcastIntent);
            }

        }

        public void doProgress(Pair<Integer, Long> progressDetails) {
            publishProgress(progressDetails);
        }

        @Override
        protected void onPostExecute(String result) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(url + "download", true);
            editor.apply();
            extract();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class saveCSVToDb extends AsyncTask<Void, Integer, Boolean> {
        File file;
        @SuppressLint("StaticFieldLeak")
        Context context;
        SharedPreferences settings;
        OfflineProductDao mOfflineProductDao;

        private saveCSVToDb(File file, SharedPreferences settings, Context context) {
            this.file = file;
            this.settings = settings;
            this.context = context;
            mOfflineProductDao = Utils.getAppDaoSession(context).getOfflineProductDao();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.txtSaving))
                    .setProgress(0, 0, true)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher);

            notificationManager.notify(7, builder.build());
        }

        @Override
        protected void onPostExecute(Boolean is) {
            super.onPostExecute(is);
            isDownloadOfflineProductServiceRunning = false;
            if (is) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(url + "extract", true);
                editor.apply();
                builder.setContentText(getString(R.string.txtSaved))
                        .setProgress(0, 0, false)
                        .setOngoing(false);
                notificationManager.notify(7, builder.build());
                broadcastIntent.putExtra(DOWNLOAD_PROGRESS_UPDATE_KEY, 100);
                broadcastIntent.putExtra("index", index);
                sendBroadcast(broadcastIntent);
            } else {
                Intent intent = new Intent(DownloadOfflineProductService.this, MainActivity.class);
                intent.putExtra("from_extract_service", "retry");
                PendingIntent pendingIntent = PendingIntent.getActivity(DownloadOfflineProductService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentText(getString(R.string.txtError))
                        .setProgress(0, 0, false)
                        .addAction(R.mipmap.ic_launcher, getString(R.string.txtRetry), pendingIntent)
                        .setOngoing(false);
                notificationManager.notify(7, builder.build());
                broadcastIntent.putExtra(DOWNLOAD_PROGRESS_UPDATE_KEY, -1);
                broadcastIntent.putExtra("index", index);
                sendBroadcast(broadcastIntent);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            builder.setContentText(getString(R.string.txtSaving))
                    .setProgress(100, progress[0], false)
                    .setOngoing(true);
            notificationManager.notify(7, builder.build());
            broadcastIntent.putExtra(DOWNLOAD_PROGRESS_UPDATE_KEY, progress[0]);
            broadcastIntent.putExtra("index", index);
            sendBroadcast(broadcastIntent);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //parsing csv file
            try (CSVReader reader = new CSVReader(new FileReader(file.getAbsolutePath()), '\t')) {
                List<String[]> records = reader.readAll();
                List<OfflineProduct> list = new ArrayList<>();
                Iterator<String[]> iterator = records.iterator();
                // To skip header in the csv file
                iterator.hasNext();
                long size = records.size();
                long count = 0;
                int progress = 0;
                while (iterator.hasNext()) {
                    String[] record = iterator.next();
                    OfflineProduct offlineProduct = new OfflineProduct(record[1], record[3], record[0], record[2], record[4], url);
                    list.add(offlineProduct);
                    if (count % 15000 == 0) {
                        // saving to db
                        mOfflineProductDao.insertOrReplaceInTx(list);
                        list.clear();
                    }
                    count++;
                    if ((int) ((count * 100) / size) > progress + 1) {
                        progress = (int) ((count * 100) / size);
                        publishProgress(progress);
                    }
                    Log.d(TAG, "progress " + ((float) count / (float) size) * 100 + "% ");
                }
                mOfflineProductDao.insertOrReplaceInTx(list);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

}