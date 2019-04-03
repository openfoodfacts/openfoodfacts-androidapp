package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;
import static openfoodfacts.github.scrachx.openfood.models.AdditiveResponseTestData.*;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;

/**
 * Tests for {@link AdditivesWrapper}
 */
public class AdditivesWrapperTest {


    @Test
    public void map_returnsListOfCorrectlyMappedAdditives() {
        AdditivesWrapper additivesWrapper = new AdditivesWrapper();
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put(LANGUAGE_CODE_ENGLISH, VINEGAR_EN);
        stringMap.put(LANGUAGE_CODE_FRENCH, VINEGAR_FR);
        AdditiveResponse additiveResponse1 = new AdditiveResponse(ADDITIVE_TAG, stringMap,null);
        AdditiveResponse additiveResponse2 =
                new AdditiveResponse(ADDITIVE_TAG, stringMap,null, WIKI_DATA_ID);
        additivesWrapper.setAdditives(Arrays.asList(additiveResponse1, additiveResponse2));
        List<Additive> mappedAdditives = additivesWrapper.map();

        assertEquals(2, mappedAdditives.size());

        Additive mappedAdditive1 = mappedAdditives.get(0);
        Additive mappedAdditive2 = mappedAdditives.get(1);

        assertEquals(ADDITIVE_TAG, mappedAdditive1.getTag());
        assertEquals(ADDITIVE_TAG, mappedAdditive2.getTag());

        // TODO: fix so that this test passes. Currently gives a null pointer.
        // The problem is based on using Boolean class rather than boolean primitive
        // Need to set the Boolean to false somewhere
         assertFalse(mappedAdditives.get(0).getIsWikiDataIdPresent());

        assertNull(mappedAdditive1.getWikiDataId());

        assertTrue(mappedAdditive2.getIsWikiDataIdPresent());
        assertEquals(WIKI_DATA_ID, mappedAdditive2.getWikiDataId());

        assertEquals(2, mappedAdditive1.getNames().size());
        assertEquals(2, mappedAdditive2.getNames().size());

        AdditiveName mA1Name1 = mappedAdditive1.getNames().get(0);
        AdditiveName mA1Name2 = mappedAdditive1.getNames().get(1);

        assertEquals(ADDITIVE_TAG, mA1Name1.getAdditiveTag());
        assertEquals(ADDITIVE_TAG, mA1Name2.getAdditiveTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, mA1Name1.getLanguageCode());
        assertEquals(LANGUAGE_CODE_FRENCH, mA1Name2.getLanguageCode());
        assertEquals(VINEGAR_EN, mA1Name1.getName());
        assertEquals(VINEGAR_FR, mA1Name2.getName());
        assertFalse(mA1Name1.getIsWikiDataIdPresent());
        assertFalse(mA1Name2.getIsWikiDataIdPresent());
        assertEquals("null", mA1Name1.getWikiDataId());
        assertEquals("null", mA1Name2.getWikiDataId());

        AdditiveName mA2Name1 = mappedAdditive2.getNames().get(0);
        AdditiveName mA2Name2 = mappedAdditive2.getNames().get(1);

        assertEquals(ADDITIVE_TAG, mA2Name1.getAdditiveTag());
        assertEquals(ADDITIVE_TAG, mA2Name2.getAdditiveTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, mA2Name1.getLanguageCode());
        assertEquals(LANGUAGE_CODE_FRENCH, mA2Name2.getLanguageCode());
        assertEquals(VINEGAR_EN, mA2Name1.getName());
        assertEquals(VINEGAR_FR, mA2Name2.getName());
        assertTrue(mA2Name1.getIsWikiDataIdPresent());
        assertTrue(mA2Name2.getIsWikiDataIdPresent());
        assertEquals(WIKI_DATA_ID, mA2Name1.getWikiDataId());
        assertEquals(WIKI_DATA_ID, mA2Name2.getWikiDataId());

    }
}
