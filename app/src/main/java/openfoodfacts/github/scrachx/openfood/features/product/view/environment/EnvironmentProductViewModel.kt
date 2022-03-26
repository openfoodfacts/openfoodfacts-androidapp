package openfoodfacts.github.scrachx.openfood.features.product.view.environment

import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.Nutriment
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import javax.inject.Inject

class EnvironmentProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val localeManager: LocaleManager,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    fun postImg(productImage: ProductImage) {
        viewModelScope.launch(dispatchers.IO) {
            productRepository.postImg(productImage)
        }
    }

    private val _product = MutableSharedFlow<Product>()
    fun setProduct(product: Product) {
        viewModelScope.launch { _product.emit(product) }
    }

    val imagePackagingUrl = _product
        .map { it.getImagePackagingUrl(localeManager.getLanguage()) }
        .filterNot { it.isNullOrBlank() }
        .asLiveData(viewModelScope.coroutineContext)

    val carbonFootprint = _product
        .map { it.nutriments[Nutriment.CARBON_FOOTPRINT] }
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * Null if the info card string is null or empty.
     */
    val environmentInfoCard = _product.map { product ->
        product.environmentInfoCard
            .takeUnless { it.isNullOrEmpty() }
            ?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT) }
    }.asLiveData(viewModelScope.coroutineContext)

    val packaging = _product.map { product ->
        product.packaging
            .takeUnless { it.isNullOrEmpty() }
            ?.replace(",", ", ")
    }.asLiveData(viewModelScope.coroutineContext)

    val recyclingInstructionsToDiscard = _product
        .map { product -> product.recyclingInstructionsToDiscard.takeUnless { it.isNullOrEmpty() } }
        .asLiveData(viewModelScope.coroutineContext)

    val recyclingInstructionsToRecycle = _product
        .map { product -> product.recyclingInstructionsToRecycle.takeUnless { it.isNullOrEmpty() } }
        .asLiveData(viewModelScope.coroutineContext)

    val statesTags = _product
        .map { it.statesTags }
        .asLiveData(viewModelScope.coroutineContext)



}