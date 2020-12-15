package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.SharedPreferences
import openfoodfacts.github.scrachx.openfood.features.PreferencesFragment

fun Context.isUserSet() = !getLoginPreferences().getString("user", null).isNullOrBlank()

fun Context.getLoginPreferences(mode: Int = 0): SharedPreferences = this.getSharedPreferences(PreferencesFragment.LOGIN_PREF, mode)