package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Tests for {@link SendProduct}
 */
public class SendProductTest {

    // TODO: add PowerMock to test compress()

    private static final long ID = 343L;
    private static final String BARCODE = "2302RER0";
    private static final String NAME = "product name";
    private static final String BRANDS = "Crunchy,Munches";
    private static final String WEIGHT = "50";
    private static final String WEIGHT_UNIT = "kg";
    private static final String IMG_UPLOAD_FRONT = "img front";
    private static final String IMG_UPLOAD_INGREDIENTS = "ingredients";
    private static final String IMG_UPLOAD_NUTRITION = "nutrition";
    private static final String LANG = "EN";

    private SendProduct sendProduct;

    @Before
    public void setup() {
        sendProduct = new SendProduct();
    }

    @Test
    public void getQuantityWithNullWeight_returnsNull() {
        assertNull(sendProduct.getQuantity());
    }

    @Test
    public void getQuantityWithWeightStringZeroLength_returnsNull() {
        sendProduct.setWeight("");
        assertNull(sendProduct.getQuantity());
    }

    @Test
    public void getQuantity_returnsWeightAndWeightUnit() {
        sendProduct.setWeight(WEIGHT);
        sendProduct.setWeight_unit(WEIGHT_UNIT);
        assertEquals(WEIGHT + " " + WEIGHT_UNIT, sendProduct.getQuantity());
    }

    @Test
    public void copy_copiesFromAnotherSendProduct() {
        SendProduct product1 = new SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
                IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        SendProduct product2 = new SendProduct();
        product2.copy(product1);
        assertEquals(BARCODE, product2.getBarcode());
        assertEquals(LANG, product2.getLang());
        assertEquals(NAME, product2.getName());
        assertEquals(BRANDS, product2.getBrands());
        assertEquals(WEIGHT, product2.getWeight());
        assertEquals(WEIGHT_UNIT, product2.getWeight_unit());
        assertEquals(IMG_UPLOAD_FRONT, product2.getImgupload_front());
        assertEquals(IMG_UPLOAD_INGREDIENTS, product2.getImgupload_ingredients());
        assertEquals(IMG_UPLOAD_NUTRITION, product2.getImgupload_nutrition());
    }

    @Test
    public void isEqualWithEqualProducts_returnsTrue() {
        SendProduct product1 = new SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
                IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        SendProduct product2 = new SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
                IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        assertTrue(product1.isEqual(product2));
    }

    @Test
    public void isEqualWithProductsDifferById_returnsTrue() {
        SendProduct product1 = new SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
                IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        SendProduct product2 = new SendProduct(567L, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
                IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        assertTrue(product1.isEqual(product2));
    }

    @Test
    public void isEqualWithDifferentProducts_returnsFalse() {
        SendProduct product1 = new SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
                IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        String differentBarcode = "different barcode";
        SendProduct product2 = new SendProduct(ID, differentBarcode, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT,
                IMG_UPLOAD_FRONT, IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        assertFalse(product1.isEqual(product2));
    }
}
