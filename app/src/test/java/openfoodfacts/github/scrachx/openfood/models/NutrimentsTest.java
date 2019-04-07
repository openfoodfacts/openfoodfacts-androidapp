package openfoodfacts.github.scrachx.openfood.models;

import openfoodfacts.github.scrachx.openfood.utils.UnitUtils;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

public class NutrimentsTest {

    // TODO: in Nutriments, there is confusion between name and value when turning it into a Nutriment
    // TODO: in Nutriments, make the key endings public Strings, or at least turn them into variables
    private static final String NUTRIMENT_NAME_KEY = "a nutriment";
    private static final String NUTRIMENT_NAME = "a nutriment";
    private static final String NUTRIMENT_VALUE_KEY = NUTRIMENT_NAME_KEY + "_value";
    private static final String NUTRIMENT_VALUE = "100.0";
    private static final String NUTRIMENT_100G_KEY = NUTRIMENT_NAME_KEY + "_100g";
    private static final String NUTRIMENT_100G = "50%";
    private static final String NUTRIMENT_SERVING_KEY = NUTRIMENT_NAME_KEY + "_serving";
    private static final String NUTRIMENT_SERVING = "70%";
    private static final String NUTRIMENT_UNIT_KEY = NUTRIMENT_NAME_KEY + "_unit";
    private static final String NUTRIMENT_UNIT = "mg";
    private Nutriments nutriments;

    @Before
    public void setup() {
        nutriments = new Nutriments();
    }

    @Test
    public void getValue_returnsStringValue() {
        nutriments.setAdditionalProperty(NUTRIMENT_VALUE_KEY, NUTRIMENT_VALUE);
        assertEquals(NUTRIMENT_VALUE, nutriments.getValue(NUTRIMENT_NAME_KEY));
    }

    @Test
    public void getForAnyValue() {
        float valueInGramFor100Gram = 30;
        float valueInGramFor200Gram = 60;
        Nutriments.Nutriment nutriment = new Nutriments.Nutriment("test", Double.toString(valueInGramFor100Gram), Double.toString(valueInGramFor200Gram), UnitUtils.UNIT_MILLIGRAM,
            "");
        assertEquals(Utils.getRoundNumber(30 * 1000) + " mg", nutriment.getDisplayStringFor100g());
        assertEquals(Utils.getRoundNumber(UnitUtils.convertFromGram(valueInGramFor100Gram * 10, nutriment.getUnit())), nutriment.getForAnyValue(1, UnitUtils.UNIT_KILOGRAM));
        assertEquals(Utils.getRoundNumber(UnitUtils.convertFromGram(valueInGramFor100Gram / 100, nutriment.getUnit())), nutriment.getForAnyValue(1, UnitUtils.UNIT_GRAM));
    }

    @Test
    public void getUnit_returnsUnit() {
        nutriments.setAdditionalProperty(NUTRIMENT_UNIT_KEY, NUTRIMENT_UNIT);
        assertEquals(NUTRIMENT_UNIT, nutriments.getUnit(NUTRIMENT_NAME_KEY));
    }

    @Test
    public void getServing_returnsServing() {
        nutriments.setAdditionalProperty(NUTRIMENT_SERVING_KEY, NUTRIMENT_SERVING);
        assertEquals(NUTRIMENT_SERVING, nutriments.getServing(NUTRIMENT_NAME_KEY));
    }

    @Test
    public void get100g_returns100g() {
        nutriments.setAdditionalProperty(NUTRIMENT_100G_KEY, NUTRIMENT_100G);
        assertEquals(NUTRIMENT_100G, nutriments.get100g(NUTRIMENT_NAME_KEY));
    }

    @Test
    public void getNonExistentNutriment_returnsNull() {
        assertNull(nutriments.get("not there"));
    }

    @Test
    public void getAvailableNutriment_returnsNutriment() {
        nutriments.setAdditionalProperty(NUTRIMENT_NAME_KEY, NUTRIMENT_NAME);
        nutriments.setAdditionalProperty(NUTRIMENT_100G_KEY, NUTRIMENT_100G);
        nutriments.setAdditionalProperty(NUTRIMENT_SERVING_KEY, NUTRIMENT_SERVING);
        nutriments.setAdditionalProperty(NUTRIMENT_UNIT_KEY, NUTRIMENT_UNIT);

        Nutriments.Nutriment nutriment = nutriments.get(NUTRIMENT_NAME_KEY);

        // See note about confusion between value and name above
        assertEquals(NUTRIMENT_NAME, nutriment.getName());
        assertEquals(NUTRIMENT_UNIT, nutriment.getUnit());
    }

    @Test
    public void setAdditionalPropertyWithMineralName_setsHasMineralsTrue() {
        nutriments.setAdditionalProperty(Nutriments.SILICA, Nutriments.SILICA);
        assertTrue(nutriments.hasMinerals());
    }

    @Test
    public void setAdditionalPropertyWithVitaminName_setsHasVitaminsTrue() {
        nutriments.setAdditionalProperty(Nutriments.VITAMIN_A, Nutriments.VITAMIN_A);
        assertTrue(nutriments.hasVitamins());
    }

    @Test
    public void containsWithAvailableElement_returnsTrue() {
        nutriments.setAdditionalProperty(Nutriments.VITAMIN_A, Nutriments.VITAMIN_A);
        assertTrue(nutriments.contains(Nutriments.VITAMIN_A));
    }

    @Test
    public void containsWithNonExistentElement_returnsFalse() {
        assertFalse(nutriments.contains(Nutriments.VITAMIN_B1));
    }
}
