package openfoodfacts.github.scrachx.openfood.features.product.edit.nutrition

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.Product
import javax.inject.Inject

@HiltViewModel
class ProductEditNutritionViewModel @Inject constructor() : ViewModel() {

    val product = MutableLiveData<Product>()

    val noNutritionFactsChecked = MutableLiveData<Boolean>()

    val dataFormat = MutableLiveData<Int>()

    val isDataPerServing = dataFormat.map { it == R.id.per_serving }
    val isDataPer100g = dataFormat.map { it == R.id.for100g_100ml }
}