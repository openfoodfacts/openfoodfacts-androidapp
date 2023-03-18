package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.images.IMAGE_URL
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.getAppPreferences
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val daoSession: DaoSession,
    private val productRepository: ProductRepository,
    private val localeManager: LocaleManager,
    private val dispatchers: CoroutineDispatchers,
) {

    suspend fun syncOldHistory() = withContext(dispatchers.IO) {
        val fieldsToRefresh = listOf(
            ApiFields.Keys.IMAGE_SMALL_URL,
            ApiFields.Keys.PRODUCT_NAME,
            ApiFields.Keys.BRANDS,
            ApiFields.Keys.QUANTITY,
            IMAGE_URL,
            ApiFields.Keys.NUTRITION_GRADE_FR,
            ApiFields.Keys.BARCODE
        )

        val historyProducts = daoSession.historyProductDao.loadAll()

        // Refresh each product in the history
        val results = historyProducts.map { product ->
            product to runCatching { refreshProduct(product, fieldsToRefresh) }
        }

        // If any product failed to sync, log the barcodes of the failed products
        val failedProducts = results.filter { it.second.isFailure }
        if (failedProducts.isNotEmpty()) {
            val failedBarcodes = failedProducts.joinToString { it.first.barcode }
            logcat(LogPriority.ERROR) {
                "Could not sync history. Errors on products: $failedBarcodes"
            }
            // If debug logging is enabled, log the full stack traces of the errors
            failedProducts.forEach {
                logcat(LogPriority.DEBUG) {
                    "Error for product ${it.first.barcode}: " +
                            it.second.exceptionOrNull()?.asLog()

                }
            }
        }


        context.getAppPreferences().edit {
            putBoolean("is_old_history_data_synced", true)
        }
    }

    /**
     * Refreshes the product in the database with the latest data from the server.
     *
     * @throws IOException if the product could not be refreshed
     */
    private suspend fun refreshProduct(
        historyProduct: HistoryProduct,
        fieldsToRefresh: List<String>,
    ) {
        val state = productRepository.getProductStateFull(
            historyProduct.barcode,
            fieldsToRefresh.joinToString(",")
        )

        // Products not found should be skipped
        if (state.status <= 0L && state.statusVerbose?.contains("not found") != true) {
            throw IOException("Could not sync history. Error with product ${state.code} ")
        }

        val product = state.product!!
        val hp = HistoryProduct(
            product.productName,
            product.brands,
            product.getImageSmallUrl(localeManager.getLanguage()),
            product.code,
            product.quantity,
            product.nutritionGradeFr,
            product.ecoscore,
            product.novaGroups
        )
        Log.d("syncOldHistory", hp.toString())
        hp.lastSeen = historyProduct.lastSeen
        daoSession.historyProductDao.insertOrReplace(hp)
    }
}
