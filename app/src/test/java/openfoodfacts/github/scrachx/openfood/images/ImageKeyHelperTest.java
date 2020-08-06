package openfoodfacts.github.scrachx.openfood.images;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;

import static com.google.common.truth.Truth.assertThat;

public class ImageKeyHelperTest {
    Product mockProduct;
    String url;

    @Before
    public void setUp() {
        mockProduct = Mockito.mock(Product.class);
    }

    @Test
    public void getImageStringKey_returnsCorrectString() {
        Mockito.when(mockProduct.getLang()).thenReturn("de");

        assertThat(ImageKeyHelper.getImageStringKey(ProductImageField.FRONT, mockProduct)).isEqualTo("front_de");
    }

    @Test
    public void getLanguageCodeFromUrl_blankURL() {
        url = "";

        assertThat(ImageKeyHelper.getLanguageCodeFromUrl(ProductImageField.INGREDIENTS, url)).isNull();
    }

    @Test
    public void getLanguageCodeFromUrl_returnsCorrectLanguage() {
        url = "https://static.openfoodfacts.org/images/products/541/004/100/1204/ingredients_de.48.100.jpg";

        assertThat(ImageKeyHelper.getLanguageCodeFromUrl(ProductImageField.INGREDIENTS, url)).isEqualTo("de");
    }

    @Test
    public void getImageUrl_BarcodeShorter() {
        String barcode = "303371";
        String imageName = "Image";
        String size = "big";

        final String expected = BuildConfig.STATICURL + "/images/products/303371/Imagebig.jpg";
        assertThat(ImageKeyHelper.getImageUrl(barcode, imageName, size)).isEqualTo(expected);
    }

    @Test
    public void getImageUrl_BarcodeLonger() {
        String barcode = "3033710001279";
        String imageName = "Image";
        String size = "big";

        final String expected = BuildConfig.STATICURL + "/images/products/303/371/000/1279/Imagebig.jpg";
        assertThat(ImageKeyHelper.getImageUrl(barcode, imageName, size)).isEqualTo(expected);
    }
}