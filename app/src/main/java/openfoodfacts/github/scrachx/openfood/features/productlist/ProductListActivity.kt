package openfoodfacts.github.scrachx.openfood.features.productlist

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.databinding.ActivityYourListedProductsBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.models.HistoryProductDao
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.ListedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.SortType.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class ProductListActivity : BaseActivity(), SwipeController.Actions {
    private var _binding: ActivityYourListedProductsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var daoSession: DaoSession

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var localeManager: LocaleManager

    @Inject
    lateinit var picasso: Picasso

    private var listID by Delegates.notNull<Long>()
    private lateinit var productList: ProductLists
    private lateinit var adapter: ProductListAdapter

    private val isLowBatteryMode by lazy { this.isDisableImageLoad() && this.isBatteryLevelLow() }
    private var listName: String? = null
    private var isEatenList = false

    private var sortType = NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityYourListedProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        // OnClick
        binding.scanFirstYourListedProduct.setOnClickListener { checkPermsStartScan() }


        // Get listid and add product to list if bundle is present
        val bundle = intent.extras
        if (bundle != null) {
            listID = bundle.getLong(KEY_LIST_ID)
            listName = bundle.getString(KEY_LIST_NAME)
            title = listName

            (bundle[KEY_PRODUCT_TO_ADD] as? Product)?.let { prodToAdd ->
                val locale = localeManager.getLanguage()
                if (prodToAdd.productName != null && prodToAdd.getImageSmallUrl(locale) != null) {
                    val barcode = prodToAdd.code
                    val productName = prodToAdd.productName
                    val productDetails = prodToAdd.getProductBrandsQuantityDetails()
                    val imageUrl = prodToAdd.getImageSmallUrl(locale)

                    val product = ListedProduct().apply {
                        this.barcode = barcode
                        this.listId = this@ProductListActivity.listID
                        this.listName = this@ProductListActivity.listName
                        this.productName = productName
                        this.productDetails = productDetails
                        this.imageUrl = imageUrl
                    }

                    daoSession.listedProductDao.insertOrReplace(product)
                }
            }
        }

        val productList = daoSession.productListsDao.load(listID)
        if (productList == null) {
            finish()
            return
        }

        productList.resetProducts()
        if (productList.id == 1L) {
            isEatenList = true
        }
        binding.rvYourListedProducts.layoutManager = LinearLayoutManager(this)
        binding.rvYourListedProducts.setHasFixedSize(false)

        this.productList = productList

        if (productList.products.isEmpty()) {
            binding.tvInfoYourListedProducts.visibility = View.VISIBLE
            binding.scanFirstYourListedProduct.visibility = View.VISIBLE
            setInfo(binding.tvInfoYourListedProducts)
        }
        adapter = ProductListAdapter(
            this,
            productList.products.toMutableList(),
            isLowBatteryMode,
            picasso,
            onItemClickListener = {
                lifecycleScope.launch { client.openProduct(it.barcode, this@ProductListActivity) }
            }
        )
        binding.rvYourListedProducts.adapter = adapter

        ItemTouchHelper(SwipeController(this, this@ProductListActivity))
            .attachToRecyclerView(binding.rvYourListedProducts)

        binding.bottomNavigation.bottomNavigation.selectNavigationItem(0)
        binding.bottomNavigation.bottomNavigation.installBottomNavigation(this)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_your_listed_products, menu)
        listOf(
            R.id.action_export_all_listed_products,
            R.id.action_sort_listed_products,
            R.id.action_share_list
        ).forEach {
            menu.findItem(it).isVisible = adapter.products.isNotEmpty()
        }
        return true
    }

    private fun MutableList<ListedProduct>.customSortBy(sortType: SortType) = when (sortType) {
        TITLE -> sortBy { it.productName }
        BRAND -> sortBy { it.productDetails }
        BARCODE -> sortBy { it.barcode }
        GRADE -> {

            //get list of HistoryProduct items for the YourListProduct items
            val gradesConditions = map { HistoryProductDao.Properties.Barcode.eq(it.barcode) }.toTypedArray()
            val qbGrade = daoSession.historyProductDao!!.queryBuilder()
            if (gradesConditions.size > 1) {
                qbGrade.whereOr(gradesConditions[0], gradesConditions[1], *gradesConditions.copyOfRange(2, gradesConditions.size))
            } else {
                qbGrade.where(gradesConditions[0])
            }
            val historyProductsGrade = qbGrade.list()
            sortWith { p1, p2 ->
                var g1 = "E"
                var g2 = "E"
                historyProductsGrade.forEach { h ->
                    if (h.barcode == p1.barcode && h.nutritionGrade != null) {
                        g1 = h.nutritionGrade
                    }
                    if (h.barcode == p2.barcode && h.nutritionGrade != null) {
                        g2 = h.nutritionGrade
                    }
                }
                g1.compareTo(g2, ignoreCase = true)
            }
        }
        TIME -> {
            //get list of HistoryProduct items for the YourListProduct items
            val times = map { HistoryProductDao.Properties.Barcode.eq(it.barcode) }.toTypedArray()

            val historyProductsTime: List<HistoryProduct>
            val qbTime = daoSession.historyProductDao!!.queryBuilder()
            qbTime.whereOr(times[0], times[1], *times.copyOfRange(2, times.size))
            historyProductsTime = qbTime.list()
            sortWith { p1: ListedProduct, p2: ListedProduct ->
                var d1 = Date(0)
                var d2 = Date(0)
                historyProductsTime.forEach {
                    if (it.barcode == p1.barcode && it.lastSeen != null) {
                        d1 = it.lastSeen
                    }
                    if (it.barcode == p2.barcode && it.lastSeen != null) {
                        d2 = it.lastSeen
                    }
                }
                d2.compareTo(d1)
            }
        }
        else -> sortWith { _, _ -> 0 }
    }

    private val requestWriteLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
    { if (it) exportAsCSV() }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            setResult(RESULT_OK, Intent().apply {
                putExtra("update", true)
            })
            finish()
            true
        }
        R.id.action_export_all_listed_products -> {
            val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
            when {
                checkSelfPermission(
                    this, perm
                ) == PackageManager.PERMISSION_GRANTED -> {
                    exportAsCSV()
                    matomoAnalytics.trackEvent(AnalyticsEvent.ShoppingListExported)
                }
                shouldShowRequestPermissionRationale(this, perm) -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.action_about)
                        .setMessage(R.string.permision_write_external_storage)
                        .setNeutralButton(android.R.string.ok) { _, _ ->
                            requestWriteLauncher.launch(perm)
                        }
                        .show()
                }
                else -> {
                    requestWriteLauncher.launch(perm)
                }
            }

            true
        }
        R.id.action_sort_listed_products -> {
            val sortTypes = if (isFlavors(OFF)) {
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
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.sort_by)
                .setItems(sortTypes) { _, position ->
                    sortType = when (position) {
                        0 -> TITLE
                        1 -> BRAND
                        2 -> if (isFlavors(OFF)) GRADE else TIME
                        3 -> BARCODE
                        else -> TIME
                    }
                    adapter.products.customSortBy(sortType)
                    adapter.notifyDataSetChanged()
                    binding.rvYourListedProducts.adapter = adapter
                }
                .show()
            true
        }
        R.id.action_share_list -> {
            shareList()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    private fun setInfo(view: TextView) = if (isEatenList) {
        view.setText(R.string.txt_info_eaten_products)
    } else {
        view.setText(R.string.txt_info_your_listed_products)
    }

    private fun checkPermsStartScan() {
        if (!isHardwareCameraInstalled(this)) {
            Log.e(this::class.simpleName, "Device has no camera installed.")
            return
        }
        when {
            checkSelfPermission(
                baseContext, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startScanActivity()
            }
            shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.action_about)
                    .setMessage(R.string.permission_camera)
                    .setNeutralButton(android.R.string.ok) { _, _ ->
                        requestCameraThenOpenScan.launch(Manifest.permission.CAMERA)
                    }
                    .show()
            }
            else -> {
                requestCameraThenOpenScan.launch(Manifest.permission.CAMERA)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    val fileWriterLauncher = registerForActivityResult(CreateCSVContract())
    { writeListToFile(this, productList, it) }

    private fun exportAsCSV() {
        Toast.makeText(this, R.string.txt_exporting_your_listed_products, Toast.LENGTH_LONG).show()

        val listName = productList.listName
        val flavor = BuildConfig.FLAVOR.uppercase(Locale.ROOT)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "$flavor-${listName}_$date.csv"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            fileWriterLauncher.launch(fileName)
        } else {
            @Suppress("DEPRECATION")
            val baseDir = File(Environment.getExternalStorageDirectory(), getCsvFolderName())
            if (!baseDir.exists()) baseDir.mkdirs()
            val file = File(baseDir, fileName)
            writeListToFile(this, productList, file.toUri())
        }
    }

    private fun shareList() {
        val shareUrl = "${BuildConfig.OFWEBSITE}search?code=${productList.products.joinToString(",") { it.barcode }}"

        startActivity(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareUrl)
            type = "text/plain"
        }, null))
    }

    override fun onRightClicked(position: Int) {
        if (adapter.products.isEmpty()) return

        val productToRemove = adapter.products[position]
        daoSession.listedProductDao.delete(productToRemove)
        adapter.remove(productToRemove)
    }

    override fun onBackPressed() {
        setResult(RESULT_OK, Intent().apply { putExtra("update", true) })
        super.onBackPressed()
    }

    companion object {

        @JvmStatic
        fun start(context: Context, listID: Long, listName: String) {
            context.startActivity(Intent(context, ProductListActivity::class.java).apply {
                putExtra(KEY_LIST_ID, listID)
                putExtra(KEY_LIST_NAME, listName)
            })
        }

        const val KEY_LIST_ID = "listId"
        const val KEY_LIST_NAME = "listName"
        const val KEY_PRODUCT_TO_ADD = "product"
    }
}

