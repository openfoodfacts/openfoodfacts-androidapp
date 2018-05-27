package openfoodfacts.github.scrachx.openfood.views;


import android.app.Application;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseOpenHelper;
import org.greenrobot.greendao.query.QueryBuilder;

import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.models.DaoMaster;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.DatabaseHelper;

public class OFFApplication extends Application {

    private DaoSession daoSession;
    private boolean DEBUG = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Use only during development: DaoMaster.DevOpenHelper (Drops all table on Upgrade!)
        // Use only during production: DatabaseHelper (see on Upgrade!)
        String nameDB = "";
        if((BuildConfig.FLAVOR.equals("off"))) {
            nameDB = "open_food_facts";
        } else
        if((BuildConfig.FLAVOR.equals("opff"))) {
            nameDB = "open_pet_food_facts";
        } else

            {
            nameDB = "open_beauty_facts";
        }
        DatabaseHelper helper = new DatabaseHelper(this, nameDB);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

        // DEBUG
        QueryBuilder.LOG_VALUES = DEBUG;
        QueryBuilder.LOG_SQL = DEBUG;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}