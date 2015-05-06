package openfoodfacts.github.scrachx.openfood.network;

import com.loopj.android.http.*;

/**
 * Created by scotscriven on 30/04/15.
 */
public class FoodAPIRestClient {

    private static final String BASE_URL = "http://fr.openfoodfacts.org/api/v0/produit/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

}
