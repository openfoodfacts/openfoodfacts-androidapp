package openfoodfacts.github.scrachx.openfood.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    fun postImage(image: ProductImage) = viewModelScope.launch(dispatchers.Default) {
        productRepository.postImg(image)
    }

    fun syncOldHistory() = viewModelScope.launch(dispatchers.Default) {
        productRepository.syncOldHistory()
    }

}