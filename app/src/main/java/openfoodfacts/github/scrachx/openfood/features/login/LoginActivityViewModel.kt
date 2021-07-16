package openfoodfacts.github.scrachx.openfood.features.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginActivityViewModel : ViewModel() {
    val canLogIn = MutableLiveData(true)
}