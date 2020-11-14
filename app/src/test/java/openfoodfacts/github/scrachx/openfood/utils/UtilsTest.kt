package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.pm.PackageManager
import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.Product
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*

class UtilsTest {
    @Test
    fun getImageGrade() {
        val mockProduct = mock(Product::class.java)

        `when`(mockProduct.nutritionGradeFr).thenReturn("a")
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(R.drawable.ic_nutriscore_a)

        `when`(mockProduct.nutritionGradeFr).thenReturn("b")
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(R.drawable.ic_nutriscore_b)

        `when`(mockProduct.nutritionGradeFr).thenReturn("c")
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(R.drawable.ic_nutriscore_c)

        `when`(mockProduct.nutritionGradeFr).thenReturn("d")
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(R.drawable.ic_nutriscore_d)

        `when`(mockProduct.nutritionGradeFr).thenReturn("e")
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(R.drawable.ic_nutriscore_e)

        `when`(mockProduct.nutritionGradeFr).thenReturn("")
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(Utils.NO_DRAWABLE_RESOURCE)

        `when`(mockProduct.nutritionGradeFr).thenReturn(null)
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(Utils.NO_DRAWABLE_RESOURCE)
    }

    @Test
    fun getRoundNumber() {
        /* TODO: Fix method before testing
        assertThat(Utils.getRoundNumber("")).isEqualTo("?");
        assertThat(Utils.getRoundNumber(null)).isEqualTo("?");
        // TODO: Something for string
        // assertThat(Utils.getRoundNumber("test")).isEqualTo("?");
        assertThat(Utils.getRoundNumber("0")).isEqualTo("0");
        assertThat(Utils.getRoundNumber(0)).isEqualTo("0");
        assertThat(Utils.getRoundNumber(1)).isEqualTo("1");
        assertThat(Utils.getRoundNumber(1.7f)).isEqualTo("1.7");
        assertThat(Utils.getRoundNumber(1.75f)).isEqualTo("1.75");
        assertThat(Utils.getRoundNumber(1.754f)).isEqualTo("1.75");
        assertThat(Utils.getRoundNumber(1.756f)).isEqualTo("1.76");
        assertThat(Utils.getRoundNumber(1.756f)).isEqualTo("1.76");

         */
    }

    @Test
    fun isHardwareCameraInstalled() {
        val mockCtx = mock(Context::class.java)
        val mockPM = mock(PackageManager::class.java)

        `when`(mockCtx.packageManager).thenReturn(mockPM)

        // Test for best condition
        `when`(mockPM.hasSystemFeature(PackageManager.FEATURE_CAMERA))
                .thenReturn(true)
        assertThat(Utils.isHardwareCameraInstalled(mockCtx)).isTrue()

        // False condition
        `when`(mockPM.hasSystemFeature(PackageManager.FEATURE_CAMERA))
                .thenReturn(false)

        assertThat(Utils.isHardwareCameraInstalled(mockCtx)).isFalse()
    }

    @Test
    fun getServingInOz_from_ml() {
        assertThat(UnitUtils.getServingInOz("100 ml"))
                .isEqualTo(String.format(Locale.getDefault(), "%.2f", 3.38) + " oz")
    }

    @Test
    fun getServingInOz_from_cl() {
        assertThat(UnitUtils.getServingInOz("250 cl"))
                .isEqualTo(String.format(Locale.getDefault(), "%.2f", 84.53) + " oz")
    }

    @Test
    fun getServingInOz_from_l() {
        assertThat(UnitUtils.getServingInOz("3 l"))
                .isEqualTo(String.format(Locale.getDefault(), "%.2f", 101.44) + " oz")
    }
}