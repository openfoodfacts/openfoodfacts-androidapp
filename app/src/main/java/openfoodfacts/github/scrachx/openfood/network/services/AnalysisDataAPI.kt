package openfoodfacts.github.scrachx.openfood.network.services

import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditivesWrapper
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergensWrapper
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagsWrapper
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfigsWrapper
import openfoodfacts.github.scrachx.openfood.models.entities.brand.BrandsWrapper
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoriesWrapper
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountriesWrapper
import openfoodfacts.github.scrachx.openfood.models.entities.ingredient.IngredientsWrapper
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelsWrapper
import openfoodfacts.github.scrachx.openfood.models.entities.states.StatesWrapper
import openfoodfacts.github.scrachx.openfood.models.entities.store.StoresWrapper
import openfoodfacts.github.scrachx.openfood.models.entities.tag.TagsWrapper
import retrofit2.http.GET

/**
 * API calls for loading static multilingual data
 * This calls should be used as rare as possible, because they load Big Data
 *
 * @author Lobster
 */
interface AnalysisDataAPI {
    @GET(LABELS_JSON)
    suspend fun getLabels(): LabelsWrapper

    @GET(ALLERGENS_JSON)
    suspend fun getAllergens(): AllergensWrapper

    @GET(INGREDIENTS_JSON)
    suspend fun getIngredients(): IngredientsWrapper

    @GET(ADDITIVES_JSON)
    suspend fun getAdditives(): AdditivesWrapper

    @GET(COUNTRIES_JSON)
    suspend fun getCountries(): CountriesWrapper

    @GET(CATEGORIES_JSON)
    suspend fun getCategories(): CategoriesWrapper

    @GET(TAGS_JSON)
    suspend fun getTags(): TagsWrapper

    @GET(INVALID_BARCODES_JSON)
    suspend fun getInvalidBarcodes(): List<String>

    @GET(VITAMINS_JSON)
    suspend fun getVitamins(): CategoriesWrapper

    @GET(ADDITIVES_CLASSES_JSON)
    suspend fun getAdditivesClasses(): CategoriesWrapper

    @GET(NUCLEOTIDES_JSON)
    suspend fun getNucleotides(): CategoriesWrapper

    @GET(NUTRIENT_LEVELS_JSON)
    suspend fun getNutrientLevels(): CategoriesWrapper

    @GET(LANGUAGES_JSON)
    suspend fun getLanguages(): CategoriesWrapper

    @GET(NUTRIENTS_JSON)
    suspend fun getNutrients(): CategoriesWrapper

    @GET(MINERALS_JSON)
    suspend fun getMinerals(): CategoriesWrapper

    @GET(STATES_JSON)
    suspend fun getStates(): StatesWrapper

    @GET(STORES_JSON)
    suspend fun getStores(): StoresWrapper

    @GET(BRANDS_JSON)
    suspend fun getBrands(): BrandsWrapper

    @GET(ANALYSIS_TAG_JSON)
    suspend fun getAnalysisTags(): AnalysisTagsWrapper

    @GET(ANALYSIS_TAG_CONFIG_JSON)
    suspend fun getAnalysisTagConfigs(): AnalysisTagConfigsWrapper


    companion object {
        private const val PREFIX = "data/taxonomies"
        const val LABELS_JSON = "$PREFIX/labels.json"
        const val COUNTRIES_JSON = "$PREFIX/countries.json"
        const val CATEGORIES_JSON = "$PREFIX/categories.json"
        const val ADDITIVES_JSON = "$PREFIX/additives.json"
        const val INGREDIENTS_JSON = "$PREFIX/ingredients.json"
        const val ALLERGENS_JSON = "$PREFIX/allergens.json"
        const val ANALYSIS_TAG_JSON = "$PREFIX/ingredients_analysis.json"
        const val TAGS_JSON = "$PREFIX/packager-codes.json"
        const val VITAMINS_JSON = "$PREFIX/vitamins.json"
        const val ADDITIVES_CLASSES_JSON = "$PREFIX/additives_classes.json"
        const val NUCLEOTIDES_JSON = "$PREFIX/nucleotides.json"
        const val NUTRIENT_LEVELS_JSON = "$PREFIX/nutrient_levels.json"
        const val LANGUAGES_JSON = "$PREFIX/languages.json"
        const val STATES_JSON = "$PREFIX/states.json"
        const val MINERALS_JSON = "$PREFIX/minerals.json"
        const val NUTRIENTS_JSON = "$PREFIX/nutrients.json"
        const val STORES_JSON = "$PREFIX/stores.json"
        const val BRANDS_JSON = "$PREFIX/brands.json"
        const val INVALID_BARCODES_JSON = "data/invalid-barcodes.json"
        const val ANALYSIS_TAG_CONFIG_JSON = "files/app/ingredients-analysis.json"
    }
}