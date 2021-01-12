package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.LanguageData
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLocale
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLocaleFromContext
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.*
import org.mockito.Mockito.`when` as mockitoWhen

/**
 * Created by n27 on 4/4/17.
 */
class LocaleHelperTest {

    @Test
    fun getLocale() {
        assertThat(getLocale("fr")).isEqualTo(Locale.FRENCH)
        assertThat(getLocale("en-US")).isEqualTo(Locale.US)
        assertThat(getLocale("en")).isEqualTo(Locale.ENGLISH)
    }

    @Test
    fun getLocale_FromContext() {
        val context = mock(Context::class.java)
        val resources = mock(Resources::class.java)
        mockitoWhen(context.resources).thenReturn(resources)

        val locale = getLocale("en-US")
        val configuration = mock(Configuration::class.java).apply {
            this.locale = locale
        }
        mockitoWhen(resources.configuration).thenReturn(configuration)

        assertThat(getLocaleFromContext(context)).isEqualTo(locale)
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