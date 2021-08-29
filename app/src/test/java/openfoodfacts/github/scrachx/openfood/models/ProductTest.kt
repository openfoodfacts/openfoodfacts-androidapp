package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Tests for [Product]
 */
class ProductTest {
    @Test
    fun `Deserializes generic_name`() {
        val productJson = """{"generic_name": $htmlEscapedSingleQuoteJson}"""
        val product = deserialize(productJson)
        assertThat(product.genericName).isEqualTo(correctlyConvertedString)
    }

    @Test
    fun `Deserializes ingredients_text`() {
        val productJson = """{"ingredients_text": $htmlEscapedSingleQuoteJson}"""
        val product = deserialize(productJson)
        assertThat(product.ingredientsText).isEqualTo(correctlyConvertedString)
    }

    @Test
    fun `Deserializes product_name`() {
        val productJson = """{"product_name": $htmlEscapedSingleQuoteJson}"""
        val product = deserialize(productJson)
        assertThat(product.productName).isEqualTo(correctlyConvertedString)
    }

    @Test
    fun `Deserializes and insert spaces in stores`() {
        val productJason = """{"stores": "CVS,Waitrose,Flunch"}"""
        val product = deserialize(productJason)
        assertThat(product.stores).isEqualTo("CVS, Waitrose, Flunch")
    }

    @Test
    fun `Deserializes and inserts spaces in countries`() {
        val productJson = """{"countries": "US,France,Germany"}"""
        val product = deserialize(productJson)
        assertThat(product.countries).isEqualTo("US, France, Germany")
    }

    @Test
    fun `Deserializes and insert spaces in brands`() {
        val productJson = """{"brands": "Kellogg,Kharma,Dharma"}"""
        val product = deserialize(productJson)
        assertThat(product.brands).isEqualTo("Kellogg,Kharma,Dharma")
    }

    @Test
    fun `Deserializes and insert spaces in packaging`() {
        val productJson = """{"packaging": "Plastic Bottle,Keg,Glass Bottle"}"""
        val product = deserialize(productJson)
        assertThat(product.packaging).isEqualTo("Plastic Bottle, Keg, Glass Bottle")
    }

    @Test
    fun `Throws exception when deserializing bad escape sequences`() {
        val productJson = """{"generic_name": $badEscapeSequence}"""
        assertThrows(JsonMappingException::class.java) { deserialize(productJson) }
    }

    @Test
    fun `toString contains barcode and product name`() {
        val code = """0022343"""
        val productName = "Ice"
        val productJson = """{"packaging": "Plastic","product_name": "$productName","code": "$code"}"""
        val product = deserialize(productJson)
        assertThat(product.toString()).endsWith("[code=$code,productName=$productName,additional_properties={}]")
    }

    companion object {
        private const val htmlEscapedSingleQuoteJson = "\"Sally\\\\\'s\""
        private const val badEscapeSequence = "\"Sally\\\'s\""
        private const val correctlyConvertedString = "Sally's"

        private fun deserialize(json: String) = jacksonObjectMapper().readValue<Product>(json)
    }
}