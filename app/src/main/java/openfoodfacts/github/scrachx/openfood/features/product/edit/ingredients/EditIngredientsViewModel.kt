package openfoodfacts.github.scrachx.openfood.features.product.edit.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenNameDao
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.list
import javax.inject.Inject

@HiltViewModel
class EditIngredientsViewModel @Inject constructor(
    private val daoSession: DaoSession,
    private val localeManager: LocaleManager
) : ViewModel() {

    val allergens = liveData {
        daoSession.allergenNameDao.list {
            where(AllergenNameDao.Properties.LanguageCode.eq(localeManager.getLanguage()))
            orderDesc(AllergenNameDao.Properties.Name)
        }?.map { it.name }.let { emit(it) }
    }

}