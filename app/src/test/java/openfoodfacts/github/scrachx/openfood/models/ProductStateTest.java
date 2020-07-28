package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * Tests for {@link ProductState}
 */
public class ProductStateTest {
    private static final String STATUS_VERBOSE = "verbose status";
    private static final long STATUS = 45L;
    private static final Product PRODUCT = new Product();
    private static final String CODE = "code";
    private static final String PROPERTY_KEY_1 = "prop_key_1";
    private static final String PROPERTY_VALUE_1 = "value_1";
    private static final String PROPERTY_KEY_2 = "prop_key_2";
    private static final String PROPERTY_VALUE_2 = "value_2";

    @Test
    public void toString_returnsProperlyFormattedString() {
        ProductState productState = new ProductState();

        productState.setStatusVerbose(STATUS_VERBOSE);
        productState.setStatus(STATUS);
        productState.setProduct(PRODUCT);
        productState.setCode(CODE);
        productState.setAdditionalProperty(PROPERTY_KEY_1, PROPERTY_VALUE_1);
        productState.setAdditionalProperty(PROPERTY_KEY_2, PROPERTY_VALUE_2);

        Map<String, Object> additionalProperties = new HashMap<>();
        additionalProperties.put(PROPERTY_KEY_1, PROPERTY_VALUE_1);
        additionalProperties.put(PROPERTY_KEY_2, PROPERTY_VALUE_2);

        final String checkString = String
            .format("State{statusVerbose='%s', status=%d, product=%s, code='%s', additionalProperties=%s}",
                STATUS_VERBOSE,
                STATUS,
                PRODUCT,
                CODE,
                additionalProperties);

        assertEquals(checkString, productState.toString());
    }
}
