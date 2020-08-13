package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProduct;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for {@link ToUploadProduct}
 */
public class ToUploadProductTest {
    // TODO: should make product field strings public static fields in ToUploadProduct or use toString from
    // ProductImageField to do comparison in getProductField(), keeping in mind the difference between
    // nutrients and NUTRITION
    private ToUploadProduct toUploadProduct;

    @Before
    public void setUp() {
        toUploadProduct = new ToUploadProduct();
    }

    @Test
    public void getProductField_returnsCorrectProductImageField() {
        toUploadProduct.setField("front");
        assertThat(toUploadProduct.getProductField()).isEqualTo(ProductImageField.FRONT);

        toUploadProduct.setField("ingredients");
        assertThat(toUploadProduct.getProductField()).isEqualTo(ProductImageField.INGREDIENTS);

        toUploadProduct.setField("nutrients");
        assertThat(toUploadProduct.getProductField()).isEqualTo(ProductImageField.NUTRITION);

        toUploadProduct.setField("something else");
        assertThat(toUploadProduct.getProductField()).isEqualTo(ProductImageField.OTHER);
    }

    @Test
    public void toUploadProductWithId_fillsCorrectly() {
        long id = 1L;
        String barcode = "CSE370";
        String imageFilePath = "C:\\Images\\Example.pdf";
        boolean uploaded = false;
        String field = "front";

        toUploadProduct = new ToUploadProduct(id, barcode, imageFilePath, uploaded, field);

        assertThat(toUploadProduct.getId()).isEqualTo(id);
        assertThat(toUploadProduct.getBarcode()).isEqualTo(barcode);
        assertThat(toUploadProduct.getImageFilePath()).isEqualTo(imageFilePath);
        assertThat(toUploadProduct.getUploaded()).isEqualTo(uploaded);
        assertThat(toUploadProduct.getField()).isEqualTo(field);
    }
}
