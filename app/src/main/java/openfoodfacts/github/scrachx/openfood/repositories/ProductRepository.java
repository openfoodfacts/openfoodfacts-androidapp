/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.repositories;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringUtils;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;
import openfoodfacts.github.scrachx.openfood.models.Additive;
import openfoodfacts.github.scrachx.openfood.models.AdditiveDao;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.AdditiveNameDao;
import openfoodfacts.github.scrachx.openfood.models.AdditivesWrapper;
import openfoodfacts.github.scrachx.openfood.models.Allergen;
import openfoodfacts.github.scrachx.openfood.models.AllergenDao;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.AllergenNameDao;
import openfoodfacts.github.scrachx.openfood.models.AllergensWrapper;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTag;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagConfig;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagConfigDao;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagDao;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagGonfigsWrapper;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagName;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagNameDao;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagsWrapper;
import openfoodfacts.github.scrachx.openfood.models.AnnotationAnswer;
import openfoodfacts.github.scrachx.openfood.models.AnnotationResponse;
import openfoodfacts.github.scrachx.openfood.models.CategoriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.Category;
import openfoodfacts.github.scrachx.openfood.models.CategoryDao;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.CategoryNameDao;
import openfoodfacts.github.scrachx.openfood.models.CountriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.Country;
import openfoodfacts.github.scrachx.openfood.models.CountryDao;
import openfoodfacts.github.scrachx.openfood.models.CountryName;
import openfoodfacts.github.scrachx.openfood.models.CountryNameDao;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.Ingredient;
import openfoodfacts.github.scrachx.openfood.models.IngredientDao;
import openfoodfacts.github.scrachx.openfood.models.IngredientName;
import openfoodfacts.github.scrachx.openfood.models.IngredientNameDao;
import openfoodfacts.github.scrachx.openfood.models.IngredientsRelation;
import openfoodfacts.github.scrachx.openfood.models.IngredientsRelationDao;
import openfoodfacts.github.scrachx.openfood.models.IngredientsWrapper;
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcode;
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcodeDao;
import openfoodfacts.github.scrachx.openfood.models.Label;
import openfoodfacts.github.scrachx.openfood.models.LabelDao;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.LabelNameDao;
import openfoodfacts.github.scrachx.openfood.models.LabelsWrapper;
import openfoodfacts.github.scrachx.openfood.models.Question;
import openfoodfacts.github.scrachx.openfood.models.QuestionsState;
import openfoodfacts.github.scrachx.openfood.models.Tag;
import openfoodfacts.github.scrachx.openfood.models.TagDao;
import openfoodfacts.github.scrachx.openfood.models.TagsWrapper;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.services.AnalysisDataAPI;
import openfoodfacts.github.scrachx.openfood.network.services.RobotoffAPI;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

/**
 * This is a repository class which implements repository interface.
 *
 * @author Lobster
 * @since 03.03.18
 */
public class ProductRepository {
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String TAG = ProductRepository.class.getSimpleName();
    private static ProductRepository instance;
    private final AdditiveDao additiveDao;
    private final AdditiveNameDao additiveNameDao;
    private final AllergenDao allergenDao;
    private final AllergenNameDao allergenNameDao;
    private final AnalysisTagConfigDao analysisTagConfigDao;
    private final AnalysisTagDao analysisTagDao;
    private final AnalysisTagNameDao analysisTagNameDao;
    private final CategoryDao categoryDao;
    private final CategoryNameDao categoryNameDao;
    private final CountryDao countryDao;
    private final CountryNameDao countryNameDao;
    private final Database db;
    private final IngredientDao ingredientDao;
    private final IngredientNameDao ingredientNameDao;
    private final IngredientsRelationDao ingredientsRelationDao;
    private final InvalidBarcodeDao invalidBarcodeDao;
    private final LabelDao labelDao;
    private final LabelNameDao labelNameDao;
    private final AnalysisDataAPI productApi;
    private final RobotoffAPI robotoffApi;
    private final TagDao tagDao;

    /**
     * A method used to get instance from the repository.
     *
     * @return : instance of the repository
     */
    public static ProductRepository getInstance() {
        if (instance == null) {
            instance = new ProductRepository();
        }

        return instance;
    }

    /**
     * Constructor of the class which is used to initialize objects.
     */
    private ProductRepository() {
        productApi = CommonApiManager.getInstance().getAnalysisDataApi();
        robotoffApi = CommonApiManager.getInstance().getRobotoffApi();

        DaoSession daoSession = OFFApplication.getDaoSession();
        db = daoSession.getDatabase();
        labelDao = daoSession.getLabelDao();
        labelNameDao = daoSession.getLabelNameDao();
        tagDao = daoSession.getTagDao();
        invalidBarcodeDao = daoSession.getInvalidBarcodeDao();
        allergenDao = daoSession.getAllergenDao();
        allergenNameDao = daoSession.getAllergenNameDao();
        additiveDao = daoSession.getAdditiveDao();
        additiveNameDao = daoSession.getAdditiveNameDao();
        countryDao = daoSession.getCountryDao();
        countryNameDao = daoSession.getCountryNameDao();
        categoryDao = daoSession.getCategoryDao();
        categoryNameDao = daoSession.getCategoryNameDao();
        ingredientDao = daoSession.getIngredientDao();
        ingredientNameDao = daoSession.getIngredientNameDao();
        ingredientsRelationDao = daoSession.getIngredientsRelationDao();
        analysisTagDao = daoSession.getAnalysisTagDao();
        analysisTagNameDao = daoSession.getAnalysisTagNameDao();
        analysisTagConfigDao = daoSession.getAnalysisTagConfigDao();
    }

    /**
     * Load labels from the server or local database
     *
     * @return The list of Labels.
     */
    public Single<List<Label>> reloadLabelsFromServer() {
        return Taxonomy.LABEL.getTaxonomyData(this, true, false, labelDao);
    }

    Single<List<Label>> loadLabels(long lastModifiedDate) {
        return productApi.getLabels()
            .map(LabelsWrapper::map)
            .doOnSuccess(labels -> {
                saveLabels(labels);
                updateLastDownloadDateInSettings(Taxonomy.LABEL, lastModifiedDate);
            });
    }

    /**
     * Load tags from the server or local database
     *
     * @return The list of Tags.
     */
    public Single<List<Tag>> reloadTagsFromServer() {
        return Taxonomy.TAGS.getTaxonomyData(this, true, false, tagDao);
    }

    Single<List<Tag>> loadTags(long lastModifiedDate) {
        return productApi.getTags()
            .map(TagsWrapper::getTags)
            .doOnSuccess(tags -> {
                saveTags(tags);
                updateLastDownloadDateInSettings(Taxonomy.TAGS, lastModifiedDate);
            });
    }

    public Single<List<InvalidBarcode>> reloadInvalidBarcodesFromServer() {
        return Taxonomy.INVALID_BARCODES.getTaxonomyData(this, true, false, invalidBarcodeDao);
    }

    Single<List<InvalidBarcode>> loadInvalidBarcodes(long lastModifiedDate) {
        return productApi.getInvalidBarcodes()
            .map(strings -> {
                List<InvalidBarcode> toSave = new ArrayList<>(strings.size());
                for (String string : strings) {
                    toSave.add(new InvalidBarcode(string));
                }
                return toSave;
            })
            .doOnSuccess(invalidBarcodes -> {
                saveInvalidBarcodes(invalidBarcodes);
                updateLastDownloadDateInSettings(Taxonomy.INVALID_BARCODES, lastModifiedDate);
            });
    }

    /**
     * Load allergens from the server or local database
     *
     * @return The allergens in the product.
     */
    public Single<List<Allergen>> reloadAllergensFromServer() {
        // FIXME: this returns 404
        return Taxonomy.ALLERGEN.getTaxonomyData(this, true, false, allergenDao);
    }

    public Single<List<Allergen>> getAllergens() {
        return Taxonomy.ALLERGEN.getTaxonomyData(this, false, true, allergenDao);
    }

    Single<List<Allergen>> loadAllergens(Long lastModifiedDate) {
        return productApi.getAllergens()
            .map(AllergensWrapper::map)
            .doOnSuccess(allergens -> {
                saveAllergens(allergens);
                updateLastDownloadDateInSettings(Taxonomy.ALLERGEN, lastModifiedDate);
            });
    }

    /**
     * Load countries from the server or local database
     *
     * @return The list of countries.
     */
    public Single<List<Country>> reloadCountriesFromServer() {
        return Taxonomy.COUNTRY.getTaxonomyData(this, true, false, countryDao);
    }

    Single<List<Country>> loadCountries(Long lastModifiedDate) {
        return productApi.getCountries()
            .map(CountriesWrapper::map)
            .doOnSuccess(countries -> {
                saveCountries(countries);
                updateLastDownloadDateInSettings(Taxonomy.COUNTRY, lastModifiedDate);
            });
    }

    /**
     * Load categories from the server or local database
     *
     * @return The list of categories.
     */
    public Single<List<Category>> reloadCategoriesFromServer() {
        return Taxonomy.CATEGORY.getTaxonomyData(this, true, false, categoryDao);
    }

    public Single<List<Category>> getCategories() {
        return Taxonomy.CATEGORY.getTaxonomyData(this, false, true, categoryDao);
    }

    Single<List<Category>> loadCategories(Long lastModifiedDate) {
        return productApi.getCategories()
            .map(CategoriesWrapper::map)
            .doOnSuccess(categories -> {
                saveCategories(categories);
                updateLastDownloadDateInSettings(Taxonomy.CATEGORY, lastModifiedDate);
            });
    }

    /**
     * Load allergens which user selected earlier (i.e user's allergens)
     *
     * @return The list of allergens.
     */
    public List<Allergen> getEnabledAllergens() {
        return allergenDao.queryBuilder().where(AllergenDao.Properties.Enabled.eq("true")).list();
    }

    /**
     * Load additives from the server or local database
     *
     * @return The list of additives.
     */
    public Single<List<Additive>> reloadAdditivesFromServer() {
        return Taxonomy.ADDITIVE.getTaxonomyData(this, true, false, additiveDao);
    }

    Single<List<Additive>> loadAdditives(long lastModifiedDate) {
        return productApi.getAdditives()
            .map(AdditivesWrapper::map)
            .doOnSuccess(additives ->
            {
                saveAdditives(additives);
                updateLastDownloadDateInSettings(Taxonomy.ADDITIVE, lastModifiedDate);
            });
    }

    /**
     * TODO to be improved by loading only in the user language ?
     * Load ingredients from (the server or) local database
     * If SharedPreferences lastDownloadIngredients is set try this :
     * if file from the server is newer than last download delete database, load the file and fill database,
     * else if database is empty, download the file and fill database,
     * else return the content from the local database.
     *
     * @return The ingredients in the product.
     */
    public Single<List<Ingredient>> reloadIngredientsFromServer() {
        return Taxonomy.INGREDIENT.getTaxonomyData(this, true, false, ingredientDao);
    }

    Single<List<Ingredient>> loadIngredients(long lastModifiedDate) {
        return productApi.getIngredients()
            .map(IngredientsWrapper::map)
            .doOnSuccess(ingredients -> {
                saveIngredients(ingredients);
                updateLastDownloadDateInSettings(Taxonomy.INGREDIENT, lastModifiedDate);
            });
    }

    /**
     * This function set lastDownloadtaxonomy setting
     *
     * @param taxonomy Name of the taxonomy (allergens, additives, categories, countries, ingredients, labels, tags)
     * @param lastDownload Date of last update on Long format
     */
    private void updateLastDownloadDateInSettings(Taxonomy taxonomy, long lastDownload) {
        SharedPreferences mSettings = OFFApplication.getInstance().getSharedPreferences("prefs", 0);
        mSettings.edit().putLong(taxonomy.getLastDownloadTimeStampPreferenceId(), lastDownload).apply();
        Log.i(TAG, "Set lastDownload of " + taxonomy + " to " + lastDownload);
    }

    /**
     * Labels saving to local database
     *
     * @param labels The list of labels to be saved.
     *     <p>
     *     Label and LabelName has One-To-Many relationship, therefore we need to save them separately.
     */
    private void saveLabels(List<Label> labels) {
        db.beginTransaction();
        try {
            for (Label label : labels) {
                labelDao.insertOrReplace(label);
                for (LabelName labelName : label.getNames()) {
                    labelNameDao.insertOrReplace(labelName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "saveLabels", e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Tags saving to local database
     *
     * @param tags The list of tags to be saved.
     */
    private void saveTags(List<Tag> tags) {
        tagDao.insertOrReplaceInTx(tags);
    }

    /**
     * Invalid Barcodess saving to local database. Will clear all previous invalid barcodes stored before.
     *
     * @param invalidBarcodes The list of invalidBarcodes to be saved.
     */
    private void saveInvalidBarcodes(List<InvalidBarcode> invalidBarcodes) {
        invalidBarcodeDao.deleteAll();
        invalidBarcodeDao.insertOrReplaceInTx(invalidBarcodes);
    }

    /**
     * Allergens saving to local database
     *
     * @param allergens The list of allergens to be saved.
     *     <p>
     *     Allergen and AllergenName has One-To-Many relationship, therefore we need to save them separately.
     */
    void saveAllergens(List<Allergen> allergens) {
        db.beginTransaction();
        try {
            for (Allergen allergen : allergens) {
                allergenDao.insertOrReplace(allergen);
                for (AllergenName allergenName : allergen.getNames()) {
                    allergenNameDao.insertOrReplace(allergenName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "saveAllergens", e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Additives saving to local database
     *
     * @param additives The list of additives to be saved.
     *     <p>
     *     Additive and AdditiveName has One-To-Many relationship, therefore we need to save them separately.
     */
    private void saveAdditives(List<Additive> additives) {
        db.beginTransaction();
        try {
            for (Additive additive : additives) {
                additiveDao.insertOrReplace(additive);
                for (AdditiveName allergenName : additive.getNames()) {
                    additiveNameDao.insertOrReplace(allergenName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "saveAdditives", e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Countries saving to local database
     *
     * @param countries The list of countries to be saved.
     *     <p>
     *     Country and CountryName has One-To-Many relationship, therefore we need to save them separately.
     */
    private void saveCountries(List<Country> countries) {
        db.beginTransaction();
        try {
            for (Country country : countries) {
                countryDao.insertOrReplace(country);
                for (CountryName countryName : country.getNames()) {
                    countryNameDao.insertOrReplace(countryName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "saveCountries", e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Categories saving to local database
     *
     * @param categories The list of categories to be saved.
     *     <p>
     *     Category and CategoryName has One-To-Many relationship, therefore we need to save them separately.
     */
    private void saveCategories(List<Category> categories) {
        db.beginTransaction();
        try {
            for (Category category : categories) {
                categoryDao.insertOrReplace(category);
                for (CategoryName categoryName : category.getNames()) {
                    categoryNameDao.insertOrReplace(categoryName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "saveCategories", e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Delete rows from Ingredient, IngredientName and IngredientsRelation
     * set the autoincrement to 0
     */
    public void deleteIngredientCascade() {
        ingredientDao.deleteAll();
        ingredientNameDao.deleteAll();
        ingredientsRelationDao.deleteAll();
        DaoSession daoSession = OFFApplication.getDaoSession();
        daoSession.getDatabase().execSQL(
            "update sqlite_sequence set seq=0 where name in ('" + ingredientDao.getTablename() + "', '" + ingredientNameDao.getTablename() + "', '" + ingredientsRelationDao
                .getTablename() + "')");
    }

    /**
     * TODO to be improved by loading only if required and only in the user language
     * Ingredients saving to local database
     *
     * @param ingredients The list of ingredients to be saved.
     *     <p>
     *     Ingredient and IngredientName has One-To-Many relationship, therefore we need to save them separately.
     */
    private void saveIngredients(List<Ingredient> ingredients) {
        db.beginTransaction();
        try {
            for (Ingredient ingredient : ingredients) {
                ingredientDao.insertOrReplace(ingredient);
                for (IngredientName ingredientName : ingredient.getNames()) {
                    ingredientNameDao.insertOrReplace(ingredientName);
                }
                for (IngredientsRelation ingredientsRelation : ingredient.getParents()) {
                    ingredientsRelationDao.insertOrReplace(ingredientsRelation);
                }
                for (IngredientsRelation ingredientsRelation : ingredient.getChildren()) {
                    ingredientsRelationDao.insertOrReplace(ingredientsRelation);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "saveIngredients", e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Ingredient saving to local database
     *
     * @param ingredient The ingredient to be saved.
     */
    public void saveIngredient(Ingredient ingredient) {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);
        saveIngredients(ingredients);
    }

    /**
     * Changes enabled field of allergen and updates it.
     *
     * @param isEnabled depends on whether user selected or unselected the allergen
     * @param allergenTag is unique Id of allergen
     */
    public void setAllergenEnabled(String allergenTag, Boolean isEnabled) {
        Allergen allergen = allergenDao.queryBuilder()
            .where(AllergenDao.Properties.Tag.eq(allergenTag))
            .unique();

        if (allergen != null) {
            allergen.setEnabled(isEnabled);
            allergenDao.update(allergen);
        }
    }

    /**
     * Loads translated label from the local database by unique tag of label and language code
     *
     * @param labelTag is a unique Id of label
     * @param languageCode is a 2-digit language code
     * @return The translated label
     */
    public Single<LabelName> getLabelByTagAndLanguageCode(String labelTag, String languageCode) {
        return Single.fromCallable(() -> {
            LabelName labelName = labelNameDao.queryBuilder()
                .where(
                    LabelNameDao.Properties.LabelTag.eq(labelTag),
                    LabelNameDao.Properties.LanguageCode.eq(languageCode)
                ).unique();

            return labelName != null ? labelName : new LabelName();
        });
    }

    /**
     * Loads translated label from the local database by unique tag of label and default language code
     *
     * @param labelTag is a unique Id of label
     * @return The translated label
     */
    public Single<LabelName> getLabelByTagAndDefaultLanguageCode(String labelTag) {
        return getLabelByTagAndLanguageCode(labelTag, DEFAULT_LANGUAGE);
    }

    /**
     * Loads translated additive from the local database by unique tag of additive and language code
     *
     * @param additiveTag is a unique Id of additive
     * @param languageCode is a 2-digit language code
     * @return The translated additive name
     */
    public Single<AdditiveName> getAdditiveByTagAndLanguageCode(String additiveTag, String languageCode) {
        return Single.fromCallable(() -> {
            AdditiveName additiveName = additiveNameDao.queryBuilder()
                .where(
                    AdditiveNameDao.Properties.AdditiveTag.eq(additiveTag),
                    AdditiveNameDao.Properties.LanguageCode.eq(languageCode)
                ).unique();

            return additiveName != null ? additiveName : new AdditiveName();
        });
    }

    /**
     * Loads translated additive from the local database by unique tag of additive and default language code
     *
     * @param additiveTag is a unique Id of additive
     * @return The translated additive tag
     */
    public Single<AdditiveName> getAdditiveByTagAndDefaultLanguageCode(String additiveTag) {
        return getAdditiveByTagAndLanguageCode(additiveTag, DEFAULT_LANGUAGE);
    }

    /**
     * Loads translated country from the local database by unique tag of country and language code
     *
     * @param countryTag is a unique Id of country
     * @param languageCode is a 2-digit language code
     * @return The translated country name
     */
    public Single<CountryName> getCountryByTagAndLanguageCode(String countryTag, String languageCode) {
        return Single.fromCallable(() -> {
            CountryName countryName = countryNameDao.queryBuilder()
                .where(
                    CountryNameDao.Properties.CountyTag.eq(countryTag),
                    CountryNameDao.Properties.LanguageCode.eq(languageCode)
                ).unique();

            return countryName != null ? countryName : new CountryName();
        });
    }

    /**
     * Loads translated country from the local database by unique tag of country and default language code
     *
     * @param countryTag is a unique Id of country
     * @return The translated country name
     */
    public Single<CountryName> getCountryByTagAndDefaultLanguageCode(String countryTag) {
        return getCountryByTagAndLanguageCode(countryTag, DEFAULT_LANGUAGE);
    }

    /**
     * Loads translated category from the local database by unique tag of category and language code
     *
     * @param categoryTag is a unique Id of category
     * @param languageCode is a 2-digit language code
     * @return The translated category name
     */
    public Single<CategoryName> getCategoryByTagAndLanguageCode(String categoryTag, String languageCode) {
        return Single.fromCallable(() -> {
            CategoryName categoryName = categoryNameDao.queryBuilder()
                .where(
                    CategoryNameDao.Properties.CategoryTag.eq(categoryTag),
                    CategoryNameDao.Properties.LanguageCode.eq(languageCode)
                ).unique();

            if (categoryName != null) {
                return categoryName;
            } else {
                CategoryName emptyCategoryName = new CategoryName();
                emptyCategoryName.setName(categoryTag);
                emptyCategoryName.setCategoryTag(categoryTag);
                emptyCategoryName.setIsWikiDataIdPresent(false);
                return emptyCategoryName;
            }
        });
    }

    /**
     * Loads translated category from the local database by unique tag of category and default language code
     *
     * @param categoryTag is a unique Id of category
     * @return The translated category name
     */
    public Single<CategoryName> getCategoryByTagAndDefaultLanguageCode(String categoryTag) {
        return getCategoryByTagAndLanguageCode(categoryTag, DEFAULT_LANGUAGE);
    }

    /**
     * Loads list of translated category names from the local database by language code
     *
     * @param languageCode is a 2-digit language code
     * @return The translated list of category name
     */
    public Single<List<CategoryName>> getAllCategoriesByLanguageCode(String languageCode) {
        return Single.fromCallable(() -> categoryNameDao.queryBuilder()
            .where(CategoryNameDao.Properties.LanguageCode.eq(languageCode))
            .orderAsc(CategoryNameDao.Properties.Name)
            .list());
    }

    /**
     * Loads list of category names from the local database by default language code
     *
     * @return The list of category name
     */
    public Single<List<CategoryName>> getAllCategoriesByDefaultLanguageCode() {
        return getAllCategoriesByLanguageCode(DEFAULT_LANGUAGE);
    }

    /**
     * Loads translated and selected/unselected allergens.
     *
     * @param isEnabled depends on whether allergen was selected or unselected by user
     * @param languageCode is a 2-digit language code
     * @return The list of allergen names
     */
    public Single<List<AllergenName>> getAllergensByEnabledAndLanguageCode(Boolean isEnabled, String languageCode) {
        return Single.fromCallable(() -> {
            List<Allergen> allergens = allergenDao.queryBuilder().where(AllergenDao.Properties.Enabled.eq(isEnabled)).list();
            if (allergens != null) {
                List<AllergenName> allergenNames = new ArrayList<>();
                for (Allergen allergen : allergens) {
                    AllergenName name = allergenNameDao.queryBuilder()
                        .where(
                            AllergenNameDao.Properties.AllergenTag.eq(allergen.getTag()),
                            AllergenNameDao.Properties.LanguageCode.eq(languageCode)
                        ).unique();

                    if (name != null) {
                        allergenNames.add(name);
                    }
                }

                return allergenNames;
            }

            return Collections.emptyList();
        });
    }

    /**
     * Loads all translated allergens.
     *
     * @param languageCode is a 2-digit language code
     * @return The list of translated allergen names
     */
    public Single<List<AllergenName>> getAllergensByLanguageCode(String languageCode) {
        return Single.fromCallable(() ->
            allergenNameDao.queryBuilder()
                .where(AllergenNameDao.Properties.LanguageCode.eq(languageCode))
                .list());
    }

    /**
     * Loads translated allergen from the local database by unique tag of allergen and language code
     *
     * @param allergenTag is a unique Id of allergen
     * @param languageCode is a 2-digit language code
     * @return The translated allergen name
     */
    public Single<AllergenName> getAllergenByTagAndLanguageCode(String allergenTag, String languageCode) {
        return Single.fromCallable(() -> {
            AllergenName allergenName = allergenNameDao.queryBuilder()
                .where(AllergenNameDao.Properties.AllergenTag.eq(allergenTag),
                    AllergenNameDao.Properties.LanguageCode.eq(languageCode))
                .unique();

            if (allergenName != null) {
                return allergenName;
            } else {
                AllergenName emptyAllergenName = new AllergenName();
                emptyAllergenName.setName(allergenTag);
                emptyAllergenName.setAllergenTag(allergenTag);
                emptyAllergenName.setIsWikiDataIdPresent(false);
                return emptyAllergenName;
            }
        });
    }

    /**
     * Loads translated allergen from the local database by unique tag of allergen and default language code
     *
     * @param allergenTag is a unique Id of allergen
     * @return The translated allergen name
     */
    public Single<AllergenName> getAllergenByTagAndDefaultLanguageCode(String allergenTag) {
        return getAllergenByTagAndLanguageCode(allergenTag, DEFAULT_LANGUAGE);
    }

    /**
     * Loads Robotoff question from the local database by code and lang of question.
     *
     * @param code for the question
     * @param lang is language of the question
     * @return The single question
     */
    public Single<Question> getSingleProductQuestion(String code, String lang) {
        return robotoffApi.getProductQuestion(code, lang, 1)
            .map(QuestionsState::getQuestions)
            .map(questions -> {
                if (!questions.isEmpty()) {
                    return questions.get(0);
                }
                return QuestionsState.EMPTY_QUESTION;
            });
    }

    /**
     * Annotate the Robotoff insight response using insight id and annotation
     *
     * @param insightId is the unique id for the insight
     * @param annotation is the annotation to be used
     * @return The annotated insight response
     */
    public Single<AnnotationResponse> annotateInsight(String insightId, AnnotationAnswer annotation) {
        // if the user is logged in, send the auth, otherwise make it anonymous
        final SharedPreferences userPref = OFFApplication.getInstance()
            .getSharedPreferences("login", 0);

        final String user = userPref.getString("user", "").trim();
        final String pass = userPref.getString("pass", "").trim();

        if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(pass)) {
            final String baseAuth = "Basic " + Base64.encodeToString(
                (user + ":" + pass).getBytes(), Base64.NO_WRAP);

            return robotoffApi.annotateInsight(insightId, annotation.getResult(), baseAuth);
        } else {
            return robotoffApi.annotateInsight(insightId, annotation.getResult());
        }
    }

    /**
     * Load analysis tags from the server or local database
     *
     * @return The analysis tags in the product.
     */
    public Single<List<AnalysisTag>> reloadAnalysisTagsFromServer() {
        return Taxonomy.ANALYSIS_TAGS.getTaxonomyData(this, true, false, analysisTagDao);
    }

    Single<List<AnalysisTag>> loadAnalysisTags(long lastModifiedDate) {
        return productApi.getAnalysisTags()
            .map(AnalysisTagsWrapper::map)
            .doOnSuccess(analysisTags -> {
                saveAnalysisTags(analysisTags);
                updateLastDownloadDateInSettings(Taxonomy.ANALYSIS_TAGS, lastModifiedDate);
            });
    }

    /**
     * AnalysisTags saving to local database
     *
     * @param analysisTags The list of analysis tags to be saved.
     *     <p>
     *     AnalysisTag and AnalysisTagName has One-To-Many relationship, therefore we need to save them separately.
     */
    private void saveAnalysisTags(List<AnalysisTag> analysisTags) {
        db.beginTransaction();
        try {
            for (AnalysisTag analysisTag : analysisTags) {
                analysisTagDao.insertOrReplace(analysisTag);
                for (AnalysisTagName analysisTagName : analysisTag.getNames()) {
                    analysisTagNameDao.insertOrReplace(analysisTagName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "saveAnalysisTags", e);
        } finally {
            db.endTransaction();
        }
    }

    public Single<List<AnalysisTagConfig>> reloadAnalysisTagConfigsFromServer() {
        return Taxonomy.ANALYSIS_TAG_CONFIG.getTaxonomyData(this, true, false, analysisTagConfigDao);
    }

    Single<List<AnalysisTagConfig>> loadAnalysisTagConfigs(long lastModifiedDate) {
        return productApi.getAnalysisTagConfigs()
            .map(AnalysisTagGonfigsWrapper::map).doOnSuccess(analysisTagConfigs -> {
                saveAnalysisTagConfigs(analysisTagConfigs);
                updateLastDownloadDateInSettings(Taxonomy.ANALYSIS_TAG_CONFIG, lastModifiedDate);
            });
    }

    private void saveAnalysisTagConfigs(List<AnalysisTagConfig> analysisTagConfigs) {
        db.beginTransaction();
        try {
            for (AnalysisTagConfig analysisTagConfig : analysisTagConfigs) {
                Picasso.get().load(analysisTagConfig.getIconUrl()).fetch();
                analysisTagConfigDao.insertOrReplace(analysisTagConfig);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "saveAnalysisTagConfigs", e);
        } finally {
            db.endTransaction();
        }
    }

    private void updateAnalysisTagConfig(final AnalysisTagConfig analysisTagConfig, String languageCode) {
        if (analysisTagConfig != null) {
            AnalysisTagName analysisTagName = analysisTagNameDao.queryBuilder()
                .where(AnalysisTagNameDao.Properties.AnalysisTag.eq(analysisTagConfig.getAnalysisTag()),
                    AnalysisTagNameDao.Properties.LanguageCode.eq(languageCode))
                .unique();
            if (analysisTagName == null) {
                analysisTagName = analysisTagNameDao.queryBuilder()
                    .where(AnalysisTagNameDao.Properties.AnalysisTag.eq(analysisTagConfig.getAnalysisTag()),
                        AnalysisTagNameDao.Properties.LanguageCode.eq(DEFAULT_LANGUAGE))
                    .unique();
            }

            analysisTagConfig.setName(analysisTagName);

            String type = "en:" + analysisTagConfig.getType();
            AnalysisTagName analysisTagTypeName = analysisTagNameDao.queryBuilder()
                .where(AnalysisTagNameDao.Properties.AnalysisTag.eq(type),
                    AnalysisTagNameDao.Properties.LanguageCode.eq(languageCode))
                .unique();
            if (analysisTagTypeName == null) {
                analysisTagTypeName = analysisTagNameDao.queryBuilder()
                    .where(AnalysisTagNameDao.Properties.AnalysisTag.eq(type),
                        AnalysisTagNameDao.Properties.LanguageCode.eq(DEFAULT_LANGUAGE))
                    .unique();
            }

            analysisTagConfig.setTypeName(analysisTagTypeName != null ? analysisTagTypeName.getName() : analysisTagConfig.getType());
        }
    }

    /**
     * @param analysisTag
     * @param languageCode
     * @return {@link Maybe#empty()} if no analysis tag found
     */
    public Maybe<AnalysisTagConfig> getAnalysisTagConfigByTagAndLanguageCode(final String analysisTag, final String languageCode) {
        return Maybe.fromCallable(() -> {
            AnalysisTagConfig analysisTagConfig = analysisTagConfigDao.queryBuilder()
                .where(AnalysisTagConfigDao.Properties.AnalysisTag.eq(analysisTag))
                .unique();
            updateAnalysisTagConfig(analysisTagConfig, languageCode);
            return analysisTagConfig;
        });
    }

    public Single<List<AnalysisTagConfig>> getUnknownAnalysisTagConfigsByLanguageCode(String languageCode) {
        return Single.fromCallable(() -> {
            List<AnalysisTagConfig> analysisTagConfigs = analysisTagConfigDao.queryBuilder()
                .where(new WhereCondition.StringCondition(AnalysisTagConfigDao.Properties.AnalysisTag.columnName + " LIKE \"%unknown%\"")).list();

            for (AnalysisTagConfig analysisTagConfig : analysisTagConfigs
            ) {
                updateAnalysisTagConfig(analysisTagConfig, languageCode);
            }
            return analysisTagConfigs;
        });
    }
}
