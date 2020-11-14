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
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.IOException

class ProductsAPITest {
    @Test
    @Throws(Exception::class)
    fun byLanguage() {
        val searchResponse = prodClient.byLanguage("italian").execute()
        Truth.assertThat(searchResponse).isNotNull()
        val search = searchResponse.body()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun byLabel() {
        val searchResponse = prodClient.byLabel("utz-certified").execute()
        Truth.assertThat(searchResponse).isNotNull()
        val search = searchResponse.body()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun byCategory() {
        val searchResponse = prodClient.byCategory("baby-foods").execute()
        Truth.assertThat(searchResponse).isNotNull()
        val search = searchResponse.body()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun byState() {
        val fieldsToFetchFacets = "brands,product_name,image_small_url,quantity,nutrition_grades_tags"
        val searchResponse = prodClient.byState("complete", fieldsToFetchFacets).execute()
        Truth.assertThat(searchResponse).isNotNull()
        val search = searchResponse.body()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun byPackaging() {
        val searchResponse = prodClient.byPackaging("cardboard").execute()
        Truth.assertThat(searchResponse).isNotNull()
        val search = searchResponse.body()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun byBrand() {
        val searchResponse = prodClient.byBrand("monoprix").execute()
        Truth.assertThat(searchResponse).isNotNull()
        val search = searchResponse.body()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun byPurchasePlace() {
        val searchResponse = prodClient.byPurchasePlace("marseille-5").execute()
        Truth.assertThat(searchResponse).isNotNull()
        val search = searchResponse.body()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun byStore() {
        val searchResponse = prodClient.byStore("super-u").execute()
        Truth.assertThat(searchResponse)
        val search = searchResponse.body()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun byCountry() {
        val searchResponse = prodClient.byCountry("france").execute()
        Truth.assertThat(searchResponse).isNotNull()
        val search = searchResponse.body()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun byIngredient() {
        val searchResponse = prodClient.byIngredient("sucre").execute()
        Truth.assertThat(searchResponse).isNotNull()
        val search = searchResponse.body()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun byTrace() {
        val searchResponse = prodClient.byIngredient("eggs").execute()
        Truth.assertThat(searchResponse).isNotNull()
        val search = searchResponse.body()
        Truth.assertThat(search).isNotNull()
        Truth.assertThat(search!!.products).isNotNull()
    }

    @Throws(Exception::class)
    @Test
    fun productByTrace_eggs_productsFound() {
        val response = prodClient.byTrace("eggs").execute()
        assertProductsFound(response)
    }

    @Throws(Exception::class)
    @Test
    fun productByPackagerCode_emb35069c_productsFound() {
        val response = prodClient.byPackagerCode("emb-35069c").execute()
        assertProductsFound(response)
    }

    @Throws(Exception::class)
    @Test
    fun productByNutritionGrade_a_productsFound() {
        val res = prodClient.byNutritionGrade("a").execute()
        assertProductsFound(res)
    }

    @Throws(Exception::class)
    @Test
    fun productByCity_Paris_noProductFound() {
        val response = prodClient.byCity("paris").execute()
        assertNoProductsFound(response)
    }

    @Throws(Exception::class)
    @Test
    fun productByAdditive_e301_productsFound() {
        val fieldsToFetchFacets = "brands,product_name,image_small_url,quantity,nutrition_grades_tags"
        val response = prodClient.byAdditive("e301-sodium-ascorbate", fieldsToFetchFacets).execute()
        assertProductsFound(response)
    }

    @Throws(Exception::class)
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
    @Throws(IOException::class)
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
                .saveProductSingle(product.barcode, productDetails, OpenFoodAPIClient.getCommentToUpload())
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
                    .addInterceptor {
                        val origReq = it.request()
                        it.proceed(origReq.newBuilder()
                                .header("Authorization", "Basic b2ZmOm9mZg==")
                                .header("Accept", "application/json")
                                .method(origReq.method(), origReq.body()).build())
                    }
                    .build()
            prodClient = CommonApiManager.getInstance().productsApi
            devClientWithAuth = Retrofit.Builder()
                    .baseUrl(DEV_API)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(httpClientWithAuth)
                    .build()
                    .create(ProductsAPI::class.java)
        }

        private fun assertProductsFound(response: Response<Search>) {
            Truth.assertThat(response).isNotNull()
            Truth.assertThat(response.isSuccessful).isTrue()
            val search = response.body()
            val products = search!!.products
            Truth.assertThat(products).isNotNull()
            Truth.assertThat(search.count.toInt()).isGreaterThan(0)
            Truth.assertThat(products.isEmpty()).isFalse()
        }

        private fun assertNoProductsFound(response: Response<Search>) {
            Truth.assertThat(response).isNotNull()
            Truth.assertThat(response.isSuccessful).isTrue()
            val search = response.body()
            Truth.assertThat(search).isNotNull()
            val products = search!!.products
            Truth.assertThat(products.isEmpty()).isTrue()
            Truth.assertThat(search.count.toInt()).isEqualTo(0)
        }
    }
}