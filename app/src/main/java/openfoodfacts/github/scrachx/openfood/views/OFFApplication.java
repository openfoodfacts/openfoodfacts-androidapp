/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.views;

import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.IOException;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.dagger.component.AppComponent;
import openfoodfacts.github.scrachx.openfood.dagger.module.AppModule;
import openfoodfacts.github.scrachx.openfood.models.DaoMaster;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.utils.AnalyticsService;
import openfoodfacts.github.scrachx.openfood.utils.OFFDatabaseHelper;

import static openfoodfacts.github.scrachx.openfood.AppFlavors.OBF;
import static openfoodfacts.github.scrachx.openfood.AppFlavors.OFF;
import static openfoodfacts.github.scrachx.openfood.AppFlavors.OPF;
import static openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF;

public class OFFApplication extends MultiDexApplication {
    private static DaoSession daoSession;
    public static final String LOG_TAG = OFFApplication.class.getSimpleName();
    private final boolean DEBUG = false;

    public static synchronized void setApplication(OFFApplication application) {
        OFFApplication.application = application;
    }

    public static synchronized void setAppComponent(AppComponent appComponent) {
        OFFApplication.appComponent = appComponent;
    }

    private static OFFApplication application;
    private static AppComponent appComponent;

    public static AppComponent getAppComponent() {
        return appComponent;
    }

    public static OFFApplication getInstance() {
        return application;
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }

    private static synchronized void setDaoSession(DaoSession session) {
        daoSession = session;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setApplication(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        AnalyticsService.init();

        // Use only during development: DaoMaster.DevOpenHelper (Drops all table on Upgrade!)
        // Use only during production: OFFDatabaseHelper (see on Upgrade!)
        String dbName;
        switch (BuildConfig.FLAVOR) {
            case OPFF:
                dbName = "open_pet_food_facts";
                break;
            case OBF:
                dbName = "open_beauty_facts";
                break;
            case OPF:
                dbName = "open_products_facts";
                break;
            case OFF:
            default:
                dbName = "open_food_facts";
                break;
        }

        setDaoSession(new DaoMaster(new OFFDatabaseHelper(this, dbName).getWritableDb()).newSession());

        // DEBUG
        QueryBuilder.LOG_VALUES = DEBUG;
        QueryBuilder.LOG_SQL = DEBUG;

        setAppComponent(AppComponent.Initializer.init(new AppModule(this)));
        getAppComponent().inject(this);

        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if (e instanceof IOException) {

                // fine, irrelevant network problem or API that throws on cancellation
                Log.i(LOG_TAG, "network exception", e);
                return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                Thread.currentThread().getUncaughtExceptionHandler()
                    .uncaughtException(Thread.currentThread(), e);
                return;
            }
            if (e instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().getUncaughtExceptionHandler()
                    .uncaughtException(Thread.currentThread(), e);
                return;
            }
            Log.w(LOG_TAG, "Undeliverable exception received, not sure what to do", e);
        });
    }
}
