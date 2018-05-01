import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.models.Allergen;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Lobster on 05.03.18.
 */

@SmallTest
@RunWith(AndroidJUnit4.class)
public class ProductRepositoryTest {

    private final String TEST_ALLERGEN_TAG = "en:lupin";
    private final String TEST_LANGUAGE_CODE = "es";
    private final String TEST_ALLERGEN_NAME = "Altramuces";

    private IProductRepository productRepository;

    @Before
    public void setup() {
        productRepository = ProductRepository.getInstance();
        productRepository.saveAllergens(createAllergens());
    }

    @Test
    public void testGetAllergens() {
        List<Allergen> allergens = productRepository.getAllergens(false).blockingGet();

        assertNotNull(allergens);
        assertEquals(allergens.size(), 2);

        Allergen allergen = allergens.get(0);
        assertEquals(allergen.getTag(), TEST_ALLERGEN_TAG);

        List<AllergenName> allergenNames = allergen.getNames();
        assertEquals(allergenNames.size(), 3);

        AllergenName allergenName = allergenNames.get(0);
        assertEquals(allergenName.getAllergenTag(), allergen.getTag());
        assertEquals(allergenName.getLanguageCode(), TEST_LANGUAGE_CODE);
        assertEquals(allergenName.getName(), TEST_ALLERGEN_NAME);
    }

    @Test
    public void testGetEnabledAllergens() {
        List<Allergen> allergens = productRepository.getEnabledAllergens();

        assertNotNull(allergens);
        assertEquals(allergens.size(), 1);
        assertEquals(allergens.get(0).getTag(), TEST_ALLERGEN_TAG);
    }

    @Test
    public void testGetAllergensByEnabledAndLanguageCode() {
        List<AllergenName> enabledAllergenNames = productRepository.getAllergensByEnabledAndLanguageCode(true, TEST_LANGUAGE_CODE).blockingGet();
        List<AllergenName> notEnabledAllergenNames = productRepository.getAllergensByEnabledAndLanguageCode(false, TEST_LANGUAGE_CODE).blockingGet();

        assertNotNull(enabledAllergenNames);
        assertNotNull(notEnabledAllergenNames);

        assertEquals(enabledAllergenNames.size(), 1);
        assertEquals(notEnabledAllergenNames.size(), 1);

        assertEquals(enabledAllergenNames.get(0).getName(), TEST_ALLERGEN_NAME);
        assertEquals(notEnabledAllergenNames.get(0).getName(), "Molluschi");
    }

    @Test
    public void testGetAllergensByLanguageCode() {
        List<AllergenName> allergenNames = productRepository.getAllergensByLanguageCode(TEST_LANGUAGE_CODE).blockingGet();

        assertNotNull(allergenNames);
        assertEquals(allergenNames.size(), 2);
    }

    private List<Allergen> createAllergens() {
        Allergen allergen1 = new Allergen(TEST_ALLERGEN_TAG, new ArrayList<>());
        allergen1.setEnabled(true);
        allergen1.getNames().add(new AllergenName(allergen1.getTag(), TEST_LANGUAGE_CODE, TEST_ALLERGEN_NAME));
        allergen1.getNames().add(new AllergenName(allergen1.getTag(), "bg", "Лупина"));
        allergen1.getNames().add(new AllergenName(allergen1.getTag(), "fr", "Lupin"));

        Allergen allergen2 = new Allergen("en:molluscs", new ArrayList<>());
        allergen2.getNames().add(new AllergenName(allergen2.getTag(), TEST_LANGUAGE_CODE, "Molluschi"));
        allergen2.getNames().add(new AllergenName(allergen2.getTag(), "en", "Mollusques"));

        return Arrays.asList(allergen1, allergen2);
    }
}
