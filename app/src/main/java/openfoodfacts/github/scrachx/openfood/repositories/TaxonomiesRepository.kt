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
package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.squareup.picasso.Picasso
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcode
import openfoodfacts.github.scrachx.openfood.models.entities.TaxonomyEntity
import openfoodfacts.github.scrachx.openfood.models.entities.additive.Additive
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.Allergen
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenDao
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTag
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfigDao
import openfoodfacts.github.scrachx.openfood.models.entities.brand.Brand
import openfoodfacts.github.scrachx.openfood.models.entities.category.Category
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.country.Country
import openfoodfacts.github.scrachx.openfood.models.entities.ingredient.Ingredient
import openfoodfacts.github.scrachx.openfood.models.entities.label.Label
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelName
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.states.States
import openfoodfacts.github.scrachx.openfood.models.entities.states.StatesName
import openfoodfacts.github.scrachx.openfood.models.entities.states.StatesNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.store.Store
import openfoodfacts.github.scrachx.openfood.models.entities.tag.Tag
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.services.AnalysisDataAPI
import openfoodfacts.github.scrachx.openfood.utils.getAppPreferences
import openfoodfacts.github.scrachx.openfood.utils.list
import openfoodfacts.github.scrachx.openfood.utils.unique
import org.greenrobot.greendao.query.WhereCondition.StringCondition
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This is a repository class which implements repository interface.
 *
 * @author Lobster
 * @since 03.03.18
 */
@Singleton
class TaxonomiesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val daoSession: DaoSession,
    private val analysisDataApi: AnalysisDataAPI,
    private val taxonomiesManager: TaxonomiesManager,
    private val picasso: Picasso,
) {

    /**
     * Load labels from the server or local database
     *
     * @return The list of Labels.
     */
    suspend fun reloadLabelsFromServer() = taxonomiesManager.getTaxonomyData(
        Taxonomy.Labels,
        true,
        daoSession.labelDao,
        this
    )

    suspend fun downloadLabels(lastModifiedDate: Long) =
        analysisDataApi.getLabels().map().also {
            saveLabels(it)
            updateLastDownloadDateInSettings(Taxonomy.Labels, lastModifiedDate)
        }

    /**
     * Load tags from the server or local database
     *
     * @return The list of Tags.
     */
    suspend fun reloadTagsFromServer() = taxonomiesManager.getTaxonomyData(
        Taxonomy.Tags,
        true,
        daoSession.tagDao,
        this
    )

    suspend fun downloadTags(lastModifiedDate: Long) =
        analysisDataApi.getTags().tags.also {
            saveTags(it)
            updateLastDownloadDateInSettings(Taxonomy.Tags, lastModifiedDate)
        }

    suspend fun reloadInvalidBarcodesFromServer() = taxonomiesManager.getTaxonomyData(
        Taxonomy.InvalidBarcodes,
        true,
        daoSession.invalidBarcodeDao,
        this
    )

    suspend fun downloadInvalidBarcodes(lastModifiedDate: Long) =
        analysisDataApi.getInvalidBarcodes().map { InvalidBarcode(it) }
            .also {
                saveInvalidBarcodes(it)
                updateLastDownloadDateInSettings(Taxonomy.InvalidBarcodes, lastModifiedDate)
            }

    /**
     * Load allergens from the server or local database
     *
     * @return The allergens in the product.
     */
    // FIXME: this returns 404
    suspend fun reloadAllergensFromServer() = getAllergens(true)

    suspend fun getAllergens(checkUpdate: Boolean = false) = taxonomiesManager.getTaxonomyData(
        Taxonomy.Allergens,
        checkUpdate,
        daoSession.allergenDao,
        this
    )

    suspend fun downloadAllergens(lastModifiedDate: Long) =
        analysisDataApi.getAllergens().map()
            .also {
                saveAllergens(it)
                updateLastDownloadDateInSettings(Taxonomy.Allergens, lastModifiedDate)
            }

    /**
     * Load countries from the server or local database
     *
     * @return The list of countries.
     */
    suspend fun reloadCountriesFromServer() = taxonomiesManager.getTaxonomyData(
        Taxonomy.Countries,
        true,
        daoSession.countryDao,
        this
    )

    suspend fun downloadCountries(lastModifiedDate: Long) =
        analysisDataApi.getCountries().map()
            .also {
                saveCountries(it)
                updateLastDownloadDateInSettings(Taxonomy.Countries, lastModifiedDate)
            }

    /**
     * Load categories from the server or local database
     *
     * @return The list of categories.
     */
    suspend fun reloadCategoriesFromServer() = taxonomiesManager.getTaxonomyData(
        Taxonomy.Categories,
        true,
        daoSession.categoryDao,
        this
    )

    suspend fun fetchCategories() = taxonomiesManager.getTaxonomyData(
        Taxonomy.Categories,
        false,
        daoSession.categoryDao,
        this
    )

    suspend fun downloadCategories(lastModifiedDate: Long) =
        analysisDataApi.getCategories().map()
            .also {
                saveCategories(it)
                updateLastDownloadDateInSettings(Taxonomy.Categories, lastModifiedDate)
            }

    /**
     * Load allergens which user selected earlier (i.e user's allergens)
     *
     * @return The list of allergens.
     */
    suspend fun getEnabledAllergens(): List<Allergen> = withContext(IO) {
        daoSession.allergenDao.queryBuilder()
            .where(AllergenDao.Properties.Enabled.eq("true"))
            .list()
    }

    /**
     * Load additives from the server or local database
     *
     * @return The list of additives.
     */
    suspend fun reloadAdditivesFromServer() = taxonomiesManager.getTaxonomyData(
        Taxonomy.Additives,
        true,
        daoSession.additiveDao,
        this
    )

    suspend fun downloadAdditives(lastModifiedDate: Long) =
        analysisDataApi.getAdditives().map()
            .also {
                saveAdditives(it)
                updateLastDownloadDateInSettings(Taxonomy.Additives, lastModifiedDate)
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
    suspend fun reloadIngredientsFromServer() = taxonomiesManager.getTaxonomyData(
        Taxonomy.Ingredients,
        true,
        daoSession.ingredientDao,
        this
    )

    suspend fun downloadIngredients(lastModifiedDate: Long) =
        analysisDataApi.getIngredients().map()
            .also {
                saveIngredients(it)
                updateLastDownloadDateInSettings(Taxonomy.Ingredients, lastModifiedDate)
            }

    /**
     * Load states from the server or local database
     *
     * @return The list of states.
     */
    suspend fun reloadStatesFromServer() = taxonomiesManager.getTaxonomyData(
        Taxonomy.ProductStates,
        true,
        daoSession.statesDao,
        this
    )

    suspend fun downloadStates(lastModifiedDate: Long) =
        analysisDataApi.getStates().map()
            .also {
                saveState(it)
                updateLastDownloadDateInSettings(Taxonomy.ProductStates, lastModifiedDate)
            }

    /**
     * Load stores from the server or local database
     *
     * @return The list of stores.
     */
    suspend fun reloadStoresFromServer(): List<Store> = taxonomiesManager.getTaxonomyData(
        Taxonomy.Stores,
        true,
        daoSession.storeDao,
        this
    )

    suspend fun downloadStores(lastModifiedDate: Long) =
        analysisDataApi.getStores().map()
            .also {
                saveStores(it)
                updateLastDownloadDateInSettings(Taxonomy.Stores, lastModifiedDate)
            }

    suspend fun reloadBrandsFromServer(): List<Brand> = taxonomiesManager.getTaxonomyData(
        Taxonomy.Brands,
        true,
        daoSession.brandDao,
        this
    )

    suspend fun downloadBrands(lastModifiedDate: Long) =
        analysisDataApi.getBrands().map()
            .also {
                saveBrands(it)
                updateLastDownloadDateInSettings(Taxonomy.Brands, lastModifiedDate)
            }


    /**
     * Labels saving to local database
     *
     * @param labels The list of labels to be saved.
     *
     *
     * Label and LabelName has One-To-Many relationship, therefore we need to save them separately.
     */
    private fun saveLabels(labels: List<Label>) {
        daoSession.database.beginTransaction()
        try {
            for (label in labels) {
                daoSession.labelDao.insertOrReplace(label)
                label.names.forEach { daoSession.labelNameDao.insertOrReplace(it) }
            }
            daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveLabels", e)
        } finally {
            daoSession.database.endTransaction()
        }
    }

    /**
     * Tags saving to local database
     *
     * @param tags The list of tags to be saved.
     */
    private fun saveTags(tags: List<Tag>) = daoSession.tagDao.insertOrReplaceInTx(tags)

    /**
     * Invalid Barcodess saving to local database. Will clear all previous invalid barcodes stored before.
     *
     * @param invalidBarcodes The list of invalidBarcodes to be saved.
     */
    private fun saveInvalidBarcodes(invalidBarcodes: List<InvalidBarcode>) {
        daoSession.invalidBarcodeDao.deleteAll()
        daoSession.invalidBarcodeDao.insertInTx(invalidBarcodes)
    }

    /**
     * Save a list of allergens to the local DB.
     *
     * Allergen and AllergenName has One-To-Many relationship,
     * therefore we need to save them separately.
     *
     * @param allergens The list of allergens to be saved.
     */
    fun saveAllergens(allergens: List<Allergen>) {
        daoSession.database.beginTransaction()
        try {

            for (allergen in allergens) {
                val oldAllergen = daoSession.allergenDao.unique {
                    where(AllergenDao.Properties.Tag.eq(allergen.tag))
                }

                // If the allergen is already in the database
                // keep the "enabled" field
                if (oldAllergen != null) {
                    allergen.enabled = oldAllergen.enabled
                }

                daoSession.allergenDao.insertOrReplace(allergen)
                allergen.names.forEach { daoSession.allergenNameDao.insertOrReplace(it) }
            }
            daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveAllergens", e)
        } finally {
            daoSession.database.endTransaction()
        }
    }

    /**
     * Additives saving to local database
     *
     * Additive and AdditiveName has One-To-Many relationship,
     * therefore we need to save them separately.
     *
     * @param additives The list of additives to be saved.
     */
    private fun saveAdditives(additives: List<Additive>) {
        daoSession.database.beginTransaction()
        try {
            for (additive in additives) {
                daoSession.additiveDao.insertOrReplace(additive)
                additive.names.forEach { daoSession.additiveNameDao.insertOrReplace(it) }
            }
            daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveAdditives", e)
        } finally {
            daoSession.database.endTransaction()
        }
    }

    /**
     * Countries saving to local database
     *
     * @param countries The list of countries to be saved.
     *
     *
     * Country and CountryName has One-To-Many relationship, therefore we need to save them separately.
     */
    private fun saveCountries(countries: List<Country>) {
        daoSession.database.beginTransaction()
        try {
            countries.forEach { country ->
                daoSession.countryDao.insertOrReplace(country)
                country.names.forEach { daoSession.countryNameDao.insertOrReplace(it) }
            }
            daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveCountries", e)
        } finally {
            daoSession.database.endTransaction()
        }
    }

    /**
     * Categories saving to local database
     *
     * @param categories The list of categories to be saved.
     *
     *
     * Category and CategoryName has One-To-Many relationship, therefore we need to save them separately.
     */
    private fun saveCategories(categories: List<Category>) {
        daoSession.database.beginTransaction()
        try {
            categories.forEach { category ->
                daoSession.categoryDao.insertOrReplace(category)
                category.names.forEach { name ->
                    daoSession.categoryNameDao.insertOrReplace(name)
                }
            }
            daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveCategories", e)
        } finally {
            daoSession.database.endTransaction()
        }
    }

    /**
     * Delete rows from Ingredient, IngredientName and IngredientsRelation
     * set the autoincrement to 0
     */
    fun deleteIngredientCascade() {
        daoSession.ingredientDao.deleteAll()
        daoSession.ingredientNameDao.deleteAll()
        daoSession.ingredientsRelationDao.deleteAll()
        this.daoSession.database.execSQL(
            """update sqlite_sequence set seq=0 where name in 
            |('${this.daoSession.ingredientDao.tablename}', 
            |'${this.daoSession.ingredientNameDao.tablename}', 
            |'${this.daoSession.ingredientsRelationDao.tablename}')""".trimMargin()
        )
    }

    /**
     * Ingredients saving to local database
     * Ingredient and IngredientName has One-To-Many relationship, therefore we need to save them separately.
     *
     * @param ingredients The list of ingredients to be saved.
     */
    // TODO to be improved by loading only if required and only in the user language
    private fun saveIngredients(ingredients: List<Ingredient>) {
        daoSession.database.beginTransaction()
        try {
            ingredients.forEach { ingredient ->
                daoSession.ingredientDao.insertOrReplace(ingredient)
                ingredient.names.forEach {
                    daoSession.ingredientNameDao.insertOrReplace(it)
                }
                ingredient.parents.forEach {
                    daoSession.ingredientsRelationDao.insertOrReplace(it)
                }
                ingredient.children.forEach {
                    daoSession.ingredientsRelationDao.insertOrReplace(it)
                }
            }
            daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveIngredients", e)
        } finally {
            daoSession.database.endTransaction()
        }
    }

    /**
     * States saving to local database
     *
     * @param states The list of states to be saved.
     *
     *
     * states and statesName has One-To-Many relationship, therefore we need to save them separately.
     */
    private fun saveState(states: List<States>) {
        daoSession.database.beginTransaction()
        try {
            states.forEach { state ->
                daoSession.statesDao.insertOrReplace(state)
                state.names.forEach {
                    daoSession.statesNameDao.insertOrReplace(it)
                }
            }
            daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveStates", e)
        } finally {
            daoSession.database.endTransaction()
        }
    }

    /**
     * Stores saving to local database
     *
     * @param stores The list of stores to be saved.
     *
     *
     * store and storeName has One-To-Many relationship, therefore we need to save them separately.
     */
    private fun saveStores(stores: List<Store>) {
        daoSession.database.beginTransaction()
        try {
            stores.forEach { store ->
                daoSession.storeDao.insertOrReplace(store)
                store.names.forEach {
                    daoSession.storeNameDao.insertOrReplace(it)
                }
            }
            daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveStores", e)
        } finally {
            daoSession.database.endTransaction()
        }
    }

    /**
     * Save Brands to local Database
     *
     * @param brands The list of brands to be stored
     *
     */
    private fun saveBrands(brands: List<Brand>) {
        daoSession.database.beginTransaction()
        try {
            for (brand in brands) {
                daoSession.brandDao.insertOrReplace(brand)
                brand.names.forEach { daoSession.brandNameDao.insertOrReplace(it) }
            }
            daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveBrands", e)
        } finally {
            daoSession.database.endTransaction()
        }
    }

    /**
     * Changes enabled field of allergen and updates it.
     *
     * @param isEnabled depends on whether user selected or unselected the allergen
     * @param allergenTag is unique Id of allergen
     */
    suspend fun setAllergenEnabled(allergenTag: String, isEnabled: Boolean) = withContext(IO) {
        daoSession.allergenDao.unique {
            where(AllergenDao.Properties.Tag.eq(allergenTag))
        }?.apply { enabled = isEnabled }.also { daoSession.allergenDao.update(it) }
    }

    /**
     * Loads translated label from the local database by unique tag of label and language code
     *
     * @param labelTag is a unique Id of label
     * @param languageCode is a 2-digit language code
     * @return The translated label
     */
    suspend fun getLabel(
        labelTag: String?,
        languageCode: String = ApiFields.Defaults.DEFAULT_LANGUAGE,
    ) = withContext(IO) {
        daoSession.labelNameDao.unique {
            where(
                LabelNameDao.Properties.LabelTag.eq(labelTag),
                LabelNameDao.Properties.LanguageCode.eq(languageCode)
            )
        } ?: LabelName()
    }

    /**
     * Loads translated additive from the local database by unique tag of additive and language code
     *
     * @param additiveTag is a unique Id of additive
     * @param languageCode is a 2-digit language code
     * @return The translated additive name
     */
    suspend fun getAdditive(
        additiveTag: String?,
        languageCode: String = ApiFields.Defaults.DEFAULT_LANGUAGE,
    ) = withContext(IO) {
        daoSession.additiveNameDao.unique {
            where(
                AdditiveNameDao.Properties.AdditiveTag.eq(additiveTag),
                AdditiveNameDao.Properties.LanguageCode.eq(languageCode)
            )
        } ?: AdditiveName()
    }

    suspend fun getCountries() = taxonomiesManager.getTaxonomyData(
        Taxonomy.Countries,
        false,
        daoSession.countryDao,
        this
    )

    suspend fun getCountry(cc2: String): Country? =
        getCountries().firstOrNull { it.cc2.equals(cc2, ignoreCase = true) }

    /**
     * Loads translated category from the local database by unique tag of category and language code
     *
     * @param categoryTag is a unique Id of category
     * @param languageCode is a 2-digit language code
     * @return The translated category name
     */
    suspend fun getCategory(
        categoryTag: String,
        languageCode: String = ApiFields.Defaults.DEFAULT_LANGUAGE,
    ) = withContext(IO) {
        daoSession.categoryNameDao.unique {
            where(
                CategoryNameDao.Properties.CategoryTag.eq(categoryTag),
                CategoryNameDao.Properties.LanguageCode.eq(languageCode)
            )
        } ?: CategoryName().apply {
            this.name = categoryTag
            this.categoryTag = categoryTag
            this.isWikiDataIdPresent = false
        }
    }


    /**
     * Loads list of translated category names from the local database by language code
     *
     * @param languageCode is a 2-digit language code
     * @return The translated list of category name
     */
    suspend fun getCategories(languageCode: String = ApiFields.Defaults.DEFAULT_LANGUAGE) = withContext(IO) {
        daoSession.categoryNameDao.list {
            where(CategoryNameDao.Properties.LanguageCode.eq(languageCode))
            orderAsc(CategoryNameDao.Properties.Name)
        }
    }


    /**
     * Loads translated and selected/unselected allergens.
     *
     * @param isEnabled depends on whether allergen was selected or unselected by user
     * @param lc is a 2-digit language code
     * @return The list of allergen names
     */
    suspend fun getAllergens(isEnabled: Boolean, lc: String) = withContext(IO) {
        daoSession.allergenDao.list {
            where(AllergenDao.Properties.Enabled.eq(isEnabled))
        }.mapNotNull {
            daoSession.allergenNameDao.unique {
                where(
                    AllergenNameDao.Properties.AllergenTag.eq(it.tag),
                    AllergenNameDao.Properties.LanguageCode.eq(lc)
                )
            }
        }
    }

    /**
     * Loads all translated allergens.
     *
     * @param languageCode is a 2-digit language code
     * @return The list of translated allergen names
     */
    suspend fun getAllergens(languageCode: String?) = withContext(IO) {
        daoSession.allergenNameDao.list {
            where(AllergenNameDao.Properties.LanguageCode.eq(languageCode))
        }
    }

    /**
     * Loads translated allergen from the local database by unique tag of allergen and language code
     *
     * @param allergenTag is a unique Id of allergen
     * @param languageCode is a 2-digit language code
     * @return The translated allergen name
     */
    suspend fun getAllergenName(
        allergenTag: String?,
        languageCode: String = ApiFields.Defaults.DEFAULT_LANGUAGE,
    ) = withContext(IO) {
        daoSession.allergenNameDao.unique {
            where(
                AllergenNameDao.Properties.AllergenTag.eq(allergenTag),
                AllergenNameDao.Properties.LanguageCode.eq(languageCode)
            )
        } ?: AllergenName().apply {
            this.name = allergenTag
            this.allergenTag = allergenTag
            this.isWikiDataIdPresent = false
        }
    }

    /**
     * Loads translated states from the local database by unique tag of states and language code
     *
     * @param statesTag is a unique Id of states
     * @param languageCode is a 2-digit language code
     * @return The translated states name
     */
    suspend fun getStatesName(statesTag: String, languageCode: String?): StatesName {
        return withContext(IO) {
            daoSession.statesNameDao.unique {
                where(
                    StatesNameDao.Properties.StatesTag.eq(statesTag),
                    StatesNameDao.Properties.LanguageCode.eq(languageCode)
                )
            } ?: StatesName(
                statesTag,
                statesTag.split(":").component1(),
                statesTag.split(":").component2()
            )
        }
    }


    /**
     * Load analysis tags from the server or local database
     *
     * @return The analysis tags in the product.
     */
    suspend fun reloadAnalysisTags() =
        taxonomiesManager.getTaxonomyData(
            Taxonomy.AnalysisTags,
            true,
            daoSession.analysisTagDao,
            this
        )

    suspend fun downloadAnalysisTags(lastModifiedDate: Long) =
        analysisDataApi.getAnalysisTags().map()
            .also {
                saveAnalysisTags(it)
                updateLastDownloadDateInSettings(Taxonomy.AnalysisTags, lastModifiedDate)
            }

    /**
     * AnalysisTags saving to local database
     *
     * @param tags The list of analysis tags to be saved.
     *
     *
     * AnalysisTag and AnalysisTagName has One-To-Many relationship, therefore we need to save them separately.
     */
    private fun saveAnalysisTags(tags: List<AnalysisTag>) {
        daoSession.database.beginTransaction()
        try {
            tags.forEach { tag ->
                daoSession.analysisTagDao.insertOrReplace(tag)
                tag.names.forEach {
                    daoSession.analysisTagNameDao.insertOrReplace(it)
                }
            }
            daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveAnalysisTags", e)
        } finally {
            daoSession.database.endTransaction()
        }
    }

    suspend fun reloadAnalysisTagConfigs() = taxonomiesManager.getTaxonomyData(
        Taxonomy.AnalysisTagConfigs,
        true,
        daoSession.analysisTagConfigDao,
        this
    )

    suspend fun downloadAnalysisTagConfigs(lastModifiedDate: Long): List<AnalysisTagConfig> {
        val configs = analysisDataApi.getAnalysisTagConfigs().map()
        saveAnalysisTagConfigs(configs)
        updateLastDownloadDateInSettings(Taxonomy.AnalysisTagConfigs, lastModifiedDate)
        return configs
    }

    private fun saveAnalysisTagConfigs(analysisTagConfigs: List<AnalysisTagConfig>) {
        daoSession.database.beginTransaction()
        try {
            for (config in analysisTagConfigs) {
                picasso.load(config.iconUrl).fetch()
                daoSession.analysisTagConfigDao.insertOrReplace(config)
            }
            daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveAnalysisTagConfigs", e)
        } finally {
            daoSession.database.endTransaction()
        }
    }

    private fun updateAnalysisTagConfig(analysisTagConfig: AnalysisTagConfig, languageCode: String) {
        var analysisTagName = daoSession.analysisTagNameDao.unique {
            where(
                AnalysisTagNameDao.Properties.AnalysisTag.eq(analysisTagConfig.analysisTag),
                AnalysisTagNameDao.Properties.LanguageCode.eq(languageCode)
            )
        }
        if (analysisTagName == null) {
            analysisTagName = daoSession.analysisTagNameDao.unique {
                where(
                    AnalysisTagNameDao.Properties.AnalysisTag.eq(analysisTagConfig.analysisTag),
                    AnalysisTagNameDao.Properties.LanguageCode.eq(ApiFields.Defaults.DEFAULT_LANGUAGE)
                )
            }
        }
        analysisTagConfig.name = analysisTagName
        val type = "en:${analysisTagConfig.type}"
        var analysisTagTypeName = daoSession.analysisTagNameDao.unique {
            where(
                AnalysisTagNameDao.Properties.AnalysisTag.eq(type),
                AnalysisTagNameDao.Properties.LanguageCode.eq(languageCode)
            )
        }
        if (analysisTagTypeName == null) {
            analysisTagTypeName = daoSession.analysisTagNameDao.unique {
                where(
                    AnalysisTagNameDao.Properties.AnalysisTag.eq(type),
                    AnalysisTagNameDao.Properties.LanguageCode.eq(ApiFields.Defaults.DEFAULT_LANGUAGE)
                )
            }
        }
        analysisTagConfig.typeName =
            if (analysisTagTypeName != null) analysisTagTypeName.name else analysisTagConfig.type
    }

    /**
     * @param analysisTag
     * @param languageCode
     */
    suspend fun getAnalysisTagConfig(analysisTag: String?, languageCode: String): AnalysisTagConfig? = withContext(IO) {
        daoSession.analysisTagConfigDao.unique {
            where(AnalysisTagConfigDao.Properties.AnalysisTag.eq(analysisTag))
        }?.also { updateAnalysisTagConfig(it, languageCode) }
    }

    suspend fun getUnknownAnalysisTagConfigs(languageCode: String) = withContext(IO) {
        daoSession.analysisTagConfigDao.list {
            where(StringCondition("""${AnalysisTagConfigDao.Properties.AnalysisTag.columnName} LIKE "%unknown%""""))
        }.onEach { updateAnalysisTagConfig(it, languageCode) }
    }


    /**
     * This function set lastDownloadtaxonomy setting
     *
     * @param taxonomy Name of the taxonomy (allergens, additives, categories, countries, ingredients, labels, tags)
     * @param lastDownload Date of last update on Long format
     */
    private fun <T : TaxonomyEntity> updateLastDownloadDateInSettings(
        taxonomy: Taxonomy<T>, lastDownload: Long,
    ) {
        context.getAppPreferences().edit {
            putLong(taxonomy.lastDownloadTimeStampPreferenceId, lastDownload)
        }
        logcat(LogPriority.INFO) { "Set lastDownload of $taxonomy to $lastDownload" }
    }


    companion object {
        private val LOG_TAG = TaxonomiesRepository::class.simpleName
    }
}
