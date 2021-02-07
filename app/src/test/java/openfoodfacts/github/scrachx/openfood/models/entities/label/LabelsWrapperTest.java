package openfoodfacts.github.scrachx.openfood.models.entities.label;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
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
        assertThat(labels).hasSize(2);
    }

    @Test
    public void map_returnsListOfCorrectlyMappedLabels_correctStructure() {
        label1 = labels.get(0);
        label2 = labels.get(1);

        assertThat(label1.getTag()).isEqualTo(LABEL_TAG);
        assertThat(label1.getWikiDataId()).isNull();
        assertThat(label1.getIsWikiDataIdPresent()).isFalse();
        assertThat(label1.getNames()).hasSize(2);

        assertThat(label2.getTag()).isEqualTo(labelTag2);
        assertThat(label2.getWikiDataId()).isNull();
        assertThat(label2.getIsWikiDataIdPresent()).isFalse();
        assertThat(label2.getNames()).hasSize(2);
    }

    @Test
    public void map_returnsListOfCorrectlyMappedLabels_SubElementsAreMappedCorrectly() {
        label1 = labels.get(0);
        label2 = labels.get(1);

        LabelName label1_FRENCH = label1.getNames().get(0);
        assertThat(label1_FRENCH.getLabelTag()).isEqualTo(LABEL_TAG);
        assertThat(label1_FRENCH.getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
        assertThat(label1_FRENCH.getName()).isEqualTo(LABEL_NAME_FR);
        assertThat(label1_FRENCH.getIsWikiDataIdPresent()).isFalse();
        assertThat(label1_FRENCH.getWikiDataId()).isEqualTo("null");

        LabelName label1_ENGLISH = label1.getNames().get(1);
        assertThat(label1_ENGLISH.getLabelTag()).isEqualTo(LABEL_TAG);
        assertThat(label1_ENGLISH.getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(label1_ENGLISH.getName()).isEqualTo(LABEL_NAME_EN);
        assertThat(label1_ENGLISH.getIsWikiDataIdPresent()).isFalse();
        assertThat(label1_ENGLISH.getWikiDataId()).isEqualTo("null");

        LabelName label2_GERMAN = label2.getNames().get(0);
        assertThat(label2_GERMAN.getLabelTag()).isEqualTo(labelTag2);
        assertThat(label2_GERMAN.getLanguageCode()).isEqualTo(LANGUAGE_CODE_GERMAN);
        assertThat(label2_GERMAN.getName()).isEqualTo(LABEL_NAME_DE);
        assertThat(label2_GERMAN.getIsWikiDataIdPresent()).isFalse();
        assertThat(label2_GERMAN.getWikiDataId()).isEqualTo("null");

        LabelName label2_ENGLISH = label2.getNames().get(1);
        assertThat(label2_ENGLISH.getLabelTag()).isEqualTo(labelTag2);
        assertThat(label2_ENGLISH.getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(label2_ENGLISH.getName()).isEqualTo(LABEL_NAME_EN);
        assertThat(label2_ENGLISH.getIsWikiDataIdPresent()).isFalse();
        assertThat(label2_ENGLISH.getWikiDataId()).isEqualTo("null");
    }
}
