package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLocale
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.*
import org.mockito.Mockito.`when` as mockitoWhen

/**
 * Created by n27 on 4/4/17.
 */
class LocaleHelperTest {
    @Test
    fun getLocale_fr() {
        assertThat(getLocale("fr")).isEqualTo(Locale.FRENCH)
    }

    @Test
    fun getLocale_en() {
        assertThat(getLocale("en")).isEqualTo(Locale.ENGLISH)
    }

    @Test
    fun getLocale_en_US() {
        assertThat(getLocale("en-US")).isEqualTo(Locale.US)
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

        assertThat(getLocale(context)).isEqualTo(locale)
    }
}