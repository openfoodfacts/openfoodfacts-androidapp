package openfoodfacts.github.scrachx.openfood.network;

import android.os.Build;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

public interface APIUtils {

    String GET_API = "https://world.openfoodfacts.org";


    default OkHttpClient HttpClientBuilder() {
        return Utils.HttpClientBuilder();
    }
}

