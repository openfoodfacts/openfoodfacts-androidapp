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

package openfoodfacts.github.scrachx.openfood.network;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

/**
 * This class lists all fields, field prefixes and suffixes and default values used by the api.
 * We discourage the use of string literals through the code and we recommend creating a field here so that it can be updated on API change.
 */
public final class ApiFields {
    private ApiFields() {
    }

    public static final class Prefix {
        static final String PRODUCT_NAME = "product_name_";
        static final String INGREDIENTS_TEXT = "ingredients_text_";

        private Prefix() {

        }
    }

    public static final class Suffix {
        public static final String MODIFIER = "_modifier";
        public static final String UNIT = "_unit";
        public static final String VALUE_100G = "_100g";
        public static final String SERVING = "_serving";

        private Suffix() {
        }
    }

    /**
     * Default values for some fields
     */
    public static final class Defaults {
        public static final String NUTRITION_DATA_PER_100G = "100g";
        public static final String NUTRITION_DATA_PER_SERVING = "serving";
        public static final String DEBUG_BARCODE = "1";
        public static final String DEFAULT_TAXO_PREFIX = "en";
        public static final String DEFAULT_LANGUAGE = "en";
        public static final String STATUS_NOT_OK = "status not ok";

        private Defaults() {

        }
    }

    public static final class Keys {
        public static final String NO_NUTRITION_DATA = "no_nutrition_data";
        /**
         * The value can either be {@link Defaults#NUTRITION_DATA_PER_100G}
         * or {@link Defaults#NUTRITION_DATA_PER_SERVING}
         */
        public static final String NUTRITION_DATA_PER = "nutrition_data_per";
        /**
         * Main language name
         * For other languages see {@link #lcProductNameKey(String)}
         */
        public static final String PRODUCT_NAME = "product_name";
        public static final String LANG = "lang";
        public static final String IMAGE_FRONT = "image_front";
        public static final String IMAGE_FRONT_UPLOADED = "image_front_uploaded";
        public static final String IMAGE_INGREDIENTS = "image_ingredients";
        public static final String IMAGE_INGREDIENTS_UPLOADED = "image_ingredients_uploaded";
        public static final String IMAGE_NUTRITION = "image_nutrition";
        public static final String IMAGE_NUTRITION_UPLOADED = "image_nutrition_uploaded";
        public static final String BARCODE = "code";
        public static final String QUANTITY = "quantity";
        public static final String ADD_BRANDS = "add_brands";
        public static final String BRANDS = "brands";
        public static final String LC = "lc";
        public static final String PERIODS_AFTER_OPENING = "periods_after_opening";
        public static final String EMB_CODES = "emb_codes";
        public static final String LINK = "link";
        public static final String ADD_PURCHASE = "add_purchase_places";
        public static final String ADD_STORES = "add_stores";
        public static final String ADD_COUNTRIES = "add_countries";
        public static final String SERVING_SIZE = "serving_size";
        public static final String ADD_TRACES = "add_traces";
        public static final String CREATOR = "creator";
        public static final String CREATED_DATE_TIME = "created_t";
        public static final String MINERALS_TAGS = "minerals_tags";
        public static final String AMINO_ACIDS_TAGS = "amino_acids_tags";
        public static final String LAST_MODIFIED_TIME = "last_modified_t";
        public static final String NOVA_GROUPS = "nova_groups";
        public static final String ENVIRONMENT_IMPACT_LEVEL_TAGS = "environment_impact_level_tags";
        public static final String PURCHASE_PLACES = "purchase_places";
        public static final String OTHER_INFORMATION = "other_information";
        public static final String TRACES_TAGS = "traces_tags";
        public static final String IMAGE_SMALL_URL = "image_small_url";
        public static final String IMAGE_NUTRITION_URL = "image_nutrition_url";
        public static final String IMAGE_FRONT_URL = "image_front_url";
        public static final String IMAGE_INGREDIENTS_URL = "image_ingredients_url";
        public static final String ADDITIVES_TAGS = "additives_tags";
        public static final String CATEGORIES_TAGS = "categories_tags";
        public static final String INGREDIENTS_TEXT = "ingredients_text";
        public static final String GENERIC_NAME = "generic_name";
        public static final String INGREDIENTS_MAY_PALM_OIL_N = "ingredients_from_or_that_may_be_from_palm_oil_n";
        public static final String LAST_MODIFIED_BY = "last_modified_by";
        public static final String NUTRIENT_LEVELS = "nutrient_levels";
        public static final String COUNTRIES_TAGS = "countries_tags";
        public static final String LABELS_HIERARCHY = "labels_hierarchy";
        public static final String EMB_CODES_TAGS = "emb_codes_tags";
        public static final String INGREDIENTS_PALM_OIL_N = "ingredients_from_palm_oil_n";
        public static final String CITIES_TAGS = "cities_tags";
        public static final String EDITORS_TAGS = "editors_tags";
        public static final String ALLERGENS_TAGS = "allergens_tags";
        public static final String NUTRITION_GRADE_FR = "nutrition_grade_fr";
        public static final String IMAGE_URL = "image_url";
        public static final String INGREDIENTS_MAY_PALM_OIL_TAGS = "ingredients_that_may_be_from_palm_oil_tags";
        public static final String STATES_TAGS = "states_tags";
        public static final String CUSTOMER_SERVICE = "customer_service";
        public static final String ENVIRONMENT_INFOCARD = "environment_infocard";
        public static final String WARNING = "warning";
        public static final String RECYCLING_INSTRUCTIONS_TO_RECYCLE = "recycling_instructions_to_recycle";
        public static final String RECYCLING_INSTRUCTIONS_TO_DISCARD = "recycling_instructions_to_discard";
        public static final String CONSERVATION_CONDITIONS = "conservation_conditions";
        public static final String INGREDIENTS_FROM_PALM_OIL_TAGS = "ingredients_from_palm_oil_tags";
        public static final String OTHER_NUTRITIONAL_SUBSTANCES_TAGS = "other_nutritional_substances_tags";
        public static final String VITAMINS_TAGS = "vitamins_tags";
        public static final String INGREDIENTS_ANALYSIS_TAGS = "ingredients_analysis_tags";
        public static final String INGREDIENTS = "ingredients";
        public static final String LABELS_TAGS = "labels_tags";
        public static final String MANUFACTURING_PLACES = "manufacturing_places";
        public static final String BRANDS_TAGS = "brands_tags";
        public static final String ALLERGENS_HIERARCHY = "allergens_hierarchy";
        public static final String SELECTED_IMAGES = "selected_images";
        public static final String IMAGES = "images";
        public static final String USER_ID = "user_id";
        public static final String USER_PASS = "password";
        public static final String USER_COMMENT = "comment";
        public static final String PACKAGING = "packaging";
        public static final String CATEGORIES = "categories";
        public static final String LABELS = "labels";
        public static final String ORIGINS = "origins";
        public static final String COUNTRIES = "countries";
        public static final String STORES = "stores";
        public static final String STATUS = "status";
        public static final String NUTRITION_GRADE = "nutrition_grades_tags";

        private Keys() {

        }

        @NonNull
        @Contract(pure = true)
        public static String lcProductNameKey(final String lang) {
            return Prefix.PRODUCT_NAME + lang;
        }

        @NonNull
        @Contract(pure = true)
        public static String lcIngredientsKey(final String lang) {
            return Prefix.INGREDIENTS_TEXT + lang;
        }
    }
}
