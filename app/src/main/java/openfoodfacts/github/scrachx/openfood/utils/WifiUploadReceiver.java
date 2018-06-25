package openfoodfacts.github.scrachx.openfood.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.SendProductDao;

/**
 * @author Prajwalm
 * @author Ross-holloway94
 *
 * @since 18 June 2018
 */


public class WifiUploadReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
     /*   if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction()) && WifiManager.WIFI_STATE_ENABLED == wifiState) {

            //context.startService(new Intent(context, WifiService.class));
            // Quick fix. TODO: Fix it the right way: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/1583
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { 
                context.startForegroundService(new Intent(context, WifiService.class)); 
            } else {
                context.startService(new Intent(context, WifiService.class));
            }
            
        }*/
        //Above code commented out by ross-holloway94 18 June 2018. Refer to issue #1583

    }

    public static class WifiService extends Service {

        private SendProductDao mSendProductDao;
        private List<SendProduct> listSaveProduct;

        @Override
        public void onCreate() {
            super.onCreate();
            startForeground(1, new Notification());
        }


        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if (intent != null) {
                mSendProductDao = Utils.getAppDaoSession(getApplicationContext()).getSendProductDao();
                listSaveProduct = mSendProductDao.loadAll();

                new Handler().postDelayed(() -> {


                    if (listSaveProduct.size() > 0) {
                        createNotification();
                    }


                }, 10000);
            }


            return START_NOT_STICKY;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }


        private void createNotification() {

            Intent intent = new Intent(this, UploadService.class);
            intent.setAction("UploadJob");
            String contentText = this.getResources().getQuantityString(R.plurals.offline_notification_count, listSaveProduct.size(), listSaveProduct.size());
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, contentText)
                    .setContentTitle(this.getString(R.string.offline_notification_title))
                    /*.setContentText(contentText)*/
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .addAction(R.drawable.ic_cloud_upload, "Upload", PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (mNotificationManager != null) {
                mNotificationManager.notify(9, builder.build());
            }
        }
    }
}
