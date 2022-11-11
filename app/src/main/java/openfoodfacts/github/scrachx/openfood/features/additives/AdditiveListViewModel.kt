package openfoodfacts.github.scrachx.openfood.features.additives

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveNameDao
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.list
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AdditiveListViewModel @Inject constructor(
    private val daoSession: DaoSession,
    private val localeManager: LocaleManager,
) : ViewModel() {


    val additives = flow {
        val additiveNameDao = daoSession.additiveNameDao
        val languageCode = localeManager.getLanguage()

        val additives = additiveNameDao.list {
            where(AdditiveNameDao.Properties.LanguageCode.eq(languageCode))
            where(AdditiveNameDao.Properties.Name.like("E%"))
        }

        val sortedAdditives = additives.sortedBy {
            it.name.lowercase(Locale.ROOT)
                .replace('x', '0')
                .split(Regex("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)"))
                .toTypedArray()[1]
                .toInt()
        }

        emit(sortedAdditives)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )


}