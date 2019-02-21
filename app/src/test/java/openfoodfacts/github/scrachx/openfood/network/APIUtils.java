package openfoodfacts.github.scrachx.openfood.network;

import android.os.Build;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;

public interface APIUtils {

    String GET_API = "https://world.openfoodfacts.org";

    String POST_API = "https://world.openfoodfacts.net";

    String POST_LOGIN = "off";

    String POST_PASSWORD = "off";

    HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    default OkHttpClient HttpClientBuilder() {
        OkHttpClient httpClient;
        if (Build.VERSION.SDK_INT == 24) {
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .cipherSuites(CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)

                    .build();

            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(5000, TimeUnit.MILLISECONDS)
                    .readTimeout(30000, TimeUnit.MILLISECONDS)
                    .writeTimeout(30000, TimeUnit.MILLISECONDS)
                    .connectionSpecs(Collections.singletonList(spec))
                    .addInterceptor(logging)
                    .build();
        } else {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(5000, TimeUnit.MILLISECONDS)
                    .readTimeout(30000, TimeUnit.MILLISECONDS)
                    .writeTimeout(30000, TimeUnit.MILLISECONDS)
                    .addInterceptor(logging)
                    .build();
        }
        return httpClient;

    }
}

