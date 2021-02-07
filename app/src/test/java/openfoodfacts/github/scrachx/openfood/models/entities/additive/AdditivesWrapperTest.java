package openfoodfacts.github.scrachx.openfood.models.entities.additive;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;
import static openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveResponseTestData.ADDITIVE_TAG;
import static openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveResponseTestData.VINEGAR_EN;
import static openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveResponseTestData.VINEGAR_FR;
import static openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveResponseTestData.WIKI_DATA_ID;

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
        AdditiveResponse additiveResponse1 = new AdditiveResponse(ADDITIVE_TAG, stringMap, null);
        AdditiveResponse additiveResponse2 =
            new AdditiveResponse(ADDITIVE_TAG, stringMap, null, WIKI_DATA_ID);
        additivesWrapper.setAdditives(Arrays.asList(additiveResponse1, additiveResponse2));
        List<Additive> mappedAdditives = additivesWrapper.map();

        assertThat(mappedAdditives).hasSize(2);

        Additive mappedAdditive1 = mappedAdditives.get(0);
        Additive mappedAdditive2 = mappedAdditives.get(1);

        assertThat(mappedAdditive1.getTag()).isEqualTo(ADDITIVE_TAG);
        assertThat(mappedAdditive2.getTag()).isEqualTo(ADDITIVE_TAG);

        // TODO: fix so that this test passes. Currently gives a null pointer.
        // The problem is based on using Boolean class rather than boolean primitive
        // Need to set the Boolean to false somewhere
        assertThat(mappedAdditives.get(0).getIsWikiDataIdPresent()).isFalse();

        assertThat(mappedAdditive1.getWikiDataId()).isNull();

        assertThat(mappedAdditive2.getIsWikiDataIdPresent()).isTrue();
        assertThat(mappedAdditive2.getWikiDataId()).isEqualTo(WIKI_DATA_ID);

        assertThat(mappedAdditive1.getNames()).hasSize(2);
        assertThat(mappedAdditive2.getNames()).hasSize(2);

        AdditiveName mA1Name1 = mappedAdditive1.getNames().get(0);
        AdditiveName mA1Name2 = mappedAdditive1.getNames().get(1);

        assertThat(mA1Name1.getAdditiveTag()).isEqualTo(ADDITIVE_TAG);
        assertThat(mA1Name2.getAdditiveTag()).isEqualTo(ADDITIVE_TAG);
        assertThat(mA1Name1.getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(mA1Name2.getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
        assertThat(mA1Name1.getName()).isEqualTo(VINEGAR_EN);
        assertThat(mA1Name2.getName()).isEqualTo(VINEGAR_FR);
        assertThat(mA1Name1.getIsWikiDataIdPresent()).isFalse();
        assertThat(mA1Name2.getIsWikiDataIdPresent()).isFalse();
        assertThat(mA1Name1.getWikiDataId()).isEqualTo("null");
        assertThat(mA1Name2.getWikiDataId()).isEqualTo("null");

        AdditiveName mA2Name1 = mappedAdditive2.getNames().get(0);
        AdditiveName mA2Name2 = mappedAdditive2.getNames().get(1);

        assertThat(mA2Name1.getAdditiveTag()).isEqualTo(ADDITIVE_TAG);
        assertThat(mA2Name2.getAdditiveTag()).isEqualTo(ADDITIVE_TAG);
        assertThat(mA2Name1.getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(mA2Name2.getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
        assertThat(mA2Name1.getName()).isEqualTo(VINEGAR_EN);
        assertThat(mA2Name2.getName()).isEqualTo(VINEGAR_FR);
        assertThat(mA2Name1.getIsWikiDataIdPresent()).isTrue();
        assertThat(mA2Name2.getIsWikiDataIdPresent()).isTrue();
        assertThat(mA2Name1.getWikiDataId()).isEqualTo(WIKI_DATA_ID);
        assertThat(mA2Name2.getWikiDataId()).isEqualTo(WIKI_DATA_ID);
    }
}
