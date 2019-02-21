package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Tests for {@link ToUploadProduct}
 */
public class ToUploadProductTest {

    // TODO: should make product field strings public static fields in ToUploadProduct or use toString from
    // ProductImageField to do comparison in getProductField(), keeping in mind the difference between
    // nutrients and NUTRITION

    @Test
    public void getProductField_returnsCorrectProductImageField() {
        ToUploadProduct toUploadProduct = new ToUploadProduct();
        toUploadProduct.setField("front");
        assertEquals(ProductImageField.FRONT, toUploadProduct.getProductField());

        toUploadProduct.setField("ingredients");
        assertEquals(ProductImageField.INGREDIENTS, toUploadProduct.getProductField());

        toUploadProduct.setField("nutrients");
        assertEquals(ProductImageField.NUTRITION, toUploadProduct.getProductField());

        toUploadProduct.setField("something else");
        assertEquals(ProductImageField.OTHER, toUploadProduct.getProductField());
    }
}
