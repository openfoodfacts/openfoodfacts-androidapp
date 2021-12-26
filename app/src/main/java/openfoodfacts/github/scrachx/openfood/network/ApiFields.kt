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
@file:Suppress("MemberVisibilityCanBePrivate")

package openfoodfacts.github.scrachx.openfood.network

import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import org.jetbrains.annotations.Contract

/**
 * This class lists all fields, field prefixes and suffixes and default values used by the api.
 * We discourage the use of string literals through the code and we recommend creating a field here so that it can be updated on API change.
 */
object ApiFields {
    object StateTags {
        const val CATEGORIES_TO_BE_COMPLETED = "en:categories-to-be-completed"
        const val NUTRITION_FACTS_TO_BE_COMPLETED = "en:nutrition-facts-to-be-completed"
        const val LABELS_TO_BE_COMPLETED = "en:labels-to-be-completed"
        const val ORIGINS_TO_BE_COMPLETED = "en:origins-to-be-completed"

        const val INGREDIENTS_COMPLETED = "en:ingredients-completed"

        val INCOMPLETE_TAGS = listOf(
            "to-be-completed",
            "to-be-uploaded",
            "to-be-checked",
            "to-be-validated",
            "to-be-selected",
            "not-selected"
        )
    }

    object Prefix {
        const val PRODUCT_NAME = "product_name_"
        const val INGREDIENTS_TEXT = "ingredients_text_"
    }

    object Suffix {
        const val MODIFIER = "_modifier"
        const val UNIT = "_unit"
        const val VALUE_100G = "_100g"
        const val SERVING = "_serving"
    }

    /**
     * Default values for some fields
     */
    object Defaults {
        const val NUTRITION_DATA_PER_100G = "100g"
        const val NUTRITION_DATA_PER_SERVING = "serving"
        const val DEBUG_BARCODE = "1"
        const val DEFAULT_TAXO_PREFIX = "en"
        const val DEFAULT_LANGUAGE = "en"
        const val STATUS_NOT_OK = "status not ok"
    }

    object Keys {
        const val ATTRIBUTE_GROUPS = "attribute_groups"
        const val NO_NUTRITION_DATA = "no_nutrition_data"

        const val IMAGE_FIELD = "imagefield"

        /**
         * The value can either be [Defaults.NUTRITION_DATA_PER_100G]
         * or [Defaults.NUTRITION_DATA_PER_SERVING]
         */
        const val NUTRITION_DATA_PER = "nutrition_data_per"

        /**
         * Main language name
         * For other languages see [.lcProductNameKey]
         */
        const val PRODUCT_NAME = "product_name"
        const val NUTRIMENTS = "nutriments"

        /**
         * The product language code.
         *
         * Eg: "it", "en", "fr"
         */
        const val LANG = "lang"
        const val IMAGE_FRONT = "image_front"
        const val IMAGE_FRONT_UPLOADED = "image_front_uploaded"
        const val IMAGE_INGREDIENTS = "image_ingredients"
        const val IMAGE_INGREDIENTS_UPLOADED = "image_ingredients_uploaded"
        const val IMAGE_NUTRITION = "image_nutrition"
        const val IMAGE_NUTRITION_UPLOADED = "image_nutrition_uploaded"
        const val BARCODE = "code"
        const val QUANTITY = "quantity"
        const val ADD_BRANDS = "add_brands"
        const val BRANDS = "brands"
        const val LC = "lc"
        const val ECOSCORE = "ecoscore_grade"
        const val PERIODS_AFTER_OPENING = "periods_after_opening"
        const val EMB_CODES = "emb_codes"
        const val LINK = "link"
        const val ADD_PURCHASE = "add_purchase_places"
        const val ADD_STORES = "add_stores"
        const val ADD_COUNTRIES = "add_countries"
        const val SERVING_SIZE = "serving_size"
        const val ADD_TRACES = "add_traces"
        const val CREATOR = "creator"
        const val CREATED_DATE_TIME = "created_t"
        const val MINERALS_TAGS = "minerals_tags"
        const val AMINO_ACIDS_TAGS = "amino_acids_tags"
        const val LAST_MODIFIED_TIME = "last_modified_t"
        const val NOVA_GROUPS = "nova_groups"
        const val ENVIRONMENT_IMPACT_LEVEL_TAGS = "environment_impact_level_tags"
        const val PURCHASE_PLACES = "purchase_places"
        const val OTHER_INFORMATION = "other_information"
        const val TRACES_TAGS = "traces_tags"
        const val TRACES = "traces"
        const val NUTRIMENT_ENERGY = "nutriment_energy"
        const val NUTRIMENT_FAT = "nutriment_fat"
        const val NUTRIMENT_ENERGY_UNIT = "nutriment_energy_unit"
        const val NUTRIMENT_FAT_UNIT = "nutriment_fat_unit"
        const val IMAGE_SMALL_URL = "image_small_url"
        const val IMAGE_NUTRITION_URL = "image_nutrition_url"
        const val IMAGE_FRONT_URL = "image_front_url"
        const val IMAGE_INGREDIENTS_URL = "image_ingredients_url"
        const val IMAGE_PACKAGING_URL = "image_packaging_url"
        const val ADDITIVES_TAGS = "additives_tags"
        const val CATEGORIES_TAGS = "categories_tags"
        const val INGREDIENTS_TEXT = "ingredients_text"
        const val GENERIC_NAME = "generic_name"
        const val INGREDIENTS_MAY_PALM_OIL_N = "ingredients_from_or_that_may_be_from_palm_oil_n"
        const val LAST_MODIFIED_BY = "last_modified_by"
        const val NUTRIENT_LEVELS = "nutrient_levels"
        const val COUNTRIES_TAGS = "countries_tags"
        const val LABELS_HIERARCHY = "labels_hierarchy"
        const val EMB_CODES_TAGS = "emb_codes_tags"
        const val INGREDIENTS_PALM_OIL_N = "ingredients_from_palm_oil_n"
        const val CITIES_TAGS = "cities_tags"
        const val EDITORS_TAGS = "editors_tags"
        const val ALLERGENS_TAGS = "allergens_tags"
        const val NUTRITION_GRADE_FR = "nutrition_grade_fr"
        const val IMAGE_URL = "image_url"
        const val INGREDIENTS_MAY_PALM_OIL_TAGS = "ingredients_that_may_be_from_palm_oil_tags"
        const val STATES_TAGS = "states_tags"
        const val CUSTOMER_SERVICE = "customer_service"
        const val ENVIRONMENT_INFOCARD = "environment_infocard"
        const val WARNING = "warning"
        const val RECYCLING_INSTRUCTIONS_TO_RECYCLE = "recycling_instructions_to_recycle"
        const val RECYCLING_INSTRUCTIONS_TO_DISCARD = "recycling_instructions_to_discard"
        const val CONSERVATION_CONDITIONS = "conservation_conditions"
        const val INGREDIENTS_FROM_PALM_OIL_TAGS = "ingredients_from_palm_oil_tags"
        const val OTHER_NUTRITIONAL_SUBSTANCES_TAGS = "other_nutritional_substances_tags"
        const val VITAMINS_TAGS = "vitamins_tags"
        const val INGREDIENTS_ANALYSIS_TAGS = "ingredients_analysis_tags"
        const val INGREDIENTS = "ingredients"
        const val LABELS_TAGS = "labels_tags"
        const val MANUFACTURING_PLACES = "manufacturing_places"
        const val BRANDS_TAGS = "brands_tags"
        const val ALLERGENS_HIERARCHY = "allergens_hierarchy"
        const val SELECTED_IMAGES = "selected_images"
        const val IMAGES = "images"
        const val USER_ID = "user_id"
        const val USER_PASS = "password"
        const val USER_COMMENT = "comment"
        const val PACKAGING = "packaging"
        const val CATEGORIES = "categories"
        const val LABELS = "labels"
        const val ORIGINS = "origins"
        const val COUNTRIES = "countries"
        const val STORES = "stores"
        const val STATUS = "status"
        const val NUTRITION_GRADE = "nutrition_grades_tags"

        const val SEARCH_TERMS = "search_terms"

        val TYPE_IMAGE = arrayOf(
            ProductImageField.FRONT,
            ProductImageField.INGREDIENTS,
            ProductImageField.NUTRITION,
            ProductImageField.PACKAGING
        )
        const val LANGUAGES_CODES = "languages_codes"
        const val URL = "url"
        const val ALLERGENS = "allergens"

        @JvmStatic
        @Contract(pure = true)
        fun lcProductNameKey(lang: String) = "${Prefix.PRODUCT_NAME}$lang"

        @JvmStatic
        @Contract(pure = true)
        fun lcIngredientsKey(lang: String) = "${Prefix.INGREDIENTS_TEXT}$lang"

        const val NUTRITION_GRADES_TAGS = "nutrition_grades_tags"

        const val OTHER = "other"

        val PRODUCT_COMMON_FIELDS = setOf(
            ALLERGENS_HIERARCHY,
            PRODUCT_NAME,
            GENERIC_NAME,
            IMAGE_SMALL_URL,
            IMAGE_FRONT_URL,
            IMAGE_INGREDIENTS_URL,
            IMAGE_NUTRITION_URL,
            IMAGE_PACKAGING_URL,
            IMAGE_URL,
            SELECTED_IMAGES,
            LANGUAGES_CODES,
            VITAMINS_TAGS,
            MINERALS_TAGS,
            AMINO_ACIDS_TAGS,
            OTHER_NUTRITIONAL_SUBSTANCES_TAGS,
            URL,
            NUTRIMENTS,
            BARCODE,
            TRACES_TAGS,
            INGREDIENTS_MAY_PALM_OIL_TAGS,
            BRANDS_TAGS,
            TRACES,
            CATEGORIES_TAGS,
            INGREDIENTS_TEXT,
            INGREDIENTS_FROM_PALM_OIL_TAGS,
            ADDITIVES_TAGS,
            SERVING_SIZE,
            ALLERGENS_TAGS,
            ALLERGENS,
            ORIGINS,
            STORES,
            NUTRITION_GRADE_FR,
            NUTRITION_GRADES_TAGS,
            NUTRIENT_LEVELS,
            ECOSCORE,
            COUNTRIES,
            COUNTRIES_TAGS,
            BRANDS,
            PACKAGING,
            LABELS_TAGS,
            LABELS_HIERARCHY,
            CITIES_TAGS,
            QUANTITY,
            INGREDIENTS_PALM_OIL_N,
            LINK,
            EMB_CODES_TAGS,
            STATES_TAGS,
            CREATOR,
            CREATED_DATE_TIME,
            LAST_MODIFIED_TIME,
            LAST_MODIFIED_BY,
            EDITORS_TAGS,
            NOVA_GROUPS,
            LANG,
            PURCHASE_PLACES,
            NUTRITION_DATA_PER,
            NO_NUTRITION_DATA,
            OTHER,
            OTHER_INFORMATION,
            CONSERVATION_CONDITIONS,
            RECYCLING_INSTRUCTIONS_TO_DISCARD,
            RECYCLING_INSTRUCTIONS_TO_RECYCLE,
            WARNING,
            CUSTOMER_SERVICE,
            ENVIRONMENT_INFOCARD,
            ENVIRONMENT_IMPACT_LEVEL_TAGS,
            INGREDIENTS_ANALYSIS_TAGS,
            INGREDIENTS,
            STATES_TAGS
        )

        val PRODUCT_LOCAL_FIELDS = setOf(
            PRODUCT_NAME to true,
            GENERIC_NAME to true,
            INGREDIENTS_TEXT to true,
            OTHER_INFORMATION to true,
            CONSERVATION_CONDITIONS to true,
            RECYCLING_INSTRUCTIONS_TO_DISCARD to true,
            RECYCLING_INSTRUCTIONS_TO_RECYCLE to true,
            WARNING to true,
            ATTRIBUTE_GROUPS to false,
            CUSTOMER_SERVICE to true,
            IMAGE_FRONT_URL to true,
            IMAGE_INGREDIENTS_URL to true,
            IMAGE_NUTRITION_URL to true,
            IMAGE_PACKAGING_URL to true
        )

        val PRODUCT_IMAGES_FIELDS = setOf(
            PRODUCT_NAME, GENERIC_NAME, BARCODE, LANG, IMAGE_SMALL_URL, IMAGES,
            IMAGE_FRONT_URL, IMAGE_INGREDIENTS_URL, IMAGE_NUTRITION_URL,
            IMAGE_PACKAGING_URL, IMAGE_URL, SELECTED_IMAGES
        )

        val PRODUCT_SEARCH_FIELDS = setOf(
            BRANDS,
            PRODUCT_NAME,
            IMAGE_SMALL_URL,
            QUANTITY,
            NUTRITION_GRADE_FR,
            BARCODE,
            ECOSCORE,
            NOVA_GROUPS
        )
    }

    fun getAllFields(langCode: String): String {
        val allFields = Keys.PRODUCT_COMMON_FIELDS
        val fieldsToLocalize = Keys.PRODUCT_LOCAL_FIELDS

        val fieldsSet = allFields.toMutableSet()
        fieldsToLocalize.forEach { (field, shouldAddEn) ->
            fieldsSet += "${field}_$langCode"
            if (shouldAddEn) fieldsSet += "${field}_en"
        }
        return fieldsSet.joinToString(",")
    }
}
