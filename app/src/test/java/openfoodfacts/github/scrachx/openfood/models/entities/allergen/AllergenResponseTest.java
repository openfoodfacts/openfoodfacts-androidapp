package openfoodfacts.github.scrachx.openfood.models.entities.allergen;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
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
        assertEquals(UNIQUE_ALLERGEN_ID_1, allergen.getTag());
        assertEquals(2, allergen.getNames().size());
        assertEquals(UNIQUE_ALLERGEN_ID_1, allergen.getNames().get(0).getAllergenTag());
        assertEquals(PEANUTS_EN, allergen.getNames().get(0).getName());
        assertEquals(LANGUAGE_CODE_ENGLISH, allergen.getNames().get(0).getLanguageCode());
        assertEquals(UNIQUE_ALLERGEN_ID_1, allergen.getNames().get(1).getAllergenTag());
        assertEquals(PEANUTS_FR, allergen.getNames().get(1).getName());
        assertEquals(LANGUAGE_CODE_FRENCH, allergen.getNames().get(1).getLanguageCode());
    }

    @Test
    public void map_returnsMappedAllergenWithWikiDataCode() {
        String wikiDataCode = "Q12345";
        AllergenResponse allergenResponse = new AllergenResponse(UNIQUE_ALLERGEN_ID_1, nameMap, wikiDataCode);
        Allergen allergen = allergenResponse.map();

        assertEquals(wikiDataCode, allergen.getWikiDataId());
    }
}
