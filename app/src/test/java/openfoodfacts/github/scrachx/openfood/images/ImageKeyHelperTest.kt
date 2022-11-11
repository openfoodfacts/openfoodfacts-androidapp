package openfoodfacts.github.scrachx.openfood.images

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ImageKeyHelperTest {

    @Test
    fun imageStringKey_returnsCorrectString(@MockK product: Product) {
        every { product.lang } returns "de"

        assertThat(product.getImageStringKey(ProductImageField.FRONT))
            .isEqualTo("front_de")
    }

    @Test
    fun `languageCode from empty url should be null`() {
        assertThat(getLanguageCodeFromUrl(ProductImageField.INGREDIENTS, "")).isNull()
    }

    @Test
    fun languageCodeFromUrl_returnsCorrectLanguage() {
        val url = "https://static.openfoodfacts.org/images/products/541/004/100/1204/ingredients_de.48.100.jpg"
        assertThat(getLanguageCodeFromUrl(ProductImageField.INGREDIENTS, url)).isEqualTo("de")
    }

    @Test
    fun imageUrl_BarcodeShorter() {
        val barcode = "303371"
        val imageName = "123"
        val size = "987"
        val expected = BuildConfig.STATICURL + "/images/products/303371/123.987.jpg"
        assertThat(getImageUrl(barcode, imageName, size)).isEqualTo(expected)
    }

    @Test
    fun imageUrl_BarcodeLonger() {
        val barcode = "3033710001279"
        val imageName = "123"
        val size = "987"
        val expected = BuildConfig.STATICURL + "/images/products/303/371/000/1279/123.987.jpg"
        assertThat(getImageUrl(barcode, imageName, size)).isEqualTo(expected)
    }
}
