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

import android.util.Log
import androidx.core.content.edit
import com.squareup.picasso.Picasso
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.Credentials
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.additive.*
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.*
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTag
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfigDao
import openfoodfacts.github.scrachx.openfood.models.entities.category.*
import openfoodfacts.github.scrachx.openfood.models.entities.country.Country
import openfoodfacts.github.scrachx.openfood.models.entities.ingredient.*
import openfoodfacts.github.scrachx.openfood.models.entities.label.*
import openfoodfacts.github.scrachx.openfood.models.entities.states.States
import openfoodfacts.github.scrachx.openfood.models.entities.states.StatesName
import openfoodfacts.github.scrachx.openfood.models.entities.states.StatesNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.store.Store
import openfoodfacts.github.scrachx.openfood.models.entities.tag.Tag
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.analysisDataApi
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.robotoffApi
import openfoodfacts.github.scrachx.openfood.repositories.TaxonomiesManager.getTaxonomyData
import openfoodfacts.github.scrachx.openfood.utils.getLoginPreferences
import org.greenrobot.greendao.query.WhereCondition.StringCondition

/**
 * This is a repository class which implements repository interface.
 *
 * @author Lobster
 * @since 03.03.18
 */
object ProductRepository {

    /**
     * Load labels from the server or local database
     *
     * @return The list of Labels.
     */
    fun reloadLabelsFromServer() =
            getTaxonomyData(Taxonomy.LABEL, this, true, OFFApplication.daoSession.labelDao)

    fun loadLabels(lastModifiedDate: Long) = analysisDataApi.getLabels()
            .map { it.map() }
            .doOnSuccess { labels ->
                saveLabels(labels)
                updateLastDownloadDateInSettings(Taxonomy.LABEL, lastModifiedDate)
            }

    /**
     * Load tags from the server or local database
     *
     * @return The list of Tags.
     */
    fun reloadTagsFromServer() =
            getTaxonomyData(Taxonomy.TAGS, this, true, OFFApplication.daoSession.tagDao)

    fun loadTags(lastModifiedDate: Long) = analysisDataApi.getTags()
            .map { it.tags }
            .doOnSuccess {
                saveTags(it)
                updateLastDownloadDateInSettings(Taxonomy.TAGS, lastModifiedDate)
            }

    fun reloadInvalidBarcodesFromServer() =
            getTaxonomyData(Taxonomy.INVALID_BARCODES, this, true, OFFApplication.daoSession.invalidBarcodeDao)

    fun loadInvalidBarcodes(lastModifiedDate: Long) = analysisDataApi.getInvalidBarcodes()
            .map { strings -> strings.map { InvalidBarcode(it) } }
            .doOnSuccess {
                saveInvalidBarcodes(it)
                updateLastDownloadDateInSettings(Taxonomy.INVALID_BARCODES, lastModifiedDate)
            }

    /**
     * Load allergens from the server or local database
     *
     * @return The allergens in the product.
     */
    fun reloadAllergensFromServer(): Single<List<Allergen>> =
            // FIXME: this returns 404
            getTaxonomyData(Taxonomy.ALLERGEN, this, true, OFFApplication.daoSession.allergenDao)

    fun getAllergens(): Single<List<Allergen>> =
            getTaxonomyData(Taxonomy.ALLERGEN, this, false, OFFApplication.daoSession.allergenDao)

    fun loadAllergens(lastModifiedDate: Long): Single<List<Allergen>> = analysisDataApi.getAllergens()
            .map { it.map() }
            .doOnSuccess {
                saveAllergens(it)
                updateLastDownloadDateInSettings(Taxonomy.ALLERGEN, lastModifiedDate)
            }

    /**
     * Load countries from the server or local database
     *
     * @return The list of countries.
     */
    fun reloadCountriesFromServer(): Single<List<Country>> =
            getTaxonomyData(Taxonomy.COUNTRY, this, true, OFFApplication.daoSession.countryDao)

    fun loadCountries(lastModifiedDate: Long): Single<List<Country>> = analysisDataApi.getCountries()
            .map { it.map() }
            .doOnSuccess {
                saveCountries(it)
                updateLastDownloadDateInSettings(Taxonomy.COUNTRY, lastModifiedDate)
            }

    /**
     * Load categories from the server or local database
     *
     * @return The list of categories.
     */
    fun reloadCategoriesFromServer() =
            getTaxonomyData(Taxonomy.CATEGORY, this, true, OFFApplication.daoSession.categoryDao)

    fun getCategories() = getTaxonomyData(Taxonomy.CATEGORY, this, false, OFFApplication.daoSession.categoryDao)

    fun loadCategories(lastModifiedDate: Long) = analysisDataApi.getCategories()
            .map { it.map() }
            .doOnSuccess {
                saveCategories(it)
                updateLastDownloadDateInSettings(Taxonomy.CATEGORY, lastModifiedDate)
            }

    /**
     * Load allergens which user selected earlier (i.e user's allergens)
     *
     * @return The list of allergens.
     */
    fun getEnabledAllergens(): List<Allergen> =
            OFFApplication.daoSession.allergenDao.queryBuilder()
                    .where(AllergenDao.Properties.Enabled.eq("true"))
                    .list()

    /**
     * Load additives from the server or local database
     *
     * @return The list of additives.
     */
    fun reloadAdditivesFromServer() =
            getTaxonomyData(Taxonomy.ADDITIVE, this, true, OFFApplication.daoSession.additiveDao)

    fun loadAdditives(lastModifiedDate: Long) = analysisDataApi.getAdditives()
            .map { it.map() }
            .doOnSuccess {
                saveAdditives(it)
                updateLastDownloadDateInSettings(Taxonomy.ADDITIVE, lastModifiedDate)
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
    fun reloadIngredientsFromServer(): Single<List<Ingredient>> =
            getTaxonomyData(Taxonomy.INGREDIENT, this, true, OFFApplication.daoSession.ingredientDao)

    fun loadIngredients(lastModifiedDate: Long): Single<List<Ingredient>> = analysisDataApi.getIngredients()
            .map { it.map() }
            .doOnSuccess {
                saveIngredients(it)
                updateLastDownloadDateInSettings(Taxonomy.INGREDIENT, lastModifiedDate)
            }

    /**
     * Load states from the server or local database
     *
     * @return The list of states.
     */
    fun reloadStatesFromServer(): Single<List<States>> =
            getTaxonomyData(Taxonomy.STATES, this, true, OFFApplication.daoSession.statesDao)

    fun loadStates(lastModifiedDate: Long): Single<List<States>> = analysisDataApi.getStates()
            .map { it.map() }
            .doOnSuccess{
                saveState(it)
                updateLastDownloadDateInSettings(Taxonomy.STATES,lastModifiedDate)
            }

    /**
     * Load stores from the server or local database
     *
     * @return The list of stores.
     */
    fun reloadStoresFromServer(): Single<List<Store>> =
            getTaxonomyData(Taxonomy.STORES, this, true, OFFApplication.daoSession.storeDao)

    fun loadStores(lastModifiedDate: Long): Single<List<Store>> = analysisDataApi.getStores()
            .map { it.map() }
            .doOnSuccess{
                saveStores(it)
                updateLastDownloadDateInSettings(Taxonomy.STORES,lastModifiedDate)
            }

    /**
     * This function set lastDownloadtaxonomy setting
     *
     * @param taxonomy Name of the taxonomy (allergens, additives, categories, countries, ingredients, labels, tags)
     * @param lastDownload Date of last update on Long format
     */
    private fun updateLastDownloadDateInSettings(taxonomy: Taxonomy, lastDownload: Long) {
        OFFApplication.instance.getSharedPreferences("prefs", 0)
                .edit { putLong(taxonomy.lastDownloadTimeStampPreferenceId, lastDownload) }
        Log.i(LOG_TAG, "Set lastDownload of $taxonomy to $lastDownload")
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
        OFFApplication.daoSession.database.beginTransaction()
        try {
            labels.forEach { label ->
                OFFApplication.daoSession.labelDao.insertOrReplace(label)
                label.names.forEach { OFFApplication.daoSession.labelNameDao.insertOrReplace(it) }
            }
            OFFApplication.daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveLabels", e)
        } finally {
            OFFApplication.daoSession.database.endTransaction()
        }
    }

    /**
     * Tags saving to local database
     *
     * @param tags The list of tags to be saved.
     */
    private fun saveTags(tags: List<Tag>) = OFFApplication.daoSession.tagDao.insertOrReplaceInTx(tags)

    /**
     * Invalid Barcodess saving to local database. Will clear all previous invalid barcodes stored before.
     *
     * @param invalidBarcodes The list of invalidBarcodes to be saved.
     */
    private fun saveInvalidBarcodes(invalidBarcodes: List<InvalidBarcode>) {
        OFFApplication.daoSession.invalidBarcodeDao.deleteAll()
        OFFApplication.daoSession.invalidBarcodeDao.insertInTx(invalidBarcodes)
    }

    /**
     * Allergens saving to local database
     *
     * @param allergens The list of allergens to be saved.
     *
     *
     * Allergen and AllergenName has One-To-Many relationship, therefore we need to save them separately.
     */
    fun saveAllergens(allergens: List<Allergen>) {
        OFFApplication.daoSession.database.beginTransaction()
        try {
            allergens.forEach { allergen ->
                OFFApplication.daoSession.allergenDao.insertOrReplace(allergen)
                allergen.names.forEach { OFFApplication.daoSession.allergenNameDao.insertOrReplace(it) }
            }
            OFFApplication.daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveAllergens", e)
        } finally {
            OFFApplication.daoSession.database.endTransaction()
        }
    }

    /**
     * Additives saving to local database
     *
     * @param additives The list of additives to be saved.
     *
     *
     * Additive and AdditiveName has One-To-Many relationship, therefore we need to save them separately.
     */
    private fun saveAdditives(additives: List<Additive>) {
        OFFApplication.daoSession.database.beginTransaction()
        try {
            additives.forEach { additive ->
                OFFApplication.daoSession.additiveDao.insertOrReplace(additive)
                additive.names.forEach { OFFApplication.daoSession.additiveNameDao.insertOrReplace(it) }
            }
            OFFApplication.daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveAdditives", e)
        } finally {
            OFFApplication.daoSession.database.endTransaction()
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
        OFFApplication.daoSession.database.beginTransaction()
        try {
            countries.forEach { country ->
                OFFApplication.daoSession.countryDao.insertOrReplace(country)
                country.names.forEach { OFFApplication.daoSession.countryNameDao.insertOrReplace(it) }
            }
            OFFApplication.daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveCountries", e)
        } finally {
            OFFApplication.daoSession.database.endTransaction()
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
        OFFApplication.daoSession.database.beginTransaction()
        try {
            categories.forEach { category ->
                OFFApplication.daoSession.categoryDao.insertOrReplace(category)
                category.names.forEach { name ->
                    OFFApplication.daoSession.categoryNameDao.insertOrReplace(name)
                }
            }
            OFFApplication.daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveCategories", e)
        } finally {
            OFFApplication.daoSession.database.endTransaction()
        }
    }

    /**
     * Delete rows from Ingredient, IngredientName and IngredientsRelation
     * set the autoincrement to 0
     */
    fun deleteIngredientCascade() {
        OFFApplication.daoSession.ingredientDao.deleteAll()
        OFFApplication.daoSession.ingredientNameDao.deleteAll()
        OFFApplication.daoSession.ingredientsRelationDao.deleteAll()
        val daoSession = OFFApplication.daoSession
        daoSession.database.execSQL("""update sqlite_sequence set seq=0 where name in 
            |('${OFFApplication.daoSession.ingredientDao.tablename}', 
            |'${OFFApplication.daoSession.ingredientNameDao.tablename}', 
            |'${OFFApplication.daoSession.ingredientsRelationDao.tablename}')""".trimMargin())
    }

    /**
     * TODO to be improved by loading only if required and only in the user language
     * Ingredients saving to local database
     *
     * @param ingredients The list of ingredients to be saved.
     *
     *
     * Ingredient and IngredientName has One-To-Many relationship, therefore we need to save them separately.
     */
    private fun saveIngredients(ingredients: List<Ingredient>) {
        OFFApplication.daoSession.database.beginTransaction()
        try {
            ingredients.forEach { ingredient ->
                OFFApplication.daoSession.ingredientDao.insertOrReplace(ingredient)
                ingredient.names.forEach {
                    OFFApplication.daoSession.ingredientNameDao.insertOrReplace(it)
                }
                ingredient.parents.forEach {
                    OFFApplication.daoSession.ingredientsRelationDao.insertOrReplace(it)
                }
                ingredient.children.forEach {
                    OFFApplication.daoSession.ingredientsRelationDao.insertOrReplace(it)
                }
            }
            OFFApplication.daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveIngredients", e)
        } finally {
            OFFApplication.daoSession.database.endTransaction()
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
        OFFApplication.daoSession.database.beginTransaction()
        try {
            states.forEach { state ->
                OFFApplication.daoSession.statesDao.insertOrReplace(state)
                state.names.forEach {
                    OFFApplication.daoSession.statesNameDao.insertOrReplace(it)
                }
            }
            OFFApplication.daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveStates", e)
        } finally {
            OFFApplication.daoSession.database.endTransaction()
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
        OFFApplication.daoSession.database.beginTransaction()
        try {
            stores.forEach { store ->
                OFFApplication.daoSession.storeDao.insertOrReplace(store)
                store.names.forEach {
                    OFFApplication.daoSession.storeNameDao.insertOrReplace(it)
                }
            }
            OFFApplication.daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveStores", e)
        } finally {
            OFFApplication.daoSession.database.endTransaction()
        }
    }

    /**
     * Ingredient saving to local database
     *
     * @param ingredient The ingredient to be saved.
     */
    fun saveIngredient(ingredient: Ingredient) = saveIngredients(listOf(ingredient))

    /**
     * Changes enabled field of allergen and updates it.
     *
     * @param isEnabled depends on whether user selected or unselected the allergen
     * @param allergenTag is unique Id of allergen
     */
    fun setAllergenEnabled(allergenTag: String, isEnabled: Boolean) {
        val allergenDao = OFFApplication.daoSession.allergenDao
        allergenDao.queryBuilder()
                .where(AllergenDao.Properties.Tag.eq(allergenTag))
                .unique()
                ?.let {
                    it.enabled = isEnabled
                    allergenDao.update(it)
                }
    }

    /**
     * Loads translated label from the local database by unique tag of label and language code
     *
     * @param labelTag is a unique Id of label
     * @param languageCode is a 2-digit language code
     * @return The translated label
     */
    fun getLabelByTagAndLanguageCode(labelTag: String?, languageCode: String?): Single<LabelName> {
        return Single.fromCallable {
            OFFApplication.daoSession.labelNameDao.queryBuilder()
                    .where(
                            LabelNameDao.Properties.LabelTag.eq(labelTag),
                            LabelNameDao.Properties.LanguageCode.eq(languageCode)
                    ).unique()
                    ?: LabelName()
        }
    }

    /**
     * Loads translated label from the local database by unique tag of label and default language code
     *
     * @param labelTag is a unique Id of label
     * @return The translated label
     */
    fun getLabelByTagAndDefaultLanguageCode(labelTag: String?) =
            getLabelByTagAndLanguageCode(labelTag, ApiFields.Defaults.DEFAULT_LANGUAGE)

    /**
     * Loads translated additive from the local database by unique tag of additive and language code
     *
     * @param additiveTag is a unique Id of additive
     * @param languageCode is a 2-digit language code
     * @return The translated additive name
     */
    fun getAdditiveByTagAndLanguageCode(additiveTag: String?, languageCode: String?) = Single.fromCallable {
        OFFApplication.daoSession.additiveNameDao.queryBuilder()
                .where(
                        AdditiveNameDao.Properties.AdditiveTag.eq(additiveTag),
                        AdditiveNameDao.Properties.LanguageCode.eq(languageCode)
                ).unique()
                ?: AdditiveName()
    }

    /**
     * Loads translated additive from the local database by unique tag of additive and default language code
     *
     * @param additiveTag is a unique Id of additive
     * @return The translated additive tag
     */
    fun getAdditiveByTagAndDefaultLanguageCode(additiveTag: String?) =
            getAdditiveByTagAndLanguageCode(additiveTag, ApiFields.Defaults.DEFAULT_LANGUAGE)

    fun getCountries() =
            getTaxonomyData(Taxonomy.COUNTRY, this, false, OFFApplication.daoSession.countryDao)

    fun getCountryByCC2OrWorld(cc2: String?) = getCountries().flatMapMaybe { countries ->
        countries.asSequence()
                .filter { it.cc2.equals(cc2, ignoreCase = true) }
                .firstOrNull()
                .let { if (it == null) Maybe.empty() else Maybe.just(it) }
    }

    /**
     * Loads translated category from the local database by unique tag of category and language code
     *
     * @param categoryTag is a unique Id of category
     * @param languageCode is a 2-digit language code
     * @return The translated category name
     */
    fun getCategoryByTagAndLanguageCode(
            categoryTag: String?,
            languageCode: String = ApiFields.Defaults.DEFAULT_LANGUAGE
    ) = Single.fromCallable {
        OFFApplication.daoSession.categoryNameDao.queryBuilder().where(
                CategoryNameDao.Properties.CategoryTag.eq(categoryTag),
                CategoryNameDao.Properties.LanguageCode.eq(languageCode)
        ).unique() ?: CategoryName().apply {
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
    fun getAllCategoriesByLanguageCode(languageCode: String?) = Single.fromCallable {
        OFFApplication.daoSession.categoryNameDao.queryBuilder()
                .where(CategoryNameDao.Properties.LanguageCode.eq(languageCode))
                .orderAsc(CategoryNameDao.Properties.Name)
                .list()
    }

    /**
     * Loads list of category names from the local database by default language code
     *
     * @return The list of category name
     */
    fun getAllCategoriesByDefaultLanguageCode() = getAllCategoriesByLanguageCode(ApiFields.Defaults.DEFAULT_LANGUAGE)

    /**
     * Loads translated and selected/unselected allergens.
     *
     * @param isEnabled depends on whether allergen was selected or unselected by user
     * @param languageCode is a 2-digit language code
     * @return The list of allergen names
     */
    fun getAllergensByEnabledAndLanguageCode(isEnabled: Boolean?, languageCode: String?) = Single.fromCallable {
        val allergens = OFFApplication.daoSession.allergenDao.queryBuilder().where(AllergenDao.Properties.Enabled.eq(isEnabled)).list()
                ?: return@fromCallable emptyList()
        allergens.mapNotNull {
            OFFApplication.daoSession.allergenNameDao.queryBuilder().where(
                    AllergenNameDao.Properties.AllergenTag.eq(it.tag),
                    AllergenNameDao.Properties.LanguageCode.eq(languageCode)
            ).unique()
        }
    }.subscribeOn(Schedulers.io())

    /**
     * Loads all translated allergens.
     *
     * @param languageCode is a 2-digit language code
     * @return The list of translated allergen names
     */
    fun getAllergensByLanguageCode(languageCode: String?) = Single.fromCallable {
        OFFApplication.daoSession.allergenNameDao.queryBuilder()
                .where(AllergenNameDao.Properties.LanguageCode.eq(languageCode))
                .list()
    }

    /**
     * Loads translated allergen from the local database by unique tag of allergen and language code
     *
     * @param allergenTag is a unique Id of allergen
     * @param languageCode is a 2-digit language code
     * @return The translated allergen name
     */
    fun getAllergenByTagAndLanguageCode(allergenTag: String?, languageCode: String?) = Single.fromCallable {
        OFFApplication.daoSession.allergenNameDao.queryBuilder()
                .where(AllergenNameDao.Properties.AllergenTag.eq(allergenTag),
                        AllergenNameDao.Properties.LanguageCode.eq(languageCode))
                .unique()
                ?: AllergenName().apply {
                    this.name = allergenTag
                    this.allergenTag = allergenTag
                    this.isWikiDataIdPresent = false
                }
    }.subscribeOn(Schedulers.io())

    /**
     * Loads translated allergen from the local database by unique tag of allergen and default language code
     *
     * @param allergenTag is a unique Id of allergen
     * @return The translated allergen name
     */
    fun getAllergenByTagAndDefaultLanguageCode(allergenTag: String?) =
            getAllergenByTagAndLanguageCode(allergenTag, ApiFields.Defaults.DEFAULT_LANGUAGE)

    /**
     * Loads translated states from the local database by unique tag of states and language code
     *
     * @param statesTag is a unique Id of states
     * @param languageCode is a 2-digit language code
     * @return The translated states name
     */
    fun getStatesByTagAndLanguageCode(statesTag: String, languageCode: String?) = Single.fromCallable {
        OFFApplication.daoSession.statesNameDao.queryBuilder()
                .where(StatesNameDao.Properties.StatesTag.eq(statesTag),
                        StatesNameDao.Properties.LanguageCode.eq(languageCode))
                .unique()
                ?: StatesName(statesTag, statesTag.split(":").component1(), statesTag.split(":").component2())
    }.subscribeOn(Schedulers.io())


    /**
     * Loads Robotoff question from the local database by code and lang of question.
     *
     * @param code for the question
     * @param lang is language of the question
     * @return The single question
     */
    fun getProductQuestion(code: String, lang: String) = robotoffApi.getProductQuestions(code, lang, 1)
            .map { it.questions }
            .flatMapMaybe { if (it.isEmpty()) Maybe.empty() else Maybe.just(it[0]) }

    /**
     * Annotate the Robotoff insight response using insight id and annotation
     *
     * @param insightId is the unique id for the insight
     * @param annotation is the annotation to be used
     * @return The annotated insight response
     */
    fun annotateInsight(insightId: String, annotation: AnnotationAnswer): Single<AnnotationResponse> {
        // if the user is logged in, send the auth, otherwise make it anonymous
        val user = OFFApplication.instance.getLoginPreferences().getString("user", "")?.trim { it <= ' ' } ?: ""
        val pass = OFFApplication.instance.getLoginPreferences().getString("pass", "")?.trim { it <= ' ' } ?: ""

        return if (user.isBlank() || pass.isBlank()) {
            robotoffApi.annotateInsight(insightId, annotation.result)
        } else {
            robotoffApi.annotateInsight(insightId, annotation.result, Credentials.basic(user, pass, Charsets.UTF_8))
        }
    }

    /**
     * Load analysis tags from the server or local database
     *
     * @return The analysis tags in the product.
     */
    fun reloadAnalysisTagsFromServer() =
            getTaxonomyData(Taxonomy.ANALYSIS_TAGS, this, true, OFFApplication.daoSession.analysisTagDao)

    fun loadAnalysisTags(lastModifiedDate: Long) = analysisDataApi.getAnalysisTags()
            .map { it.map() }
            .doOnSuccess {
                saveAnalysisTags(it)
                updateLastDownloadDateInSettings(Taxonomy.ANALYSIS_TAGS, lastModifiedDate)
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
        OFFApplication.daoSession.database.beginTransaction()
        try {
            tags.forEach { tag ->
                OFFApplication.daoSession.analysisTagDao.insertOrReplace(tag)
                tag.names.forEach {
                    OFFApplication.daoSession.analysisTagNameDao.insertOrReplace(it)
                }
            }
            OFFApplication.daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveAnalysisTags", e)
        } finally {
            OFFApplication.daoSession.database.endTransaction()
        }
    }

    fun reloadAnalysisTagConfigsFromServer(): Single<List<AnalysisTagConfig>> =
            getTaxonomyData(Taxonomy.ANALYSIS_TAG_CONFIG, this, true, OFFApplication.daoSession.analysisTagConfigDao)

    fun loadAnalysisTagConfigs(lastModifiedDate: Long): Single<List<AnalysisTagConfig>> = analysisDataApi.getAnalysisTagConfigs()
            .map { it.map() }
            .doOnSuccess {
                saveAnalysisTagConfigs(it)
                updateLastDownloadDateInSettings(Taxonomy.ANALYSIS_TAG_CONFIG, lastModifiedDate)
            }

    private fun saveAnalysisTagConfigs(analysisTagConfigs: List<AnalysisTagConfig>) {
        OFFApplication.daoSession.database.beginTransaction()
        try {
            analysisTagConfigs.forEach {
                Picasso.get().load(it.iconUrl).fetch()
                OFFApplication.daoSession.analysisTagConfigDao.insertOrReplace(it)
            }
            OFFApplication.daoSession.database.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveAnalysisTagConfigs", e)
        } finally {
            OFFApplication.daoSession.database.endTransaction()
        }
    }

    private fun updateAnalysisTagConfig(analysisTagConfig: AnalysisTagConfig?, languageCode: String) {
        if (analysisTagConfig != null) {
            var analysisTagName = OFFApplication.daoSession.analysisTagNameDao.queryBuilder().where(
                    AnalysisTagNameDao.Properties.AnalysisTag.eq(analysisTagConfig.analysisTag),
                    AnalysisTagNameDao.Properties.LanguageCode.eq(languageCode)
            ).unique()
            if (analysisTagName == null) {
                analysisTagName = OFFApplication.daoSession.analysisTagNameDao.queryBuilder().where(
                        AnalysisTagNameDao.Properties.AnalysisTag.eq(analysisTagConfig.analysisTag),
                        AnalysisTagNameDao.Properties.LanguageCode.eq(ApiFields.Defaults.DEFAULT_LANGUAGE)
                ).unique()
            }
            analysisTagConfig.name = analysisTagName
            val type = "en:${analysisTagConfig.type}"
            var analysisTagTypeName = OFFApplication.daoSession.analysisTagNameDao.queryBuilder()
                    .where(AnalysisTagNameDao.Properties.AnalysisTag.eq(type),
                            AnalysisTagNameDao.Properties.LanguageCode.eq(languageCode))
                    .unique()
            if (analysisTagTypeName == null) {
                analysisTagTypeName = OFFApplication.daoSession.analysisTagNameDao.queryBuilder()
                        .where(AnalysisTagNameDao.Properties.AnalysisTag.eq(type),
                                AnalysisTagNameDao.Properties.LanguageCode.eq(ApiFields.Defaults.DEFAULT_LANGUAGE))
                        .unique()
            }
            analysisTagConfig.typeName = if (analysisTagTypeName != null) analysisTagTypeName.name else analysisTagConfig.type
        }
    }

    /**
     * @param analysisTag
     * @param languageCode
     * @return [Maybe.empty] if no analysis tag found
     */
    fun getAnalysisTagConfigByTagAndLanguageCode(analysisTag: String?, languageCode: String) = Maybe.fromCallable {
        OFFApplication.daoSession.analysisTagConfigDao.queryBuilder()
                .where(AnalysisTagConfigDao.Properties.AnalysisTag.eq(analysisTag))
                .unique().also { updateAnalysisTagConfig(it, languageCode) }
    }.subscribeOn(Schedulers.io())

    fun getUnknownAnalysisTagConfigsByLanguageCode(languageCode: String) = Single.fromCallable {
        OFFApplication.daoSession.analysisTagConfigDao.queryBuilder()
                .where(StringCondition("""${AnalysisTagConfigDao.Properties.AnalysisTag.columnName} LIKE "%unknown%""""))
                .list().onEach { updateAnalysisTagConfig(it, languageCode) }
    }.subscribeOn(Schedulers.io())


    private val LOG_TAG = ProductRepository::class.simpleName
}