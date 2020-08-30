package openfoodfacts.github.scrachx.openfood.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.greenrobot.greendao.database.Database;

import openfoodfacts.github.scrachx.openfood.models.DaoMaster;
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcodeDao;
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProductDao;
import openfoodfacts.github.scrachx.openfood.models.entities.ProductListsDao;
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProductDao;
import openfoodfacts.github.scrachx.openfood.models.entities.YourListedProductDao;
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveDao;
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveNameDao;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenDao;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenNameDao;
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagDao;
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagNameDao;
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfigDao;
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryDao;
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryNameDao;
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryDao;
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameDao;
import openfoodfacts.github.scrachx.openfood.models.entities.ingredient.IngredientDao;
import openfoodfacts.github.scrachx.openfood.models.entities.ingredient.IngredientNameDao;
import openfoodfacts.github.scrachx.openfood.models.entities.ingredient.IngredientsRelationDao;
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelDao;
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameDao;
import openfoodfacts.github.scrachx.openfood.models.entities.tag.TagDao;

public class OFFDatabaseHelper extends DaoMaster.OpenHelper {
    private final SharedPreferences settings;

    public OFFDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);

        settings = context.getSharedPreferences("prefs", 0);
    }

    public OFFDatabaseHelper(Context context, String name) {
        super(context, name);

        settings = context.getSharedPreferences("prefs", 0);
    }

    @Override
    public void onCreate(Database db) {
        Log.i("greenDAO", "Creating tables for schema version " + DaoMaster.SCHEMA_VERSION);
        DaoMaster.createAllTables(db, true);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        Log.i("greenDAO", "migrating schema from version " + oldVersion + " to " + newVersion);
        //dropAllTables(db, true);
        for (int migrateVersion = oldVersion + 1; migrateVersion <= newVersion; migrateVersion++) {
            migrateDB(db, migrateVersion);
        }

        //db model has changed we need to invalidate and reload taxonomies
        if (settings != null && oldVersion != newVersion) {
            settings.edit().putBoolean(Utils.FORCE_REFRESH_TAXONOMIES, true).apply();
        }
    }

    /**
     * in case of android.database.sqlite.SQLiteException, the schema version is
     * left untouched just fix the code in the version case and push a new
     * release
     *
     * @param db database
     * @param migrateVersion
     */
    private void migrateDB(Database db, int migrateVersion) {
        Log.e("MIGRATE VERSION", "" + migrateVersion);
        switch (migrateVersion) {
            case 2:
                db.execSQL("ALTER TABLE send_product ADD COLUMN 'lang' TEXT NOT NULL DEFAULT 'fr';");
                break;
            case 3:
                ToUploadProductDao.createTable(db, true);
                break;
            case 4:
                TagDao.createTable(db, true);
                break;
            case 5: {
                db.execSQL("ALTER TABLE history_product ADD COLUMN 'quantity' TEXT NOT NULL DEFAULT '';");
                db.execSQL("ALTER TABLE history_product ADD COLUMN 'nutrition_grade' TEXT NOT NULL DEFAULT '';");
                break;
            }
            case 6: {
                LabelDao.createTable(db, true);
                LabelNameDao.createTable(db, true);

                AllergenDao.dropTable(db, true);
                AllergenDao.createTable(db, true);
                AllergenNameDao.createTable(db, true);

                AdditiveDao.dropTable(db, true);
                AdditiveDao.createTable(db, true);
                AdditiveNameDao.createTable(db, true);

                CountryDao.createTable(db, true);
                CountryNameDao.createTable(db, true);

                CategoryDao.createTable(db, true);
                CategoryNameDao.createTable(db, true);
                break;
            }
            case 7: {
                String[] newColumns = new String[]{"wiki_data_id", "is_wiki_data_id_present"};
                String[] updatedTables = new String[]{"additive_name", "additive", "category_name", "category", "label_name", "label"};
                for (String table : updatedTables) {
                    for (String column : newColumns) {
                        if (!isFieldExist(db, table, column)) {
                            db.execSQL(String.format("ALTER TABLE %s ADD COLUMN '%s' TEXT NOT NULL DEFAULT '';", table, column));
                        }
                    }
                }

                break;
            }
            case 8: {
                OfflineSavedProductDao.createTable(db, true);
                break;
            }
            case 9: {
                String[] newColumns = new String[]{"overexposure_risk", "exposure_mean_greater_than_adi", "exposure_mean_greater_than_noael",
                    "exposure95_th_greater_than_adi", "exposure95_th_greater_than_noael"};
                String[] updatedTables = new String[]{"additive_name", "additive"};
                for (String table : updatedTables) {
                    for (String column : newColumns) {
                        if (!isFieldExist(db, table, column)) {
                            db.execSQL(String.format("ALTER TABLE %s ADD COLUMN '%s' TEXT;", table, column));
                        }
                    }
                }
                break;
            }
            case 10:
                String[] newColumns = new String[]{"WIKI_DATA_ID", "IS_WIKI_DATA_ID_PRESENT"};
                String[] updatedTables = new String[]{"allergen_name", "allergen"};
                for (String table : updatedTables) {
                    for (String column : newColumns) {
                        if (!isFieldExist(db, table, column)) {
                            db.execSQL(String.format("ALTER TABLE %s ADD COLUMN '%s' TEXT NOT NULL DEFAULT '';", table, column));
                        }
                    }
                }
                break;
            case 11:
                ProductListsDao.createTable(db, true);
                YourListedProductDao.createTable(db, true);
                break;
            //12 -13 - 14 - issue with merge and bad numerotation
            case 15:
                IngredientDao.createTable(db, true);
                IngredientNameDao.createTable(db, true);
                IngredientsRelationDao.createTable(db, true);
                AnalysisTagNameDao.createTable(db, true);
                AnalysisTagDao.createTable(db, true);
                AnalysisTagConfigDao.createTable(db, true);
                break;
            case 16:
                InvalidBarcodeDao.createTable(db, true);
                break;
            case 17:
                db.execSQL("ALTER TABLE OFFLINE_SAVED_PRODUCT ADD COLUMN 'IS_DATA_UPLOADED' BOOLEAN NOT NULL DEFAULT FALSE;");
                break;
            case 18: {
                db.execSQL("ALTER TABLE COUNTRY ADD COLUMN 'CC2' TEXT");
                db.execSQL("ALTER TABLE COUNTRY ADD COLUMN 'CC3' TEXT");
                break;
            }
        }
    }

    private boolean isFieldExist(Database db, String tableName, String fieldName) {
        boolean isExist = false;
        String query = String.format("PRAGMA table_info(%s)", tableName);
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();
        do {
            String currentColumn = res.getString(1);
            if (currentColumn.equals(fieldName)) {
                isExist = true;
            }
        } while (res.moveToNext());

        return isExist;
    }
}
