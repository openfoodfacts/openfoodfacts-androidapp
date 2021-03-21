package openfoodfacts.github.scrachx.openfood.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import openfoodfacts.github.scrachx.openfood.hilt.qualifiers.MainRetrofit
import openfoodfacts.github.scrachx.openfood.hilt.qualifiers.RobotoffRetrofit
import openfoodfacts.github.scrachx.openfood.hilt.qualifiers.WikiRetrofit
import openfoodfacts.github.scrachx.openfood.network.services.AnalysisDataAPI
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.network.services.RobotoffAPI
import openfoodfacts.github.scrachx.openfood.network.services.WikidataAPI
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideProductsAPI(@MainRetrofit retrofit: Retrofit): ProductsAPI = retrofit.create(ProductsAPI::class.java)

    @Provides
    @Singleton
    fun provideWikiDataAPI(@WikiRetrofit retrofit: Retrofit): WikidataAPI = retrofit.create(WikidataAPI::class.java)

    @Provides
    @Singleton
    fun provideAnalysisDataApi(@MainRetrofit retrofit: Retrofit): AnalysisDataAPI = retrofit.create(AnalysisDataAPI::class.java)

    @Provides
    @Singleton
    fun robotoffApi(@RobotoffRetrofit retrofit: Retrofit): RobotoffAPI = retrofit.create(RobotoffAPI::class.java)
}
