package openfoodfacts.github.scrachx.openfood.models.entities.label;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_GERMAN;
import static openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_DE;
import static openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_EN;
import static openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_FR;
import static openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_TAG;

/**
 * Tests for {@link LabelsWrapper}
 */
public class LabelsWrapperTest {

    List<Label> labels;
    String labelTag2;
    Label label1;
    Label label2;

    @Before
    public void setUp(){
        LabelsWrapper labelsWrapper = new LabelsWrapper();

        Map<String, String> namesMap1 = new HashMap<>(2);
        namesMap1.put(LANGUAGE_CODE_ENGLISH, LABEL_NAME_EN);
        namesMap1.put(LANGUAGE_CODE_FRENCH, LABEL_NAME_FR);
        LabelResponse labelResponse1 = new LabelResponse(LABEL_TAG, namesMap1);

        Map<String, String> namesMap2 = new HashMap<>(2);
        namesMap2.put(LANGUAGE_CODE_GERMAN, LABEL_NAME_DE);
        namesMap2.put(LANGUAGE_CODE_ENGLISH, LABEL_NAME_EN);
        labelTag2 = "Tag2";
        LabelResponse labelResponse2 = new LabelResponse(labelTag2, namesMap2);

        labelsWrapper.setLabels(Arrays.asList(labelResponse1, labelResponse2));
        labels = labelsWrapper.map();
    }


    @Test
    public void map_returnsListOfCorrectlyMappedLabels_correctSize(){
        assertEquals(2, labels.size());
    }

    @Test
    public void map_returnsListOfCorrectlyMappedLabels_correctStructure(){
        label1 = labels.get(0);
        label2 = labels.get(1);

        assertEquals(LABEL_TAG, label1.getTag());
        assertNull(label1.getWikiDataId());
        assertFalse(label1.getIsWikiDataIdPresent());
        assertEquals(2, label1.getNames().size());

        assertEquals(labelTag2, label2.getTag());
        assertNull(label2.getWikiDataId());
        assertFalse(label2.getIsWikiDataIdPresent());
        assertEquals(2, label2.getNames().size());
    }

    @Test
    public void map_returnsListOfCorrectlyMappedLabels_SubElementsAreMappedCorrectly() {
        label1 = labels.get(0);
        label2 = labels.get(1);

        LabelName label1_FRENCH = label1.getNames().get(0);
        assertEquals(LABEL_TAG, label1_FRENCH.getLabelTag());
        assertEquals(LANGUAGE_CODE_FRENCH, label1_FRENCH.getLanguageCode());
        assertEquals(LABEL_NAME_FR, label1_FRENCH.getName());
        assertFalse(label1_FRENCH.getIsWikiDataIdPresent());
        assertEquals("null", label1_FRENCH.getWikiDataId());

        LabelName label1_ENGLISH = label1.getNames().get(1);
        assertEquals(LABEL_TAG, label1_ENGLISH.getLabelTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, label1_ENGLISH.getLanguageCode());
        assertEquals(LABEL_NAME_EN, label1_ENGLISH.getName());
        assertFalse(label1_ENGLISH.getIsWikiDataIdPresent());
        assertEquals("null", label1_ENGLISH.getWikiDataId());

        LabelName label2_GERMAN = label2.getNames().get(0);
        assertEquals(labelTag2, label2_GERMAN.getLabelTag());
        assertEquals(LANGUAGE_CODE_GERMAN, label2_GERMAN.getLanguageCode());
        assertEquals(LABEL_NAME_DE, label2_GERMAN.getName());
        assertFalse(label2_GERMAN.getIsWikiDataIdPresent());
        assertEquals("null", label2_GERMAN.getWikiDataId());

        LabelName label2_ENGLISH = label2.getNames().get(1);
        assertEquals(labelTag2, label2_ENGLISH.getLabelTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, label2_ENGLISH.getLanguageCode());
        assertEquals(LABEL_NAME_EN, label2_ENGLISH.getName());
        assertFalse(label2_ENGLISH.getIsWikiDataIdPresent());
        assertEquals("null", label2_ENGLISH.getWikiDataId());
    }
}
