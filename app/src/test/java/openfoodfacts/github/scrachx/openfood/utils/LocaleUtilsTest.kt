package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import openfoodfacts.github.scrachx.openfood.models.LanguageData
import openfoodfacts.github.scrachx.openfood.utils.LocaleUtils.parseLocale
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Created by n27 on 4/4/17.
 */
class LocaleUtilsTest {

    @Test
    fun getLocale() {
        assertThat(parseLocale("fr")).isEqualTo(Locale.FRENCH)
        assertThat(parseLocale("en-US")).isEqualTo(Locale.US)
        assertThat(parseLocale("en")).isEqualTo(Locale.ENGLISH)
    }

    @Test
    fun getLocale_FromContext() {
        val locale = parseLocale("en-US")
        val configuration = Configuration().apply { this.locale = locale }
        val resources = mockk<Resources> {
            every { this@mockk.configuration } returns configuration
        }
        val context = mockk<Context> {
            every { this@mockk.resources } returns resources
            every { getString(any()) } returns ""
        }
        val sharedPreferences = mockk<SharedPreferences>(relaxed = true)
        val localeManager = LocaleManager(context, sharedPreferences)

        assertThat(localeManager.getLocaleFromContext(context)).isEqualTo(locale)
    }

    @Test
    fun languageDataProperties() {
        val code = "en"
        val langData1 = LanguageData(code, "test1", false)
        val langData2 = LanguageData(code, "test2", true)

        assertThat(langData1).isEqualTo(langData2)
        assertThat(langData1.hashCode()).isEqualTo(code.hashCode())
        assertThat(langData1.toString()).isEqualTo("test1 [en]")
    }
}
