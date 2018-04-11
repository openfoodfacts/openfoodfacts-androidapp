package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Tests for {@link AdditiveResponse}
 */
public class AdditiveResponseTest {

    private static final String ADDITIVE_TAG = "tag";
    private static final String VINEGAR_EN = "vinegar";
    private static final String VINEGAR_FR = "vinaigre";
    private static final String WIKI_DATA_ID = "wikiId";

    private Map<String, String> mStringMap = new HashMap<>();

    private AdditiveResponse mAdditiveResponse;

    @Before
    public void setup() {
        mStringMap.put("en", VINEGAR_EN);
        mStringMap.put("fr", VINEGAR_FR);
    }

    @Test
    public void mapWithoutWikiDataId_returnsWithNamesWithoutWikiDataId() {
        mAdditiveResponse = new AdditiveResponse(ADDITIVE_TAG, mStringMap);
        Additive additive = mAdditiveResponse.map();

        assertEquals(ADDITIVE_TAG, additive.getTag());
        assertEquals(2, additive.getNames().size());

        assertEquals(VINEGAR_EN, additive.getNames().get(0).getName());
        assertEquals(ADDITIVE_TAG, additive.getNames().get(0).getAdditiveTag());
        assertEquals("null", additive.getNames().get(0).getWikiDataId());
        assertFalse(additive.getNames().get(0).getIsWikiDataIdPresent());

        assertEquals(VINEGAR_FR, additive.getNames().get(1).getName());
        assertEquals(ADDITIVE_TAG, additive.getNames().get(1).getAdditiveTag());
        assertEquals("null", additive.getNames().get(1).getWikiDataId());
        assertFalse(additive.getNames().get(1).getIsWikiDataIdPresent());
    }

    @Test
    public void mapWithWikiDataId_returnsAdditiveWithNamesWithWikiDataId() {
        mAdditiveResponse = new AdditiveResponse(ADDITIVE_TAG, mStringMap, WIKI_DATA_ID);
        Additive additive = mAdditiveResponse.map();

        assertEquals(ADDITIVE_TAG, additive.getTag());
        assertEquals(2, additive.getNames().size());

        assertEquals(VINEGAR_EN, additive.getNames().get(0).getName());
        assertEquals(ADDITIVE_TAG, additive.getNames().get(0).getAdditiveTag());
        assertEquals(WIKI_DATA_ID, additive.getNames().get(0).getWikiDataId());
        assertTrue(additive.getNames().get(0).getIsWikiDataIdPresent());

        assertEquals(VINEGAR_FR, additive.getNames().get(1).getName());
        assertEquals(ADDITIVE_TAG, additive.getNames().get(1).getAdditiveTag());
        assertEquals(WIKI_DATA_ID, additive.getNames().get(1).getWikiDataId());
        assertTrue(additive.getNames().get(1).getIsWikiDataIdPresent());
    }
}
