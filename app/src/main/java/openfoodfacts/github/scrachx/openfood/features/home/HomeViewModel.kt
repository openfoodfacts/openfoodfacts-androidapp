package openfoodfacts.github.scrachx.openfood.features.home

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority.ERROR
import logcat.LogPriority.WARN
import logcat.asLog
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.features.login.LoginActivity
import openfoodfacts.github.scrachx.openfood.models.TagLine
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.getUserAgent
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val productsAPI: ProductsAPI,
    private val sharedPrefs: SharedPreferences,
    private val localeManager: LocaleManager
) : ViewModel() {

    private val _signInState = MutableSharedFlow<Boolean>()
    val signInState = _signInState.asLiveData(viewModelScope.coroutineContext)

    fun signIn(login: String, password: String) {

        viewModelScope.launch(dispatchers.IO) {
            val response = try {
                productsAPI.signIn(login, password, "Sign-in")
            } catch (err: Throwable) {
                logcat(ERROR) { "Cannot check user credentials. ${err.asLog()}" }
                return@launch
            }

            val htmlBody: String = try {
                response.body()!!.string()
            } catch (err: IOException) {
                logcat(ERROR) { "I/O Exception while checking user saved credentials. ${err.asLog()}" }
                return@launch
            }
            if (LoginActivity.isHtmlNotValid(htmlBody)) {
                logcat(WARN) { "Cannot validate login, deleting saved credentials and asking the user to log back in." }
                _signInState.emit(false)
            } else {
                _signInState.emit(true)
            }
        }
    }

    private val _productCount = MutableSharedFlow<Int>()
    val productCount = _productCount.asLiveData(viewModelScope.coroutineContext)

    fun refreshProductCount() {
        logcat { "Refreshing total product count..." }
        viewModelScope.launch {

            val count = try {
                withContext(dispatchers.IO) {
                    productsAPI.getTotalProductCount(getUserAgent()).count.toInt()
                }.also {
                    sharedPrefs.edit { putInt(PRODUCT_COUNT_KEY, it) }
                }
            } catch (err: Exception) {
                logcat(ERROR) { "Could not retrieve product count from server. ${err.asLog()}" }
                sharedPrefs.getInt(PRODUCT_COUNT_KEY, 0)
            }
            logcat { "Refreshed total product count. There are $count products on the database." }

            _productCount.emit(count)
        }
    }

    private val _tagline = MutableSharedFlow<TagLine>()
    val tagline = _tagline.asLiveData(viewModelScope.coroutineContext)

    fun refreshTagline() {
        viewModelScope.launch {
            val taglineLanguages = try {
                withContext(Dispatchers.IO) { productsAPI.getTaglineLanguages(getUserAgent()) }
            } catch (err: Exception) {
                logcat(WARN) { "Could not retrieve tag-line from server. ${err.asLog()}" }
                return@launch
            }

            val appLanguage = localeManager.getLanguage()

            val selectedLang = taglineLanguages
                .firstOrNull { tag -> appLanguage == tag.language }
                ?: taglineLanguages.lastOrNull { appLanguage in it.language }
                ?: taglineLanguages.last()

            _tagline.emit(selectedLang.tagLine)
        }
    }

    companion object {
        private const val PRODUCT_COUNT_KEY = "productCount"
    }
}