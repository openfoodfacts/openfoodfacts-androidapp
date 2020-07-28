package openfoodfacts.github.scrachx.openfood.network.services;

/*
  Created by Lobster on 03.03.18.
 */

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import openfoodfacts.github.scrachx.openfood.models.AdditivesWrapper;
import openfoodfacts.github.scrachx.openfood.models.AllergensWrapper;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagGonfigsWrapper;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagsWrapper;
import openfoodfacts.github.scrachx.openfood.models.CategoriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.CountriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.IngredientsWrapper;
import openfoodfacts.github.scrachx.openfood.models.LabelsWrapper;
import openfoodfacts.github.scrachx.openfood.models.TagsWrapper;
import retrofit2.http.GET;

/**
 * API calls for loading static multilingual data
 * This calls should be used as rare as possible, because they load Big Data
 */
public interface AnalysisDataAPI {
    String LABELS_JSON = "data/taxonomies/labels.json";
    String COUNTRIES_JSON = "data/taxonomies/countries.json";
    String CATEGORIES_JSON = "data/taxonomies/categories.json";
    String ADDITIVES_JSON = "data/taxonomies/additives.json";
    String INGREDIENTS_JSON = "data/taxonomies/ingredients.json";
    String ALLERGENS_JSON = "data/taxonomies/allergens.json";
    String ANALYSIS_TAG_JSON = "data/taxonomies/ingredients_analysis.json";
    String ANALYSIS_TAG_CONFIG_JSON = "files/app/ingredients-analysis.json";
    String TAGS_JSON = "data/taxonomies/packager-codes.json";
    String INVALID_BARCODES_JSON = "data/invalid-barcodes.json";

    @GET(LABELS_JSON)
    Single<LabelsWrapper> getLabels();

    @GET(ALLERGENS_JSON)
    Single<AllergensWrapper> getAllergens();

    @GET(INGREDIENTS_JSON)
    Single<IngredientsWrapper> getIngredients();

    @GET(ADDITIVES_JSON)
    Single<AdditivesWrapper> getAdditives();

    @GET(COUNTRIES_JSON)
    Single<CountriesWrapper> getCountries();

    @GET(CATEGORIES_JSON)
    Single<CategoriesWrapper> getCategories();

    @GET(TAGS_JSON)
    Single<TagsWrapper> getTags();

    @GET(INVALID_BARCODES_JSON)
    Single<List<String>> getInvalidBarcodes();

    @GET("data/taxonomies/vitamins.json")
    Single<CategoriesWrapper> getVitamins();

    @GET("data/taxonomies/additives_classes.json")
    Single<CategoriesWrapper> getAdditivesClasses();

    @GET("data/taxonomies/nucleotides.json")
    Single<CategoriesWrapper> getNucleotides();

    @GET("data/taxonomies/nutrient_levels.json")
    Single<CategoriesWrapper> getNutrientLevels();

    @GET("data/taxonomies/languages.json")
    Single<CategoriesWrapper> getLanguages();

    @GET("data/taxonomies/nutrients.json")
    Single<CategoriesWrapper> getNutrients();

    @GET("data/taxonomies/minerals.json")
    Single<CategoriesWrapper> getMinerals();

    @GET("data/taxonomies/states.json")
    Single<CategoriesWrapper> getStates();

    @GET(ANALYSIS_TAG_JSON)
    Single<AnalysisTagsWrapper> getAnalysisTags();

    @GET(ANALYSIS_TAG_CONFIG_JSON)
    Single<AnalysisTagGonfigsWrapper> getAnalysisTagConfigs();
}
