package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.*;
import static openfoodfacts.github.scrachx.openfood.models.LabelNameTestData.*;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;

/**
 * Tests for {@link LabelResponse}
 */
public class LabelResponseTest {

    private Map<String, String> mNamesMap;

    private LabelResponse mLabelResponse;

    @Before
    public void setup() {
        mNamesMap = new HashMap<>(2);
        mNamesMap.put(LANGUAGE_CODE_ENGLISH, LABEL_NAME_EN);
        mNamesMap.put(LANGUAGE_CODE_FRENCH, LABEL_NAME_FR);
    }

    @Test
    public void mapWithoutWikiDataId_returnsLabelWithNamesWithoutWikiDataId() {
        mLabelResponse = new LabelResponse(LABEL_TAG, mNamesMap);
        Label label = mLabelResponse.map();

        assertEquals(LABEL_TAG, label.getTag());
        assertFalse(label.getIsWikiDataIdPresent());
        assertNull(label.getWikiDataId());
        assertEquals(2, label.getNames().size());

        LabelName labelName1 = label.getNames().get(0);
        assertEquals(LABEL_TAG, labelName1.getLabelTag());
        assertEquals(LANGUAGE_CODE_FRENCH, labelName1.getLanguageCode());
        assertEquals(LABEL_NAME_FR, labelName1.getName());
        assertFalse(labelName1.getIsWikiDataIdPresent());
        assertEquals("null", labelName1.getWikiDataId());

        LabelName labelName2 = label.getNames().get(1);
        assertEquals(LABEL_TAG, labelName2.getLabelTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, labelName2.getLanguageCode());
        assertEquals(LABEL_NAME_EN, labelName2.getName());
        assertFalse(labelName2.getIsWikiDataIdPresent());
        assertEquals("null", labelName2.getWikiDataId());
    }

    @Test
    public void mapWithWikiDataId_returnsLabelsWithNamesWithWikiDataId() {
        String wikiDataId = "wikiDataId";
        mLabelResponse = new LabelResponse(LABEL_TAG, mNamesMap, wikiDataId);
        Label label = mLabelResponse.map();

        assertEquals(LABEL_TAG, label.getTag());
        assertTrue(label.getIsWikiDataIdPresent());
        assertEquals(wikiDataId, label.getWikiDataId());
        assertEquals(2, label.getNames().size());

        LabelName labelName1 = label.getNames().get(0);
        assertEquals(LABEL_TAG, labelName1.getLabelTag());
        assertEquals(LANGUAGE_CODE_FRENCH, labelName1.getLanguageCode());
        assertEquals(LABEL_NAME_FR, labelName1.getName());
        assertTrue(labelName1.getIsWikiDataIdPresent());
        assertEquals(wikiDataId, labelName1.getWikiDataId());

        LabelName labelName2 = label.getNames().get(1);
        assertEquals(LABEL_TAG, labelName2.getLabelTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, labelName2.getLanguageCode());
        assertEquals(LABEL_NAME_EN, labelName2.getName());
        assertTrue(labelName2.getIsWikiDataIdPresent());
        assertEquals(wikiDataId, labelName2.getWikiDataId());
    }
}
