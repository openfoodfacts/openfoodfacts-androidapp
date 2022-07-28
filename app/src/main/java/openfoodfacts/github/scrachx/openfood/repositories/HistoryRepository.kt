package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.images.IMAGE_URL
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
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
) {

    suspend fun syncOldHistory() = withContext(Dispatchers.IO) {
        val fields = listOf(
            ApiFields.Keys.IMAGE_SMALL_URL,
            ApiFields.Keys.PRODUCT_NAME,
            ApiFields.Keys.BRANDS,
            ApiFields.Keys.QUANTITY,
            IMAGE_URL,
            ApiFields.Keys.NUTRITION_GRADE_FR,
            ApiFields.Keys.BARCODE
        ).joinToString(",")

        daoSession.historyProductDao.loadAll().forEach { historyProduct ->
            val state = productRepository.getProductStateFull(historyProduct.barcode, fields)

            // Products not found should be skipped
            if (state.status == 0L && state.statusVerbose?.contains("not found") != true) {
                throw IOException("Could not sync history. Error with product ${state.code} ")
            } else if (state.status > 0L) {
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


        context.getAppPreferences().edit {
            putBoolean("is_old_history_data_synced", true)
        }
    }
}
