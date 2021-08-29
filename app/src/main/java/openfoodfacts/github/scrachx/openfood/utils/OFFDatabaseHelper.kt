package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.util.Log
import androidx.core.content.edit
import openfoodfacts.github.scrachx.openfood.models.DaoMaster
import openfoodfacts.github.scrachx.openfood.models.DaoMaster.OpenHelper
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcodeDao
import openfoodfacts.github.scrachx.openfood.models.entities.ListedProductDao
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProductDao
import openfoodfacts.github.scrachx.openfood.models.entities.ProductListsDao
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProductDao
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveDao
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenDao
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagDao
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfigDao
import openfoodfacts.github.scrachx.openfood.models.entities.brand.BrandDao
import openfoodfacts.github.scrachx.openfood.models.entities.brand.BrandNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryDao
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryDao
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.ingredient.IngredientDao
import openfoodfacts.github.scrachx.openfood.models.entities.ingredient.IngredientNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.ingredient.IngredientsRelationDao
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelDao
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.states.StatesDao
import openfoodfacts.github.scrachx.openfood.models.entities.states.StatesNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.store.StoreDao
import openfoodfacts.github.scrachx.openfood.models.entities.store.StoreNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.tag.TagDao
import org.greenrobot.greendao.database.Database

class OFFDatabaseHelper @JvmOverloads constructor(
        context: Context,
        name: String,
        factory: CursorFactory? = null
) : OpenHelper(context, name, factory) {
    private val settings: SharedPreferences by lazy { context.getAppPreferences() }

    override fun onCreate(db: Database) {
        Log.i(LOG_TAG, "Creating tables for schema version ${DaoMaster.SCHEMA_VERSION}")
        DaoMaster.createAllTables(db, true)
    }

    override fun onUpgrade(db: Database, oldVersion: Int, newVersion: Int) {
        Log.i(LOG_TAG, "Migrating schema from version $oldVersion to $newVersion")
        //dropAllTables(db, true);

        for (migrateVersion in oldVersion + 1..newVersion) migrateDB(db, migrateVersion)

        //db model has changed we need to invalidate and reload taxonomies
        if (oldVersion != newVersion) {
            settings.edit { putBoolean(Utils.FORCE_REFRESH_TAXONOMIES, true) }
        }
    }

    /**
     * In case of android.database.sqlite.SQLiteException, the schema version is
     * left untouched just fix the code in the version case and push a new
     * release
     *
     * @param db database
     * @param migrateVersion
     */
    private fun migrateDB(db: Database, migrateVersion: Int) {
        Log.e("MIGRATE VERSION", migrateVersion.toString())
        when (migrateVersion) {
            2 -> db.execSQL("ALTER TABLE send_product ADD COLUMN 'lang' TEXT NOT NULL DEFAULT 'fr';")
            3 -> ToUploadProductDao.createTable(db, true)
            4 -> TagDao.createTable(db, true)
            5 -> {
                db.execSQL("ALTER TABLE history_product ADD COLUMN 'quantity' TEXT NOT NULL DEFAULT '';")
                db.execSQL("ALTER TABLE history_product ADD COLUMN 'nutrition_grade' TEXT NOT NULL DEFAULT '';")
            }
            6 -> {
                LabelDao.createTable(db, true)
                LabelNameDao.createTable(db, true)
                AllergenDao.dropTable(db, true)
                AllergenDao.createTable(db, true)
                AllergenNameDao.createTable(db, true)
                AdditiveDao.dropTable(db, true)
                AdditiveDao.createTable(db, true)
                AdditiveNameDao.createTable(db, true)
                CountryDao.createTable(db, true)
                CountryNameDao.createTable(db, true)
                CategoryDao.createTable(db, true)
                CategoryNameDao.createTable(db, true)
            }
            7 -> {
                val newColumns = listOf("wiki_data_id", "is_wiki_data_id_present")
                val updatedTables = listOf("additive_name", "additive", "category_name", "category", "label_name", "label")
                updatedTables.forEach { table ->
                    newColumns.filterNot { isFieldExist(db, table, it) }
                            .forEach { column ->
                                db.execSQL("ALTER TABLE $table ADD COLUMN '$column' TEXT NOT NULL DEFAULT '';")
                            }
                }
            }
            8 -> {
                OfflineSavedProductDao.createTable(db, true)
            }
            9 -> {
                listOf("additive_name", "additive").forEach { table ->
                    listOf("overexposure_risk", "exposure_mean_greater_than_adi", "exposure_mean_greater_than_noael",
                            "exposure95_th_greater_than_adi", "exposure95_th_greater_than_noael")
                            .filterNot { isFieldExist(db, table, it) }
                            .forEach { db.execSQL("ALTER TABLE $table ADD COLUMN '$it' TEXT;") }
                }
            }
            10 -> {
                listOf("allergen_name", "allergen").forEach { table ->
                    listOf("WIKI_DATA_ID", "IS_WIKI_DATA_ID_PRESENT")
                            .filterNot { isFieldExist(db, table, it) }
                            .forEach { column ->
                                db.execSQL("ALTER TABLE $table ADD COLUMN '$column' TEXT NOT NULL DEFAULT '';")
                            }
                }
            }
            11 -> {
                ProductListsDao.createTable(db, true)
                ListedProductDao.createTable(db, true)
            }
            15 -> {
                IngredientDao.createTable(db, true)
                IngredientNameDao.createTable(db, true)
                IngredientsRelationDao.createTable(db, true)
                AnalysisTagNameDao.createTable(db, true)
                AnalysisTagDao.createTable(db, true)
                AnalysisTagConfigDao.createTable(db, true)
            }
            16 -> InvalidBarcodeDao.createTable(db, true)
            17 -> db.execSQL("ALTER TABLE OFFLINE_SAVED_PRODUCT ADD COLUMN 'IS_DATA_UPLOADED' BOOLEAN NOT NULL DEFAULT FALSE;")
            18 -> {
                db.execSQL("ALTER TABLE COUNTRY ADD COLUMN 'CC2' TEXT;")
                db.execSQL("ALTER TABLE COUNTRY ADD COLUMN 'CC3' TEXT;")
            }
            19 -> {
                StatesDao.createTable(db, true)
                StatesNameDao.createTable(db, true)
            }
            20 -> {
                db.execSQL("ALTER TABLE HISTORY_PRODUCT ADD COLUMN 'ECOSCORE' TEXT;")
                db.execSQL("ALTER TABLE HISTORY_PRODUCT ADD COLUMN 'NOVA_GROUP' TEXT;")
            }
            21 -> {
                StoreDao.createTable(db, true)
                StoreNameDao.createTable(db, true)
            }
            22 -> {
                BrandDao.createTable(db, true)
                BrandNameDao.createTable(db, true)
            }
        }
    }

    private fun isFieldExist(db: Database, tableName: String, fieldName: String): Boolean {
        val res = db.rawQuery("PRAGMA table_info($tableName)", null)
        res.moveToFirst()
        do {
            val currentColumn = res.getString(1)
            if (currentColumn == fieldName) return true
        } while (res.moveToNext())
        return false
    }

    companion object {
        private const val LOG_TAG = "greenDAO"
    }
}