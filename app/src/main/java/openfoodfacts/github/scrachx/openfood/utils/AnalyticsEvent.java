package openfoodfacts.github.scrachx.openfood.utils;

public class AnalyticsEvent {
    public String category;
    public String action;
    public String name;
    public Float value;

    private AnalyticsEvent(String category, String action, String name, Float value) {
        this.category = category;
        this.action = action;
        this.name = name;
        this.value = value;
    }

    public void track() {
        AnalyticsService.getInstance().trackEvent(this);
    }

    public static AnalyticsEvent ScannedBarcode(String barcode) {
        return new AnalyticsEvent("scanner", "scanned", barcode, null);
    }

    public static AnalyticsEvent ScannedBarcodeResultExpanded(String barcode) {
        return new AnalyticsEvent("scanner", "result-expanded", barcode, null);
    }

    public static AnalyticsEvent AllergenAlertCreated(String allergenCode) {
        return new AnalyticsEvent("allergen-alerts", "created", allergenCode, null);
    }

    public static AnalyticsEvent ProductCreated(String barcode) {
        return new AnalyticsEvent("products", "created", barcode, null);
    }

    public static AnalyticsEvent ProductEdited(String barcode) {
        return new AnalyticsEvent("products", "edited", barcode, null);
    }

    public static AnalyticsEvent ProductIngredientsPictureEdited(String barcode) {
        return new AnalyticsEvent("products", "edited-ingredients-picture", barcode, null);
    }

    public static AnalyticsEvent UserLogin() {
        return new AnalyticsEvent("user-account", "login", null, null);
    }

    public static AnalyticsEvent UserLogout() {
        return new AnalyticsEvent("user-account", "logout", null, null);
    }

    public static AnalyticsEvent RobotoffLoginPrompt() {
        return new AnalyticsEvent("user-account", "login-prompt", "robotoff", null);
    }

    public static AnalyticsEvent RobotoffLoggedInAfterPrompt() {
        return new AnalyticsEvent("user-account", "logged-in-after-prompt", "robotoff", null);
    }

    public static AnalyticsEvent ShoppingListCreated() {
        return new AnalyticsEvent("shopping-lists", "created", null, null);
    }

    public static AnalyticsEvent ShoppingListExported() {
        return new AnalyticsEvent("shopping-lists", "exported", null, null);
    }

    public static AnalyticsEvent IngredientAnalysisEnabled(String name) {
        return new AnalyticsEvent("ingredient-analysis", "enabled", name, null);
    }

    public static AnalyticsEvent IngredientAnalysisDisabled(String name) {
        return new AnalyticsEvent("ingredient-analysis", "disabled", name, null);
    }

    public static AnalyticsEvent AddProductToComparison(String barcode) {
        return new AnalyticsEvent("products", "compare-add", barcode, null);
    }

    public static AnalyticsEvent CompareProducts(int count) {
        return new AnalyticsEvent("products", "compare-multiple", null, (float) count);
    }
}
