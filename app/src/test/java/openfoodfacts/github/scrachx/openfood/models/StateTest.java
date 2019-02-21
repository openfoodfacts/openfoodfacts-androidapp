package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * Tests for {@link State}
 */
public class StateTest {

    private static final String STATUS_VERBOSE = "verbose status";
    private static final long STATUS = 45L;
    private static final Product PRODUCT = new Product();
    private static final String CODE = "code";
    private static final String PROPERTY_KEY_1 = "prop key";
    private static final String PROPERTY_VALUE_1 = "value 1";
    private static final String PROPERTY_KEY_2 = "prop key 2";
    private static final String PROPERTY_VALUE_2 = "value 2";

    @Test
    public void toString_returnsProperlyFormattedString() {
        State state = new State();
        state.setStatusVerbose(STATUS_VERBOSE);
        state.setStatus(STATUS);
        state.setProduct(PRODUCT);
        state.setCode(CODE);
        state.setAdditionalProperty(PROPERTY_KEY_1, PROPERTY_VALUE_1);
        state.setAdditionalProperty(PROPERTY_KEY_2, PROPERTY_VALUE_2);
        Map<String, Object> additionalProperties = new HashMap<>();
        additionalProperties.put(PROPERTY_KEY_1, PROPERTY_VALUE_1);
        additionalProperties.put(PROPERTY_KEY_2, PROPERTY_VALUE_2);

        String returnString = "State{" +
                "statusVerbose='" + STATUS_VERBOSE + '\'' +
                ", status=" + STATUS +
                ", product=" + PRODUCT +
                ", code='" + CODE + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';

        assertEquals(returnString, state.toString());
    }
}
