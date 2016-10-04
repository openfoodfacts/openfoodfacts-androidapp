package openfoodfacts.github.scrachx.openfood.network;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

/**
 * OpenFoodFacts Rest Client using Async HTTP
 */
public class FoodAPIRestClient {

    private static AsyncHttpClient client = new AsyncHttpClient();

    static {
        client.setTimeout(5000);
        client.setResponseTimeout(70000);
    }

    /**
     * Perform a Async HTTP GET request with parameters.
     * @param url url to send the request
     * @param params request parameters
     * @param responseHandler
     */
    public static void get(String url, RequestParams params, ResponseHandlerInterface responseHandler) {
        client.get(url, params, responseHandler);
    }

    /**
     * Perform a Async HTTP POST request with parameters.
     * @param url url to send the request
     * @param params request parameters
     * @param responseHandler
     */
    public static void post(String url, RequestParams params, ResponseHandlerInterface responseHandler) {
        client.post(url, params, responseHandler);
    }

}
