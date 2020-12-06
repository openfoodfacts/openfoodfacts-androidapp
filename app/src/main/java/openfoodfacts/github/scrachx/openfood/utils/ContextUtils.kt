package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.SharedPreferences

fun Context.isUserLoggedIn(): Boolean {
    val login = getLoginPreferences()?.getString("user", "")
    return !login.isNullOrBlank()
}

fun Context.getLoginPreferences(): SharedPreferences? = this.getSharedPreferences("login", 0)