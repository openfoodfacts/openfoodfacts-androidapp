package openfoodfacts.github.scrachx.openfood.features.scanhistory

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.BehaviorRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.flatMapIterable
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.models.HistoryProductDao
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.SortType
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class ScanHistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val daoSession: DaoSession,
    private val client: OpenFoodAPIClient,
    private val localeManager: LocaleManager
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val fetchProductsStateRelay = BehaviorRelay.createDefault<FetchProductsState>(FetchProductsState.Loading)
    private var sortType = SortType.TIME

    init {
        refreshItems()
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    fun observeFetchProductState() = fetchProductsStateRelay

    fun refreshItems() {
        fetchProductsStateRelay.accept(FetchProductsState.Loading)
        Single.fromCallable { daoSession.historyProductDao.queryBuilder().list() }
            .flatMap { products ->
                client.getProductsByBarcode(products.map { it.barcode })
            }
            .toObservable()
            .flatMapIterable()
            .map { product ->
                val historyProduct = daoSession.historyProductDao.queryBuilder()
                    .where(HistoryProductDao.Properties.Barcode.eq(product.code))
                    .build()
                    .unique()

                product.productName?.let { historyProduct.title = it }
                product.brands?.let { historyProduct.brands = it }
                product.getImageSmallUrl(localeManager.getLanguage())?.let { historyProduct.url = it }
                product.quantity?.let { historyProduct.quantity = it }
                product.nutritionGradeFr?.let { historyProduct.nutritionGrade = it }
                product.ecoscore?.let { historyProduct.ecoscore = it }
                product.novaGroups?.let { historyProduct.novaGroup = it }
                daoSession.historyProductDao.update(historyProduct)
            }.toList()

            .map { daoSession.historyProductDao.queryBuilder().list() }
            .map { it.customSort() }
            .subscribeOn(Schedulers.io())
            .doOnError { fetchProductsStateRelay.accept(FetchProductsState.Error) }
            .subscribe { items -> fetchProductsStateRelay.accept(FetchProductsState.Data(items)) }
            .addTo(compositeDisposable)
    }

    fun clearHistory() {
        fetchProductsStateRelay.accept(FetchProductsState.Loading)
        Completable.fromCallable { daoSession.historyProductDao.deleteAll() }
            .subscribeOn(Schedulers.io())
            .doOnError { fetchProductsStateRelay.accept(FetchProductsState.Error) }
            .subscribe { fetchProductsStateRelay.accept(FetchProductsState.Data(emptyList())) }
            .addTo(compositeDisposable)
    }

    fun removeProductFromHistory(product: HistoryProduct) {
        Observable.fromCallable {
            daoSession.historyProductDao.delete(product)
            daoSession.historyProductDao.queryBuilder().list()
        }
            .map { it.customSort() }
            .subscribeOn(Schedulers.io())
            .doOnError { fetchProductsStateRelay.accept(FetchProductsState.Error) }
            .subscribe { products -> fetchProductsStateRelay.accept(FetchProductsState.Data(products)) }
            .addTo(compositeDisposable)

    }

    fun updateSortType(type: SortType) {
        sortType = type
        fetchProductsStateRelay
            .take(1)
            .map {
                (it as? FetchProductsState.Data)?.items?.customSort() ?: emptyList()
            }
            .filter { it.isNotEmpty() }
            .subscribeOn(Schedulers.io())
            .doOnError { fetchProductsStateRelay.accept(FetchProductsState.Error) }
            .subscribe { products -> fetchProductsStateRelay.accept(FetchProductsState.Data(products)) }
            .addTo(compositeDisposable)
    }

    fun openProduct(barcode: String, activity: Activity) {
        client.openProduct(barcode, activity)
    }

    /**
     * Function to compare history items based on title, brand, barcode, time and nutrition grade
     */
    private fun List<HistoryProduct>.customSort() = when (sortType) {
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
        SortType.NONE -> this
    }

    sealed class FetchProductsState {
        object Loading : FetchProductsState()
        data class Data(val items: List<HistoryProduct>) : FetchProductsState()
        object Error : FetchProductsState()
    }
}
