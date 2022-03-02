package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import androidx.lifecycle.SavedStateHandle
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.CoroutinesSetMainExtension
import openfoodfacts.github.scrachx.openfood.utils.InstantTaskExecutorExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class, CoroutinesSetMainExtension::class, InstantTaskExecutorExtension::class)
internal class ProductPhotosViewModelTest(@Mock private val mockProductsApi: ProductsAPI) {

    private val stubProduct = Product().also {
        it.code = "code"
    }

    private val stubProductState = ProductState().also {
        it.product = stubProduct
    }

    private val savedStateHandle = SavedStateHandle(mapOf(ProductEditActivity.KEY_STATE to stubProductState))

    private val testSubject = ProductPhotosViewModel(
        productsAPI = mockProductsApi,
        savedStateHandle = savedStateHandle
    )

    @Nested
    @DisplayName("Given product api returns product images")
    inner class GivenProductApi {
        @BeforeEach
        fun beforeEach() {
            runBlocking {
                val objectMapper = ObjectMapper()
                val objectNode = objectMapper.readValue<ObjectNode>(PRODUCTS_API_PAYLOAD.reader())
                whenever(mockProductsApi.getProductImages(any())).thenReturn(objectNode)
            }
        }

        @Nested
        @DisplayName("When imageNames is called")
        inner class ImageNamesCalled {
            private lateinit var result: List<String>

            @BeforeEach
            fun beforeEach() {
                runTest {
                    result = testSubject.imageNames.first()
                }
            }

            @Test
            fun `it should return non empty list`() {
                assertThat(result).isNotEmpty()
            }

            @Test
            fun `it should return image names sorted by uploaded time`() {
                val expected = listOf(24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 9, 8, 11, 10, 7, 6, 5, 4, 3, 2, 1).map(Int::toString)

                assertThat(result).isEqualTo(expected)
            }
        }
    }


    companion object {
        private const val PRODUCTS_API_PAYLOAD = """
            {"code":"7613035694958","product":{"images":{"1":{"sizes":{"100":{"h":100,"w":75},"400":{"h":400,"w":300},"full":{"h":4032,"w":3024}},"uploaded_t":"1466844778","uploader":"tacite"}
            ,"10":{"sizes":{"100":{"h":100,"w":56},"400":{"h":400,"w":225},"full":{"h":1200,"w":675}},"uploaded_t":"1536090986","uploader":"kiliweb"},"11":{"sizes":{"100":{"h":100,"w":56},"400":{"h":400,"w":225},"full":{"h":1200,"w":675}},"uploaded_t":"1536090988","uploader":"kiliweb"},"12":{"sizes":{"100":{"h":100,"w":69},"400":{"h":400,"w":274},"full":{"h":1659,"w":1137}},"uploaded_t":1566650585,"uploader":"floriane-03"},"13":{"sizes":{"100":{"h":62,"w":100},"400":{"h":248,"w":400},"full":{"h":1057,"w":1704}},"uploaded_t":1566650686,"uploader":"floriane-03"},"14":{"sizes":{"100":{"h":100,"w":67},"400":{"h":400,"w":266},"full":{"h":1938,"w":1289}},"uploaded_t":1566650736,"uploader":"floriane-03"},"15":{"sizes":{"100":{"h":66,"w":100},"400":{"h":266,"w":400},"full":{"h":1030,"w":1550}},"uploaded_t":1566650786,"uploader":"floriane-03"},"16":{"sizes":{"100":{"h":100,"w":78},"400":{"h":400,"w":311},"full":{"h":1899,"w":1475}},"uploaded_t":1566650950,"uploader":"floriane-03"},"17":{"sizes":{"100":{"h":100,"w":73},"400":{"h":400,"w":292},"full":{"h":1370,"w":1000}},"uploaded_t":1587642566,"uploader":"date-limite-app"},"18":{"sizes":{"100":{"h":100,"w":73},"400":{"h":400,"w":292},"full":{"h":1370,"w":1000}},"uploaded_t":1587730085,"uploader":"date-limite-app"},"19":{"sizes":{"100":{"h":91,"w":100},"400":{"h":365,"w":400},"full":{"h":2075,"w":2272}},"uploaded_t":1589455950,"uploader":"openfoodfacts-contributors"},"2":{"sizes":{"100":{"h":100,"w":75},"400":{"h":400,"w":300},"full":{"h":4032,"w":3024}},"uploaded_t":"1466844783","uploader":"tacite"},"20":{"sizes":{"100":{"h":75,"w":100},"400":{"h":300,"w":400},"full":{"h":750,"w":1000}},"uploaded_t":1590841671,"uploader":"date-limite-app"},"21":{"sizes":{"100":{"h":100,"w":89},"400":{"h":400,"w":357},"full":{"h":3384,"w":3024}},"uploaded_t":1613418654,"uploader":"openfoodfacts-contributors"},"22":{"sizes":{"100":{"h":100,"w":77},"400":{"h":400,"w":309},"full":{"h":2871,"w":2216}},"uploaded_t":1615828822,"uploader":"aleene"},"23":{"sizes":{"100":{"h":31,"w":100},"400":{"h":125,"w":400},"full":{"h":531,"w":1698}},"uploaded_t":1615828948,"uploader":"aleene"},"24":{"sizes":{"100":{"h":64,"w":100},"400":{"h":258,"w":400},"full":{"h":883,"w":1370}},"uploaded_t":1615829021,"uploader":"aleene"},"3":{"sizes":{"100":{"h":100,"w":76},"400":{"h":400,"w":303},"full":{"h":3232,"w":2448}},"uploaded_t":"1510596999","uploader":"kiliweb"},"4":{"sizes":{"100":{"h":100,"w":56},"400":{"h":400,"w":226},"full":{"h":3542,"w":2000}},"uploaded_t":"1517418305","uploader":"openfoodfacts-contributors"},"5":{"sizes":{"100":{"h":100,"w":68},"400":{"h":400,"w":273},"full":{"h":1200,"w":820}},"uploaded_t":"1520079155","uploader":"kiliweb"},"6":{"sizes":{"100":{"h":100,"w":73},"400":{"h":400,"w":292},"full":{"h":1200,"w":875}},"uploaded_t":"1527092159","uploader":"kiliweb"},"7":{"sizes":{"100":{"h":69,"w":100},"400":{"h":274,"w":400},"full":{"h":1198,"w":1747}},"uploaded_t":"1528314934","uploader":"kiliweb"},"8":{"sizes":{"100":{"h":100,"w":75},"400":{"h":400,"w":300},"full":{"h":4032,"w":3024}},"uploaded_t":1536923209,"uploader":"openfoodfacts-contributors"},"9":{"sizes":{"100":{"h":100,"w":56},"400":{"h":400,"w":225},"full":{"h":2611,"w":1469}},"uploaded_t":1536958725,"uploader":"openfoodfacts-contributors"},"front_fr":{"angle":0,"coordinates_image_size":"full","geometry":"0x0--1--1","imgid":"22","normalize":null,"rev":"79","sizes":{"100":{"h":100,"w":77},"200":{"h":200,"w":154},"400":{"h":400,"w":309},"full":{"h":2871,"w":2216}},"white_magic":null,"x1":"-1","x2":"-1","y1":"-1","y2":"-1"},"ingredients_fr":{"angle":0,"coordinates_image_size":"full","geometry":"0x0--1--1","imgid":"24","normalize":null,"rev":"88","sizes":{"100":{"h":64,"w":100},"200":{"h":129,"w":200},"400":{"h":258,"w":400},"full":{"h":883,"w":1370}},"white_magic":null,"x1":"-1","x2":"-1","y1":"-1","y2":"-1"},"nutrition_fr":{"angle":"0","coordinates_image_size":"full","geometry":"0x0-0-0","imgid":"16","normalize":"false","rev":"73","sizes":{"100":{"h":100,"w":78},"200":{"h":200,"w":155},"400":{"h":400,"w":311},"full":{"h":1899,"w":1475}},"white_magic":"false","x1":"0","x2":"0","y1":"0","y2":"0"},
            "packaging_fr":{"angle":0,"coordinates_image_size":"full","geometry":"0x0--1--1","imgid":"23","normalize":null,"rev":"84","sizes":{"100":{"h":31,"w":100},"200":{"h":63,"w":200},"400":{"h":125,"w":400},"full":{"h":531,"w":1698}}
            ,"white_magic":null,"x1":"-1","x2":"-1","y1":"-1","y2":"-1"}}},"status":1,"status_verbose":"product found"}
        """
    }
}
