package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import openfoodfacts.github.scrachx.openfood.images.ImageSize
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.utils.ProductStringConverter
import java.io.Serializable
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
open class SearchProduct : Serializable {
    /**
     * @return The code
     */
    lateinit var code: String

    val barcode get() = code.asBarcode()

    /**
     * Get the default product name.
     *
     * @return The default product name
     */
    @JsonProperty(ApiFields.Keys.PRODUCT_NAME)
    @JsonDeserialize(converter = ProductStringConverter::class)
    val productName: String? = null

    @get:JsonAnyGetter
    val additionalProperties = HashMap<String, Any?>()

    /**
     * @return The imageIngredientsUrl
     */
    @JsonProperty(ApiFields.Keys.IMAGE_INGREDIENTS_URL)
    val imageIngredientsUrl: String? = null

    /**
     * @return The imagePackagingUrl
     */
    @JsonProperty(ApiFields.Keys.IMAGE_PACKAGING_URL)
    val imagePackagingUrl: String? = null

    /**
     * @return The imageNutritionUrl
     */
    @JsonProperty(ApiFields.Keys.IMAGE_NUTRITION_URL)
    val imageNutritionUrl: String? = null

    /**
     * @return The imageUrl
     */
    @JsonProperty(ApiFields.Keys.IMAGE_URL)
    var imageUrl: String? = null

    /**
     * @return The imageSmallUrl
     */
    @JsonProperty(ApiFields.Keys.IMAGE_SMALL_URL)
    protected val imageSmallUrl: String? = null

    /**
     * A string containing the brands, comma separated
     */
    var brands: String? = null

    /**
     * @return The quantity
     */
    @JsonProperty(ApiFields.Keys.QUANTITY)
    val quantity: String? = null

    /**
     * @return The NutriScore as specified by the
     * [ApiFields.Keys.NUTRITION_GRADE_FR] api field.
     */
    @JsonProperty(ApiFields.Keys.NUTRITION_GRADE_FR)
    val nutritionGradeFr: String? = null

    @JsonProperty(ApiFields.Keys.NOVA_GROUPS)
    val novaGroups: String? = null

    @JsonProperty(ApiFields.Keys.ECOSCORE)
    val ecoscore: String? = null

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any?) {
        additionalProperties[name] = value
    }

    fun getSelectedImage(languageCode: String?, type: ProductImageField, size: ImageSize): String? {
        var images = additionalProperties[ApiFields.Keys.SELECTED_IMAGES] as Map<String?, Map<*, *>>?
        if (images != null) {
            images = images[type.toString()] as Map<String?, Map<*, *>>?
            if (images != null) {
                val imagesByLocale = images[size.name.lowercase(Locale.ROOT)] as Map<String?, String>?
                if (imagesByLocale != null) {
                    val url = imagesByLocale[languageCode]
                    if (!url.isNullOrBlank()) {
                        return url
                    }
                }
            }
        }
        return when (type) {
            ProductImageField.FRONT -> imageUrl
            ProductImageField.INGREDIENTS -> imageIngredientsUrl
            ProductImageField.NUTRITION -> imageNutritionUrl
            ProductImageField.PACKAGING -> imagePackagingUrl
            ProductImageField.OTHER -> null
        }
    }

    fun getImageSmallUrl(languageCode: String?) =
        getSelectedImage(languageCode, ProductImageField.FRONT, ImageSize.SMALL)
            ?.ifBlank { null } ?: imageSmallUrl
}