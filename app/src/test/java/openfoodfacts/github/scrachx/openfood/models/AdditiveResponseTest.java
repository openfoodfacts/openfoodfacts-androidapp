package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.*;
import static openfoodfacts.github.scrachx.openfood.models.AdditiveResponseTestData.*;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;

/**
 * Tests for {@link AdditiveResponse}
 */
public class AdditiveResponseTest {

    private Map<String, String> mStringMap = new HashMap<>();

    private AdditiveResponse mAdditiveResponse;

    @Before
    public void setup() {
        mStringMap.put(LANGUAGE_CODE_ENGLISH, VINEGAR_EN);
        mStringMap.put(LANGUAGE_CODE_FRENCH, VINEGAR_FR);
    }

    @Test
    public void mapWithoutWikiDataId_returnsAdditiveWithNamesWithoutWikiDataId() {
        mAdditiveResponse = new AdditiveResponse(ADDITIVE_TAG, mStringMap,null);
        Additive additive = mAdditiveResponse.map();

        assertEquals(ADDITIVE_TAG, additive.getTag());
        assertEquals(2, additive.getNames().size());

        assertEquals(LANGUAGE_CODE_ENGLISH, additive.getNames().get(0).getLanguageCode());
        assertEquals(VINEGAR_EN, additive.getNames().get(0).getName());
        assertEquals(ADDITIVE_TAG, additive.getNames().get(0).getAdditiveTag());
        assertEquals("null", additive.getNames().get(0).getWikiDataId());
        assertFalse(additive.getNames().get(0).getIsWikiDataIdPresent());

        assertEquals(LANGUAGE_CODE_FRENCH, additive.getNames().get(1).getLanguageCode());
        assertEquals(VINEGAR_FR, additive.getNames().get(1).getName());
        assertEquals(ADDITIVE_TAG, additive.getNames().get(1).getAdditiveTag());
        assertEquals("null", additive.getNames().get(1).getWikiDataId());
        assertFalse(additive.getNames().get(1).getIsWikiDataIdPresent());
    }

    @Test
    public void mapWithWikiDataId_returnsAdditiveWithNamesWithWikiDataId() {
        mAdditiveResponse = new AdditiveResponse(ADDITIVE_TAG, mStringMap,null, WIKI_DATA_ID);
        Additive additive = mAdditiveResponse.map();

        assertEquals(ADDITIVE_TAG, additive.getTag());
        assertEquals(2, additive.getNames().size());

        assertEquals(LANGUAGE_CODE_ENGLISH, additive.getNames().get(0).getLanguageCode());
        assertEquals(VINEGAR_EN, additive.getNames().get(0).getName());
        assertEquals(ADDITIVE_TAG, additive.getNames().get(0).getAdditiveTag());
        assertEquals(WIKI_DATA_ID, additive.getNames().get(0).getWikiDataId());
        assertTrue(additive.getNames().get(0).getIsWikiDataIdPresent());

        assertEquals(LANGUAGE_CODE_FRENCH, additive.getNames().get(1).getLanguageCode());
        assertEquals(VINEGAR_FR, additive.getNames().get(1).getName());
        assertEquals(ADDITIVE_TAG, additive.getNames().get(1).getAdditiveTag());
        assertEquals(WIKI_DATA_ID, additive.getNames().get(1).getWikiDataId());
        assertTrue(additive.getNames().get(1).getIsWikiDataIdPresent());
    }
}
