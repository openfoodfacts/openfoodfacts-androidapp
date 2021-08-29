package openfoodfacts.github.scrachx.openfood.hilt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.schedulers.Schedulers
import okhttp3.*
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
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
            )
            .build()
        return OkHttpClient.Builder().apply {
            connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS)
            connectionSpecs(listOf(connectionSpecModernTLS, ConnectionSpec.COMPATIBLE_TLS))

            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                authenticator { _, response ->
                    response.request()
                        .newBuilder()
                        .header("Authorization", Credentials.basic("off", "off"))
                        .build()
                }
            } else {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }.build()
    }

    @MainRetrofit
    @Provides
    @Singleton
    fun provideMainRetrofit(httpClient: OkHttpClient): Retrofit =
        httpClient.createDefaultRetrofit()
            .baseUrl(BuildConfig.HOST)
            .build()

    @WikiRetrofit
    @Provides
    @Singleton
    fun provideWikiRetrofit(httpClient: OkHttpClient): Retrofit =
        httpClient.createDefaultRetrofit()
            .baseUrl(BuildConfig.WIKIDATA)
            .build()

    @RobotoffRetrofit
    @Provides
    @Singleton
    fun provideRobotoffRetrofit(httpClient: OkHttpClient): Retrofit =
        httpClient.createDefaultRetrofit()
            .baseUrl("https://robotoff.openfoodfacts.org")
            .build()

    private fun OkHttpClient.createDefaultRetrofit() = Retrofit.Builder()
        .client(this)
        .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
}
