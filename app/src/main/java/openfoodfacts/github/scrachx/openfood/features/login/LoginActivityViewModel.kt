package openfoodfacts.github.scrachx.openfood.features.login

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import openfoodfacts.github.scrachx.openfood.utils.getLoginPreferences
import java.net.HttpCookie
import javax.inject.Inject

@HiltViewModel
class LoginActivityViewModel @Inject constructor(
    application: Application,
    private val dispatchers: CoroutineDispatchers,
    private val productsAPI: ProductsAPI,
    private val matomoAnalytics: MatomoAnalytics,
) : AndroidViewModel(application) {

    private val loginPreferences = application.getLoginPreferences()

    enum class LoginStatus {
        WebError, IncorrectCredentials, Success, Loading
    }

    val loginStatus = MutableSharedFlow<LoginStatus>()

    fun tryLogin(login: String, password: String) {
        viewModelScope.launch(dispatchers.IO) {

            loginStatus.emit(LoginStatus.Loading)

            val response = productsAPI.runCatching { signIn(login, password, "Sign-in") }
                .fold(
                    onFailure = {
                        loginStatus.emit(LoginStatus.WebError)
                        logcat(LogPriority.ERROR) { it.asLog() }
                        return@launch
                    },
                    onSuccess = { it }
                )

            if (!response.isSuccessful) {
                loginStatus.emit(LoginStatus.WebError)
                return@launch
            }

            val htmlNoParsed = response.runCatching { body()?.string() }
                .fold(
                    onFailure = {
                        logcat(LogPriority.ERROR) { "Unable to parse the login response page: " + it.asLog() }
                        loginStatus.emit(LoginStatus.WebError)
                        return@launch
                    },
                    onSuccess = { it }
                )

            if (LoginActivity.isHtmlNotValid(htmlNoParsed)) {
                loginStatus.emit(LoginStatus.IncorrectCredentials)
                return@launch
            }

            // store the user session id (user_session and user_id)
            for (httpCookie in HttpCookie.parse(response.headers()["set-cookie"])) {
                // Example format of set-cookie: session=user_session&S0MeR@nD0MSECRETk3Y&user_id&testuser; domain=.openfoodfacts.org; path=/
                if (BuildConfig.HOST.contains(httpCookie.domain) && httpCookie.path == "/") {
                    httpCookie.value
                        .split("&")
                        .windowed(2, 2)
                        .forEach { (name, value) -> loginPreferences.edit { putString(name, value) } }
                    break
                }
            }

            loginPreferences.edit {
                putString("user", login)
                putString("pass", password)
            }

            matomoAnalytics.trackEvent(AnalyticsEvent.UserLogin)
            loginStatus.emit(LoginStatus.Success)
        }
    }


    val loginButtonEnabled = loginStatus.map {
        it != LoginStatus.Loading
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        true
    )

}