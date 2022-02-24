package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.images.extractImagesNameSortedByUploadTimeDesc
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import javax.inject.Inject

@HiltViewModel
class ProductPhotosViewModel @Inject constructor(
    private val productsAPI: ProductsAPI,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productState = savedStateHandle.getLiveData<ProductState>(ProductEditActivity.KEY_STATE).asFlow()

    val imageNames = productState
        .map { it.product }
        .filterNotNull()
        .map { product ->
            productsAPI.getProductImages(product.code).extractImagesNameSortedByUploadTimeDesc()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
        .asLiveData(viewModelScope.coroutineContext)
}
