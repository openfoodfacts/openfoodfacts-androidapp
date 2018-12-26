package openfoodfacts.github.scrachx.openfood.repositories;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.Arrays;
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
import openfoodfacts.github.scrachx.openfood.models.IngredientsRelation;
import openfoodfacts.github.scrachx.openfood.models.IngredientsRelationDao;
import openfoodfacts.github.scrachx.openfood.models.IngredientsWrapper;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.ProductApiService;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

/**
 * <b>DietRepository is the class to manage the database tables for diet.</b>
 *
 * @author dobriseb
 * @version 0.2
 */

public class DietRepository implements IDietRepository {

    private ProductApiService productApi;
    private static final String DEFAULT_LANGUAGE = "en";
    private static IDietRepository instance;
    private Database db;
    private DietDao dietDao;
    private DietNameDao dietNameDao;
    private IngredientDao ingredientDao;
    private IngredientNameDao ingredientNameDao;
    private IngredientsRelationDao ingredientsRelationDao;
    private DietIngredientsDao dietIngredientsDao;
    private HashMap<Integer, String> colors = new HashMap<Integer, String>();

    /**
     *
     * @return instance of dietRepository
     * @author dobriseb
     */
    public static IDietRepository getInstance() {
        if (instance == null) {
            instance = new DietRepository();
        }
        return instance;
    }

    private DietRepository() {
        productApi = CommonApiManager.getInstance().getProductApiService();
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        db = daoSession.getDatabase();
        dietDao = daoSession.getDietDao();
        dietNameDao = daoSession.getDietNameDao();
        ingredientDao = daoSession.getIngredientDao();
        ingredientNameDao = daoSession.getIngredientNameDao();
        ingredientsRelationDao = daoSession.getIngredientsRelationDao();
        dietIngredientsDao = daoSession.getDietIngredientsDao();

        colors.put(-1, "#ff0000");
        colors.put(0, "#ff9900");
        colors.put(1, "#00b400");
        colors.put(2, "#393939");
    }

    /**
     * Checks whether table is empty
     *
     * @param dao checks records count of any table
     */
    private Boolean tableIsEmpty(AbstractDao dao) {
        return dao.count() == 0;
    }

    /**
     * Load diets from (the server or) local database
     * Inspired by ProductRepository but not used
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     *                Pour le moment, pas de question a se poser, les données ne sont que locales.
     * @author dobriseb
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
        Log.i("INFO", "getIngredients("+ refresh +")");
        if (refresh || tableIsEmpty(ingredientDao)) {
            return productApi.getIngredients()
                    .map(IngredientsWrapper::map);
        } else {
            return Single.fromCallable(() -> ingredientDao.loadAll());
        }
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
        Log.i("INFO", "Début de saveIngredients");
        db.beginTransaction();
        try {
            for (Ingredient ingredient : ingredients) {
                ingredientDao.insertOrReplace(ingredient);
                for (IngredientName ingredientName : ingredient.getNames()) {
                    ingredientNameDao.insertOrReplace(ingredientName);
                }
                for (IngredientsRelation ingredientsRelation : ingredient.getParents()) {
                    ingredientsRelationDao.insertOrReplace(ingredientsRelation);
                }
                for (IngredientsRelation ingredientsRelation : ingredient.getChildren()) {
                    ingredientsRelationDao.insertOrReplace(ingredientsRelation);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        Log.i("INFO", "Fin de saveIngredients");
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
     * Return a Diet from its Tag.
     * In case of doubloons suppress the outnumbers rows.
     *
     * @param tag       The Tag
     * @return Diet     Diet object
     *
     * @author dobriseb
     */
    @Override
    public Diet getDietByTag(String tag) {
        //Log.i("INFO", "Début de getDietByTag avec " + tag);
        //Looking for the Diet.
        List<Diet> diets = dietDao.queryBuilder().where(DietDao.Properties.Tag.eq(tag)).list();
        if (diets.size() == 0) {
            //Not found, return a new one
            return new Diet();
        } else if (diets.size() > 1) {
            //Too many diet objects with the same Tag. This will never happened, but, well just in case, suppress doubloons and return the survivor
            for (int i = 1; i < diets.size(); i++) {
                Diet diet = diets.get(i);
                diet.delete();
            }
        }
        //return the first (and only) Diet object
        //Log.i("INFO", "fin de getDietByTag : " + diets.get(0).getTag());
        return diets.get(0);
    }

    /**
     * Return the Diet object from a Name and a languageCode
     *
     * @param name          The Name of the Diet
     * @param languageCode  is a 2-digit language code
     * @return Diet         Diet object
     *
     * @author dobriseb
     */
    @Override
    public Diet getDietByNameAndLanguageCode(String name, String languageCode) {
        name = name.trim();
        //Log.i("INFO", "Début de getDietByNameAndLanguageCode avec " + name + ", " + languageCode);
        //Looking for a DietName with name and languageCode
        DietName dietName = getDietNameByNameAndLanguageCode(name, languageCode);
        if (dietName.getDietTag() == null) {
            //Not found, return a new Diet object
            return new Diet();
        }
        //Return the Diet with the Tag of the DietName found.
        //Log.i("INFO", "Fin de getDietByNameAndLanguageCode : " + dietName.getDietTag());
        return getDietByTag(dietName.getDietTag());
    }

    /**
     * Return the DietName object from a Name and langageCode.
     * In case of doubloons suppress the outnumbers rows.
     *
     * @param name          The Name
     * @param languageCode  is a 2-digit language code
     * @return DietName     DietName object
     *
     * @author dobriseb
     */
    @Override
    public DietName getDietNameByNameAndLanguageCode(String name, String languageCode) {
        name = name.trim();
        //Log.i("INFO", "Début de getDietNameByNameAndLanguageCode avec " + name + ", " + languageCode);
        //Looking for the DietName with name and languageCode
        List<DietName> dietNames = dietNameDao.queryBuilder().where(
                DietNameDao.Properties.Name.eq(name),
                DietNameDao.Properties.LanguageCode.eq(languageCode)
        ).list();
        if (dietNames.size() == 0) {
            //Not fount, return a new one
            return new DietName();
        } else if (dietNames.size() > 1) {
            //Too many DietName objects found, suppress doubloons
            for (int i = 1; i < dietNames.size(); i++) {
                DietName dietName = dietNames.get(i);
                dietNameDao.delete(dietName);
            }
        }
        //Return the first (and only) DietName.
        //Log.i("INFO", "Fin de getDietNameByNameAndLanguageCode : " + dietNames.get(0).getDietTag());
        return dietNames.get(0);
    }

    /**
     * Return the DietName from a Tag and LanguageCode
     * In case of doubloons suppress the outnumbers rows.
     *
     * @param dietTag       dietTag
     * @param languageCode  is a 2-digit language code
     * @return DietName     DietName object
     *
     * @author dobriseb
     */
    @Override
    public DietName getDietNameByDietTagAndLanguageCode(String dietTag, String languageCode) {
        //Log.i("INFO", "Début de getDietNameByDietTagAndLanguageCode avec " + dietTag + ", " + languageCode);
        //Looking for a DietName with the dietTag and LanguageCode passed
        List<DietName> dietNames = dietNameDao.queryBuilder().where(
                DietNameDao.Properties.DietTag.eq(dietTag),
                DietNameDao.Properties.LanguageCode.eq(languageCode)
        ).list();
        if (dietNames.size() == 0) {
            //Not fount, return a new one
            return new DietName();
        } else if (dietNames.size() > 1) {
            //Too many DietName, suppress doubloons
            for (int i = 1; i < dietNames.size(); i++) {
                DietName dietName = dietNames.get(i);
                dietNameDao.delete(dietName);
            }
        }
        //return the first (and only) DietName object.
        //Log.i("INFO", "Fin de getDietNameByDietTagAndLanguageCode : " + dietNames.get(0).getDietTag());
        return dietNames.get(0);
    }

    /**
     * Add a new diet with Name, Description...
     * A test of non existance is done before insertion.
     *
     * @param name         Name of the diet
     * @param description  Description of the diet
     * @param isEnabled    depends on whether user enabled or disabled the diet
     * @param languageCode User's LanguageCode
     *
     * @author dobriseb
     */
    @Override
    public void addDiet(String name, String description, boolean isEnabled, String languageCode) {
        name = name.trim();
        if (name != "") {
            //Log.i("INFO", "Début de addDiet avec " + name + ", " + description + ", " + isEnabled + ", " + languageCode);
            //Looking for a DietName object with this Name and LanguageCode
            DietName dietName = getDietNameByNameAndLanguageCode(name, languageCode);
            if (dietName.getDietTag() == null) {
                //Not found, so a new one has just be created, complete it.
                dietName.setName(name);
                dietName.setLanguageCode(languageCode);
                dietName.setDietTag(languageCode + ":" + name);
            }
            //In all the case update description and save the IngredientName object
            dietName.setDescription(description);
            dietNameDao.getSession().insertOrReplace(dietName);
            //Looking for an existing diet with this name and languageCode
            Diet diet = getDietByNameAndLanguageCode(name, languageCode);
            if (diet.getTag() == null) {
                //The returned Diet object is a new one, set its Tag
                diet.setTag(languageCode + ":" + name);
            }
            //In all the case, update its Enabled field and save the Diet object
            diet.setEnabled(isEnabled);
            dietDao.getSession().insertOrReplace(diet);
            //Log.i("INFO", "Fin de addDiet avec " + name + ", " + description + ", " + isEnabled + ", " + languageCode);
        }
    }

    /**
     * Return an Ingredient object from its Tag
     * In case of doubloons suppress the outnumbers rows.
     *
     * @param tag   The Tag
     * @return Ingredient  Ingredient object
     *
     * @author dobriseb
     */
    @Override
    public Ingredient getIngredientByTag(String tag) {
        //Log.i("INFO", "Début de getIngredientByTag avec " + tag);
        //On recherche la Ingredient associée.
        List<Ingredient> ingredients = ingredientDao.queryBuilder().where(IngredientDao.Properties.Tag.eq(tag)).list();
        if (ingredients.size() == 0) {
            //Not found, return a new Ingredient
            return new Ingredient();
        } else if (ingredients.size() > 1) {
            //Too many ingredient objects with the same Tag. This will never happened, but, well just in case, suppress doubloons and return the survivor
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
     * Return the ingredient object search by a name and a languageCode
     *
     * @param name         The Name
     * @param languageCode is a 2-digit language code
     * @return Ingredient  Ingredient object
     *
     * @author dobriseb
     */
    @Override
    public Ingredient getIngredientByNameAndLanguageCode(String name, String languageCode) {
        name = name.trim();
        //Log.i("INFO", "Début de getIngredientByNameAndLanguageCode avec " + name + ", " + languageCode);
        //Looking for an IngredientName object with name and languageCode
        IngredientName ingredientName = getIngredientNameByNameAndLanguageCode(name, languageCode);
        if (ingredientName.getIngredientTag() == null) {
            //Not found, return a new Ingredient
            //Log.i("INFO", "Fin de getIngredientByNameAndLanguageCode : New Ingredient.");
            return new Ingredient();
        }
        //Return the ingredient that has this ingredientName.
        //Log.i("INFO", "Fin de getIngredientByNameAndLanguageCode via getIngredientByTag.");
        return getIngredientByTag(ingredientName.getIngredientTag());
    }

    /**
     * Return a new IngredientName object or the first one of a list
     * In case of doubloons suppress the outnumbers rows.
     *
     * @param ingredientNames   Liste of IngredientName objects
     * @return IngredientName   IngredientName object
     *
     * @author dobriseb
     */
    private IngredientName getIngredientNameFromDoublon(List<IngredientName> ingredientNames) {
        //Log.i("INFO", "Début de getIngredientNameFromDoublon avec " + ingredientNames.toString());
        if (ingredientNames != null) {
            if (ingredientNames.size() == 0) {
                //No IngredientName, return a new one
                //Log.i("INFO", "Fin de getIngredientNameFromDoublon : New IngredientName.");
                return new IngredientName();
            } else if (ingredientNames.size() > 1) {
                //Doubloon, suppress after test.
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
            //Return the first IngredientName.
            //Log.i("INFO", "Fin de getIngredientNameFromDoublon : " + ingredientNames.get(0).getLanguageCode() + ":" + ingredientNames.get(0).getName());
            return ingredientNames.get(0);
        }
        return null;
    }

    /**
     * Return the IngredientName object from a name and a LanguageCode
     *
     * @param name              le nom
     * @param languageCode      is a 2-digit language code
     * @return IngredientName   IngredientName object
     *
     * @author dobriseb
     */
    @Override
    public IngredientName getIngredientNameByNameAndLanguageCode(String name, String languageCode) {
        name = name.trim();
        //Log.i("INFO", "Début de getIngredientNameByNameAndLanguageCode avec " + name + ", " + languageCode);
        //Recherche d'un IngredientName ayant name et languageCode
        List<IngredientName> ingredientNames = ingredientNameDao.queryBuilder().where(
                new WhereCondition.StringCondition("lower(NAME) = lower('" + name.replaceAll("'", "''") + "')"),
                IngredientNameDao.Properties.LanguageCode.eq(languageCode)
        ).list();
        //Log.i("INFO", "Fin de getIngredientNameByNameAndLanguageCode, trouvé " + (ingredientNames.size() > 0 ? ingredientNames.get(0).getName() : "aucun"));
        return getIngredientNameFromDoublon(ingredientNames);
    }

    /**
     * Return the IngredientName object from an ingredientTag and a LanguageCode
     *
     * @param ingredientTag     The searched ingredientTag
     * @param languageCode      is a 2-digit language code
     * @return IngredientName   IngredientName object
     *
     * @author dobriseb
     */
    @Override
    public IngredientName getIngredientNameByIngredientTagAndLanguageCode(String ingredientTag, String languageCode) {
        //Log.i("INFO", "Début de getIngredientNameByIngredientTagAndLanguageCode avec " + ingredientTag + ", " + languageCode);
        List<IngredientName> ingredientNames = ingredientNameDao.queryBuilder().where(
                IngredientNameDao.Properties.IngredientTag.eq(ingredientTag),
                IngredientNameDao.Properties.LanguageCode.eq(languageCode)
        ).list();
        //Log.i("INFO", "Fin de getIngredientNameByIngredientTagAndLanguageCode via getIngredientNameFromDoublon.");
        return getIngredientNameFromDoublon(ingredientNames);
    }

    /**
     * Add a new ingredient from the information languageCode and name of one of its ingredientName(s) if it doesn't already exists.
     *
     * @param name         Name of the ingredient
     * @param languageCode LanguageCode used
     *
     * @author dobriseb
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
     * Add a new link Diet/Ingredient from dietName, IngredientName and languageCode if it doesn't already exists.
     *
     * @param dietTag        Tag of the diet
     * @param ingredientName Ingrédient we wan't to link to the diet
     * @param languageCode   Language code of the ingredientName
     * @param state          State code (-1: forbidden, 0: so-so, 1: authorised, 2: no impact)
     *
     * @author dobriseb
     */
    @Override
    public void addDietIngredients(String dietTag, String ingredientName, String languageCode, long state) {
        ingredientName = ingredientName.trim();
        if (ingredientName != "") {
            //Log.i("INFO", "Début de addDietIngredients avec " + dietTag + ", " + ingredientName + ", " + languageCode + ", " + state);
            Ingredient ingredient = getIngredientByNameAndLanguageCode(ingredientName, languageCode);
            if (ingredient.getTag() != null) {
                //Ingredient trouvé. Association des deux.
                DietIngredients dietIngredients = new DietIngredients();
                dietIngredients.setDietTag(dietTag);
                dietIngredients.setIngredientTag(ingredient.getTag());
                dietIngredients.setState(state);
                saveDietIngredients(dietIngredients);
            } else {
                //No ingredient found, create it and then create the link
                addIngredient(ingredientName, languageCode);
                addDietIngredients(dietTag, ingredientName, languageCode, state);
            }
            //Log.i("INFO", "Fin de addDietIngredients avec " + dietTag + ", " + ingredientName + ", " + languageCode + ", " + state);
        }
    }

    /**
     * Return the DietIngredients objects linked to a diet with a certain state from a dietTag
     *
     * @param dietTag               Tag of the diet
     * @param state                 The searched state (-2 : all, -1 : forbidden, 0 : problematic, 1 authorised, 2 no impact)
     * @return List<DietIngredients>   List of DietIngredients objects.
     *
     * @author dobriseb
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
     * Return the DietIngredients objects linked to a diet from a dietTag
     *
     * @param dietTag                  Tag of the diet
     * @return List<DietIngredients>   List of DietIngredients objects.
     *
     * @author dobriseb
     */
    @Override
    public List<DietIngredients> getDietIngredientsListByDietTag(String dietTag) {
        return getDietIngredientsListByDietTagAndState(dietTag, -2);
    }

    /**
     * Return a list of ingredient objects linked to a diet with a certain state from a dietTag
     *
     * @param dietTag               Tag of the diet
     * @param state                 The searched state (-2 : all, -1 : forbidden, 0 : problematic, 1 authorised, 2 no impact)
     * @return List<Ingredient>     List of ingredient objects.
     *
     * @author dobriseb
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
     * Return a list of ingredient objects linked to a diet from its dietTag
     *
     * @param dietTag               Tag of the diet
     * @return List<Ingredient>     List of ingredient objects.
     *
     * @author dobriseb
     */
    @Override
    public List<Ingredient> getIngredientsLinkedToDietByDietTag(String dietTag) {
        return getIngredientsLinkedToDietByDietTagAndState(dietTag,-2);
    }

    /**
     * Return a list of ingredient objects linked to a diet with a certain state from a dietName and its languageCode
     *
     * @param dietName              Name of the diet
     * @param languageCode          LanguageCode of the dietName
     * @param state                 The searched state (-2 : all, -1 : forbidden, 0 : problematic, 1 authorised, 2 no impact)
     * @return List<Ingredient>     List of ingredient objects.
     *
     * @author dobriseb
     */
    @Override
    public List<Ingredient> getIngredientsLinkedToDietByDietNameLanguageCodeAndState(String dietName, String languageCode, long state) {
        //Log.i("INFO", "Début de getIngredientsLinkedToDietByDietNameLanguageCodeAndState avec " + dietName + " " + languageCode + " " + state);
        Diet diet = getDietByNameAndLanguageCode(dietName, languageCode);
        //Log.i("INFO", "Fin de getIngredientsLinkedToDietByDietNameLanguageCodeAndState via getIngredientsLinkedToDietByDietTagAndState");
        return getIngredientsLinkedToDietByDietTagAndState(diet.getTag(), state);
    }

    /**
     * Return a list of ingredient objects linked to a diet from a dietName and his languageCode
     *
     * @param dietName          Name of the diet
     * @param languageCode      LanguageCode of the dietName
     * @return List<Ingredient>     List of ingredient objects.
     *
     * @author dobriseb
     */
    @Override
    public List<Ingredient> getIngredientsLinkedToDietByDietNameAndLanguageCode(String dietName, String languageCode) {
        return getIngredientsLinkedToDietByDietNameLanguageCodeAndState(dietName, languageCode, -2);
    }

    /**
     * Return a list of names of ingredients in a languageCode from a list of ingredient (objects)
     *
     * @param ingredients               List of ingrédient objects
     * @param languageCode              LanguageCode to be used
     * @return List<IngredientName>     List of ingredientName objects.
     *
     * @author dobriseb
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
     * Return a sorted list of name of ingredients that have a relation with a diet with a certain state and i a languageCode
     *
     * @param dietTag           The tag of the diet
     * @param state             The searched state (-2 : all, -1 : forbidden, 0 : problematic, 1 authorised, 2 no impact)
     * @param languageCode      languageCode for the names of ingredients
     * @return List<String>     Sorted list of ingredients.
     *
     * @author dobriseb
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
     * Return a list of ingredient that match a state for all enabled diets in a languageCode
     *
     * @param state             The searched state (-2 : all, -1 : forbidden, 0 : problematic, 1 authorised, 2 no impact)
     * @param languageCode      languageCode for the names of ingredients
     * @return List<String>     list of ingredients.
     *
     * @author dobriseb
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
     * Return the state value for a ingretient/diet relation
     *
     * @param ingredientTag     Tag de l'ingrédient
     * @param dietTag           Tag de la diet
     * @return long             State value.
     *
     * @author dobriseb
     */
    @Override
    public long stateFromIngredientTagDietTag(String ingredientTag, String dietTag){
        long state = 2;
        DietIngredients dietIngredients = dietIngredientsDao.queryBuilder()
                .where(
                        DietIngredientsDao.Properties.DietTag.eq(dietTag),
                        DietIngredientsDao.Properties.IngredientTag.eq(ingredientTag)
                )
                .unique();

        if (dietIngredients != null) {
            state = dietIngredients.getState();
        }
        return state;
    }

    /**
     * Return the minimal value of ths states that has an ingredient in its relation with enabled diets
     *
     * @param ingredientTag    the ingredient Tag
     * @return long             min State of the ingredient for enabled diets
     *
     * @author dobriseb
     */
    public long minStateForEnabledDietFromIngredientTag(String ingredientTag){
        long state = 2;
        List<DietIngredients> dietIngredients = dietIngredientsDao.queryBuilder()
                .where(
                        new WhereCondition.StringCondition("DIET_TAG in (SELECT TAG FROM DIET WHERE ENABLED)"),
                        DietIngredientsDao.Properties.IngredientTag.eq(ingredientTag)
                )
                .list();

        if (dietIngredients.size() > 0) {
            for (int i = 0; i < dietIngredients.size(); i++) {
                DietIngredients dietIngredient =  dietIngredients.get(i);
                state = dietIngredient.getState() < state ? dietIngredient.getState() : state;
            }
        }
        return state;
    }

    private long stateFromIngredientDietTag(String ingredientxt, String dietTag, String languageCode){
        long state = 2;
        Ingredient ingredient = getIngredientByNameAndLanguageCode(ingredientxt, languageCode);
        if (ingredient.getTag() != null) {
            state = stateFromIngredientTagDietTag(ingredient.getTag(), dietTag);
        }
        return state;
    }

    private long minStateForEnabledDietFromIngredient(String ingredientxt, String languageCode){
        long state = 2;
        Ingredient ingredient = getIngredientByNameAndLanguageCode(ingredientxt, languageCode);
        if (ingredient.getTag() != null) {
            state = minStateForEnabledDietFromIngredientTag(ingredient.getTag());
        }
        return state;
    }

    private long stateFromIngredientDiet(String ingredientxt, String dietxt, String languageCode){
        long state = 2;
        Diet diet = getDietByNameAndLanguageCode(dietxt, languageCode);
        if (diet != null) {
            state = stateFromIngredientDietTag(ingredientxt, diet.getTag(), languageCode);
        }
        return state;
    }

    /**
     * Return a SpannableStringBuilder of the ingredients colored when associate with a Diet. Parameters : list Pattern ingredient splitter, ingredients text.
     *
     * @param INGREDIENT_PATTERN    Pattern splitter.
     * @param txtIngredients        SpannableStringBuilder composed with the list of ingrédients.
     * @return SpannableStringBuilder   A SpannableStringBuilder with coloured text.
     *
     * @author dobriseb
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

    /**
     * Return a SpannableStringBuilder of the ingredients colored when associate with an active Diet. Parameters : ingredients on a SpannableStringBuilder form.
     *
     * @param ssbIngredients        SpannableStringBuilder composed with the ingrédients.
     * @param languageCode          LanguageCode of the product for search in the same languageCode.
     * @return SpannableStringBuilder   A SpannableStringBuilder with coloured text.
     *
     * @author dobriseb
     */
    @Override
    public SpannableStringBuilder getColoredSpannableStringBuilderFromSpannableStringBuilderIngredients(SpannableStringBuilder ssbIngredients, String languageCode) {
        //Log.i("INFO", "Début de getColoredSpannableStringBuilderFromSpannableStringBuilderIngredients avec " + ssbIngredients.toString());
        String ingredientsText = ssbIngredients.toString();
        List<String> ingredientsList = getIngredientsListFromIngredientsText(ingredientsText, true);
        List<SpannableStringBuilder> ssbIngredientsList = getColoredSpannableStringBuilderFromIngredientsDiet(ingredientsList, "enabled", languageCode);
        int start = 0;
        int end = 0;
        for (int i = 0; i < ssbIngredientsList.size(); i++) {
            SpannableStringBuilder ssbIngredient =  ssbIngredientsList.get(i);
            start = ingredientsText.indexOf(ssbIngredient.toString());
            if (start >= 0) {
                end = start + ssbIngredient.length();
                ssbIngredients.replace(start, end, ssbIngredient);
            }
        }
        return ssbIngredients;
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
                    //Log.i("INFO", "Ajout d'un Span de " + start + " à " + end + " de state " + state + " sur " + ingredientValue + ".");
                    ssb.setSpan(new ForegroundColorSpan(Color.parseColor(colors.get((int) (long) state))), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        //Log.i("INFO", "Fin de setSpanColorBetweenTokens avec " + ssb.toString());
        return ssb;
    }

    /**
     * Return a list of SpannableStringBuilder of ingredients witch color depend of state from a list of ingredients, a dietTag and a languageCode
     *
     * @param ingredients    List of ingrédients to be colored.
     * @param dietTag        dietTag of the diet or "enabled" for all diets that are enabled.
     * @param languageCode   Language code of the user.
     * @return List<SpannableStringBuilder>   List of SpannableStringBuilder with coloured text.
     *
     * @author dobriseb
     */
    @Override
    public List<SpannableStringBuilder> getColoredSpannableStringBuilderFromIngredientsDiet(List<String> ingredients, String dietTag, String languageCode) {
        //Log.i("INFO", "Début de getColoredSpannableStringBuilderFromIngredientsDiet avec " + ingredients.toString() + " et " + dietTag);
        List<SpannableStringBuilder> ingredientsSp = new ArrayList<>();
        long state;
        for (int i = 0; i < ingredients.size(); i++) {
            String ingredient =  ingredients.get(i);
            state = dietTag.equalsIgnoreCase("enabled") ? minStateForEnabledDietFromIngredient(ingredient, languageCode) : stateFromIngredientDietTag(ingredient, dietTag, languageCode);
            if (state == 2 && ingredient.indexOf(" ") > 0) {
                //Ingredient is composed from at least 2 words, test each one.
                SpannableStringBuilder ingredientSp = new SpannableStringBuilder(ingredient);
                String[] ingredientWords = ingredient.split(" ");
                for (int j = 0; j < ingredientWords.length; j++) {
                    String ingredientWord = ingredientWords[j];
                    state = dietTag.equalsIgnoreCase("enabled") ? minStateForEnabledDietFromIngredient(ingredientWord, languageCode) : stateFromIngredientDietTag(ingredientWord, dietTag, languageCode);
                    if (state != 2) {
                        int start = j == 0 ? 0 : ingredient.indexOf(" ",j);
                        int end = start + ingredientWord.length();
                        ingredientSp = coloredSpannableStringBuilderFromState(ingredientSp, start, end, state);
                    }
                }
                ingredientsSp.add(ingredientSp);
            } else {
                ingredientsSp.add(coloredSpannableStringBuilderFromState(new SpannableStringBuilder(ingredient), 0, ingredient.length(), state));
            }
        }
        return ingredientsSp;
    }

    private SpannableStringBuilder coloredSpannableStringBuilderFromState(SpannableStringBuilder ss, int start, int end, long state) {
        ss.setSpan(new ForegroundColorSpan(Color.parseColor(colors.get((int) (long) state))), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }


    /**
     * Return a String list ingredients from a text of ingredients
     *
     * @param ingredientsText    Ingrédients in a text form.
     * @param preserveAllSign    true : preserve oll Sign except _; false just preserve ingredient
     * @return List<String>      List of ingredients.
     *
     * @author dobriseb
     */
    @Override
    public List<String> getIngredientsListFromIngredientsText(String ingredientsText, boolean preserveAllSign){
        //Remove underscore "_".
        ingredientsText = ingredientsText.replaceAll("_","");
        List<String> ingredientsList = new ArrayList<String>();
        if (preserveAllSign) {
            ingredientsList.addAll(Arrays.asList(ingredientsText.replaceAll("([\\*\\n]|[ (]+\\p{Nd}.[\\.,]*\\p{Nd}*+[ %]*|\\s*[,():.]+\\s*)","_$1_").split("_")));
        } else {
            //Remove percent, underscore, asterisk, then split on coma",", point".", parentheses"()", colon":".
            ingredientsList.addAll(Arrays.asList(ingredientsText.replaceAll("[_\\*]|[ (]+\\p{Nd}.[\\.,]*\\p{Nd}*+[ %]*", "").split("\\s*[,():.]+\\s*")));
            //Remove blank (or so) lines
            for (int i = ingredientsList.size()-1; i >= 0; i--) {
                String s =  ingredientsList.get(i);
                //Log.i("SUPPRBLANK", "D"+s+"F");
                if (s.matches("^\\s*$")) {
                    ingredientsList.remove(i);
                }
            }
        }
        return ingredientsList;
    }

   /**
    * Return an highlighted SpannableStringBuilder from a list of dietIngredients and a languageCode.
    * Ingredients text is coloured according to their state in their relation to the diet.
    *
    * @param dietIngredientsList      The list of dietIngredient to be coloured
    * @param languageCode              The languageCode to be used
    * @return SpannableStringBuilder   A SpannableStringBuilder with coloured text.
    *
    * @author dobriseb
    */
    @Override
    public SpannableStringBuilder getSpannableStringBuilderFromDietIngredientsAndLanguageCode(List<DietIngredients> dietIngredientsList, String languageCode) {
        //Log.i("INFO", "Début de getSpannableStringBuilderFromDietIngredientsAndLanguageCode avec " + dietIngredientsList + " " + languageCode);
        if (dietIngredientsList != null) {
//        List<IngredientName> ingredientNames = new ArrayList<>();
            //The String that will contains the ingredients
            String ingredients = "";
            //Separated characters, first null then ", "
            String sep = "";
            //Color to be used
            ForegroundColorSpan color;
            //HashMap tha associate ingrédient/color
            HashMap<String, ForegroundColorSpan> ingredientColors = new HashMap<String, ForegroundColorSpan>();
            for (int i = 0; i < dietIngredientsList.size(); i++) {
                //For each dietIngredients
                DietIngredients dietIngredients =  dietIngredientsList.get(i);
                //Color is function of the state
                color = new ForegroundColorSpan(Color.parseColor(colors.get((int) (long) dietIngredients.getState())));
                //Looking for the name of the ingredient in the languageCode
                IngredientName ingredientName = getIngredientNameByIngredientTagAndLanguageCode(dietIngredients.getIngredientTag(), languageCode);
                //Add to the HashMap
                ingredientColors.put(ingredientName.getName(), color);
                //Add to the String ingredients
                ingredients = sep + ingredientName.getName();
                //Change sep for the next ingredients
                sep = ", ";
//            ingredientNames.add(getIngredientNameByIngredientTagAndLanguageCode(dietIngredients.getIngredientTag(), languageCode));
            }
            //Create a SpannableStringBuilder from the String ingredients
            SpannableStringBuilder ingredientsSS = new SpannableStringBuilder(ingredients);
            //For each couple in the HashMap
            for (String ingredient : ingredientColors.keySet()) {
                //Get the color
                color = ingredientColors.get(ingredient);
                //looking for the position of the ingredient in the string
                int start = (", " + ingredients).toLowerCase().indexOf(", " + ingredient.toLowerCase() + ",");
                //Lenght of the ingredient
                int length = ingredient.length();
                if (start >= 0) {
                    //Colorisation
                    ingredientsSS.setSpan(color, start, start + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            //Return the SpannableStringBuilder
            //Log.i("INFO", "Fin de getSpannableStringBuilderFromDietIngredientsAndLanguageCode : " + ingredientsSS.toString());
            return ingredientsSS;
        }
        //return an empty SpannableStringBuilder
        return new SpannableStringBuilder("");
    }
}