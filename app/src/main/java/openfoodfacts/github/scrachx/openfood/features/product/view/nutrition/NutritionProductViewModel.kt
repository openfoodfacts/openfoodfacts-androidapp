package openfoodfacts.github.scrachx.openfood.features.product.view.nutrition

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import openfoodfacts.github.scrachx.openfood.models.ProductState

class NutritionProductViewModel : ViewModel() {
    val productState = MutableLiveData<ProductState>()
}