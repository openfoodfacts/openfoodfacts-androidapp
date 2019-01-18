package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static openfoodfacts.github.scrachx.openfood.models.AllergenResponseTestData.*;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.*;

/**
 * Tests for {@link AllergensWrapper}
 */
public class AllergensWrapperTest {

    @Test
    public void map_returnsListOfCorrectlyMappedAllergens() {
        AllergensWrapper allergensWrapper = new AllergensWrapper();
        Map<String, String> nameMap1 = new HashMap<>();
        nameMap1.put(LANGUAGE_CODE_ENGLISH, PEANUTS_EN);
        nameMap1.put(LANGUAGE_CODE_FRENCH, PEANUTS_FR);

        // See AllergenResponseTest for the naming issue with UNIQUE_ALLERGEN_ID_1 and 2
        AllergenResponse allergenResponse1 = new AllergenResponse(UNIQUE_ALLERGEN_ID_1, nameMap1);
        Map<String, String> nameMap2 = new HashMap<>();
        nameMap2.put(LANGUAGE_CODE_ENGLISH, STRAWBERRY_EN);
        nameMap2.put(LANGUAGE_CODE_GERMAN, STRAWBERRY_DE);
        AllergenResponse allergenResponse2 = new AllergenResponse(UNIQUE_ALLERGEN_ID_2, nameMap2);
        allergensWrapper.setAllergens(Arrays.asList(allergenResponse1, allergenResponse2));

        List<Allergen> allergens = allergensWrapper.map();

        assertEquals(2, allergens.size());

        Allergen allergen1 = allergens.get(0);
        assertEquals(UNIQUE_ALLERGEN_ID_1, allergen1.getTag());
        assertEquals(2, allergen1.getNames().size());
        assertEquals(UNIQUE_ALLERGEN_ID_1, allergen1.getNames().get(0).getAllergenTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, allergen1.getNames().get(0).getLanguageCode());
        assertEquals(PEANUTS_EN, allergen1.getNames().get(0).getName());
        assertEquals(UNIQUE_ALLERGEN_ID_1, allergen1.getNames().get(1).getAllergenTag());
        assertEquals(LANGUAGE_CODE_FRENCH, allergen1.getNames().get(1).getLanguageCode());
        assertEquals(PEANUTS_FR, allergen1.getNames().get(1).getName());

        Allergen allergen2 = allergens.get(1);
        assertEquals(UNIQUE_ALLERGEN_ID_2, allergen2.getTag());
        assertEquals(2, allergen2.getNames().size());
        assertEquals(UNIQUE_ALLERGEN_ID_2, allergen2.getNames().get(0).getAllergenTag());
        assertEquals(LANGUAGE_CODE_GERMAN, allergen2.getNames().get(0).getLanguageCode());
        assertEquals(STRAWBERRY_DE, allergen2.getNames().get(0).getName());
        assertEquals(UNIQUE_ALLERGEN_ID_2, allergen2.getNames().get(1).getAllergenTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, allergen2.getNames().get(1).getLanguageCode());
        assertEquals(STRAWBERRY_EN, allergen2.getNames().get(1).getName());
    }
}
