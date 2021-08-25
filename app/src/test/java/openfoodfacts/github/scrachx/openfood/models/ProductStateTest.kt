package openfoodfacts.github.scrachx.openfood.models

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for [ProductState]
 */
class ProductStateTest {
    @Test
    fun `toString returns properly formatted string`() {
        val productState = ProductState().apply {
            statusVerbose = STATUS_VERBOSE
            status = STATUS
            product = PRODUCT
            code = CODE
            setAdditionalProperty(PROPERTY_KEY_1, PROPERTY_VALUE_1)
            setAdditionalProperty(PROPERTY_KEY_2, PROPERTY_VALUE_2)
        }
        val additionalProperties = mapOf<String, Any>(
            PROPERTY_KEY_1 to PROPERTY_VALUE_1,
            PROPERTY_KEY_2 to PROPERTY_VALUE_2
        )
        val checkString = "ProductState[" +
                "statusVerbose=$STATUS_VERBOSE," +
                "status=$STATUS," +
                "product=$PRODUCT," +
                "code=$CODE," +
                "additionalProperties=$additionalProperties" +
                "]"

        assertThat(productState.toString()).isEqualTo(checkString)
    }

    companion object {
        private const val STATUS_VERBOSE = "verbose status"
        private const val STATUS = 45L
        private val PRODUCT = Product().apply { code = CODE }
        private const val CODE = "code"
        private const val PROPERTY_KEY_1 = "prop_key_1"
        private const val PROPERTY_VALUE_1 = "value_1"
        private const val PROPERTY_KEY_2 = "prop_key_2"
        private const val PROPERTY_VALUE_2 = "value_2"
    }
}