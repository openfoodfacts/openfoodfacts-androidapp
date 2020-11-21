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
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductComparisonBinding
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.install
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler
import openfoodfacts.github.scrachx.openfood.utils.Utils
import java.util.*

class ProductCompareActivity : BaseActivity() {
    private var _binding: ActivityProductComparisonBinding? = null
    private val binding get() = _binding!!
    private var photoReceiverHandler: PhotoReceiverHandler? = null
    private lateinit var productComparisonAdapter: ProductCompareAdapter
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductComparisonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.compare_products)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        photoReceiverHandler = PhotoReceiverHandler(productComparisonAdapter::setImageOnPhotoReturn)

        var productsToCompare = mutableListOf<Product>()
        if (intent.extras != null && intent.getBooleanExtra(KEY_PRODUCT_FOUND, false)) {
            productsToCompare = intent.extras?.getSerializable(KEY_PRODUCTS_TO_COMPARE) as ArrayList<Product>
            if (intent.getBooleanExtra(KEY_PRODUCT_ALREADY_EXISTS, false)) {
                Toast.makeText(this, getString(R.string.product_already_exists_in_comparison), Toast.LENGTH_SHORT).show()
            }
        }

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.productComparisonRv.layoutManager = layoutManager

        productComparisonAdapter = ProductCompareAdapter(productsToCompare, this)
        binding.productComparisonRv.adapter = productComparisonAdapter

        val finalProductsToCompare = productsToCompare
        binding.productComparisonButton.setOnClickListener {
            if (Utils.isHardwareCameraInstalled(this@ProductCompareActivity)) {
                if (ContextCompat.checkSelfPermission(this@ProductCompareActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this@ProductCompareActivity, Manifest.permission.CAMERA)) {
                        MaterialDialog.Builder(this@ProductCompareActivity)
                                .title(R.string.action_about)
                                .content(R.string.permission_camera)
                                .neutralText(R.string.txtOk)
                                .onNeutral { _: MaterialDialog?, _: DialogAction? ->
                                    ActivityCompat
                                            .requestPermissions(this@ProductCompareActivity, arrayOf(Manifest.permission.CAMERA), Utils.MY_PERMISSIONS_REQUEST_CAMERA)
                                }
                                .show()
                    } else {
                        ActivityCompat.requestPermissions(this@ProductCompareActivity, arrayOf(Manifest.permission.CAMERA), Utils.MY_PERMISSIONS_REQUEST_CAMERA)
                    }
                } else {
                    startActivity(Intent(this@ProductCompareActivity, ContinuousScanActivity::class.java).apply {
                        putExtra(KEY_COMPARE_PRODUCT, true)
                        putExtra(KEY_PRODUCTS_TO_COMPARE, finalProductsToCompare.toTypedArray())
                    })
                }
            }
        }
        install(this, binding.navigationBottomInclude.bottomNavigation)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler!!.onActivityResult(this, requestCode, resultCode, data)
    }

    public override fun onResume() {
        super.onResume()
        selectNavigationItem(binding.navigationBottomInclude.bottomNavigation, R.id.compare_products)
    }

    companion object {
        const val KEY_PRODUCTS_TO_COMPARE = "products_to_compare"
        const val KEY_PRODUCT_FOUND = "product_found"
        const val KEY_COMPARE_PRODUCT = "compare_product"
        const val KEY_PRODUCT_ALREADY_EXISTS = "product_already_exists"

        @JvmStatic
        fun start(context: Context, product: Product) {
            val intent = Intent(context, ProductCompareActivity::class.java)
            intent.putExtra(KEY_PRODUCT_FOUND, true)
            val productsToCompare = ArrayList<Product>()
            productsToCompare.add(product)
            intent.putExtra(KEY_PRODUCTS_TO_COMPARE, productsToCompare)
            context.startActivity(intent)
        }

        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, ProductCompareActivity::class.java)
            context.startActivity(starter)
        }
    }
}