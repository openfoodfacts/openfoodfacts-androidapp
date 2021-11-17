package openfoodfacts.github.scrachx.openfood.features.product.view

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.repositories.NetworkConnectivityRepository
import openfoodfacts.github.scrachx.openfood.utils.*
import java.io.IOException
import javax.inject.Inject

class ProductViewActivityStarter @Inject constructor(
    private val productsApi: ProductsAPI,
    private val localeManager: LocaleManager,
    private val client: ProductRepository,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val networkConnectivityRepository: NetworkConnectivityRepository,
) {

    /**
     * Open the product in [ProductViewActivity] if the barcode exist.
     * Also add it in the history if the product exist.
     *
     * @param barcode product barcode
     * @param activity
     */
    fun openProduct(barcode: String, activity: FragmentActivity) {
        if (networkConnectivityRepository.isNetworkAvailable()) {
            activity.hideKeyboard()
            activity.lifecycleScope.launch {
                tryToStartActivity(activity, barcode)
            }
        } else {
            showNoNetworkDialog(activity) {
                openProduct(barcode, activity)
            }
        }
    }

    private suspend fun tryToStartActivity(activity: Activity, barcode: String) {
        val result = withContext(coroutineDispatchers.io()) {
            runCatching {
                productsApi.getProductByBarcode(
                    barcode,
                    ApiFields.getAllFields(localeManager.getLanguage()),
                    localeManager.getLanguage(),
                    getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
                )
            }
        }
        withContext(coroutineDispatchers.main()) {
            result.fold(
                onSuccess = { productState ->
                    if (productState.status == 0L) {
                        showNotFoundDialog(activity, barcode, true)
                    } else {
                        client.addToHistory(productState.product!!)
                        ProductViewActivity.start(activity, productState)
                    }
                },
                onFailure = {
                    when (it) {
                        is IOException -> Toast.makeText(activity, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                        else -> showNotFoundDialog(activity, barcode, false)
                    }
                }
            )
        }
    }

    private fun showNotFoundDialog(activity: Activity, barcode: String, withBackPressure: Boolean) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.txtDialogsTitle)
            .setMessage(R.string.product_does_not_exist_please_add_it)
            .setPositiveButton(R.string.txtYes) { _, _ ->
                val product = Product().apply {
                    code = barcode
                    lang = localeManager.getLanguage()
                }
                val intent = Intent(activity, ProductEditActivity::class.java).apply {
                    putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, product)
                }
                activity.startActivity(intent)
                activity.finish()
            }
            .setNegativeButton(R.string.txtNo) { _, _ ->
                if (withBackPressure) {
                    activity.onBackPressed()
                }
            }
            .show()
    }

    private fun showNoNetworkDialog(activity: Activity, positiveAction: () -> Unit) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.device_offline_dialog_title)
            .setMessage(R.string.connectivity_check)
            .setPositiveButton(R.string.txt_try_again) { _, _ -> positiveAction() }
            .setNegativeButton(R.string.dismiss) { d, _ -> d.dismiss() }
            .show()
    }
}
