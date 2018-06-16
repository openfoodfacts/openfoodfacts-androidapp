package openfoodfacts.github.scrachx.openfood.views;


import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;

import holloway.allergenChecker.JSONManager;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.dagger.component.AppComponent;
import openfoodfacts.github.scrachx.openfood.dagger.module.AppModule;
import openfoodfacts.github.scrachx.openfood.models.DaoMaster;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.DatabaseHelper;

public class OFFApplication extends MultiDexApplication {

    public static DaoSession daoSession;
    private boolean DEBUG = false;

    private static OFFApplication application;
    private static AppComponent appComponent;

    public static AppComponent getAppComponent() {
        return appComponent;
    }

    public static OFFApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // Use only during development: DaoMaster.DevOpenHelper (Drops all table on Upgrade!)
        // Use only during production: DatabaseHelper (see on Upgrade!)
        String nameDB;
        if((BuildConfig.FLAVOR.equals("off"))) {
            nameDB = "open_food_facts";
        } else if ((BuildConfig.FLAVOR.equals("opff"))) {
            nameDB = "open_pet_food_facts";
        } else if ((BuildConfig.FLAVOR.equals("opf"))) {
            nameDB = "open_products_facts";
        }
        else

        {
            nameDB = "open_beauty_facts";
        }
        DatabaseHelper helper = new DatabaseHelper(this, nameDB);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

        // DEBUG
        QueryBuilder.LOG_VALUES = DEBUG;
        QueryBuilder.LOG_SQL = DEBUG;

        appComponent = AppComponent.Initializer.init(new AppModule(this));
        appComponent.inject(this);



        /* Initialize the Allergen Detector */
        File folder = new File(getFilesDir() + "/consumers");
        if (folder.exists()) {
            Log.i("ConsumerFragment", "Found existing consumers directory at" + getFilesDir());
        }
        if (!folder.exists()) {
            folder.mkdir();
            Log.i("ConsumerFragment", "Attempted to create directory 'consumers'");
        }

        JSONManager.getInstance().setConsumerJSONLocation(folder.toString());


        MediaScannerConnection.scanFile(this,
                new String[]{folder.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });


        /* End of allergen detector setup */
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}