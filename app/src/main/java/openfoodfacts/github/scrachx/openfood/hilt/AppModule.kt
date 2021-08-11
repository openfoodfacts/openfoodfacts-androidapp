package openfoodfacts.github.scrachx.openfood.hilt

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.category.network.CategoryNetworkService
import openfoodfacts.github.scrachx.openfood.models.DaoMaster
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.utils.OFFDatabaseHelper
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideDaoSession(@ApplicationContext context: Context): DaoSession {
        // Use only during development: DaoMaster.DevOpenHelper (Drops all table on Upgrade!)
        // Use only during production: OFFDatabaseHelper (see on Upgrade!)
        val dbName = when (BuildConfig.FLAVOR) {
            AppFlavors.OPFF -> "open_pet_food_facts"
            AppFlavors.OBF -> "open_beauty_facts"
            AppFlavors.OPF -> "open_products_facts"
            AppFlavors.OFF -> "open_food_facts"
            else -> "open_food_facts"
        }
        return DaoMaster(OFFDatabaseHelper(context, dbName).writableDb).newSession()
    }

    @Provides
    fun provideCategoryNetworkService(retrofit: Retrofit): CategoryNetworkService =
            retrofit.create(CategoryNetworkService::class.java)


    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
}
