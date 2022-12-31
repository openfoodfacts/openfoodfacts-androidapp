package openfoodfacts.github.scrachx.openfood.features.product.view.environment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.Barcode
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EnvironmentProductViewModel @Inject constructor(
    private val localeManager: LocaleManager,
    private val productRepository: ProductRepository,
) : ViewModel() {

    fun uploadImage(file: File, barcode: Barcode) {
        // Create a new instance of ProductImage so we can load to server
        val image = ProductImage(
            code = barcode.raw,
            field = ProductImageField.PACKAGING,
            imageFile = file,
            language = localeManager.getLanguage()
        )

        // Load to server
        viewModelScope.launch { productRepository.postImg(image) }
    }


}