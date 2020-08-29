package openfoodfacts.github.scrachx.openfood.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.images.ImageSize;
import openfoodfacts.github.scrachx.openfood.network.ApiFields;

import static org.apache.commons.lang.StringUtils.isNotBlank;

class ProductStringConverter extends StdConverter<String, String> {
    public String convert(String value) {
        return StringEscapeUtils.unescapeHtml(value).replace("\\'", "'").replace("&quot", "'");
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, Object> additionalProperties = new HashMap<>();
    @JsonProperty(ApiFields.Keys.ADDITIVES_TAGS)
    private final List<String> additivesTags = new ArrayList<>();
    private String allergens;
    @JsonProperty(ApiFields.Keys.ALLERGENS_HIERARCHY)
    private final List<String> allergensHierarchy = new ArrayList<>();
    @JsonProperty(ApiFields.Keys.ALLERGENS_TAGS)
    private List<String> allergensTags;
    @JsonProperty(ApiFields.Keys.AMINO_ACIDS_TAGS)
    private List<String> aminoAcidTags = new ArrayList<>();
    private String brands;
    @JsonProperty(ApiFields.Keys.BRANDS_TAGS)
    private final List<String> brandsTags = new ArrayList<>();
    @JsonProperty(ApiFields.Keys.CATEGORIES_TAGS)
    private List<String> categoriesTags;
    @JsonProperty(ApiFields.Keys.CITIES_TAGS)
    private final List<Object> citiesTags = new ArrayList<>();
    private String code;
    @JsonProperty(ApiFields.Keys.CONSERVATION_CONDITIONS)
    private String conservationConditions;
    private String countries;
    @JsonProperty(ApiFields.Keys.COUNTRIES_TAGS)
    private List<String> countriesTags;
    @JsonProperty(ApiFields.Keys.CREATED_DATE_TIME)
    private String createdDateTime;
    @JsonProperty(ApiFields.Keys.CREATOR)
    private String creator;
    @JsonProperty(ApiFields.Keys.CUSTOMER_SERVICE)
    private String customerService;
    @JsonProperty(ApiFields.Keys.EDITORS_TAGS)
    private final List<String> editorsTags = new ArrayList<>();
    @JsonProperty(ApiFields.Keys.EMB_CODES_TAGS)
    private final List<Object> embTags = new ArrayList<>();
    @JsonProperty(ApiFields.Keys.ENVIRONMENT_IMPACT_LEVEL_TAGS)
    private List<String> environmentImpactLevelTags;
    @JsonProperty(ApiFields.Keys.ENVIRONMENT_INFOCARD)
    private String environmentInfocard;
    @JsonProperty(ApiFields.Keys.GENERIC_NAME)
    @JsonDeserialize(converter = ProductStringConverter.class)
    private String genericName;
    @JsonProperty(ApiFields.Keys.IMAGE_FRONT_URL)
    private String imageFrontUrl;
    @JsonProperty(ApiFields.Keys.IMAGE_INGREDIENTS_URL)
    private String imageIngredientsUrl;
    @JsonProperty(ApiFields.Keys.IMAGE_NUTRITION_URL)
    private String imageNutritionUrl;
    @JsonProperty(ApiFields.Keys.IMAGE_SMALL_URL)
    private String imageSmallUrl;
    @JsonProperty(ApiFields.Keys.IMAGE_URL)
    private String imageUrl;
    @JsonProperty(ApiFields.Keys.INGREDIENTS)
    private final List<LinkedHashMap<String, String>> ingredients = new ArrayList<>();
    @JsonProperty(ApiFields.Keys.INGREDIENTS_ANALYSIS_TAGS)
    private final List<String> ingredientsAnalysisTags = new ArrayList<>();
    @JsonProperty(ApiFields.Keys.INGREDIENTS_MAY_PALM_OIL_N)
    private long ingredientsFromOrThatMayBeFromPalmOilN;
    @JsonProperty(ApiFields.Keys.INGREDIENTS_PALM_OIL_N)
    private long ingredientsFromPalmOilN;
    @JsonProperty(ApiFields.Keys.INGREDIENTS_FROM_PALM_OIL_TAGS)
    private final List<Object> ingredientsFromPalmOilTags = new ArrayList<>();
    @JsonProperty(ApiFields.Keys.INGREDIENTS_TEXT)
    @JsonDeserialize(converter = ProductStringConverter.class)
    private String ingredientsText;
    @JsonProperty(ApiFields.Keys.INGREDIENTS_MAY_PALM_OIL_TAGS)
    private final List<String> ingredientsThatMayBeFromPalmOilTags = new ArrayList<>();
    @JsonProperty(ApiFields.Keys.LABELS_HIERARCHY)
    private List<String> labelsHierarchy;
    @JsonProperty(ApiFields.Keys.LABELS_TAGS)
    private List<String> labelsTags;
    @JsonProperty(ApiFields.Keys.LANG)
    private String lang;
    @JsonProperty(ApiFields.Keys.LAST_MODIFIED_BY)
    private String lastModifiedBy;
    @JsonProperty(ApiFields.Keys.LAST_MODIFIED_TIME)
    private String lastModifiedTime;
    @JsonProperty(ApiFields.Keys.LINK)
    private String manufacturerUrl;
    @JsonProperty(ApiFields.Keys.MANUFACTURING_PLACES)
    private String manufacturingPlaces;
    @JsonProperty(ApiFields.Keys.MINERALS_TAGS)
    private List<String> mineralTags = new ArrayList<>();
    @JsonProperty(ApiFields.Keys.NO_NUTRITION_DATA)
    private String noNutritionData;
    @JsonProperty(ApiFields.Keys.NOVA_GROUPS)
    private String novaGroups;
    @JsonProperty(ApiFields.Keys.NUTRIENT_LEVELS)
    private NutrientLevels nutrientLevels;
    private Nutriments nutriments;
    @JsonProperty(ApiFields.Keys.NUTRITION_DATA_PER)
    private String nutritionDataPer;
    @JsonProperty(ApiFields.Keys.NUTRITION_GRADE_FR)
    private String nutritionGradeFr;
    private String origins;
    @JsonProperty(ApiFields.Keys.OTHER_INFORMATION)
    private String otherInformation;
    @JsonProperty(ApiFields.Keys.OTHER_NUTRITIONAL_SUBSTANCES_TAGS)
    private List<String> otherNutritionTags = new ArrayList<>();
    private String packaging;
    @JsonProperty(ApiFields.Keys.PRODUCT_NAME)
    @JsonDeserialize(converter = ProductStringConverter.class)
    private String productName;
    @JsonProperty(ApiFields.Keys.PURCHASE_PLACES)
    private String purchasePlaces;
    @JsonProperty(ApiFields.Keys.QUANTITY)
    private String quantity;
    @JsonProperty(ApiFields.Keys.RECYCLING_INSTRUCTIONS_TO_DISCARD)
    private String recyclingInstructionsToDiscard;
    @JsonProperty(ApiFields.Keys.RECYCLING_INSTRUCTIONS_TO_RECYCLE)
    private String recyclingInstructionsToRecycle;
    @JsonProperty(ApiFields.Keys.SERVING_SIZE)
    private String servingSize;
    @JsonProperty(ApiFields.Keys.STATES_TAGS)
    private final List<String> statesTags = new ArrayList<>();
    private String stores;
    private String traces;
    @JsonProperty(ApiFields.Keys.TRACES_TAGS)
    private final List<String> tracesTags = new ArrayList<>();
    private String url;
    @JsonProperty(ApiFields.Keys.VITAMINS_TAGS)
    private List<String> vitaminTags = new ArrayList<>();
    @JsonProperty(ApiFields.Keys.WARNING)
    private String warning;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public boolean hasProductNameIn(String languageCode) {
        return additionalProperties.get(ApiFields.Keys.lcProductNameKey(languageCode)) != null;
    }

    public String getGenericName(String languageCode) {
        String result = getFieldForLanguage("generic_name", languageCode);
        if (result != null) {
            return result;
        } else {
            return getGenericName();
        }
    }

    public String getIngredientsText(String languageCode) {
        String result = getFieldForLanguage(ApiFields.Keys.INGREDIENTS_TEXT, languageCode);
        if (result != null) {
            return result;
        } else {
            return getIngredientsText();
        }
    }

    public String getImageIngredientsUrl(String languageCode) {
        String result = getSelectedImage(languageCode, ProductImageField.INGREDIENTS, ImageSize.DISPLAY);
        if (StringUtils.isNotBlank(result)) {
            return result;
        } else {
            return getImageIngredientsUrl();
        }
    }

    public String getImageNutritionUrl(String languageCode) {
        String result = getSelectedImage(languageCode, ProductImageField.NUTRITION, ImageSize.DISPLAY);
        if (StringUtils.isNotBlank(result)) {
            return result;
        } else {
            return getImageNutritionUrl();
        }
    }

    @Nullable
    private String getFieldForLanguage(@NonNull String field, @NonNull String languageCode) {
        // First try the passed language
        if (!ApiFields.Defaults.DEFAULT_LANGUAGE.equals(languageCode) && additionalProperties.get(field + "_" + languageCode) != null
            && isNotBlank(additionalProperties.get(field + "_" + languageCode).toString())) {
            return additionalProperties.get(field + "_" + languageCode)
                .toString()
                .replace("\\'", "'")
                .replace("&quot", "'");
        } else if (additionalProperties.get(field + "_en") != null
            && isNotBlank(additionalProperties.get(field + "_en").toString())) { // Then try english
            return additionalProperties.get(field + "_en")
                .toString()
                .replace("\\'", "'")
                .replace("&quot", "'");
        } else {
            return null;
        }
    }

    /**
     * @return The statesTags
     */
    public List<String> getStatesTags() {
        return statesTags;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public String getWarning() {
        return warning;
    }

    /**
     * @return The vitaminTags
     */
    public List<String> getVitaminTags() {
        return vitaminTags;
    }

    public void setVitaminTags(List<String> vitaminTags) {
        this.vitaminTags = vitaminTags;
    }

    /**
     * @return The mineralsTags
     */
    public List<String> getMineralTags() {
        return mineralTags;
    }

    public void setMineralTags(List<String> mineralTags) {
        this.mineralTags = mineralTags;
    }

    /**
     * @return The aminoAcidTags
     */
    public List<String> getAminoAcidTags() {
        return aminoAcidTags;
    }

    public void setAminoAcidTags(List<String> aminoAcidTags) {
        this.aminoAcidTags = aminoAcidTags;
    }

    /**
     * @return The otherNutritionTags
     */
    public List<String> getOtherNutritionTags() {
        return otherNutritionTags;
    }

    public void setOtherNutritionTags(List<String> otherNutritionTags) {
        this.otherNutritionTags = otherNutritionTags;
    }

    /**
     * @return The imageSmallUrl
     */
    private String getImageSmallUrl() {
        return imageSmallUrl;
    }

    public String getImageSmallUrl(String languageCode) {
        String image = getSelectedImage(languageCode, ProductImageField.FRONT, ImageSize.SMALL);
        if (StringUtils.isNotBlank(image)) {
            return image;
        }
        return getImageSmallUrl();
    }

    public String getSelectedImage(String languageCode, ProductImageField type, ImageSize size) {
        Map<String, Map> images = (Map<String, Map>) additionalProperties.get(ApiFields.Keys.SELECTED_IMAGES);
        if (images != null) {
            images = (Map<String, Map>) images.get(type.name().toLowerCase());
            if (images != null) {
                Map<String, String> imagesByLocale = (Map<String, String>) images.get(size.name().toLowerCase());
                if (imagesByLocale != null) {
                    String url = imagesByLocale.get(languageCode);
                    if (StringUtils.isNotBlank(url)) {
                        return url;
                    }
                }
            }
        }
        switch (type) {
            case FRONT:
                return getImageUrl();
            case INGREDIENTS:
                return getImageIngredientsUrl();
            case NUTRITION:
                return getImageNutritionUrl();
            case OTHER:
                return null;
        }
        return null;
    }

    public List<String> getAvailableLanguageForImage(ProductImageField type, ImageSize size) {
        Map<String, Map<String, Map<String, String>>> images = (Map<String, Map<String, Map<String, String>>>) additionalProperties.get(ApiFields.Keys.SELECTED_IMAGES);
        if (images != null) {
            Map<String, Map<String, String>> imagesType = images.get(type.name().toLowerCase());
            if (imagesType != null) {
                Map<String, String> imagesByLocale = imagesType.get(size.name().toLowerCase());
                return new ArrayList<>(imagesByLocale.keySet());
            }
        }
        return Collections.emptyList();
    }

    public Map<String, ?> getImageDetails(String imageKey) {
        Map<String, Map<String, ?>> images = (Map<String, Map<String, ?>>) additionalProperties.get(ApiFields.Keys.IMAGES);
        if (images != null) {
            return images.get(imageKey);
        }
        return null;
    }

    public boolean isLanguageSupported(String languageCode) {
        Map<String, Map> languagesCodes = (Map<String, Map>) additionalProperties.get("languages_codes");
        return languageCode != null && languagesCodes != null && languagesCodes.containsKey(languageCode.toLowerCase());
    }

    /**
     * @return The imageFrontUrl
     */
    public String getImageFrontUrl() {
        return imageFrontUrl;
    }

    public String getImageFrontUrl(String languageCode) {
        String image = getSelectedImage(languageCode, ProductImageField.FRONT, ImageSize.DISPLAY);
        if (StringUtils.isNotBlank(image)) {
            return image;
        }
        return getImageFrontUrl();
    }

    /**
     * @return The imageIngredientsUrl
     */
    public String getImageIngredientsUrl() {
        return imageIngredientsUrl;
    }

    /**
     * @return The imageNutritionUrl
     */
    public String getImageNutritionUrl() {
        return imageNutritionUrl;
    }

    /**
     * @return The manufactureUrl
     */
    public String getManufacturerUrl() {
        return manufacturerUrl;
    }

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return The code
     */
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return The tracesTags
     */
    public List<String> getTracesTags() {
        return tracesTags;
    }

    /**
     * @return The ingredientsThatMayBeFromPalmOilTags
     */
    public List<String> getIngredientsThatMayBeFromPalmOilTags() {
        return ingredientsThatMayBeFromPalmOilTags;
    }

    /**
     * @return The additivesTags
     */
    public List<String> getAdditivesTags() {
        return additivesTags;
    }

    /**
     * @return The allergensHierarchy
     */
    public List<String> getAllergensHierarchy() {
        return allergensHierarchy;
    }

    /**
     * @return The manufacturingPlaces
     */
    public String getManufacturingPlaces() {
        return manufacturingPlaces;
    }

    /**
     * @return The nutriments
     */
    public Nutriments getNutriments() {
        return nutriments;
    }

    /**
     * @return The ingredientsFromPalmOilTags
     */
    public List<Object> getIngredientsFromPalmOilTags() {
        return ingredientsFromPalmOilTags;
    }

    /**
     * @return The brandsTags
     */
    public List<String> getBrandsTags() {
        return brandsTags;
    }

    /**
     * @return The traces
     */
    public String getTraces() {
        return traces;
    }

    /**
     * @return The categoriesTags
     */
    @Nullable
    public List<String> getCategoriesTags() {
        return categoriesTags;
    }

    /**
     * @return The ingredientsText
     */
    public String getIngredientsText() {
        return ingredientsText;
    }

    /**
     * Get the default product name.
     *
     * @return The default product name
     */
    @Nullable
    public String getProductName() {
        return productName;
    }

    /**
     * Get the product name for the specified language code. If null return default product name.
     *
     * @param languageCode The language code for the language we get the product in.
     * @return The product name for the specified language code.
     *     If null returns default product name.
     * @see #getProductName()
     */
    @Nullable
    public String getProductName(final String languageCode) {
        String result = getFieldForLanguage(ApiFields.Keys.PRODUCT_NAME, languageCode);
        if (result != null) {
            return result;
        } else {
            return getProductName();
        }
    }

    /**
     * @return The genericName
     */
    public String getGenericName() {
        return genericName;
    }

    /**
     * @return The ingredientsFromOrThatMayBeFromPalmOilN
     */
    public long getIngredientsFromOrThatMayBeFromPalmOilN() {
        return ingredientsFromOrThatMayBeFromPalmOilN;
    }

    /**
     * @return The servingSize
     */
    public String getServingSize() {
        return servingSize;
    }

    public List<String> getAllergensTags() {
        return allergensTags;
    }

    /**
     * @return The allergens
     */
    public String getAllergens() {
        return allergens;
    }

    /**
     * @return The origins
     */
    public String getOrigins() {
        return origins;
    }

    /**
     * @return The stores where the product is sold.
     */
    @Nullable
    public String getStores() {
        if (stores == null) {
            return null;
        }
        return stores.replace(",", ", ");
    }

    /**
     * @return The NutriScore as specified by the
     *     {@link ApiFields.Keys#NUTRITION_GRADE_FR} api field.
     */
    @Nullable
    public String getNutritionGradeFr() {
        return nutritionGradeFr;
    }

    /**
     * @return The nutrientLevels
     */
    public NutrientLevels getNutrientLevels() {
        return nutrientLevels;
    }

    /**
     * @return The countries where the product is sold.
     */
    @Nullable
    public String getCountries() {
        if (countries == null) {
            return null;
        }
        return countries.replace(",", ", ");
    }

    /**
     * @return The brands
     */
    @Nullable
    public String getBrands() {
        if (brands == null) {
            return null;
        }
        return brands.replace(",", ", ");
    }

    /**
     * @return The packaging
     */
    @Nullable
    public String getPackaging() {
        if (packaging == null) {
            return null;
        }
        return packaging.replace(",", ", ");
    }

    /**
     * @return The labels tags
     */
    public List<String> getLabelsTags() {
        return labelsTags;
    }

    /**
     * @return The labels hierarchy
     */
    public List<String> getLabelsHierarchy() {
        return labelsHierarchy;
    }

    /**
     * @return The citiesTags
     */
    public List<Object> getCitiesTags() {
        return citiesTags;
    }

    /**
     * @return The quantity
     */
    public String getQuantity() {
        return quantity;
    }

    /**
     * @return The ingredientsFromPalmOilN
     */
    public long getIngredientsFromPalmOilN() {
        return ingredientsFromPalmOilN;
    }

    /**
     * @return The imageUrl
     */
    private String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl(String languageCode) {
        String url = getSelectedImage(languageCode, ProductImageField.FRONT, ImageSize.DISPLAY);
        if (StringUtils.isNotBlank(url)) {
            return url;
        }
        return getImageUrl();
    }

    /**
     * @return The Emb_codes
     */
    public List<Object> getEmbTags() {
        return embTags;
    }

    public List<String> getCountriesTags() {
        return countriesTags;
    }

    public String getCreator() {
        return creator;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public List<String> getEditors() {
        return editorsTags;
    }

    public String getNovaGroups() {
        return novaGroups;
    }

    public List<String> getEnvironmentImpactLevelTags() {
        return environmentImpactLevelTags;
    }

    public String getLang() {
        return lang;
    }

    public String getPurchasePlaces() {
        return purchasePlaces;
    }

    public String getNutritionDataPer() {
        return nutritionDataPer;
    }

    public String getNoNutritionData() {
        return noNutritionData;
    }

    public String getEnvironmentInfocard() {
        return environmentInfocard;
    }

    public List<String> getIngredientsAnalysisTags() {
        return ingredientsAnalysisTags;
    }

    public List<LinkedHashMap<String, String>> getIngredients() {
        return ingredients;
    }

    /**
     * @return Other information
     */
    public String getOtherInformation() {
        return otherInformation;
    }

    /**
     * @return Conservation conditions
     */
    public String getConservationConditions() {
        return conservationConditions;
    }

    /**
     * @return Recycling instructions to discard
     */
    public String getRecyclingInstructionsToDiscard() {
        return recyclingInstructionsToDiscard;
    }

    /**
     * @return Recycling instructions to recycle
     */
    public String getRecyclingInstructionsToRecycle() {
        return recyclingInstructionsToRecycle;
    }

    @Nullable
    public String getNutritionGradeTag() {
        if (!additionalProperties.containsKey(ApiFields.Keys.NUTRITION_GRADE)) {
            return null;
        }
        List<String> nutritionGradeTags = (List<String>) additionalProperties.get(ApiFields.Keys.NUTRITION_GRADE);
        if (nutritionGradeTags == null || nutritionGradeTags.isEmpty()) {
            return null;
        }
        return nutritionGradeTags.get(0);
    }

    @NonNull
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("code", code)
            .append("productName", productName)
            .append("additional_properties", additionalProperties)
            .toString();
    }
}
