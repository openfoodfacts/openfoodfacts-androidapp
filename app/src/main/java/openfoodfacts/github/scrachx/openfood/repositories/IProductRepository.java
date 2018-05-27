
package org.openfoodfacts.scanner.repositories;

import java.util.List;

import io.reactivex.Single;
import org.openfoodfacts.scanner.models.Additive;
import org.openfoodfacts.scanner.models.AdditiveName;
import org.openfoodfacts.scanner.models.Allergen;
import org.openfoodfacts.scanner.models.AllergenName;
import org.openfoodfacts.scanner.models.Category;
import org.openfoodfacts.scanner.models.CategoryName;
import org.openfoodfacts.scanner.models.Country;
import org.openfoodfacts.scanner.models.CountryName;
import org.openfoodfacts.scanner.models.Label;
import org.openfoodfacts.scanner.models.LabelName;
import org.openfoodfacts.scanner.models.Tag;

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

    Boolean additivesIsEmpty();

}