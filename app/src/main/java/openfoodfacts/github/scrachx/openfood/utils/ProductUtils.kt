package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import io.reactivex.Single
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.models.Units
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit

fun OfflineSavedProduct.toState(context: Context): Single<ProductState> {
    return OpenFoodAPIClient(context).getProductStateFull(barcode)
}

fun OfflineSavedProduct.toOnlineProduct(context: Context): Single<Product> {
    return toState(context).map { obj: ProductState -> obj.product }
}

/**
 * @param barcode
 * @return true if valid according to [EAN13CheckDigit.EAN13_CHECK_DIGIT]
 * and if the barcode doesn't start will 977/978/979 (Book barcode)
 */
fun isBarcodeValid(barcode: String?): Boolean {
    // For debug only: the barcode '1' is used for test:
    if (ApiFields.Defaults.DEBUG_BARCODE == barcode) {
        return true
    }
    return if (barcode == null) {
        false
    } else EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(barcode) && barcode.length > 3 &&
            (!barcode.substring(0, 3).contains("977") ||
                    !barcode.substring(0, 3).contains("978") ||
                    !barcode.substring(0, 3).contains("979"))
}

fun Product.isPerServingInLiter() = servingSize?.contains(Units.UNIT_LITER, true)