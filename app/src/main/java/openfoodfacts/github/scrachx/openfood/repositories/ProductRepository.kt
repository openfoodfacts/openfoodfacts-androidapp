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
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagDao
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfigDao
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfigsWrapper
import openfoodfacts.github.scrachx.openfood.models.entities.category.*
import openfoodfacts.github.scrachx.openfood.models.entities.country.Country
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryDao
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.ingredient.*
import openfoodfacts.github.scrachx.openfood.models.entities.label.*
import openfoodfacts.github.scrachx.openfood.models.entities.tag.Tag
import openfoodfacts.github.scrachx.openfood.models.entities.tag.TagDao
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.analysisDataApi
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.robotoffApi
import openfoodfacts.github.scrachx.openfood.repositories.TaxonomiesManager.getTaxonomyData
import openfoodfacts.github.scrachx.openfood.utils.getLoginPreferences
import org.greenrobot.greendao.database.Database
import org.greenrobot.greendao.query.WhereCondition.StringCondition
import java.util.*

/**
 * This is a repository class which implements repository interface.
 *
 * @author Lobster
 * @since 03.03.18
 */
object ProductRepository {
    private val additiveDao: AdditiveDao
    private val additiveNameDao: AdditiveNameDao
    private val allergenDao: AllergenDao
    private val allergenNameDao: AllergenNameDao
    private val analysisTagConfigDao: AnalysisTagConfigDao
    private val analysisTagDao: AnalysisTagDao
    private val analysisTagNameDao: AnalysisTagNameDao
    private val categoryDao: CategoryDao
    private val categoryNameDao: CategoryNameDao
    private val countryDao: CountryDao
    private val countryNameDao: CountryNameDao
    private val db: Database
    private val ingredientDao: IngredientDao
    private val ingredientNameDao: IngredientNameDao
    private val ingredientsRelationDao: IngredientsRelationDao
    private val invalidBarcodeDao: InvalidBarcodeDao
    private val labelDao: LabelDao
    private val labelNameDao: LabelNameDao
    private val tagDao: TagDao

    /**
     * Load labels from the server or local database
     *
     * @return The list of Labels.
     */
    fun reloadLabelsFromServer(): Single<List<Label>> =
            getTaxonomyData(Taxonomy.LABEL, this, true, labelDao)

    fun loadLabels(lastModifiedDate: Long): Single<List<Label>> = analysisDataApi.getLabels()
            .map { it.map() }
            .doOnSuccess { labels: List<Label> ->
                saveLabels(labels)
                updateLastDownloadDateInSettings(Taxonomy.LABEL, lastModifiedDate)
            }

    /**
     * Load tags from the server or local database
     *
     * @return The list of Tags.
     */
    fun reloadTagsFromServer(): Single<List<Tag>> = getTaxonomyData(Taxonomy.TAGS, this, true, tagDao)

    fun loadTags(lastModifiedDate: Long): Single<List<Tag>> = analysisDataApi.getTags()
            .map { it.tags }
            .doOnSuccess {
                saveTags(it)
                updateLastDownloadDateInSettings(Taxonomy.TAGS, lastModifiedDate)
            }

    fun reloadInvalidBarcodesFromServer(): Single<List<InvalidBarcode>> = getTaxonomyData(Taxonomy.INVALID_BARCODES, this, true, invalidBarcodeDao)

    fun loadInvalidBarcodes(lastModifiedDate: Long) = analysisDataApi.getInvalidBarcodes()
            .map { strings -> strings.map { InvalidBarcode(it) } }
            .doOnSuccess { invalidBarcodes ->
                saveInvalidBarcodes(invalidBarcodes)
                updateLastDownloadDateInSettings(Taxonomy.INVALID_BARCODES, lastModifiedDate)
            }

    /**
     * Load allergens from the server or local database
     *
     * @return The allergens in the product.
     */
    fun reloadAllergensFromServer(): Single<List<Allergen>> =
            // FIXME: this returns 404
            getTaxonomyData(Taxonomy.ALLERGEN, this, true, allergenDao)

    fun getAllergens(): Single<List<Allergen>> =
            getTaxonomyData(Taxonomy.ALLERGEN, this, false, allergenDao)

    fun loadAllergens(lastModifiedDate: Long): Single<List<Allergen>> = analysisDataApi.getAllergens()
            .map { it.map() }
            .doOnSuccess { allergens: List<Allergen> ->
                saveAllergens(allergens)
                updateLastDownloadDateInSettings(Taxonomy.ALLERGEN, lastModifiedDate)
            }

    /**
     * Load countries from the server or local database
     *
     * @return The list of countries.
     */
    fun reloadCountriesFromServer(): Single<List<Country>> =
            getTaxonomyData(Taxonomy.COUNTRY, this, true, countryDao)

    fun loadCountries(lastModifiedDate: Long): Single<List<Country>> = analysisDataApi.getCountries()
            .map { it.map() }
            .doOnSuccess { countries: List<Country> ->
                saveCountries(countries)
                updateLastDownloadDateInSettings(Taxonomy.COUNTRY, lastModifiedDate)
            }

    /**
     * Load categories from the server or local database
     *
     * @return The list of categories.
     */
    fun reloadCategoriesFromServer(): Single<List<Category>> =
            getTaxonomyData(Taxonomy.CATEGORY, this, true, categoryDao)

    fun getCategories(): Single<List<Category>> = getTaxonomyData(Taxonomy.CATEGORY, this, false, categoryDao)

    fun loadCategories(lastModifiedDate: Long): Single<List<Category>> {
        return analysisDataApi.getCategories()
                .map { obj: CategoriesWrapper -> obj.map() }
                .doOnSuccess { categories: List<Category> ->
                    saveCategories(categories)
                    updateLastDownloadDateInSettings(Taxonomy.CATEGORY, lastModifiedDate)
                }
    }

    /**
     * Load allergens which user selected earlier (i.e user's allergens)
     *
     * @return The list of allergens.
     */
    val enabledAllergens: List<Allergen>
        get() = allergenDao.queryBuilder().where(AllergenDao.Properties.Enabled.eq("true")).list()

    /**
     * Load additives from the server or local database
     *
     * @return The list of additives.
     */
    fun reloadAdditivesFromServer(): Single<List<Additive>> {
        return getTaxonomyData(Taxonomy.ADDITIVE, this, true, additiveDao)
    }

    fun loadAdditives(lastModifiedDate: Long): Single<List<Additive>> {
        return analysisDataApi.getAdditives()
                .map { it.map() }
                .doOnSuccess { additives: List<Additive> ->
                    saveAdditives(additives)
                    updateLastDownloadDateInSettings(Taxonomy.ADDITIVE, lastModifiedDate)
                }
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
            getTaxonomyData(Taxonomy.INGREDIENT, this, true, ingredientDao)

    fun loadIngredients(lastModifiedDate: Long): Single<List<Ingredient>> = analysisDataApi.getIngredients()
            .map { it.map() }
            .doOnSuccess { ingredients: List<Ingredient> ->
                saveIngredients(ingredients)
                updateLastDownloadDateInSettings(Taxonomy.INGREDIENT, lastModifiedDate)
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
        db.beginTransaction()
        try {
            labels.forEach { label ->
                labelDao.insertOrReplace(label)
                label.names.forEach { labelNameDao.insertOrReplace(it) }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveLabels", e)
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Tags saving to local database
     *
     * @param tags The list of tags to be saved.
     */
    private fun saveTags(tags: List<Tag>) = tagDao.insertOrReplaceInTx(tags)

    /**
     * Invalid Barcodess saving to local database. Will clear all previous invalid barcodes stored before.
     *
     * @param invalidBarcodes The list of invalidBarcodes to be saved.
     */
    private fun saveInvalidBarcodes(invalidBarcodes: List<InvalidBarcode>) {
        invalidBarcodeDao.deleteAll()
        invalidBarcodeDao.insertOrReplaceInTx(invalidBarcodes)
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
        db.beginTransaction()
        try {
            allergens.forEach { allergen ->
                allergenDao.insertOrReplace(allergen)
                allergen.names.forEach { allergenNameDao.insertOrReplace(it) }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveAllergens", e)
        } finally {
            db.endTransaction()
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
        db.beginTransaction()
        try {
            additives.forEach { additive ->
                additiveDao.insertOrReplace(additive)
                additive.names.forEach { additiveNameDao.insertOrReplace(it) }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveAdditives", e)
        } finally {
            db.endTransaction()
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
        db.beginTransaction()
        try {
            countries.forEach { country ->
                countryDao.insertOrReplace(country)
                country.names.forEach { countryNameDao.insertOrReplace(it) }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveCountries", e)
        } finally {
            db.endTransaction()
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
        db.beginTransaction()
        try {
            for (category in categories) {
                categoryDao.insertOrReplace(category)
                for (categoryName in category.names) {
                    categoryNameDao.insertOrReplace(categoryName)
                }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveCategories", e)
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Delete rows from Ingredient, IngredientName and IngredientsRelation
     * set the autoincrement to 0
     */
    fun deleteIngredientCascade() {
        ingredientDao.deleteAll()
        ingredientNameDao.deleteAll()
        ingredientsRelationDao.deleteAll()
        val daoSession = OFFApplication.daoSession
        daoSession.database.execSQL("""update sqlite_sequence set seq=0 where name in 
            |('${ingredientDao.tablename}', 
            |'${ingredientNameDao.tablename}', 
            |'${ingredientsRelationDao.tablename}')""".trimMargin())
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
        db.beginTransaction()
        try {
            for (ingredient in ingredients) {
                ingredientDao.insertOrReplace(ingredient)
                for (ingredientName in ingredient.names) {
                    ingredientNameDao.insertOrReplace(ingredientName)
                }
                for (ingredientsRelation in ingredient.parents) {
                    ingredientsRelationDao.insertOrReplace(ingredientsRelation)
                }
                for (ingredientsRelation in ingredient.children) {
                    ingredientsRelationDao.insertOrReplace(ingredientsRelation)
                }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveIngredients", e)
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Ingredient saving to local database
     *
     * @param ingredient The ingredient to be saved.
     */
    fun saveIngredient(ingredient: Ingredient) {
        val ingredients: MutableList<Ingredient> = ArrayList()
        ingredients.add(ingredient)
        saveIngredients(ingredients)
    }

    /**
     * Changes enabled field of allergen and updates it.
     *
     * @param isEnabled depends on whether user selected or unselected the allergen
     * @param allergenTag is unique Id of allergen
     */
    fun setAllergenEnabled(allergenTag: String?, isEnabled: Boolean?) {
        val allergen = allergenDao.queryBuilder()
                .where(AllergenDao.Properties.Tag.eq(allergenTag))
                .unique()
        if (allergen != null) {
            allergen.enabled = isEnabled
            allergenDao.update(allergen)
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
            val labelName = labelNameDao.queryBuilder()
                    .where(
                            LabelNameDao.Properties.LabelTag.eq(labelTag),
                            LabelNameDao.Properties.LanguageCode.eq(languageCode)
                    ).unique()
            labelName ?: LabelName()
        }
    }

    /**
     * Loads translated label from the local database by unique tag of label and default language code
     *
     * @param labelTag is a unique Id of label
     * @return The translated label
     */
    fun getLabelByTagAndDefaultLanguageCode(labelTag: String?): Single<LabelName> {
        return getLabelByTagAndLanguageCode(labelTag, ApiFields.Defaults.DEFAULT_LANGUAGE)
    }

    /**
     * Loads translated additive from the local database by unique tag of additive and language code
     *
     * @param additiveTag is a unique Id of additive
     * @param languageCode is a 2-digit language code
     * @return The translated additive name
     */
    fun getAdditiveByTagAndLanguageCode(additiveTag: String?, languageCode: String?): Single<AdditiveName> {
        return Single.fromCallable {
            val additiveName = additiveNameDao.queryBuilder()
                    .where(
                            AdditiveNameDao.Properties.AdditiveTag.eq(additiveTag),
                            AdditiveNameDao.Properties.LanguageCode.eq(languageCode)
                    ).unique()
            additiveName ?: AdditiveName()
        }
    }

    /**
     * Loads translated additive from the local database by unique tag of additive and default language code
     *
     * @param additiveTag is a unique Id of additive
     * @return The translated additive tag
     */
    fun getAdditiveByTagAndDefaultLanguageCode(additiveTag: String?): Single<AdditiveName> {
        return getAdditiveByTagAndLanguageCode(additiveTag, ApiFields.Defaults.DEFAULT_LANGUAGE)
    }

    val countries: Single<List<Country>>
        get() = getTaxonomyData(Taxonomy.COUNTRY, this, false, countryDao)

    fun getCountryByCC2OrWorld(cc2: String?): Single<Optional<Country>> {
        return countries.map { countries: List<Country> ->
            countries.stream()
                    .filter { country: Country -> country.cc2.equals(cc2, ignoreCase = true) }
                    .findFirst()
        }
    }

    /**
     * Loads translated category from the local database by unique tag of category and language code
     *
     * @param categoryTag is a unique Id of category
     * @param languageCode is a 2-digit language code
     * @return The translated category name
     */
    fun getCategoryByTagAndLanguageCode(categoryTag: String?, languageCode: String = ApiFields.Defaults.DEFAULT_LANGUAGE): Single<CategoryName> =
            Single.fromCallable {
                categoryNameDao.queryBuilder().where(
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
    fun getAllCategoriesByLanguageCode(languageCode: String?): Single<List<CategoryName>> = Single.fromCallable {
        categoryNameDao.queryBuilder()
                .where(CategoryNameDao.Properties.LanguageCode.eq(languageCode))
                .orderAsc(CategoryNameDao.Properties.Name)
                .list()
    }

    /**
     * Loads list of category names from the local database by default language code
     *
     * @return The list of category name
     */
    fun getAllCategoriesByDefaultLanguageCode(): Single<List<CategoryName>> = getAllCategoriesByLanguageCode(ApiFields.Defaults.DEFAULT_LANGUAGE)

    /**
     * Loads translated and selected/unselected allergens.
     *
     * @param isEnabled depends on whether allergen was selected or unselected by user
     * @param languageCode is a 2-digit language code
     * @return The list of allergen names
     */
    fun getAllergensByEnabledAndLanguageCode(isEnabled: Boolean?, languageCode: String?): Single<List<AllergenName>> = Single.fromCallable {
        val allergens = allergenDao.queryBuilder().where(AllergenDao.Properties.Enabled.eq(isEnabled)).list() ?: return@fromCallable emptyList()
        allergens.mapNotNull {
            allergenNameDao.queryBuilder().where(
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
        allergenNameDao.queryBuilder()
                .where(AllergenNameDao.Properties.LanguageCode.eq(languageCode))
                .list()
    }.subscribeOn(Schedulers.io())

    /**
     * Loads translated allergen from the local database by unique tag of allergen and language code
     *
     * @param allergenTag is a unique Id of allergen
     * @param languageCode is a 2-digit language code
     * @return The translated allergen name
     */
    fun getAllergenByTagAndLanguageCode(allergenTag: String?, languageCode: String?): Single<AllergenName> = Single.fromCallable {
        val allergenName = allergenNameDao.queryBuilder()
                .where(AllergenNameDao.Properties.AllergenTag.eq(allergenTag),
                        AllergenNameDao.Properties.LanguageCode.eq(languageCode))
                .unique()
        if (allergenName != null) {
            return@fromCallable allergenName
        } else {
            val emptyAllergenName = AllergenName()
            emptyAllergenName.name = allergenTag
            emptyAllergenName.allergenTag = allergenTag
            emptyAllergenName.isWikiDataIdPresent = false
            return@fromCallable emptyAllergenName
        }
    }.subscribeOn(Schedulers.io())

    /**
     * Loads translated allergen from the local database by unique tag of allergen and default language code
     *
     * @param allergenTag is a unique Id of allergen
     * @return The translated allergen name
     */
    fun getAllergenByTagAndDefaultLanguageCode(allergenTag: String?): Single<AllergenName> =
            getAllergenByTagAndLanguageCode(allergenTag, ApiFields.Defaults.DEFAULT_LANGUAGE)

    /**
     * Loads Robotoff question from the local database by code and lang of question.
     *
     * @param code for the question
     * @param lang is language of the question
     * @return The single question
     */
    fun getSingleProductQuestion(code: String?, lang: String?): Maybe<Question> = robotoffApi.getProductQuestions(code, lang, 1)
            .map { obj: QuestionsState -> obj.questions }
            .flatMapMaybe { questions -> if (questions.isNotEmpty()) Maybe.just(questions[0]) else Maybe.empty() }

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
    fun reloadAnalysisTagsFromServer(): Single<List<AnalysisTag>> =
            getTaxonomyData(Taxonomy.ANALYSIS_TAGS, this, true, analysisTagDao)

    fun loadAnalysisTags(lastModifiedDate: Long): Single<List<AnalysisTag>> = analysisDataApi.getAnalysisTags()
            .map { it.map() }
            .doOnSuccess { analysisTags: List<AnalysisTag> ->
                saveAnalysisTags(analysisTags)
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
        db.beginTransaction()
        try {
            tags.forEach { tag ->
                analysisTagDao.insertOrReplace(tag)
                tag.names.forEach { name ->
                    analysisTagNameDao.insertOrReplace(name)
                }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveAnalysisTags", e)
        } finally {
            db.endTransaction()
        }
    }

    fun reloadAnalysisTagConfigsFromServer(): Single<List<AnalysisTagConfig>> =
            getTaxonomyData(Taxonomy.ANALYSIS_TAG_CONFIG, this, true, analysisTagConfigDao)

    fun loadAnalysisTagConfigs(lastModifiedDate: Long): Single<List<AnalysisTagConfig>> = analysisDataApi.getAnalysisTagConfigs()
            .map<List<AnalysisTagConfig>> { obj: AnalysisTagConfigsWrapper -> obj.map() }
            .doOnSuccess { analysisTagConfigs: List<AnalysisTagConfig> ->
                saveAnalysisTagConfigs(analysisTagConfigs)
                updateLastDownloadDateInSettings(Taxonomy.ANALYSIS_TAG_CONFIG, lastModifiedDate)
            }

    private fun saveAnalysisTagConfigs(analysisTagConfigs: List<AnalysisTagConfig>) {
        db.beginTransaction()
        try {
            for (analysisTagConfig in analysisTagConfigs) {
                Picasso.get().load(analysisTagConfig.iconUrl).fetch()
                analysisTagConfigDao.insertOrReplace(analysisTagConfig)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "saveAnalysisTagConfigs", e)
        } finally {
            db.endTransaction()
        }
    }

    private fun updateAnalysisTagConfig(analysisTagConfig: AnalysisTagConfig?, languageCode: String) {
        if (analysisTagConfig != null) {
            var analysisTagName = analysisTagNameDao.queryBuilder().where(
                    AnalysisTagNameDao.Properties.AnalysisTag.eq(analysisTagConfig.analysisTag),
                    AnalysisTagNameDao.Properties.LanguageCode.eq(languageCode)
            ).unique()
            if (analysisTagName == null) {
                analysisTagName = analysisTagNameDao.queryBuilder().where(
                        AnalysisTagNameDao.Properties.AnalysisTag.eq(analysisTagConfig.analysisTag),
                        AnalysisTagNameDao.Properties.LanguageCode.eq(ApiFields.Defaults.DEFAULT_LANGUAGE)
                ).unique()
            }
            analysisTagConfig.name = analysisTagName
            val type = "en:${analysisTagConfig.type}"
            var analysisTagTypeName = analysisTagNameDao.queryBuilder()
                    .where(AnalysisTagNameDao.Properties.AnalysisTag.eq(type),
                            AnalysisTagNameDao.Properties.LanguageCode.eq(languageCode))
                    .unique()
            if (analysisTagTypeName == null) {
                analysisTagTypeName = analysisTagNameDao.queryBuilder()
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
    fun getAnalysisTagConfigByTagAndLanguageCode(analysisTag: String?, languageCode: String): Maybe<AnalysisTagConfig> = Maybe.fromCallable {
        val analysisTagConfig = analysisTagConfigDao.queryBuilder()
                .where(AnalysisTagConfigDao.Properties.AnalysisTag.eq(analysisTag))
                .unique()
        updateAnalysisTagConfig(analysisTagConfig, languageCode)
        analysisTagConfig
    }.subscribeOn(Schedulers.io())

    fun getUnknownAnalysisTagConfigsByLanguageCode(languageCode: String): Single<List<AnalysisTagConfig>> = Single.fromCallable {
        val analysisTagConfigs = analysisTagConfigDao.queryBuilder()
                .where(StringCondition("""${AnalysisTagConfigDao.Properties.AnalysisTag.columnName} LIKE "%unknown%"""")).list()
        analysisTagConfigs.forEach { analysisTagConfig -> updateAnalysisTagConfig(analysisTagConfig, languageCode) }
        analysisTagConfigs
    }.subscribeOn(Schedulers.io())


    private val LOG_TAG = ProductRepository::class.simpleName


    /**
     * Constructor of the class which is used to initialize objects.
     */
    init {
        db = OFFApplication.daoSession.database
        labelDao = OFFApplication.daoSession.labelDao
        labelNameDao = OFFApplication.daoSession.labelNameDao
        tagDao = OFFApplication.daoSession.tagDao
        invalidBarcodeDao = OFFApplication.daoSession.invalidBarcodeDao
        allergenDao = OFFApplication.daoSession.allergenDao
        allergenNameDao = OFFApplication.daoSession.allergenNameDao
        additiveDao = OFFApplication.daoSession.additiveDao
        additiveNameDao = OFFApplication.daoSession.additiveNameDao
        countryDao = OFFApplication.daoSession.countryDao
        countryNameDao = OFFApplication.daoSession.countryNameDao
        categoryDao = OFFApplication.daoSession.categoryDao
        categoryNameDao = OFFApplication.daoSession.categoryNameDao
        ingredientDao = OFFApplication.daoSession.ingredientDao
        ingredientNameDao = OFFApplication.daoSession.ingredientNameDao
        ingredientsRelationDao = OFFApplication.daoSession.ingredientsRelationDao
        analysisTagDao = OFFApplication.daoSession.analysisTagDao
        analysisTagNameDao = OFFApplication.daoSession.analysisTagNameDao
        analysisTagConfigDao = OFFApplication.daoSession.analysisTagConfigDao
    }
}