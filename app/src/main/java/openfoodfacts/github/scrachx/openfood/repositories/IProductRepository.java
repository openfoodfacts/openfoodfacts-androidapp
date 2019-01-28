
package openfoodfacts.github.scrachx.openfood.repositories;

import java.util.List;

import io.reactivex.Single;
import openfoodfacts.github.scrachx.openfood.models.Additive;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.Allergen;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.Category;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.Country;
import openfoodfacts.github.scrachx.openfood.models.CountryName;
import openfoodfacts.github.scrachx.openfood.models.Ingredient;
import openfoodfacts.github.scrachx.openfood.models.Label;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.Tag;

/**
 * Created by Lobster on 03.03.18.
 */

public interface IProductRepository {

    Single<List<Label>> getLabels(Boolean refresh);

    Single<List<Allergen>> getAllergens(Boolean refresh);

    Single<List<Tag>> getTags(Boolean refresh);

    Single<List<Additive>> getAdditives(Boolean refresh);

    Single<List<Country>> getCountries(Boolean refresh);

    Single<List<Category>> getCategories(Boolean refresh);

    Single<List<Ingredient>> getIngredients(Boolean refresh);

    void saveLabels(List<Label> labels);

    void saveTags(List<Tag> tags);

    void saveAdditives(List<Additive> additives);

    void saveCountries(List<Country> countries);

    void saveAllergens(List<Allergen> allergens);

    void saveCategories(List<Category> categories);

    void deleteIngredientCascade();

    void saveIngredients(List<Ingredient> ingredients);

    void saveIngredient(Ingredient ingredient);

    void setAllergenEnabled(String allergenTag, Boolean isEnabled);

    Single<LabelName> getLabelByTagAndLanguageCode(String labelTag, String languageCode);

    Single<LabelName> getLabelByTagAndDefaultLanguageCode(String labelTag);

    Single<CountryName> getCountryByTagAndLanguageCode(String labelTag, String languageCode);

    Single<CountryName> getCountryByTagAndDefaultLanguageCode(String labelTag);

    Single<AdditiveName> getAdditiveByTagAndLanguageCode(String additiveTag, String languageCode);

    Single<AdditiveName> getAdditiveByTagAndDefaultLanguageCode(String additiveTag);

    Single<CategoryName> getCategoryByTagAndLanguageCode(String categoryTag, String languageCode);

    Single<CategoryName> getCategoryByTagAndDefaultLanguageCode(String categoryTag);

    Single<List<CategoryName>> getAllCategoriesByLanguageCode(String languageCode);

    Single<List<CategoryName>> getAllCategoriesByDefaultLanguageCode();

    List<Allergen> getEnabledAllergens();

    Single<List<AllergenName>> getAllergensByEnabledAndLanguageCode(Boolean isEnabled, String languageCode);

    Single<List<AllergenName>> getAllergensByLanguageCode(String languageCode);

    Single<AllergenName> getAllergenByTagAndLanguageCode(String allergenTag, String languageCode);

    Single<AllergenName> getAllergenByTagAndDefaultLanguageCode(String allergenTag);

    Boolean additivesIsEmpty();

}