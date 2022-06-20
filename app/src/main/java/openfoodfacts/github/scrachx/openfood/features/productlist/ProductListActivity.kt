package openfoodfacts.github.scrachx.openfood.features.productlist

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.net.toUri
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import logcat.LogPriority
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.databinding.ActivityYourListedProductsBinding
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivityStarter
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists
import openfoodfacts.github.scrachx.openfood.utils.SortType.*
import openfoodfacts.github.scrachx.openfood.utils.SwipeController
import openfoodfacts.github.scrachx.openfood.utils.getCsvFolderName
import openfoodfacts.github.scrachx.openfood.utils.isHardwareCameraInstalled
import openfoodfacts.github.scrachx.openfood.utils.writeListToFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ProductListActivity : BaseActivity() {
    private var _binding: ActivityYourListedProductsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductListViewModel by viewModels()


    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var productViewActivityStarter: ProductViewActivityStarter

    private lateinit var adapter: ProductListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityYourListedProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        title = viewModel.listName

        // OnClick
        binding.scanFirstYourListedProduct.setOnClickListener { checkPermsStartScan() }

        adapter = ProductListAdapter(this, picasso).apply {
            onItemClickListener = {
                // TODO: Find a better way to do this
                lifecycleScope.launch { productViewActivityStarter.openProduct(it.barcode, this@ProductListActivity) }
            }
        }
        binding.rvYourListedProducts.adapter = adapter
        binding.rvYourListedProducts.layoutManager = LinearLayoutManager(this)
        binding.rvYourListedProducts.setHasFixedSize(false)

        lifecycleScope.launch {
            viewModel.productList.flowWithLifecycle(lifecycle).collect {
                if (it.products.isEmpty()) {
                    binding.tvInfoYourListedProducts.visibility = View.VISIBLE
                    binding.scanFirstYourListedProduct.visibility = View.VISIBLE
                    setInfo(it, binding.tvInfoYourListedProducts)
                }

                adapter.products = it.products
                adapter.notifyDataSetChanged()
            }
        }

        ItemTouchHelper(SwipeController(this) { onRightSwiped(it) })
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
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu == null) return false

        listOf(
            R.id.action_export_all_listed_products,
            R.id.action_sort_listed_products,
            R.id.action_share_list
        ).forEach {
            menu.findItem(it).isVisible = adapter.products.isNotEmpty()
        }
        return true
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
                checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED -> {
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
                    val sortType = when (position) {
                        0 -> TITLE
                        1 -> BRAND
                        2 -> if (isFlavors(OFF)) GRADE else TIME
                        3 -> BARCODE
                        else -> TIME
                    }
                    viewModel.sortBy(sortType)
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


    private fun setInfo(productList: ProductLists, view: TextView) = if (productList.id == 1L) {
        view.setText(R.string.txt_info_eaten_products)
    } else {
        view.setText(R.string.txt_info_your_listed_products)
    }

    private fun checkPermsStartScan() {
        if (!isHardwareCameraInstalled(this)) {
            logcat(LogPriority.WARN) { "Device has no camera installed." }
            return
        }
        when {
            checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
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
    { uri -> uri?.let { writeListToFile(this, viewModel.productList.value, it) } }

    private fun exportAsCSV() {
        Toast.makeText(this, R.string.txt_exporting_your_listed_products, Toast.LENGTH_LONG).show()

        val productList = viewModel.productList.value

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
        val productList = viewModel.productList.value
        val shareUrl = "${BuildConfig.OFWEBSITE}search?code=${productList.products.joinToString(",") { it.barcode }}"

        startActivity(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareUrl)
        }, null))

        matomoAnalytics.trackEvent(AnalyticsEvent.ShoppingListShared)
    }


    private fun onRightSwiped(position: Int) {
        val productToRemove = adapter.products.getOrNull(position) ?: return
        adapter.remove(productToRemove)
        viewModel.removeProduct(productToRemove)

        matomoAnalytics.trackEvent(AnalyticsEvent.ShoppingListProductRemoved(productToRemove.barcode))
    }


    override fun onBackPressed() {
        setResult(RESULT_OK, Intent().apply { putExtra("update", true) })
        super.onBackPressed()
    }

    companion object {

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

