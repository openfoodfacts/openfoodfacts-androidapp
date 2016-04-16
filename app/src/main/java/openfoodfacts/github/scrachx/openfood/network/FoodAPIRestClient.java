package openfoodfacts.github.scrachx.openfood.network;

import com.loopj.android.http.*;

import java.util.Locale;

/**
 * Created by scotscriven on 30/04/15.
 */
public class FoodAPIRestClient {

    private static String BASE_URL = "http://world.openfoodfacts.org/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setTimeout(5000);
        client.setResponseTimeout(70000);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        if (Locale.getDefault().getLanguage().contains("fr")){
            BASE_URL = "http://fr.openfoodfacts.org/api/v0/produit/";
        }
        return BASE_URL + relativeUrl;
    }


}
