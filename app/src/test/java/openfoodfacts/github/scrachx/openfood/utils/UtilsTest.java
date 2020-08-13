package openfoodfacts.github.scrachx.openfood.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import org.junit.Test;

import java.util.Locale;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UtilsTest {
    @Test
    public void getImageGrade() {
        final Product mockProduct = mock(Product.class);

        when(mockProduct.getNutritionGradeFr()).thenReturn("a");
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(R.drawable.ic_nutriscore_a);

        when(mockProduct.getNutritionGradeFr()).thenReturn("b");
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(R.drawable.ic_nutriscore_b);

        when(mockProduct.getNutritionGradeFr()).thenReturn("c");
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(R.drawable.ic_nutriscore_c);

        when(mockProduct.getNutritionGradeFr()).thenReturn("d");
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(R.drawable.ic_nutriscore_d);

        when(mockProduct.getNutritionGradeFr()).thenReturn("e");
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(R.drawable.ic_nutriscore_e);

        when(mockProduct.getNutritionGradeFr()).thenReturn("");
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(Utils.NO_DRAWABLE_RESOURCE);

        when(mockProduct.getNutritionGradeFr()).thenReturn(null);
        assertThat(Utils.getImageGrade(mockProduct)).isEqualTo(Utils.NO_DRAWABLE_RESOURCE);
    }

    @Test
    public void getRoundNumber() {
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
    public void isHardwareCameraInstalled() {
        final Context mockCtx = mock(Context.class);
        final PackageManager mockPM = mock(PackageManager.class);

        when(mockCtx.getPackageManager()).thenReturn(mockPM);

        // Test for best condition
        when(mockPM.hasSystemFeature(PackageManager.FEATURE_CAMERA))
            .thenReturn(true);
        assertThat(Utils.isHardwareCameraInstalled(mockCtx)).isTrue();

        // False condition
        when(mockPM.hasSystemFeature(PackageManager.FEATURE_CAMERA))
            .thenReturn(false);

        assertThat(Utils.isHardwareCameraInstalled(mockCtx)).isFalse();
    }

    @Test
    public void getServingInOz_from_ml() {

        assertThat(UnitUtils.getServingInOz("100 ml"))
            .isEqualTo(String.format(Locale.getDefault(), "%.2f", 3.38) + " oz");
    }

    @Test
    public void getServingInOz_from_cl() {
        assertThat(UnitUtils.getServingInOz("250 cl"))
            .isEqualTo(String.format(Locale.getDefault(), "%.2f", 84.53) + " oz");
    }

    @Test
    public void getServingInOz_from_l() {

        assertThat(UnitUtils.getServingInOz("3 l"))
            .isEqualTo(String.format(Locale.getDefault(), "%.2f", 101.44) + " oz");
    }
}