
package openfoodfacts.github.scrachx.openfood.repositories;

import android.text.SpannableStringBuilder;

import java.util.List;
import java.util.regex.Pattern;

import openfoodfacts.github.scrachx.openfood.models.Diet;
import openfoodfacts.github.scrachx.openfood.models.DietIngredients;
import openfoodfacts.github.scrachx.openfood.models.DietName;
import openfoodfacts.github.scrachx.openfood.models.Ingredient;
import openfoodfacts.github.scrachx.openfood.models.IngredientName;
import openfoodfacts.github.scrachx.openfood.models.Product;

/**
 * Created by dobriseb on 1018.10.17.
 */

public interface IDietRepository {

    void saveDiets(List<Diet> diets);

    void saveDiet(Diet diet);

    void saveDietIngredients(DietIngredients dietIngredients);

    void setDietEnabled(String dietTag, Boolean isEnabled);

    List<Diet> getEnabledDiets();

    Diet getDietByTag(String tag);

    Diet getDietByNameAndLanguageCode(String name, String languageCode);

    DietName getDietNameByNameAndLanguageCode(String name, String languageCode);

    DietName getDietNameByDietTagAndLanguageCode(String dietTag, String languageCode);

    void addDiet(String name, String description, boolean isEnabled, String languageCode);

    Ingredient getIngredientByTag(String tag);

    Ingredient getIngredientByNameAndLanguageCode(String name, String languageCode);

    IngredientName getIngredientNameByNameAndLanguageCode(String name, String languageCode);

    IngredientName getIngredientNameByIngredientTagAndLanguageCode(String ingredientTag, String languageCode);

    void addIngredient(String name, String languageCode);

    void addDietIngredientsByTags(String dietTag, String ingredientTag, long state);

    void addDietIngredients(String dietTag, String ingredientName, String languageCode, long state);

    List<DietIngredients> getDietIngredientsListByDietTagAndState(String dietTag, long state);

    List<DietIngredients> getDietIngredientsListByDietTag(String dietTag);

    List<Ingredient> getIngredientsLinkedToDietByDietTagAndState(String dietTag, long state);

    List<Ingredient> getIngredientsLinkedToDietByDietTag(String dietTag);

    List<Ingredient> getIngredientsLinkedToDietByDietNameLanguageCodeAndState(String dietName, String languageCode, long state);

    List<Ingredient> getIngredientsLinkedToDietByDietNameAndLanguageCode(String dietName, String languageCode);

    List<IngredientName> getIngredientNamesByIngredientsAndLanguageCode(List<Ingredient> ingredients, String languageCode);

    String getSortedIngredientNameStringByDietTagStateAndLanguageCode(String dietTag, long state, String languageCode);

    List<String> getIngredientNameLinkedToEnabledDietsByLanguageCode(long state, String languageCode);

    long stateFromIngredientTagDietTag(String ingredientTag, String dietTag);

    List<SpannableStringBuilder> getColoredSSBFromProductAndDiet(Product product, String dietTag);

    List<SpannableStringBuilder> getColoredSSBFromIngredientsDiet(List<String> ingredients, String dietTag, String languageCode);

    SpannableStringBuilder getColoredSpannableStringBuilderFromSpannableIngredients(Pattern INGREDIENT_PATTERN, SpannableStringBuilder txtIngredients);

    SpannableStringBuilder getColoredSSBFromSSBIngredients(SpannableStringBuilder ssbIngredients, String languageCode);

    SpannableStringBuilder getColoredSSBFromSSBAndProduct(SpannableStringBuilder ssbIngredients, Product product);

    List<String> getIngredientsListFromIngredientsText (String ingredientsText, boolean preserveAllSign);

    String exportDietToJson(Diet diet);
}