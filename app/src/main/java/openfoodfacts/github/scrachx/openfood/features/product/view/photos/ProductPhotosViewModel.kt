package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.node.ObjectNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.images.ImageNameParser
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import javax.inject.Inject

@HiltViewModel
class ProductPhotosViewModel @Inject constructor(
    private val productsAPI: ProductsAPI,
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productState = savedStateHandle.getLiveData<ProductState>(ProductEditActivity.KEY_STATE).asFlow()

    val imageNames = productState
        .map { it.product }
        .filterNotNull()
        .map { product ->
            ImageNameParser.extractImageNames(productsAPI.getProductImages(product.code))
                .map { it.value }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )


    fun editImage(product: Product, imgMap: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = productRepository.editImage(product.code, imgMap)
            _stateFlow.emit(State.SetImageName(response))
        }
    }

    private val _stateFlow = MutableSharedFlow<State>()
    val stateFlow = _stateFlow.asSharedFlow()

    sealed class State {
        data class SetImageName(val response: ObjectNode) : State()
    }
}
