package openfoodfacts.github.scrachx.openfood.features.product.edit.overview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.brand.BrandNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.store.StoreNameDao
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.list
import javax.inject.Inject

@HiltViewModel
class EditOverviewViewModel @Inject constructor(
    private val daoSession: DaoSession,
    private val localeManager: LocaleManager
) : ViewModel() {
    private val appLang by lazy { localeManager.getLanguage() }

    internal val suggestCategories = liveData {
        daoSession.categoryNameDao.list {
            where(CategoryNameDao.Properties.LanguageCode.eq(appLang))
            orderDesc(CategoryNameDao.Properties.Name)
        }.mapNotNull { it.name }.let { emit(it) }
    }

    internal val suggestCountries = liveData {
        daoSession.countryNameDao.list {
            where(CountryNameDao.Properties.LanguageCode.eq(appLang))
            orderDesc(CountryNameDao.Properties.Name)
        }.mapNotNull { it.name }.let { emit(it) }
    }

    internal val suggestLabels = liveData {
        daoSession.labelNameDao.list {
            where(LabelNameDao.Properties.LanguageCode.eq(appLang))
            orderDesc(LabelNameDao.Properties.Name)
        }.mapNotNull { it.name }.let { emit(it) }

    }

    internal val suggestStores = liveData {
        daoSession.storeNameDao.list {
            where(StoreNameDao.Properties.LanguageCode.eq(appLang))
            orderDesc(StoreNameDao.Properties.Name).list()
        }.mapNotNull { it.name }.let { emit(it) }
    }

    internal val suggestBrands = liveData {
        daoSession.brandNameDao.list {
            where(BrandNameDao.Properties.LanguageCode.eq(appLang))
            orderDesc(BrandNameDao.Properties.Name)
        }.mapNotNull { it.name }.let { emit(it) }
    }

    internal val product = MutableLiveData<Product>()

}