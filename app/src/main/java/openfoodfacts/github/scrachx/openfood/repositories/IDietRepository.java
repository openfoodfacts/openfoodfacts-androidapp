
package openfoodfacts.github.scrachx.openfood.repositories;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;

import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.Single;
import openfoodfacts.github.scrachx.openfood.models.Diet;
import openfoodfacts.github.scrachx.openfood.models.DietName;
import openfoodfacts.github.scrachx.openfood.models.DietIngredients;
import openfoodfacts.github.scrachx.openfood.models.Ingredient;
import openfoodfacts.github.scrachx.openfood.models.IngredientName;

/**
 * Created by dobriseb on 1018.10.17.
 */

public interface IDietRepository {

    Single<List<Diet>> getDiets(Boolean refresh);

    Single<List<Ingredient>> getIngredients(Boolean refresh);

    Single<List<DietIngredients>> getDietIngredients(Boolean refresh);

    void saveDiets(List<Diet> diets);

    void saveDiet(Diet diet);

    void saveIngredients(List<Ingredient> ingredients);

    void saveIngredient(Ingredient ingredient);

    void saveDietIngredientsList(List<DietIngredients> dietIngredientsList);

    void saveDietIngredients(DietIngredients dietIngredients);

    void setDietEnabled(String dietTag, Boolean isEnabled);

    Single<DietName> getDietNameByTagAndLanguageCode(String dietTag, String languageCode);

    Single<DietName> getDietNameByTagAndDefaultLanguageCode(String dietTag);

    List<Diet> getEnabledDiets();

    Single<List<DietName>> getDietNameByEnabledAndLanguageCode(Boolean isEnabled, String languageCode);

    Single<List<DietName>> getDietsByLanguageCode(String languageCode);

    Single<IngredientName> getIngredientNameByTagAndLanguageCode(String ingredientTag, String languageCode);

    Single<IngredientName> getIngredientNameByTagAndDefaultLanguageCode(String ingredientTag);

    Single<List<IngredientName>> getIngredientNameByLanguageCode(String languageCode);

    Diet getDietByTag(String tag);

    Single<List<IngredientName>> getIngredientsByLanguageCode(String languageCode);

    Diet getDietByNameAndLanguageCode(String name, String languageCode);

    DietName getDietNameByNameAndLanguageCode(String name, String languageCode);

    DietName getDietNameByDietTagAndLanguageCode(String dietTag, String languageCode);

    void addDiet(String name, String description, boolean isEnabled, String languageCode);

    Ingredient getIngredientByTag(String tag);

    Ingredient getIngredientByNameAndLanguageCode(String name, String languageCode);

    IngredientName getIngredientNameByNameAndLanguageCode(String name, String languageCode);

    IngredientName getIngredientNameByIngredientTagAndLanguageCode(String ingredientTag, String languageCode);

    void addIngredient(String name, String languageCode);

    void addDietIngredients(String dietName, String ingredientName, String languageCode, long state);

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

    List<SpannableStringBuilder> getColoredSpannableStringBuilderFromIngredientsDiet(List<String> ingredients, String dietTag, String languageCode);

    SpannableStringBuilder getColoredSpannableStringBuilderFromSpannableIngredients(Pattern INGREDIENT_PATTERN, SpannableStringBuilder txtIngredients);

    SpannableStringBuilder getColoredSpannableStringBuilderFromSpannableStringBuilderIngredients(SpannableStringBuilder ssbIngredients);

    List<String> getIngredientsListFromIngredientsText (String ingredientsText, boolean preserveAllSign);

    SpannableStringBuilder getSpannableStringBuilderFromDietIngredientsAndLanguageCode(List<DietIngredients> dietIngredientsList, String languageCode);
}