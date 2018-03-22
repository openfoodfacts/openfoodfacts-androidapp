package openfoodfacts.github.scrachx.openfood.repositories;

import org.greenrobot.greendao.AbstractDao;

import java.util.ArrayList;
import java.util.List;

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
import openfoodfacts.github.scrachx.openfood.models.Label;
import openfoodfacts.github.scrachx.openfood.models.LabelDao;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.LabelNameDao;
import openfoodfacts.github.scrachx.openfood.models.LabelsWrapper;
import openfoodfacts.github.scrachx.openfood.models.Tag;
import openfoodfacts.github.scrachx.openfood.models.TagDao;
import openfoodfacts.github.scrachx.openfood.models.TagsWrapper;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.network.ProductApiService;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

/**
 * Created by Lobster on 03.03.18.
 */

public class ProductRepository implements IProductRepository {

    private static final String DEFAULT_LANGUAGE = "en";

    private static IProductRepository instance;

    private ProductApiService productApi;
    private OpenFoodAPIService openFooApi;

    private LabelDao labelDao;
    private LabelNameDao labelNameDao;
    private TagDao tagDao;
    private AllergenDao allergenDao;
    private AllergenNameDao allergenNameDao;
    private AdditiveDao additiveDao;
    private AdditiveNameDao additiveNameDao;
    private CountryDao countryDao;
    private CountryNameDao countryNameDao;
    private CategoryDao categoryDao;
    private CategoryNameDao categoryNameDao;

    public static IProductRepository getInstance() {
        if (instance == null) {
            instance = new ProductRepository();
        }

        return instance;
    }

    private ProductRepository() {
        productApi = CommonApiManager.getInstance().getProductApiService();
        openFooApi = CommonApiManager.getInstance().getOpenFoodApiService();

        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        labelDao = daoSession.getLabelDao();
        labelNameDao = daoSession.getLabelNameDao();
        tagDao = daoSession.getTagDao();
        allergenDao = daoSession.getAllergenDao();
        allergenNameDao = daoSession.getAllergenNameDao();
        additiveDao = daoSession.getAdditiveDao();
        additiveNameDao = daoSession.getAdditiveNameDao();
        countryDao = daoSession.getCountryDao();
        countryNameDao = daoSession.getCountryNameDao();
        categoryDao = daoSession.getCategoryDao();
        categoryNameDao = daoSession.getCategoryNameDao();
    }

    /**
     * Load labels from the server or local database
     *
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     */
    @Override
    public Single<List<Label>> getLabels(Boolean refresh) {
        if (refresh || tableIsEmpty(labelDao)) {
            return productApi.getLabels()
                    .map(LabelsWrapper::map);
        } else {
            return Single.fromCallable(() -> labelDao.loadAll());
        }
    }

    /**
     * Load tags from the server or local database
     *
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     */
    @Override
    public Single<List<Tag>> getTags(Boolean refresh) {
        if (refresh || tableIsEmpty(labelDao)) {
            return openFooApi.getTags()
                    .map(TagsWrapper::getTags);
        } else {
            return Single.fromCallable(() -> tagDao.loadAll());
        }
    }

    /**
     * Load allergens from the server or local database
     *
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     * @return The allergens in the product.
     */
    @Override
    public Single<List<Allergen>> getAllergens(Boolean refresh) {
        if (refresh || tableIsEmpty(allergenDao)) {
            return productApi.getAllergens()
                    .map(AllergensWrapper::map);
        } else {
            return Single.fromCallable(() -> allergenDao.loadAll());
        }
    }

    /**
     * Load countries from the server or local database
     *
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     */
    @Override
    public Single<List<Country>> getCountries(Boolean refresh) {
        if (refresh || tableIsEmpty(countryDao)) {
            return productApi.getCountries()
                    .map(CountriesWrapper::map);
        } else {
            return Single.fromCallable(() -> countryDao.loadAll());
        }
    }

    /**
     * Load categories from the server or local database
     *
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     */
    @Override
    public Single<List<Category>> getCategories(Boolean refresh) {
        if (refresh || tableIsEmpty(countryDao)) {
            return productApi.getCategories()
                    .map(CategoriesWrapper::map);
        } else {
            return Single.fromCallable(() -> categoryDao.loadAll());
        }
    }

    /**
     * Load allergens which user selected earlier (i.e user's allergens)
     */
    @Override
    public List<Allergen> getEnabledAllergens() {
        return allergenDao.queryBuilder().where(AllergenDao.Properties.Enabled.eq("true")).list();
    }

    /**
     * Load additives from the server or local database
     *
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     */
    @Override
    public Single<List<Additive>> getAdditives(Boolean refresh) {
        if (refresh || tableIsEmpty(additiveDao)) {
            return productApi.getAdditives()
                    .map(AdditivesWrapper::map);
        } else {
            return Single.fromCallable(() -> additiveDao.loadAll());
        }
    }

    /**
     * Labels saving to local database
     * <p>
     * Label and LabelName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveLabels(List<Label> labels) {
        for (Label label : labels) {
            labelDao.insertOrReplaceInTx(label);
            for (LabelName labelName : label.getNames()) {
                labelNameDao.insertOrReplace(labelName);
            }
        }
    }

    /**
     * Tags saving to local database
     */
    @Override
    public void saveTags(List<Tag> tags) {
        tagDao.insertOrReplaceInTx(tags);
    }


    /**
     * Allergens saving to local database
     * <p>
     * Allergen and AllergenName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveAllergens(List<Allergen> allergens) {
        for (Allergen allergen : allergens) {
            allergenDao.insertOrReplaceInTx(allergen);
            for (AllergenName allergenName : allergen.getNames()) {
                allergenNameDao.insertOrReplace(allergenName);
            }
        }
    }

    /**
     * Additives saving to local database
     * <p>
     * Additive and AdditiveName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveAdditives(List<Additive> additives) {
        for (Additive additive : additives) {
            additiveDao.insertOrReplaceInTx(additive);
            for (AdditiveName additiveName : additive.getNames()) {
                additiveNameDao.insertOrReplace(additiveName);
            }
        }
    }

    /**
     * Countries saving to local database
     * <p>
     * Country and CountryName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveCountries(List<Country> countries) {
        for (Country country : countries) {
            countryDao.insertOrReplaceInTx(country);
            for (CountryName countryName : country.getNames()) {
                countryNameDao.insertOrReplace(countryName);
            }
        }
    }

    /**
     * Categories saving to local database
     * <p>
     * Category and CategoryName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveCategories(List<Category> categories) {
        for (Category category : categories) {
            categoryDao.insertOrReplaceInTx(category);
            for (CategoryName categoryName : category.getNames()) {
                categoryNameDao.insertOrReplace(categoryName);
            }
        }
    }

    /**
     * Changes enabled field of allergen and updates it.
     *
     * @param isEnabled   depends on whether user selected or unselected the allergen
     * @param allergenTag is unique Id of allergen
     */
    @Override
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
     * @param labelTag     is a unique Id of label
     * @param languageCode is a 2-digit language code
     */
    @Override
    public LabelName getLabelByTagAndLanguageCode(String labelTag, String languageCode) {
        return labelNameDao.queryBuilder()
                .where(
                        LabelNameDao.Properties.LabelTag.eq(labelTag),
                        LabelNameDao.Properties.LanguageCode.eq(languageCode)
                ).unique();
    }

    /**
     * Loads translated label from the local database by unique tag of label and default language code
     *
     * @param labelTag is a unique Id of label
     */
    @Override
    public LabelName getLabelByTagAndDefaultLanguageCode(String labelTag) {
        return getLabelByTagAndLanguageCode(labelTag, DEFAULT_LANGUAGE);
    }

    /**
     * Loads translated additive from the local database by unique tag of additive and language code
     *
     * @param additiveTag  is a unique Id of additive
     * @param languageCode is a 2-digit language code
     */
    @Override
    public AdditiveName getAdditiveByTagAndLanguageCode(String additiveTag, String languageCode) {
        return additiveNameDao.queryBuilder()
                .where(
                        AdditiveNameDao.Properties.AdditiveTag.eq(additiveTag),
                        AdditiveNameDao.Properties.LanguageCode.eq(languageCode)
                ).unique();
    }

    /**
     * Loads translated additive from the local database by unique tag of additive and default language code
     *
     * @param additiveTag is a unique Id of additive
     */
    @Override
    public AdditiveName getAdditiveByTagAndDefaultLanguageCode(String additiveTag) {
        return getAdditiveByTagAndLanguageCode(additiveTag, DEFAULT_LANGUAGE);
    }

    /**
     * Loads translated country from the local database by unique tag of country and language code
     *
     * @param countryTag   is a unique Id of country
     * @param languageCode is a 2-digit language code
     */
    @Override
    public CountryName getCountryByTagAndLanguageCode(String countryTag, String languageCode) {
        return countryNameDao.queryBuilder()
                .where(
                        CountryNameDao.Properties.CountyTag.eq(countryTag),
                        CountryNameDao.Properties.LanguageCode.eq(languageCode)
                ).unique();
    }

    /**
     * Loads translated country from the local database by unique tag of country and default language code
     *
     * @param countryTag is a unique Id of country
     */
    @Override
    public CountryName getCountryByTagAndDefaultLanguageCode(String countryTag) {
        return getCountryByTagAndLanguageCode(countryTag, DEFAULT_LANGUAGE);
    }

    /**
     * Loads translated category from the local database by unique tag of category and language code
     *
     * @param categoryTag  is a unique Id of category
     * @param languageCode is a 2-digit language code
     */
    @Override
    public CategoryName getCategoryByTagAndLanguageCode(String categoryTag, String languageCode) {
        return categoryNameDao.queryBuilder()
                .where(
                        CategoryNameDao.Properties.CategoryTag.eq(categoryTag),
                        CategoryNameDao.Properties.LanguageCode.eq(languageCode)
                ).unique();
    }

    /**
     * Loads translated category from the local database by unique tag of category and default language code
     *
     * @param categoryTag is a unique Id of category
     */
    @Override
    public CategoryName getCategoryByTagAndDefaultLanguageCode(String categoryTag) {
        return getCategoryByTagAndLanguageCode(categoryTag, DEFAULT_LANGUAGE);
    }

    @Override
    public Single<List<CategoryName>> getAllCategoriesByLanguageCode(String languageCode) {
        return Single.fromCallable(() -> categoryNameDao.queryBuilder()
                .where(CategoryNameDao.Properties.LanguageCode.eq(languageCode))
                .orderAsc(CategoryNameDao.Properties.Name)
                .list());
    }

    @Override
    public Single<List<CategoryName>> getAllCategoriesByDefaultLanguageCode() {
        return getAllCategoriesByLanguageCode(DEFAULT_LANGUAGE);
    }

    /**
     * Loads translated and selected/unselected allergens.
     *
     * @param isEnabled    depends on whether allergen was selected or unselected by user
     * @param languageCode is a 2-digit language code
     */
    @Override
    public List<AllergenName> getAllergensByEnabledAndLanguageCode(Boolean isEnabled, String languageCode) {
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

        return null;
    }

    /**
     * Loads all translated allergens.
     *
     * @param languageCode is a 2-digit language code
     */
    @Override
    public List<AllergenName> getAllergensByLanguageCode(String languageCode) {
        return allergenNameDao.queryBuilder()
                .where(AllergenNameDao.Properties.LanguageCode.eq(languageCode))
                .list();
    }

    /**
     * Checks whether table is empty
     *
     * @param dao checks records count of any table
     */
    private Boolean tableIsEmpty(AbstractDao dao) {
        return dao.count() == 0;
    }

    /**
     * Checks whether table is not empty
     *
     * @param dao checks records count of any table
     */
    private Boolean tableIsNotEmpty(AbstractDao dao) {
        return dao.count() != 0;
    }

    /**
     * Checks whether table of additives is empty
     */
    @Override
    public Boolean additivesIsEmpty() {
        return tableIsEmpty(additiveDao);
    }
}
