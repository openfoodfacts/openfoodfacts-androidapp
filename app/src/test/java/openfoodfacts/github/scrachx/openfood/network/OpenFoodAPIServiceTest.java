package openfoodfacts.github.scrachx.openfood.network;


import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService.PRODUCT_API_COMMENT;
import static org.junit.Assert.assertTrue;

public class OpenFoodAPIServiceTest implements APIUtils {

    private OpenFoodAPIService serviceRead;
    private OpenFoodAPIService serviceWrite;

    @Before
    public void setUp() throws Exception {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = HttpClientBuilder();

        serviceRead = new Retrofit.Builder()
                .baseUrl(APIUtils.GET_API)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(httpClient)
                .build()
                .create(OpenFoodAPIService.class);

        OkHttpClient httpClientWithAuth = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();

                        Request.Builder requestBuilder = original.newBuilder()
                                // not works Base64.encodeToString("off:off".getBytes(), Base64.NO_WRAP);
                                .header("Authorization", "Basic b2ZmOm9mZg==")
                                .header("Accept", "application/json")
                                .method(original.method(), original.body());

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                }).build();

        serviceWrite = new Retrofit.Builder()
                .baseUrl("http://world.openfoodfacts.net")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(httpClientWithAuth)
                .build()
                .create(OpenFoodAPIService.class);
    }

    @Test
    public void byLanguage() throws Exception {
        Response<Search> searchResponse = serviceRead.byLanguage("italian").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byLabel() throws Exception {
        Response<Search> searchResponse = serviceRead.byLabel("utz-certified").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byCategory() throws Exception {
        Response<Search> searchResponse = serviceRead.byCategory("baby-foods").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byState() throws Exception {
        Response<Search> searchResponse = serviceRead.byState("complete").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byPackaging() throws Exception {
        Response<Search> searchResponse = serviceRead.byPackaging("cardboard").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byBrand() throws Exception {
        Response<Search> searchResponse = serviceRead.byBrand("monoprix").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byPurchasePlace() throws Exception {
        Response<Search> searchResponse = serviceRead.byPurchasePlace("marseille-5").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byStore() throws Exception {
        Response<Search> searchResponse = serviceRead.byStore("super-u").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byCountry() throws Exception {
        Response<Search> searchResponse = serviceRead.byCountry("france").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byIngredient() throws Exception {
        Response<Search> searchResponse = serviceRead.byIngredient("sucre").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byTrace() throws Exception {
        Response<Search> searchResponse = serviceRead.byIngredient("eggs").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void getProduct_notFound() throws Exception {
        String barcode = "457457457";
        Response<State> response = serviceRead.getFullProductByBarcode(barcode,Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)).execute();

        assertTrue(response.isSuccessful());

        assertEquals(0, response.body().getStatus());
        assertEquals("product not found", response.body().getStatusVerbose());
        assertEquals(barcode, response.body().getCode());
    }
/*
    @Test
    public void saveImage_noImageFile_ko() throws IOException {

        File outputFile = File.createTempFile("prefix", "png", new File("/"));

        ProductImage image = new ProductImage("01010101010101", ProductImageField.FRONT, outputFile);
        Map<String, RequestBody> imgMap = new HashMap<>();
        imgMap.put("code", image.getUniqueAllergenID());
        imgMap.put("imagefield", image.getField());
        imgMap.put("imgupload_front\"; filename=\"front_fr.png\"", image.getImguploadFront());
        imgMap.put("imgupload_ingredients\"; filename=\"ingredients_fr.png\"", image.getImguploadIngredients());
        imgMap.put("imgupload_nutrition\"; filename=\"nutrition_fr.png\"", image.getImguploadNutrition());
        imgMap.put("imgupload_other\"; filename=\"other_fr.png\"", image.getImguploadOther());

        Response<JsonNode> response = serviceWrite.saveImage(imgMap).execute();

        assertTrue(response.isSuccess());

        assertThatJson(response.body())
                .node("status")
                    .isEqualTo("status not ok");
    }
*/
    @Test
    public void post_product() throws IOException {
        SendProduct product = new SendProduct();
        product.setBarcode("978020137962");
        product.setName("coca");
        product.setBrands("auchan");
        product.setWeight("300");
        product.setWeight_unit("g");
        product.setLang("fr");

//        Response<State> execute = serviceWrite.saveProduct(product).execute();
        Response<State> execute = serviceWrite.saveProduct(product.getBarcode(), product.getLang(), product.getName(), product.getBrands(), product.getQuantity(), null, null, PRODUCT_API_COMMENT).execute();

        assertTrue(execute.isSuccessful());

        State body = execute.body();
        assertEquals(body.getStatus(), 1);
        assertEquals(body.getStatusVerbose(), "fields saved");

        Response<State> response = serviceWrite.getFullProductByBarcode(product.getBarcode(), Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)).execute();
        Product savedProduct = response.body().getProduct();
        assertEquals(product.getName(), savedProduct.getProductName());
        assertEquals(product.getBrands(), savedProduct.getBrands());
        assertTrue(savedProduct.getBrandsTags().contains(product.getBrands()));
        assertEquals(product.getWeight() + " " + product.getWeight_unit(), savedProduct.getQuantity());
    }

    @Test
    public void getProductByTrace_eggs_productsFound() throws Exception {
        Response<Search> response = serviceRead.byTrace("eggs").execute();
        assertProductsFound(response);
    }

    @Test
    public void getProductByPackagerCode_emb35069c_productsFound() throws Exception {
        Response<Search> response = serviceRead.byPackagerCode("emb-35069c").execute();
        assertProductsFound(response);
    }

    @Test
    public void getProductByNutritionGrade_a_productsFound() throws Exception {
        Response<Search> res = serviceRead.byNutritionGrade("a").execute();
        assertProductsFound(res);
    }

    @Test
    public void getProductByCity_Paris_noProductFound() throws Exception {
        Response<Search> response = serviceRead.byCity("paris").execute();
        assertNoProductsFound(response);
    }

    @Test
    public void getProductByAdditive_e301_productsFound() throws Exception {
        Response<Search> response = serviceRead.byAdditive("e301-sodium-ascorbate").execute();
        assertProductsFound(response);
    }

    private void assertProductsFound(Response<Search> response) {
        assertTrue(response.isSuccessful());
        Search search = response.body();
        List<Product> products = search.getProducts();
        assertNotNull(products);
        assertTrue(Integer.valueOf(search.getCount()) > 0);
        assertFalse(products.isEmpty());
    }
    private void assertNoProductsFound(Response<Search> response) {
        assertTrue(response.isSuccessful());
        Search search = response.body();
        List<Product> products = search.getProducts();
        assertTrue(products.isEmpty());
        assertTrue(Integer.valueOf(search.getCount()) == 0);
    }
}
