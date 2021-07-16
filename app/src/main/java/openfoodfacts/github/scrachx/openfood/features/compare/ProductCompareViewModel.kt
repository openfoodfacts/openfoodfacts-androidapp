package openfoodfacts.github.scrachx.openfood.features.compare

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.rx2.await
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import javax.inject.Inject

@HiltViewModel
class ProductCompareViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val localeManager: LocaleManager
) : ViewModel(

) {
    data class CompareProduct(
        val product: Product,
        val additiveNames: List<AdditiveName>
    )

    private val lang: String by lazy { localeManager.getLanguage() }

    val productsToCompare = MutableLiveData<ArrayList<Product>>()

    val products = productsToCompare.switchMap { products ->
        liveData<List<CompareProduct>> {
            products.map { CompareProduct(it, fetchAdditives(it)) }
        }
    }


    private suspend fun fetchAdditives(product: Product): List<AdditiveName> {
        return product.additivesTags.map { tag ->
            productRepository.getAdditiveByTagAndLanguageCode(tag, lang).await()
                .takeUnless { it.isNull } ?: productRepository.getAdditiveByTagAndDefaultLanguageCode(tag).await()
        }.filter { it.isNotNull }
    }

}