package openfoodfacts.github.scrachx.openfood.utils;

import openfoodfacts.github.scrachx.openfood.models.Product;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;

public class ProductUtils {
    public static final String DEFAULT_NUTRITION_SIZE = "100g";

    private ProductUtils(){

    }

    public static boolean isPerServingInLiter(Product product) {
        return StringUtils.containsIgnoreCase(product.getServingSize(), UnitUtils.UNIT_LITER);
    }

    public static boolean isBareCodeValid(String barcode){
        return  (EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(barcode) && (!barcode.substring(0, 3).contains("977") || !barcode.substring(0, 3)
            .contains("978") || !barcode.substring(0, 3).contains("979")));
    }
}
