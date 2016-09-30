package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "last_edit_dates_tags",
        "labels_hierarchy",
        "_id",
        "categories_hierarchy",
        "pnns_groups_1",
        "checkers_tags",
        "states_tags",
        "labels_tags",
        "image_small_url",
        "image_nutrition_url",
        "image_front_url",
        "url",
        "code",
        "traces_tags",
        "lang",
        "photographers",
        "ingredients_that_may_be_from_palm_oil_tags",
        "generic_name",
        "old_additives_tags",
        "rev",
        "_keywords",
        "emb_codes",
        "editors",
        "max_imgid",
        "additives_tags",
        "emb_codes_orig",
        "nutrient_levels_tags",
        "informers_tags",
        "photographers_tags",
        "additives_n",
        "allergens_hierarchy",
        "pnns_groups_2_tags",
        "unknown_nutrients_tags",
        "packaging_tags",
        "manufacturing_places",
        "unique_scans_n",
        "nutriments",
        "countries_tags",
        "ingredients_from_palm_oil_tags",
        "purchase_places",
        "emb_codes_tags",
        "brands_tags",
        "pnns_groups_2",
        "countries_hierarchy",
        "traces",
        "categories",
        "ingredients_text",
        "created_t",
        "product_name",
        "creator",
        "ingredients_from_or_that_may_be_from_palm_oil_n",
        "serving_size",
        "no_nutrition_data",
        "completed_t",
        "last_modified_by",
        "allergens",
        "new_additives_n",
        "origins",
        "stores",
        "nutrition_grade_fr",
        "nutrient_levels",
        "id",
        "stores_tags",
        "countries",
        "purchase_places_tags",
        "fruits-vegetables-nuts_100g_estimate",
        "interface_version_modified",
        "sortkey",
        "last_modified_t",
        "nutrition_score_debug",
        "countries.20131227",
        "correctors_tags",
        "new_additives_debug",
        "correctors",
        "brands",
        "ingredients_tags",
        "new_additives_tags",
        "informers",
        "states",
        "entry_dates_tags",
        "nutrition_grades_tags",
        "ingredients_text_with_allergens",
        "packaging",
        "serving_quantity",
        "manufacturing_places_tags",
        "origins_tags",
        "scans_n",
        "nutrition_data_per",
        "labels",
        "link",
        "cities_tags",
        "emb_codes_20141016",
        "categories_tags",
        "quantity",
        "expiration_date",
        "ingredients_that_may_be_from_palm_oil_n",
        "states_hierarchy",
        "emb_code",
        "allergens_tags",
        "ingredients_from_palm_oil_n",
        "image_url",
        "lc",
        "ingredients",
        "pnns_groups_1_tags",
        "checkers",
        "complete"
})
public class Product implements Serializable{

    @JsonProperty("last_edit_dates_tags")
    private List<String> lastEditDatesTags = new ArrayList<>();
    @JsonProperty("labels_hierarchy")
    private List<Object> labelsHierarchy = new ArrayList<>();
    @JsonProperty("_id")
    private String Id;
    @JsonProperty("categories_hierarchy")
    private List<String> categoriesHierarchy = new ArrayList<>();
    @JsonProperty("pnns_groups_1")
    private String pnnsGroups1;
    @JsonProperty("checkers_tags")
    private List<Object> checkersTags = new ArrayList<>();
    @JsonProperty("states_tags")
    private List<String> statesTags = new ArrayList<>();
    @JsonProperty("labels_tags")
    private List<Object> labelsTags = new ArrayList<>();
    @JsonProperty("image_small_url")
    private String imageSmallUrl;
    @JsonProperty("image_nutrition_url")
    private String imageNutritionUrl;
    @JsonProperty("image_front_url")
    private String imageFrontUrl;
    @JsonProperty("image_ingredients_url")
    private String imageIngredientsUrl;
    private String url;
    private String code;
    @JsonProperty("traces_tags")
    private List<String> tracesTags = new ArrayList<>();
    private String lang;
    private List<String> photographers = new ArrayList<>();
    @JsonProperty("ingredients_that_may_be_from_palm_oil_tags")
    private List<String> ingredientsThatMayBeFromPalmOilTags = new ArrayList<>();
    @JsonProperty("generic_name")
    private String genericName;
    @JsonProperty("old_additives_tags")
    private List<String> oldAdditivesTags = new ArrayList<>();
    private long rev;
    @JsonProperty("_keywords")
    private List<String> Keywords = new ArrayList<>();
    @JsonProperty("emb_codes")
    private String embCodes;
    private List<String> editors = new ArrayList<>();
    @JsonProperty("max_imgid")
    private String maxImgid;
    @JsonProperty("additives_tags")
    private List<String> additivesTags = new ArrayList<>();
    @JsonProperty("emb_codes_orig")
    private String embCodesOrig;
    @JsonProperty("nutrient_levels_tags")
    private List<String> nutrientLevelsTags = new ArrayList<>();
    @JsonProperty("informers_tags")
    private List<String> informersTags = new ArrayList<>();
    @JsonProperty("photographers_tags")
    private List<String> photographersTags = new ArrayList<>();
    @JsonProperty("additives_n")
    private long additivesN;
    @JsonProperty("allergens_hierarchy")
    private List<String> allergensHierarchy = new ArrayList<>();
    @JsonProperty("pnns_groups_2_tags")
    private List<String> pnnsGroups2Tags = new ArrayList<>();
    @JsonProperty("unknown_nutrients_tags")
    private List<Object> unknownNutrientsTags = new ArrayList<>();
    @JsonProperty("packaging_tags")
    private List<String> packagingTags = new ArrayList<>();
    @JsonProperty("manufacturing_places")
    private String manufacturingPlaces;
    @JsonProperty("unique_scans_n")
    private long uniqueScansN;
    private Nutriments nutriments;
    @JsonProperty("countries_tags")
    private List<String> countriesTags = new ArrayList<>();
    @JsonProperty("ingredients_from_palm_oil_tags")
    private List<Object> ingredientsFromPalmOilTags = new ArrayList<>();
    @JsonProperty("purchase_places")
    private String purchasePlaces;
    @JsonProperty("emb_codes_tags")
    private List<Object> embCodesTags = new ArrayList<>();
    @JsonProperty("brands_tags")
    private List<String> brandsTags = new ArrayList<>();
    @JsonProperty("pnns_groups_2")
    private String pnnsGroups2;
    @JsonProperty("countries_hierarchy")
    private List<String> countriesHierarchy = new ArrayList<>();
    private String traces;
    private String categories;
    @JsonProperty("ingredients_text")
    private String ingredientsText;
    @JsonProperty("created_t")
    private long createdT;
    @JsonProperty("product_name")
    private String productName;
    private String creator;
    @JsonProperty("ingredients_from_or_that_may_be_from_palm_oil_n")
    private long ingredientsFromOrThatMayBeFromPalmOilN;
    @JsonProperty("serving_size")
    private String servingSize;
    @JsonProperty("no_nutrition_data")
    private Object noNutritionData;
    @JsonProperty("completed_t")
    private long completedT;
    @JsonProperty("last_modified_by")
    private String lastModifiedBy;
    private String allergens;
    @JsonProperty("new_additives_n")
    private long newAdditivesN;
    private String origins;
    private String stores;
    @JsonProperty("nutrition_grade_fr")
    private String nutritionGradeFr;
    @JsonProperty("nutrient_levels")
    private NutrientLevels nutrientLevels;
    private String id;
    @JsonProperty("stores_tags")
    private List<Object> storesTags = new ArrayList<>();
    private String countries;
    @JsonProperty("purchase_places_tags")
    private List<Object> purchasePlacesTags = new ArrayList<>();
    @JsonProperty("fruits-vegetables-nuts_100g_estimate")
    private double fruitsVegetablesNuts100gEstimate;
    @JsonProperty("interface_version_modified")
    private String interfaceVersionModified;
    private long sortkey;
    @JsonProperty("last_modified_t")
    private long lastModifiedT;
    @JsonProperty("nutrition_score_debug")
    private String nutritionScoreDebug;
    @JsonProperty("countries.20131227")
    private Object countries20131227;
    @JsonProperty("correctors_tags")
    private List<String> correctorsTags = new ArrayList<>();
    @JsonProperty("new_additives_debug")
    private String newAdditivesDebug;
    private List<String> correctors = new ArrayList<>();
    private String brands;
    @JsonProperty("ingredients_tags")
    private List<String> ingredientsTags = new ArrayList<>();
    @JsonProperty("new_additives_tags")
    private List<String> newAdditivesTags = new ArrayList<>();
    private List<String> informers = new ArrayList<>();
    private String states;
    @JsonProperty("entry_dates_tags")
    private List<String> entryDatesTags = new ArrayList<>();
    @JsonProperty("nutrition_grades_tags")
    private List<String> nutritionGradesTags = new ArrayList<>();
    @JsonProperty("ingredients_text_with_allergens")
    private String ingredientsTextWithAllergens;
    private String packaging;
    @JsonProperty("serving_quantity")
    private double servingQuantity;
    @JsonProperty("manufacturing_places_tags")
    private List<Object> manufacturingPlacesTags = new ArrayList<>();
    @JsonProperty("origins_tags")
    private List<String> originsTags = new ArrayList<>();
    @JsonProperty("scans_n")
    private long scansN;
    @JsonProperty("nutrition_data_per")
    private String nutritionDataPer;
    private String labels;
    private String link;
    @JsonProperty("cities_tags")
    private List<Object> citiesTags = new ArrayList<>();
    @JsonProperty("emb_codes_20141016")
    private String embCodes20141016;
    @JsonProperty("categories_tags")
    private List<String> categoriesTags = new ArrayList<>();
    private String quantity;
    @JsonProperty("expiration_date")
    private String expirationDate;
    @JsonProperty("ingredients_that_may_be_from_palm_oil_n")
    private long ingredientsThatMayBeFromPalmOilN;
    @JsonProperty("states_hierarchy")
    private List<String> statesHierarchy = new ArrayList<>();
    @JsonProperty("emb_code")
    private String embCode;
    @JsonProperty("allergens_tags")
    private List<Object> allergensTags = new ArrayList<>();
    @JsonProperty("ingredients_from_palm_oil_n")
    private long ingredientsFromPalmOilN;
    @JsonProperty("image_url")
    private String imageUrl;
    private String lc;
    private List<Ingredient> ingredients = new ArrayList<>();
    @JsonProperty("pnns_groups_1_tags")
    private List<String> pnnsGroups1Tags = new ArrayList<>();
    private List<Object> checkers = new ArrayList<>();
    private long complete;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    /**
     *
     * @return
     * The lastEditDatesTags
     */
    public List<String> getLastEditDatesTags() {
        return lastEditDatesTags;
    }

    /**
     *
     * @param lastEditDatesTags
     * The last_edit_dates_tags
     */
    public void setLastEditDatesTags(List<String> lastEditDatesTags) {
        this.lastEditDatesTags = lastEditDatesTags;
    }

    public Product withLastEditDatesTags(List<String> lastEditDatesTags) {
        this.lastEditDatesTags = lastEditDatesTags;
        return this;
    }

    /**
     *
     * @return
     * The labelsHierarchy
     */
    public List<Object> getLabelsHierarchy() {
        return labelsHierarchy;
    }

    /**
     *
     * @param labelsHierarchy
     * The labels_hierarchy
     */
    public void setLabelsHierarchy(List<Object> labelsHierarchy) {
        this.labelsHierarchy = labelsHierarchy;
    }

    public Product withLabelsHierarchy(List<Object> labelsHierarchy) {
        this.labelsHierarchy = labelsHierarchy;
        return this;
    }

    /**
     *
     * @return
     * The categoriesHierarchy
     */
    public List<String> getCategoriesHierarchy() {
        return categoriesHierarchy;
    }

    /**
     *
     * @param categoriesHierarchy
     * The categories_hierarchy
     */
    public void setCategoriesHierarchy(List<String> categoriesHierarchy) {
        this.categoriesHierarchy = categoriesHierarchy;
    }

    public Product withCategoriesHierarchy(List<String> categoriesHierarchy) {
        this.categoriesHierarchy = categoriesHierarchy;
        return this;
    }

    /**
     *
     * @return
     * The pnnsGroups1
     */
    public String getPnnsGroups1() {
        return pnnsGroups1;
    }

    /**
     *
     * @param pnnsGroups1
     * The pnns_groups_1
     */
    public void setPnnsGroups1(String pnnsGroups1) {
        this.pnnsGroups1 = pnnsGroups1;
    }

    public Product withPnnsGroups1(String pnnsGroups1) {
        this.pnnsGroups1 = pnnsGroups1;
        return this;
    }

    /**
     *
     * @return
     * The checkersTags
     */
    public List<Object> getCheckersTags() {
        return checkersTags;
    }

    /**
     *
     * @param checkersTags
     * The checkers_tags
     */
    public void setCheckersTags(List<Object> checkersTags) {
        this.checkersTags = checkersTags;
    }

    public Product withCheckersTags(List<Object> checkersTags) {
        this.checkersTags = checkersTags;
        return this;
    }

    /**
     *
     * @return
     * The statesTags
     */
    public List<String> getStatesTags() {
        return statesTags;
    }

    /**
     *
     * @param statesTags
     * The states_tags
     */
    public void setStatesTags(List<String> statesTags) {
        this.statesTags = statesTags;
    }

    public Product withStatesTags(List<String> statesTags) {
        this.statesTags = statesTags;
        return this;
    }

    /**
     *
     * @return
     * The labelsTags
     */
    public List<Object> getLabelsTags() {
        return labelsTags;
    }

    /**
     *
     * @param labelsTags
     * The labels_tags
     */
    public void setLabelsTags(List<Object> labelsTags) {
        this.labelsTags = labelsTags;
    }

    public Product withLabelsTags(List<Object> labelsTags) {
        this.labelsTags = labelsTags;
        return this;
    }

    /**
     *
     * @return
     * The imageSmallUrl
     */
    public String getImageSmallUrl() {
        return imageSmallUrl;
    }

    /**
     *
     * @param imageSmallUrl
     * The image_small_url
     */
    public void setImageSmallUrl(String imageSmallUrl) {
        this.imageSmallUrl = imageSmallUrl;
    }

    public Product withImageSmallUrl(String imageSmallUrl) {
        this.imageSmallUrl = imageSmallUrl;
        return this;
    }

    /**
     *
     * @return
     * The imageFrontUrl
     */
    public String getImageFrontUrl() {
        return imageFrontUrl;
    }

    /**
     *
     * @param imageFrontUrl
     * The image_front_url
     */
    public void setImageFrontUrl(String imageFrontUrl) {
        this.imageFrontUrl = imageFrontUrl;
    }

    public Product withImageFrontUrl(String imageFrontUrl) {
        this.imageFrontUrl = imageFrontUrl;
        return this;
    }

    /**
     *
     * @return
     * The imageIngredientsUrl
     */
    public String getImageIngredientsUrl() {
        return imageIngredientsUrl;
    }

    /**
     *
     * @param imageIngredientsUrl
     * The image_ingredients_url
     */
    public void setImageIngredientsUrl(String imageIngredientsUrl) {
        this.imageIngredientsUrl = imageIngredientsUrl;
    }

    public Product withImageIngredientsUrl(String imageIngredientsUrl) {
        this.imageIngredientsUrl = imageIngredientsUrl;
        return this;
    }

    /**
     *
     * @return
     * The imageNutritionUrl
     */
    public String getImageNutritionUrl() {
        return imageNutritionUrl;
    }

    /**
     *
     * @param imageNutritionUrl
     * The image_small_url
     */
    public void setImageNutritionUrl(String imageNutritionUrl) {
        this.imageNutritionUrl = imageNutritionUrl;
    }

    public Product withImageNutritionUrl(String imageNutritionUrl) {
        this.imageNutritionUrl = imageNutritionUrl;
        return this;
    }

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    public void url(String url) {
        this.url = url;
    }

    public Product withUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     *
     * @return
     * The code
     */
    public String getCode() {
        return code;
    }

    /**
     *
     * @param code
     * The code
     */
    public void setCode(String code) {
        this.code = code;
    }

    public Product withCode(String code) {
        this.code = code;
        return this;
    }

    /**
     *
     * @return
     * The tracesTags
     */
    public List<String> getTracesTags() {
        return tracesTags;
    }

    /**
     *
     * @param tracesTags
     * The traces_tags
     */
    public void setTracesTags(List<String> tracesTags) {
        this.tracesTags = tracesTags;
    }

    public Product withTracesTags(List<String> tracesTags) {
        this.tracesTags = tracesTags;
        return this;
    }

    /**
     *
     * @return
     * The lang
     */
    public String getLang() {
        return lang;
    }

    /**
     *
     * @param lang
     * The lang
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    public Product withLang(String lang) {
        this.lang = lang;
        return this;
    }

    /**
     *
     * @return
     * The photographers
     */
    public List<String> getPhotographers() {
        return photographers;
    }

    /**
     *
     * @param photographers
     * The photographers
     */
    public void setPhotographers(List<String> photographers) {
        this.photographers = photographers;
    }

    public Product withPhotographers(List<String> photographers) {
        this.photographers = photographers;
        return this;
    }

    /**
     *
     * @return
     * The ingredientsThatMayBeFromPalmOilTags
     */
    public List<String> getIngredientsThatMayBeFromPalmOilTags() {
        return ingredientsThatMayBeFromPalmOilTags;
    }

    /**
     *
     * @param ingredientsThatMayBeFromPalmOilTags
     * The ingredients_that_may_be_from_palm_oil_tags
     */
    public void setIngredientsThatMayBeFromPalmOilTags(List<String> ingredientsThatMayBeFromPalmOilTags) {
        this.ingredientsThatMayBeFromPalmOilTags = ingredientsThatMayBeFromPalmOilTags;
    }

    public Product withIngredientsThatMayBeFromPalmOilTags(List<String> ingredientsThatMayBeFromPalmOilTags) {
        this.ingredientsThatMayBeFromPalmOilTags = ingredientsThatMayBeFromPalmOilTags;
        return this;
    }

    /**
     *
     * @return
     * The genericName
     */
    public String getGenericName() {
        return genericName;
    }

    /**
     *
     * @param genericName
     * The generic_name
     */
    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public Product withGenericName(String genericName) {
        this.genericName = genericName;
        return this;
    }

    /**
     *
     * @return
     * The oldAdditivesTags
     */
    public List<String> getOldAdditivesTags() {
        return oldAdditivesTags;
    }

    /**
     *
     * @param oldAdditivesTags
     * The old_additives_tags
     */
    public void setOldAdditivesTags(List<String> oldAdditivesTags) {
        this.oldAdditivesTags = oldAdditivesTags;
    }

    public Product withOldAdditivesTags(List<String> oldAdditivesTags) {
        this.oldAdditivesTags = oldAdditivesTags;
        return this;
    }

    /**
     *
     * @return
     * The rev
     */
    public long getRev() {
        return rev;
    }

    /**
     *
     * @param rev
     * The rev
     */
    public void setRev(long rev) {
        this.rev = rev;
    }

    public Product withRev(long rev) {
        this.rev = rev;
        return this;
    }

    /**
     *
     * @return
     * The Keywords
     */
    public List<String> getKeywords() {
        return Keywords;
    }

    /**
     *
     * @param Keywords
     * The _keywords
     */
    public void setKeywords(List<String> Keywords) {
        this.Keywords = Keywords;
    }

    public Product withKeywords(List<String> Keywords) {
        this.Keywords = Keywords;
        return this;
    }

    /**
     *
     * @return
     * The embCodes
     */
    public String getEmbCodes() {
        return embCodes;
    }

    /**
     *
     * @param embCodes
     * The emb_codes
     */
    public void setEmbCodes(String embCodes) {
        this.embCodes = embCodes;
    }

    public Product withEmbCodes(String embCodes) {
        this.embCodes = embCodes;
        return this;
    }

    /**
     *
     * @return
     * The editors
     */
    public List<String> getEditors() {
        return editors;
    }

    /**
     *
     * @param editors
     * The editors
     */
    public void setEditors(List<String> editors) {
        this.editors = editors;
    }

    public Product withEditors(List<String> editors) {
        this.editors = editors;
        return this;
    }

    /**
     *
     * @return
     * The maxImgid
     */
    public String getMaxImgid() {
        return maxImgid;
    }

    /**
     *
     * @param maxImgid
     * The max_imgid
     */
    public void setMaxImgid(String maxImgid) {
        this.maxImgid = maxImgid;
    }

    public Product withMaxImgid(String maxImgid) {
        this.maxImgid = maxImgid;
        return this;
    }

    /**
     *
     * @return
     * The additivesTags
     */
    public List<String> getAdditivesTags() {
        return additivesTags;
    }

    /**
     *
     * @param additivesTags
     * The additives_tags
     */
    public void setAdditivesTags(List<String> additivesTags) {
        this.additivesTags = additivesTags;
    }

    public Product withAdditivesTags(List<String> additivesTags) {
        this.additivesTags = additivesTags;
        return this;
    }

    /**
     *
     * @return
     * The embCodesOrig
     */
    public String getEmbCodesOrig() {
        return embCodesOrig;
    }

    /**
     *
     * @param embCodesOrig
     * The emb_codes_orig
     */
    public void setEmbCodesOrig(String embCodesOrig) {
        this.embCodesOrig = embCodesOrig;
    }

    public Product withEmbCodesOrig(String embCodesOrig) {
        this.embCodesOrig = embCodesOrig;
        return this;
    }

    /**
     *
     * @return
     * The nutrientLevelsTags
     */
    public List<String> getNutrientLevelsTags() {
        return nutrientLevelsTags;
    }

    /**
     *
     * @param nutrientLevelsTags
     * The nutrient_levels_tags
     */
    public void setNutrientLevelsTags(List<String> nutrientLevelsTags) {
        this.nutrientLevelsTags = nutrientLevelsTags;
    }

    public Product withNutrientLevelsTags(List<String> nutrientLevelsTags) {
        this.nutrientLevelsTags = nutrientLevelsTags;
        return this;
    }

    /**
     *
     * @return
     * The informersTags
     */
    public List<String> getInformersTags() {
        return informersTags;
    }

    /**
     *
     * @param informersTags
     * The informers_tags
     */
    public void setInformersTags(List<String> informersTags) {
        this.informersTags = informersTags;
    }

    public Product withInformersTags(List<String> informersTags) {
        this.informersTags = informersTags;
        return this;
    }

    /**
     *
     * @return
     * The photographersTags
     */
    public List<String> getPhotographersTags() {
        return photographersTags;
    }

    /**
     *
     * @param photographersTags
     * The photographers_tags
     */
    public void setPhotographersTags(List<String> photographersTags) {
        this.photographersTags = photographersTags;
    }

    public Product withPhotographersTags(List<String> photographersTags) {
        this.photographersTags = photographersTags;
        return this;
    }

    /**
     *
     * @return
     * The additivesN
     */
    public long getAdditivesN() {
        return additivesN;
    }

    /**
     *
     * @param additivesN
     * The additives_n
     */
    public void setAdditivesN(long additivesN) {
        this.additivesN = additivesN;
    }

    public Product withAdditivesN(long additivesN) {
        this.additivesN = additivesN;
        return this;
    }

    /**
     *
     * @return
     * The allergensHierarchy
     */
    public List<String> getAllergensHierarchy() {
        return allergensHierarchy;
    }

    /**
     *
     * @param allergensHierarchy
     * The allergens_hierarchy
     */
    public void setAllergensHierarchy(List<String> allergensHierarchy) {
        this.allergensHierarchy = allergensHierarchy;
    }

    public Product withAllergensHierarchy(List<String> allergensHierarchy) {
        this.allergensHierarchy = allergensHierarchy;
        return this;
    }


    /**
     *
     * @return
     * The pnnsGroups2Tags
     */
    public List<String> getPnnsGroups2Tags() {
        return pnnsGroups2Tags;
    }

    /**
     *
     * @param pnnsGroups2Tags
     * The pnns_groups_2_tags
     */
    public void setPnnsGroups2Tags(List<String> pnnsGroups2Tags) {
        this.pnnsGroups2Tags = pnnsGroups2Tags;
    }

    public Product withPnnsGroups2Tags(List<String> pnnsGroups2Tags) {
        this.pnnsGroups2Tags = pnnsGroups2Tags;
        return this;
    }

    /**
     *
     * @return
     * The unknownNutrientsTags
     */
    public List<Object> getUnknownNutrientsTags() {
        return unknownNutrientsTags;
    }

    /**
     *
     * @param unknownNutrientsTags
     * The unknown_nutrients_tags
     */
    public void setUnknownNutrientsTags(List<Object> unknownNutrientsTags) {
        this.unknownNutrientsTags = unknownNutrientsTags;
    }

    public Product withUnknownNutrientsTags(List<Object> unknownNutrientsTags) {
        this.unknownNutrientsTags = unknownNutrientsTags;
        return this;
    }

    /**
     *
     * @return
     * The packagingTags
     */
    public List<String> getPackagingTags() {
        return packagingTags;
    }

    /**
     *
     * @param packagingTags
     * The packaging_tags
     */
    public void setPackagingTags(List<String> packagingTags) {
        this.packagingTags = packagingTags;
    }

    public Product withPackagingTags(List<String> packagingTags) {
        this.packagingTags = packagingTags;
        return this;
    }

    /**
     *
     * @return
     * The manufacturingPlaces
     */
    public String getManufacturingPlaces() {
        return manufacturingPlaces;
    }

    /**
     *
     * @param manufacturingPlaces
     * The manufacturing_places
     */
    public void setManufacturingPlaces(String manufacturingPlaces) {
        this.manufacturingPlaces = manufacturingPlaces;
    }

    public Product withManufacturingPlaces(String manufacturingPlaces) {
        this.manufacturingPlaces = manufacturingPlaces;
        return this;
    }

    /**
     *
     * @return
     * The uniqueScansN
     */
    public long getUniqueScansN() {
        return uniqueScansN;
    }

    /**
     *
     * @param uniqueScansN
     * The unique_scans_n
     */
    public void setUniqueScansN(long uniqueScansN) {
        this.uniqueScansN = uniqueScansN;
    }

    public Product withUniqueScansN(long uniqueScansN) {
        this.uniqueScansN = uniqueScansN;
        return this;
    }

    /**
     *
     * @return
     * The nutriments
     */
    public Nutriments getNutriments() {
        return nutriments;
    }

    /**
     *
     * @param nutriments
     * The nutriments
     */
    public void setNutriments(Nutriments nutriments) {
        this.nutriments = nutriments;
    }

    public Product withNutriments(Nutriments nutriments) {
        this.nutriments = nutriments;
        return this;
    }

    /**
     *
     * @return
     * The countriesTags
     */
    public List<String> getCountriesTags() {
        return countriesTags;
    }

    /**
     *
     * @param countriesTags
     * The countries_tags
     */
    public void setCountriesTags(List<String> countriesTags) {
        this.countriesTags = countriesTags;
    }

    public Product withCountriesTags(List<String> countriesTags) {
        this.countriesTags = countriesTags;
        return this;
    }

    /**
     *
     * @return
     * The ingredientsFromPalmOilTags
     */
    public List<Object> getIngredientsFromPalmOilTags() {
        return ingredientsFromPalmOilTags;
    }

    /**
     *
     * @param ingredientsFromPalmOilTags
     * The ingredients_from_palm_oil_tags
     */
    public void setIngredientsFromPalmOilTags(List<Object> ingredientsFromPalmOilTags) {
        this.ingredientsFromPalmOilTags = ingredientsFromPalmOilTags;
    }

    public Product withIngredientsFromPalmOilTags(List<Object> ingredientsFromPalmOilTags) {
        this.ingredientsFromPalmOilTags = ingredientsFromPalmOilTags;
        return this;
    }

    /**
     *
     * @return
     * The purchasePlaces
     */
    public String getPurchasePlaces() {
        return purchasePlaces;
    }

    /**
     *
     * @param purchasePlaces
     * The purchase_places
     */
    public void setPurchasePlaces(String purchasePlaces) {
        this.purchasePlaces = purchasePlaces;
    }

    public Product withPurchasePlaces(String purchasePlaces) {
        this.purchasePlaces = purchasePlaces;
        return this;
    }

    /**
     *
     * @return
     * The embCodesTags
     */
    public List<Object> getEmbCodesTags() {
        return embCodesTags;
    }

    /**
     *
     * @param embCodesTags
     * The emb_codes_tags
     */
    public void setEmbCodesTags(List<Object> embCodesTags) {
        this.embCodesTags = embCodesTags;
    }

    public Product withEmbCodesTags(List<Object> embCodesTags) {
        this.embCodesTags = embCodesTags;
        return this;
    }

    /**
     *
     * @return
     * The brandsTags
     */
    public List<String> getBrandsTags() {
        return brandsTags;
    }

    /**
     *
     * @param brandsTags
     * The brands_tags
     */
    public void setBrandsTags(List<String> brandsTags) {
        this.brandsTags = brandsTags;
    }

    public Product withBrandsTags(List<String> brandsTags) {
        this.brandsTags = brandsTags;
        return this;
    }

    /**
     *
     * @return
     * The pnnsGroups2
     */
    public String getPnnsGroups2() {
        return pnnsGroups2;
    }

    /**
     *
     * @param pnnsGroups2
     * The pnns_groups_2
     */
    public void setPnnsGroups2(String pnnsGroups2) {
        this.pnnsGroups2 = pnnsGroups2;
    }

    public Product withPnnsGroups2(String pnnsGroups2) {
        this.pnnsGroups2 = pnnsGroups2;
        return this;
    }

    /**
     *
     * @return
     * The countriesHierarchy
     */
    public List<String> getCountriesHierarchy() {
        return countriesHierarchy;
    }

    /**
     *
     * @param countriesHierarchy
     * The countries_hierarchy
     */
    public void setCountriesHierarchy(List<String> countriesHierarchy) {
        this.countriesHierarchy = countriesHierarchy;
    }

    public Product withCountriesHierarchy(List<String> countriesHierarchy) {
        this.countriesHierarchy = countriesHierarchy;
        return this;
    }

    /**
     *
     * @return
     * The traces
     */
    public String getTraces() {
        return traces;
    }

    /**
     *
     * @param traces
     * The traces
     */
    public void setTraces(String traces) {
        this.traces = traces;
    }

    public Product withTraces(String traces) {
        this.traces = traces;
        return this;
    }

    /**
     *
     * @return
     * The categories
     */
    public String getCategories() {
        return categories;
    }

    /**
     *
     * @param categories
     * The categories
     */
    public void setCategories(String categories) {
        this.categories = categories;
    }

    public Product withCategories(String categories) {
        this.categories = categories;
        return this;
    }

    /**
     *
     * @return
     * The ingredientsText
     */
    public String getIngredientsText() {
        return ingredientsText;
    }

    /**
     *
     * @param ingredientsText
     * The ingredients_text
     */
    public void setIngredientsText(String ingredientsText) {
        this.ingredientsText = ingredientsText;
    }

    public Product withIngredientsText(String ingredientsText) {
        this.ingredientsText = ingredientsText;
        return this;
    }

    /**
     *
     * @return
     * The createdT
     */
    public long getCreatedT() {
        return createdT;
    }

    /**
     *
     * @param createdT
     * The created_t
     */
    public void setCreatedT(long createdT) {
        this.createdT = createdT;
    }

    public Product withCreatedT(long createdT) {
        this.createdT = createdT;
        return this;
    }

    /**
     *
     * @return
     * The productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     *
     * @param productName
     * The product_name
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Product withProductName(String productName) {
        this.productName = productName;
        return this;
    }

    /**
     *
     * @return
     * The creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     *
     * @param creator
     * The creator
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Product withCreator(String creator) {
        this.creator = creator;
        return this;
    }

    /**
     *
     * @return
     * The ingredientsFromOrThatMayBeFromPalmOilN
     */
    public long getIngredientsFromOrThatMayBeFromPalmOilN() {
        return ingredientsFromOrThatMayBeFromPalmOilN;
    }

    /**
     *
     * @param ingredientsFromOrThatMayBeFromPalmOilN
     * The ingredients_from_or_that_may_be_from_palm_oil_n
     */
    public void setIngredientsFromOrThatMayBeFromPalmOilN(long ingredientsFromOrThatMayBeFromPalmOilN) {
        this.ingredientsFromOrThatMayBeFromPalmOilN = ingredientsFromOrThatMayBeFromPalmOilN;
    }

    public Product withIngredientsFromOrThatMayBeFromPalmOilN(long ingredientsFromOrThatMayBeFromPalmOilN) {
        this.ingredientsFromOrThatMayBeFromPalmOilN = ingredientsFromOrThatMayBeFromPalmOilN;
        return this;
    }

    /**
     *
     * @return
     * The servingSize
     */




    public String getServingSize() {
        return servingSize;
    }

    /**
     *
     * @param servingSize
     * The serving_size
     */

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
    }

    public Product withServingSize(String servingSize) {
        this.servingSize = servingSize;
        return this;
    }

    /**
     *
     * @return
     * The noNutritionData
     */
    public Object getNoNutritionData() {
        return noNutritionData;
    }

    /**
     *
     * @param noNutritionData
     * The no_nutrition_data
     */
    public void setNoNutritionData(Object noNutritionData) {
        this.noNutritionData = noNutritionData;
    }

    public Product withNoNutritionData(Object noNutritionData) {
        this.noNutritionData = noNutritionData;
        return this;
    }

    /**
     *
     * @return
     * The completedT
     */
    public long getCompletedT() {
        return completedT;
    }

    /**
     *
     * @param completedT
     * The completed_t
     */
    public void setCompletedT(long completedT) {
        this.completedT = completedT;
    }

    public Product withCompletedT(long completedT) {
        this.completedT = completedT;
        return this;
    }

    /**
     *
     * @return
     * The lastModifiedBy
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     *
     * @param lastModifiedBy
     * The last_modified_by
     */

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Product withLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    /**
     *
     * @return
     * The allergens
     */
    public String getAllergens() {
        return allergens;
    }

    /**
     *
     * @param allergens
     * The allergens
     */
    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }

    public Product withAllergens(String allergens) {
        this.allergens = allergens;
        return this;
    }

    /**
     *
     * @return
     * The newAdditivesN
     */
    public long getNewAdditivesN() {
        return newAdditivesN;
    }

    /**
     *
     * @param newAdditivesN
     * The new_additives_n
     */
    public void setNewAdditivesN(long newAdditivesN) {
        this.newAdditivesN = newAdditivesN;
    }

    public Product withNewAdditivesN(long newAdditivesN) {
        this.newAdditivesN = newAdditivesN;
        return this;
    }

    /**
     *
     * @return
     * The origins
     */
    public String getOrigins() {
        return origins;
    }

    /**
     *
     * @param origins
     * The origins
     */
    public void setOrigins(String origins) {
        this.origins = origins;
    }

    public Product withOrigins(String origins) {
        this.origins = origins;
        return this;
    }

    /**
     *
     * @return
     * The stores
     */
    public String getStores() {
        return stores;
    }

    /**
     *
     * @param stores
     * The stores
     */
    public void setStores(String stores) {
        this.stores = stores;
    }

    public Product withStores(String stores) {
        this.stores = stores;
        return this;
    }

    /**
     *
     * @return
     * The nutritionGradeFr
     */
    public String getNutritionGradeFr() {
        return nutritionGradeFr;
    }

    /**
     *
     * @param nutritionGradeFr
     * The nutrition_grade_fr
     */
    public void setNutritionGradeFr(String nutritionGradeFr) {
        this.nutritionGradeFr = nutritionGradeFr;
    }

    public Product withNutritionGradeFr(String nutritionGradeFr) {
        this.nutritionGradeFr = nutritionGradeFr;
        return this;
    }

    /**
     *
     * @return
     * The nutrientLevels
     */
    public NutrientLevels getNutrientLevels() {
        return nutrientLevels;
    }

    /**
     *
     * @param nutrientLevels
     * The nutrient_levels
     */
    public void setNutrientLevels(NutrientLevels nutrientLevels) {
        this.nutrientLevels = nutrientLevels;
    }

    public Product withNutrientLevels(NutrientLevels nutrientLevels) {
        this.nutrientLevels = nutrientLevels;
        return this;
    }

    /**
     *
     * @return
     * The storesTags
     */
    public List<Object> getStoresTags() {
        return storesTags;
    }

    /**
     *
     * @param storesTags
     * The stores_tags
     */
    public void setStoresTags(List<Object> storesTags) {
        this.storesTags = storesTags;
    }

    public Product withStoresTags(List<Object> storesTags) {
        this.storesTags = storesTags;
        return this;
    }

    /**
     *
     * @return
     * The countries
     */
    public String getCountries() {
        return countries;
    }

    /**
     *
     * @param countries
     * The countries
     */
    public void setCountries(String countries) {
        this.countries = countries;
    }

    public Product withCountries(String countries) {
        this.countries = countries;
        return this;
    }

    /**
     *
     * @return
     * The purchasePlacesTags
     */
    public List<Object> getPurchasePlacesTags() {
        return purchasePlacesTags;
    }

    /**
     *
     * @param purchasePlacesTags
     * The purchase_places_tags
     */
    public void setPurchasePlacesTags(List<Object> purchasePlacesTags) {
        this.purchasePlacesTags = purchasePlacesTags;
    }

    public Product withPurchasePlacesTags(List<Object> purchasePlacesTags) {
        this.purchasePlacesTags = purchasePlacesTags;
        return this;
    }

    /**
     *
     * @return
     * The fruitsVegetablesNuts100gEstimate
     */
    public double getFruitsVegetablesNuts100gEstimate() {
        return fruitsVegetablesNuts100gEstimate;
    }

    /**
     *
     * @param fruitsVegetablesNuts100gEstimate
     * The fruits-vegetables-nuts_100g_estimate
     */
    public void setFruitsVegetablesNuts100gEstimate(double fruitsVegetablesNuts100gEstimate) {
        this.fruitsVegetablesNuts100gEstimate = fruitsVegetablesNuts100gEstimate;
    }

    public Product withFruitsVegetablesNuts100gEstimate(double fruitsVegetablesNuts100gEstimate) {
        this.fruitsVegetablesNuts100gEstimate = fruitsVegetablesNuts100gEstimate;
        return this;
    }

    /**
     *
     * @return
     * The interfaceVersionModified
     */
    public String getInterfaceVersionModified() {
        return interfaceVersionModified;
    }

    /**
     *
     * @param interfaceVersionModified
     * The interface_version_modified
     */
    public void setInterfaceVersionModified(String interfaceVersionModified) {
        this.interfaceVersionModified = interfaceVersionModified;
    }

    public Product withInterfaceVersionModified(String interfaceVersionModified) {
        this.interfaceVersionModified = interfaceVersionModified;
        return this;
    }

    /**
     *
     * @return
     * The sortkey
     */
    public long getSortkey() {
        return sortkey;
    }

    /**
     *
     * @param sortkey
     * The sortkey
     */
    public void setSortkey(long sortkey) {
        this.sortkey = sortkey;
    }

    public Product withSortkey(long sortkey) {
        this.sortkey = sortkey;
        return this;
    }

    /**
     *
     * @return
     * The lastModifiedT
     */
    public long getLastModifiedT() {
        return lastModifiedT;
    }

    /**
     *
     * @param lastModifiedT
     * The last_modified_t
     */
    public void setLastModifiedT(long lastModifiedT) {
        this.lastModifiedT = lastModifiedT;
    }

    public Product withLastModifiedT(long lastModifiedT) {
        this.lastModifiedT = lastModifiedT;
        return this;
    }

    /**
     *
     * @return
     * The nutritionScoreDebug
     */
    public String getNutritionScoreDebug() {
        return nutritionScoreDebug;
    }

    /**
     *
     * @param nutritionScoreDebug
     * The nutrition_score_debug
     */
    public void setNutritionScoreDebug(String nutritionScoreDebug) {
        this.nutritionScoreDebug = nutritionScoreDebug;
    }

    public Product withNutritionScoreDebug(String nutritionScoreDebug) {
        this.nutritionScoreDebug = nutritionScoreDebug;
        return this;
    }

    /**
     *
     * @return
     * The countries20131227
     */
    public Object getCountries20131227() {
        return countries20131227;
    }

    /**
     *
     * @param countries20131227
     * The countries.20131227
     */
    public void setCountries20131227(Object countries20131227) {
        this.countries20131227 = countries20131227;
    }

    public Product withCountries20131227(Object countries20131227) {
        this.countries20131227 = countries20131227;
        return this;
    }

    /**
     *
     * @return
     * The correctorsTags
     */
    public List<String> getCorrectorsTags() {
        return correctorsTags;
    }

    /**
     *
     * @param correctorsTags
     * The correctors_tags
     */
    public void setCorrectorsTags(List<String> correctorsTags) {
        this.correctorsTags = correctorsTags;
    }

    public Product withCorrectorsTags(List<String> correctorsTags) {
        this.correctorsTags = correctorsTags;
        return this;
    }

    /**
     *
     * @return
     * The newAdditivesDebug
     */
    public String getNewAdditivesDebug() {
        return newAdditivesDebug;
    }

    /**
     *
     * @param newAdditivesDebug
     * The new_additives_debug
     */
    public void setNewAdditivesDebug(String newAdditivesDebug) {
        this.newAdditivesDebug = newAdditivesDebug;
    }

    public Product withNewAdditivesDebug(String newAdditivesDebug) {
        this.newAdditivesDebug = newAdditivesDebug;
        return this;
    }

    /**
     *
     * @return
     * The correctors
     */
    public List<String> getCorrectors() {
        return correctors;
    }

    /**
     *
     * @param correctors
     * The correctors
     */
    public void setCorrectors(List<String> correctors) {
        this.correctors = correctors;
    }

    public Product withCorrectors(List<String> correctors) {
        this.correctors = correctors;
        return this;
    }

    /**
     *
     * @return
     * The brands
     */
    public String getBrands() {
        return brands;
    }

    /**
     *
     * @param brands
     * The brands
     */
    public void setBrands(String brands) {
        this.brands = brands;
    }

    public Product withBrands(String brands) {
        this.brands = brands;
        return this;
    }

    /**
     *
     * @return
     * The ingredientsTags
     */
    public List<String> getIngredientsTags() {
        return ingredientsTags;
    }

    /**
     *
     * @param ingredientsTags
     * The ingredients_tags
     */
    public void setIngredientsTags(List<String> ingredientsTags) {
        this.ingredientsTags = ingredientsTags;
    }

    public Product withIngredientsTags(List<String> ingredientsTags) {
        this.ingredientsTags = ingredientsTags;
        return this;
    }

    /**
     *
     * @return
     * The newAdditivesTags
     */
    public List<String> getNewAdditivesTags() {
        return newAdditivesTags;
    }

    /**
     *
     * @param newAdditivesTags
     * The new_additives_tags
     */
    public void setNewAdditivesTags(List<String> newAdditivesTags) {
        this.newAdditivesTags = newAdditivesTags;
    }

    public Product withNewAdditivesTags(List<String> newAdditivesTags) {
        this.newAdditivesTags = newAdditivesTags;
        return this;
    }

    /**
     *
     * @return
     * The informers
     */
    public List<String> getInformers() {
        return informers;
    }

    /**
     *
     * @param informers
     * The informers
     */
    public void setInformers(List<String> informers) {
        this.informers = informers;
    }

    public Product withInformers(List<String> informers) {
        this.informers = informers;
        return this;
    }

    /**
     *
     * @return
     * The states
     */
    public String getStates() {
        return states;
    }

    /**
     *
     * @param states
     * The states
     */
    public void setStates(String states) {
        this.states = states;
    }

    public Product withStates(String states) {
        this.states = states;
        return this;
    }

    /**
     *
     * @return
     * The entryDatesTags
     */
    public List<String> getEntryDatesTags() {
        return entryDatesTags;
    }

    /**
     *
     * @param entryDatesTags
     * The entry_dates_tags
     */
    public void setEntryDatesTags(List<String> entryDatesTags) {
        this.entryDatesTags = entryDatesTags;
    }

    public Product withEntryDatesTags(List<String> entryDatesTags) {
        this.entryDatesTags = entryDatesTags;
        return this;
    }

    /**
     *
     * @return
     * The nutritionGradesTags
     */
    public List<String> getNutritionGradesTags() {
        return nutritionGradesTags;
    }

    /**
     *
     * @param nutritionGradesTags
     * The nutrition_grades_tags
     */
    public void setNutritionGradesTags(List<String> nutritionGradesTags) {
        this.nutritionGradesTags = nutritionGradesTags;
    }

    public Product withNutritionGradesTags(List<String> nutritionGradesTags) {
        this.nutritionGradesTags = nutritionGradesTags;
        return this;
    }

    /**
     *
     * @return
     * The ingredientsTextWithAllergens
     */
    public String getIngredientsTextWithAllergens() {
        return ingredientsTextWithAllergens;
    }

    /**
     *
     * @param ingredientsTextWithAllergens
     * The ingredients_text_with_allergens
     */
    public void setIngredientsTextWithAllergens(String ingredientsTextWithAllergens) {
        this.ingredientsTextWithAllergens = ingredientsTextWithAllergens;
    }

    public Product withIngredientsTextWithAllergens(String ingredientsTextWithAllergens) {
        this.ingredientsTextWithAllergens = ingredientsTextWithAllergens;
        return this;
    }

    /**
     *
     * @return
     * The packaging
     */
    public String getPackaging() {
        return packaging;
    }

    /**
     *
     * @param packaging
     * The packaging
     */
    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public Product withPackaging(String packaging) {
        this.packaging = packaging;
        return this;
    }

    /**
     *
     * @return
     * The servingQuantity
     */
    public double getServingQuantity() {
        return servingQuantity;
    }

    /**
     *
     * @param servingQuantity
     * The serving_quantity
     */
    public void setServingQuantity(double servingQuantity) {
        this.servingQuantity = servingQuantity;
    }

    public Product withServingQuantity(double servingQuantity) {
        this.servingQuantity = servingQuantity;
        return this;
    }

    /**
     *
     * @return
     * The manufacturingPlacesTags
     */
    public List<Object> getManufacturingPlacesTags() {
        return manufacturingPlacesTags;
    }

    /**
     *
     * @param manufacturingPlacesTags
     * The manufacturing_places_tags
     */
    public void setManufacturingPlacesTags(List<Object> manufacturingPlacesTags) {
        this.manufacturingPlacesTags = manufacturingPlacesTags;
    }

    public Product withManufacturingPlacesTags(List<Object> manufacturingPlacesTags) {
        this.manufacturingPlacesTags = manufacturingPlacesTags;
        return this;
    }

    /**
     *
     * @return
     * The originsTags
     */
    public List<String> getOriginsTags() {
        return originsTags;
    }

    /**
     *
     * @param originsTags
     * The origins_tags
     */
    public void setOriginsTags(List<String> originsTags) {
        this.originsTags = originsTags;
    }

    public Product withOriginsTags(List<String> originsTags) {
        this.originsTags = originsTags;
        return this;
    }

    /**
     *
     * @return
     * The scansN
     */
    public long getScansN() {
        return scansN;
    }

    /**
     *
     * @param scansN
     * The scans_n
     */
    public void setScansN(long scansN) {
        this.scansN = scansN;
    }

    public Product withScansN(long scansN) {
        this.scansN = scansN;
        return this;
    }

    /**
     *
     * @return
     * The nutritionDataPer
     */
    public String getNutritionDataPer() {
        return nutritionDataPer;
    }

    /**
     *
     * @param nutritionDataPer
     * The nutrition_data_per
     */
    public void setNutritionDataPer(String nutritionDataPer) {
        this.nutritionDataPer = nutritionDataPer;
    }

    public Product withNutritionDataPer(String nutritionDataPer) {
        this.nutritionDataPer = nutritionDataPer;
        return this;
    }

    /**
     *
     * @return
     * The labels
     */
    public String getLabels() {
        return labels;
    }

    /**
     *
     * @param labels
     * The labels
     */
    public void setLabels(String labels) {
        this.labels = labels;
    }

    public Product withLabels(String labels) {
        this.labels = labels;
        return this;
    }

    /**
     *
     * @return
     * The link
     */
    public String getLink() {
        return link;
    }

    /**
     *
     * @param link
     * The link
     */
    public void setLink(String link) {
        this.link = link;
    }

    public Product withLink(String link) {
        this.link = link;
        return this;
    }

    /**
     *
     * @return
     * The citiesTags
     */
    public List<Object> getCitiesTags() {
        return citiesTags;
    }

    /**
     *
     * @param citiesTags
     * The cities_tags
     */
    public void setCitiesTags(List<Object> citiesTags) {
        this.citiesTags = citiesTags;
    }

    public Product withCitiesTags(List<Object> citiesTags) {
        this.citiesTags = citiesTags;
        return this;
    }

    /**
     *
     * @return
     * The embCodes20141016
     */
    public String getEmbCodes20141016() {
        return embCodes20141016;
    }

    /**
     *
     * @param embCodes20141016
     * The emb_codes_20141016
     */
    public void setEmbCodes20141016(String embCodes20141016) {
        this.embCodes20141016 = embCodes20141016;
    }

    public Product withEmbCodes20141016(String embCodes20141016) {
        this.embCodes20141016 = embCodes20141016;
        return this;
    }

    /**
     *
     * @return
     * The categoriesTags
     */
    public List<String> getCategoriesTags() {
        return categoriesTags;
    }

    /**
     *
     * @param categoriesTags
     * The categories_tags
     */
    public void setCategoriesTags(List<String> categoriesTags) {
        this.categoriesTags = categoriesTags;
    }

    public Product withCategoriesTags(List<String> categoriesTags) {
        this.categoriesTags = categoriesTags;
        return this;
    }

    /**
     *
     * @return
     * The quantity
     */
    public String getQuantity() {
        return quantity;
    }

    /**
     *
     * @param quantity
     * The quantity
     */
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public Product withQuantity(String quantity) {
        this.quantity = quantity;
        return this;
    }

    /**
     *
     * @return
     * The expirationDate
     */
    public String getExpirationDate() {
        return expirationDate;
    }

    /**
     *
     * @param expirationDate
     * The expiration_date
     */
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Product withExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    /**
     *
     * @return
     * The ingredientsThatMayBeFromPalmOilN
     */
    public long getIngredientsThatMayBeFromPalmOilN() {
        return ingredientsThatMayBeFromPalmOilN;
    }

    /**
     *
     * @param ingredientsThatMayBeFromPalmOilN
     * The ingredients_that_may_be_from_palm_oil_n
     */
    public void setIngredientsThatMayBeFromPalmOilN(long ingredientsThatMayBeFromPalmOilN) {
        this.ingredientsThatMayBeFromPalmOilN = ingredientsThatMayBeFromPalmOilN;
    }

    public Product withIngredientsThatMayBeFromPalmOilN(long ingredientsThatMayBeFromPalmOilN) {
        this.ingredientsThatMayBeFromPalmOilN = ingredientsThatMayBeFromPalmOilN;
        return this;
    }

    /**
     *
     * @return
     * The statesHierarchy
     */
    public List<String> getStatesHierarchy() {
        return statesHierarchy;
    }

    /**
     *
     * @param statesHierarchy
     * The states_hierarchy
     */
    public void setStatesHierarchy(List<String> statesHierarchy) {
        this.statesHierarchy = statesHierarchy;
    }

    public Product withStatesHierarchy(List<String> statesHierarchy) {
        this.statesHierarchy = statesHierarchy;
        return this;
    }

    /**
     *
     * @return
     * The embCode
     */
    public String getEmbCode() {
        return embCode;
    }

    /**
     *
     * @param embCode
     * The emb_code
     */
    public void setEmbCode(String embCode) {
        this.embCode = embCode;
    }

    public Product withEmbCode(String embCode) {
        this.embCode = embCode;
        return this;
    }

    /**
     *
     * @return
     * The allergensTags
     */
    public List<Object> getAllergensTags() {
        return allergensTags;
    }

    /**
     *
     * @param allergensTags
     * The allergens_tags
     */
    public void setAllergensTags(List<Object> allergensTags) {
        this.allergensTags = allergensTags;
    }

    public Product withAllergensTags(List<Object> allergensTags) {
        this.allergensTags = allergensTags;
        return this;
    }

    /**
     *
     * @return
     * The ingredientsFromPalmOilN
     */
    public long getIngredientsFromPalmOilN() {
        return ingredientsFromPalmOilN;
    }

    /**
     *
     * @param ingredientsFromPalmOilN
     * The ingredients_from_palm_oil_n
     */

    public void setIngredientsFromPalmOilN(long ingredientsFromPalmOilN) {
        this.ingredientsFromPalmOilN = ingredientsFromPalmOilN;
    }

    public Product withIngredientsFromPalmOilN(long ingredientsFromPalmOilN) {
        this.ingredientsFromPalmOilN = ingredientsFromPalmOilN;
        return this;
    }

    /**
     *
     * @return
     * The imageUrl
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     *
     * @param imageUrl
     * The image_url
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Product withImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    /**
     *
     * @return
     * The lc
     */
    public String getLc() {
        return lc;
    }

    /**
     *
     * @param lc
     * The lc
     */

    public void setLc(String lc) {
        this.lc = lc;
    }

    public Product withLc(String lc) {
        this.lc = lc;
        return this;
    }

    /**
     *
     * @return
     * The ingredients
     */
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    /**
     *
     * @param ingredients
     * The ingredients
     */
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public Product withIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
        return this;
    }

    /**
     *
     * @return
     * The pnnsGroups1Tags
     */
    public List<String> getPnnsGroups1Tags() {
        return pnnsGroups1Tags;
    }

    /**
     *
     * @param pnnsGroups1Tags
     * The pnns_groups_1_tags
     */
    public void setPnnsGroups1Tags(List<String> pnnsGroups1Tags) {
        this.pnnsGroups1Tags = pnnsGroups1Tags;
    }

    public Product withPnnsGroups1Tags(List<String> pnnsGroups1Tags) {
        this.pnnsGroups1Tags = pnnsGroups1Tags;
        return this;
    }

    /**
     *
     * @return
     * The checkers
     */
    public List<Object> getCheckers() {
        return checkers;
    }

    /**
     *
     * @param checkers
     * The checkers
     */
    public void setCheckers(List<Object> checkers) {
        this.checkers = checkers;
    }

    public Product withCheckers(List<Object> checkers) {
        this.checkers = checkers;
        return this;
    }

    /**
     *
     * @return
     * The complete
     */
    public long getComplete() {
        return complete;
    }

    /**
     *
     * @param complete
     * The complete
     */
    public void setComplete(long complete) {
        this.complete = complete;
    }

    public Product withComplete(long complete) {
        this.complete = complete;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Product withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return "Product{" +
                "lastEditDatesTags=" + lastEditDatesTags +
                ", labelsHierarchy=" + labelsHierarchy +
                ", Id='" + Id + '\'' +
                ", categoriesHierarchy=" + categoriesHierarchy +
                ", pnnsGroups1='" + pnnsGroups1 + '\'' +
                ", checkersTags=" + checkersTags +
                ", statesTags=" + statesTags +
                ", labelsTags=" + labelsTags +
                ", imageSmallUrl='" + imageSmallUrl + '\'' +
                ", code='" + code + '\'' +
                ", tracesTags=" + tracesTags +
                ", lang='" + lang + '\'' +
                ", photographers=" + photographers +
                ", ingredientsThatMayBeFromPalmOilTags=" + ingredientsThatMayBeFromPalmOilTags +
                ", genericName='" + genericName + '\'' +
                ", oldAdditivesTags=" + oldAdditivesTags +
                ", rev=" + rev +
                ", Keywords=" + Keywords +
                ", embCodes='" + embCodes + '\'' +
                ", editors=" + editors +
                ", maxImgid='" + maxImgid + '\'' +
                ", additivesTags=" + additivesTags +
                ", embCodesOrig='" + embCodesOrig + '\'' +
                ", nutrientLevelsTags=" + nutrientLevelsTags +
                ", informersTags=" + informersTags +
                ", photographersTags=" + photographersTags +
                ", additivesN=" + additivesN +
                ", allergensHierarchy=" + allergensHierarchy +
                ", pnnsGroups2Tags=" + pnnsGroups2Tags +
                ", unknownNutrientsTags=" + unknownNutrientsTags +
                ", packagingTags=" + packagingTags +
                ", manufacturingPlaces='" + manufacturingPlaces + '\'' +
                ", uniqueScansN=" + uniqueScansN +
                ", nutriments=" + nutriments +
                ", countriesTags=" + countriesTags +
                ", ingredientsFromPalmOilTags=" + ingredientsFromPalmOilTags +
                ", purchasePlaces='" + purchasePlaces + '\'' +
                ", embCodesTags=" + embCodesTags +
                ", brandsTags=" + brandsTags +
                ", pnnsGroups2='" + pnnsGroups2 + '\'' +
                ", countriesHierarchy=" + countriesHierarchy +
                ", traces='" + traces + '\'' +
                ", categories='" + categories + '\'' +
                ", ingredientsText='" + ingredientsText + '\'' +
                ", createdT=" + createdT +
                ", productName='" + productName + '\'' +
                ", creator='" + creator + '\'' +
                ", ingredientsFromOrThatMayBeFromPalmOilN=" + ingredientsFromOrThatMayBeFromPalmOilN +
                ", servingSize='" + servingSize + '\'' +
                ", noNutritionData=" + noNutritionData +
                ", completedT=" + completedT +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", allergens='" + allergens + '\'' +
                ", newAdditivesN=" + newAdditivesN +
                ", origins='" + origins + '\'' +
                ", stores='" + stores + '\'' +
                ", nutritionGradeFr='" + nutritionGradeFr + '\'' +
                ", nutrientLevels=" + nutrientLevels +
                ", id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", storesTags=" + storesTags +
                ", countries='" + countries + '\'' +
                ", purchasePlacesTags=" + purchasePlacesTags +
                ", fruitsVegetablesNuts100gEstimate=" + fruitsVegetablesNuts100gEstimate +
                ", interfaceVersionModified='" + interfaceVersionModified + '\'' +
                ", sortkey=" + sortkey +
                ", lastModifiedT=" + lastModifiedT +
                ", nutritionScoreDebug='" + nutritionScoreDebug + '\'' +
                ", countries20131227=" + countries20131227 +
                ", correctorsTags=" + correctorsTags +
                ", newAdditivesDebug='" + newAdditivesDebug + '\'' +
                ", correctors=" + correctors +
                ", brands='" + brands + '\'' +
                ", ingredientsTags=" + ingredientsTags +
                ", newAdditivesTags=" + newAdditivesTags +
                ", informers=" + informers +
                ", states='" + states + '\'' +
                ", entryDatesTags=" + entryDatesTags +
                ", nutritionGradesTags=" + nutritionGradesTags +
                ", ingredientsTextWithAllergens='" + ingredientsTextWithAllergens + '\'' +
                ", packaging='" + packaging + '\'' +
                ", servingQuantity=" + servingQuantity +
                ", manufacturingPlacesTags=" + manufacturingPlacesTags +
                ", originsTags=" + originsTags +
                ", scansN=" + scansN +
                ", nutritionDataPer='" + nutritionDataPer + '\'' +
                ", labels='" + labels + '\'' +
                ", link='" + link + '\'' +
                ", citiesTags=" + citiesTags +
                ", embCodes20141016='" + embCodes20141016 + '\'' +
                ", categoriesTags=" + categoriesTags +
                ", quantity='" + quantity + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                ", ingredientsThatMayBeFromPalmOilN=" + ingredientsThatMayBeFromPalmOilN +
                ", statesHierarchy=" + statesHierarchy +
                ", embCode='" + embCode + '\'' +
                ", allergensTags=" + allergensTags +
                ", ingredientsFromPalmOilN=" + ingredientsFromPalmOilN +
                ", imageUrl='" + imageUrl + '\'' +
                ", lc='" + lc + '\'' +
                ", ingredients=" + ingredients +
                ", pnnsGroups1Tags=" + pnnsGroups1Tags +
                ", checkers=" + checkers +
                ", complete=" + complete +
                ", additionalProperties=" + additionalProperties +
                ", imageNutritionUrl='" + imageNutritionUrl + '\'' +
                '}';
    }
}
