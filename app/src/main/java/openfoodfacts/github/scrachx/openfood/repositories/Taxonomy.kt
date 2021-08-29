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

import kotlinx.coroutines.rx2.await
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcode
import openfoodfacts.github.scrachx.openfood.models.entities.additive.Additive
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.Allergen
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTag
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.models.entities.brand.Brand
import openfoodfacts.github.scrachx.openfood.models.entities.category.Category
import openfoodfacts.github.scrachx.openfood.models.entities.country.Country
import openfoodfacts.github.scrachx.openfood.models.entities.ingredient.Ingredient
import openfoodfacts.github.scrachx.openfood.models.entities.label.Label
import openfoodfacts.github.scrachx.openfood.models.entities.states.States
import openfoodfacts.github.scrachx.openfood.models.entities.store.Store
import openfoodfacts.github.scrachx.openfood.models.entities.tag.Tag
import openfoodfacts.github.scrachx.openfood.network.services.AnalysisDataAPI

sealed class Taxonomy<T>(val jsonUrl: String) {
    object Labels : Taxonomy<Label>(AnalysisDataAPI.LABELS_JSON) {
        override suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<Label> =
            repository.loadLabels(lastModifiedDate).await()
    }

    object Countries : Taxonomy<Country>(AnalysisDataAPI.COUNTRIES_JSON) {
        override suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<Country> =
            repository.loadCountries(lastModifiedDate).await()
    }

    object Categories : Taxonomy<Category>(AnalysisDataAPI.CATEGORIES_JSON) {
        override suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<Category> =
            repository.loadCategories(lastModifiedDate).await()
    }

    object Additives : Taxonomy<Additive>(AnalysisDataAPI.ADDITIVES_JSON) {
        override suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<Additive> =
            repository.loadAdditives(lastModifiedDate).await()
    }

    object Ingredients : Taxonomy<Ingredient>(AnalysisDataAPI.INGREDIENTS_JSON) {
        override suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<Ingredient> =
            repository.loadIngredients(lastModifiedDate).await()
    }

    object Allergens : Taxonomy<Allergen>(AnalysisDataAPI.ALLERGENS_JSON) {
        override suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<Allergen> =
            repository.loadAllergens(lastModifiedDate).await()
    }

    object AnalysisTags : Taxonomy<AnalysisTag>(AnalysisDataAPI.ANALYSIS_TAG_JSON) {
        override suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<AnalysisTag> =
            repository.loadAnalysisTags(lastModifiedDate).await()
    }

    object AnalysisTagConfigs : Taxonomy<AnalysisTagConfig>(AnalysisDataAPI.ANALYSIS_TAG_CONFIG_JSON) {
        override suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<AnalysisTagConfig> =
            repository.loadAnalysisTagConfigs(lastModifiedDate).await()
    }

    object Tags : Taxonomy<Tag>(AnalysisDataAPI.TAGS_JSON) {
        override suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<Tag> =
            repository.loadTags(lastModifiedDate).await()
    }

    object InvalidBarcodes : Taxonomy<InvalidBarcode>(AnalysisDataAPI.INVALID_BARCODES_JSON) {
        override suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<InvalidBarcode> =
            repository.loadInvalidBarcodes(lastModifiedDate).await()
    }

    object ProductStates : Taxonomy<States>(AnalysisDataAPI.STATES_JSON) {
        override suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<States> =
            repository.loadStates(lastModifiedDate).await()
    }

    object Stores : Taxonomy<Store>(AnalysisDataAPI.STORES_JSON) {
        override suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<Store> =
            repository.loadStores(lastModifiedDate).await()
    }

    object Brands : Taxonomy<Brand>(AnalysisDataAPI.BRANDS_JSON) {
        override suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<Brand> =
            repository.loadBrands(lastModifiedDate).await()
    }

    fun getLastDownloadTimeStampPreferenceId() = "taxonomy_lastDownloadTimeStamp_${this::class.simpleName}"
    fun getDownloadActivatePreferencesId() = "taxonomy_download_${this::class.simpleName}"

    abstract suspend fun load(repository: ProductRepository, lastModifiedDate: Long): List<T>
}