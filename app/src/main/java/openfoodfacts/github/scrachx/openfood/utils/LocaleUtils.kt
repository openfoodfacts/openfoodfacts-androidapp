package openfoodfacts.github.scrachx.openfood.utils

import android.os.Build
import openfoodfacts.github.scrachx.openfood.models.LanguageData
import java.util.*

object LocaleUtils {

    /**
     * Extract language and region from the locale string
     *
     * @param locale language
     * @return Locale from locale string
     */
    fun parseLocale(locale: String): Locale {
        var localeParts = locale.split("-").toTypedArray()
        var language = localeParts[0]
        val country = if (localeParts.size == 2) localeParts[1] else ""

        return if (!locale.contains("+")) {
            Locale(language, country)
        } else {
            localeParts = locale.split("+").toTypedArray()
            language = localeParts[1]
            val script = localeParts[2]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Locale.Builder().setLanguage(language).setRegion(country).setScript(script).build()
            } else {
                Locale.getAvailableLocales().firstOrNull {
                    it.isO3Language == language && it.country == country && it.variant == ""
                } ?: Locale.getDefault()
            }
        }
    }

    fun getLanguageData(codes: Collection<String>, supported: Boolean): List<LanguageData> = codes
            .map { getLanguageData(it, supported) }
            .sorted()

    fun getLanguageData(code: String, supported: Boolean): LanguageData {
        val locale = parseLocale(code)
        return LanguageData(locale.language,
            locale.getDisplayName(locale).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, supported
        )
    }
}
