package openfoodfacts.github.scrachx.openfood.repositories;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Single;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.Diet;
import openfoodfacts.github.scrachx.openfood.models.DietDao;
import openfoodfacts.github.scrachx.openfood.models.DietName;
import openfoodfacts.github.scrachx.openfood.models.DietNameDao;
import openfoodfacts.github.scrachx.openfood.models.Ingredient;
import openfoodfacts.github.scrachx.openfood.models.IngredientDao;
import openfoodfacts.github.scrachx.openfood.models.IngredientName;
import openfoodfacts.github.scrachx.openfood.models.IngredientNameDao;
import openfoodfacts.github.scrachx.openfood.models.DietIngredients;
import openfoodfacts.github.scrachx.openfood.models.DietIngredientsDao;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

/**
 * Created by dobriseb on 2018.10.17.
 */

public class DietRepository implements IDietRepository {

    private static final String DEFAULT_LANGUAGE = "en";

    private static IDietRepository instance;

    private Database db;
    private DietDao dietDao;
    private DietNameDao dietNameDao;
    private IngredientDao ingredientDao;
    private IngredientNameDao ingredientNameDao;
    private DietIngredientsDao dietIngredientsDao;

    private HashMap<Integer, String> colors = new HashMap<Integer, String>();

    public static IDietRepository getInstance() {
        if (instance == null) {
            instance = new DietRepository();
        }
        return instance;
    }

    private DietRepository() {
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        db = daoSession.getDatabase();
        dietDao = daoSession.getDietDao();
        dietNameDao = daoSession.getDietNameDao();
        ingredientDao = daoSession.getIngredientDao();
        ingredientNameDao = daoSession.getIngredientNameDao();
        dietIngredientsDao = daoSession.getDietIngredientsDao();

        colors.put(-1, "#ff0000");
        colors.put(0, "#ff9900");
        colors.put(1, "#00b400");
        colors.put(2, "#393939");
    }

    /**
     * Load diets from (the server or) local database
     *
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     *                Pour le moment, pas de question a se poser, les données ne sont que locales.
     */
    @Override
    public Single<List<Diet>> getDiets(Boolean refresh) {
        /*if (refresh || tableIsEmpty(dietDao)) {
            return productApi.getDiets()
                    .map(DietsWrapper::map);
        } else {*/
        return Single.fromCallable(() -> dietDao.loadAll());
        /*}*/
    }

    /**
     * Load ingredients from (the server or) local database
     *
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     * @return The ingredients in the product.
     * Pour le moment, pas de question a se poser, les données ne sont que locales.
     */
    @Override
    public Single<List<Ingredient>> getIngredients(Boolean refresh) {
        /*if (refresh || tableIsEmpty(ingredientDao)) {
            return productApi.getIngredients()
                    .map(IngredientsWrapper::map);
        } else {*/
        return Single.fromCallable(() -> ingredientDao.loadAll());
        /*}*/
    }

    /**
     * Load dietIngredients (from the server or) local database
     *
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     *                Pour le moment, pas de question a se poser, les données ne sont que locales.
     */
    @Override
    public Single<List<DietIngredients>> getDietIngredients(Boolean refresh) {
        /*if (refresh || tableIsEmpty(dietIngredientsDao)) {
            return productApi.getDietIngredients()
                    .map(DietIngredientsWrapper::map);
        } else {*/
        return Single.fromCallable(() -> dietIngredientsDao.loadAll());
        /*}*/
    }

    /**
     * Diets saving to local database
     * <p>
     * Diet and DietName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveDiets(List<Diet> diets) {
        //Log.i("INFO", "Début de saveDiets");
        db.beginTransaction();
        try {
            for (Diet diet : diets) {
                dietDao.insertOrReplace(diet);
                for (DietName dietName : diet.getNames()) {
                    dietNameDao.insertOrReplace(dietName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        //Log.i("INFO", "Fin de saveDiets");
    }

    /**
     * Diet saving to local database
     */
    @Override
    public void saveDiet(Diet diet) {
        //Log.i("INFO", "Début de saveDiet");
        List<Diet> diets = new ArrayList<>();
        diets.add(diet);
        saveDiets(diets);
        //Log.i("INFO", "Fin de saveDiet");
    }

    /**
     * Ingredients saving to local database
     * <p>
     * Ingredient and IngredientName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveIngredients(List<Ingredient> ingredients) {
        //Log.i("INFO", "Début de saveIngredients");
        db.beginTransaction();
        try {
            for (Ingredient ingredient : ingredients) {
                ingredientDao.insertOrReplace(ingredient);
                for (IngredientName ingredientName : ingredient.getNames()) {
                    ingredientNameDao.insertOrReplace(ingredientName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        //Log.i("INFO", "Fin de saveIngredients");
    }

    /**
     * Ingredient saving to local database
     */
    @Override
    public void saveIngredient(Ingredient ingredient) {
        //Log.i("INFO", "Début de saveIngredient");
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);
        saveIngredients(ingredients);
        //Log.i("INFO", "Début de saveIngredient");
    }

    /**
     * DietIngredientsList saving to local database
     */
    @Override
    public void saveDietIngredientsList(List<DietIngredients> dietIngredientsList) {
        //Log.i("INFO", "Début de saveDietIngredientsList");
        dietIngredientsDao.insertOrReplaceInTx(dietIngredientsList);
        //Log.i("INFO", "Début de saveDietIngredientsList");
    }

    /**
     * DietIngredients saving to local database
     */
    @Override
    public void saveDietIngredients(DietIngredients dietIngredients) {
        //Log.i("INFO", "Début de saveDietIngredients");
        dietIngredientsDao.insertOrReplace(dietIngredients);
        //Log.i("INFO", "Début de saveDietIngredients");
    }

    /**
     * Changes enabled field of diet and updates it.
     *
     * @param isEnabled depends on whether user selected or unselected the diet
     * @param dietTag   is unique Id of diet
     */
    @Override
    public void setDietEnabled(String dietTag, Boolean isEnabled) {
        //Log.i("INFO", "Début de setDietEnabled avec " + dietTag + "-" + isEnabled);
        Diet diet = dietDao.queryBuilder()
                .where(DietDao.Properties.Tag.eq(dietTag))
                .unique();

        if (diet != null) {
            diet.setEnabled(isEnabled);
            dietDao.update(diet);
        }
        //Log.i("INFO", "Fin de setDietEnabled avec " + dietTag + "-" + isEnabled);
    }

    /**
     * Loads translated diet from the local database by unique tag of diet and language code
     *
     * @param dietTag      is a unique Id of diet
     * @param languageCode is a 2-digit language code
     */
    @Override
    public Single<DietName> getDietNameByTagAndLanguageCode(String dietTag, String languageCode) {
        //Log.i("INFO", "Début de getDietNameByTagAndLanguageCode avec " + dietTag + "-" + languageCode);
        return Single.fromCallable(() -> {
            DietName dietName = dietNameDao.queryBuilder()
                    .where(
                            DietNameDao.Properties.DietTag.eq(dietTag),
                            DietNameDao.Properties.LanguageCode.eq(languageCode)
                    ).unique();
            //Log.i("INFO", "Fin de getDietNameByTagAndLanguageCode : " + dietName.toString());
            return dietName != null ? dietName : new DietName();
        });
    }

    /**
     * Loads translated diet from the local database by unique tag of diet and default language code
     *
     * @param dietTag is a unique Id of diet
     */
    @Override
    public Single<DietName> getDietNameByTagAndDefaultLanguageCode(String dietTag) {
        return getDietNameByTagAndLanguageCode(dietTag, DEFAULT_LANGUAGE);
    }

    /**
     * Load diets which user selected earlier (i.e user's diets)
     */
    @Override
    public List<Diet> getEnabledDiets() {
        return dietDao.queryBuilder().where(DietDao.Properties.Enabled.eq("true")).list();
    }

    /**
     * Loads translated ingredient from the local database by unique tag of ingredient and language code
     *
     * @param ingredientTag is a unique Id of dietIngredients
     * @param languageCode  is a 2-digit language code
     */
    @Override
    public Single<IngredientName> getIngredientNameByTagAndLanguageCode(String ingredientTag, String languageCode) {
        //Log.i("INFO", "Début de getIngredientNameByTagAndLanguageCode avec " + ingredientTag + "-" + languageCode);
        return Single.fromCallable(() -> {
            IngredientName ingredientName = ingredientNameDao.queryBuilder()
                    .where(
                            IngredientNameDao.Properties.IngredientTag.eq(ingredientTag),
                            IngredientNameDao.Properties.LanguageCode.eq(languageCode)
                    ).unique();
            //Log.i("INFO", "Fin de getIngredientNameByTagAndLanguageCode : " + ingredientName.toString());
            return ingredientName != null ? ingredientName : new IngredientName();
        });
    }

    /**
     * Loads ingredientNames from the local database by unique tag of ingredient and default language code
     *
     * @param ingredientTag is a unique Id of ingredient
     */
    @Override
    public Single<IngredientName> getIngredientNameByTagAndDefaultLanguageCode(String ingredientTag) {
        return getIngredientNameByTagAndLanguageCode(ingredientTag, DEFAULT_LANGUAGE);
    }

    /**
     * Loads ingredientNames from the local database by language code.
     *
     * @param languageCode is a unique Id of ingredient
     */
    @Override
    public Single<List<IngredientName>> getIngredientNameByLanguageCode(String languageCode) {
        //Log.i("INFO", "Début de getIngredientNameByLanguageCode avec " + languageCode);
        return Single.fromCallable(() -> {
            List<IngredientName> ingredientNames = ingredientNameDao.queryBuilder().where(IngredientNameDao.Properties.LanguageCode.eq(languageCode)).list();
            //Log.i("INFO", "Fin de getIngredientNameByLanguageCode : " + ingredientNames.toString());
            return (ingredientNames != null) ? ingredientNames : new ArrayList<>();
        });
    }

    /**
     * Loads translated and selected/unselected diets.
     *
     * @param isEnabled    depends on whether diet was selected or unselected by user
     * @param languageCode is a 2-digit language code
     */
    @Override
    public Single<List<DietName>> getDietNameByEnabledAndLanguageCode(Boolean isEnabled, String languageCode) {
        //Log.i("INFO", "Début de getDietNameByEnabledAndLanguageCode avec " + isEnabled + "-" + languageCode);
        return Single.fromCallable(() -> {
            List<Diet> diets = dietDao.queryBuilder().where(DietDao.Properties.Enabled.eq(isEnabled)).list();
            if (diets != null) {
                List<DietName> dietNames = new ArrayList<>();
                for (Diet diet : diets) {
                    DietName name = dietNameDao.queryBuilder()
                            .where(
                                    DietNameDao.Properties.DietTag.eq(diet.getTag()),
                                    DietNameDao.Properties.LanguageCode.eq(languageCode)
                            ).unique();

                    if (name != null) {
                        dietNames.add(name);
                    }
                }
                //Log.i("INFO", "Début de getDietNameByEnabledAndLanguageCode : " + dietNames.toString());
                return dietNames;
            }
            //Log.i("INFO", "Début de getDietNameByEnabledAndLanguageCode : null");
            return new ArrayList<>();
        });
    }

    /**
     * Loads all translated diets.
     *
     * @param languageCode is a 2-digit language code
     */
    @Override
    public Single<List<DietName>> getDietsByLanguageCode(String languageCode) {
        //Log.i("INFO", "Début de getDietsByLanguageCode avec " + languageCode);
        return Single.fromCallable(() ->
                dietNameDao.queryBuilder()
                        .where(DietNameDao.Properties.LanguageCode.eq(languageCode))
                        .list());
    }

    /**
     * Loads all translated ingredients.
     *
     * @param languageCode is a 2-digit language code
     */
    @Override
    public Single<List<IngredientName>> getIngredientsByLanguageCode(String languageCode) {
        //Log.i("INFO", "Début de getIngredientsByLanguageCode : " + languageCode);
        return Single.fromCallable(() ->
                ingredientNameDao.queryBuilder()
                        .where(IngredientNameDao.Properties.LanguageCode.eq(languageCode))
                        .list());
    }

    /**
     * Trouve la diet correspondante à un Tag.
     *
     * @param tag le nom
     */
    @Override
    public Diet getDietByTag(String tag) {
        //Log.i("INFO", "Début de getDietByTag avec " + tag);
        //On recherche la Diet associée.
        List<Diet> diets = dietDao.queryBuilder().where(DietDao.Properties.Tag.eq(tag)).list();
        if (diets.size() == 0) {
            //Pas de Diet, on en renvoie une nouvelle
            return new Diet();
        } else if (diets.size() > 1) {
            //Trop de diets pour un seul Tag. Ceci ne devrait jamais arriver, mais au cas où, on ne conserve que le premier
            //Log.i("INFO", "Trop (" + diets.size() + ") de régimes liés au Tag de " + tag + "... Suppression.");
            for (int i = 1; i < diets.size(); i++) {
                Diet diet = diets.get(i);
                diet.delete();
            }
        }
        //Log.i("INFO", "fin de getDietByTag : " + diets.get(0).getTag());
        return diets.get(0);
    }

    /**
     * Trouve la diet correspondante à un nom dans un Code langage.
     *
     * @param name         le nom
     * @param languageCode is a 2-digit language code
     */
    @Override
    public Diet getDietByNameAndLanguageCode(String name, String languageCode) {
        name = name.trim();
        //Log.i("INFO", "Début de getDietByNameAndLanguageCode avec " + name + ", " + languageCode);
        //Recherche d'un DietName ayant name et languageCode
        DietName dietName = getDietNameByNameAndLanguageCode(name, languageCode);
        if (dietName.getDietTag() == null) {
            //Pas de DietName, retour d'une nouvelle Diet
            //Log.i("INFO", "Fin de getDietByNameAndLanguageCode : New Diet");
            return new Diet();
        }
        //On recherche la Diet associée au premier nom.
        //Log.i("INFO", "Fin de getDietByNameAndLanguageCode : " + dietName.getDietTag());
        return getDietByTag(dietName.getDietTag());
    }

    /**
     * Trouve le DietName correspondant à un nom dans un Code langage.
     *
     * @param name         le nom
     * @param languageCode is a 2-digit language code
     */
    @Override
    public DietName getDietNameByNameAndLanguageCode(String name, String languageCode) {
        name = name.trim();
        //Log.i("INFO", "Début de getDietNameByNameAndLanguageCode avec " + name + ", " + languageCode);
        //Recherche d'un DietName ayant name et languageCode
        List<DietName> dietNames = dietNameDao.queryBuilder().where(
                DietNameDao.Properties.Name.eq(name),
                DietNameDao.Properties.LanguageCode.eq(languageCode)
        ).list();
        if (dietNames.size() == 0) {
            //Pas de DietName, retour d'un nouvel DietName
            //Log.i("INFO", "Fin de getDietNameByNameAndLanguageCode : New DietName");
            return new DietName();
        } else if (dietNames.size() > 1) {
            //Trop de DietName, On ne conserve que le premier.
            for (int i = 1; i < dietNames.size(); i++) {
                DietName dietName = dietNames.get(i);
                dietNameDao.delete(dietName);
            }
        }
        //On renvoie le premier DietName.
        //Log.i("INFO", "Fin de getDietNameByNameAndLanguageCode : " + dietNames.get(0).getDietTag());
        return dietNames.get(0);
    }

    /**
     * Trouve le DietName correspondant à un dietTag dans un Code langage.
     *
     * @param dietTag      le dietTag
     * @param languageCode is a 2-digit language code
     */
    @Override
    public DietName getDietNameByDietTagAndLanguageCode(String dietTag, String languageCode) {
        //Log.i("INFO", "Début de getDietNameByDietTagAndLanguageCode avec " + dietTag + ", " + languageCode);
        //Recherche d'un DietName ayant dietTag et languageCode
        List<DietName> dietNames = dietNameDao.queryBuilder().where(
                DietNameDao.Properties.DietTag.eq(dietTag),
                DietNameDao.Properties.LanguageCode.eq(languageCode)
        ).list();
        if (dietNames.size() == 0) {
            //Pas de DietName, retour d'une nouvelle DietName
            //Log.i("INFO", "Fin de getDietNameByDietTagAndLanguageCode : New DietName");
            return new DietName();
        } else if (dietNames.size() > 1) {
            //Trop de DietName, On ne conserve que le premier.
            for (int i = 1; i < dietNames.size(); i++) {
                DietName dietName = dietNames.get(i);
                dietNameDao.delete(dietName);
            }
        }
        //On renvoie le premier DietName.
        //Log.i("INFO", "Fin de getDietNameByDietTagAndLanguageCode : " + dietNames.get(0).getDietTag());
        return dietNames.get(0);
    }

    /**
     * Ajoute une nouvelle diet à partir des informations name... d'un de ses dietName(s) après avoir vérifier qu'elle n'existait pas.
     *
     * @param name         le nom souhaité
     * @param description  la description du régime
     * @param isEnabled    depends on whether user selected or unselected the diet
     * @param languageCode Le code language utilisé par l'utilisateur
     */
    @Override
    public void addDiet(String name, String description, boolean isEnabled, String languageCode) {
        name = name.trim();
        if (name != "") {
            //Log.i("INFO", "Début de addDiet avec " + name + ", " + description + ", " + isEnabled + ", " + languageCode);
            //Recherche du DietName correspondant au name et language code
            DietName dietName = getDietNameByNameAndLanguageCode(name, languageCode);
            if (dietName.getDietTag() == null) {
                //Le DietName retourné est vide, on lui ajoute les infos name, description, languageCode et dietTag
                dietName.setName(name);
                dietName.setLanguageCode(languageCode);
                dietName.setDietTag(languageCode + ":" + name);
            }
            //Dans tous les cas, mise à jour de la description
            dietName.setDescription(description);
            dietNameDao.getSession().insertOrReplace(dietName);
            //Recherche de la Diet correspondante au name et languageCode
            Diet diet = getDietByNameAndLanguageCode(name, languageCode);
            if (diet.getTag() == null) {
                //La Diet retournée est vide, on lui ajoute sont tag
                diet.setTag(languageCode + ":" + name);
            }
            //Dans tous les cas, mise à jour du Enabled
            diet.setEnabled(isEnabled);
            dietDao.getSession().insertOrReplace(diet);
            //Log.i("INFO", "Fin de addDiet avec " + name + ", " + description + ", " + isEnabled + ", " + languageCode);
        }
    }

    /**
     * Trouve la ingredient correspondante à un Tag.
     *
     * @param tag le nom
     */
    @Override
    public Ingredient getIngredientByTag(String tag) {
        //Log.i("INFO", "Début de getIngredientByTag avec " + tag);
        //On recherche la Ingredient associée.
        List<Ingredient> ingredients = ingredientDao.queryBuilder().where(IngredientDao.Properties.Tag.eq(tag)).list();
        if (ingredients.size() == 0) {
            //Pas de Ingredient, on en renvoie une nouvelle
            return new Ingredient();
        } else if (ingredients.size() > 1) {
            //Trop de ingredients pour un seul Tag. Ceci ne devrait jamais arriver, mais au cas où, on ne conserve que le premier
            //Log.i("INFO", "Trop (" + ingredients.size() + ") de régimes liés au Tag de " + tag + "... Suppression");
            for (int i = 1; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);
                ingredient.delete();
            }
        }
        //Log.i("INFO", "Fin de getIngredientByTag avec " + ingredients.get(0).getTag());
        return ingredients.get(0);
    }

    /**
     * Trouve l'ingredient correspondante à un nom dans un Code langage.
     *
     * @param name         le nom
     * @param languageCode is a 2-digit language code
     */
    @Override
    public Ingredient getIngredientByNameAndLanguageCode(String name, String languageCode) {
        name = name.trim();
        //Log.i("INFO", "Début de getIngredientByNameAndLanguageCode avec " + name + ", " + languageCode);
        //Recherche d'un IngredientName ayant name et languageCode
        IngredientName ingredientName = getIngredientNameByNameAndLanguageCode(name, languageCode);
        if (ingredientName.getIngredientTag() == null) {
            //Pas de IngredientName, retour d'un nouvel Ingredient
            //Log.i("INFO", "Fin de getIngredientByNameAndLanguageCode : New Ingredient.");
            return new Ingredient();
        }
        //On recherche l'Ingredient associée au premier nom.
        //Log.i("INFO", "Fin de getIngredientByNameAndLanguageCode via getIngredientByTag.");
        return getIngredientByTag(ingredientName.getIngredientTag());
    }

    /**
     * Renvoie un nouvel IngredientName ou le premier d'une liste (ménage si plusieurs.
     *
     * @param ingredientNames la liste d'ingrédient
     */
    private IngredientName getIngredientNameFromDoublon(List<IngredientName> ingredientNames) {
        //Log.i("INFO", "Début de getIngredientNameFromDoublon avec " + ingredientNames.toString());
        if (ingredientNames != null) {
            if (ingredientNames.size() == 0) {
                //Pas de IngredientName, retour d'un nouvel IngredientName
                //Log.i("INFO", "Fin de getIngredientNameFromDoublon : New IngredientName.");
                return new IngredientName();
            } else if (ingredientNames.size() > 1) {
                //Trop de IngredientName, suppression après vérification pui renvoie du premier.
                String mLanguageCode = ingredientNames.get(0).getLanguageCode();
                String mName = ingredientNames.get(0).getName();
                String mIngredientTag = ingredientNames.get(0).getIngredientTag();
                for (int i = 1; i < ingredientNames.size(); i++) {
                    IngredientName ingredientName = ingredientNames.get(i);
                    if (ingredientName.getLanguageCode().equals(mLanguageCode) && (ingredientName.getName().equalsIgnoreCase(mName) || ingredientName.getIngredientTag().equalsIgnoreCase(mIngredientTag))) {
                        ingredientNameDao.delete(ingredientName);
                    }
                }
            }
            //On renvoie le premier IngredientName.
            //Log.i("INFO", "Fin de getIngredientNameFromDoublon : " + ingredientNames.get(0).getLanguageCode() + ":" + ingredientNames.get(0).getName());
            return ingredientNames.get(0);
        }
        return null;
    }

    /**
     * Trouve le IngredientName correspondant à un nom dans un Code langage.
     *
     * @param name         le nom
     * @param languageCode is a 2-digit language code
     */
    @Override
    public IngredientName getIngredientNameByNameAndLanguageCode(String name, String languageCode) {
        name = name.trim();
        //Log.i("INFO", "Début de getIngredientNameByNameAndLanguageCode avec " + name + ", " + languageCode);
        //Recherche d'un IngredientName ayant name et languageCode
        List<IngredientName> ingredientNames = ingredientNameDao.queryBuilder().where(
                IngredientNameDao.Properties.Name.eq(name),
                IngredientNameDao.Properties.LanguageCode.eq(languageCode)
        ).list();
        //Log.i("INFO", "Fin de getIngredientNameByNameAndLanguageCode via getIngredientNameFromDoublon");
        return getIngredientNameFromDoublon(ingredientNames);
    }

    /**
     * Trouve le IngredientName correspondant à un ingredientTag dans un Code langage.
     *
     * @param ingredientTag le ingredientTag
     * @param languageCode  is a 2-digit language code
     */
    @Override
    public IngredientName getIngredientNameByIngredientTagAndLanguageCode(String ingredientTag, String languageCode) {
        //Log.i("INFO", "Début de getIngredientNameByIngredientTagAndLanguageCode avec " + ingredientTag + ", " + languageCode);
        //Recherche d'un IngredientName ayant ingredientTag et languageCode
        List<IngredientName> ingredientNames = ingredientNameDao.queryBuilder().where(
                IngredientNameDao.Properties.IngredientTag.eq(ingredientTag),
                IngredientNameDao.Properties.LanguageCode.eq(languageCode)
        ).list();
        //Log.i("INFO", "Fin de getIngredientNameByIngredientTagAndLanguageCode via getIngredientNameFromDoublon.");
        return getIngredientNameFromDoublon(ingredientNames);
    }

    /**
     * Ajoute une nouvelle ingredient à partir des informations name... d'un de ses ingredientName(s) après avoir vérifier qu'elle n'existait pas.
     *
     * @param name         le nom souhaité
     * @param languageCode Le code language utilisé par l'utilisateur
     */
    @Override
    public void addIngredient(String name, String languageCode) {
        name = name.trim();
        if (name != "") {
            //Log.i("INFO", "Début de addIngredient avec " + name + ", " + languageCode);
            //Recherche du IngredientName correspondant au name et language code
            IngredientName ingredientName = getIngredientNameByNameAndLanguageCode(name, languageCode);
            if (ingredientName.getIngredientTag() == null) {
                //Le IngredientName retourné est vide, on lui ajoute les infos name, languageCode et ingredientTag
                ingredientName.setName(name);
                ingredientName.setLanguageCode(languageCode);
                ingredientName.setIngredientTag(languageCode + ":" + name);
                ingredientNameDao.getSession().insert(ingredientName);
            }
            //Recherche de la Ingredient correspondante au name et languageCode
            Ingredient ingredient = getIngredientByNameAndLanguageCode(name, languageCode);
            if (ingredient.getTag() == null) {
                //La Ingredient retournée est vide, on lui ajoute sont tag
                ingredient.setTag(languageCode + ":" + name);
                ingredientDao.getSession().insertOrReplace(ingredient);
            }
            //Log.i("INFO", "Fin de addIngredient avec " + name + ", " + languageCode);
        }
    }

    /**
     * Ajoute une nouvelle liaison Diet/Ingredient à partir des informations dietName, IngredientName et languageCode après avoir vérifier qu'elle n'existait pas.
     *
     * @param dietName       le nom du régime
     * @param ingredientName l'ingrédient que l'on souhaite lui lier
     * @param languageCode   Le code language utilisé par l'utilisateur
     * @param state          Le code de l'état (-1, interdit, 0 couci couça, 1 autorisé, 2 pas d'avis)
     */
    @Override
    public void addDietIngredients(String dietName, String ingredientName, String languageCode, long state) {
        dietName = dietName.trim();
        ingredientName = ingredientName.trim();
        if (dietName != "" && ingredientName != "") {
            //Log.i("INFO", "Début de addDietIngredients avec " + dietName + ", " + ingredientName + ", " + languageCode + ", " + state);
            Diet diet = getDietByNameAndLanguageCode(dietName, languageCode);
            if (diet.getTag() != null) {
                //Diet trouvée.
                Ingredient ingredient = getIngredientByNameAndLanguageCode(ingredientName, languageCode);
                if (ingredient.getTag() != null) {
                    //Ingredient trouvé. Association des deux.
                    DietIngredients dietIngredients = new DietIngredients();
                    dietIngredients.setDietTag(diet.getTag());
                    dietIngredients.setIngredientTag(ingredient.getTag());
                    dietIngredients.setState(state);
                    saveDietIngredients(dietIngredients);
                }
            }
            //Log.i("INFO", "Fin de addDietIngredients avec " + dietName + ", " + ingredientName + ", " + languageCode + ", " + state);
        }
    }

    /**
     * Renvoie les dietIngrédients liés à une diet à partir de sont dietTag et du state attendu
     *
     * @param dietTag le Tag du régime
     * @param state   Le code de l'état (-2, tous, -1, interdit, 0 couci couça, 1 autorisé, 2 pas d'avis)
     */
    @Override
    public List<DietIngredients> getDietIngredientsListByDietTagAndState(String dietTag, long state) {
        //Log.i("INFO", "Début de getDietIngredientsListByDietTag avec " + dietTag + ", " + state);
        //Recherche des DietIngredients ayant dietTag
        List<DietIngredients> dietIngredientsList;
        if (state == -2) {
            dietIngredientsList = dietIngredientsDao.queryBuilder().where(
                    DietIngredientsDao.Properties.DietTag.eq(dietTag)
            ).list();
        } else {
            dietIngredientsList = dietIngredientsDao.queryBuilder().where(
                    DietIngredientsDao.Properties.DietTag.eq(dietTag),
                    DietIngredientsDao.Properties.State.eq(state)
            ).list();
        }
        //Log.i("INFO", "Fin de getDietIngredientsListByDietTag avec " + dietIngredientsList.size() + " éléments.");
        return dietIngredientsList;
    }

    /**
     * Renvoie les dietIngrédients liés à une diet à partir de sont dietTag
     *
     * @param dietTag le Tag du régime
     */
    @Override
    public List<DietIngredients> getDietIngredientsListByDietTag(String dietTag) {
        return getDietIngredientsListByDietTagAndState(dietTag, -2);
    }

    /**
     * Renvoie les ingrédients liés à une diet à partir des informations dietTag et state
     *
     * @param dietTag     le Tag du régime
     * @param state        Le code de l'état (-2, tous, -1, interdit, 0 couci couça, 1 autorisé, 2 pas d'avis)
     */
    @Override
    public List<Ingredient> getIngredientsLinkedToDietByDietTagAndState(String dietTag, long state) {
        //Log.i("INFO", "Début de getIngredientsLinkedToDietByDietTagAndState avec " + dietTag + " " + state);
        List<DietIngredients> dietIngredientsList = getDietIngredientsListByDietTagAndState(dietTag, state);
        if (dietIngredientsList != null) {
            List<Ingredient> ingredients = new ArrayList<>();
            for (int i = 0; i < dietIngredientsList.size(); i++) {
                DietIngredients dietIngredients = dietIngredientsList.get(i);
                //Log.i("INFO", "Ajout de " + dietIngredients.getIngredientTag());
                ingredients.add(getIngredientByTag(dietIngredients.getIngredientTag()));
            }
            //Log.i("INFO", "Fin de getIngredientsLinkedToDietByDietTagAndState avec " + (ingredients != null ? ingredients.size() : 0) + " éléments.");
            return ingredients;
        }
        return null;
    }

    /**
     * Renvoie les ingrédients liés à une diet à partir de l'information dietTag
     *
     * @param dietTag     le Tag du régime
     */
    @Override
    public List<Ingredient> getIngredientsLinkedToDietByDietTag(String dietTag) {
        return getIngredientsLinkedToDietByDietTagAndState(dietTag,-2);
    }

    /**
     * Renvoie les ingrédients liés à une diet à partir des informations dietName, languageCode et state
     *
     * @param dietName     le nom du régime
     * @param languageCode Le code language utilisé par l'utilisateur
     * @param state        Le code de l'état (-2, tous, -1, interdit, 0 couci couça, 1 autorisé, 2 pas d'avis)
     */
    @Override
    public List<Ingredient> getIngredientsLinkedToDietByDietNameLanguageCodeAndState(String dietName, String languageCode, long state) {
        //Log.i("INFO", "Début de getIngredientsLinkedToDietByDietNameLanguageCodeAndState avec " + dietName + " " + languageCode + " " + state);
        Diet diet = getDietByNameAndLanguageCode(dietName, languageCode);
        //Log.i("INFO", "Fin de getIngredientsLinkedToDietByDietNameLanguageCodeAndState via getIngredientsLinkedToDietByDietTagAndState");
        return getIngredientsLinkedToDietByDietTagAndState(diet.getTag(), state);
    }

    /**
     * Renvoie les ingrédients liés à une diet à partir des informations dietName et languageCode
     *
     * @param dietName     le nom du régime
     * @param languageCode Le code language utilisé par l'utilisateur
     */
    @Override
    public List<Ingredient> getIngredientsLinkedToDietByDietNameAndLanguageCode(String dietName, String languageCode) {
        return getIngredientsLinkedToDietByDietNameLanguageCodeAndState(dietName, languageCode, -2);
    }

    /**
     * Renvoie les ingrédientNames d'une liste d'ingrédients à partir des informations List<Ingredient>ingredients et languageCode
     *
     * @param ingredients     la liste d'ingrédient
     * @param languageCode Le code language utilisé par l'utilisateur
     */
    @Override
    public List<IngredientName> getIngredientNamesByIngredientsAndLanguageCode(List<Ingredient> ingredients, String languageCode) {
        //Log.i("INFO", "Début de getIngredientNamesByIngredientsAndLanguageCode avec " + ingredients + " " + languageCode);
        List<IngredientName> ingredientNames = new ArrayList<>();
        if (ingredients != null) {
            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);
                ingredientNames.add(getIngredientNameByIngredientTagAndLanguageCode(ingredient.getTag(), languageCode));
            }
        }
        //Log.i("INFO", "Fin de getIngredientNamesByIngredientsAndLanguageCode : " + ingredientNames.size() + " éléments.");
        return ingredientNames;
    }

    /**
     * Renvoie les ingrédientNames d'une liste d'ingrédients à partir des informations List<Ingredient>ingredients et languageCode
     *
     * @param dietTag      Le tag du régime
     * @param state        Le code de l'état (-2, tous, -1, interdit, 0 couci couça, 1 autorisé, 2 pas d'avis)
     * @param languageCode Le code language souhaité pour le nom des ingrédients
     */
    @Override
    public String getSortedIngredientNameStringByDietTagStateAndLanguageCode(String dietTag, long state, String languageCode) {
        //Log.i("INFO", "Début de getSortedIngredientNameStringByDietTagStateAndLanguageCode avec " + dietTag + " " + state + " " + languageCode);
        List<Ingredient> ingredients = getIngredientsLinkedToDietByDietTagAndState(dietTag, state);
        if (ingredients != null) {
            List<IngredientName> ingredientNames = getIngredientNamesByIngredientsAndLanguageCode(ingredients, languageCode);
            if (ingredientNames != null) {
                Collections.sort(ingredientNames, new Comparator<IngredientName>() {
                    @Override
                    public int compare(IngredientName iN1, IngredientName iN2) {
                        return iN1.getName().compareTo(iN2.getName());
                    }
                });
                List<String> ingredientNameList = new ArrayList<>();
                for (int i = 0; i < ingredientNames.size(); i++) {
                    ingredientNameList.add(ingredientNames.get(i).getName());
                }
                //Log.i("INFO", "Fin de getSortedIngredientNameStringByDietTagStateAndLanguageCode avec " + android.text.TextUtils.join(", ", ingredientNameList));
                return android.text.TextUtils.join(", ", ingredientNameList);
            }
        }
        //Log.i("INFO", "Fin de getSortedIngredientNameStringByDietTagStateAndLanguageCode avec null.");
        return null;
    }

    /**
     * Renvoie une châine d'ingrédients dont la couleur dépend du state dans toutes les Diets Enabled et d'un languageCode
     *
     * @param state        Le code de l'état (-2, tous, -1, interdit, 0 couci couça, 1 autorisé, 2 pas d'avis)
     * @param languageCode Le code language souhaité pour le nom des ingrédients
     */
    @Override
    public List<String> getIngredientNameLinkedToEnabledDietsByLanguageCode(long state, String languageCode) {
        //Log.i("INFO", "Début de getIngredientNameLinkedToEnabledDietsByLanguageCode avec " + state + ", " + languageCode);
        List<Diet> diets = getEnabledDiets();
        List<Ingredient> ingredientsTBC = new ArrayList<>();
        for (int i = 0; i < diets.size(); i++) {
            Diet diet = diets.get(i);
            List<Ingredient> ingredients = getIngredientsLinkedToDietByDietTagAndState(diet.getTag(), state);
            for (int j = 0; j < ingredients.size(); j++) {
                Ingredient ingredient = ingredients.get(j);
                if (ingredient.getTag().equals("en:")) {
                    List<DietIngredients> dietIngredientsList = dietIngredientsDao.queryBuilder()
                            .where(DietIngredientsDao.Properties.IngredientTag.eq(ingredient.getTag()))
                            .list();
                    for (int k = 0; k < dietIngredientsList.size(); k++) {
                        DietIngredients dietIngredients =  dietIngredientsList.get(k);
                        dietIngredientsDao.delete(dietIngredients);
                    }
                    List<IngredientName> ingredientNames = ingredientNameDao.queryBuilder()
                            .where(IngredientNameDao.Properties.IngredientTag.eq(ingredient.getTag()))
                            .list();
                    for (int k = 0; k < ingredientNames.size(); k++) {
                        IngredientName ingredientName =  ingredientNames.get(k);
                        ingredientNameDao.delete(ingredientName);
                    }
                    ingredientDao.delete(ingredient);
                } else {
                    if (!ingredientsTBC.contains(ingredient)) {
                        //Log.i("INFO", "Ajout de l'ingrédient " + ingredient.getTag());
                        ingredientsTBC.add(ingredient);
                    }
                }
            }
        }
        List<IngredientName> ingredientNames = getIngredientNamesByIngredientsAndLanguageCode(ingredientsTBC, languageCode);
        if (ingredientNames != null) {
            List<String> ingredientNameList = new ArrayList<>();
            for (int i = 0; i < ingredientNames.size(); i++) {
                if (ingredientNames.get(i).getName() != null) {
                    ingredientNameList.add(ingredientNames.get(i).getName());
                }
            }
            //Log.i("INFO", "Fin de getIngredientNameLinkedToEnabledDietsByLanguageCode avec " + ingredientNameList.size() + " éléments.");
            return ingredientNameList;
        }
        //Log.i("INFO", "Fin de getIngredientNameLinkedToEnabledDietsByLanguageCode avec une liste vide.");
        return new ArrayList<String>();
    }

    /**
     * Renvoie un statetment des ingrédient dont la couleur dépend du state à partir d'une liste de DietIngredients et d'un languageCode
     *
     * @param INGREDIENT_PATTERN    Pattern de séparation des ingrédients.
     * @param txtIngredients        SpannableStringBuilder contenant la liste des ingrédients.
     * //@param languageCode Le code language utilisé par l'utilisateur
     */
    @Override
    public SpannableStringBuilder getColoredSpannableStringBuilderFromSpannableIngredients(Pattern INGREDIENT_PATTERN, SpannableStringBuilder txtIngredients) {
        //Log.i("INFO", "Début de getColoredSpannableStringBuilderFromSpannableIngredients avec " + INGREDIENT_PATTERN.toString() + " " + txtIngredients.toString());
        List<String> ingredientsToBeColoredInRed = getIngredientNameLinkedToEnabledDietsByLanguageCode(-1, Locale.getDefault().getLanguage());
        List<String> ingredientsToBeColoredInOrange = getIngredientNameLinkedToEnabledDietsByLanguageCode(0, Locale.getDefault().getLanguage());
        List<String> ingredientsToBeColoredInGreen = getIngredientNameLinkedToEnabledDietsByLanguageCode(1, Locale.getDefault().getLanguage());
        if (ingredientsToBeColoredInGreen.size() > 0) txtIngredients = setSpanColorBetweenTokens(INGREDIENT_PATTERN, txtIngredients, ingredientsToBeColoredInGreen, 1);
        if (ingredientsToBeColoredInOrange.size() > 0) txtIngredients = setSpanColorBetweenTokens(INGREDIENT_PATTERN, txtIngredients, ingredientsToBeColoredInOrange, 0);
        if (ingredientsToBeColoredInRed.size() > 0) txtIngredients = setSpanColorBetweenTokens(INGREDIENT_PATTERN, txtIngredients, ingredientsToBeColoredInRed, -1);
        //Log.i("INFO", "Fin de getColoredSpannableStringBuilderFromSpannableIngredients : " + ingredientsToBeColoredInGreen.size() + " vert, " + ingredientsToBeColoredInOrange.size() + " Orange, " + ingredientsToBeColoredInRed.size() + " rouge dans " + txtIngredients.toString());
        return txtIngredients;
    }

    private SpannableStringBuilder setSpanColorBetweenTokens(Pattern INGREDIENT_PATTERN, CharSequence text, List<String> ingredientsToBeColored, long state) {
        //Log.i("INFO", "Début de setSpanColorBetweenTokens avec " + INGREDIENT_PATTERN.toString() + ", " + text.toString() + ", " + ingredientsToBeColored.toString() + ", " + state);
        final SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        Matcher m = INGREDIENT_PATTERN.matcher(ssb);
        while (m.find()) {
            final String tm = m.group();
            final String ingredientValue = tm.replaceAll("[(),.-]+", "");
            for (String ingredientToBeColored : ingredientsToBeColored) {
                if (ingredientToBeColored.equalsIgnoreCase(ingredientValue)) {
                    int start = m.start();
                    int end = m.end();
                    if (tm.contains("(")) {
                        start += 1;
                    } else if (tm.contains(")")) {
                        end -= 1;
                    }
                    Log.i("INFO", "Ajout d'un Span de " + start + " à " + end + " de state " + state + " sur " + ingredientValue + ".");
                    ssb.setSpan(new ForegroundColorSpan(Color.parseColor(colors.get((int) (long) state))), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        //Log.i("INFO", "Fin de setSpanColorBetweenTokens avec " + ssb.toString());
        return ssb;
    }

    /**
     * Renvoie un statetment des ingrédient dont la couleur dépend du state à partir d'une liste de DietIngredients et d'un languageCode
     *
     * @param dietIngredientsList    La liste à transformée.
     * @param languageCode Le code language utilisé par l'utilisateur
     */
    @Override
    public SpannableString getSpannableStringFromDietIngredientsAndLanguageCode(List<DietIngredients> dietIngredientsList, String languageCode) {
        //Log.i("INFO", "Début de getSpannableStringFromDietIngredientsAndLanguageCode avec " + dietIngredientsList + " " + languageCode);
        if (dietIngredientsList != null) {
//        List<IngredientName> ingredientNames = new ArrayList<>();
            //La String qui contiendra les ingrédients
            String ingredients = "";
            //La chaîne de séparation, nulle au début puis ", "
            String sep = "";
            //La couleur à appliquer
            ForegroundColorSpan color;
            //Le HashMap d'association ingrédient/color
            HashMap<String, ForegroundColorSpan> ingredientColors = new HashMap<String, ForegroundColorSpan>();
            for (int i = 0; i < dietIngredientsList.size(); i++) {
                //Pour chaque DietIngredients
                DietIngredients dietIngredients =  dietIngredientsList.get(i);
                //Color en fonction du state
                color = new ForegroundColorSpan(Color.parseColor(colors.get((int) (long) dietIngredients.getState())));
                //Recherche du nom de l'ingrédient dans languageCode
                IngredientName ingredientName = getIngredientNameByIngredientTagAndLanguageCode(dietIngredients.getIngredientTag(), languageCode);
                //Ajout dans le HashMap
                ingredientColors.put(ingredientName.getName(), color);
                //Ajout à la String ingredients
                ingredients = sep + ingredientName.getName();
                //Changement de séparateur pour les ingrédient 2 et suivants
                sep = ", ";
//            ingredientNames.add(getIngredientNameByIngredientTagAndLanguageCode(dietIngredients.getIngredientTag(), languageCode));
            }
            //Création d'un SpannableString à partir de la liste précédente
            SpannableString ingredientsSS = new SpannableString(ingredients);
            //Pour chaque couple du HashMap
            for (String ingredient : ingredientColors.keySet()) {
                //Récupération de la color
                color = ingredientColors.get(ingredient);
                //Recherche de la position de l'ingrédient dans la liste
                int start = (", " + ingredients).toLowerCase().indexOf(", " + ingredient.toLowerCase() + ",");
                //Longueur de l'ingredient
                int length = ingredient.length();
                if (start >= 0) {
                    //Mise en couleur de l'ingrédient
                    ingredientsSS.setSpan(color, start, start + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
/*
        Collections.sort(ingredientNames, new Comparator<IngredientName>() {
            @Override
            public int compare(IngredientName iN2, IngredientName iN1)
            {
                return iN1.getName().compareTo(iN2.getName());
            }
        });
*/
            //Renvoie de la SpannableString
            //Log.i("INFO", "Fin de getSpannableStringFromDietIngredientsAndLanguageCode : " + ingredientsSS.toString());
            return ingredientsSS;
        }
        return new SpannableString("");
    }
}