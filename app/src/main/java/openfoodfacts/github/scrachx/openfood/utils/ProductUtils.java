package openfoodfacts.github.scrachx.openfood.utils;

import androidx.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;

import openfoodfacts.github.scrachx.openfood.models.Product;

public class ProductUtils {
    public static final String DEFAULT_NUTRITION_SIZE = "100g";
    public static final String DEBUG_BARCODE = "1";

    private ProductUtils() {

    }

    public static boolean isPerServingInLiter(Product product) {
        return StringUtils.containsIgnoreCase(product.getServingSize(), UnitUtils.UNIT_LITER);
    }

    /**
     * @param barcode
     * @return true if valid according to {@link EAN13CheckDigit#EAN13_CHECK_DIGIT}
     *     and if the barcode doesn't start will 977/978/979 (Book barcode)
     */
    public static boolean isBarcodeValid(@Nullable String barcode) {
        // For debug only: the barcode '1' is used for test:
        if (DEBUG_BARCODE.equals(barcode)) {
            return true;
        }
        if (barcode == null) {
            return false;
        }
        return (EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(barcode) && barcode.length() > 3) &&
            (!barcode.substring(0, 3).contains("977") ||
                !barcode.substring(0, 3).contains("978") ||
                !barcode.substring(0, 3).contains("979"));
    }
}
