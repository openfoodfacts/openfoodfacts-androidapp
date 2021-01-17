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
import android.os.Build
import androidx.core.content.edit
import androidx.core.os.ConfigurationCompat
import androidx.preference.PreferenceManager
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import java.util.*

/**
 * This class is used to change your application locale and persist this change for the next time
 * that your app is going to be used.
 *
 *
 * You can also change the locale of your application on the fly by using the setLocale method.
 *
 *
 * Created by gunhansancar on 07/10/15.
 */
object LocaleHelper {
    private val SELECTED_LANGUAGE by lazy { OFFApplication.instance.getString(R.string.pref_language_key) }

    fun List<LanguageData>.find(language: String) = indexOfFirst { language == it.code }

    fun onCreate(context: Context, defaultLanguage: String = Locale.getDefault().language) =
            setLanguageInPrefs(context, getLanguageInPreferences(context, defaultLanguage))

    fun getLanguageData(codes: Collection<String?>, supported: Boolean) =
            codes.map { getLanguageData(it, supported) }.sorted().toMutableList()

    fun getLanguageData(code: String?, supported: Boolean) =
            getLocale(code).let { LanguageData(it.language, it.getDisplayName(it).capitalize(Locale.ROOT), supported) }

    /**
     * Used by screenshots test
     */
    internal fun setLanguageInPrefs(locale: Locale) = setContextLanguage(OFFApplication.instance, locale)

    fun getLanguage(context: Context?): String {
        var lang = getLanguageInPreferences(context, Locale.getDefault().language)
        if (lang.contains("-")) {
            lang = lang.split("-")[0]
        }
        return lang
    }

    fun getLCOrDefault(languageCode: String?) =
            if (!languageCode.isNullOrEmpty()) languageCode else ApiFields.Defaults.DEFAULT_LANGUAGE

    fun getLocaleFromContext(context: Context? = OFFApplication.instance): Locale {
        var locale: Locale? = null
        if (context != null) {
            val resources = context.resources
            if (resources != null) {
                val configuration = resources.configuration
                if (configuration != null) {
                    locale = ConfigurationCompat.getLocales(configuration)[0]
                }
            }
        }
        return locale ?: Locale.getDefault()
    }

    /**
     * Used by screenshots generator.
     *
     * @param context
     * @param locale
     */
    fun setContextLanguage(context: Context, locale: Locale): Context {
        var newContext = context
        PreferenceManager.getDefaultSharedPreferences(newContext).edit {
            putString(SELECTED_LANGUAGE, locale.language)
        }
        Locale.setDefault(locale)
        val resources = newContext.resources
        val configuration = resources.configuration
        if (Build.VERSION.SDK_INT >= 17) {
            configuration.setLocale(locale)
            newContext = newContext.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return newContext
    }

    fun setLanguageInPrefs(context: Context, language: String?): Context {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(SELECTED_LANGUAGE, language)
            apply()
        }
        return setContextLanguage(context, getLocale(language))
    }

    /**
     * Extract language and region from the locale string
     *
     * @param locale language
     * @return Locale from locale string
     */
    fun getLocale(locale: String?): Locale {
        if (locale == null) return Locale.getDefault()

        var localeParts = locale.split("-").toTypedArray()
        var language = localeParts[0]
        val country = if (localeParts.size == 2) localeParts[1] else ""
        if (!locale.contains("+")) return Locale(language, country)

        localeParts = locale.split("+").toTypedArray()
        language = localeParts[1]
        val script = localeParts[2]
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Locale.Builder().setLanguage(language).setRegion(country).setScript(script).build()
        } else {
            Locale.getAvailableLocales().firstOrNull {
                it.isO3Language == language && it.country == country && it.variant == ""
            } ?: Locale.getDefault()
        }
    }

    private fun getLanguageInPreferences(context: Context?, defaultLanguage: String) = context?.let {
        PreferenceManager.getDefaultSharedPreferences(it).getString(SELECTED_LANGUAGE, defaultLanguage)
    } ?: defaultLanguage

    class LanguageData internal constructor(
            val code: String,
            val name: String,
            val isSupported: Boolean
    ) : Comparable<LanguageData> {
        override fun toString() = "$name [$code]"

        override fun equals(other: Any?) = other is LanguageData && this.code == other.code

        override fun hashCode() = code.hashCode()

        override fun compareTo(other: LanguageData) = name.compareTo(other.name)
    }
}