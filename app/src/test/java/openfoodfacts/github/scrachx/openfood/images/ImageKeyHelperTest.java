package openfoodfacts.github.scrachx.openfood.images;

import openfoodfacts.github.scrachx.openfood.BuildConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;

import static org.junit.Assert.*;

public class ImageKeyHelperTest {

    Product mockProduct;
    String url;

    @Before
    public void setUp(){
        mockProduct = Mockito.mock(Product.class);
    }

    @Test
    public void getImageStringKey_returnsCorrectString(){
        Mockito.when(mockProduct.getLang()).thenReturn("de");

        assertEquals("front_de", ImageKeyHelper.getImageStringKey(ProductImageField.FRONT, mockProduct));
    }

    @Test
    public void getLanguageCodeFromUrl_blankURL(){
        url = "";

        assertNull(ImageKeyHelper.getLanguageCodeFromUrl(ProductImageField.INGREDIENTS, url));
    }

    @Test
    public void getLanguageCodeFromUrl_returnsCorrectLanguage(){
        url = "https://static.openfoodfacts.org/images/products/541/004/100/1204/ingredients_de.48.100.jpg";

        assertEquals("de", ImageKeyHelper.getLanguageCodeFromUrl(ProductImageField.INGREDIENTS, url));
    }

    @Test
    public void getImageUrl_BarcodeShorter(){
        String barcode = "303371";
        String imageName = "Image";
        String size = "big";

        final String expected = BuildConfig.STATICURL + "/images/products/303371/Imagebig.jpg";
        assertEquals(expected, ImageKeyHelper.getImageUrl(barcode, imageName, size));
    }


    @Test
    public void getImageUrl_BarcodeLonger() {
        String barcode = "3033710001279";
        String imageName = "Image";
        String size = "big";

        final String expected = BuildConfig.STATICURL + "/images/products/303/371/000/1279/Imagebig.jpg";
        assertEquals(expected, ImageKeyHelper.getImageUrl(barcode, imageName, size));
    }
}