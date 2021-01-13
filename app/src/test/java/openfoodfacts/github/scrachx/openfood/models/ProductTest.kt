package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.truth.Truth.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.function.ThrowingRunnable
import java.io.IOException

/**
 * Tests for [Product]
 */
class ProductTest {
    @Test
    fun productStringConverter_convertsGenericName() {
        val productJson = """{"generic_name": $htmlEscapedSingleQuoteJson}"""
        val product = deserialize(productJson)
        assertThat(product.genericName).isEqualTo(correctlyConvertedString)
    }

    @Test
    fun productStringConverter_convertsIngredientsText() {
        val productJson = """{"ingredients_text": $htmlEscapedSingleQuoteJson}"""
        val product = deserialize(productJson)
        assertThat(product.ingredientsText).isEqualTo(correctlyConvertedString)
    }

    @Test
    fun productStringConverter_convertsProductName() {
        val productJson = """{"product_name": $htmlEscapedSingleQuoteJson}"""
        val product = deserialize(productJson)
        assertThat(product.productName).isEqualTo(correctlyConvertedString)
    }

    @Test
    fun getStores_insertsSpacesAfterCommas() {
        val productJason = """{"stores": "CVS,Waitrose,Flunch"}"""
        val product = deserialize(productJason)
        assertThat(product.stores).isEqualTo("CVS, Waitrose, Flunch")
    }

    @Test
    fun getCountries_insertsSpacesAfterCommas() {
        val productJson = """{"countries": "US,France,Germany"}"""
        val product = deserialize(productJson)
        assertThat(product.countries).isEqualTo("US, France, Germany")
    }

    @Test
    fun getBrands_insertsSpacesAfterCommas() {
        val productJson = """{"brands": "Kellogg,Kharma,Dharma"}"""
        val product = deserialize(productJson)
        assertThat(product.brands).isEqualTo("Kellogg,Kharma,Dharma")
    }

    @Test
    fun getPackaging_insertsSpacesAfterCommas() {
        val productJson = """{"packaging": "Plastic Bottle,Keg,Glass Bottle"}"""
        val product = deserialize(productJson)
        assertThat(product.packaging).isEqualTo("Plastic Bottle, Keg, Glass Bottle")
    }

    @Test
    fun productStringConverter_badEscapeSequence_throwsJsonMappingException() {
        val productJson = """{"generic_name": $badEscapeSequence}"""
        Assert.assertThrows<JsonMappingException>(JsonMappingException::class.java,
                ThrowingRunnable { deserialize(productJson) })
    }

    @Test
    @Throws(IOException::class)
    fun toString_addsCodeAndProductName() {
        val productJson = """{"packaging": "Plastic","product_name": "Ice","code": "0022343"}"""
        val product = deserialize(productJson)
        assertThat(product.toString()).endsWith("[code=0022343,productName=Ice,additional_properties={}]")
    }

    companion object {
        private const val htmlEscapedSingleQuoteJson = "\"Sally\\\\\'s\""
        private const val badEscapeSequence = "\"Sally\\\'s\""
        private const val correctlyConvertedString = "Sally's"
        private val mapper = jacksonObjectMapper()

        private fun deserialize(json: String) = mapper.readValue<Product>(json)
    }
}