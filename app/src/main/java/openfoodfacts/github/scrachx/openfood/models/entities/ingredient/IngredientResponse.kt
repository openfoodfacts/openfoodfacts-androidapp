package openfoodfacts.github.scrachx.openfood.models.entities.ingredient

import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse

/**
 * Intermediate class between [IngredientsWrapper] and [Ingredient]
 *
 * @author dobriseb 2018-12-21 inspired by IngredientResponse
 */
class IngredientResponse(
        private var uniqueIngredientID: String,
        private var names: Map<String, String>,
        private var parents: List<String>,
        private var children: List<String>,
        private val wikiDataCode: String?
) : EntityResponse<Ingredient> {

    /**
     * Converts an IngredientResponse object into a new Ingredient object.
     *
     * @return The newly constructed Ingredient object.
     */
    override fun map(): Ingredient {
        return if (wikiDataCode != null) {
            Ingredient(
                    uniqueIngredientID,
                    names.map { IngredientName(uniqueIngredientID, it.key, it.value) },
                    parents.map { IngredientsRelation(it, uniqueIngredientID) },
                    children.map { IngredientsRelation(uniqueIngredientID, it) },
                    wikiDataCode
            )
        } else {
            Ingredient(
                    uniqueIngredientID,
                    names.map { IngredientName(uniqueIngredientID, it.key, it.value) },
                    parents.map { IngredientsRelation(it, uniqueIngredientID) },
                    children.map { IngredientsRelation(uniqueIngredientID, it) },
            )
        }
    }
}