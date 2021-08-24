package openfoodfacts.github.scrachx.openfood.features.compare

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import javax.inject.Inject

@HiltViewModel
class ProductCompareViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val localeManager: LocaleManager
) : ViewModel() {

    val productsToCompare = MutableLiveData<List<CompareProduct>>()

    fun addProductsToCompare(items: List<Product>) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                items.map { CompareProduct(it, fetchAdditives(it)) }
            }
            withContext(Dispatchers.Main) {
                productsToCompare.postValue(result)
            }
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
