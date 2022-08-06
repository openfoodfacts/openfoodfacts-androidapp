package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.images.ImageNamesParser
import openfoodfacts.github.scrachx.openfood.images.sortedByTimestampDescending
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import javax.inject.Inject

@HiltViewModel
class ProductPhotosViewModel @Inject constructor(
    private val productsAPI: ProductsAPI,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productState = savedStateHandle.getLiveData<ProductState>(ProductEditActivity.KEY_STATE).asFlow()

    val imageNames = productState
        .map { it.product }
        .filterNotNull()
        .map { product ->
            ImageNamesParser.extractImageNames(productsAPI.getProductImages(product.code))
                .sortedByTimestampDescending()
                .map { it.key }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )
}
