package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import openfoodfacts.github.scrachx.openfood.models.entities.SendProduct;

import static com.google.common.truth.Truth.assertThat;

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
        assertThat(sendProduct.getQuantity()).isNull();
    }

    @Test
    public void getQuantityWithWeightStringZeroLength_returnsNull() {
        sendProduct.setWeight("");
        assertThat(sendProduct.getQuantity()).isNull();
    }

    @Test
    public void getQuantity_returnsWeightAndWeightUnit() {
        sendProduct.setWeight(WEIGHT);
        sendProduct.setWeight_unit(WEIGHT_UNIT);
        assertThat(sendProduct.getQuantity()).isEqualTo(WEIGHT + " " + WEIGHT_UNIT);
    }

    @Test
    public void copy_copiesFromAnotherSendProduct() {
        SendProduct product1 = new SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
            IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        SendProduct product2 = new SendProduct();
        product2.copy(product1);
        assertThat(product2.getBarcode()).isEqualTo(BARCODE);
        assertThat(product2.getLang()).isEqualTo(LANG);
        assertThat(product2.getName()).isEqualTo(NAME);
        assertThat(product2.getBrands()).isEqualTo(BRANDS);
        assertThat(product2.getWeight()).isEqualTo(WEIGHT);
        assertThat(product2.getWeight_unit()).isEqualTo(WEIGHT_UNIT);
        assertThat(product2.getImgupload_front()).isEqualTo(IMG_UPLOAD_FRONT);
        assertThat(product2.getImgupload_ingredients()).isEqualTo(IMG_UPLOAD_INGREDIENTS);
        assertThat(product2.getImgupload_nutrition()).isEqualTo(IMG_UPLOAD_NUTRITION);
    }

    @Test
    public void isEqualWithEqualProducts_returnsTrue() {
        SendProduct product1 = new SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
            IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        SendProduct product2 = new SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
            IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        assertThat(product1.isEqual(product2)).isTrue();
    }

    @Test
    public void isEqualWithProductsDifferById_returnsTrue() {
        SendProduct product1 = new SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
                IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        Long id = 567L;
        SendProduct product2 = new SendProduct(id, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
                IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        assertThat(product1.isEqual(product2)).isTrue();
    }

    @Test
    public void isEqualWithDifferentProducts_returnsFalse() {
        SendProduct product1 = new SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
            IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        String differentBarcode = "different barcode";
        SendProduct product2 = new SendProduct(ID, differentBarcode, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT,
            IMG_UPLOAD_FRONT, IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION);
        assertThat(product1.isEqual(product2)).isFalse();
    }
}
