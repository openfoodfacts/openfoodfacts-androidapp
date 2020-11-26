package openfoodfacts.github.scrachx.openfood.utils

import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import openfoodfacts.github.scrachx.openfood.features.PreferencesFragment

object PreferencesUtils {
    @JvmStatic
    fun <T : Preference?> requirePreference(preferencesFragment: PreferencesFragment, key: String): T {
        return preferencesFragment.findPreference(key) ?: throw IllegalStateException("$key preference does not exist.")
    }

    @JvmStatic
    fun <T : Preference?> requirePreference(screen: PreferenceScreen, key: String): T {
        return screen.findPreference(key) ?: throw IllegalStateException("$key preference does not exist.")
    }
}