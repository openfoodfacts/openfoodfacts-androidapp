package openfoodfacts.github.scrachx.openfood.features.scanhistory

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityHistoryScanBinding
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.productlist.CreateCSVContract
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.models.HistoryProductDao
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.SortType.*
import openfoodfacts.github.scrachx.openfood.utils.Utils.daoSession
import java.io.File
import java.time.LocalDate
import java.util.*

class ScanHistoryActivity : BaseActivity(), SwipeController.Actions {
    private var _binding: ActivityHistoryScanBinding? = null
    private val binding get() = _binding!!

    /**
     * boolean to determine if image should be loaded or not
     */
    private val isLowBatteryMode by lazy { this.isDisableImageLoad() && this.isBatteryLevelLow() }

    private lateinit var adapter: ScanHistoryAdapter

    private var sortType = NONE
    private var dbDisp: Disposable? = null


    private val storagePermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
    { isGranted ->
        if (isGranted) {
            exportAsCSV()
        } else {
            MaterialDialog.Builder(this).run {
                title(R.string.permission_title)
                content(R.string.permission_denied)
                negativeText(R.string.txtNo)
                positiveText(R.string.txtYes)
                onPositive { _, _ ->
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", this@ScanHistoryActivity.packageName, null)
                    })
                }
                onNegative { dialog, _ -> dialog.dismiss() }
                show()
            }
        }
    }

    private val cameraPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
    { isGranted ->
        if (isGranted) {
            startActivity(Intent(this@ScanHistoryActivity, ContinuousScanActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        _binding = ActivityHistoryScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.scan_history_drawer)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = ScanHistoryAdapter(this@ScanHistoryActivity, isLowBatteryMode, mutableListOf())
        binding.listHistoryScan.adapter = adapter
        binding.listHistoryScan.layoutManager = LinearLayoutManager(this@ScanHistoryActivity)
        val swipeController = SwipeController(this@ScanHistoryActivity, this@ScanHistoryActivity)
        ItemTouchHelper(swipeController).attachToRecyclerView(binding.listHistoryScan)

        setInfo()

        binding.scanFirst.setOnClickListener { startScan() }
        binding.srRefreshHistoryScanList.setOnRefreshListener {
            adapter.products.clear()
            setInfo()
            fillView()
            binding.srRefreshHistoryScanList.isRefreshing = false
        }
        binding.navigationBottom.bottomNavigation.installBottomNavigation(this)
    }

    override fun onStart() {
        super.onStart()
        //to fill the view in any case even if the user scans products from History screen...
        fillView()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbDisp?.dispose()
        _binding = null
    }

    override fun onRightClicked(position: Int) {
        if (adapter.products.isNotEmpty()) {
            daoSession.historyProductDao.delete(adapter.products[position])
        }
        adapter.removeAndNotify(adapter.products[position])
        if (adapter.itemCount == 0) {
            binding.emptyHistoryInfo.visibility = View.VISIBLE
            binding.scanFirst.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)
        menu.findItem(R.id.action_export_all_history).isVisible = adapter.itemCount != 0
        menu.findItem(R.id.action_remove_all_history).isVisible = adapter.itemCount != 0
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            NavUtils.navigateUpFromSameTask(this)
            true
        }
        R.id.action_remove_all_history -> {
            MaterialDialog.Builder(this).run {
                title(R.string.title_clear_history_dialog)
                content(R.string.text_clear_history_dialog)
                onPositive { _, _ ->
                    daoSession.historyProductDao.deleteAll()
                    adapter.products.clear()
                    adapter.notifyDataSetChanged()

                    binding.emptyHistoryInfo.visibility = View.VISIBLE
                    binding.scanFirst.visibility = View.VISIBLE
                }
                positiveText(R.string.txtYes)
                negativeText(R.string.txtNo)
                show()
            }
            true
        }
        R.id.action_export_all_history -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    MaterialDialog.Builder(this).run {
                        title(R.string.action_about)
                        content(R.string.permision_write_external_storage)
                        positiveText(R.string.txtOk)
                        onPositive { _, _ ->
                            storagePermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                        show()
                    }
                } else {
                    storagePermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            } else {
                exportAsCSV()
            }
            true
        }
        R.id.sort_history -> {
            MaterialDialog.Builder(this).run {
                title(R.string.sort_by)
                val sortTypes = if (BuildConfig.FLAVOR == "off") {
                    arrayOf(
                            getString(R.string.by_title),
                            getString(R.string.by_brand),
                            getString(R.string.by_nutrition_grade),
                            getString(R.string.by_barcode),
                            getString(R.string.by_time)
                    )
                } else {
                    arrayOf(
                            getString(R.string.by_title),
                            getString(R.string.by_brand),
                            getString(R.string.by_time),
                            getString(R.string.by_barcode)
                    )
                }
                items(*sortTypes)
                itemsCallback { _, _, position, _ ->
                    sortType = when (position) {
                        0 -> TITLE
                        1 -> BRAND
                        2 -> if (isFlavors(OFF)) GRADE else TIME
                        3 -> BARCODE
                        else -> TIME
                    }
                    fillView()
                }
                show()
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    public override fun onResume() {
        super.onResume()
        binding.navigationBottom.bottomNavigation.selectNavigationItem(R.id.history_bottom_nav)
    }

    private fun exportAsCSV() {
        Toast.makeText(this, R.string.txt_exporting_history, Toast.LENGTH_LONG).show()

        val flavor = BuildConfig.FLAVOR.toUpperCase(Locale.ROOT)
        val date = LocalDate.now()
        val fileName = "$flavor-history_$date.csv"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            fileWriterLauncher.launch(fileName)
        } else {
            val baseDir = File(Environment.getExternalStorageDirectory(), getCsvFolderName())
            if (!baseDir.exists()) baseDir.mkdirs()
            writeHistoryToFile(this, adapter.products, File(baseDir, fileName))
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    val fileWriterLauncher = registerForActivityResult(CreateCSVContract())
    { writeHistoryToFile(this, adapter.products, it?.toFile() ?: error("File path must not be null.")) }

    private fun startScan() {
        if (!isHardwareCameraInstalled(baseContext)) return
        val perm = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(baseContext, perm) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                MaterialDialog.Builder(this).run {
                    title(R.string.action_about)
                    content(R.string.permission_camera)
                    positiveText(R.string.txtOk)
                    onPositive { _, _ ->
                        cameraPermLauncher.launch(perm)
                    }
                    show()
                }
            } else {
                cameraPermLauncher.launch(perm)
            }
        } else {
            Intent(this, ContinuousScanActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(this)
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
     */
    private fun MutableList<HistoryProduct>.customSortBy(sortType: SortType) = when (sortType) {
        TITLE -> sortWith { item1, item2 ->
            if (item1.title.isNullOrEmpty()) item1.title = resources.getString(R.string.no_title)
            if (item2.title.isNullOrEmpty()) item2.title = resources.getString(R.string.no_title)
            item1.title.compareTo(item2.title, true)
        }

        BRAND -> sortWith { item1, item2 ->
            if (item1.brands.isNullOrEmpty()) item1.brands = resources.getString(R.string.no_brand)
            if (item2.brands.isNullOrEmpty()) item2.brands = resources.getString(R.string.no_brand)
            item1.brands!!.compareTo(item2.brands!!, true)
        }
        BARCODE -> sortBy { it.barcode }
        GRADE -> sortBy { it.nutritionGrade }
        TIME -> sortBy { it.lastSeen }
        NONE -> sortWith { _, _ -> 0 }
    }

    private fun fillView() {
        dbDisp?.dispose()
        dbDisp = getFillViewCompletable()
                .doOnSubscribe { Log.i(LOG_TAG, "Task fillview started...") }
                .subscribe { Log.i(LOG_TAG, "Task fillview ended.") }

    }

    private fun getFillViewCompletable(): Completable {
        val refreshAct = Completable.fromAction {
            binding.historyProgressbar.visibility =
                    if (binding.srRefreshHistoryScanList.isRefreshing) View.GONE else View.VISIBLE
        }
        val getProducts = Single.fromCallable {
            daoSession.historyProductDao.queryBuilder().orderDesc(HistoryProductDao.Properties.LastSeen).list()
        }
        return refreshAct.subscribeOn(AndroidSchedulers.mainThread()) // Change ui on main thread
                .observeOn(Schedulers.io()) // Switch for db operations
                .andThen(getProducts)
                .observeOn(AndroidSchedulers.mainThread()) // Change ui on main thread
                .flatMapCompletable { newProducts: List<HistoryProduct> ->
                    adapter.products.clear()
                    if (newProducts.isEmpty()) {
                        binding.historyProgressbar.visibility = View.GONE
                        binding.emptyHistoryInfo.visibility = View.VISIBLE
                        binding.scanFirst.visibility = View.VISIBLE
                        invalidateOptionsMenu()
                        return@flatMapCompletable Completable.complete()
                    }
                    adapter.products.addAll(newProducts)
                    adapter.products.customSortBy(sortType)
                    adapter.notifyDataSetChanged()
                    binding.historyProgressbar.visibility = View.GONE
                    return@flatMapCompletable Completable.complete()
                }
    }

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, ScanHistoryActivity::class.java))

        val LOG_TAG = ScanHistoryActivity::class.simpleName
    }

}