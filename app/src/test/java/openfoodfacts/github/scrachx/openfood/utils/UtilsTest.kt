package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.pm.PackageManager
import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.Product
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

class UtilsTest {
    @Test
    fun testGetImageGrade() {
        val mockProduct = mock(Product::class.java)

        whenever(mockProduct.nutritionGradeFr) doReturn "a"
        assertThat(mockProduct.getNutriScoreResource()).isEqualTo(R.drawable.ic_nutriscore_a)

        whenever(mockProduct.nutritionGradeFr) doReturn "b"
        assertThat(mockProduct.getNutriScoreResource()).isEqualTo(R.drawable.ic_nutriscore_b)

        whenever(mockProduct.nutritionGradeFr) doReturn "c"
        assertThat(mockProduct.getNutriScoreResource()).isEqualTo(R.drawable.ic_nutriscore_c)

        whenever(mockProduct.nutritionGradeFr) doReturn "d"
        assertThat(mockProduct.getNutriScoreResource()).isEqualTo(R.drawable.ic_nutriscore_d)

        whenever(mockProduct.nutritionGradeFr) doReturn "e"
        assertThat(mockProduct.getNutriScoreResource()).isEqualTo(R.drawable.ic_nutriscore_e)

        whenever(mockProduct.nutritionGradeFr) doReturn ""
        assertThat(mockProduct.getNutriScoreResource()).isEqualTo(R.drawable.ic_nutriscore_unknown)

        whenever(mockProduct.nutritionGradeFr) doReturn null
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
        val mockPM = mock<PackageManager> {}
        val mockCtx = mock<Context> {
            on { this.packageManager } doReturn mockPM
        }

        // Test for true/false
        whenever(mockPM.hasSystemFeature(PackageManager.FEATURE_CAMERA)) doReturn true
        assertThat(isHardwareCameraInstalled(mockCtx)).isTrue()

        whenever(mockPM.hasSystemFeature(PackageManager.FEATURE_CAMERA)) doReturn false
        assertThat(isHardwareCameraInstalled(mockCtx)).isFalse()
    }

}