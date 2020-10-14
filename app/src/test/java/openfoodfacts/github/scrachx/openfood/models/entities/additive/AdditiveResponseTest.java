package openfoodfacts.github.scrachx.openfood.models.entities.additive;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;
import static openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveResponseTestData.ADDITIVE_TAG;
import static openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveResponseTestData.VINEGAR_EN;
import static openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveResponseTestData.VINEGAR_FR;
import static openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveResponseTestData.WIKI_DATA_ID;

/**
 * Tests for {@link AdditiveResponse}
 */
public class AdditiveResponseTest {
    private final Map<String, String> mStringMap = new HashMap<>();

    private AdditiveResponse mAdditiveResponse;

    @Before
    public void setup() {
        mStringMap.put(LANGUAGE_CODE_ENGLISH, VINEGAR_EN);
        mStringMap.put(LANGUAGE_CODE_FRENCH, VINEGAR_FR);
    }

    @Test
    public void mapWithoutWikiDataId_returnsAdditiveWithNamesWithoutWikiDataId() {
        mAdditiveResponse = new AdditiveResponse(ADDITIVE_TAG, mStringMap, null);
        Additive additive = mAdditiveResponse.map();

        assertThat(additive.getTag()).isEqualTo(ADDITIVE_TAG);
        assertThat(additive.getNames()).hasSize(2);

        assertThat(additive.getNames().get(0).getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(additive.getNames().get(0).getName()).isEqualTo(VINEGAR_EN);
        assertThat(additive.getNames().get(0).getAdditiveTag()).isEqualTo(ADDITIVE_TAG);
        assertThat(additive.getNames().get(0).getWikiDataId()).isEqualTo("null");
        assertThat(additive.getNames().get(0).getIsWikiDataIdPresent()).isFalse();

        assertThat(additive.getNames().get(1).getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
        assertThat(additive.getNames().get(1).getName()).isEqualTo(VINEGAR_FR);
        assertThat(additive.getNames().get(1).getAdditiveTag()).isEqualTo(ADDITIVE_TAG);
        assertThat(additive.getNames().get(1).getWikiDataId()).isEqualTo("null");
        assertThat(additive.getNames().get(1).getIsWikiDataIdPresent()).isFalse();
    }

    @Test
    public void mapWithWikiDataId_returnsAdditiveWithNamesWithWikiDataId() {
        mAdditiveResponse = new AdditiveResponse(ADDITIVE_TAG, mStringMap, null, WIKI_DATA_ID);
        Additive additive = mAdditiveResponse.map();

        assertThat(additive.getTag()).isEqualTo(ADDITIVE_TAG);
        assertThat(additive.getNames()).hasSize(2);

        assertThat(additive.getNames().get(0).getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(additive.getNames().get(0).getName()).isEqualTo(VINEGAR_EN);
        assertThat(additive.getNames().get(0).getAdditiveTag()).isEqualTo(ADDITIVE_TAG);
        assertThat(additive.getNames().get(0).getWikiDataId()).isEqualTo(WIKI_DATA_ID);
        assertThat(additive.getNames().get(0).getIsWikiDataIdPresent()).isTrue();

        assertThat(additive.getNames().get(1).getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
        assertThat(additive.getNames().get(1).getName()).isEqualTo(VINEGAR_FR);
        assertThat(additive.getNames().get(1).getAdditiveTag()).isEqualTo(ADDITIVE_TAG);
        assertThat(additive.getNames().get(1).getWikiDataId()).isEqualTo(WIKI_DATA_ID);
        assertThat(additive.getNames().get(1).getIsWikiDataIdPresent()).isTrue();
    }
}
