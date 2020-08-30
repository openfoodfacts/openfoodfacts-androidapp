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

import java.util.List;

import io.reactivex.Single;
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcode;
import openfoodfacts.github.scrachx.openfood.models.entities.additive.Additive;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.Allergen;
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTag;
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig;
import openfoodfacts.github.scrachx.openfood.models.entities.category.Category;
import openfoodfacts.github.scrachx.openfood.models.entities.country.Country;
import openfoodfacts.github.scrachx.openfood.models.entities.ingredient.Ingredient;
import openfoodfacts.github.scrachx.openfood.models.entities.label.Label;
import openfoodfacts.github.scrachx.openfood.models.entities.tag.Tag;
import openfoodfacts.github.scrachx.openfood.network.services.AnalysisDataAPI;

@SuppressWarnings("unchecked")
public enum Taxonomy {
    LABEL(AnalysisDataAPI.LABELS_JSON) {
        @Override
        public Single<List<Label>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadLabels(lastModifiedDate);
        }
    },
    COUNTRY(AnalysisDataAPI.COUNTRIES_JSON) {
        @Override
        public Single<List<Country>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadCountries(lastModifiedDate);
        }
    },
    CATEGORY(AnalysisDataAPI.CATEGORIES_JSON) {
        @Override
        public Single<List<Category>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadCategories(lastModifiedDate);
        }
    },
    ADDITIVE(AnalysisDataAPI.ADDITIVES_JSON) {
        @Override
        public Single<List<Additive>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAdditives(lastModifiedDate);
        }
    },
    INGREDIENT(AnalysisDataAPI.INGREDIENTS_JSON) {
        @Override
        public Single<List<Ingredient>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadIngredients(lastModifiedDate);
        }
    },
    ALLERGEN(AnalysisDataAPI.ALLERGENS_JSON) {
        @Override
        public Single<List<Allergen>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAllergens(lastModifiedDate);
        }
    },
    ANALYSIS_TAGS(AnalysisDataAPI.ANALYSIS_TAG_JSON) {
        @Override
        public Single<List<AnalysisTag>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAnalysisTags(lastModifiedDate);
        }
    },
    ANALYSIS_TAG_CONFIG(AnalysisDataAPI.ANALYSIS_TAG_CONFIG_JSON) {
        @Override
        public Single<List<AnalysisTagConfig>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAnalysisTagConfigs(lastModifiedDate);
        }
    },
    TAGS(AnalysisDataAPI.TAGS_JSON) {
        @Override
        public Single<List<Tag>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadTags(lastModifiedDate);
        }
    },
    INVALID_BARCODES(AnalysisDataAPI.INVALID_BARCODES_JSON) {
        @Override
        public Single<List<InvalidBarcode>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadInvalidBarcodes(lastModifiedDate);
        }
    };
    private final String jsonUrl;

    Taxonomy(String jsonUrl) {
        this.jsonUrl = jsonUrl;
    }

    public String getJsonUrl() {
        return jsonUrl;
    }

    public String getLastDownloadTimeStampPreferenceId() {
        return "taxonomy_lastDownloadTimeStamp_" + name();
    }

    public String getDownloadActivatePreferencesId() {
        return "taxonomy_download_" + name();
    }

    public abstract <T> Single<List<T>> load(ProductRepository repository, long lastModifiedDate);
}
