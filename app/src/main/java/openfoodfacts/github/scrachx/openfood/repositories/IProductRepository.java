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

    void saveLabels(List<Label> labels);

    void saveTags(List<Tag> tags);

    void saveAdditives(List<Additive> additives);

    void saveCountries(List<Country> countries);

    void saveAllergens(List<Allergen> allergens);

    void saveCategories(List<Category> categories);

    void setAllergenEnabled(String allergenTag, Boolean isEnabled);

    LabelName getLabelByTagAndLanguageCode(String labelTag, String languageCode);

    LabelName getLabelByTagAndDefaultLanguageCode(String labelTag);

    CountryName getCountryByTagAndLanguageCode(String labelTag, String languageCode);

    CountryName getCountryByTagAndDefaultLanguageCode(String labelTag);

    AdditiveName getAdditiveByTagAndLanguageCode(String additiveTag, String languageCode);

    AdditiveName getAdditiveByTagAndDefaultLanguageCode(String additiveTag);

    CategoryName getCategoryByTagAndLanguageCode(String categoryTag, String languageCode);

    CategoryName getCategoryByTagAndDefaultLanguageCode(String categoryTag);

    Single<List<CategoryName>> getAllCategoriesByLanguageCode(String languageCode);

    Single<List<CategoryName>> getAllCategoriesByDefaultLanguageCode();

    List<Allergen> getEnabledAllergens();

    List<AllergenName> getAllergensByEnabledAndLanguageCode(Boolean isEnabled, String languageCode);

    List<AllergenName> getAllergensByLanguageCode(String languageCode);

    Boolean additivesIsEmpty();

}
