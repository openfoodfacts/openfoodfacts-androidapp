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
import openfoodfacts.github.scrachx.openfood.models.ProductState;
import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.models.entities.SendProduct;
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static com.google.common.truth.Truth.assertThat;

public class ProductsAPITest {
    /**
     * We need to use auth because we use world.openfoodfacts.dev
     */
    private static ProductsAPI devClientWithAuth;
    private static ProductsAPI prodClient;
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

        prodClient = CommonApiManager.getInstance().getProductsApi();

        devClientWithAuth = new Retrofit.Builder()
            .baseUrl(DEV_API)
            .addConverterFactory(JacksonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClientWithAuth)
            .build()
            .create(ProductsAPI.class);
    }

    private static void assertProductsFound(Response<Search> response) {
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isTrue();
        Search search = response.body();

        List<Product> products = search.getProducts();
        assertThat(products).isNotNull();
        assertThat(Integer.parseInt(search.getCount())).isGreaterThan(0);
        assertThat(products.isEmpty()).isFalse();
    }

    private static void assertNoProductsFound(Response<Search> response) {
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isTrue();
        Search search = response.body();
        assertThat(search).isNotNull();

        List<Product> products = search.getProducts();
        assertThat(products.isEmpty()).isTrue();
        assertThat(Integer.parseInt(search.getCount())).isEqualTo(0);
    }

    @Test
    public void byLanguage() throws Exception {
        Response<Search> searchResponse = prodClient.byLanguage("italian").execute();

        assertThat(searchResponse).isNotNull();
        Search search = searchResponse.body();
        assertThat(search).isNotNull();
        assertThat(search.getProducts()).isNotNull();
    }

    @Test
    public void byLabel() throws Exception {
        Response<Search> searchResponse = prodClient.byLabel("utz-certified").execute();

        assertThat(searchResponse).isNotNull();
        Search search = searchResponse.body();
        assertThat(search).isNotNull();
        assertThat(search.getProducts()).isNotNull();
    }

    @Test
    public void byCategory() throws Exception {
        Response<Search> searchResponse = prodClient.byCategory("baby-foods").execute();

        assertThat(searchResponse).isNotNull();
        Search search = searchResponse.body();
        assertThat(search).isNotNull();
        assertThat(search.getProducts()).isNotNull();
    }

    @Test
    public void byState() throws Exception {
        String fieldsToFetchFacets = "brands,product_name,image_small_url,quantity,nutrition_grades_tags";
        Response<Search> searchResponse = prodClient.byState("complete", fieldsToFetchFacets).execute();

        assertThat(searchResponse).isNotNull();
        Search search = searchResponse.body();
        assertThat(search).isNotNull();
        assertThat(search.getProducts()).isNotNull();
    }

    @Test
    public void byPackaging() throws Exception {
        Response<Search> searchResponse = prodClient.byPackaging("cardboard").execute();

        assertThat(searchResponse).isNotNull();
        Search search = searchResponse.body();
        assertThat(search).isNotNull();
        assertThat(search.getProducts()).isNotNull();
    }

    @Test
    public void byBrand() throws Exception {
        Response<Search> searchResponse = prodClient.byBrand("monoprix").execute();

        assertThat(searchResponse).isNotNull();
        Search search = searchResponse.body();
        assertThat(search).isNotNull();
        assertThat(search.getProducts()).isNotNull();
    }

    @Test
    public void byPurchasePlace() throws Exception {
        Response<Search> searchResponse = prodClient.byPurchasePlace("marseille-5").execute();

        assertThat(searchResponse).isNotNull();
        Search search = searchResponse.body();
        assertThat(search).isNotNull();
        assertThat(search.getProducts()).isNotNull();
    }

    @Test
    public void byStore() throws Exception {
        Response<Search> searchResponse = prodClient.byStore("super-u").execute();

        assertThat(searchResponse);
        Search search = searchResponse.body();
        assertThat(search).isNotNull();
        assertThat(search.getProducts()).isNotNull();
    }

    @Test
    public void byCountry() throws Exception {
        Response<Search> searchResponse = prodClient.byCountry("france").execute();

        assertThat(searchResponse).isNotNull();
        Search search = searchResponse.body();
        assertThat(search).isNotNull();
        assertThat(search.getProducts()).isNotNull();
    }

    @Test
    public void byIngredient() throws Exception {
        Response<Search> searchResponse = prodClient.byIngredient("sucre").execute();

        assertThat(searchResponse).isNotNull();
        Search search = searchResponse.body();
        assertThat(search).isNotNull();
        assertThat(search.getProducts()).isNotNull();
    }

    @Test
    public void byTrace() throws Exception {
        Response<Search> searchResponse = prodClient.byIngredient("eggs").execute();

        assertThat(searchResponse).isNotNull();
        Search search = searchResponse.body();
        assertThat(search).isNotNull();
        assertThat(search.getProducts()).isNotNull();
    }

    @Test
    public void getProductByTrace_eggs_productsFound() throws Exception {
        Response<Search> response = prodClient.byTrace("eggs").execute();
        assertProductsFound(response);
    }

    @Test
    public void getProductByPackagerCode_emb35069c_productsFound() throws Exception {
        Response<Search> response = prodClient.byPackagerCode("emb-35069c").execute();
        assertProductsFound(response);
    }

    @Test
    public void getProductByNutritionGrade_a_productsFound() throws Exception {
        Response<Search> res = prodClient.byNutritionGrade("a").execute();
        assertProductsFound(res);
    }

    @Test
    public void getProductByCity_Paris_noProductFound() throws Exception {
        Response<Search> response = prodClient.byCity("paris").execute();
        assertNoProductsFound(response);
    }

    @Test
    public void getProductByAdditive_e301_productsFound() throws Exception {
        String fieldsToFetchFacets = "brands,product_name,image_small_url,quantity,nutrition_grades_tags";
        Response<Search> response = prodClient.byAdditive("e301-sodium-ascorbate", fieldsToFetchFacets).execute();
        assertProductsFound(response);
    }

    @Test
    public void getProduct_notFound() throws Exception {
        String barcode = "457457457";
        Response<ProductState> response = prodClient.getProductByBarcode(barcode, "code", Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)).execute();

        assertThat(response.isSuccessful()).isTrue();

        assertThat(response.body().getStatus()).isEqualTo(0);
        assertThat(response.body().getStatusVerbose()).isEqualTo("product not found");
        assertThat(response.body().getCode()).isEqualTo(barcode);
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

        ProductState body = devClientWithAuth
            .saveProductSingle(product.getBarcode(), productDetails, OpenFoodAPIClient.getCommentToUpload())
            .blockingGet();

        assertThat(body.getStatus()).isEqualTo(1);
        assertThat(body.getStatusVerbose()).isEqualTo("fields saved");

        String fields = "product_name,brands,brands_tags,quantity";

        Response<ProductState> response = devClientWithAuth.getProductByBarcode(
            product.getBarcode(),
            fields,
            Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
        ).execute();

        Product savedProduct = response.body().getProduct();
        assertThat(savedProduct.getProductName()).isEqualTo(product.getName());
        assertThat(savedProduct.getBrands()).isEqualTo(product.getBrands());
        assertThat(savedProduct.getBrandsTags()).contains(product.getBrands());
        assertThat(savedProduct.getQuantity()).isEqualTo(product.getWeight() + " " + product.getWeight_unit());
    }
}
