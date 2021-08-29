package openfoodfacts.github.scrachx.openfood.features.compare

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductComparisonBinding
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ProductCompareActivity : BaseActivity() {
    private var _binding: ActivityProductComparisonBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductCompareViewModel by viewModels()

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

    private lateinit var photoReceiverHandler: PhotoReceiverHandler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityProductComparisonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.compare_products)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Use activity results if present
        var returnedProducts = emptyList<Product>()
        if (intent.extras != null && intent.getBooleanExtra(KEY_PRODUCT_FOUND, false)) {
            returnedProducts = intent.extras?.getSerializable(KEY_PRODUCTS_TO_COMPARE) as List<Product>
            if (intent.getBooleanExtra(KEY_PRODUCT_ALREADY_EXISTS, false)) {
                Toast.makeText(this, getString(R.string.product_already_exists_in_comparison), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.addProductsToCompare(returnedProducts)

        binding.navigationBottomInclude.bottomNavigation.installBottomNavigation(this)

        viewModel.productsToCompare.observe(this) { products ->
            // Track compare event
            if (products.size > 1) {
                matomoAnalytics.trackEvent(AnalyticsEvent.CompareProducts(products.size.toFloat()))
            }

            // Create adapter
            val productComparisonAdapter = ProductCompareAdapter(
                products,
                this,
                client,
                picasso,
                localeManager.getLanguage()
            ).apply {
                imageReturnedListener = { product, file ->

                    val image = ProductImage(
                        product.code,
                        ProductImageField.FRONT,
                        file,
                        localeManager.getLanguage()
                    ).apply { filePath = file.absolutePath }

                    lifecycleScope.launch { client.postImg(image).await() }
                    product.imageUrl = file.absolutePath
                }

                fullProductClickListener = {
                    val barcode = it.code
                    openProduct(barcode)
                }
            }

            // Update product comparison recyclerview
            binding.productComparisonRv.layoutManager = LinearLayoutManager(this, HORIZONTAL, false)
            binding.productComparisonRv.adapter = productComparisonAdapter

            // Set activity result handler
            photoReceiverHandler = PhotoReceiverHandler(sharedPreferences) {
                productComparisonAdapter.onImageReturned(it)
            }
        }

        binding.productComparisonButton.setOnClickListener {
            if (!isHardwareCameraInstalled()) return@setOnClickListener

            when {
                checkSelfPermission(this, Manifest.permission.CAMERA) == PERMISSION_GRANTED -> {
                    startScanActivity()
                }
                shouldShowRequestPermissionRationale(this@ProductCompareActivity, Manifest.permission.CAMERA) -> {
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
    }

    override fun startScanActivity() {
        viewModel.productsToCompare.value
            ?.map { it.product }
            ?.let { ContinuousScanActivity.start(this, it as java.util.ArrayList<Product>) }
            ?: error("Products still not set.")
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
