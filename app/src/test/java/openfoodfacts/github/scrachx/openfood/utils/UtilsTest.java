package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {
    String servingSize;

    @Test
    public void getServingInOz_from_ml(){
        servingSize = "100 ml";

        assertEquals("3.38 oz", Utils.getServingInOz(servingSize));
    }

    @Test
    public void getServingInOz_from_cl(){
        servingSize = "250 cl";

        assertEquals("84.54 oz", Utils.getServingInOz(servingSize));
    }

    @Test
    public void getServingInOz_from_l(){
        servingSize = "3 l";

        assertEquals("101.44 oz", Utils.getServingInOz(servingSize));
    }
}