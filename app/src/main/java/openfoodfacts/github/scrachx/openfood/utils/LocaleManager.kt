/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import androidx.core.os.ConfigurationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import openfoodfacts.github.scrachx.openfood.R
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class is used to change your application locale and persist this change for the next time
 * that your app is going to be used.
 */
@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext context: Context,
    private val sharedPreferences: SharedPreferences
) {

    private val selectedLanguagePrefKey by lazy { context.getString(R.string.pref_language_key) }
    private var currentLocale: Locale

    init {
        currentLocale = getLanguageFromPrefs()?.let {
            LocaleUtils.parseLocale(it)
        } ?: Locale.getDefault()
    }

    fun restoreLocalizedContext(context: Context) = changeAppLanguage(context, currentLocale)

    /**
     * Get the language of the app [Locale] (Selected in settings).
     *
     * Calling this method is a shorthand for:
     * ```
     *      getLocale().getLanguage()
     * ```
     */
    fun getLanguage(): String = currentLocale.language

    fun getLocale(): Locale = currentLocale

    fun saveLanguageToPrefs(context: Context, locale: Locale): Context {
        saveLanguageToPrefs(locale.language)
        return changeAppLanguage(context, locale)
    }

    @Deprecated("Only for UI tests.")
    fun getLocaleFromContext(context: Context?): Locale {
        return context?.resources?.configuration?.let {
            ConfigurationCompat.getLocales(it)[0]
        } ?: currentLocale
    }

    @Suppress("DEPRECATION")
    private fun changeAppLanguage(context: Context, locale: Locale): Context {
        var newContext = context
        Locale.setDefault(locale)
        currentLocale = locale
        val resources = newContext.resources
        val configuration = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(currentLocale)
            newContext = newContext.createConfigurationContext(configuration)
        } else {
            configuration.locale = currentLocale
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        return newContext
    }

    private fun saveLanguageToPrefs(language: String) {
        sharedPreferences.edit {
            putString(selectedLanguagePrefKey, language)
        }
    }

    private fun getLanguageFromPrefs() = sharedPreferences.getString(selectedLanguagePrefKey, null)
}
