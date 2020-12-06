package openfoodfacts.github.scrachx.openfood.utils

import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import openfoodfacts.github.scrachx.openfood.features.PreferencesFragment

fun <T : Preference?> PreferencesFragment.requirePreference(key: String): T {
    return findPreference(key) ?: throw IllegalStateException("$key preference does not exist.")
}

fun <T : Preference?> PreferenceScreen.requirePreference(key: String): T {
    return findPreference(key) ?: throw IllegalStateException("$key preference does not exist.")
}