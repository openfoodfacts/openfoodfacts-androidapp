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

import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import org.greenrobot.eventbus.EventBus
import org.greenrobot.greendao.query.QueryBuilder
import java.io.IOException
import javax.inject.Inject

@HiltAndroidApp
class OFFApplication : MultiDexApplication(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var daoSession: DaoSession

    override fun getWorkManagerConfiguration() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()


    @Suppress("DEPRECATION")
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        EventBus.builder().addIndex(OFFEventsIndex()).installDefaultEventBus()

        // DEBUG
        QueryBuilder.LOG_VALUES = DEBUG
        QueryBuilder.LOG_SQL = DEBUG
        RxJavaPlugins.setErrorHandler {
            when (it) {
                is UndeliverableException ->
                    logcat(LogPriority.WARN) { "Undeliverable exception received, not sure what to do: ${it.cause}" }
                is IOException -> {
                    // fine, irrelevant network problem or API that throws on cancellation
                    logcat(LogPriority.WARN) { "IO Exception: " + it.asLog() }
                }
                is InterruptedException -> {
                    // fine, some blocking code was interrupted by a dispose call
                }
                is NullPointerException, is IllegalArgumentException, is IllegalStateException -> {
                    // that's likely a bug in the application
                    Thread.currentThread().uncaughtExceptionHandler!!
                        .uncaughtException(Thread.currentThread(), it)
                }
            }
        }
    }

    companion object {
        private const val DEBUG = false
    }
}
