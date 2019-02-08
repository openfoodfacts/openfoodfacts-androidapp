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

import com.opencsv.CSVReader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.OfflineProduct;
import openfoodfacts.github.scrachx.openfood.models.OfflineProductDao;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;


public class ExtractOfflineProductService extends IntentService {

    private static final String TAG = "EOPService";
    public static final String IDENTIFIER_ = "INTENT_IDENTIFIER__FOR_BRAODCASTING";
    public static final String EXTRACT_PROGRESS_UPDATE_KEY = "PROGRESS_UPDATE_FOR_EXTRACTING";
    public static boolean isExtractOfflineProductServiceRunning = false;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    SharedPreferences settings;
    Intent broadcastIntent;

    public ExtractOfflineProductService() {
        super("ExtractOfflineProductService");
        broadcastIntent = new Intent(IDENTIFIER_);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        isExtractOfflineProductServiceRunning = true;
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

        unzip(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "fr.openfoodfacts.org.products.small.zip"), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
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

            new saveCSVToDb(new File(_location + File.separator + string), settings, ExtractOfflineProductService.this).execute();
            zin.close();
        } catch (Exception e) {
            Log.e("Decompress", "unzip", e);
            Intent intent = new Intent(ExtractOfflineProductService.this, MainActivity.class);
            intent.putExtra("from_extract_service", "retry");
            PendingIntent pendingIntent = PendingIntent.getActivity(ExtractOfflineProductService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentText(getString(R.string.txtError))
                    .setProgress(0, 0, false)
                    .addAction(R.mipmap.ic_launcher, getString(R.string.txtRetry), pendingIntent)
                    .setOngoing(false);
            notificationManager.notify(7, builder.build());
            broadcastIntent.putExtra(EXTRACT_PROGRESS_UPDATE_KEY, -1);
            sendBroadcast(broadcastIntent);
        }

    }

    private void _dirChecker(String _location, String dir) {
        File f = new File(_location + dir);

        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
            isExtractOfflineProductServiceRunning = false;
            if (is) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("is_data_saved", true);
                editor.apply();
                builder.setContentText(getString(R.string.txtSaved))
                        .setProgress(0, 0, false)
                        .setOngoing(false);
                notificationManager.notify(7, builder.build());
                broadcastIntent.putExtra(EXTRACT_PROGRESS_UPDATE_KEY, 100);
                sendBroadcast(broadcastIntent);
            } else {
                Intent intent = new Intent(ExtractOfflineProductService.this, MainActivity.class);
                intent.putExtra("from_extract_service", "retry");
                PendingIntent pendingIntent = PendingIntent.getActivity(ExtractOfflineProductService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentText(getString(R.string.txtError))
                        .setProgress(0, 0, false)
                        .addAction(R.mipmap.ic_launcher, getString(R.string.txtRetry), pendingIntent)
                        .setOngoing(false);
                notificationManager.notify(7, builder.build());
                broadcastIntent.putExtra(EXTRACT_PROGRESS_UPDATE_KEY, -1);
                sendBroadcast(broadcastIntent);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            builder.setContentText(getString(R.string.txtSaving))
                    .setProgress(100, progress[0], false)
                    .setOngoing(true);
            notificationManager.notify(7, builder.build());
            broadcastIntent.putExtra(EXTRACT_PROGRESS_UPDATE_KEY, progress[0]);
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
                    OfflineProduct offlineProduct = new OfflineProduct(record[1], record[3], record[0], record[2], record[4]);
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