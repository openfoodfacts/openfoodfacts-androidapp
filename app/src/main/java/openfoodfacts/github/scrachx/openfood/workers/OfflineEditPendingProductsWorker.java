package openfoodfacts.github.scrachx.openfood.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProductDao;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.splash.SplashActivity;

public class OfflineEditPendingProductsWorker extends Worker {
    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param appContext the application {@link Context}
     * @param workerParams the set of {@link WorkerParameters}
     */
    public OfflineEditPendingProductsWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        OfflineSavedProductDao offlineSavedProduct = Utils.getAppDaoSession(getApplicationContext()).getOfflineSavedProductDao();
        List<OfflineSavedProduct> listSaveProduct = offlineSavedProduct.loadAll();

        if (listSaveProduct.size() > 0) {
            String text = getApplicationContext().getResources().getQuantityString(R.plurals.offline_notification_count, listSaveProduct.size(), listSaveProduct.size());
            sendNotification(text);
        }

        return Result.success();
    }

    private void sendNotification(String message) {

        Intent notifyIntent = new Intent(getApplicationContext(), SplashActivity.class);
        // Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Create the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //If on Oreo then notification required a notification channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "default")
            .setContentTitle(getApplicationContext().getString(R.string.offline_notification_title))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(notifyPendingIntent)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message))
            .setAutoCancel(true);

        NotificationManagerCompat.from(getApplicationContext()).notify(1, notification.build());
    }
}
