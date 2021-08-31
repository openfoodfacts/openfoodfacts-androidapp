package openfoodfacts.github.scrachx.openfood.features.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import javax.inject.Inject

@HiltViewModel
class ProductCompareViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val localeManager: LocaleManager,
    private val matomoAnalytics: MatomoAnalytics,
    private val coroutineDispatchers: CoroutineDispatchers
) : ViewModel() {

    private val _alreadyExistFlow = MutableSharedFlow<Unit>()
    val alreadyExistFlow = _alreadyExistFlow.asSharedFlow()

    private val _productsFlow = MutableStateFlow<List<CompareProduct>>(emptyList())
    val productsFlow = _productsFlow.asStateFlow()

    init {
        viewModelScope.launch {
            delay(2000)

        }
    }

    fun addProductToCompare(product: Product) {
        viewModelScope.launch {
            if (_productsFlow.value.any { it.product.code == product.code }) {
                _alreadyExistFlow.emit(Unit)
            } else {
                matomoAnalytics.trackEvent(AnalyticsEvent.AddProductToComparison(product.code))
                val result = withContext(coroutineDispatchers.io()) {
                    CompareProduct(product, fetchAdditives(product))
                }
                withContext(coroutineDispatchers.main()) {
                    updateProductList(result)
                }
            }
        }
    }

    fun getCurrentLanguage(): String {
        return localeManager.getLanguage()
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

    data class CompareProduct(
        val product: Product,
        val additiveNames: List<AdditiveName>
    )
}
