package openfoodfacts.github.scrachx.openfood.features.compare

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductComparisonBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.utils.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ProductCompareActivity : BaseActivity() {

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var binding: ActivityProductComparisonBinding
    private lateinit var photoReceiverHandler: PhotoReceiverHandler

    private val viewModel: ProductCompareViewModel by viewModels()

    private val scanProductContract = registerForActivityResult(ScanProductActivityContract()) { product ->
        product?.let { viewModel.addProductToCompare(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductComparisonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.compare_products)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navigationBottomInclude.bottomNavigation.installBottomNavigation(this)

        (intent.extras?.getSerializable(KEY_PRODUCTS_TO_COMPARE) as? Product)?.let {
            viewModel.addProductToCompare(it)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.alreadyExistFlow.collect {
                    Log.d("ProductCompareActivity", "alreadyExistFlow")
                    Toast.makeText(this@ProductCompareActivity, getString(R.string.product_already_exists_in_comparison), Toast.LENGTH_SHORT).show()
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productsFlow.collect { products ->
                    Log.d("ProductCompareActivity", "alreadyExistFlow")
                        createAdapter(products)
                    }
            }
        }

        binding.productComparisonButton.setOnClickListener {
            openScanActivity()
        }
    }

    override fun startScanActivity() {
        scanProductContract.launch()
    }


    public override fun onResume() {
        super.onResume()
        binding.navigationBottomInclude.bottomNavigation.selectNavigationItem(R.id.compare_products)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data)
    }

    private fun createAdapter(products: List<ProductCompareViewModel.CompareProduct>) {
        // Create adapter
        val productComparisonAdapter = ProductCompareAdapter(
            products,
            this@ProductCompareActivity,
            client,
            picasso,
            viewModel.getCurrentLanguage()
        ).apply {
            imageReturnedListener = { product, file ->

                val image = ProductImage(
                    product.code,
                    ProductImageField.FRONT,
                    file,
                    viewModel.getCurrentLanguage()
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
        binding.productComparisonRv.layoutManager = LinearLayoutManager(this@ProductCompareActivity, HORIZONTAL, false)
        binding.productComparisonRv.adapter = productComparisonAdapter

        // Set activity result handler
        photoReceiverHandler = PhotoReceiverHandler(sharedPreferences) {
            productComparisonAdapter.onImageReturned(it)
        }
    }

    private fun openScanActivity() {
        if (!isHardwareCameraInstalled()) return

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

    companion object {
        const val KEY_PRODUCTS_TO_COMPARE = "products_to_compare"
        const val KEY_COMPARE_PRODUCT = "compare_product"

        @JvmStatic
        fun start(context: Context, product: Product) {
            context.startActivity(Intent(context, ProductCompareActivity::class.java).apply {
                putExtra(KEY_PRODUCTS_TO_COMPARE, product)
            })
        }

        @JvmStatic
        fun start(context: Context) = context.startActivity(Intent(context, ProductCompareActivity::class.java))
    }
}
