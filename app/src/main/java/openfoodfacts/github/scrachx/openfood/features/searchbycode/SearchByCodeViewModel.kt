package openfoodfacts.github.scrachx.openfood.features.searchbycode

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivityStarter
import openfoodfacts.github.scrachx.openfood.models.Barcode
import javax.inject.Inject

@HiltViewModel
class SearchByCodeViewModel @Inject constructor(
    private val productViewActivityStarter: ProductViewActivityStarter
) : ViewModel() {

    // Todo: remove activity leak
    fun openProduct(barcode: Barcode, activity: FragmentActivity) {
        viewModelScope.launch {
            productViewActivityStarter.openProduct(barcode, activity)
        }
    }
}