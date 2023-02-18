package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * Tests for [ProductIngredient]
 */

class ProductIngredientTest {
    @Test
    fun `test api response without additional properties`() {
        val response = """
        {"code":"3336413247198","product":{"ingredients":[{"id":"en:water","percent_estimate":68,"percent_max":68,"percent_min":68,"rank":1,"text":"Eau","vegan":"yes","vegetarian":"yes"},{"id":"en:buckwheat-flour","labels":"en:organic","percent":31,"percent_estimate":31,"percent_max":31,"percent_min":31,"rank":2,"text":"farine de blé noir","vegan":"yes","vegetarian":"yes"},{"id":"en:salt-from-guerande","percent":1,"percent_estimate":1,"percent_max":1,"percent_min":1,"rank":3,"text":"sel de Guérande","vegan":"yes","vegetarian":"yes"}]},"status":1,"status_verbose":"product found"}
        """
        val productState = objectMapper.readValue<ProductState>(response)

        assertThat(productState.product).isNotNull()
        val product = productState.product!!

        assertThat(product.ingredients).hasSize(3)
        assertThat(product.ingredients[0].id).isEqualTo("en:water")
        assertThat(product.ingredients[0].percentEstimate).isEqualTo(68)
        assertThat(product.ingredients[0].percentMax).isEqualTo(68)
        assertThat(product.ingredients[0].percentMin).isEqualTo(68)
        assertThat(product.ingredients[0].rank).isEqualTo(1)
        assertThat(product.ingredients[0].text).isEqualTo("Eau")
        assertThat(product.ingredients[0].vegan).isEqualTo("yes")
        assertThat(product.ingredients[0].vegetarian).isEqualTo("yes")
        assertThat(product.ingredients[0].additionalProperties).isEmpty()
    }

    @Test
    fun test2() {
        val response = """
            {"code":"8000139929472","product":{"ingredients":[{"has_sub_ingredients":"yes","id":"en:ricotta","percent":25,"percent_estimate":25,"percent_max":25,"percent_min":25,"rank":1,"text":"Ricotta","vegan":"no","vegetarian":"maybe"},{"id":"en:wheat-flour","percent_estimate":19,"percent_max":25,"percent_min":13,"rank":2,"text":"farine de _blé_","vegan":"yes","vegetarian":"yes"},{"id":"en:durum-wheat-semolina","percent_estimate":19,"percent_max":25,"percent_min":13,"rank":3,"text":"semoule de _blé_ dur","vegan":"yes","vegetarian":"yes"},{"id":"en:spinach","percent":13,"percent_estimate":13,"percent_max":13,"percent_min":13,"rank":4,"text":"épinards","vegan":"yes","vegetarian":"yes"},{"has_sub_ingredients":"yes","id":"en:egg","percent":10,"percent_estimate":10,"percent_max":10,"percent_min":10,"rank":5,"text":"_œufs_","vegan":"no","vegetarian":"yes"},{"has_sub_ingredients":"yes","id":"en:breadcrumbs","percent_estimate":5.01785714285714,"percent_max":9.75,"percent_min":0.285714285714286,"rank":6,"text":"chapelure","vegan":"maybe","vegetarian":"maybe"},{"id":"en:water","percent_estimate":3.87142857142857,"percent_max":7.74285714285714,"percent_min":0,"rank":7,"text":"eau","vegan":"yes","vegetarian":"yes"},{"id":"en:parmigiano-reggiano","percent_estimate":2.55535714285715,"percent_max":6.45238095238095,"percent_min":0,"rank":8,"text":"_fromage_ Parmigiano Reggiano DOP","vegan":"no","vegetarian":"maybe"},{"id":"en:butter","percent_estimate":1.27767857142857,"percent_max":5.53061224489796,"percent_min":0,"rank":9,"text":"_beurre_","vegan":"no","vegetarian":"yes"},{"id":"fr:sel-de-cuisine-oignons","percent_estimate":0.638839285714283,"percent_max":4.83928571428571,"percent_min":0,"rank":10,"text":"sel de cuisine oignons"},{"id":"en:pepper","percent_estimate":0.319419642857142,"percent_max":4.3015873015873,"percent_min":0,"rank":11,"text":"poivre","vegan":"yes","vegetarian":"yes"},{"id":"en:nutmeg-nut","percent_estimate":0.319419642857142,"percent_max":3.87142857142857,"percent_min":0,"rank":12,"text":"noix de muscade","vegan":"yes","vegetarian":"yes"},{"id":"en:cheese","percent_estimate":25,"percent_max":25,"percent_min":25,"text":"_fromage_","vegan":"no","vegetarian":"maybe"},{"id":"fr:d-elevage-en-plein-air","percent_estimate":10,"percent_max":10,"percent_min":10,"text":"d'élevage en plein air"},{"id":"en:wheat-flour","percent_estimate":2.50892857142857,"percent_max":9.75,"percent_min":0,"text":"farine de _blé_","vegan":"yes","vegetarian":"yes"},{"id":"en:yeast","percent_estimate":1.25446428571429,"percent_max":4.875,"percent_min":0,"text":"levure","vegan":"yes","vegetarian":"yes"},{"id":"en:salt","percent_estimate":1.25446428571429,"percent_max":3.33333333333333,"percent_min":0,"text":"sel de cuisine","vegan":"yes","vegetarian":"yes"}]},"status":1,"status_verbose":"product found"}
        """

        val productState = objectMapper.readValue<ProductState>(response)

        assertThat(productState.product).isNotNull()
        val product = productState.product!!

        assertThat(product.ingredients).hasSize(17)

        assertThat(product.ingredients[0].id).isEqualTo("en:ricotta")
        assertThat(product.ingredients[0].percentEstimate).isEqualTo(25)
        assertThat(product.ingredients[0].percentMax).isEqualTo(25)
        assertThat(product.ingredients[0].percentMin).isEqualTo(25)
        assertThat(product.ingredients[0].rank).isEqualTo(1)
        assertThat(product.ingredients[0].text).isEqualTo("Ricotta")
        assertThat(product.ingredients[0].vegan).isEqualTo("no")
        assertThat(product.ingredients[0].vegetarian).isEqualTo("maybe")
        assertThat(product.ingredients[0].additionalProperties).isEmpty()
    }

    companion object {
        lateinit var objectMapper: ObjectMapper

        @BeforeAll
        @JvmStatic
        fun setup() {
            objectMapper = jacksonObjectMapper()
        }
    }
}