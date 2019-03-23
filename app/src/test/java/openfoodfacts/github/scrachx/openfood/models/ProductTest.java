package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Tests for {@link Product}
 */
public class ProductTest {

    private static final String htmlEscapedSingleQuoteJson = "\"Sally\\\\\'s\"";
    private static final String badEscapeSequence = "\"Sally\\\'s\"";
    private static final String correctlyConvertedString = "Sally's";

    @Test
    public void productStringConverter_convertsGenericName() throws IOException {
        String productJson = "{\"generic_name\": " + htmlEscapedSingleQuoteJson + "}";
        Product product = deserialize(productJson);
        assertEquals(correctlyConvertedString, product.getGenericName());
    }

    @Test(expected = JsonMappingException.class)
    public void productStringConverter_badEscapeSequence_throwsJsonMappingException()
            throws Exception {
        String productJson = "{\"generic_name\": " + badEscapeSequence + "}";
        Product product = deserialize(productJson);
    }

    @Test
    public void productStringConverter_convertsIngredientsText() throws IOException {
        String productJson = "{\"ingredients_text\": " + htmlEscapedSingleQuoteJson + "}";
        Product product = deserialize(productJson);
        assertEquals(correctlyConvertedString, product.getIngredientsText());
    }

    @Test
    public void productStringConverter_convertsProductName() throws IOException {
        String productJson = "{\"product_name\": " + htmlEscapedSingleQuoteJson + "}";
        Product product = deserialize(productJson);
        assertEquals(correctlyConvertedString, product.getProductName());
    }

    @Test
    public void getStores_insertsSpacesAfterCommas() throws IOException {
        String productJason = "{\"stores\": \"CVS,Waitrose,Flunch\"}";
        Product product = deserialize(productJason);
        assertEquals("CVS, Waitrose, Flunch", product.getStores());
    }

    @Test
    public void getCountries_insertsSpacesAfterCommas() throws IOException {
        String productJson = "{\"countries\": \"US,France,Germany\"}";
        Product product = deserialize(productJson);
        assertEquals("US, France, Germany", product.getCountries());
    }

    @Test
    public void getBrands_insertsSpacesAfterCommas() throws IOException {
        String productJson = "{\"brands\": \"Kellogg,Kharma,Dharma\"}";
        Product product = deserialize(productJson);
        assertEquals("Kellogg, Kharma, Dharma", product.getBrands());
    }

    @Test
    public void getPackaging_insertsSpacesAfterCommas() throws IOException {
        String productJson = "{\"packaging\": \"Plastic Bottle,Keg,Glass Bottle\"}";
        Product product = deserialize(productJson);
        assertEquals("Plastic Bottle, Keg, Glass Bottle", product.getPackaging());
    }

    @Test
    public void toString_addsCodeAndProductName() throws IOException {
        String productJson = "{\"packaging\": \"Plastic\"," +
                "\"product_name\": \"Ice\"," +
                "\"code\": \"0022343\"}";
        Product product = deserialize(productJson);
        assertTrue(product.toString().endsWith("[code=0022343,productName=Ice,additional_properties={}]"));
    }

    private Product deserialize(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, Product.class);
    }
}
