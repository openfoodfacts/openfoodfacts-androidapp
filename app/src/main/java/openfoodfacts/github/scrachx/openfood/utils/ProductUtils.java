package openfoodfacts.github.scrachx.openfood.utils;

import androidx.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;

import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.Units;
import openfoodfacts.github.scrachx.openfood.network.ApiFields;

public class ProductUtils {
    private ProductUtils() {

    }

    public static boolean isPerServingInLiter(Product product) {
        return StringUtils.containsIgnoreCase(product.getServingSize(), Units.UNIT_LITER);
    }

    /**
     * @param barcode
     * @return true if valid according to {@link EAN13CheckDigit#EAN13_CHECK_DIGIT}
     *     and if the barcode doesn't start will 977/978/979 (Book barcode)
     */
    public static boolean isBarcodeValid(@Nullable String barcode) {
        // For debug only: the barcode '1' is used for test:
        if (ApiFields.Defaults.DEBUG_BARCODE.equals(barcode)) {
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
