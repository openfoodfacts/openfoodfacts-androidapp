package openfoodfacts.github.scrachx.openfood.hilt

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.category.network.CategoryNetworkService
import openfoodfacts.github.scrachx.openfood.models.DaoMaster
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.utils.OFFDatabaseHelper
import retrofit2.Retrofit
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideDaoSession(@ApplicationContext context: Context): DaoSession {
        // Use only during development: DaoMaster.DevOpenHelper (Drops all table on Upgrade!)
        // Use only during production: OFFDatabaseHelper (see on Upgrade!)
        return DaoMaster(OFFDatabaseHelper(context).writableDb).newSession()
    }

    @Provides
    fun provideCategoryNetworkService(retrofit: Retrofit): CategoryNetworkService =
            retrofit.create(CategoryNetworkService::class.java)

    @Provides
    @Singleton
    fun providePicasso(@ApplicationContext context: Context, httpClient: OkHttpClient): Picasso {
        val cacheSize: Long = 50 * 1024 * 1024
        val cacheDir = File(context.cacheDir, "http-cache")
        val httpClientWithCache = httpClient.newBuilder()
                .cache(Cache(cacheDir, cacheSize))
                .build()
        return Picasso.Builder(context)
                .downloader(OkHttp3Downloader(httpClientWithCache))
                .build()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
}
