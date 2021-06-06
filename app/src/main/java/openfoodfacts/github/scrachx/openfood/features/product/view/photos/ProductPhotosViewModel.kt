package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.rx2.await
import openfoodfacts.github.scrachx.openfood.images.extractImagesNameSortedByUploadTimeDesc
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import javax.inject.Inject

@HiltViewModel
class ProductPhotosViewModel @Inject constructor(
    private val productsAPI: ProductsAPI
) : ViewModel() {

    internal val product = MutableLiveData<Product>()

    internal val imageNames = product.switchMap {
        liveData {
            productsAPI.getProductImages(it.code).await()
                .extractImagesNameSortedByUploadTimeDesc()
                .let { emit(it) }
        }
    }

}