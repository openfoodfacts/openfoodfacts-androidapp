package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.pm.PackageManager
import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.Product
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.*
import org.mockito.Mockito.`when` as mockitoWhen

class UtilsTest {
    @Test
    fun testGetImageGrade() {
        val mockProduct = mock(Product::class.java)

        mockitoWhen(mockProduct.nutritionGradeFr).thenReturn("a")
        assertThat(mockProduct.getNutriScoreResource()).isEqualTo(R.drawable.ic_nutriscore_a)

        mockitoWhen(mockProduct.nutritionGradeFr).thenReturn("b")
        assertThat(mockProduct.getNutriScoreResource()).isEqualTo(R.drawable.ic_nutriscore_b)

        mockitoWhen(mockProduct.nutritionGradeFr).thenReturn("c")
        assertThat(mockProduct.getNutriScoreResource()).isEqualTo(R.drawable.ic_nutriscore_c)

        mockitoWhen(mockProduct.nutritionGradeFr).thenReturn("d")
        assertThat(mockProduct.getNutriScoreResource()).isEqualTo(R.drawable.ic_nutriscore_d)

        mockitoWhen(mockProduct.nutritionGradeFr).thenReturn("e")
        assertThat(mockProduct.getNutriScoreResource()).isEqualTo(R.drawable.ic_nutriscore_e)

        mockitoWhen(mockProduct.nutritionGradeFr).thenReturn("")
        assertThat(mockProduct.getNutriScoreResource()).isEqualTo(R.drawable.ic_nutriscore_unknown)

        mockitoWhen(mockProduct.nutritionGradeFr).thenReturn(null)
        assertThat(mockProduct.getNutriScoreResource()).isEqualTo(R.drawable.ic_nutriscore_unknown)
    }

    @Test
    fun getRoundNumber() {
        assertThat(getRoundNumber("")).isEqualTo("?")
        assertThat(getRoundNumber("test")).isEqualTo("?")
        assertThat(getRoundNumber("0")).isEqualTo("0")
        assertThat(getRoundNumber(0.0f)).isEqualTo("0")
        assertThat(getRoundNumber(1.00f)).isEqualTo("1")
        assertThat(getRoundNumber(1.70f, Locale.ENGLISH)).isEqualTo("1.7")
        assertThat(getRoundNumber(1.75f, Locale.ENGLISH)).isEqualTo("1.75")
        assertThat(getRoundNumber(1.754f, Locale.ENGLISH)).isEqualTo("1.75")
        assertThat(getRoundNumber(1.756f, Locale.ENGLISH)).isEqualTo("1.76")
        assertThat(getRoundNumber(1.756f, Locale.ENGLISH)).isEqualTo("1.76")
    }

    @Test
    fun isHardwareCameraInstalled() {
        val mockCtx = mock(Context::class.java)
        val mockPM = mock(PackageManager::class.java)

        mockitoWhen(mockCtx.packageManager).thenReturn(mockPM)

        // Test for best condition
        mockitoWhen(mockPM.hasSystemFeature(PackageManager.FEATURE_CAMERA))
            .thenReturn(true)
        assertThat(isHardwareCameraInstalled(mockCtx)).isTrue()

        // False condition
        mockitoWhen(mockPM.hasSystemFeature(PackageManager.FEATURE_CAMERA))
            .thenReturn(false)

        assertThat(isHardwareCameraInstalled(mockCtx)).isFalse()
    }

}