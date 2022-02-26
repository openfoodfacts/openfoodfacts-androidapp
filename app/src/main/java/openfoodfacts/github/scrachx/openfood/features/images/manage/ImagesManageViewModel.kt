package openfoodfacts.github.scrachx.openfood.features.images.manage

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.images.ImageTransformation
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import openfoodfacts.github.scrachx.openfood.utils.FileDownloader
import javax.inject.Inject

@HiltViewModel
class ImagesManageViewModel @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val productRepository: ProductRepository,
    private val fileDownloader: FileDownloader,
) : ViewModel() {

    private val _sendState = MutableSharedFlow<Result<Unit>>()
    val sendState = _sendState.asLiveData(viewModelScope.coroutineContext)

    fun sendImage(image: ProductImage) {
        // Send image
        viewModelScope.launch(dispatchers.IO) {
            _sendState.emit(runCatching { productRepository.postImg(image, true) })
        }
    }

    fun unSelectImage(product: Product, field: ProductImageField, language: String) {
        viewModelScope.launch(dispatchers.IO) {
            _sendState.emit(runCatching { productRepository.unSelectImage(product.barcode, field, language) })
        }
    }

    data class EditPhoto(val field: ProductImageField, val transformation: ImageTransformation, val fileUri: Uri?)

    private val _editState = MutableSharedFlow<EditPhoto>()
    val editState = _editState.asLiveData(viewModelScope.coroutineContext)

    fun editPhoto(field: ProductImageField, transformation: ImageTransformation) {
        val url = transformation.imageUrl?.takeIf { it.isNotBlank() } ?: return

        viewModelScope.launch(dispatchers.IO) {
            val fileUri = fileDownloader.download(url)
            _editState.emit(EditPhoto(field, transformation, fileUri))

        }
    }

}