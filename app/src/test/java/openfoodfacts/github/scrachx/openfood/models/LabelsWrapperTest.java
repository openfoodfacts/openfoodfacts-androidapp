package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static openfoodfacts.github.scrachx.openfood.models.LabelNameTestData.*;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.*;

/**
 * Tests for {@link LabelsWrapper}
 */
public class LabelsWrapperTest {

    @Test
    public void map_returnsListOfCorrectlyMappedLabels() {
        LabelsWrapper labelsWrapper = new LabelsWrapper();
        Map<String, String> namesMap1 = new HashMap<>(2);
        namesMap1.put(LANGUAGE_CODE_ENGLISH, LABEL_NAME_EN);
        namesMap1.put(LANGUAGE_CODE_FRENCH, LABEL_NAME_FR);
        LabelResponse labelResponse1 = new LabelResponse(LABEL_TAG, namesMap1);

        Map<String, String> namesMap2 = new HashMap<>(2);
        namesMap2.put(LANGUAGE_CODE_GERMAN, LABEL_NAME_DE);
        namesMap2.put(LANGUAGE_CODE_ENGLISH, LABEL_NAME_EN);
        String labelTag2 = "Tag2";
        LabelResponse labelResponse2 = new LabelResponse(labelTag2, namesMap2);
        labelsWrapper.setLabels(Arrays.asList(labelResponse1, labelResponse2));

        List<Label> labels = labelsWrapper.map();

        assertEquals(2, labels.size());

        Label label1 = labels.get(0);
        assertEquals(LABEL_TAG, label1.getTag());
        assertEquals(null, label1.getWikiDataId());
        assertFalse(label1.getIsWikiDataIdPresent());
        assertEquals(2, label1.getNames().size());

        LabelName label1Name1 = label1.getNames().get(0);
        assertEquals(LABEL_TAG, label1Name1.getLabelTag());
        assertEquals(LANGUAGE_CODE_FRENCH, label1Name1.getLanguageCode());
        assertEquals(LABEL_NAME_FR, label1Name1.getName());
        assertFalse(label1Name1.getIsWikiDataIdPresent());
        assertEquals("null", label1Name1.getWikiDataId());

        LabelName label1Name2 = label1.getNames().get(1);
        assertEquals(LABEL_TAG, label1Name2.getLabelTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, label1Name2.getLanguageCode());
        assertEquals(LABEL_NAME_EN, label1Name2.getName());
        assertFalse(label1Name2.getIsWikiDataIdPresent());
        assertEquals("null", label1Name2.getWikiDataId());

        Label label2 = labels.get(1);
        assertEquals(labelTag2, label2.getTag());
        assertEquals(null, label2.getWikiDataId());
        assertFalse(label2.getIsWikiDataIdPresent());
        assertEquals(2, label2.getNames().size());

        LabelName label2Name1 = label2.getNames().get(0);
        assertEquals(labelTag2, label2Name1.getLabelTag());
        assertEquals(LANGUAGE_CODE_GERMAN, label2Name1.getLanguageCode());
        assertEquals(LABEL_NAME_DE, label2Name1.getName());
        assertFalse(label2Name1.getIsWikiDataIdPresent());
        assertEquals("null", label2Name1.getWikiDataId());

        LabelName label2Name2 = label2.getNames().get(1);
        assertEquals(labelTag2, label2Name2.getLabelTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, label2Name2.getLanguageCode());
        assertEquals(LABEL_NAME_EN, label2Name2.getName());
        assertFalse(label2Name2.getIsWikiDataIdPresent());
        assertEquals("null", label2Name2.getWikiDataId());
    }
}
