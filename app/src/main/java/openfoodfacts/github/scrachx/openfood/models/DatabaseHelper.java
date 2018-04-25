package openfoodfacts.github.scrachx.openfood.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.greenrobot.greendao.database.Database;

public class DatabaseHelper extends DaoMaster.OpenHelper {

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    public DatabaseHelper(Context context, String name) {
        super(context, name);
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
            upgrade(db, migrateVersion);
        }

    }

    /**
     * in case of android.database.sqlite.SQLiteException, the schema version is
     * left untouched just fix the code in the version case and push a new
     * release
     *
     * @param db             database
     * @param migrateVersion
     */
    private void upgrade(Database db, int migrateVersion) {
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
                String wikiDataID = "wiki_data_id";
                String isWikiDataPresent = "is_wiki_data_id_present";

                if (columnIsNew(db, "additive_name", wikiDataID)) {
                    db.execSQL("ALTER TABLE additive_name ADD COLUMN 'wiki_data_id' TEXT NOT NULL DEFAULT '';");
                }

                if (columnIsNew(db, "additive_name", isWikiDataPresent)) {
                    db.execSQL("ALTER TABLE additive_name ADD COLUMN 'is_wiki_data_id_present' INTEGER NOT NULL DEFAULT '';");
                }

                if (columnIsNew(db, "additive", wikiDataID)) {
                    db.execSQL("ALTER TABLE additive ADD COLUMN 'wiki_data_id' TEXT NOT NULL DEFAULT '';");
                }

                if (columnIsNew(db, "additive", isWikiDataPresent)) {
                    db.execSQL("ALTER TABLE additive ADD COLUMN 'is_wiki_data_id_present' INTEGER NOT NULL DEFAULT '';");
            }

                if (columnIsNew(db, "category_name", wikiDataID)) {
                    db.execSQL("ALTER TABLE category_name ADD COLUMN 'wiki_data_id' TEXT NOT NULL DEFAULT '';");
                }

                if (columnIsNew(db, "category_name", isWikiDataPresent)) {
                    db.execSQL("ALTER TABLE category_name ADD COLUMN 'is_wiki_data_id_present' INTEGER NOT NULL DEFAULT '';");
                }

                if (columnIsNew(db, "category", wikiDataID)) {
                    db.execSQL("ALTER TABLE category ADD COLUMN 'wiki_data_id' TEXT NOT NULL DEFAULT '';");
                }

                if (columnIsNew(db, "category", isWikiDataPresent)) {
                    db.execSQL("ALTER TABLE category ADD COLUMN 'is_wiki_data_id_present' INTEGER NOT NULL DEFAULT '';");
                }

                if (columnIsNew(db, "label_name", wikiDataID)) {
                    db.execSQL("ALTER TABLE label_name ADD COLUMN 'wiki_data_id' TEXT NOT NULL DEFAULT '';");
                }

                if (columnIsNew(db, "label_name", isWikiDataPresent)) {
                    db.execSQL("ALTER TABLE label_name ADD COLUMN 'is_wiki_data_id_present' INTEGER NOT NULL DEFAULT '';");
                }

                if (columnIsNew(db, "label", wikiDataID)) {
                    db.execSQL("ALTER TABLE label ADD COLUMN 'wiki_data_id' TEXT NOT NULL DEFAULT '';");
                }

                if (columnIsNew(db, "label", isWikiDataPresent)) {
                    db.execSQL("ALTER TABLE label ADD COLUMN 'is_wiki_data_id_present' INTEGER NOT NULL DEFAULT '';");
                }
                
                break;
            }
        }
    }

    /**
     * Helper method to prevent SQLite Exception for duplicate columns. {@Link https://stackoverflow.com/questions/18920136/check-if-a-column-exists-in-sqlite/45774056#45774056 }
     *
     * @param database     The database to check
     * @param tableName    The name of the table
     * @param columnToFind The column to check for
     * @return false if the column is found in the table already.
     */
    private boolean columnIsNew(Database database,
                                String tableName,
                                String columnToFind) {
        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "PRAGMA table_info(" + tableName + ")",
                    null
            );

            int nameColumnIndex = cursor.getColumnIndexOrThrow("name");

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameColumnIndex);

                if (name.equals(columnToFind)) {
                    return false;
                }
            }

            return true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}