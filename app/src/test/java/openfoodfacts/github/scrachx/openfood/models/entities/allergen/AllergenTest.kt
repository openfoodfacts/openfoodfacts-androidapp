package openfoodfacts.github.scrachx.openfood.models.entities.allergen

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException

class AllergenTest {
    lateinit var allergens: List<Allergen>

    @BeforeEach
    @Throws(IOException::class)
    fun setUp() {
        //language=JSON
        val json = """{
  "en:lupin": {
    "name": {
      "es": "Altramuces",
      "nl": "Lupine",
      "et": "Lupiin",
      "de": "Lupinen",
      "fi": "Lupiinit",
      "sv": "Lupin",
      "lt": "Lubinai",
      "mt": "Lupina",
      "da": "Lupin"
    }
  },
  "en:molluscs": {
    "name": {
      "it": "Molluschi",
      "fr": "Mollusques",
      "lv": "Gliemji",
      "pt": "Moluscos",
      "nl": "Weekdieren",
      "et": "Molluskid",
      "de": "Weichtiere",
      "es": "Moluscos",
      "ga": "Moilisc",
      "mt": "Molluski",
      "lt": "Moliuskai",
      "en": "Molluscs"
    }
  }
}"""
        allergens = jacksonObjectMapper().readValue<AllergensWrapper>(json).map()
    }

    @Test
    fun deserialization_success() {
        assertThat(allergens).hasSize(2)
    }

    @Test
    fun allergensWrapper_AllergenAreCorrectlyTagged() {
        val allergen = allergens[0]
        assertThat(allergen.tag).isEqualTo("en:lupin")
        assertThat(allergen.enabled).isFalse()
        assertThat(allergen.names).hasSize(9)
    }

    @Test
    fun allergensWrapper_SubElementsAreCorrectlyTagged() {
        val allergen = allergens[0]
        val allergenName = allergen.names[0]
        assertThat(allergenName.allergenTag).isEqualTo(allergen.tag)
        assertThat(allergenName.languageCode).isEqualTo("es")
        assertThat(allergenName.name).isEqualTo("Altramuces")
    }

}
