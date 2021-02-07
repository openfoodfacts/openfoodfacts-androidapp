package openfoodfacts.github.scrachx.openfood.models.entities.label;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;
import static openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_EN;
import static openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_FR;
import static openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_TAG;

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

        assertThat(label.getTag()).isEqualTo(LABEL_TAG);
        assertThat(label.getIsWikiDataIdPresent()).isFalse();
        assertThat(label.getWikiDataId()).isNull();
        assertThat(label.getNames()).hasSize(2);

        LabelName labelName1 = label.getNames().get(0);
        assertThat(labelName1.getLabelTag()).isEqualTo(LABEL_TAG);
        assertThat(labelName1.getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
        assertThat(labelName1.getName()).isEqualTo(LABEL_NAME_FR);
        assertThat(labelName1.getIsWikiDataIdPresent()).isFalse();
        assertThat(labelName1.getWikiDataId()).isEqualTo("null");

        LabelName labelName2 = label.getNames().get(1);
        assertThat(labelName2.getLabelTag()).isEqualTo(LABEL_TAG);
        assertThat(labelName2.getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(labelName2.getName()).isEqualTo(LABEL_NAME_EN);
        assertThat(labelName2.getIsWikiDataIdPresent()).isFalse();
        assertThat(labelName2.getWikiDataId()).isEqualTo("null");
    }

    @Test
    public void mapWithWikiDataId_returnsLabelsWithNamesWithWikiDataId() {
        String wikiDataId = "wikiDataId";
        mLabelResponse = new LabelResponse(LABEL_TAG, mNamesMap, wikiDataId);
        Label label = mLabelResponse.map();

        assertThat(label.getTag()).isEqualTo(LABEL_TAG);
        assertThat(label.getIsWikiDataIdPresent()).isTrue();
        assertThat(label.getWikiDataId()).isEqualTo(wikiDataId);
        assertThat(label.getNames()).hasSize(2);

        LabelName labelName1 = label.getNames().get(0);
        assertThat(labelName1.getLabelTag()).isEqualTo(LABEL_TAG);
        assertThat(labelName1.getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
        assertThat(labelName1.getName()).isEqualTo(LABEL_NAME_FR);
        assertThat(labelName1.getIsWikiDataIdPresent()).isTrue();
        assertThat(labelName1.getWikiDataId()).isEqualTo(wikiDataId);

        LabelName labelName2 = label.getNames().get(1);
        assertThat(labelName2.getLabelTag()).isEqualTo(LABEL_TAG);
        assertThat(labelName2.getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(labelName2.getName()).isEqualTo(LABEL_NAME_EN);
        assertThat(labelName2.getIsWikiDataIdPresent()).isTrue();
        assertThat(labelName2.getWikiDataId()).isEqualTo(wikiDataId);
    }
}
