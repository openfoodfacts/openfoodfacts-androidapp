package openfoodfacts.github.scrachx.openfood.analytics

enum class AnalyticsView(val path: String) {
    Scanner("scanner"),
    ProductEditOverview("products/edit/overview"),
    ProductEditIngredients("products/edit/ingredients"),
    ProductEditNutritionFacts("products/edit/nutrition_facts");
}
