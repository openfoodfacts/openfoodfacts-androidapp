package openfoodfacts.github.scrachx.openfood.network

import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.models.Search
import openfoodfacts.github.scrachx.openfood.models.entities.SendProduct
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.getUserAgent
import org.junit.Assert.fail
import org.junit.BeforeClass
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.time.Duration

class ProductsAPITest {
    @Test
    fun byLanguage() {
        val search = prodClient.getProductsByLanguage("italian").blockingGet() as Search
        assertThat(search).isNotNull()
        assertThat(search.products).isNotNull()
    }

    @Test
    fun byLabel() {
        val search = prodClient.getProductsByLabel("utz-certified").blockingGet() as Search
        assertThat(search).isNotNull()
        assertThat(search.products).isNotNull()
    }

    @Test
    fun byCategory() {
        val search = prodClient.getProductsByCategory("baby-foods").blockingGet() as Search
        assertThat(search).isNotNull()
        assertThat(search.products).isNotNull()
    }

    @Test
    fun byState() {
        val fieldsToFetchFacets = "brands,product_name,image_small_url,quantity,nutrition_grades_tags"
        val search = prodClient.getProductsByState("complete", fieldsToFetchFacets).blockingGet() as Search
        assertThat(search).isNotNull()
        assertThat(search.products).isNotNull()
    }

    @Test
    fun byPackaging() {
        val search = prodClient.getProductsByPackaging("cardboard").blockingGet() as Search
        assertThat(search).isNotNull()
        assertThat(search.products).isNotNull()
    }

    @Test
    fun byBrand() {
        val search = prodClient.getProductsByBrand("monoprix").blockingGet() as Search
        assertThat(search).isNotNull()
        assertThat(search.products).isNotNull()
    }

    @Test
    fun byPurchasePlace() {
        val search = prodClient.getProductsByPurchasePlace("marseille-5").blockingGet() as Search
        assertThat(search).isNotNull()
        assertThat(search.products).isNotNull()
    }

    @Test
    fun byStore() {
        val search = prodClient.getProductsByStore("super-u").blockingGet() as Search
        assertThat(search).isNotNull()
        assertThat(search.products).isNotNull()
    }

    @Test
    fun byCountry() {
        val search = prodClient.byCountry("france").blockingGet() as Search
        assertThat(search).isNotNull()
        assertThat(search.products).isNotEmpty()
    }

    @Test
    fun byIngredient() {
        val search = prodClient.getProductsByIngredient("sucre").blockingGet() as Search
        assertThat(search).isNotNull()
        assertThat(search.products).isNotEmpty()
    }

    @Test
    fun byTrace() {
        val search = prodClient.getProductsByTrace("eggs").blockingGet() as Search
        assertThat(search).isNotNull()
        assertThat(search.products).isNotNull()
    }

    @Test
    fun productByTrace_eggs_productsFound() {
        val response = prodClient.getProductsByTrace("eggs").blockingGet() as Search
        response.assertProductsFound()
    }

    @Test
    fun productByPackagerCode_emb35069c_productsFound() {
        val response = prodClient.getProductsByPackagerCode("emb-35069c").blockingGet() as Search
        response.assertProductsFound()
    }

    @Test
    fun productByNutritionGrade_a_productsFound() {
        val res = prodClient.getProductsByNutriScore("a").blockingGet() as Search
        res.assertProductsFound()
    }

    @Test
    fun productByCity_Paris_noProductFound() {
        val response = prodClient.getProducsByCity("paris").blockingGet() as Search
        response.assertNoProductsFound()
    }

    @Test
    fun productByAdditive_e301_productsFound() {
        val fieldsToFetchFacets = "brands,product_name,image_small_url,quantity,nutrition_grades_tags"
        val response = prodClient.getProductsByAdditive("e301-sodium-ascorbate", fieldsToFetchFacets).blockingGet() as Search
        response.assertProductsFound()
    }

    @Test
    fun product_notFound() {
        val barcode = "457457457"
        prodClient.getProductByBarcode(
                barcode,
                "code",
                getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
        ).subscribe({ productState ->
            assertThat(productState.status).isEqualTo(0)
            assertThat(productState.statusVerbose).isEqualTo("product not found")
            assertThat(productState.code).isEqualTo(barcode)
        }) {
            fail("Request returned error")
            it.printStackTrace()
        }
    }

    @Test
    fun post_product() {
        val product = SendProduct().apply {
            barcode = "1234567890"
            name = "ProductName"
            brands = "productbrand"
            weight = "123"
            weight_unit = "g"
            lang = "en"
        }

        val productDetails = mapOf<String?, String?>(
                "lang" to product.lang,
                "product_name" to product.name,
                "brands" to product.brands,
                "quantity" to product.quantity
        )


        val body = devClientWithAuth
                .saveProduct(product.barcode, productDetails, "Automated test")
                .blockingGet() as ProductState

        assertThat(body.status).isEqualTo(1)
        assertThat(body.statusVerbose).isEqualTo("fields saved")

        val fields = "product_name,brands,brands_tags,quantity"
        val response = devClientWithAuth.getProductByBarcode(
                product.barcode,
                fields,
                getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
        ).blockingGet() as ProductState

        assertThat(response.product).isNotNull()
        val savedProduct = response.product!!
        assertThat(savedProduct.productName).isEqualTo(product.name)
        assertThat(savedProduct.brands).isEqualTo(product.brands)
        assertThat(savedProduct.brandsTags).contains(product.brands)
        assertThat(savedProduct.quantity).isEqualTo("${product.weight} ${product.weight_unit}")
    }

    companion object {
        private const val DEV_API = "https://world.openfoodfacts.dev"

        /**
         * We need to use auth because we use world.openfoodfacts.dev
         */
        private lateinit var devClientWithAuth: ProductsAPI
        private lateinit var prodClient: ProductsAPI

        @BeforeClass
        @JvmStatic
        fun setupClient() {
            val httpClientWithAuth = OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .connectTimeout(Duration.ZERO)
                    .readTimeout(Duration.ZERO)
                    .addInterceptor {
                        val origReq = it.request()
                        it.proceed(origReq.newBuilder()
                                .header("Authorization", "Basic b2ZmOm9mZg==")
                                .header("Accept", "application/json")
                                .method(origReq.method(), origReq.body()).build())
                    }
                    .build()
            prodClient = CommonApiManager.productsApi
            devClientWithAuth = Retrofit.Builder()
                    .baseUrl(DEV_API)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(httpClientWithAuth)
                    .build()
                    .create(ProductsAPI::class.java)
        }

        private fun Search.assertProductsFound() {
            assertThat(count.toInt()).isGreaterThan(0)
            assertThat(products).isNotEmpty()
        }

        private fun Search.assertNoProductsFound() {
            assertThat(count.toInt()).isEqualTo(0)
            assertThat(products).isEmpty()
        }
    }
}