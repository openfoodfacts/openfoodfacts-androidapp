package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import openfoodfacts.github.scrachx.openfood.images.ImageSize
import openfoodfacts.github.scrachx.openfood.models.entities.attribute.AttributeGroup
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.ApiFields.Keys.lcProductNameKey
import openfoodfacts.github.scrachx.openfood.utils.ProductStringConverter
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import java.util.*
import kotlin.collections.ArrayList

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class Product : SearchProduct() {
    /**
     * @return The additivesTags
     */
    @JsonProperty(ApiFields.Keys.ADDITIVES_TAGS)
    val additivesTags: ArrayList<String> = arrayListOf<String>()

    /**
     * @return The allergens
     */
    val allergens: String? = null

    /**
     * @return The allergensHierarchy
     */
    @JsonProperty(ApiFields.Keys.ALLERGENS_HIERARCHY)
    val allergensHierarchy: ArrayList<String> = arrayListOf<String>()

    @JsonProperty(ApiFields.Keys.ALLERGENS_TAGS)
    val allergensTags: ArrayList<String> = arrayListOf<String>()

    /**
     * @return The aminoAcidTags
     */
    @JsonProperty(ApiFields.Keys.AMINO_ACIDS_TAGS)
    var aminoAcidTags: ArrayList<String> = arrayListOf<String>()


    /**
     * @return The brandsTags
     */
    @JsonProperty(ApiFields.Keys.BRANDS_TAGS)
    val brandsTags: ArrayList<String> = arrayListOf<String>()

    /**
     * @return The categoriesTags
     */
    @JsonProperty(ApiFields.Keys.CATEGORIES_TAGS)
    val categoriesTags: List<String>? = null

    /**
     * @return The citiesTags
     */
    @JsonProperty(ApiFields.Keys.CITIES_TAGS)
    val citiesTags: ArrayList<Any> = arrayListOf()

    /**
     * @return Conservation conditions
     */
    @JsonProperty(ApiFields.Keys.CONSERVATION_CONDITIONS)
    val conservationConditions: String? = null

    /**
     *  The countries where the product is sold.
     */
    val countries: String? = null
        get() = field?.replace(",", ", ")

    @JsonProperty(ApiFields.Keys.COUNTRIES_TAGS)
    val countriesTags: List<String>? = null

    @JsonProperty(ApiFields.Keys.CREATED_DATE_TIME)
    val createdDateTime: String? = null

    @JsonProperty(ApiFields.Keys.CREATOR)
    val creator: String? = null

    @JsonProperty(ApiFields.Keys.CUSTOMER_SERVICE)
    private val customerService: String? = null

    @JsonProperty(ApiFields.Keys.EDITORS_TAGS)
    val editors: ArrayList<String> = ArrayList<String>()

    /**
     * @return The Emb_codes
     */
    @JsonProperty(ApiFields.Keys.EMB_CODES_TAGS)
    val embTags: ArrayList<Any> = ArrayList<Any>()

    @JsonProperty(ApiFields.Keys.ENVIRONMENT_IMPACT_LEVEL_TAGS)
    val environmentImpactLevelTags: List<String>? = null

    @JsonProperty(ApiFields.Keys.ENVIRONMENT_INFOCARD)
    val environmentInfoCard: String? = null

    /**
     * @return The genericName
     */
    @JsonProperty(ApiFields.Keys.GENERIC_NAME)
    @JsonDeserialize(converter = ProductStringConverter::class)
    val genericName: String? = null

    /**
     * @return The imageFrontUrl
     */
    @JsonProperty(ApiFields.Keys.IMAGE_FRONT_URL)
    val imageFrontUrl: String? = null

    @JsonProperty(ApiFields.Keys.INGREDIENTS)
    val ingredients = arrayListOf<Map<String, Any>>()

    @JsonProperty(ApiFields.Keys.INGREDIENTS_ANALYSIS_TAGS)
    val ingredientsAnalysisTags = arrayListOf<String>()

    /**
     * @return The ingredientsFromOrThatMayBeFromPalmOilN
     */
    @JsonProperty(ApiFields.Keys.INGREDIENTS_MAY_PALM_OIL_N)
    val ingredientsFromOrThatMayBeFromPalmOilN: Long = 0

    /**
     * @return The ingredientsFromPalmOilN
     */
    @JsonProperty(ApiFields.Keys.INGREDIENTS_PALM_OIL_N)
    val ingredientsFromPalmOilN: Long = 0

    /**
     * @return The ingredientsText
     */
    @JsonProperty(ApiFields.Keys.INGREDIENTS_TEXT)
    @JsonDeserialize(converter = ProductStringConverter::class)
    val ingredientsText: String? = null

    /**
     * @return The ingredientsThatMayBeFromPalmOilTags
     */
    @JsonProperty(ApiFields.Keys.INGREDIENTS_MAY_PALM_OIL_TAGS)
    val ingredientsThatMayBeFromPalmOilTags = arrayListOf<String>()

    /**
     * @return The labels hierarchy
     */
    @JsonProperty(ApiFields.Keys.LABELS_HIERARCHY)
    val labelsHierarchy: List<String>? = null

    /**
     * @return The labels tags
     */
    @JsonProperty(ApiFields.Keys.LABELS_TAGS)
    val labelsTags: List<String>? = null

    @JsonProperty(ApiFields.Keys.LANG)
    var lang: String = Locale.getDefault().language

    @JsonProperty(ApiFields.Keys.LAST_MODIFIED_BY)
    val lastModifiedBy: String? = null

    @JsonProperty(ApiFields.Keys.LAST_MODIFIED_TIME)
    val lastModifiedTime: String? = null

    /**
     * @return The manufactureUrl
     */
    @JsonProperty(ApiFields.Keys.LINK)
    val manufacturerUrl: String? = null

    /**
     * @return The manufacturingPlaces
     */
    @JsonProperty(ApiFields.Keys.MANUFACTURING_PLACES)
    val manufacturingPlaces: String? = null

    /**
     * @return The mineralsTags
     */
    @JsonProperty(ApiFields.Keys.MINERALS_TAGS)
    var mineralTags: ArrayList<String> = arrayListOf<String>()

    @JsonProperty(ApiFields.Keys.NO_NUTRITION_DATA)
    val noNutritionData: String? = null

    fun isNoNutrition(): Boolean = noNutritionData.contentEquals("on", true)

    /**
     * The nutrientLevels
     */
    @JsonProperty(ApiFields.Keys.NUTRIENT_LEVELS)
    val nutrientLevels: NutrientLevels? = null

    /**
     * The nutriments
     */
    @JsonProperty(ApiFields.Keys.NUTRIMENTS)
    var nutriments: ProductNutriments = ProductNutriments()

    @JsonProperty(ApiFields.Keys.NUTRITION_DATA_PER)
    val nutritionDataPer: String? = null

    /**
     * @return The origins
     */
    val origins: String? = null

    /**
     * @return Other information
     */
    @JsonProperty(ApiFields.Keys.OTHER_INFORMATION)
    val otherInformation: String? = null

    /**
     * @return The otherNutritionTags
     */
    @JsonProperty(ApiFields.Keys.OTHER_NUTRITIONAL_SUBSTANCES_TAGS)
    var otherNutritionTags: ArrayList<String> = arrayListOf<String>()

    val packaging: String? = null
        get() = field?.replace(",", ", ")


    @JsonProperty(ApiFields.Keys.PURCHASE_PLACES)
    val purchasePlaces: String? = null

    /**
     * @return Recycling instructions to discard
     */
    @JsonProperty(ApiFields.Keys.RECYCLING_INSTRUCTIONS_TO_DISCARD)
    val recyclingInstructionsToDiscard: String? = null

    /**
     * @return Recycling instructions to recycle
     */
    @JsonProperty(ApiFields.Keys.RECYCLING_INSTRUCTIONS_TO_RECYCLE)
    val recyclingInstructionsToRecycle: String? = null

    /**
     * @return The servingSize
     */
    @JsonProperty(ApiFields.Keys.SERVING_SIZE)
    val servingSize: String? = null

    /**
     * @return The statesTags
     */
    @JsonProperty(ApiFields.Keys.STATES_TAGS)
    val statesTags: ArrayList<String> = arrayListOf<String>()

    val stores: String? = null
        get() = field?.replace(",", ", ")

    /**
     * @return The traces
     */
    val traces: String? = null

    /**
     * @return The tracesTags
     */
    @JsonProperty(ApiFields.Keys.TRACES_TAGS)
    val tracesTags: ArrayList<String> = arrayListOf()

    /**
     * @return The url
     */
    val url: String? = null

    /**
     * @return The vitaminTags
     */
    @JsonProperty(ApiFields.Keys.VITAMINS_TAGS)
    var vitaminTags: ArrayList<String>? = arrayListOf<String>()

    @JsonProperty(ApiFields.Keys.WARNING)
    val warning: String? = null


    fun hasProductNameIn(languageCode: String?) =
        additionalProperties[lcProductNameKey(languageCode!!)] != null

    fun getGenericName(languageCode: String) =
        getFieldForLanguage("generic_name", languageCode) ?: genericName

    fun getIngredientsText(languageCode: String) =
        getFieldForLanguage(ApiFields.Keys.INGREDIENTS_TEXT, languageCode) ?: ingredientsText

    fun getImageIngredientsUrl(languageCode: String?): String? {
        val result = getSelectedImage(languageCode, ProductImageField.INGREDIENTS, ImageSize.DISPLAY)
        return if (!result.isNullOrBlank()) result else imageIngredientsUrl
    }

    fun getImagePackagingUrl(languageCode: String?): String? {
        val result = getSelectedImage(languageCode, ProductImageField.PACKAGING, ImageSize.DISPLAY)
        return if (!result.isNullOrBlank()) result else imagePackagingUrl
    }

    fun getImageNutritionUrl(languageCode: String?): String? {
        val result = getSelectedImage(languageCode, ProductImageField.NUTRITION, ImageSize.DISPLAY)
        return if (!result.isNullOrBlank()) result else imageNutritionUrl
    }

    private fun getFieldForLanguage(field: String, languageCode: String) =
        additionalProperties["${field}_$languageCode"] // First try the passed language
            ?.toString()
            ?.ifBlank { null }
            ?.replace("\\'", "'")
            ?.replace("&quot", "'")
            ?: additionalProperties["${field}_${ApiFields.Defaults.DEFAULT_LANGUAGE}"] // Then try english
                ?.toString()
                ?.ifBlank { null }
                ?.replace("\\'", "'")
                ?.replace("&quot", "'")


    fun getAvailableLanguageForImage(type: ProductImageField, size: ImageSize): List<String> {
        val images =
            additionalProperties[ApiFields.Keys.SELECTED_IMAGES] as Map<String, Map<String, Map<String, String>>>?
        if (images != null) {
            val imagesType = images[type.name.lowercase(Locale.ROOT)]
            if (imagesType != null) {
                val imagesByLocale = imagesType[size.name.lowercase(Locale.ROOT)] ?: error("")
                return ArrayList(imagesByLocale.keys)
            }
        }
        return emptyList()
    }

    fun getImageDetails(imageKey: String) =
        (additionalProperties[ApiFields.Keys.IMAGES] as Map<String, Map<String, *>>?)?.get(imageKey)


    fun isLanguageSupported(languageCode: String?): Boolean {
        val languagesCodes = additionalProperties["languages_codes"] as Map<String, Map<*, *>>?
        return languageCode != null
                && languagesCodes != null
                && languagesCodes.containsKey(languageCode.lowercase(Locale.ROOT))
    }

    fun getImageFrontUrl(languageCode: String?): String? {
        val image = getSelectedImage(languageCode, ProductImageField.FRONT, ImageSize.DISPLAY)
        return if (image.isNullOrBlank()) image else imageFrontUrl
    }

    /**
     * Get the product name for the specified language code. If null return default product name.
     *
     * @param languageCode The language code for the language we get the product in.
     * @return The product name for the specified language code.
     * If null returns default product name.
     */
    fun getProductName(languageCode: String) =
        getFieldForLanguage(ApiFields.Keys.PRODUCT_NAME, languageCode) ?: productName

    fun getImageUrl(languageCode: String?): String? {
        val url = getSelectedImage(languageCode, ProductImageField.FRONT, ImageSize.DISPLAY)
        return if (!url.isNullOrBlank()) url else imageUrl
    }

    fun getNutritionGradeTag(): String? {
        if (!additionalProperties.containsKey(ApiFields.Keys.NUTRITION_GRADE)) return null
        val nutritionGradeTags = additionalProperties[ApiFields.Keys.NUTRITION_GRADE] as List<String>?
        return if (nutritionGradeTags != null && nutritionGradeTags.isNotEmpty()) nutritionGradeTags[0] else null
    }

    fun getAttributeGroups(language: String): List<AttributeGroup> {
        val attributeGroups = additionalProperties["${ApiFields.Keys.ATTRIBUTE_GROUPS}_${language}"] as Any
        return jacksonObjectMapper().convertValue(attributeGroups)
    }

    @Suppress("DEPRECATION")
    override fun toString() = ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("code", code)
        .append("productName", productName)
        .append("additional_properties", additionalProperties)
        .toString()

    companion object {
        private const val serialVersionUID = 1L
    }
}
