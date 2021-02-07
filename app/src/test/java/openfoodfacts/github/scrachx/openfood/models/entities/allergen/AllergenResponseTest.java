package openfoodfacts.github.scrachx.openfood.models.entities.allergen;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;
import static openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenResponseTestData.PEANUTS_EN;
import static openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenResponseTestData.PEANUTS_FR;
import static openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenResponseTestData.UNIQUE_ALLERGEN_ID_1;

/**
 * Tests for {@link AllergenResponse}
 */
public class AllergenResponseTest {

    private Map<String, String> nameMap;

    @Before
    public void setUp(){
        nameMap = new HashMap<>();
    }

    @Test
    public void map_returnsMappedAllergen() {
        nameMap.put(LANGUAGE_CODE_ENGLISH, PEANUTS_EN);
        nameMap.put(LANGUAGE_CODE_FRENCH, PEANUTS_FR);
        AllergenResponse allergenResponse = new AllergenResponse(UNIQUE_ALLERGEN_ID_1, nameMap);
        Allergen allergen = allergenResponse.map();

        // TODO: expected this to be the allergen ID but a string to constructor actually makes it
        // the tag. Should update AllergenResponse to fix that.
        assertThat(allergen.getTag()).isEqualTo(UNIQUE_ALLERGEN_ID_1);
        assertThat(allergen.getNames()).hasSize(2);
        assertThat(allergen.getNames().get(0).getAllergenTag()).isEqualTo(UNIQUE_ALLERGEN_ID_1);
        assertThat(allergen.getNames().get(0).getName()).isEqualTo(PEANUTS_EN);
        assertThat(allergen.getNames().get(0).getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(allergen.getNames().get(1).getAllergenTag()).isEqualTo(UNIQUE_ALLERGEN_ID_1);
        assertThat(allergen.getNames().get(1).getName()).isEqualTo(PEANUTS_FR);
        assertThat(allergen.getNames().get(1).getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
    }

    @Test
    public void map_returnsMappedAllergenWithWikiDataCode() {
        String wikiDataCode = "Q12345";
        AllergenResponse allergenResponse = new AllergenResponse(UNIQUE_ALLERGEN_ID_1, nameMap, wikiDataCode);
        Allergen allergen = allergenResponse.map();

        assertThat(allergen.getWikiDataId()).isEqualTo(wikiDataCode);
    }
}
