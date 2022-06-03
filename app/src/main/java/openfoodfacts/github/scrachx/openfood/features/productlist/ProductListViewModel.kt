package openfoodfacts.github.scrachx.openfood.features.productlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.HistoryProductDao
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.ListedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists
import openfoodfacts.github.scrachx.openfood.utils.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val localeManager: LocaleManager,
    private val savedStateHandle: SavedStateHandle,
    private val daoSession: DaoSession
) : ViewModel() {

    init {
        tryAddProduct()
        fetchProductList()
    }

    val listId: Long = savedStateHandle.get(ProductListActivity.KEY_LIST_ID)
        ?: error("Could not load listId from intent data!")

    val listName: String = savedStateHandle.get(ProductListActivity.KEY_LIST_NAME)
        ?: error("Could not load listName from intent data!")

    val productList = MutableStateFlow(ProductLists())

    fun removeProduct(productToRemove: ListedProduct) {
        viewModelScope.launch {
            // Update view model
            productList.update {
                it.apply {
                    numOfProducts -= 1
                    products.remove(productToRemove)
                }
            }

            // Update db
            daoSession.listedProductDao.delete(productToRemove)
            daoSession.productListsDao.update(productList.value)
        }
    }

    fun sortBy(sortType: SortType) {
        viewModelScope.launch {
            // Update view model
            productList.update {
                val oldList = it.products
                it.apply { products = oldList.customSortedBy(sortType) }
            }
        }
    }

    private fun MutableList<ListedProduct>.customSortedBy(sortType: SortType): List<ListedProduct> {
        return when (sortType) {
            SortType.TITLE -> sortedBy { it.productName }
            SortType.BRAND -> sortedBy { it.productDetails }
            SortType.BARCODE -> sortedBy { it.barcode }
            SortType.GRADE -> {

                // Get list of HistoryProduct items for the YourListProduct items
                // We need the toTypedArray because of the varargs of whereOr
                val gradesConditions = this.map { HistoryProductDao.Properties.Barcode.eq(it.barcode) }
                val historyProductsGrade = daoSession.historyProductDao.list {
                    whereOr(gradesConditions)
                }

                sortedWith { p1, p2 ->
                    var g1 = "E"
                    var g2 = "E"
                    historyProductsGrade.filter { it.nutritionGrade != null }.forEach { h ->
                        if (h.barcode == p1.barcode) {
                            g1 = h.nutritionGrade
                        }
                        if (h.barcode == p2.barcode) {
                            g2 = h.nutritionGrade
                        }
                    }
                    g1.compareTo(g2, ignoreCase = true)
                }
            }
            SortType.TIME -> {
                //get list of HistoryProduct items for the YourListProduct items
                val times = this.map { HistoryProductDao.Properties.Barcode.eq(it.barcode) }

                val historyProductsTime = daoSession.historyProductDao.list {
                    whereOr(times)
                }

                sortedWith { p1: ListedProduct, p2: ListedProduct ->
                    var d1 = Date(0)
                    var d2 = Date(0)
                    historyProductsTime.filter { it.lastSeen != null }.forEach {
                        if (it.barcode == p1.barcode) {
                            d1 = it.lastSeen
                        }
                        if (it.barcode == p2.barcode) {
                            d2 = it.lastSeen
                        }
                    }
                    d2.compareTo(d1)
                }
            }
            else -> sortedWith { _, _ -> 0 }
        }
    }

    private fun fetchProductList() {
        viewModelScope.launch {
            val list = daoSession.productListsDao.load(listId) ?: error("Could not load list with id=$listId")
            list.resetProducts()
            productList.emit(list)
        }
    }

    private fun tryAddProduct() {
        val product = savedStateHandle.get<Product?>(ProductListActivity.KEY_PRODUCT_TO_ADD) ?: return

        val locale = localeManager.getLanguage()
        if (product.productName != null && product.getImageSmallUrl(locale) != null) {

            val listedProduct = ListedProduct().also {
                it.listId = listId
                it.listName = listName
                it.barcode = product.code
                it.productName = product.productName
                it.productDetails = product.getProductBrandsQuantityDetails()
                it.imageUrl = product.getImageSmallUrl(locale)
            }

            daoSession.listedProductDao.insertOrReplace(listedProduct)
        }
    }
}
