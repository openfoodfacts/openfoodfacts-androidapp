package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import openfoodfacts.github.scrachx.openfood.utils.getAppPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AllergenPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        private const val ERROR_ALLERGENS_KEY = "errorAllergens"
    }

    private val appPrefs by lazy { context.getAppPreferences() }

    fun setAllergenFetchResult(failure: Boolean) {
        appPrefs.edit { putBoolean(ERROR_ALLERGENS_KEY, failure) }
    }
}
