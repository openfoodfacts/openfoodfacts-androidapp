package openfoodfacts.github.scrachx.openfood.network;

import okhttp3.OkHttpClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

public interface APIUtils {

    String GET_API = "https://world.openfoodfacts.org";
    String DEV_API = "https://world.openfoodfacts.dev";


    default OkHttpClient HttpClientBuilder() {
        return Utils.buildHttpClient();
    }
}

