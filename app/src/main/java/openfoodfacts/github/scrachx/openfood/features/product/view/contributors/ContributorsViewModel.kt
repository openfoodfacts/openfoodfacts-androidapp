package openfoodfacts.github.scrachx.openfood.features.product.view.contributors

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import javax.inject.Inject

@HiltViewModel
class ContributorsViewModel @Inject constructor(
    private val localeManager: LocaleManager,
    private val productRepository: ProductRepository
) : ViewModel() {

    val product = MutableLiveData<Product>()

    val states = product.switchMap { product ->
        liveData {
            val tags = product.statesTags
            if (tags.isEmpty()) emit(null)

            val languageCode = localeManager.getLanguage()

            try {
                emit(tags.map { productRepository.getStatesByTagAndLanguageCode(it, languageCode) })
            } catch (err: Throwable) {
                emit(null)
            }
        }
    }
}