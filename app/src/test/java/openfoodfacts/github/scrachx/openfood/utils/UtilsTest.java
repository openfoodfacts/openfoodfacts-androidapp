package openfoodfacts.github.scrachx.openfood.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import org.junit.Test;

import java.util.Locale;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UtilsTest {
    @Test
    public void getImageGrade() {
        final Product mockProduct = mock(Product.class);

        when(mockProduct.getNutritionGradeFr()).thenReturn("a");
        assertEquals(R.drawable.ic_nutriscore_a, Utils.getImageGrade(mockProduct));

        when(mockProduct.getNutritionGradeFr()).thenReturn("b");
        assertEquals(R.drawable.ic_nutriscore_b, Utils.getImageGrade(mockProduct));

        when(mockProduct.getNutritionGradeFr()).thenReturn("c");
        assertEquals(R.drawable.ic_nutriscore_c, Utils.getImageGrade(mockProduct));

        when(mockProduct.getNutritionGradeFr()).thenReturn("d");
        assertEquals(R.drawable.ic_nutriscore_d, Utils.getImageGrade(mockProduct));

        when(mockProduct.getNutritionGradeFr()).thenReturn("e");
        assertEquals(R.drawable.ic_nutriscore_e, Utils.getImageGrade(mockProduct));

        when(mockProduct.getNutritionGradeFr()).thenReturn("");
        assertEquals(Utils.NO_DRAWABLE_RESOURCE, Utils.getImageGrade(mockProduct));

        when(mockProduct.getNutritionGradeFr()).thenReturn(null);
        assertEquals(Utils.NO_DRAWABLE_RESOURCE, Utils.getImageGrade(mockProduct));
    }

    @Test
    public void getRoundNumber() {
        /* TODO: Fix method before testing
        assertEquals("?", Utils.getRoundNumber(""));
        assertEquals("?", Utils.getRoundNumber(null));
        // TODO: Something for string
        // assertEquals("?", Utils.getRoundNumber("test"));
        assertEquals("0", Utils.getRoundNumber("0"));
        assertEquals("0", Utils.getRoundNumber(0));
        assertEquals("1", Utils.getRoundNumber(1));
        assertEquals("1.7", Utils.getRoundNumber(1.7f));
        assertEquals("1.75", Utils.getRoundNumber(1.75f));
        assertEquals("1.75", Utils.getRoundNumber(1.754f));
        assertEquals("1.76", Utils.getRoundNumber(1.756f));
        assertEquals("1.76", Utils.getRoundNumber(1.756f));

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
        assertTrue(Utils.isHardwareCameraInstalled(mockCtx));

        // False condition
        when(mockPM.hasSystemFeature(PackageManager.FEATURE_CAMERA))
            .thenReturn(false);

        assertFalse(Utils.isHardwareCameraInstalled(mockCtx));
    }

    @Test
    public void getServingInOz_from_ml() {
        String servingSize = "100 ml";

        assertEquals(String.format(Locale.getDefault(), "%.2f", 3.38) + " oz", UnitUtils.getServingInOz(servingSize));
    }

    @Test
    public void getServingInOz_from_cl() {
        String servingSize = "250 cl";
        assertEquals(String.format(Locale.getDefault(), "%.2f", 84.53) + " oz", UnitUtils.getServingInOz(servingSize));
    }

    @Test
    public void getServingInOz_from_l() {
        String servingSize = "3 l";

        assertEquals(String.format(Locale.getDefault(), "%.2f", 101.44) + " oz", UnitUtils.getServingInOz(servingSize));
    }
}