package openfoodfacts.github.scrachx.openfood.hilt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.schedulers.Schedulers
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.hilt.qualifiers.MainRetrofit
import openfoodfacts.github.scrachx.openfood.hilt.qualifiers.RobotoffRetrofit
import openfoodfacts.github.scrachx.openfood.hilt.qualifiers.WikiRetrofit
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECTION_TIMEOUT = 5L //sec
    private const val READ_WRITE_TIMEOUT = 30L //sec

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        // Our servers don't support TLS 1.3 therefore we need to create custom connectionSpec
        // with the correct ciphers to support network requests successfully on Android 7
        val connectionSpecModernTLS = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                .build()
        return OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .connectionSpecs(listOf(connectionSpecModernTLS, ConnectionSpec.COMPATIBLE_TLS))
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    } else {
                        addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                    }
                }
                .build()
    }

    @MainRetrofit
    @Provides
    @Singleton
    fun provideMainRetrofit(httpClient: OkHttpClient): Retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.HOST)
            .client(httpClient)
            .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()

    @WikiRetrofit
    @Provides
    @Singleton
    fun provideWikiRetrofit(httpClient: OkHttpClient): Retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.WIKIDATA)
            .client(httpClient)
            .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()

    @RobotoffRetrofit
    @Provides
    @Singleton
    fun provideRobotoffRetrofit(httpClient: OkHttpClient): Retrofit = Retrofit.Builder()
            .baseUrl("https://robotoff.openfoodfacts.org")
            .client(httpClient)
            .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
}
