package openfoodfacts.github.scrachx.openfood.features.compare

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductComparisonBinding
import openfoodfacts.github.scrachx.openfood.features.compare.ProductCompareViewModel.SideEffect
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivityStarter
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.features.simplescan.SimpleScanActivityContract
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.Intent
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler
import javax.inject.Inject

@AndroidEntryPoint
class ProductCompareActivity : BaseActivity() {

    private lateinit var binding: ActivityProductComparisonBinding

    private val viewModel: ProductCompareViewModel by viewModels()

    @Inject
    lateinit var client: ProductRepository

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var productViewActivityStarter: ProductViewActivityStarter

    @Inject
    lateinit var photoReceiverHandler: PhotoReceiverHandler

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
            launch {
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
            launch {
                viewModel.productsFlow
                    .flowWithLifecycle(lifecycle)
                    .distinctUntilChanged()
                    .collect { createAdapter(it) }
            }
            launch {
                viewModel.loadingVisibleFlow
                    .flowWithLifecycle(lifecycle)
                    .distinctUntilChanged()
                    .collect {
                        binding.comparisonProgressView.isVisible = it
                    }
            }
        }


        binding.productComparisonButton.setOnClickListener { openScanActivity() }
    }

    override fun startScanActivity() {
        scanProductContract.launch()
    }


    public override fun onResume() {
        super.onResume()
        binding.navigationBottomInclude.bottomNavigation.selectNavigationItem(R.id.compare_products)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data) {
            (binding.productComparisonRv.adapter as ProductCompareAdapter).onImageReturned(it)
        }
    }

    private fun createAdapter(products: List<ProductCompareViewModel.CompareProduct>) {
        // Create adapter
        val productComparisonAdapter = ProductCompareAdapter(
            products,
            this,
            this,
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

                lifecycleScope.launch { client.postImg(image) }
                product.imageUrl = file.absolutePath
            }

            fullProductClickListener = {
                val barcode = it.code
                productViewActivityStarter.openProduct(barcode, this@ProductCompareActivity)
            }
        }

        // Update product comparison recyclerview
        binding.productComparisonRv.layoutManager = LinearLayoutManager(this@ProductCompareActivity, HORIZONTAL, false)
        binding.productComparisonRv.adapter = productComparisonAdapter

    }

    private fun showProductAlreadyAddedDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.product_already_exists_in_comparison)
            .setPositiveButton(R.string.ok_button) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showProductNotFoundDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.product_does_not_exist_please_add_it)
            .setPositiveButton(R.string.ok_button) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showConnectionErrorDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.alert_dialog_warning_title)
            .setMessage(R.string.txtConnectionError)
            .setPositiveButton(R.string.ok_button) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        const val KEY_PRODUCTS_TO_COMPARE = "products_to_compare"

        @JvmStatic
        fun start(context: Context, product: Product? = null) {
            context.startActivity(Intent<ProductCompareActivity>(context) {
                if (product != null) putExtra(KEY_PRODUCTS_TO_COMPARE, product)
            })
        }
    }
}
