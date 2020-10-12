package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Tests for {@link ToUploadProduct}
 */
public class ToUploadProductTest {

    // TODO: should make product field strings public static fields in ToUploadProduct or use toString from
    // ProductImageField to do comparison in getProductField(), keeping in mind the difference between
    // nutrients and NUTRITION
    ToUploadProduct toUploadProduct;

    @Before
    public void setUp(){
        toUploadProduct = new ToUploadProduct();
    }

    @Test
    public void getProductField_returnsCorrectProductImageField() {
        toUploadProduct.setField("front");
        assertEquals(ProductImageField.FRONT, toUploadProduct.getProductField());

        toUploadProduct.setField("ingredients");
        assertEquals(ProductImageField.INGREDIENTS, toUploadProduct.getProductField());

        toUploadProduct.setField("nutrients");
        assertEquals(ProductImageField.NUTRITION, toUploadProduct.getProductField());

        toUploadProduct.setField("something else");
        assertEquals(ProductImageField.OTHER, toUploadProduct.getProductField());
    }

    @Test
    public void toUploadProductWithId_fillsCorrectly() {
        Long id = 1L;
        String barcode = "CSE370";
        String imageFilePath = "C:\\Images\\Example.pdf";
        Boolean uploaded = false;
        String field = "front";

        toUploadProduct = new ToUploadProduct(id, barcode, imageFilePath, uploaded, field);

        assertEquals(toUploadProduct.getId(), id);
        assertEquals(toUploadProduct.getBarcode(), barcode);
        assertEquals(toUploadProduct.getImageFilePath(), imageFilePath);
        assertFalse(toUploadProduct.getUploaded());
        assertEquals(toUploadProduct.getField(), field);
    }
}
