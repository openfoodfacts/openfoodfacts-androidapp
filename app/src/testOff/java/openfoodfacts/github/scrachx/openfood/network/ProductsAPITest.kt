package openfoodfacts.github.scrachx.openfood.network

import com.google.common.truth.Truth
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import openfoodfacts.github.scrachx.openfood.models.Search
import openfoodfacts.github.scrachx.openfood.models.entities.SendProduct
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.Utils
import org.junit.BeforeClass
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.time.Duration

class ProductsAPITest {
    @Test
    fun byLanguage() {
        val search = prodClient.getProductsByLanguage("italian").blockingGet()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    fun byLabel() {
        val search = prodClient.getProductsByLabel("utz-certified").blockingGet()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    fun byCategory() {
        val search = prodClient.getProductsByCategory("baby-foods").blockingGet()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    fun byState() {
        val fieldsToFetchFacets = "brands,product_name,image_small_url,quantity,nutrition_grades_tags"
        val search = prodClient.getProductsByState("complete", fieldsToFetchFacets).blockingGet()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    fun byPackaging() {
        val search = prodClient.getProductsByPackaging("cardboard").blockingGet()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    fun byBrand() {
        val search = prodClient.getProductsByBrand("monoprix").blockingGet()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    fun byPurchasePlace() {
        val search = prodClient.getProductsByPurchasePlace("marseille-5").blockingGet()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    fun byStore() {
        val search = prodClient.getProductsByStore("super-u").blockingGet()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    fun byCountry() {
        val search = prodClient.byCountry("france").blockingGet()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    fun byIngredient() {
        val search = prodClient.getProductsByIngredient("sucre").blockingGet()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    fun byTrace() {
        val search = prodClient.getProductsByTrace("eggs").blockingGet()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    fun productByTrace_eggs_productsFound() {
        val response = prodClient.getProductsByTrace("eggs").blockingGet()
        assertProductsFound(response)
    }

    @Test
    fun productByPackagerCode_emb35069c_productsFound() {
        val response = prodClient.byPackagerCode("emb-35069c").blockingGet()
        assertProductsFound(response)
    }

    @Test
    fun productByNutritionGrade_a_productsFound() {
        val res = prodClient.byNutritionGrade("a").blockingGet()
        assertProductsFound(res)
    }

    @Test
    fun productByCity_Paris_noProductFound() {
        val response = prodClient.byCity("paris").blockingGet()
        assertNoProductsFound(response)
    }

    @Test
    fun productByAdditive_e301_productsFound() {
        val fieldsToFetchFacets = "brands,product_name,image_small_url,quantity,nutrition_grades_tags"
        val response = prodClient.getProductsByAdditive("e301-sodium-ascorbate", fieldsToFetchFacets).blockingGet()
        assertProductsFound(response)
    }

    @Test
    fun product_notFound() {
        val barcode = "457457457"
        val response = prodClient.getProductByBarcode(barcode, "code", Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)).execute()
        Truth.assertThat(response.isSuccessful).isTrue()
        Truth.assertThat(response.body()!!.status).isEqualTo(0)
        Truth.assertThat(response.body()!!.statusVerbose).isEqualTo("product not found")
        Truth.assertThat(response.body()!!.code).isEqualTo(barcode)
    }

    @Test
    fun post_product() {
        val product = SendProduct()
        product.barcode = "1234567890"
        product.name = "ProductName"
        product.brands = "productbrand"
        product.weight = "123"
        product.weight_unit = "g"
        product.lang = "en"
        val productDetails = mapOf(
                "lang" to product.lang,
                "product_name" to product.name,
                "brands" to product.brands,
                "quantity" to product.quantity
        )


        val body = devClientWithAuth
                .saveProductSingle(product.barcode, productDetails, OpenFoodAPIClient.commentToUpload)
                .blockingGet()
        Truth.assertThat(body.status).isEqualTo(1)
        Truth.assertThat(body.statusVerbose).isEqualTo("fields saved")
        val fields = "product_name,brands,brands_tags,quantity"
        val response = devClientWithAuth.getProductByBarcode(
                product.barcode,
                fields,
                Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
        ).execute()
        val savedProduct = response.body()!!.product
        Truth.assertThat(savedProduct.productName).isEqualTo(product.name)
        Truth.assertThat(savedProduct.brands).isEqualTo(product.brands)
        Truth.assertThat(savedProduct.brandsTags).contains(product.brands)
        Truth.assertThat(savedProduct.quantity).isEqualTo(product.weight + " " + product.weight_unit)
    }

    companion object {
        /**
         * We need to use auth because we use world.openfoodfacts.dev
         */
        private lateinit var devClientWithAuth: ProductsAPI
        private lateinit var prodClient: ProductsAPI
        private const val DEV_API = "https://world.openfoodfacts.dev"

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

        private fun assertProductsFound(search: Search) {
            val products = search.products
            Truth.assertThat(products).isNotNull()
            Truth.assertThat(search.count.toInt()).isGreaterThan(0)
            Truth.assertThat(products.isEmpty()).isFalse()
        }

        private fun assertNoProductsFound(search: Search) {
            Truth.assertThat(search).isNotNull()
            val products = search.products
            Truth.assertThat(products.isEmpty()).isTrue()
            Truth.assertThat(search.count.toInt()).isEqualTo(0)
        }
    }
}