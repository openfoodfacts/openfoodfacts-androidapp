package openfoodfacts.github.scrachx.openfood.utils;

public enum AnalyticsView {
    SCANNER("scanner"),
    PRODUCT_EDIT_OVERVIEW("products/edit/overview"),
    PRODUCT_EDIT_INGREDIENTS("products/edit/ingredients"),
    PRODUCT_EDIT_NUTRITION_FACTS("products/edit/nutrition_facts");
    public String path;

    AnalyticsView(String path) {
        this.path = path;
    }
}
