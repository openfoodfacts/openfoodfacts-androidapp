package openfoodfacts.github.scrachx.openfood.network;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

public class ProductsAPITest {
    /**
     * We need to use auth because we use world.openfoodfacts.dev
     */
    private static ProductsAPI devClientWithAuth;
    private static final String DEV_API = "https://world.openfoodfacts.dev";

    @BeforeClass
    public static void setupClient() {
        OkHttpClient httpClientWithAuth = new OkHttpClient.Builder()
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(chain -> {
                final Request origReq = chain.request();

                Request request = origReq.newBuilder()
                    .header("Authorization", "Basic b2ZmOm9mZg==")
                    .header("Accept", "application/json")
                    .method(origReq.method(), origReq.body()).build();

                return chain.proceed(request);
            })
            .build();

        devClientWithAuth = new Retrofit.Builder()
            .baseUrl(DEV_API)
            .addConverterFactory(JacksonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClientWithAuth)
            .build()
            .create(ProductsAPI.class);
    }

    @Test
    public void byLanguage() throws Exception {
        Response<Search> searchResponse = devClientWithAuth.byLanguage("italian").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byLabel() throws Exception {
        Response<Search> searchResponse = devClientWithAuth.byLabel("utz-certified").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byCategory() throws Exception {
        Response<Search> searchResponse = devClientWithAuth.byCategory("baby-foods").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byState() throws Exception {
        String fieldsToFetchFacets = "brands,product_name,image_small_url,quantity,nutrition_grades_tags";
        Response<Search> searchResponse = devClientWithAuth.byState("complete", fieldsToFetchFacets).execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byPackaging() throws Exception {
        Response<Search> searchResponse = devClientWithAuth.byPackaging("cardboard").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byBrand() throws Exception {
        Response<Search> searchResponse = devClientWithAuth.byBrand("monoprix").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byPurchasePlace() throws Exception {
        Response<Search> searchResponse = devClientWithAuth.byPurchasePlace("marseille-5").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byStore() throws Exception {
        Response<Search> searchResponse = devClientWithAuth.byStore("super-u").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byCountry() throws Exception {
        Response<Search> searchResponse = devClientWithAuth.byCountry("france").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byIngredient() throws Exception {
        Response<Search> searchResponse = devClientWithAuth.byIngredient("sucre").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void byTrace() throws Exception {
        Response<Search> searchResponse = devClientWithAuth.byIngredient("eggs").execute();

        assertNotNull(searchResponse);
        Search search = searchResponse.body();
        assertNotNull(search);
        assertNotNull(search.getProducts());
    }

    @Test
    public void getProduct_notFound() throws Exception {
        String barcode = "457457457";
        Response<State> response = devClientWithAuth.getProductByBarcode(barcode, "code", Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)).execute();

        assertTrue(response.isSuccessful());

        assertEquals(0, response.body().getStatus());
        assertEquals("product not found", response.body().getStatusVerbose());
        assertEquals(barcode, response.body().getCode());
    }

    @Test
    public void post_product() throws IOException {
        SendProduct product = new SendProduct();

        product.setBarcode("1234567890");
        product.setName("ProductName");
        product.setBrands("productbrand");
        product.setWeight("123");
        product.setWeight_unit("g");
        product.setLang("en");

        Map<String, String> productDetails = new HashMap<String, String>() {{
            put("lang", product.getLang());
            put("product_name", product.getName());
            put("brands", product.getBrands());
            put("quantity", product.getQuantity());
        }};

        State body = devClientWithAuth
            .saveProductSingle(product.getBarcode(), productDetails, OpenFoodAPIClient.getCommentToUpload())
            .blockingGet();

        assertEquals(body.getStatus(), 1);
        assertEquals(body.getStatusVerbose(), "fields saved");

        String fields = "product_name,brands,brands_tags,quantity";

        Response<State> response = devClientWithAuth.getProductByBarcode(
            product.getBarcode(),
            fields,
            Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
        ).execute();

        Product savedProduct = response.body().getProduct();
        assertEquals(product.getName(), savedProduct.getProductName());
        assertEquals(product.getBrands(), savedProduct.getBrands());
        assertTrue(savedProduct.getBrandsTags().contains(product.getBrands()));
        assertEquals(product.getWeight() + " " + product.getWeight_unit(), savedProduct.getQuantity());
    }

    @Test
    public void getProductByTrace_eggs_productsFound() throws Exception {
        Response<Search> response = devClientWithAuth.byTrace("eggs").execute();
        assertProductsFound(response);
    }

    @Test
    public void getProductByPackagerCode_emb35069c_productsFound() throws Exception {
        Response<Search> response = devClientWithAuth.byPackagerCode("emb-35069c").execute();
        assertProductsFound(response);
    }

    @Test
    public void getProductByNutritionGrade_a_productsFound() throws Exception {
        Response<Search> res = devClientWithAuth.byNutritionGrade("a").execute();
        assertProductsFound(res);
    }

    @Test
    public void getProductByCity_Paris_noProductFound() throws Exception {
        Response<Search> response = devClientWithAuth.byCity("paris").execute();
        assertNoProductsFound(response);
    }

    @Test
    public void getProductByAdditive_e301_productsFound() throws Exception {
        String fieldsToFetchFacets = "brands,product_name,image_small_url,quantity,nutrition_grades_tags";
        Response<Search> response = devClientWithAuth.byAdditive("e301-sodium-ascorbate", fieldsToFetchFacets).execute();
        assertProductsFound(response);
    }

    private static void assertProductsFound(Response<Search> response) {
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        Search search = response.body();

        List<Product> products = search.getProducts();
        assertNotNull(products);
        assertTrue(Integer.parseInt(search.getCount()) > 0);
        assertFalse(products.isEmpty());
    }

    private static void assertNoProductsFound(Response<Search> response) {
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        Search search = response.body();
        assertNotNull(search);

        List<Product> products = search.getProducts();
        assertTrue(products.isEmpty());
        assertEquals(0, Integer.parseInt(search.getCount()));
    }
}
