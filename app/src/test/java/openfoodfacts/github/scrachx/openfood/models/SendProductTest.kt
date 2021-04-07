package openfoodfacts.github.scrachx.openfood.models

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.entities.SendProduct
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests for [SendProduct]
 */
@RunWith(RobolectricTestRunner::class)
class SendProductTest {
    private var sendProduct: SendProduct? = null

    @Before
    fun setup() {
        sendProduct = SendProduct()
    }

    @Test
    fun getQuantityWithNullWeight_returnsNull() {
        assertThat(sendProduct!!.quantity).isNull()
    }

    @Test
    fun getQuantityWithWeightStringZeroLength_returnsNull() {
        sendProduct!!.weight = ""
        assertThat(sendProduct!!.quantity).isNull()
    }

    @Test
    fun getQuantity_returnsWeightAndWeightUnit() {
        sendProduct!!.weight = WEIGHT
        sendProduct!!.weightUnit = WEIGHT_UNIT
        assertThat(sendProduct!!.quantity).isEqualTo("$WEIGHT $WEIGHT_UNIT")
    }

    @Test
    fun copy_copiesFromAnotherSendProduct() {
        val product1 = SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, IMG_UPLOAD_FRONT, IMG_UPLOAD_INGREDIENTS,
                IMG_UPLOAD_NUTRITION, IMG_UPLOAD_PACKAGING, WEIGHT_UNIT)
        val product2 = SendProduct(product1)
        assertThat(product2.barcode).isEqualTo(BARCODE)
        assertThat(product2.lang).isEqualTo(LANG)
        assertThat(product2.name).isEqualTo(NAME)
        assertThat(product2.brands).isEqualTo(BRANDS)
        assertThat(product2.weight).isEqualTo(WEIGHT)
        assertThat(product2.weightUnit).isEqualTo(WEIGHT_UNIT)
        assertThat(product2.imguploadFront).isEqualTo(IMG_UPLOAD_FRONT)
        assertThat(product2.imguploadIngredients).isEqualTo(IMG_UPLOAD_INGREDIENTS)
        assertThat(product2.imguploadNutrition).isEqualTo(IMG_UPLOAD_NUTRITION)
    }

    @Test
    fun isEqualWithEqualProducts_returnsTrue() {
        val product1 = SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
                IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION, IMG_UPLOAD_PACKAGING)
        val product2 = SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
                IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION, IMG_UPLOAD_PACKAGING)
        assertThat(product1.isEqual(product2)).isTrue()
    }

    @Test
    fun isEqualWithProductsDifferById_returnsTrue() {
        val product1 = SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
                IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION, IMG_UPLOAD_PACKAGING)
        val id = 567L
        val product2 = SendProduct(id, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
                IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION, IMG_UPLOAD_PACKAGING)
        assertThat(product1.isEqual(product2)).isTrue()
    }

    @Test
    fun isEqualWithDifferentProducts_returnsFalse(): Unit {
        val product1 = SendProduct(ID, BARCODE, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT, IMG_UPLOAD_FRONT,
                IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION, IMG_UPLOAD_PACKAGING)
        val differentBarcode = "different barcode"
        val product2 = SendProduct(ID, differentBarcode, LANG, NAME, BRANDS, WEIGHT, WEIGHT_UNIT,
                IMG_UPLOAD_FRONT, IMG_UPLOAD_INGREDIENTS, IMG_UPLOAD_NUTRITION, IMG_UPLOAD_PACKAGING)
        assertThat(product1.isEqual(product2)).isFalse()
    }

    companion object {
        // TODO: add PowerMock to test compress()
        private const val ID = 343L
        private const val BARCODE = "2302RER0"
        private const val NAME = "product name"
        private const val BRANDS = "Crunchy,Munches"
        private const val WEIGHT = "50"
        private const val WEIGHT_UNIT = "kg"
        private const val IMG_UPLOAD_FRONT = "img front"
        private const val IMG_UPLOAD_INGREDIENTS = "ingredients"
        private const val IMG_UPLOAD_NUTRITION = "nutrition"
        private const val IMG_UPLOAD_PACKAGING = "packaging"
        private const val LANG = "EN"
    }
}