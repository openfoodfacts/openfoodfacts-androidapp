package openfoodfacts.github.scrachx.openfood.features.scanhistory

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.models.HistoryProductDao
import openfoodfacts.github.scrachx.openfood.models.getSmallFrontImageUrl
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.*
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class ScanHistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    daoSession: DaoSession,
    private val productRepository: ProductRepository,
    private val localeManager: LocaleManager,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    private val historyProductDao: HistoryProductDao = daoSession.historyProductDao

    private val unorderedProductState = MutableStateFlow<FetchProductsState>(FetchProductsState.Loading)

    val productsState = unorderedProductState.map { state ->
        when (state) {
            is FetchProductsState.Loading, is FetchProductsState.Error -> return@map state
            is FetchProductsState.Data -> {
                try {
                    state.items
                        .customSort(_sortType.value)
                        .let { return@map FetchProductsState.Data(it) }
                } catch (err: Exception) {
                    return@map FetchProductsState.Error
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, FetchProductsState.Loading)

    private val _sortType = MutableStateFlow(SortType.TIME)
    val sortType = _sortType.asStateFlow()

    fun refreshItems() {
        viewModelScope.launch {
            unorderedProductState.emit(FetchProductsState.Loading)

            withContext(dispatchers.IO) {
                val barcodes = historyProductDao.list().map { it.barcode }
                if (barcodes.isNotEmpty()) {
                    try {
                        productRepository.getProductsByBarcode(barcodes)
                            .forEach { product ->
                                val historyProduct = historyProductDao.unique {
                                    where(HistoryProductDao.Properties.Barcode.eq(product.barcode.raw))
                                } ?: HistoryProduct()

                                product.productName?.let { historyProduct.title = it }
                                product.brands?.let { historyProduct.brands = it }
                                product.getSmallFrontImageUrl(localeManager.getLanguage())
                                    ?.let { historyProduct.url = it }
                                product.quantity?.let { historyProduct.quantity = it }
                                product.nutritionGradeFr?.let { historyProduct.nutritionGrade = it }
                                product.ecoscore?.let { historyProduct.ecoscore = it }
                                product.novaGroups?.let { historyProduct.novaGroup = it }

                                historyProductDao.update(historyProduct)
                            }
                    } catch (err: Exception) {
                        unorderedProductState.emit(FetchProductsState.Error)
                    }
                }

                val updatedProducts = historyProductDao.list()
                unorderedProductState.emit(FetchProductsState.Data(updatedProducts))
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            withContext(dispatchers.IO) {
                unorderedProductState.emit(FetchProductsState.Loading)

                try {
                    historyProductDao.deleteAll()
                } catch (err: Exception) {
                    unorderedProductState.emit(FetchProductsState.Error)
                }

                unorderedProductState.emit(FetchProductsState.Data(emptyList()))
            }
        }
    }


    fun removeProductFromHistory(product: HistoryProduct) {
        viewModelScope.launch {
            withContext(dispatchers.IO) {
                try {
                    historyProductDao.delete(product)
                } catch (err: Exception) {
                    unorderedProductState.emit(FetchProductsState.Error)
                }

                val products = historyProductDao.list()
                unorderedProductState.emit(FetchProductsState.Data(products))
            }
        }
    }

    fun updateSortType(type: SortType) {
        viewModelScope.launch {
            _sortType.emit(type)

            // refresh
            unorderedProductState.emit(productsState.value)
        }
    }

    /**
     * Function to compare history items based on title, brand, barcode, time and nutrition grade
     */
    private fun List<HistoryProduct>.customSort(sortType: SortType?) = when (sortType) {
        SortType.TITLE -> sortedWith { item1, item2 ->
            if (item1.title.isNullOrEmpty()) item1.title = context.getString(R.string.no_title)
            if (item2.title.isNullOrEmpty()) item2.title = context.getString(R.string.no_title)
            item1.title.compareTo(item2.title, true)
        }

        SortType.BRAND -> sortedWith { item1, item2 ->
            if (item1.brands.isNullOrEmpty()) item1.brands = context.getString(R.string.no_brand)
            if (item2.brands.isNullOrEmpty()) item2.brands = context.getString(R.string.no_brand)
            item1.brands!!.compareTo(item2.brands!!, true)
        }

        SortType.BARCODE -> sortedBy { it.barcode }
        SortType.GRADE -> sortedBy { it.nutritionGrade }
        SortType.TIME -> sortedByDescending { it.lastSeen }
        SortType.NONE, null -> this
    }

    sealed class FetchProductsState {
        data class Data(val items: List<HistoryProduct>) : FetchProductsState()
        object Loading : FetchProductsState()
        object Error : FetchProductsState()
    }
}
