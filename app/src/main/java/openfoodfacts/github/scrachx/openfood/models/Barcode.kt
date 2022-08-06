package openfoodfacts.github.scrachx.openfood.models

import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit


@JvmInline
value class Barcode(val raw: String) {
    /**
     * @return true if valid according to [EAN13CheckDigit.EAN13_CHECK_DIGIT]
     * and if the barcode doesn't start with 977/978/979 (Book barcode)
     */
    fun isValid(): Boolean {
        return when {
            // DEBUG ONLY: the barcode '1' is used for test:
            BuildConfig.DEBUG && raw == ApiFields.Defaults.DEBUG_BARCODE -> true

            raw.length <= 3 || !EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(raw) -> false

            // It must not start with these prefixes
            else -> raw.take(3) !in listOf("977", "978", "979")
        }
    }

    fun isEmpty(): Boolean = raw.isEmpty()
}