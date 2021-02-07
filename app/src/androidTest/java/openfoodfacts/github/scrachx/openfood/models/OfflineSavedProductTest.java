package openfoodfacts.github.scrachx.openfood.models;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.network.ApiFields;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * Tests for {@link OfflineSavedProduct}
 */

@SmallTest
@RunWith(AndroidJUnit4.class)
public class OfflineSavedProductTest {

    private static final String BARCODE = "8888888888";
    private static final String LANG = "en";
    private static final String PRODUCT_NAME = "product name";
    private static final String QUANTITY = "200g";
    private static final String BRAND = "testing brand";
    private static final String PACKAGING = "carton";
    private static final String LABELS = "Halal, Brown Dot India";
    private static final String CATEGORIES = "Meats";
    private static final String EMB_CODE = "FR 40.001.053 EC";
    private static final String STORES = "store, store 2";
    private static final String COUNTRIES_WHERE_SOLD = "India, France";
    private static final String INGREDIENTS = "Maltodextrin, buttermilk, salt, monosodium glutamate, lactic acid, dried garlic, dried onion, spices, natural flavors (soy).";
    private static final String TRACES = "Gluten";
    private static final String SERVING_SIZE = "75g";
    private static final String ENERGY = "520";
    private static final String ENERGY_UNIT = "kcal";
    private static final String FAT = "25";
    private static final String FAT_UNIT = "g";

    private OfflineSavedProduct offlineSavedProduct;

    @Before
    public void setup() {
        offlineSavedProduct = new OfflineSavedProduct();
    }

    @Test
    public void getBarcodeWithNullBarcode_returnsNull() {
        assertNull(offlineSavedProduct.getBarcode());
    }

    @Test
    public void getBarcode_returnsBarcode() {
        offlineSavedProduct.setBarcode(BARCODE);
        assertEquals(BARCODE, offlineSavedProduct.getBarcode());
    }

    @Test
    public void getProductDetailsMapWithNullDetails_returnsNull() {
        assertNull(offlineSavedProduct.getProductDetailsMap());
    }

    @Test
    public void getProductDetailsMap_returnsProductDetailsMap() {
        Map<String, String> productDetails = new HashMap<>();
        productDetails.put(ApiFields.Keys.LANG, LANG);
        productDetails.put(ApiFields.Keys.PRODUCT_NAME, PRODUCT_NAME);
        productDetails.put(ApiFields.Keys.QUANTITY, QUANTITY);
        productDetails.put(ApiFields.Keys.BRANDS, BRAND);
        productDetails.put("packaging", PACKAGING);
        productDetails.put("categories", CATEGORIES);
        productDetails.put("labels", LABELS);
        productDetails.put("emb_codes", EMB_CODE);
        productDetails.put("stores", STORES);
        productDetails.put("countries", COUNTRIES_WHERE_SOLD);
        productDetails.put("ingredients_text", INGREDIENTS);
        productDetails.put("traces", TRACES);
        productDetails.put("serving_size", SERVING_SIZE);
        productDetails.put("nutrition_data_per", ApiFields.Defaults.NUTRITION_DATA_PER_100G);
        productDetails.put("nutriment_energy", ENERGY);
        productDetails.put("nutriment_energy_unit", ENERGY_UNIT);
        productDetails.put("nutriment_fat", FAT);
        productDetails.put("nutriment_fat_unit", FAT_UNIT);
        offlineSavedProduct.setProductDetailsMap(productDetails);
        assertEquals(productDetails, offlineSavedProduct.getProductDetailsMap());
    }

}
