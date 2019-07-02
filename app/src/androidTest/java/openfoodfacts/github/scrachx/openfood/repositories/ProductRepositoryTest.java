package openfoodfacts.github.scrachx.openfood.repositories;

import android.util.Log;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;
import openfoodfacts.github.scrachx.openfood.models.Allergen;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import org.greenrobot.greendao.database.Database;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Lobster on 05.03.18.
 */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class ProductRepositoryTest {
    private static final String TEST_ALLERGEN_TAG = "en:lupin";
    private static final String TEST_LANGUAGE_CODE = "es";
    private static final String TEST_ALLERGEN_NAME = "Altramuces";
    private static IProductRepository productRepository;

    @BeforeClass
    public static void cleanAllergens() {
        clearDatabase();
        productRepository = ProductRepository.getInstance();
        productRepository.saveAllergens(createAllergens());
    }

    private static void clearDatabase() {
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        Database db = daoSession.getDatabase();
        db.beginTransaction();
        try {
            daoSession.getAllergenDao().deleteAll();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("openfoodfacts.github.scrachx.openfood.repositories.ProductRepositoryTest", "error in transaction", e);
        } finally {
            db.endTransaction();
        }
    }

    @AfterClass
    public static void close() {
        clearDatabase();
    }

    @Test
    public void testGetAllergens() {
        List<Allergen> allergens = productRepository.getAllergens(false).blockingGet();

        assertNotNull(allergens);
        assertEquals(2, allergens.size());

        Allergen allergen = allergens.get(0);
        assertEquals(TEST_ALLERGEN_TAG, allergen.getTag());

        List<AllergenName> allergenNames = allergen.getNames();
        assertEquals(3, allergenNames.size());

        AllergenName allergenName = allergenNames.get(0);
        assertEquals(allergenName.getAllergenTag(), allergen.getTag());
        assertEquals(TEST_LANGUAGE_CODE, allergenName.getLanguageCode());
        assertEquals(TEST_ALLERGEN_NAME, allergenName.getName());
    }

    @Test
    public void testGetEnabledAllergens() {
        List<Allergen> allergens = productRepository.getEnabledAllergens();

        assertNotNull(allergens);
        assertEquals(1, allergens.size());
        assertEquals(TEST_ALLERGEN_TAG, allergens.get(0).getTag());
    }

    @Test
    public void testGetAllergensByEnabledAndLanguageCode() {
        List<AllergenName> enabledAllergenNames = productRepository.getAllergensByEnabledAndLanguageCode(true, TEST_LANGUAGE_CODE).blockingGet();
        List<AllergenName> notEnabledAllergenNames = productRepository.getAllergensByEnabledAndLanguageCode(false, TEST_LANGUAGE_CODE).blockingGet();

        assertNotNull(enabledAllergenNames);
        assertNotNull(notEnabledAllergenNames);

        assertEquals(1, enabledAllergenNames.size());
        assertEquals(1, notEnabledAllergenNames.size());

        assertEquals(TEST_ALLERGEN_NAME, enabledAllergenNames.get(0).getName());
        assertEquals("Molluschi", notEnabledAllergenNames.get(0).getName());
    }

    @Test
    public void testGetAllergensByLanguageCode() {
        List<AllergenName> allergenNames = productRepository.getAllergensByLanguageCode(TEST_LANGUAGE_CODE).blockingGet();

        assertNotNull(allergenNames);
        assertEquals(2, allergenNames.size());
    }

    private static List<Allergen> createAllergens() {
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
