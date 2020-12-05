package openfoodfacts.github.scrachx.openfood.models.entities.ingredient

import android.util.Log
import java.util.*

/**
 * Intermediate class between [IngredientsWrapper] and [Ingredient]
 *
 * @author dobriseb 2018-12-21 inspired by IngredientResponse
 */
class IngredientResponse(uniqueIngredientId: String, names: Map<String?, String?>, parents: List<String>, children: List<String>, wikiDataCode: String) {
    var uniqueIngredientID: String
    var names: Map<String?, String?>
    var parents: List<String>
    var children: List<String>
    private val isWikiDataIdPresent: Boolean
    private val wikiDataCode: String

    /**
     * Converts an IngredientResponse object into a new Ingredient object.
     *
     * @return The newly constructed Ingredient object.
     */
    fun map(): Ingredient {
        Log.i("INFO", "IngredientResponse.map()")
        val ingredient: Ingredient = if (isWikiDataIdPresent) {
            Ingredient(uniqueIngredientID, ArrayList(), ArrayList(), ArrayList(), wikiDataCode)
        } else {
            Ingredient(uniqueIngredientID, ArrayList(), ArrayList(), ArrayList())
        }
        names.forEach { (key, value) ->
            ingredient.names.add(IngredientName(ingredient.tag, key, value))
        }
        parents.forEach { parentValue ->
            ingredient.parents.add(IngredientsRelation(parentValue, ingredient.tag))
        }
        children.forEach { childValue ->
            ingredient.children.add(IngredientsRelation(ingredient.tag, childValue))
        }
        return ingredient
    }

    /**
     * Constructor.
     *
     * @param uniqueIngredientId Unique ID of the ingredient
     * @param names Map of key: Country code, value: Translated name of ingredient.
     * @param wikiDataCode Code to look up ingredient in wikidata
     */
    init {
        Log.i("INFO", "IngredientResponse($uniqueIngredientId, $names, $wikiDataCode)")
        uniqueIngredientID = uniqueIngredientId
        this.names = names
        this.parents = parents
        this.children = children
        this.wikiDataCode = wikiDataCode
        isWikiDataIdPresent = true
    }
}