package openfoodfacts.github.scrachx.openfood.features.compare

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.Utils
import javax.inject.Inject

@HiltViewModel
class ProductCompareViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val localeManager: LocaleManager,
    private val matomoAnalytics: MatomoAnalytics,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val openFoodAPIClient: OpenFoodAPIClient,
) : ViewModel() {

    private val _sideEffectFlow = MutableSharedFlow<SideEffect>()
    val sideEffectFlow = _sideEffectFlow.asSharedFlow()

    private val _productsFlow = MutableStateFlow(listOf<CompareProduct>())
    val productsFlow = _productsFlow.asStateFlow()

    private val _loadingVisibleFlow = MutableStateFlow(false)
    val loadingVisibleFlow = _loadingVisibleFlow.asStateFlow()

    fun barcodeDetected(barcode: String) {
        viewModelScope.launch {
            if (isProductAlreadyAdded(barcode)) {
                emitSideEffect(SideEffect.ProductAlreadyAdded)
            } else {
                fetchProduct(barcode)
            }
        }
    }

    fun addProductToCompare(product: Product) {
        viewModelScope.launch {
            if (isProductAlreadyAdded(product.code)) {
                emitSideEffect(SideEffect.ProductAlreadyAdded)
            } else {
                _loadingVisibleFlow.emit(true)
                matomoAnalytics.trackEvent(AnalyticsEvent.AddProductToComparison(product.code))
                val result = withContext(coroutineDispatchers.io()) {
                    CompareProduct(product, fetchAdditives(product))
                }
                withContext(coroutineDispatchers.main()) {
                    updateProductList(result)
                }
                _loadingVisibleFlow.emit(false)
            }
        }
    }

    fun getCurrentLanguage(): String {
        return localeManager.getLanguage()
    }

    private fun isProductAlreadyAdded(barcode: String): Boolean {
        return _productsFlow.value.any { it.product.code == barcode }
    }

    private fun updateProductList(item: CompareProduct) {
        val newList = _productsFlow.value + item
        _productsFlow.value = newList
        if (newList.size > 1) {
            matomoAnalytics.trackEvent(AnalyticsEvent.CompareProducts(newList.size.toFloat()))
        }
    }

    private suspend fun fetchAdditives(product: Product): List<AdditiveName> {
        return product
            .additivesTags
            .map { tag ->
                productRepository.getAdditiveByTagAndLanguageCode(tag, localeManager.getLanguage()).await()
                    .takeUnless { it.isNull } ?: productRepository.getAdditiveByTagAndDefaultLanguageCode(tag).await()
            }
            .filter { it.isNotNull }
    }

    private suspend fun fetchProduct(barcode: String) {
        _loadingVisibleFlow.emit(true)
        withContext(coroutineDispatchers.io()) {
            try {
                val product = openFoodAPIClient.getProductStateFull(barcode, userAgent = Utils.HEADER_USER_AGENT_SCAN).product
                if (product == null) {
                    emitSideEffect(SideEffect.ProductNotFound)
                } else {
                    addProductToCompare(product)
                }
            } catch (t: Throwable) {
                Log.w("ProductCompareViewModel", t.message, t)
                emitSideEffect(SideEffect.ConnectionError)
            }
        }
    }

    private suspend fun emitSideEffect(effect: SideEffect) {
        withContext(coroutineDispatchers.main()) {
            _sideEffectFlow.emit(effect)
            _loadingVisibleFlow.emit(false)
        }
    }

    data class CompareProduct(
        val product: Product,
        val additiveNames: List<AdditiveName>
    )

    sealed class SideEffect {
        object ProductAlreadyAdded : SideEffect()
        object ProductNotFound : SideEffect()
        object ConnectionError : SideEffect()
    }
}
