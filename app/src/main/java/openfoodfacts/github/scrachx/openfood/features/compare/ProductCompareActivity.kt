package openfoodfacts.github.scrachx.openfood.features.compare

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.afollestad.materialdialogs.MaterialDialog
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductComparisonBinding
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.utils.MY_PERMISSIONS_REQUEST_CAMERA
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler
import openfoodfacts.github.scrachx.openfood.utils.isHardwareCameraInstalled
import java.util.*

class ProductCompareActivity : BaseActivity() {
    private var _binding: ActivityProductComparisonBinding? = null
    private val binding get() = _binding!!

    private lateinit var productComparisonAdapter: ProductCompareAdapter
    private lateinit var photoReceiverHandler: PhotoReceiverHandler

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

        productComparisonAdapter = ProductCompareAdapter(productsToCompare, this)
        binding.productComparisonRv.layoutManager = LinearLayoutManager(this, HORIZONTAL, false)
        binding.productComparisonRv.adapter = productComparisonAdapter

        photoReceiverHandler = PhotoReceiverHandler { productComparisonAdapter.setImageOnPhotoReturn(it) }

        val finalProductsToCompare = productsToCompare
        binding.productComparisonButton.setOnClickListener {
            if (isHardwareCameraInstalled(this@ProductCompareActivity)) {
                if (ContextCompat.checkSelfPermission(this@ProductCompareActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this@ProductCompareActivity, Manifest.permission.CAMERA)) {
                        MaterialDialog.Builder(this@ProductCompareActivity)
                                .title(R.string.action_about)
                                .content(R.string.permission_camera)
                                .neutralText(R.string.txtOk)
                                .onNeutral { _, _ ->
                                    ActivityCompat.requestPermissions(this@ProductCompareActivity, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
                                }
                                .show()
                    } else {
                        ActivityCompat.requestPermissions(this@ProductCompareActivity, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
                    }
                } else {
                    startActivity(Intent(this@ProductCompareActivity, ContinuousScanActivity::class.java).apply {
                        putExtra(KEY_COMPARE_PRODUCT, true)
                        putExtra(KEY_PRODUCTS_TO_COMPARE, finalProductsToCompare)
                    })
                }
            }
        }
        binding.navigationBottomInclude.bottomNavigation.installBottomNavigation(this)
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