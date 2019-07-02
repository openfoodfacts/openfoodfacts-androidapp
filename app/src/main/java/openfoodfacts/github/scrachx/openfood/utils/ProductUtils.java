package openfoodfacts.github.scrachx.openfood.utils;

import androidx.annotation.Nullable;
import openfoodfacts.github.scrachx.openfood.models.Product;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;

public class ProductUtils {
    public static final String DEFAULT_NUTRITION_SIZE = "100g";
    public static final String DEBUG_BARCODE = "1";

    private ProductUtils(){

    }

    public static boolean isPerServingInLiter(Product product) {
        return StringUtils.containsIgnoreCase(product.getServingSize(), UnitUtils.UNIT_LITER);
    }

    /**
     *
     * @param barcode
     * @return true if valid according to {@link EAN13CheckDigit#EAN13_CHECK_DIGIT} and if the barecode doesn't start will 977/978/979 (Book barcode)
     */
    public static boolean isBarcodeValid(@Nullable String barcode){
        //for debug only:the barcode 1 is used for test:
        if(DEBUG_BARCODE.equals(barcode)){
            return true;
        }
        return  barcode!=null && (EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(barcode) && (!barcode.substring(0, 3).contains("977") || !barcode.substring(0, 3)
            .contains("978") || !barcode.substring(0, 3).contains("979")));
    }
}
