package openfoodfacts.github.scrachx.openfood.features.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {
    private val tagLines = MutableLiveData<Array<String>>()
    val tagLine = tagLines.switchMap {
        liveData {
            var i = 0
            while (true) {
                emit(it[i++ % it.size])
                delay(1500)
            }
        }
    }

    fun setupTagLines(items: Array<String>) {
        tagLines.value = items
    }
}
