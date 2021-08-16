package openfoodfacts.github.scrachx.openfood.network.services

import io.reactivex.Single
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
    fun getLabels(): Single<LabelsWrapper>

    @GET(ALLERGENS_JSON)
    fun getAllergens(): Single<AllergensWrapper>

    @GET(INGREDIENTS_JSON)
    fun getIngredients(): Single<IngredientsWrapper>

    @GET(ADDITIVES_JSON)
    fun getAdditives(): Single<AdditivesWrapper>

    @GET(COUNTRIES_JSON)
    fun getCountries(): Single<CountriesWrapper>

    @GET(CATEGORIES_JSON)
    fun getCategories(): Single<CategoriesWrapper>

    @GET(TAGS_JSON)
    fun getTags(): Single<TagsWrapper>

    @GET(INVALID_BARCODES_JSON)
    fun getInvalidBarcodes(): Single<List<String>>

    @GET(VITAMINS_JSON)
    fun getVitamins(): Single<CategoriesWrapper>

    @GET(ADDITIVES_CLASSES_JSON)
    fun getAdditivesClasses(): Single<CategoriesWrapper>

    @GET(NUCLEOTIDES_JSON)
    fun getNucleotides(): Single<CategoriesWrapper>

    @GET(NUTRIENT_LEVELS_JSON)
    fun getNutrientLevels(): Single<CategoriesWrapper>

    @GET(LANGUAGES_JSON)
    fun getLanguages(): Single<CategoriesWrapper>

    @GET(NUTRIENTS_JSON)
    fun getNutrients(): Single<CategoriesWrapper>

    @GET(MINERALS_JSON)
    fun getMinerals(): Single<CategoriesWrapper>

    @GET(STATES_JSON)
    fun getStates(): Single<StatesWrapper>

    @GET(STORES_JSON)
    fun getStores(): Single<StoresWrapper>

    @GET(BRANDS_JSON)
    fun getBrands(): Single<BrandsWrapper>

    @GET(ANALYSIS_TAG_JSON)
    fun getAnalysisTags(): Single<AnalysisTagsWrapper>

    @GET(ANALYSIS_TAG_CONFIG_JSON)
    fun getAnalysisTagConfigs(): Single<AnalysisTagConfigsWrapper>


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