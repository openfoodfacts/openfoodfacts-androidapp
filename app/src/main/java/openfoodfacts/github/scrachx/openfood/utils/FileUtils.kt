package openfoodfacts.github.scrachx.openfood.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.core.app.NotificationCompat
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity
import openfoodfacts.github.scrachx.openfood.features.scanhistory.ScanHistoryActivity
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.jetbrains.annotations.Contract
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.OutputStream

fun isLocaleFile(url: String?) = url?.startsWith(LOCALE_FILE_SCHEME) ?: false

fun isAbsoluteUrl(url: String?) = url?.startsWith("/") ?: false

@Contract(pure = true)
@CheckResult
fun getCsvFolderName() = when (BuildConfig.FLAVOR) {
    OPFF -> "Open Pet Food Facts"
    OPF -> "Open Products Facts"
    OBF -> "Open Beauty Facts"
    OFF -> "Open Food Facts"
    else -> "Open Food Facts"
}

fun writeListToFile(context: Context, productList: ProductLists, csvUri: Uri,outputStream: OutputStream) {
    var success: Boolean
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

    val downloadIntent = Intent(Intent.ACTION_VIEW)
    val notificationManager = ProductListActivity.createNotification(csvUri, downloadIntent, context)
    if (success) {
        val builder = NotificationCompat.Builder(context, "export_channel")
                .setContentTitle(context.getString(R.string.notify_title))
                .setContentText(context.getString(R.string.notify_content))
                .setContentIntent(PendingIntent.getActivity(context, 4, downloadIntent, 0))
                .setSmallIcon(R.mipmap.ic_launcher)
        notificationManager.notify(8, builder.build())
    }
}

fun writeHistoryToFile(context: Context, productList: List<HistoryProduct>, csvUri: Uri,outputStream: OutputStream) {
    var success = false

    try {
        CSVPrinter(outputStream.bufferedWriter(), CSVFormat.DEFAULT.withHeader(*context.resources.getStringArray(R.array.headers))).use { writer ->
            productList.forEach { writer.printRecord(it.barcode, it.title, it.brands) }
            Toast.makeText(context, R.string.txt_history_exported, Toast.LENGTH_LONG).show()
            success = true
        }
    } catch (e: IOException) {
        Log.e(ScanHistoryActivity.LOG_TAG, "Can't export to $csvUri.", e)
    }
    val downloadIntent = Intent(Intent.ACTION_VIEW)
    val notificationManager = ProductListActivity.createNotification(csvUri, downloadIntent, context)
    if (success) {
        val builder = NotificationCompat.Builder(context, "export_channel")
                .setContentTitle(context.getString(R.string.notify_title))
                .setContentText(context.getString(R.string.notify_content))
                .setContentIntent(PendingIntent.getActivity(context, 4, downloadIntent, 0))
                .setSmallIcon(R.mipmap.ic_launcher)
        notificationManager.notify(7, builder.build())
    }
}

const val LOCALE_FILE_SCHEME = "file://"