package openfoodfacts.github.scrachx.openfood.images

import com.google.common.truth.Truth
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.models.ImageType
import openfoodfacts.github.scrachx.openfood.models.Product
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class ImageKeyHelperTest {
    private lateinit var mockProduct: Product

    @BeforeEach
    fun setUp() {
        mockProduct = Mockito.mock(Product::class.java)
    }

    @Test
    fun imageStringKey_returnsCorrectString() {
        Mockito.`when`(mockProduct.lang).thenReturn("de")
        Truth.assertThat(mockProduct.getImageStringKey(ImageType.FRONT)).isEqualTo("front_de")
    }

    @Test
    fun languageCodeFromUrl_blankURL() {
        val url = ""
        Truth.assertThat(getLanguageCodeFromUrl(ImageType.INGREDIENTS, url)).isNull()
    }

    @Test
    fun languageCodeFromUrl_returnsCorrectLanguage() {
        val url = "https://static.openfoodfacts.org/images/products/541/004/100/1204/ingredients_de.48.100.jpg"
        Truth.assertThat(getLanguageCodeFromUrl(ImageType.INGREDIENTS, url)).isEqualTo("de")
    }

    @Test
    fun imageUrl_BarcodeShorter() {
        val barcode = "303371"
        val imageName = "Image"
        val size = "big"
        val expected = BuildConfig.STATICURL + "/images/products/303371/Imagebig.jpg"
        Truth.assertThat(getImageUrl(barcode, imageName, size)).isEqualTo(expected)
    }

    @Test
    fun imageUrl_BarcodeLonger() {
        val barcode = "3033710001279"
        val imageName = "Image"
        val size = "big"
        val expected = BuildConfig.STATICURL + "/images/products/303/371/000/1279/Imagebig.jpg"
        Truth.assertThat(getImageUrl(barcode, imageName, size)).isEqualTo(expected)
    }
}