package openfoodfacts.github.scrachx.openfood.features.compare

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductComparisonBinding
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler
import openfoodfacts.github.scrachx.openfood.utils.isHardwareCameraInstalled
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ProductCompareActivity : BaseActivity() {
    private var _binding: ActivityProductComparisonBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var api: OpenFoodAPIClient

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var localeManager: LocaleManager

    private lateinit var productComparisonAdapter: ProductCompareAdapter
    private lateinit var photoReceiverHandler: PhotoReceiverHandler

    private val productsToCompare = MutableLiveData<ArrayList<Product>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductComparisonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.compare_products)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var productsToCompare = ArrayList<Product>()
        if (intent.extras != null && intent.getBooleanExtra(KEY_PRODUCT_FOUND, false)) {
            productsToCompare = intent.extras?.getSerializable(KEY_PRODUCTS_TO_COMPARE) as ArrayList<Product>
            if (intent.getBooleanExtra(KEY_PRODUCT_ALREADY_EXISTS, false)) {
                Toast.makeText(this, getString(R.string.product_already_exists_in_comparison), Toast.LENGTH_SHORT).show()
            }
        }

        if (productsToCompare.size > 1) {
            matomoAnalytics.trackEvent(AnalyticsEvent.CompareProducts(productsToCompare.size.toFloat()))
        }

        this.productsToCompare.value = productsToCompare

        productComparisonAdapter = ProductCompareAdapter(
            productsToCompare,
            this,
            api,
            productRepository,
            picasso,
            localeManager.getLanguage()
        )
        binding.productComparisonRv.layoutManager = LinearLayoutManager(this, HORIZONTAL, false)
        binding.productComparisonRv.adapter = productComparisonAdapter

        photoReceiverHandler = PhotoReceiverHandler(sharedPreferences) {
            productComparisonAdapter.setImageOnPhotoReturn(it)
        }

        val finalProductsToCompare = productsToCompare

        binding.productComparisonButton.setOnClickListener {
            if (!isHardwareCameraInstalled(this@ProductCompareActivity)) {
                return@setOnClickListener
            }
            when {
                ContextCompat.checkSelfPermission(this@ProductCompareActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                    startActivity(Intent(this@ProductCompareActivity, ContinuousScanActivity::class.java).apply {
                        putExtra(KEY_COMPARE_PRODUCT, true)
                        putExtra(KEY_PRODUCTS_TO_COMPARE, finalProductsToCompare)
                    })
                }
                ActivityCompat.shouldShowRequestPermissionRationale(this@ProductCompareActivity, Manifest.permission.CAMERA) -> {
                    MaterialAlertDialogBuilder(this@ProductCompareActivity)
                        .setTitle(R.string.action_about)
                        .setMessage(R.string.permission_camera)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            requestCameraThenOpenScan.launch(Manifest.permission.CAMERA)
                        }
                        .show()
                }
                else -> {
                    requestCameraThenOpenScan.launch(Manifest.permission.CAMERA)
                }
            }

        }
        binding.navigationBottomInclude.bottomNavigation.installBottomNavigation(this)
    }

    override fun startScanActivity() {
        startActivity(Intent(this@ProductCompareActivity, ContinuousScanActivity::class.java).apply {
            putExtra(KEY_COMPARE_PRODUCT, true)
            putExtra(KEY_PRODUCTS_TO_COMPARE, productsToCompare.value)
        })
    }


    public override fun onResume() {
        super.onResume()
        binding.navigationBottomInclude.bottomNavigation.selectNavigationItem(R.id.compare_products)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data)
    }

    companion object {
        const val KEY_PRODUCTS_TO_COMPARE = "products_to_compare"
        const val KEY_PRODUCT_FOUND = "product_found"
        const val KEY_COMPARE_PRODUCT = "compare_product"
        const val KEY_PRODUCT_ALREADY_EXISTS = "product_already_exists"

        @JvmStatic
        fun start(context: Context, product: Product) {
            context.startActivity(Intent(context, ProductCompareActivity::class.java).apply {
                putExtra(KEY_PRODUCT_FOUND, true)
                putExtra(KEY_PRODUCTS_TO_COMPARE, arrayListOf(product))
            })
        }

        @JvmStatic
        fun start(context: Context) = context.startActivity(Intent(context, ProductCompareActivity::class.java))
    }
}
