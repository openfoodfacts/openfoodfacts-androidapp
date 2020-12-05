package openfoodfacts.github.scrachx.openfood.features.scanhistory

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Completable
import io.reactivex.CompletableSource
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityHistoryScanBinding
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.install
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.HistoryItem
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.models.HistoryProductDao
import openfoodfacts.github.scrachx.openfood.utils.SwipeController
import openfoodfacts.github.scrachx.openfood.utils.SwipeControllerActions
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.Utils.daoSession
import openfoodfacts.github.scrachx.openfood.utils.Utils.isBatteryLevelLow
import openfoodfacts.github.scrachx.openfood.utils.Utils.isDisableImageLoad
import openfoodfacts.github.scrachx.openfood.utils.Utils.isHardwareCameraInstalled
import openfoodfacts.github.scrachx.openfood.utils.getCsvFolderName
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ScanHistoryActivity : BaseActivity(), SwipeControllerActions {
    private var _binding: ActivityHistoryScanBinding? = null
    private val binding get() = _binding!!
    private lateinit var productItems: MutableList<HistoryItem>
    private var emptyHistory = false
    private lateinit var mHistoryProductDao: HistoryProductDao
    private var adapter: ScanHistoryAdapter? = null
    private var disposable: Disposable? = null
    private val listHistoryProducts: List<HistoryProduct>? = null

    //boolean to determine if image should be loaded or not
    private var isLowBatteryMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        _binding = ActivityHistoryScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.scan_history_drawer)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.scanFirst.setOnClickListener { onScanFirst() }

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        if (isDisableImageLoad(this) && isBatteryLevelLow(this)) {
            isLowBatteryMode = true
        }

        mHistoryProductDao = daoSession.historyProductDao
        productItems = mutableListOf()
        setInfo()
        binding.srRefreshHistoryScanList.setOnRefreshListener {
            mHistoryProductDao = daoSession.historyProductDao
            productItems = mutableListOf()
            setInfo()
            fillView()
            binding.srRefreshHistoryScanList.isRefreshing = false
        }
        install(this, binding.navigationBottom.bottomNavigation)
    }

    override fun onStart() {
        super.onStart()
        //to fill the view in any case even if the user scans products from History screen...
        fillView()
    }

    override fun onRightClicked(position: Int) {
        if (CollectionUtils.isNotEmpty(listHistoryProducts)) {
            mHistoryProductDao.delete(listHistoryProducts!![position])
        }
        adapter!!.remove(productItems[position])
        adapter!!.notifyItemRemoved(position)
        adapter!!.notifyItemRangeChanged(position, adapter!!.itemCount)
        if (adapter!!.itemCount == 0) {
            binding.emptyHistoryInfo.visibility = View.VISIBLE
            binding.scanFirst.visibility = View.VISIBLE
        }
    }

    private fun exportCSV() {
        val folderMain = getCsvFolderName()
        Toast.makeText(this, R.string.txt_exporting_history, Toast.LENGTH_LONG).show()
        val baseDir = File(Environment.getExternalStorageDirectory(), folderMain)
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        Log.d("dir", baseDir.toString())
        val fileName = "${BuildConfig.FLAVOR.toUpperCase(Locale.ROOT)}-${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.csv"
        val csvFile = File(baseDir, fileName)
        var isDownload = false
        try {
            CSVPrinter(FileWriter(csvFile), CSVFormat.DEFAULT.withHeader(*resources.getStringArray(R.array.headers))).use { writer ->
                for (hp in mHistoryProductDao.loadAll()) {
                    writer.printRecord(hp.barcode, hp.title, hp.brands)
                }
                Toast.makeText(this, R.string.txt_history_exported, Toast.LENGTH_LONG).show()
                isDownload = true
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Can't export to $csvFile.", e)
        }
        val downloadIntent = Intent(Intent.ACTION_VIEW)
        val notificationManager = ProductListActivity.createNotification(csvFile, downloadIntent, this)
        if (isDownload) {
            val builder = NotificationCompat.Builder(this, "export_channel")
                    .setContentTitle(getString(R.string.notify_title))
                    .setContentText(getString(R.string.notify_content))
                    .setContentIntent(PendingIntent.getActivity(this, 4, downloadIntent, 0))
                    .setSmallIcon(R.mipmap.ic_launcher)
            notificationManager.notify(7, builder.build())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)
        menu.findItem(R.id.action_export_all_history).isVisible = !emptyHistory
        menu.findItem(R.id.action_remove_all_history).isVisible = !emptyHistory
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        _binding = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                true
            }
            R.id.action_remove_all_history -> {
                MaterialDialog.Builder(this)
                        .title(R.string.title_clear_history_dialog)
                        .content(R.string.text_clear_history_dialog)
                        .onPositive { _, _ ->
                            mHistoryProductDao.deleteAll()
                            productItems.clear()
                            val adapter = binding.listHistoryScan.adapter
                            adapter?.notifyDataSetChanged()
                            binding.emptyHistoryInfo.visibility = View.VISIBLE
                            binding.scanFirst.visibility = View.VISIBLE
                        }
                        .positiveText(R.string.txtYes)
                        .negativeText(R.string.txtNo)
                        .show()
                true
            }
            R.id.action_export_all_history -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        MaterialDialog.Builder(this)
                                .title(R.string.action_about)
                                .content(R.string.permision_write_external_storage)
                                .neutralText(R.string.txtOk)
                                .show()
                    } else {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Utils.MY_PERMISSIONS_REQUEST_STORAGE)
                    }
                } else {
                    exportCSV()
                }
                true
            }
            R.id.sort_history -> {
                val builder = MaterialDialog.Builder(this)
                builder.title(R.string.sort_by)
                val sortTypes = if (BuildConfig.FLAVOR == "off") {
                    arrayOf(getString(R.string.by_title), getString(R.string.by_brand), getString(R.string.by_nutrition_grade), getString(
                            R.string.by_barcode), getString(R.string.by_time))
                } else {
                    arrayOf(getString(R.string.by_title), getString(R.string.by_brand), getString(R.string.by_time), getString(R.string.by_barcode))
                }
                builder.items(*sortTypes)
                builder.itemsCallback { _, _, position, _ ->
                    when (position) {
                        0 -> {
                            SORT_TYPE = "title"
                            fillView()
                        }
                        1 -> {
                            SORT_TYPE = "brand"
                            fillView()
                        }
                        2 -> {
                            SORT_TYPE = if (AppFlavors.isFlavors(OFF)) "grade" else "time"
                            fillView()
                        }
                        3 -> {
                            SORT_TYPE = "barcode"
                            fillView()
                        }
                        else -> {
                            SORT_TYPE = "time"
                            fillView()
                        }
                    }
                }
                builder.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Utils.MY_PERMISSIONS_REQUEST_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportCSV()
            } else {
                MaterialDialog.Builder(this)
                        .title(R.string.permission_title)
                        .content(R.string.permission_denied)
                        .negativeText(R.string.txtNo)
                        .positiveText(R.string.txtYes)
                        .onPositive { _, _ ->
                            startActivity(Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", this@ScanHistoryActivity.packageName, null)
                            })
                        }
                        .show()
            }
        } else if (requestCode == Utils.MY_PERMISSIONS_REQUEST_CAMERA
                && grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity(Intent(this@ScanHistoryActivity, ContinuousScanActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }
    }

    public override fun onResume() {
        super.onResume()
        selectNavigationItem(binding.navigationBottom.bottomNavigation, R.id.history_bottom_nav)
    }

    private fun onScanFirst() {
        if (isHardwareCameraInstalled(baseContext)) {
            if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@ScanHistoryActivity, Manifest.permission.CAMERA)) {
                    MaterialDialog.Builder(this)
                            .title(R.string.action_about)
                            .content(R.string.permission_camera)
                            .neutralText(R.string.txtOk)
                            .onNeutral { _: MaterialDialog?, _: DialogAction? -> ActivityCompat.requestPermissions(this@ScanHistoryActivity, arrayOf(Manifest.permission.CAMERA), Utils.MY_PERMISSIONS_REQUEST_CAMERA) }
                            .show()
                } else {
                    ActivityCompat.requestPermissions(this@ScanHistoryActivity, arrayOf(Manifest.permission.CAMERA), Utils.MY_PERMISSIONS_REQUEST_CAMERA)
                }
            } else {
                val intent = Intent(this@ScanHistoryActivity, ContinuousScanActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
    }

    private fun setInfo() {
        binding.emptyHistoryInfo.text = getString(R.string.scan_first_string)
    }

    /**
     * Function to compare history items based on title, brand, barcode, time and nutrition grade
     *
     * @param sortType String to determine type of sorting
     * @param productItems List of history items to be sorted
     */
    private fun sort(sortType: String, productItems: MutableList<HistoryItem>) {
        when (sortType) {
            "title" -> productItems.sortWith { item1: HistoryItem, item2: HistoryItem ->
                var title1 = item1.title
                if (title1.isNullOrEmpty())
                    title1 = resources.getString(R.string.no_title)

                var title2 = item2.title
                if (title2.isNullOrEmpty())
                    title2 = resources.getString(R.string.no_title)

                title1.compareTo(title2, ignoreCase = true)
            }
            "brand" -> productItems.sortWith { historyItem: HistoryItem, t1: HistoryItem ->
                if (TextUtils.isEmpty(historyItem.brands)) {
                    historyItem.brands = resources.getString(R.string.no_brand)
                }
                if (TextUtils.isEmpty(t1.brands)) {
                    t1.brands = resources.getString(R.string.no_brand)
                }
                historyItem.brands!!.compareTo(t1.brands!!, ignoreCase = true)
            }
            "barcode" -> productItems.sortWith { (_, _, _, barcode1), (_, _, _, barcode2) ->
                barcode1.compareTo(barcode2)
            }
            "grade" -> productItems.sortWith { (_, _, _, _, _, _, nutritionGrade1), (_, _, _, _, _, _, nutritionGrade2) ->
                val nGrade1 = nutritionGrade1 ?: "E"
                val nGrade2 = nutritionGrade2 ?: "E"
                nGrade1.compareTo(nGrade2, ignoreCase = true)
            }
            else -> productItems.sortWith { _: HistoryItem?, _: HistoryItem? -> 0 }
        }
    }

    private fun fillView() {
        disposable?.dispose()
        disposable = fillViewCompletable.doOnSubscribe { Log.i(LOG_TAG, "Task fillview started...") }
                .subscribe { Log.i(LOG_TAG, "Task fillview ended.") }

    }

    // Change ui on main thread
    // Switch for db operations
    // Change ui on main thread
    private val fillViewCompletable: Completable
        get() {
            val refreshAct = Completable.fromAction {
                if (binding.srRefreshHistoryScanList.isRefreshing) {
                    binding.historyProgressbar.visibility = View.GONE
                } else {
                    binding.historyProgressbar.visibility = View.VISIBLE
                }
            }
            val dbSingle = Single.fromCallable {
                productItems.clear()
                val historyProducts = mHistoryProductDao.queryBuilder().orderDesc(HistoryProductDao.Properties.LastSeen).list()
                historyProducts.forEach { historyProduct ->
                    productItems.add(HistoryItem(
                            historyProduct.title,
                            historyProduct.brands,
                            historyProduct.url,
                            historyProduct.barcode,
                            historyProduct.lastSeen,
                            historyProduct.quantity,
                            historyProduct.nutritionGrade
                    ))
                }
                historyProducts
            }
            val updateUiFunc = fun(historyProducts: List<HistoryProduct>): CompletableSource {
                if (historyProducts.isEmpty()) {
                    emptyHistory = true
                    binding.historyProgressbar.visibility = View.GONE
                    binding.emptyHistoryInfo.visibility = View.VISIBLE
                    binding.scanFirst.visibility = View.VISIBLE
                    invalidateOptionsMenu()
                    return Completable.complete()
                }
                sort(SORT_TYPE, productItems)
                adapter = ScanHistoryAdapter(this@ScanHistoryActivity, isLowBatteryMode, productItems)
                binding.listHistoryScan.adapter = adapter
                binding.listHistoryScan.layoutManager = LinearLayoutManager(this@ScanHistoryActivity)
                binding.historyProgressbar.visibility = View.GONE
                val swipeController = SwipeController(this@ScanHistoryActivity, this@ScanHistoryActivity)
                val itemTouchhelper = ItemTouchHelper(swipeController)
                itemTouchhelper.attachToRecyclerView(binding.listHistoryScan)
                return Completable.complete()
            }
            return refreshAct.subscribeOn(AndroidSchedulers.mainThread()) // Change ui on main thread
                    .observeOn(Schedulers.io()) // Switch for db operations
                    .andThen(dbSingle)
                    .observeOn(AndroidSchedulers.mainThread()) // Change ui on main thread
                    .flatMapCompletable(updateUiFunc)
        }

    companion object {
        private var SORT_TYPE = "none"
        fun start(context: Context) {
            val starter = Intent(context, ScanHistoryActivity::class.java)
            context.startActivity(starter)
        }

        private val LOG_TAG = ScanHistoryActivity::class.simpleName
    }
}