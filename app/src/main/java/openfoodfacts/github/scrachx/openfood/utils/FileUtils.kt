package openfoodfacts.github.scrachx.openfood.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.core.app.NotificationCompat
import openfoodfacts.github.scrachx.openfood.AppFlavor
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity
import openfoodfacts.github.scrachx.openfood.features.scanhistory.ScanHistoryActivity
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.jetbrains.annotations.Contract
import java.io.IOException


fun isLocaleFile(url: String?) = url?.startsWith(LOCALE_FILE_SCHEME) ?: false

fun isAbsoluteUrl(url: String?) = url?.startsWith("/") ?: false

@Contract(pure = true)
@CheckResult
fun getCsvFolderName() = when (AppFlavor.currentFlavor) {
    AppFlavor.OPFF -> "Open Pet Food Facts"
    AppFlavor.OPF -> "Open Products Facts"
    AppFlavor.OBF -> "Open Beauty Facts"
    AppFlavor.OFF -> "Open Food Facts"
    else -> "Open Food Facts"
}

fun writeListToFile(context: Context, productList: ProductLists, csvUri: Uri) {
    var success: Boolean
    val outputStream = context.contentResolver.openOutputStream(csvUri) ?: error("File path must not be null.")
    try {
        CSVPrinter(
            outputStream.bufferedWriter(),
            CSVFormat.DEFAULT.withHeader(*context.resources.getStringArray(R.array.your_products_headers))
        ).use { writer ->
            productList.products.forEach {
                writer.printRecord(it.barcode, it.productName, it.listName, it.productDetails)
            }
        }
        Toast.makeText(context, R.string.txt_your_listed_products_exported, Toast.LENGTH_LONG).show()
        success = true
    } catch (e: IOException) {
        success = false
        Log.e(ProductListActivity::class.simpleName, "Can't export to $csvUri.", e)
    }

    val downloadIntent = Intent(ACTION_VIEW).apply {
        flags = FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_NEW_TASK or FLAG_GRANT_READ_URI_PERMISSION
        data = csvUri
        type = "text/csv"
    }
    val notificationManager = createNotificationManager(context)
    if (success) {
        val builder = NotificationCompat.Builder(context, "export_channel")
            .setContentTitle(context.getString(R.string.notify_title))
            .setContentText(context.getString(R.string.notify_content))
            .setContentIntent(PendingIntent.getActivity(context, 4, downloadIntent, 0))
            .setSmallIcon(R.mipmap.ic_launcher)
        notificationManager.notify(8, builder.build())
    }
}

fun writeHistoryToFile(context: Context, productList: List<HistoryProduct>, csvUri: Uri) {
    var success = false
    val outputStream = context.contentResolver.openOutputStream(csvUri) ?: error("File path must not be null.")
    try {
        CSVPrinter(
            outputStream.bufferedWriter(),
            CSVFormat.DEFAULT.withHeader(*context.resources.getStringArray(R.array.headers))
        ).use { writer ->
            productList.forEach { writer.printRecord(it.barcode, it.title, it.brands) }
        }
        Toast.makeText(context, R.string.txt_history_exported, Toast.LENGTH_LONG).show()
        success = true
    } catch (e: IOException) {
        Log.e(ScanHistoryActivity.LOG_TAG, "Can't export to $csvUri.", e)
    }
    val downloadIntent = Intent(ACTION_VIEW).apply {
        flags = FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_NEW_TASK or FLAG_GRANT_READ_URI_PERMISSION
        data = csvUri
        type = "text/csv"
    }
    val notificationManager = createNotificationManager(context)
    if (success) {
        val builder = NotificationCompat.Builder(context, "export_channel")
            .setContentTitle(context.getString(R.string.notify_title))
            .setContentText(context.getString(R.string.notify_content))
            .setContentIntent(PendingIntent.getActivity(context, 0, downloadIntent, PendingIntent.FLAG_CANCEL_CURRENT))
            .setSmallIcon(R.mipmap.ic_launcher)
        notificationManager.notify(7, builder.build())
    }
}

const val LOCALE_FILE_SCHEME = "file://"

// TODO: Use constants and refactor
fun createNotificationManager(context: Context): NotificationManager {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel("downloadChannel", "ChannelCSV", importance)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = "export_channel"
        val channelName = context.getString(R.string.notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(channelId, channelName, importance)
        notificationChannel.description = context.getString(R.string.notify_channel_description)
        notificationManager.createNotificationChannel(notificationChannel)
    }
    return notificationManager
}




