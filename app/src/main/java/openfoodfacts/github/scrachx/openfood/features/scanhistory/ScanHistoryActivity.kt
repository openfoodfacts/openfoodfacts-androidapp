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
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityHistoryScanBinding
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivityStarter
import openfoodfacts.github.scrachx.openfood.features.productlist.CreateCSVContract
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.SortType.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ScanHistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityHistoryScanBinding
    private val viewModel: ScanHistoryViewModel by viewModels()

    @Inject
    lateinit var productViewActivityStarter: ProductViewActivityStarter

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var localeManager: LocaleManager

    /**
     * boolean to determine if menu buttons should be visible or not
     */
    private var menuButtonsEnabled = false

    private val adapter by lazy {
        ScanHistoryAdapter(isLowBatteryMode = isDisableImageLoad() && isBatteryLevelLow(), picasso) {
            productViewActivityStarter.openProduct(it.barcode, this)
        }
    }

    private val storagePermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            exportAsCSV()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.permission_title)
                .setMessage(R.string.permission_denied)
                .setNegativeButton(R.string.txtNo) { dialog, _ -> dialog.dismiss() }
                .setPositiveButton(R.string.txtYes) { dialog, _ ->
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", this@ScanHistoryActivity.packageName, null)
                    })
                    dialog.dismiss()
                }
                .show()
        }
    }

    private val cameraPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startScanActivity()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    val fileWriterLauncher = registerForActivityResult(CreateCSVContract()) { uri ->
        uri?.let {
            writeHistoryToFile(this, adapter.products, it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        binding = ActivityHistoryScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.scan_history_drawer)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.listHistoryScan.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.listHistoryScan.adapter = adapter
        val swipeController = SwipeController(this) { position ->
            adapter.products.getOrNull(position)?.let {
                viewModel.removeProductFromHistory(it)
            }
        }
        ItemTouchHelper(swipeController).attachToRecyclerView(binding.listHistoryScan)

        binding.scanFirst.setOnClickListener { startScan() }
        binding.srRefreshHistoryScanList.setOnRefreshListener { refreshViewModel() }
        binding.navigationBottom.bottomNavigation.installBottomNavigation(this)

        viewModel.productsState.observe(this) { state ->
            when (state) {
                is ScanHistoryViewModel.FetchProductsState.Data -> {
                    binding.srRefreshHistoryScanList.isRefreshing = false
                    binding.historyProgressbar.isVisible = false

                    adapter.products = state.items

                    if (state.items.isEmpty()) {
                        setMenuEnabled(false)
                        binding.scanFirstProductContainer.isVisible = true
                    } else {
                        binding.scanFirstProductContainer.isVisible = false
                        setMenuEnabled(true)
                    }

                    adapter.notifyItemRangeChanged(0, state.items.count())
                }
                ScanHistoryViewModel.FetchProductsState.Error -> {
                    setMenuEnabled(false)
                    binding.srRefreshHistoryScanList.isRefreshing = false
                    binding.historyProgressbar.isVisible = false
                    binding.scanFirstProductContainer.isVisible = true
                }
                ScanHistoryViewModel.FetchProductsState.Loading -> {
                    setMenuEnabled(false)
                    if (binding.srRefreshHistoryScanList.isRefreshing.not()) {
                        binding.historyProgressbar.isVisible = true
                    }
                }
            }
        }

        refreshViewModel()
    }

    private fun refreshViewModel() = viewModel.refreshItems()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()

        if (menuButtonsEnabled) {
            menuInflater.inflate(R.menu.menu_history, menu)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            NavUtils.navigateUpFromSameTask(this)
            true
        }
        R.id.action_remove_all_history -> {
            showDeleteConfirmationDialog()
            true
        }
        R.id.action_export_all_history -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.action_about)
                        .setMessage(R.string.permision_write_external_storage)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            storagePermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                        .show()

                } else {
                    storagePermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            } else {
                exportAsCSV()
            }
            true
        }
        R.id.sort_history -> {
            showListSortingDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    override fun onResume() {
        super.onResume()
        binding.navigationBottom.bottomNavigation.selectNavigationItem(R.id.history_bottom_nav)
    }

    private fun setMenuEnabled(enabled: Boolean) {
        menuButtonsEnabled = enabled
        invalidateOptionsMenu()
    }


    private fun exportAsCSV() {
        Toast.makeText(this, R.string.txt_exporting_history, Toast.LENGTH_LONG).show()

        val flavor = BuildConfig.FLAVOR.uppercase(Locale.ROOT)
        val date = SimpleDateFormat("yyyy-MM-dd", localeManager.getLocale()).format(Date())
        val fileName = "$flavor-history_$date.csv"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            fileWriterLauncher.launch(fileName)
        } else {
            val baseDir = File(Environment.getExternalStorageDirectory(), getCsvFolderName())
            if (!baseDir.exists()) baseDir.mkdirs()
            val file = File(baseDir, fileName)
            writeHistoryToFile(this, adapter.products, file.toUri())
        }
    }

    private fun startScan() {
        if (!isHardwareCameraInstalled(this)) return
        // TODO: 21/06/2021 add dialog to explain why we can't
        val perm = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(baseContext, perm) == PackageManager.PERMISSION_GRANTED -> {
                startScanActivity()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, perm) -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.action_about)
                    .setMessage(R.string.permission_camera)
                    .setPositiveButton(android.R.string.ok) { d, _ ->
                        d.dismiss()
                        cameraPermLauncher.launch(perm)
                    }
                    .show()
            }
            else -> {
                cameraPermLauncher.launch(perm)
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.title_clear_history_dialog)
            .setMessage(R.string.text_clear_history_dialog)
            .setPositiveButton(android.R.string.ok) { d, _ ->
                viewModel.clearHistory()
                d.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
            .show()
    }

    private fun showListSortingDialog() {
        val sortTypes = if (isFlavors(OFF)) arrayOf(
            TITLE,
            BRAND,
            GRADE,
            BARCODE,
            TIME,
        ) else arrayOf(
            TITLE,
            BRAND,
            TIME,
            BARCODE,
        )

        val selectedItemPosition = sortTypes.indexOf(viewModel.sortType.value)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sort_by)
            .setSingleChoiceItems(
                sortTypes.map { getString(it.stringRes) }.toTypedArray(),
                if (selectedItemPosition < 0) 0 else selectedItemPosition
            ) { dialog, which ->
                val newType = when (which) {
                    0 -> TITLE
                    1 -> BRAND
                    2 -> if (isFlavors(OFF)) GRADE else TIME
                    3 -> BARCODE
                    else -> TIME
                }

                viewModel.updateSortType(newType)
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, ScanHistoryActivity::class.java))
        val LOG_TAG = ScanHistoryActivity::class.simpleName
    }

}
