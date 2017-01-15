package openfoodfacts.github.scrachx.openfood.views;


import android.app.Application;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseOpenHelper;
import org.greenrobot.greendao.query.QueryBuilder;

import openfoodfacts.github.scrachx.openfood.models.DaoMaster;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.ProductionDbOpenHelper;

public class OFFApplication extends Application {

    private DaoSession daoSession;
    private boolean DEBUG = true;

    @Override
    public void onCreate() {
        super.onCreate();

        ProductionDbOpenHelper helper = new ProductionDbOpenHelper(this, "open_food_facts");
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