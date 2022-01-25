package openfoodfacts.github.scrachx.openfood.models.entities.allergen

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for [AllergenResponse]
 */
class AllergenResponseTest {
    lateinit var nameMap: MutableMap<String, String>
    @BeforeEach
    fun setUp() {
        nameMap = mutableMapOf()
    }

    @Test
    fun map_returnsMappedAllergen() {
        nameMap[LANGUAGE_CODE_ENGLISH] = PEANUTS_EN
        nameMap[LANGUAGE_CODE_FRENCH] = PEANUTS_FR
        val allergenResponse = AllergenResponse(UNIQUE_ALLERGEN_ID_1, nameMap)
        val allergen = allergenResponse.map()

        // TODO: expected this to be the allergen ID but a string to constructor actually makes it
        // the tag. Should update AllergenResponse to fix that.
        assertThat(allergen.tag).isEqualTo(UNIQUE_ALLERGEN_ID_1)
        assertThat(allergen.names).hasSize(2)
        assertThat(allergen.names[0].allergenTag).isEqualTo(UNIQUE_ALLERGEN_ID_1)
        assertThat(allergen.names[0].name).isEqualTo(PEANUTS_EN)
        assertThat(allergen.names[0].languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(allergen.names[1].allergenTag).isEqualTo(UNIQUE_ALLERGEN_ID_1)
        assertThat(allergen.names[1].name).isEqualTo(PEANUTS_FR)
        assertThat(allergen.names[1].languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
    }

    @Test
    fun map_returnsMappedAllergenWithWikiDataCode() {
        val wikiDataCode = "Q12345"
        val allergenResponse = AllergenResponse(UNIQUE_ALLERGEN_ID_1, nameMap, wikiDataCode)
        val allergen = allergenResponse.map()
        assertThat(allergen.wikiDataId).isEqualTo(wikiDataCode)
    }
}