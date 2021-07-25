package openfoodfacts.github.scrachx.openfood.features.product.edit.nutrition

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.Product
import javax.inject.Inject

@HiltViewModel
class ProductEditNutritionViewModel @Inject constructor() : ViewModel() {

    val product = MutableLiveData<Product>()

    val noNutritionFactsChecked = MutableLiveData<Boolean>()

    val dataFormat = MutableLiveData(R.id.for100g_100ml)

}