package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import openfoodfacts.github.scrachx.openfood.features.preferences.PreferencesFragment

fun <T : Preference?> PreferencesFragment.requirePreference(key: String): T =
    findPreference(key) ?: error("$key preference does not exist.")

fun <T : Preference?> PreferenceScreen.requirePreference(key: String): T =
    findPreference(key) ?: error("$key preference does not exist.")

inline fun <T : Preference?> PreferencesFragment.requirePreference(key: String, action: T.() -> Unit) {
    requirePreference<T>(key).run(action)
}

fun Context.getLoginPreferences(mode: Int = Context.MODE_PRIVATE): SharedPreferences =
    getSharedPreferences(PreferencesFragment.LOGIN_SHARED_PREF, mode)

fun Context.getLoginUsername(defValue: String? = null) =
    getLoginPreferences().getString("user", defValue)

fun Context.getLoginPassword(defValue: String? = null) =
    getLoginPreferences().getString("pass", defValue)

fun Context.setLogin(username: String, password: String) {
    getLoginPreferences().edit {
        putString("user", username)
        putString("pass", password)
    }
}

fun Context.getAppPreferences(mode: Int = Context.MODE_PRIVATE): SharedPreferences =
    getSharedPreferences(PreferencesFragment.APP_SHARED_PREF, mode)

fun Context.isUserSet() = !getLoginUsername().isNullOrBlank()

fun Context.getUserSession() = getLoginPreferences().getString("user_session", null)
