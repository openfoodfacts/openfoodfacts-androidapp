package openfoodfacts.github.scrachx.openfood.features.images.zoom

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageZoomViewModel @Inject constructor() : ViewModel() {
    val isRefreshing = MutableLiveData<Boolean>()
}