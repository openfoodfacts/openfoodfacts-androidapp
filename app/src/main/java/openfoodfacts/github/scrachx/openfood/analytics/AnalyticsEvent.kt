package openfoodfacts.github.scrachx.openfood.analytics

import openfoodfacts.github.scrachx.openfood.models.Barcode

sealed class AnalyticsEvent(val category: String, val action: String, val name: String?, val value: Float?) {

    object ProductSearch :
        AnalyticsEvent("search", "completed", null, null)

    object ProductSearchStart :
        AnalyticsEvent("search", "started", null, null)

    data class BarcodeDecoder(val success: Boolean = false, val duration: Float = 0f) :
        AnalyticsEvent("scanner", "scanning", success.toString(), duration)

    data class ScannedBarcode(val barcode: String) :
        AnalyticsEvent("scanner", "scanned", barcode, null)

    data class ScannedBarcodeResultExpanded(val barcode: String?) :
        AnalyticsEvent("scanner", "result-expanded", barcode, null)

    data class AllergenAlertCreated(val allergenTag: String) :
        AnalyticsEvent("allergen-alerts", "created", allergenTag, null)

    data class ProductCreated(val barcode: String?) :
        AnalyticsEvent("products", "created", barcode, null)

    data class ProductEdited(val barcode: String?) :
        AnalyticsEvent("products", "edited", barcode, null)

    data class ProductIngredientsPictureEdited(val barcode: String?) :
        AnalyticsEvent("products", "edited-ingredients-picture", barcode, null)

    object UserLogin :
        AnalyticsEvent("user-account", "login", null, null)

    object UserLogout :
        AnalyticsEvent("user-account", "logout", null, null)

    object RobotoffLoginPrompt :
        AnalyticsEvent("user-account", "login-prompt", "robotoff", null)

    object RobotoffLoggedInAfterPrompt :
        AnalyticsEvent("user-account", "logged-in-after-prompt", "robotoff", null)

    object ShoppingListCreated :
        AnalyticsEvent("shopping-lists", "created", null, null)

    data class ShoppingListProductAdded(val barcode: String) :
        AnalyticsEvent("shopping-lists", "add_product", barcode, null)

    data class ShoppingListProductRemoved(val barcode: String) :
        AnalyticsEvent("shopping-lists", "remove_product", barcode, null)

    object ShoppingListShared :
        AnalyticsEvent("shopping-lists", "shared", null, null)

    object ShoppingListExported :
        AnalyticsEvent("shopping-lists", "exported", null, null)

    data class IngredientAnalysisEnabled(val type: String) :
        AnalyticsEvent("ingredient-analysis", "enabled", type, null)

    data class IngredientAnalysisDisabled(val type: String) :
        AnalyticsEvent("ingredient-analysis", "disabled", type, null)

    data class AddProductToComparison(val barcode: Barcode) :
        AnalyticsEvent("products", "compare-add", barcode.raw, null)

    data class CompareProducts(val count: Float) :
        AnalyticsEvent("products", "compare-multiple", null, count)
}
