package openfoodfacts.github.scrachx.openfood.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Intermediate class between {@link IngredientsWrapper} and {@link Ingredient}
 *
 * @author dobriseb 2018-12-21 inspired by IngredientResponse
 */
public class IngredientResponse {
    private String uniqueIngredientID;
    private Map<String, String> names;
    private List<String> parents;
    private List<String> children;
    private String wikiDataCode;
    private Boolean isWikiDataIdPresent;

    /**
     * Constructor.
     *
     * @param uniqueIngredientId Unique ID of the ingredient
     * @param names Map of key: Country code, value: Translated name of ingredient.
     * @param wikiDataCode Code to look up ingredient in wikidata
     */
    public IngredientResponse(String uniqueIngredientId, Map<String, String> names, List<String> parents, List<String> children, String wikiDataCode) {
        Log.i("INFO", "IngredientResponse(" + uniqueIngredientId + ", " + names.toString() + ", " + wikiDataCode + ")");
        this.uniqueIngredientID = uniqueIngredientId;
        this.names = names;
        this.parents = parents;
        this.children = children;
        this.wikiDataCode = wikiDataCode;
        this.isWikiDataIdPresent = true;
    }

    /**
     * Converts an IngredientResponse object into a new Ingredient object.
     *
     * @return The newly constructed Ingredient object.
     */
    public Ingredient map() {
        Log.i("INFO", "IngredientResponse.map()");
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
        for (String parentValue : parents) {
            ingredient.getParents()
                .add(new IngredientsRelation(parentValue, ingredient.getTag()));
        }
        for (String childValue : children) {
            ingredient.getChildren()
                .add(new IngredientsRelation(ingredient.getTag(), childValue));
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

    public List<String> getParents() {
        return parents;
    }

    public void setParents(List<String> parents) {
        this.parents = parents;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }
}
