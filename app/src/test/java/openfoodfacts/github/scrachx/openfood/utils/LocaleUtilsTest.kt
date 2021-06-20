package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.LanguageData
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.*
import org.mockito.Mockito.`when` as mockitoWhen

/**
 * Created by n27 on 4/4/17.
 */
class LocaleUtilsTest {

    @Test
    fun getLocale() {
        assertThat(LocaleUtils.parseLocale("fr")).isEqualTo(Locale.FRENCH)
        assertThat(LocaleUtils.parseLocale("en-US")).isEqualTo(Locale.US)
        assertThat(LocaleUtils.parseLocale("en")).isEqualTo(Locale.ENGLISH)
    }

    @Test
    fun getLocale_FromContext() {
        val context = mock(Context::class.java)
        val resources = mock(Resources::class.java)
        val sharedPreferences = mock(SharedPreferences::class.java)
        val localeManager = LocaleManager(context, sharedPreferences)
        mockitoWhen(context.resources).thenReturn(resources)

        val locale = LocaleUtils.parseLocale("en-US")
        val configuration = mock(Configuration::class.java).apply {
            this.locale = locale
        }
        mockitoWhen(resources.configuration).thenReturn(configuration)

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
