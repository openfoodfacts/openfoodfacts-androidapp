package openfoodfacts.github.scrachx.openfood.images

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT
import openfoodfacts.github.scrachx.openfood.models.ProductImageField.INGREDIENTS
import openfoodfacts.github.scrachx.openfood.models.asBarcode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ImageKeyHelperTest {
    private lateinit var mockProduct: Product

    @BeforeEach
    fun setUp() {
        mockProduct = mock()
    }

    @Test
    fun imageStringKey_returnsCorrectString() {
        whenever(mockProduct.lang).doReturn("de")
        assertThat(mockProduct.getImageStringKey(FRONT)).isEqualTo("front_de")
    }

    @Test
    fun languageCodeFromUrl_blankURL() {
        val url = ""
        assertThat(getLanguageCodeFromUrl(INGREDIENTS, url)).isNull()
    }

    @Test
    fun languageCodeFromUrl_returnsCorrectLanguage() {
        val url = "https://static.openfoodfacts.org/images/products/541/004/100/1204/ingredients_de.48.100.jpg"
        assertThat(getLanguageCodeFromUrl(INGREDIENTS, url)).isEqualTo("de")
    }

    @Test
    fun imageUrl_BarcodeShorter() {
        val barcode = "303371".asBarcode()
        val imageName = "Image"
        val size = "big"
        val expected = BuildConfig.STATICURL + "/images/products/303371/Imagebig.jpg"
        assertThat(getImageUrl(barcode, imageName, size)).isEqualTo(expected)
    }

    @Test
    fun imageUrl_BarcodeLonger() {
        val barcode = "3033710001279".asBarcode()
        val imageName = "Image"
        val size = "big"
        val expected = BuildConfig.STATICURL + "/images/products/303/371/000/1279/Imagebig.jpg"
        assertThat(getImageUrl(barcode, imageName, size)).isEqualTo(expected)
    }
}