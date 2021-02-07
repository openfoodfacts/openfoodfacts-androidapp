package openfoodfacts.github.scrachx.openfood.repositories

import android.util.Log
import androidx.test.filters.SmallTest
import openfoodfacts.github.scrachx.openfood.app.OFFApplication.Companion.daoSession
import openfoodfacts.github.scrachx.openfood.app.OFFApplication.Companion.instance
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.Allergen
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Test
import java.util.*

/**
 * Created by Lobster on 05.03.18.
 */
@SmallTest
class ProductRepositoryTest {
    @Test
    fun testGetAllergens() {
        val mSettings = instance.getSharedPreferences("prefs", 0)
        val isDownloadActivated = mSettings.getBoolean(Taxonomy.ALLERGEN.downloadActivatePreferencesId, false)
        val allergens = ProductRepository.reloadAllergensFromServer().blockingGet()
        assertNotNull(allergens)
        if (!isDownloadActivated) {
            assertEquals(0, allergens.size.toLong())
        } else {
            assertEquals(2, allergens.size.toLong())
            val allergen = allergens[0]
            assertEquals(TEST_ALLERGEN_TAG, allergen.tag)
            val allergenNames = allergen.names
            assertEquals(3, allergenNames.size.toLong())
            val allergenName = allergenNames[0]
            assertEquals(allergenName.allergenTag, allergen.tag)
            assertEquals(TEST_LANGUAGE_CODE, allergenName.languageCode)
            assertEquals(TEST_ALLERGEN_NAME, allergenName.name)
        }
    }

    @Test
    fun testGetEnabledAllergens() {
        val allergens = ProductRepository.getEnabledAllergens()
        assertNotNull(allergens)
        assertEquals(1, allergens.size.toLong())
        assertEquals(TEST_ALLERGEN_TAG, allergens[0].tag)
    }

    @Test
    fun testGetAllergensByEnabledAndLanguageCode() {
        val enabledAllergenNames = ProductRepository.getAllergensByEnabledAndLanguageCode(true, TEST_LANGUAGE_CODE).blockingGet()
        val notEnabledAllergenNames = ProductRepository.getAllergensByEnabledAndLanguageCode(false, TEST_LANGUAGE_CODE).blockingGet()
        assertNotNull(enabledAllergenNames)
        assertNotNull(notEnabledAllergenNames)
        assertEquals(1, enabledAllergenNames.size.toLong())
        assertEquals(1, notEnabledAllergenNames.size.toLong())
        assertEquals(TEST_ALLERGEN_NAME, enabledAllergenNames[0].name)
        assertEquals("Molluschi", notEnabledAllergenNames[0].name)
    }

    @Test
    fun testGetAllergensByLanguageCode() {
        val allergenNames = ProductRepository.getAllergensByLanguageCode(TEST_LANGUAGE_CODE).blockingGet()
        assertNotNull(allergenNames)
        assertEquals(2, allergenNames.size.toLong())
    }

    companion object {
        private const val TEST_ALLERGEN_TAG = "en:lupin"
        private const val TEST_LANGUAGE_CODE = "es"
        private const val TEST_ALLERGEN_NAME = "Altramuces"

        @BeforeClass
        fun cleanAllergens() {
            clearDatabase()
            ProductRepository.saveAllergens(createAllergens())
        }

        @AfterClass
        fun close() = clearDatabase()

        private fun clearDatabase() {
            val daoSession = daoSession
            val db = daoSession.database
            db.beginTransaction()
            try {
                daoSession.allergenDao.deleteAll()
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                Log.e(this::class.simpleName, "Error in transaction.", e)
            } finally {
                db.endTransaction()
            }
        }

        private fun createAllergens(): List<Allergen> {
            val allergen1 = Allergen(TEST_ALLERGEN_TAG, ArrayList()).apply {
                enabled = true
                names.add(AllergenName(tag, TEST_LANGUAGE_CODE, TEST_ALLERGEN_NAME))
                names.add(AllergenName(tag, "bg", "Лупина"))
                names.add(AllergenName(tag, "fr", "Lupin"))
            }

            val allergen2 = Allergen("en:molluscs", ArrayList()).apply {
                names.add(AllergenName(tag, TEST_LANGUAGE_CODE, "Molluschi"))
                names.add(AllergenName(tag, "en", "Mollusques"))
            }

            return listOf(allergen1, allergen2)
        }
    }
}