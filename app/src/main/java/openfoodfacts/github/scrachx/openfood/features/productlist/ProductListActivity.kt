package openfoodfacts.github.scrachx.openfood.features.productlist

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityYourListedProductsBinding
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.install
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.HistoryItem
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.models.HistoryProductDao
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists
import openfoodfacts.github.scrachx.openfood.models.entities.YourListedProduct
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.Utils.daoSession
import openfoodfacts.github.scrachx.openfood.utils.Utils.isBatteryLevelLow
import openfoodfacts.github.scrachx.openfood.utils.Utils.isDisableImageLoad
import openfoodfacts.github.scrachx.openfood.utils.Utils.isHardwareCameraInstalled
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class ProductListActivity : BaseActivity(), SwipeControllerActions {
    private var _binding: ActivityYourListedProductsBinding? = null
    private val binding get() = _binding!!
    private var productList: ProductLists? = null
    private var products: MutableList<YourListedProduct>? = null
    private val yourListedProductDao = daoSession.yourListedProductDao
    private val historyProductDao = daoSession.historyProductDao
    private var listID by Delegates.notNull<Long>()
    private var adapter: ProductListAdapter? = null
    private var isLowBatteryMode = false
    private var listName: String? = null
    private var emptyList = false
    private var isEatenList = false
    private var sortType = "none"

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_your_listed_products, menu)
        menu.findItem(R.id.action_export_all_listed_products).isVisible = !emptyList
        menu.findItem(R.id.action_sort_listed_products).isVisible = !emptyList
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityYourListedProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        if (isDisableImageLoad(this) && isBatteryLevelLow(this)) isLowBatteryMode = true

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
            val productDetails = getProductBrandsQuantityDetails(prodToAdd)
            val imageUrl = prodToAdd.getImageSmallUrl(locale)
            val product = YourListedProduct()
            product.barcode = barcode
            product.listId = listID
            product.listName = listName
            product.productName = productName
            product.productDetails = productDetails
            product.imageUrl = imageUrl
            yourListedProductDao.insertOrReplace(product)
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
        products = productList.products.toMutableList()


        products?.let {
            if (it.isEmpty()) {
                emptyList = true
                binding.tvInfoYourListedProducts.visibility = View.VISIBLE
                binding.scanFirstYourListedProduct.visibility = View.VISIBLE
                setInfo(binding.tvInfoYourListedProducts)
            }
            adapter = ProductListAdapter(this, it, isLowBatteryMode)
            binding.rvYourListedProducts.adapter = adapter
            val swipeController = SwipeController(this, this@ProductListActivity)
            val itemTouchHelper = ItemTouchHelper(swipeController)
            itemTouchHelper.attachToRecyclerView(binding.rvYourListedProducts)
        }

        this.productList = productList
        selectNavigationItem(binding.bottomNavigation.bottomNavigation, 0)
        install(this, binding.bottomNavigation.bottomNavigation)

    }

    private fun sortProducts() {
        when (sortType) {
            "title" -> products!!.sortWith { p1, p2 -> p1!!.productName.compareTo(p2!!.productName, ignoreCase = true) }
            "brand" -> products!!.sortWith { p1, p2 -> p1!!.productDetails.compareTo(p2!!.productDetails, ignoreCase = true) }
            "barcode" -> products!!.sortWith { p1, p2 -> p1!!.barcode.compareTo(p2!!.barcode, ignoreCase = true) }
            "grade" -> {

                //get list of HistoryProduct items for the YourListProduct items
                val gradesConditions = products!!.map { HistoryProductDao.Properties.Barcode.eq(it.barcode) }.toTypedArray()
                val historyProductsGrade: List<HistoryProduct>
                val qbGrade = historyProductDao!!.queryBuilder()
                if (gradesConditions.size > 1) {
                    qbGrade.whereOr(gradesConditions[0], gradesConditions[1], *gradesConditions.copyOfRange(2, gradesConditions.size))
                } else {
                    qbGrade.where(gradesConditions[0])
                }
                historyProductsGrade = qbGrade.list()
                products!!.sortWith { p1: YourListedProduct, p2: YourListedProduct ->
                    var g1 = "E"
                    var g2 = "E"
                    for (h in historyProductsGrade) {
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
            "time" -> {
                //get list of HistoryProduct items for the YourListProduct items
                val times = products!!.map { HistoryProductDao.Properties.Barcode.eq(it.barcode) }.toTypedArray()

                val historyProductsTime: List<HistoryProduct>
                val qbTime = historyProductDao!!.queryBuilder()
                qbTime.whereOr(times[0], times[1], *times.copyOfRange(2, times.size))
                historyProductsTime = qbTime.list()
                products!!.sortWith { p1: YourListedProduct, p2: YourListedProduct ->
                    var d1 = Date(0)
                    var d2 = Date(0)
                    for (h in historyProductsTime) {
                        if (h.barcode == p1.barcode && h.lastSeen != null) {
                            d1 = h.lastSeen
                        }
                        if (h.barcode == p2.barcode && h.lastSeen != null) {
                            d2 = h.lastSeen
                        }
                    }
                    d2.compareTo(d1)
                }
            }
            else -> products!!.sortWith { _, _ -> 0 }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val data = Intent()
                data.putExtra("update", true)
                setResult(RESULT_OK, data)
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
                    exportCSV()
                }
                true
            }
            R.id.action_sort_listed_products -> {
                val builder = MaterialDialog.Builder(this)
                builder.title(R.string.sort_by)
                val sortTypes = if (isFlavors(AppFlavors.OFF)) {
                    arrayOf(getString(R.string.by_title), getString(R.string.by_brand), getString(R.string.by_nutrition_grade), getString(
                            R.string.by_barcode), getString(R.string.by_time))
                } else {
                    arrayOf(getString(R.string.by_title), getString(R.string.by_brand), getString(R.string.by_time), getString(R.string.by_barcode))
                }
                builder.items(*sortTypes)
                builder.itemsCallback { _: MaterialDialog?, _: View?, position: Int, _: CharSequence? ->
                    sortType = when (position) {
                        0 -> "title"
                        1 -> "brand"
                        2 -> if (isFlavors(AppFlavors.OFF)) {
                            "grade"
                        } else {
                            "time"
                        }
                        3 -> "barcode"
                        else -> "time"
                    }
                    sortProducts()
                    adapter = ProductListAdapter(this, products ?: emptyList(), isLowBatteryMode)
                    binding.rvYourListedProducts.adapter = adapter
                }
                builder.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setInfo(view: TextView) {
        if (isEatenList) {
            view.setText(R.string.txt_info_eaten_products)
        } else {
            view.setText(R.string.txt_info_your_listed_products)
        }
    }

    private fun onFirstScan() {
        if (!isHardwareCameraInstalled(this)) {
            Log.e(this::class.simpleName, "Device has no camera installed.")
            return
        }
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                MaterialDialog.Builder(this)
                        .title(R.string.action_about)
                        .content(R.string.permission_camera)
                        .neutralText(R.string.txtOk)
                        .onNeutral { _: MaterialDialog?, _: DialogAction? -> ActivityCompat.requestPermissions(this@ProductListActivity, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA) }
                        .show()
            } else {
                ActivityCompat.requestPermissions(this@ProductListActivity, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
            }
        } else {
            val intent = Intent(this, ContinuousScanActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    fun exportCSV() {
        val folderMain = getCsvFolderName()
        Toast.makeText(this, R.string.txt_exporting_your_listed_products, Toast.LENGTH_LONG).show()
        val baseDir = File(Environment.getExternalStorageDirectory(), folderMain)
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        val productListName = productList!!.listName
        val fileName = BuildConfig.FLAVOR.toUpperCase(Locale.ROOT) + "-" + productListName + "-" + SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) + ".csv"
        val f = File(baseDir, fileName)
        var isDownload: Boolean
        try {
            CSVPrinter(FileWriter(f), CSVFormat.DEFAULT.withHeader(*resources.getStringArray(R.array.your_products_headers))).use { writer ->
                val listProducts = productList!!.products
                for (product in listProducts) {
                    writer.printRecord(product.barcode, product.productName, product.listName, product.productDetails)
                }
                Toast.makeText(this, R.string.txt_your_listed_products_exported, Toast.LENGTH_LONG).show()
                isDownload = true
            }
        } catch (e: IOException) {
            isDownload = false
            Log.e(ProductListActivity::class.java.simpleName, "exportCSV", e)
        }
        val downloadIntent = Intent(Intent.ACTION_VIEW)
        val notificationManager = createNotification(f, downloadIntent, this)
        if (isDownload) {
            val builder = NotificationCompat.Builder(this, "export_channel")
                    .setContentTitle(getString(R.string.notify_title))
                    .setContentText(getString(R.string.notify_content))
                    .setContentIntent(PendingIntent.getActivity(this, 4, downloadIntent, 0))
                    .setSmallIcon(R.mipmap.ic_launcher)
            notificationManager.notify(8, builder.build())
        }
    }

    override fun onRightClicked(position: Int) {
        if (CollectionUtils.isNotEmpty(products)) {
            val productToRemove = products!![position]
            yourListedProductDao!!.delete(productToRemove)
            adapter!!.remove(productToRemove)
            adapter!!.notifyItemRemoved(position)
            adapter!!.notifyItemRangeChanged(position, adapter!!.itemCount)
        }
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

        fun createNotification(f: File?, downloadIntent: Intent, context: Context): NotificationManager {
            val csvUri = FileProvider.getUriForFile(context, context.packageName + ".provider", f!!)
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
                val channelName: CharSequence = context.getString(R.string.notification_channel_name)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val notificationChannel = NotificationChannel(channelId, channelName, importance)
                notificationChannel.description = context.getString(R.string.notify_channel_description)
                notificationManager.createNotificationChannel(notificationChannel)
            }
            return notificationManager
        }


        @JvmStatic
        fun getProductBrandsQuantityDetails(p: Product) = getProductBrandsQuantityDetails(p.brands, p.quantity)

        fun getProductBrandsQuantityDetails(p: HistoryItem) = getProductBrandsQuantityDetails(p.brands, p.quantity)

        private fun getProductBrandsQuantityDetails(brands: String?, quantity: String?): String {
            val stringBuilder = StringBuilder()
            if (!brands.isNullOrEmpty()) {
                stringBuilder.append(brands.split(",").toTypedArray()[0].trim { it <= ' ' }.capitalize(Locale.ROOT))
            }
            if (!quantity.isNullOrEmpty()) {
                stringBuilder.append(" - ").append(quantity)
            }
            return stringBuilder.toString()
        }

        private const val KEY_LIST_ID = "listId"
        private const val KEY_LIST_NAME = "listName"
        private const val KEY_PRODUCT_TO_ADD = "product"
    }
}