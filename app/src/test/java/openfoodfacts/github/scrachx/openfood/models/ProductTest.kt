package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.truth.Truth
import org.junit.Assert
import org.junit.Test
import org.junit.function.ThrowingRunnable
import java.io.IOException

/**
 * Tests for [Product]
 */
class ProductTest {
    @Test
    @Throws(IOException::class)
    fun productStringConverter_convertsGenericName() {
        val productJson = """{"generic_name": $htmlEscapedSingleQuoteJson}"""
        val product = deserialize(productJson)
        Truth.assertThat(product.genericName).isEqualTo(correctlyConvertedString)
    }

    @Test
    @Throws(IOException::class)
    fun productStringConverter_convertsIngredientsText() {
        val productJson = """{"ingredients_text": $htmlEscapedSingleQuoteJson}"""
        val product = deserialize(productJson)
        Truth.assertThat(product.ingredientsText).isEqualTo(correctlyConvertedString)
    }

    @Test
    @Throws(IOException::class)
    fun productStringConverter_convertsProductName() {
        val productJson = """{"product_name": $htmlEscapedSingleQuoteJson}"""
        val product = deserialize(productJson)
        Truth.assertThat(product.productName).isEqualTo(correctlyConvertedString)
    }

    @Throws(IOException::class)
    @Test
    fun getStores_insertsSpacesAfterCommas(): Unit {
        val productJason = """{"stores": "CVS,Waitrose,Flunch"}"""
        val product = deserialize(productJason)
        Truth.assertThat(product.stores).isEqualTo("CVS, Waitrose, Flunch")
    }

    @Throws(IOException::class)
    @Test
    fun getCountries_insertsSpacesAfterCommas(): Unit {
        val productJson = """{"countries": "US,France,Germany"}"""
        val product = deserialize(productJson)
        Truth.assertThat(product.countries).isEqualTo("US, France, Germany")
    }

    @Throws(IOException::class)
    @Test
    fun getBrands_insertsSpacesAfterCommas(): Unit {
        val productJson = """{"brands": "Kellogg,Kharma,Dharma"}"""
        val product = deserialize(productJson)
        Truth.assertThat(product.brands).isEqualTo("Kellogg, Kharma, Dharma")
    }

    @Throws(IOException::class)
    @Test
    fun getPackaging_insertsSpacesAfterCommas(): Unit {
        val productJson = """{"packaging": "Plastic Bottle,Keg,Glass Bottle"}"""
        val product = deserialize(productJson)
        Truth.assertThat(product.packaging).isEqualTo("Plastic Bottle, Keg, Glass Bottle")
    }

    @Test
    fun productStringConverter_badEscapeSequence_throwsJsonMappingException() {
        val productJson = """{"generic_name": $badEscapeSequence}"""
        Assert.assertThrows<JsonMappingException>(JsonMappingException::class.java, ThrowingRunnable { deserialize(productJson) })
    }

    @Test
    @Throws(IOException::class)
    fun toString_addsCodeAndProductName() {
        val productJson = """{"packaging": "Plastic","product_name": "Ice","code": "0022343"}"""
        val product = deserialize(productJson)
        Truth.assertThat(product.toString().endsWith("[code=0022343,productName=Ice,additional_properties={}]")).isTrue()
    }

    companion object {
        private const val htmlEscapedSingleQuoteJson = "\"Sally\\\\\'s\""
        private const val badEscapeSequence = "\"Sally\\\'s\""
        private const val correctlyConvertedString = "Sally's"

        @Throws(IOException::class)
        private fun deserialize(json: String): Product {
            val mapper = jacksonObjectMapper()
            return mapper.readValue(json, Product::class.java)
        }
    }
}