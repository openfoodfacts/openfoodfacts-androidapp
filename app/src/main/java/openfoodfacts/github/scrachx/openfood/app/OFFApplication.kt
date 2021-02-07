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
package openfoodfacts.github.scrachx.openfood.app

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.dagger.component.AppComponent
import openfoodfacts.github.scrachx.openfood.dagger.component.AppComponent.Initializer.init
import openfoodfacts.github.scrachx.openfood.dagger.module.AppModule
import openfoodfacts.github.scrachx.openfood.models.DaoMaster
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.utils.OFFDatabaseHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.greendao.query.QueryBuilder
import java.io.IOException
import openfoodfacts.github.scrachx.openfood.app.AnalyticsService.init as initAnalytics

class OFFApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initAnalytics()

        EventBus.builder().addIndex(OFFEventsIndex()).installDefaultEventBus()

        // Use only during development: DaoMaster.DevOpenHelper (Drops all table on Upgrade!)
        // Use only during production: OFFDatabaseHelper (see on Upgrade!)
        val dbName = when (BuildConfig.FLAVOR) {
            OPFF -> "open_pet_food_facts"
            OBF -> "open_beauty_facts"
            OPF -> "open_products_facts"
            OFF -> "open_food_facts"
            else -> "open_food_facts"
        }
        daoSession = DaoMaster(OFFDatabaseHelper(this, dbName).writableDb).newSession()

        // DEBUG
        QueryBuilder.LOG_VALUES = DEBUG
        QueryBuilder.LOG_SQL = DEBUG
        appComponent = init(AppModule(this))
        appComponent.inject(this)
        RxJavaPlugins.setErrorHandler {
            when (it) {
                is UndeliverableException ->
                    Log.w(LOG_TAG, "Undeliverable exception received, not sure what to do", it.cause)
                is IOException -> {
                    // fine, irrelevant network problem or API that throws on cancellation
                    Log.i(LOG_TAG, "network exception", it)
                    return@setErrorHandler
                }
                is InterruptedException -> {
                    // fine, some blocking code was interrupted by a dispose call
                    return@setErrorHandler
                }
                is NullPointerException, is IllegalArgumentException, is IllegalStateException -> {
                    // that's likely a bug in the application
                    Thread.currentThread().uncaughtExceptionHandler!!
                            .uncaughtException(Thread.currentThread(), it)
                    return@setErrorHandler
                }
            }
        }
    }

    companion object {
        private const val DEBUG = false
        val LOG_TAG = OFFApplication::class.simpleName!!

        @JvmStatic
        lateinit var daoSession: DaoSession
            @Synchronized
            private set

        lateinit var instance: OFFApplication
            @Synchronized
            private set
        @JvmStatic
        lateinit var appComponent: AppComponent
            @Synchronized
            private set

    }
}