package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import openfoodfacts.github.scrachx.openfood.images.extractImagesNameSortedByUploadTimeDesc
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import javax.inject.Inject

@HiltViewModel
class ProductPhotosViewModel @Inject constructor(
    private val productsAPI: ProductsAPI
) : ViewModel() {

    private val _product = MutableSharedFlow<Product>()
    val product = _product.asSharedFlow()

    fun setProduct(product: Product) {
        viewModelScope.launch { _product.emit(product) }
    }

    val imageNames = product.map {
        productsAPI.getProductImages(it.code)
            .extractImagesNameSortedByUploadTimeDesc()
    }

}