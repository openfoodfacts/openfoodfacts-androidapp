package openfoodfacts.github.scrachx.openfood.network;

/**
 * Created by Lobster on 03.03.18.
 */

import io.reactivex.Single;
import openfoodfacts.github.scrachx.openfood.models.AdditivesWrapper;
import openfoodfacts.github.scrachx.openfood.models.AllergensWrapper;
import openfoodfacts.github.scrachx.openfood.models.CategoriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.CountriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.LabelsWrapper;
import retrofit2.http.GET;

/**
 * API calls for loading static multilingual data
 * This calls should be used as rare as possible, because they load Big Data
 */
public interface ProductApiService {

    @GET("data/taxonomies/labels.json")
    Single<LabelsWrapper> getLabels();

    @GET("data/taxonomies/allergens.json")
    Single<AllergensWrapper> getAllergens();

    @GET("data/taxonomies/additives.json")
    Single<AdditivesWrapper> getAdditives();

    @GET("data/taxonomies/countries.json")
    Single<CountriesWrapper> getCountries();

    @GET("data/taxonomies/categories.json")
    Single<CategoriesWrapper> getCategories();
    
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
    
    
}
