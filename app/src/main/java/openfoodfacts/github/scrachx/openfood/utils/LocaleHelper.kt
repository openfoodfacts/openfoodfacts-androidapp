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
import androidx.preference.PreferenceManager
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import org.apache.commons.lang.StringUtils
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
    @JvmStatic
    fun find(availableLanguages: List<LanguageData?>, language: String?): Int {
        if (language != null) {
            availableLanguages.filterNotNull().withIndex().forEach { (i, availableLanguage) ->
                if (language == availableLanguage.code) {
                    return i
                }
            }
        }
        return -1
    }

    /**
     * Used by screenshots generator.
     *
     * @param context
     * @param locale
     */
    @JvmStatic
    fun setLocale(context: Context, locale: Locale?): Context {
        if (locale == null) return context
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

    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    const val USER_COUNTRY_PREFERENCE_KEY = "user_country"
    fun onCreate(context: Context): Context {
        val lang = getLanguageInPreferences(context, Locale.getDefault().language)
        return setLocale(context, lang)
    }

    fun onCreate(context: Context, defaultLanguage: String): Context {
        val lang = getLanguageInPreferences(context, defaultLanguage)
        return setLocale(context, lang)
    }

    @JvmStatic
    fun getLanguageData(codes: Collection<String?>?, supported: Boolean): MutableList<LanguageData?> {
        val res = arrayListOf<LanguageData>()
        codes?.forEach {
            val languageData = getLanguageData(it, supported)
            if (languageData != null) {
                res.add(languageData)
            }
        }
        res.sort()
        return res.toMutableList()
    }

    @JvmStatic
    fun getLanguageData(code: String?, supported: Boolean): LanguageData? {
        val locale = getLocale(code)
        return LanguageData(locale.language, StringUtils.capitalize(locale.getDisplayName(locale)), supported)
    }

    /**
     * Used by screenshots test
     */
    @JvmStatic
    fun setLocale(locale: Locale?): Context {
        return setLocale(OFFApplication.instance, locale)
    }

    @JvmStatic
    fun getLanguage(context: Context?): String {
        var lang = getLanguageInPreferences(context, Locale.getDefault().language)
        if (lang.contains("-")) {
            lang = lang.split("-").toTypedArray()[0]
        }
        return lang
    }

    @JvmStatic
    fun getLCOrDefault(languageCode: String?) =
            if (!languageCode.isNullOrEmpty()) languageCode else ApiFields.Defaults.DEFAULT_LANGUAGE

    fun getLocale(context: Context? = OFFApplication.instance): Locale {
        var locale: Locale? = null
        if (context != null) {
            val resources = context.resources
            if (resources != null) {
                val configuration = resources.configuration
                if (configuration != null) {
                    locale = configuration.locale
                }
            }
        }
        return locale ?: Locale.getDefault()
    }

    @JvmStatic
    fun setLocale(context: Context, language: String?): Context {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(SELECTED_LANGUAGE, language)
            apply()
        }
        return setLocale(context, getLocale(language))
    }

    /**
     * Extract language and region from the locale string
     *
     * @param locale language
     * @return Locale from locale string
     */
    @JvmStatic
    fun getLocale(locale: String?): Locale {
        if (locale == null) {
            return Locale.getDefault()
        }
        var localeParts = locale.split("-").toTypedArray()
        var language = localeParts[0]
        val country = if (localeParts.size == 2) localeParts[1] else ""
        if (!locale.contains("+")) {
            return Locale(language, country)
        }
        localeParts = locale.split("+").toTypedArray()
        language = localeParts[1]
        val script = localeParts[2]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Locale.Builder().setLanguage(language).setRegion(country).setScript(script).build()
        } else {
            Locale.getAvailableLocales().forEach { checkLocale ->
                if (checkLocale.isO3Language == language && checkLocale.country == country && checkLocale.variant == "") {
                    return checkLocale
                }
            }
            return Locale.getDefault()
        }
    }

    private fun getLanguageInPreferences(context: Context?, defaultLanguage: String): String {
        if (context == null) {
            return defaultLanguage
        }
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage) ?: defaultLanguage
    }

    class LanguageData internal constructor(
            val code: String,
            val name: String,
            val isSupported: Boolean
    ) : Comparable<LanguageData> {
        override fun toString(): String {
            return "$name [$code]"
        }

        override fun equals(other: Any?): Boolean {
            if (other == null) {
                return false
            }
            return if (LanguageData::class.java == other.javaClass) {
                code == (other as LanguageData).code
            } else false
        }

        override fun hashCode(): Int {
            return code.hashCode()
        }

        override fun compareTo(other: LanguageData): Int {
            return name.compareTo(other.name)
        }
    }
}