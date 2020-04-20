package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
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

    List<Allergen> allergens;
    Allergen allergen1;
    Allergen allergen2;


    @Before
    public void setUp(){
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

        allergens = allergensWrapper.map();
        allergen1 = allergens.get(0);
        allergen2 = allergens.get(1);
    }

    @Test
    public void allergensWrapper_CreatesOneListPerAllergen(){
        assertEquals(2, allergens.size());
    }

    @Test
    public void map_returnsListOfCorrectlyMappedAllergens() {
        assertEquals(UNIQUE_ALLERGEN_ID_1, allergen1.getTag());
        assertEquals(2, allergen1.getNames().size());

        assertEquals(UNIQUE_ALLERGEN_ID_2, allergen2.getTag());
        assertEquals(2, allergen2.getNames().size());
    }

    @Test
    public void map_returnsListOfCorrectlyMappedSubAllergens_Tag(){
        assertEquals(UNIQUE_ALLERGEN_ID_1, allergen1.getNames().get(0).getAllergenTag());
        assertEquals(UNIQUE_ALLERGEN_ID_1, allergen1.getNames().get(1).getAllergenTag());

        assertEquals(UNIQUE_ALLERGEN_ID_2, allergen2.getNames().get(0).getAllergenTag());
        assertEquals(UNIQUE_ALLERGEN_ID_2, allergen2.getNames().get(1).getAllergenTag());
    }

    @Test
    public void map_returnsListOfCorrectlyMappedSubAllergens_LanguageCode(){
        assertEquals(LANGUAGE_CODE_ENGLISH, allergen1.getNames().get(0).getLanguageCode());
        assertEquals(LANGUAGE_CODE_FRENCH, allergen1.getNames().get(1).getLanguageCode());

        assertEquals(LANGUAGE_CODE_GERMAN, allergen2.getNames().get(0).getLanguageCode());
        assertEquals(LANGUAGE_CODE_ENGLISH, allergen2.getNames().get(1).getLanguageCode());
    }

    @Test
    public void map_returnsListOfCorrectlyMappedSubAllergens_Names(){
        assertEquals(PEANUTS_EN, allergen1.getNames().get(0).getName());
        assertEquals(PEANUTS_FR, allergen1.getNames().get(1).getName());

        assertEquals(STRAWBERRY_DE, allergen2.getNames().get(0).getName());
        assertEquals(STRAWBERRY_EN, allergen2.getNames().get(1).getName());
    }
}
