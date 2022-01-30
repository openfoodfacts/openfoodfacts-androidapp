package openfoodfacts.github.scrachx.openfood.models.entities.allergen

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_GERMAN
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for [AllergensWrapper]
 */
class AllergensWrapperTest {
    lateinit var allergens: List<Allergen>
    lateinit var allergen1: Allergen
    lateinit var allergen2: Allergen

    @BeforeEach
    fun setUp() {

        val nameMap1 = mapOf(
                LANGUAGE_CODE_ENGLISH to PEANUTS_EN,
                LANGUAGE_CODE_FRENCH to PEANUTS_FR
        )

        // See AllergenResponseTest for the naming issue with UNIQUE_ALLERGEN_ID_1 and 2
        val allergenResponse1 = AllergenResponse(UNIQUE_ALLERGEN_ID_1, nameMap1)

        val nameMap2 = mapOf(
                LANGUAGE_CODE_ENGLISH to STRAWBERRY_EN,
                LANGUAGE_CODE_GERMAN to STRAWBERRY_DE
        )
        val allergenResponse2 = AllergenResponse(UNIQUE_ALLERGEN_ID_2, nameMap2)

        val allergensWrapper = AllergensWrapper(listOf(allergenResponse1, allergenResponse2))

        allergens = allergensWrapper.map()
        allergen1 = allergens[0]
        allergen2 = allergens[1]
    }

    @Test
    fun allergensWrapper_CreatesOneListPerAllergen() {
        assertThat(allergens).hasSize(2)
    }

    @Test
    fun map_returnsListOfCorrectlyMappedAllergens() {
        assertThat(allergen1.tag).isEqualTo(UNIQUE_ALLERGEN_ID_1)
        assertThat(allergen1.names).hasSize(2)
        assertThat(allergen2.tag).isEqualTo(UNIQUE_ALLERGEN_ID_2)
        assertThat(allergen2.names).hasSize(2)
    }

    @Test
    fun map_returnsListOfCorrectlyMappedSubAllergens_Tag() {
        assertThat(allergen1.names[0].allergenTag).isEqualTo(UNIQUE_ALLERGEN_ID_1)
        assertThat(allergen1.names[1].allergenTag).isEqualTo(UNIQUE_ALLERGEN_ID_1)
        assertThat(allergen2.names[0].allergenTag).isEqualTo(UNIQUE_ALLERGEN_ID_2)
        assertThat(allergen2.names[1].allergenTag).isEqualTo(UNIQUE_ALLERGEN_ID_2)
    }

    @Test
    fun map_returnsListOfCorrectlyMappedSubAllergens_LanguageCode() {
        assertThat(allergen1.names[0].languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(allergen1.names[1].languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(allergen2.names[0].languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(allergen2.names[1].languageCode).isEqualTo(LANGUAGE_CODE_GERMAN)
    }

    @Test
    fun map_returnsListOfCorrectlyMappedSubAllergens_Names() {
        assertThat(allergen1.names[0].name).isEqualTo(PEANUTS_EN)
        assertThat(allergen1.names[1].name).isEqualTo(PEANUTS_FR)
        assertThat(allergen2.names[0].name).isEqualTo(STRAWBERRY_EN)
        assertThat(allergen2.names[1].name).isEqualTo(STRAWBERRY_DE)
    }
}