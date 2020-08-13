package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.*;

/**
 * Tests for {@link Product}
 */
@SuppressWarnings("UnnecessaryStringEscape")
public class ProductTest {
    private static final String htmlEscapedSingleQuoteJson = "\"Sally\\\\\'s\"";
    private static final String badEscapeSequence = "\"Sally\\\'s\"";
    private static final String correctlyConvertedString = "Sally's";

    @Test
    public void productStringConverter_convertsGenericName() throws IOException {
        String productJson = "{\"generic_name\": " + htmlEscapedSingleQuoteJson + "}";
        Product product = deserialize(productJson);
        assertThat(product.getGenericName()).isEqualTo(correctlyConvertedString);
    }

    private static Product deserialize(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, Product.class);
    }

    @Test
    public void productStringConverter_convertsIngredientsText() throws IOException {
        String productJson = "{\"ingredients_text\": " + htmlEscapedSingleQuoteJson + "}";
        Product product = deserialize(productJson);
        assertThat(product.getIngredientsText()).isEqualTo(correctlyConvertedString);
    }

    @Test
    public void productStringConverter_convertsProductName() throws IOException {
        String productJson = "{\"product_name\": " + htmlEscapedSingleQuoteJson + "}";
        Product product = deserialize(productJson);
        assertThat(product.getProductName()).isEqualTo(correctlyConvertedString);
    }

    @Test
    public void getStores_insertsSpacesAfterCommas() throws IOException {
        String productJason = "{\"stores\": \"CVS,Waitrose,Flunch\"}";
        Product product = deserialize(productJason);
        assertThat(product.getStores()).isEqualTo("CVS, Waitrose, Flunch");
    }

    @Test
    public void getCountries_insertsSpacesAfterCommas() throws IOException {
        String productJson = "{\"countries\": \"US,France,Germany\"}";
        Product product = deserialize(productJson);
        assertThat(product.getCountries()).isEqualTo("US, France, Germany");
    }

    @Test
    public void getBrands_insertsSpacesAfterCommas() throws IOException {
        String productJson = "{\"brands\": \"Kellogg,Kharma,Dharma\"}";
        Product product = deserialize(productJson);
        assertThat(product.getBrands()).isEqualTo("Kellogg, Kharma, Dharma");
    }

    @Test
    public void getPackaging_insertsSpacesAfterCommas() throws IOException {
        String productJson = "{\"packaging\": \"Plastic Bottle,Keg,Glass Bottle\"}";
        Product product = deserialize(productJson);
        assertThat(product.getPackaging()).isEqualTo("Plastic Bottle, Keg, Glass Bottle");
    }

    @Test
    public void productStringConverter_badEscapeSequence_throwsJsonMappingException() {
        String productJson = "{\"generic_name\": " + badEscapeSequence + "}";
        assertThrows(JsonMappingException.class, () -> deserialize(productJson));
    }

    @Test
    public void toString_addsCodeAndProductName() throws IOException {
        String productJson = "{\"packaging\": \"Plastic\"," +
            "\"product_name\": \"Ice\"," +
            "\"code\": \"0022343\"}";
        Product product = deserialize(productJson);
        assertThat(product.toString().endsWith("[code=0022343,productName=Ice,additional_properties={}]")).isTrue();
    }
}
