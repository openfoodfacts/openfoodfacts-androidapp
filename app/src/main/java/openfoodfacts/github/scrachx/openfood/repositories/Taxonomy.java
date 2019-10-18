package openfoodfacts.github.scrachx.openfood.repositories;

import io.reactivex.Single;
import openfoodfacts.github.scrachx.openfood.models.*;

import java.util.List;

public enum Taxonomy {
    LABEL("data/taxonomies/labels.json") {
        @Override
        public Single<List<Label>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadLabels(lastModifiedDate);
        }
    },
    COUNTRY("data/taxonomies/countries.json") {
        @Override
        public Single<List<Country>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadCountries(lastModifiedDate);
        }
    },
    CATEGORY("data/taxonomies/categories.json") {
        @Override
        public Single<List<Category>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadCategories(lastModifiedDate);
        }
    },
    ADDITIVE("data/taxonomies/additives.json") {
        @Override
        public Single<List<Additive>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAdditives(lastModifiedDate);
        }
    },
    INGREDIENT("data/taxonomies/ingredients.json") {
        @Override
        public Single<List<Ingredient>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadIngredients(lastModifiedDate);
        }
    },
    ALLERGEN("data/taxonomies/allergens.json") {
        @Override
        public Single<List<Allergen>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAllergens(lastModifiedDate);
        }
    },
    ANALYSIS_TAGS("data/taxonomies/ingredients_analysis.json") {
        @Override
        public Single<List<AnalysisTag>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAnalysisTags(lastModifiedDate);
        }
    },
    ANALYSIS_TAG_CONFIG("files/app/ingredients-analysis.json") {
        @Override
        public Single<List<AnalysisTagConfig>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAnalysisTagConfigs(lastModifiedDate);
        }
    };
    private final String jsonUrl;

    Taxonomy(String jsonName) {
        this.jsonUrl = jsonName;
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
