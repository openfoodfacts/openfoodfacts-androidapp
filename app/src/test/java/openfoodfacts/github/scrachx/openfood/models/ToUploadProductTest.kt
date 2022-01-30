package openfoodfacts.github.scrachx.openfood.models

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProduct
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for [ToUploadProduct]
 */
class ToUploadProductTest {
    // TODO: should make product field strings public static fields in ToUploadProduct or use toString from
    // ProductImageField to do comparison in getProductField(), keeping in mind the difference between
    // nutrients and NUTRITION
    private lateinit var toUploadProduct: ToUploadProduct

    @BeforeEach
    fun setUp() {
        toUploadProduct = ToUploadProduct()
    }

    @Test
    fun `getProductField returns correct ProductImageField`() {
        toUploadProduct.field = "front"
        assertThat(toUploadProduct.productField).isEqualTo(ProductImageField.FRONT)
        toUploadProduct.field = "ingredients"
        assertThat(toUploadProduct.productField).isEqualTo(ProductImageField.INGREDIENTS)
        toUploadProduct.field = "nutrients"
        assertThat(toUploadProduct.productField).isEqualTo(ProductImageField.NUTRITION)
        toUploadProduct.field = "something else"
        assertThat(toUploadProduct.productField).isEqualTo(ProductImageField.OTHER)
    }

    @Test
    fun `Fills correctly its fields`() {
        val id = 1L
        val barcode = "CSE370"
        val imageFilePath = "C:\\Images\\Example.pdf"
        val uploaded = false
        val field = "front"
        toUploadProduct = ToUploadProduct(id, barcode, imageFilePath, uploaded, field)
        assertThat(toUploadProduct.id).isEqualTo(id)
        assertThat(toUploadProduct.barcode).isEqualTo(barcode)
        assertThat(toUploadProduct.imageFilePath).isEqualTo(imageFilePath)
        assertThat(toUploadProduct.uploaded).isEqualTo(uploaded)
        assertThat(toUploadProduct.field).isEqualTo(field)
    }
}