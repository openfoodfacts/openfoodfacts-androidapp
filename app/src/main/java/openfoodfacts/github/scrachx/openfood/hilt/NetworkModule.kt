package openfoodfacts.github.scrachx.openfood.hilt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.hilt.qualifiers.MainRetrofit
import openfoodfacts.github.scrachx.openfood.hilt.qualifiers.RobotoffRetrofit
import openfoodfacts.github.scrachx.openfood.hilt.qualifiers.WikiRetrofit
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.HttpLoggingInterceptor
import retrofit2.Retrofit
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
        val connectionSpecModernTLS = ConnectionSpec(ConnectionSpec.MODERN_TLS) {
            tlsVersions(TlsVersion.TLS_1_2)
            cipherSuites(
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
            )
        }

        return OkHttpClient {
            connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS)
            connectionSpecs(listOf(connectionSpecModernTLS, ConnectionSpec.COMPATIBLE_TLS))

            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor { level = HttpLoggingInterceptor.Level.BODY })
                authenticator { _, response ->
                    response.request().buildUpon {
                        header("Authorization", Credentials.basic("off", "off"))
                    }
                }
            } else {
                addInterceptor(HttpLoggingInterceptor { level = HttpLoggingInterceptor.Level.BASIC })
            }
        }
    }

    @MainRetrofit
    @Provides
    @Singleton
    fun provideMainRetrofit(httpClient: OkHttpClient): Retrofit {
        return defaultRetrofitBuilder(httpClient) { baseUrl(BuildConfig.HOST) }
    }

    @WikiRetrofit
    @Provides
    @Singleton
    fun provideWikiRetrofit(httpClient: OkHttpClient): Retrofit {
        return defaultRetrofitBuilder(httpClient) { baseUrl(BuildConfig.WIKIDATA) }
    }

    @RobotoffRetrofit
    @Provides
    @Singleton
    fun provideRobotoffRetrofit(httpClient: OkHttpClient): Retrofit {
        return defaultRetrofitBuilder(httpClient) { baseUrl("https://robotoff.openfoodfacts.org") }
    }

    private inline fun defaultRetrofitBuilder(
        client: OkHttpClient,
        crossinline builderAction: Retrofit.Builder.() -> Unit,
    ) = Retrofit {
        client(client)
        addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
        builderAction()
    }
}
