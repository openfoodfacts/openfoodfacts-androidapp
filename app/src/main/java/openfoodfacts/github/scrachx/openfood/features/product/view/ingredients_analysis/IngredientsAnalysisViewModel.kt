package openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductIngredient
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import javax.inject.Inject

@HiltViewModel
class IngredientsAnalysisViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val product: MutableSharedFlow<Product> = MutableSharedFlow()

    fun updateProduct(product: Product) = viewModelScope.launch {
        this@IngredientsAnalysisViewModel.product.emit(product)
    }

    val ingredients: Flow<List<ProductIngredient>?> = product.map(productRepository::getIngredients)

}