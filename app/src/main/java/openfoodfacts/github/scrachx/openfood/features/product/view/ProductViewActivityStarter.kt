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
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.repositories.NetworkConnectivityRepository
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.*
import java.io.IOException
import javax.inject.Inject

class ProductViewActivityStarter @Inject constructor(
    private val productsApi: ProductsAPI,
    private val localeManager: LocaleManager,
    private val client: ProductRepository,
    private val dispatchers: CoroutineDispatchers,
    private val networkConnectivityRepository: NetworkConnectivityRepository,
) {

    /**
     * Open the product in [ProductViewActivity] if the barcode exist.
     * Also add it in the history if the product exist.
     *
     * @param barcode product barcode
     * @param activity
     */
    fun openProduct(
        barcode: String,
        activity: FragmentActivity,
        productOpenedListener: (() -> Unit)? = null,
        productErrorListener: ((ErrorType) -> Unit)? = null,
    ) {
        if (networkConnectivityRepository.isNetworkAvailable()) {
            activity.hideKeyboard()
            activity.lifecycleScope.launch {
                val errorType = tryToStartActivity(activity, barcode)

                if (errorType == null) {
                    productOpenedListener?.invoke()
                } else {
                    productErrorListener?.invoke(errorType)
                }
            }
        } else {
            showNoNetworkDialog(activity) {
                productErrorListener?.invoke(ErrorType.NoNetworkAvailable)
                openProduct(barcode, activity)
            }
        }
    }

    private suspend fun tryToStartActivity(activity: Activity, barcode: String): ErrorType? {
        val result = withContext(dispatchers.IO) {
            runCatching {
                productsApi.getProductByBarcode(
                    barcode,
                    ApiFields.getAllFields(localeManager.getLanguage()),
                    localeManager.getLanguage(),
                    getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
                )
            }
        }

        return withContext(dispatchers.Main) {
            result.fold(
                onSuccess = { state ->
                    if (state.status == 0L) {
                        showNotFoundDialog(activity, barcode, true)
                        ErrorType.NotFound
                    } else {
                        client.addToHistory(state.product!!)
                        ProductViewActivity.start(activity, state)
                        null
                    }
                },
                onFailure = {
                    when (it) {
                        is IOException -> {
                            Toast.makeText(activity, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                            ErrorType.NotFound
                        }
                        else -> {
                            showNotFoundDialog(activity, barcode, false)
                            ErrorType.NotFound
                        }
                    }
                }
            )
        }
    }

    private fun showNotFoundDialog(activity: Activity, barcode: String, withBackPressure: Boolean) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.txtDialogsTitle)
            .setMessage(R.string.product_does_not_exist_please_add_it)
            .setPositiveButton(R.string.txtYes) { d, _ ->
                d.dismiss()

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
            .setNegativeButton(R.string.txtNo) { d, _ ->
                d.dismiss()

                if (withBackPressure) {
                    activity.onBackPressed()
                }
            }
            .show()
    }

    private fun showNoNetworkDialog(activity: Activity, tryAgainAction: () -> Unit) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.device_offline_dialog_title)
            .setMessage(R.string.connectivity_check)
            .setPositiveButton(R.string.txt_try_again) { d, _ ->
                d.dismiss()
                tryAgainAction()
            }
            .setNegativeButton(R.string.dismiss) { d, _ -> d.dismiss() }
            .show()
    }


    enum class ErrorType {
        NoNetworkAvailable, NotFound, GenericError
    }
}



