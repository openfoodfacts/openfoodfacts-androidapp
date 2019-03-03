package openfoodfacts.github.scrachx.openfood.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

/**
 * Intermediate class between {@link IngredientsWrapper} and {@link Ingredient}
 *
 * @author dobriseb 2018-12-21 inspired by IngredientResponse
 */

public class IngredientResponse {

    private String uniqueIngredientID;

    private Map<String, String> names;
    private Map<String, String> parents;
    private Map<String, String> children;

    private String wikiDataCode;
    private Boolean isWikiDataIdPresent;

    /**
     * Constructor.
     *
     * @param uniqueIngredientId Unique ID of the ingredient
     * @param names              Map of key: Country code, value: Translated name of ingredient.
     * @param wikiDataCode       Code to look up ingredient in wikidata
     */
    public IngredientResponse(String uniqueIngredientId, Map<String, String> names, Map<String, String> parents, Map<String, String> children, String wikiDataCode) {
        this.uniqueIngredientID = uniqueIngredientId;
        this.names = names;
        this.parents = parents;
        this.children = children;
        this.wikiDataCode = wikiDataCode;
        this.isWikiDataIdPresent = true;
    }

    /**
     * Constructor.
     *
     * @param uniqueIngredientId Unique ID of the ingredient
     * @param names              Map of key: Country code, value: Translated name of ingredient.
     */
    public IngredientResponse(String uniqueIngredientId, Map<String, String> names, Map<String, String> parents, Map<String, String> children) {
        this.uniqueIngredientID = uniqueIngredientId;
        this.names = names;
        this.parents = parents;
        this.children = children;
        this.isWikiDataIdPresent = false;
    }

    /**
     * Converts an IngredientResponse object into a new Ingredient object.
     *
     * @return The newly constructed Ingredient object.
     */
    public Ingredient map() {
        Ingredient ingredient;
        if (isWikiDataIdPresent) {
            ingredient = new Ingredient(uniqueIngredientID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), wikiDataCode);
        } else {
            ingredient = new Ingredient(uniqueIngredientID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        for (Map.Entry<String, String> name : names.entrySet()) {
            ingredient.getNames()
                    .add(new IngredientName(ingredient.getTag(), name.getKey(), name.getValue()));
        }
        for (Map.Entry<String, String> parent : parents.entrySet()) {
            ingredient.getParents()
                    .add(new IngredientsRelation(parent.getValue(), ingredient.getTag()));
        }
        for (Map.Entry<String, String> child : children.entrySet()) {
            ingredient.getChildren()
                    .add(new IngredientsRelation(ingredient.getTag(), child.getValue()));
        }
        return ingredient;
    }

    public String getUniqueIngredientID() {
        return uniqueIngredientID;
    }

    public void setUniqueIngredientID(String uniqueIngredientID) {
        this.uniqueIngredientID = uniqueIngredientID;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(Map<String, String> names) {
        this.names = names;
    }

    public Map<String, String> getParents() {
        return parents;
    }

    public void setParents(Map<String, String> parents) {
        this.parents = parents;
    }

    public Map<String, String> getChildren() {
        return children;
    }

    public void setChildren(Map<String, String> children) {
        this.children = children;
    }

}
