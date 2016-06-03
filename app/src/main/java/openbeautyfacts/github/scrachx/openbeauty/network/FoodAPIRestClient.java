package openbeautyfacts.github.scrachx.openfood.network;

import com.loopj.android.http.*;
import java.util.Locale;

public class FoodAPIRestClient {

    private static String BASE_URL = "http://world.openbeautyfacts.org";

    private static AsyncHttpClient client = new AsyncHttpClient();
    private static AsyncHttpClient clientS = new SyncHttpClient();

    public static void getAsync(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setTimeout(5000);
        client.setResponseTimeout(70000);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void getSync(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        clientS.setTimeout(5000);
        clientS.setResponseTimeout(70000);
        clientS.get(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        /*if (Locale.getDefault().getLanguage().contains("fr")){
            BASE_URL = "http://fr.openbeautyfacts.org/";
        }*/
        return BASE_URL + relativeUrl;
    }


}
