package openfoodfacts.github.scrachx.openfood.features.productlist

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityYourListedProductsBinding
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.models.HistoryProductDao
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists
import openfoodfacts.github.scrachx.openfood.models.entities.YourListedProduct
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.SortType.*
import openfoodfacts.github.scrachx.openfood.utils.Utils.daoSession
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class ProductListActivity : BaseActivity(), SwipeController.Actions {
    private var _binding: ActivityYourListedProductsBinding? = null
    private val binding get() = _binding!!

    private var listID by Delegates.notNull<Long>()
    private lateinit var productList: ProductLists
    private lateinit var adapter: ProductListAdapter

    private var isLowBatteryMode = false
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

        if (this.isDisableImageLoad() && this.isBatteryLevelLow()) isLowBatteryMode = true

        // OnClick
        binding.scanFirstYourListedProduct.setOnClickListener { onFirstScan() }


        // Get listid and add product to list if bundle is present
        val bundle = intent.extras
        var prodToAdd: Product? = null
        if (bundle != null) {
            listID = bundle.getLong(KEY_LIST_ID)
            listName = bundle.getString(KEY_LIST_NAME)
            title = listName
            prodToAdd = bundle[KEY_PRODUCT_TO_ADD] as Product?
        }

        val locale = getLanguage(this)
        if (prodToAdd?.code != null && prodToAdd.productName != null && prodToAdd.getImageSmallUrl(locale) != null) {
            val barcode = prodToAdd.code
            val productName = prodToAdd.productName
            val productDetails = prodToAdd.getProductBrandsQuantityDetails()
            val imageUrl = prodToAdd.getImageSmallUrl(locale)
            val product = YourListedProduct()
            product.barcode = barcode
            product.listId = listID
            product.listName = listName
            product.productName = productName
            product.productDetails = productDetails
            product.imageUrl = imageUrl
            daoSession.yourListedProductDao.insertOrReplace(product)
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
        adapter = ProductListAdapter(this, productList.products.toMutableList(), isLowBatteryMode)
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
        menu.findItem(R.id.action_export_all_listed_products).isVisible = adapter.products.isNotEmpty()
        menu.findItem(R.id.action_sort_listed_products).isVisible = adapter.products.isNotEmpty()
        return true
    }

    private fun MutableList<YourListedProduct>.customSortBy(sortType: SortType) = when (sortType) {
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
            sortWith { p1: YourListedProduct, p2: YourListedProduct ->
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


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            setResult(RESULT_OK, Intent().apply {
                putExtra("update", true)
            })
            finish()
            true
        }
        R.id.action_export_all_listed_products -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    MaterialDialog.Builder(this)
                            .title(R.string.action_about)
                            .content(R.string.permision_write_external_storage)
                            .neutralText(R.string.txtOk)
                            .show()
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_STORAGE)
                }
            } else {
                exportAsCSV()
            }
            true
        }
        R.id.action_sort_listed_products -> {
            MaterialDialog.Builder(this).run {
                title(R.string.sort_by)
                val sortTypes = if (isFlavors(AppFlavors.OFF)) {
                    listOf(
                            getString(R.string.by_title),
                            getString(R.string.by_brand),
                            getString(R.string.by_nutrition_grade),
                            getString(
                                    R.string.by_barcode),
                            getString(R.string.by_time)
                    )
                } else {
                    listOf(
                            getString(R.string.by_title),
                            getString(R.string.by_brand),
                            getString(R.string.by_time),
                            getString(R.string.by_barcode)
                    )
                }
                items(sortTypes)
                itemsCallback { _, _, position, _ ->
                    sortType = when (position) {
                        0 -> TITLE
                        1 -> BRAND
                        2 -> if (isFlavors(AppFlavors.OFF)) GRADE else TIME
                        3 -> BARCODE
                        else -> TIME
                    }
                    adapter.products.customSortBy(sortType)
                    adapter.notifyDataSetChanged()
                    binding.rvYourListedProducts.adapter = adapter
                }
                show()
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MY_PERMISSIONS_REQUEST_STORAGE && grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            exportAsCSV()
        }

    }


    private fun setInfo(view: TextView) = if (isEatenList) {
        view.setText(R.string.txt_info_eaten_products)
    } else {
        view.setText(R.string.txt_info_your_listed_products)
    }

    private fun onFirstScan() {
        if (!isHardwareCameraInstalled(this)) {
            Log.e(this::class.simpleName, "Device has no camera installed.")
            return
        }
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                MaterialDialog.Builder(this).run {
                    title(R.string.action_about)
                    content(R.string.permission_camera)
                    neutralText(R.string.txtOk)
                    onNeutral { _, _ -> ActivityCompat.requestPermissions(this@ProductListActivity, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA) }
                    show()
                }
            } else {
                ActivityCompat.requestPermissions(this@ProductListActivity, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
            }
        } else {
            val intent = Intent(this, ContinuousScanActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    val fileWriterLauncher = registerForActivityResult(CreateCSVContract()) {
        writeListToFile(this, productList, it,contentResolver.openOutputStream(it) ?: error("File path must not be null."))
    }

    private fun exportAsCSV() {
        Toast.makeText(this, R.string.txt_exporting_your_listed_products, Toast.LENGTH_LONG).show()

        val listName = productList.listName
        val flavor = BuildConfig.FLAVOR.toUpperCase(Locale.ROOT)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "$flavor-${listName}_$date.csv"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            fileWriterLauncher.launch(fileName)
        } else {
            @Suppress("DEPRECATION")
            val baseDir = File(Environment.getExternalStorageDirectory(), getCsvFolderName())
            if (!baseDir.exists()) baseDir.mkdirs()
            val file = File(baseDir, fileName)
            writeListToFile(this, productList,Uri.fromFile(file), file.outputStream())
        }
    }

    override fun onRightClicked(position: Int) {
        if (adapter.products.isEmpty()) return
        val productToRemove = adapter.products[position]

        daoSession.yourListedProductDao!!.delete(productToRemove)
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

        fun createNotification(csvUri: Uri, downloadIntent: Intent, context: Context): NotificationManager {
            downloadIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            downloadIntent.setDataAndType(csvUri, "text/csv")
            downloadIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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

        fun getProductBrandsQuantityDetails(brands: String?, quantity: String?): String {
            val builder = StringBuilder()
            if (!brands.isNullOrEmpty()) {
                builder.append(brands.split(",")[0].trim { it <= ' ' }.capitalize(Locale.ROOT))
            }
            if (!quantity.isNullOrEmpty()) {
                builder.append(" - ").append(quantity)
            }
            return builder.toString()
        }

        const val KEY_LIST_ID = "listId"
        const val KEY_LIST_NAME = "listName"
        const val KEY_PRODUCT_TO_ADD = "product"
    }
}