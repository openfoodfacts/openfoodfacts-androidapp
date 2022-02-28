package openfoodfacts.github.scrachx.openfood.features.images.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.images.extractImagesNameSortedByUploadTimeDesc
import openfoodfacts.github.scrachx.openfood.models.Barcode
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import javax.inject.Inject

@HiltViewModel
class ImageSelectViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {


    private val barcode = MutableSharedFlow<Barcode>()

    fun setBarcode(code: Barcode) {
        viewModelScope.launch { barcode.emit(code) }
    }

    val imageNames = barcode.map {
        productRepository.getProductImages(it).extractImagesNameSortedByUploadTimeDesc()
    }.asLiveData(viewModelScope.coroutineContext)


}