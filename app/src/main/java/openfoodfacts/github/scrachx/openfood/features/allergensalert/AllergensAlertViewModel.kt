package openfoodfacts.github.scrachx.openfood.features.allergensalert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.repositories.AllergenPreferencesRepository
import openfoodfacts.github.scrachx.openfood.repositories.NetworkConnectivityRepository
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import javax.inject.Inject

@HiltViewModel
class AllergensAlertViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val matomoAnalytics: MatomoAnalytics,
    private val localeManager: LocaleManager,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val allergenPreferencesRepository: AllergenPreferencesRepository,
    private val networkConnectivityRepository: NetworkConnectivityRepository
) : ViewModel() {

    private val _sideEffectFlow = MutableSharedFlow<SideEffect>()
    val sideEffectFlow = _sideEffectFlow.asSharedFlow()

    private val _viewStateFlow = MutableStateFlow(ViewState(false, emptyList()))
    val viewStateFlow = _viewStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            refreshAllergens()
        }
    }

    fun addAllergenClicked() {
        viewModelScope.launch {
            if (isDatabaseEmpty()) {
                if (networkConnectivityRepository.isNetworkAvailable()) {
                    downloadAllergensFromServer()
                } else {
                    _sideEffectFlow.emit(SideEffect.ShowNoDataDialog)
                }
            } else {
                val allergens = getNotEnabledAllergens()
                _sideEffectFlow.emit(SideEffect.ShowAddAllergenDialog(allergens))
            }
        }
    }

    fun addAllergen(allergen: AllergenName) {
        viewModelScope.launch {
            withContext(coroutineDispatchers.io()) {
                productRepository.setAllergenEnabled(allergen.allergenTag, true).await()
                matomoAnalytics.trackEvent(AnalyticsEvent.AllergenAlertCreated(allergen.allergenTag))
            }
            refreshAllergens()
        }
    }

    fun removeAllergen(allergen: AllergenName) {
        viewModelScope.launch {
            withContext(coroutineDispatchers.io()) {
                productRepository.setAllergenEnabled(allergen.allergenTag, false).await()
            }
            refreshAllergens()
        }
    }

    private suspend fun refreshAllergens() {
        val enabledAllergens = withContext(coroutineDispatchers.io()) {
            productRepository.getAllergensByEnabledAndLanguageCode(true, localeManager.getLanguage())
                .await()
                .sortedBy { it.name }
        }
        _viewStateFlow.emit(_viewStateFlow.value.copy(allergens = enabledAllergens))
    }

    private suspend fun downloadAllergensFromServer() {
        _viewStateFlow.emit(_viewStateFlow.value.copy(loading = true))
        val result = withContext(coroutineDispatchers.io()) {
            runCatching {
                productRepository.getAllergens()
            }
        }
        _viewStateFlow.emit(_viewStateFlow.value.copy(loading = false))
        result.fold(
            onSuccess = {
                allergenPreferencesRepository.setAllergenFetchResult(failure = false)
                addAllergenClicked() // retry add action
            },
            onFailure = {
                allergenPreferencesRepository.setAllergenFetchResult(failure = true)
                _sideEffectFlow.emit(SideEffect.ShowNetworkErrorDialog)
            }
        )
    }

    private suspend fun getNotEnabledAllergens() = withContext(coroutineDispatchers.io()) {
        productRepository.getAllergensByEnabledAndLanguageCode(false, localeManager.getLanguage())
            .await()
            .filter { it.allergenTag != "en:none" }
            .sortedBy { it.name }
    }

    private suspend fun isDatabaseEmpty() = withContext(coroutineDispatchers.io()) {
        productRepository.getAllergensByLanguageCode(localeManager.getLanguage()).isEmpty()
    }

    data class ViewState(
        val loading: Boolean,
        val allergens: List<AllergenName>
    )

    sealed class SideEffect {
        data class ShowAddAllergenDialog(val items: List<AllergenName>) : SideEffect()
        object ShowNoDataDialog : SideEffect()
        object ShowNetworkErrorDialog : SideEffect()
    }
}
