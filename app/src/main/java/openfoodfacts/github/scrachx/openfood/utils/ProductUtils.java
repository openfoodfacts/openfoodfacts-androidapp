package openfoodfacts.github.scrachx.openfood.utils;

import openfoodfacts.github.scrachx.openfood.models.Product;
import org.apache.commons.lang3.StringUtils;

public class ProductUtils {
    public static final String DEFAULT_NUTRITION_SIZE = "100g";

    private ProductUtils(){

    }

    public static boolean isPerServingInLiter(Product product) {
        return StringUtils.containsIgnoreCase(product.getServingSize(), UnitUtils.UNIT_LITER);
    }
}
