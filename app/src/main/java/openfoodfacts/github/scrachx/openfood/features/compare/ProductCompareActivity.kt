package openfoodfacts.github.scrachx.openfood.features.compare

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductComparisonBinding
import openfoodfacts.github.scrachx.openfood.features.compare.ProductCompareViewModel.SideEffect
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.features.simplescan.SimpleScanActivityContract
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

    private val scanProductContract = registerForActivityResult(SimpleScanActivityContract()) { barcode ->
        barcode?.let { viewModel.barcodeDetected(it) }
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
            viewModel.sideEffectFlow
                .flowWithLifecycle(lifecycle)
                .collect {
                    when (it) {
                        is SideEffect.ProductAlreadyAdded -> showProductAlreadyAddedDialog()
                        is SideEffect.ProductNotFound -> showProductNotFoundDialog()
                        is SideEffect.ConnectionError -> showConnectionErrorDialog()
                    }
                }
        }
        lifecycleScope.launch {
            viewModel.productsFlow
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .collect { products ->
                    createAdapter(products)
                }
        }
        lifecycleScope.launch {
            viewModel.loadingVisibleFlow
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .collect {
                    binding.comparisonProgressView.isVisible = it
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

    private fun showProductAlreadyAddedDialog() {
        MaterialDialog.Builder(this)
            .content(R.string.product_already_exists_in_comparison)
            .positiveText(R.string.ok_button)
            .show()
    }

    private fun showProductNotFoundDialog() {
        MaterialDialog.Builder(this)
            .content(R.string.txtDialogsContentPowerMode)
            .positiveText(R.string.ok_button)
            .show()
    }

    private fun showConnectionErrorDialog() {
        MaterialDialog.Builder(this)
            .title(R.string.alert_dialog_warning_title)
            .content(R.string.txtConnectionError)
            .positiveText(R.string.ok_button)
            .show()
    }

    companion object {
        const val KEY_PRODUCTS_TO_COMPARE = "products_to_compare"

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
