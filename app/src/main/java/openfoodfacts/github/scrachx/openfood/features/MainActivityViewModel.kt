package openfoodfacts.github.scrachx.openfood.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) : ViewModel() {

    fun postImage(image: ProductImage) {
        viewModelScope.launch { productRepository.postImg(image) }
    }

    fun syncOldHistory() = viewModelScope.launch { productRepository.syncOldHistory() }

}