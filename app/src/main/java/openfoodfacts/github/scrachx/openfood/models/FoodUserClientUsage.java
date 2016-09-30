package openfoodfacts.github.scrachx.openfood.models;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.FoodAPIRestClient;
import openfoodfacts.github.scrachx.openfood.views.adapters.SaveListAdapter;

public class FoodUserClientUsage {

    public interface OnProductSentCallback {
        void onProductSentResponse(boolean value);
    }

    public void post(final Activity activity, RequestParams params, final String imgFront, final String imgIng, final String imgNut, final String barcode, final OnProductSentCallback productSentCallback){
        FoodAPIRestClient.post(getApiUrl(activity) + "/cgi/product_jqm2.pl", params, new JsonHttpResponseHandler() {

            LoadToast lt = new LoadToast(activity);

            @Override
            public void onStart() {
                // called before request is started
                lt.setText(activity.getString(R.string.toastSending));
                lt.setBackgroundColor(activity.getResources().getColor(R.color.indigo_600));
                lt.setTextColor(activity.getResources().getColor(R.color.white));
                lt.show();
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {
                    int status = response.getInt("status");
                    if (status == 0) {
                        lt.error();
                        productSentCallback.onProductSentResponse(false);
                    } else {
                        lt.success();
                        if (!imgFront.isEmpty()) {
                            RequestParams paramsImgF = new RequestParams();
                            File myFileFront = new File(imgFront);
                            paramsImgF.put("code", barcode);
                            paramsImgF.put("imagefield", "front");
                            try {
                                paramsImgF.put("imgupload_front", myFileFront);
                            } catch (FileNotFoundException e) {
                                e.getMessage();
                            }
                            postImg(activity, paramsImgF);
                        }

                        if (!imgIng.isEmpty()) {
                            RequestParams paramsImgI = new RequestParams();
                            File myFileIng = new File(imgIng);
                            paramsImgI.put("code", barcode);
                            paramsImgI.put("imagefield", "ingredients");
                            try {
                                paramsImgI.put("imgupload_ingredients", myFileIng);
                            } catch (FileNotFoundException e) {
                                e.getMessage();
                            }
                            postImg(activity, paramsImgI);
                        }

                        if (!imgNut.isEmpty()) {
                            RequestParams paramsImgN = new RequestParams();
                            File myFileNut = new File(imgNut);
                            paramsImgN.put("code", barcode);
                            paramsImgN.put("imagefield", "front");
                            try {
                                paramsImgN.put("imgupload_nutrition", myFileNut);
                            } catch (FileNotFoundException e) {
                                e.getMessage();
                            }
                            postImg(activity, paramsImgN);
                        }
                        productSentCallback.onProductSentResponse(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    productSentCallback.onProductSentResponse(false);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                productSentCallback.onProductSentResponse(false);
                lt.error();
            }

            @Override
            public void onRetry ( int retryNo){
                // called when request is retried
            }
        });
    }

    public void postSaved(final Activity activity, RequestParams params, final String imgFront, final String imgIng, final String imgNut, final String barcode, final ListView lv, final int pos, final ArrayList<SaveItem> saveItems){
        FoodAPIRestClient.post(getApiUrl(activity) + "/cgi/product_jqm2.pl", params, new JsonHttpResponseHandler() {

            LoadToast lt = new LoadToast(activity);

            @Override
            public void onStart () {
                // called before request is started
                lt.setText(activity.getString(R.string.toastSending));
                lt.setBackgroundColor(activity.getResources().getColor(R.color.indigo_600));
                lt.setTextColor(activity.getResources().getColor(R.color.white));
                lt.show();
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {
                    int status = response.getInt("status");
                    if (status == 0) {
                        lt.error();
                        Toast.makeText(activity, response.getString("error"), Toast.LENGTH_LONG).show();
                    } else {
                        lt.success();
                        if(!imgFront.isEmpty()) {
                            RequestParams paramsImgF = new RequestParams();
                            File myFileFront = new File(imgFront);
                            paramsImgF.put("code", barcode);
                            paramsImgF.put("imagefield", "front");
                            try {
                                paramsImgF.put("imgupload_front", myFileFront);
                            } catch(FileNotFoundException e) {e.getMessage();}
                            postImg(activity, paramsImgF);
                        }

                        if(!imgIng.isEmpty()) {
                            RequestParams paramsImgI = new RequestParams();
                            File myFileIng = new File(imgIng);
                            paramsImgI.put("code", barcode);
                            paramsImgI.put("imagefield", "ingredients");
                            try {
                                paramsImgI.put("imgupload_ingredients", myFileIng);
                            } catch(FileNotFoundException e) {e.getMessage();}
                            postImg(activity, paramsImgI);
                        }

                        if(!imgNut.isEmpty()) {
                            RequestParams paramsImgN = new RequestParams();
                            File myFileNut = new File(imgNut);
                            paramsImgN.put("code", barcode);
                            paramsImgN.put("imagefield", "front");
                            try {
                                paramsImgN.put("imgupload_nutrition", myFileNut);
                            } catch(FileNotFoundException e) {e.getMessage();}
                            postImg(activity, paramsImgN);
                        }

                        SaveListAdapter sl = (SaveListAdapter) lv.getAdapter();
                        saveItems.remove(pos);
                        sl.notifyDataSetChanged();
                        SendProduct.deleteAll(SendProduct.class, "barcode = ?", barcode);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(activity, activity.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                lt.error();
            }

            @Override
            public void onRetry ( int retryNo){
                // called when request is retried
            }
        });
    }

    public void postImg(final Activity activity, final RequestParams params){
        FoodAPIRestClient.post(getApiUrl(activity) + "/cgi/product_image_upload.pl", params, new JsonHttpResponseHandler() {

            LoadToast lt = new LoadToast(activity);

            @Override
            public void onStart () {
                // called before request is started
                lt.setText(activity.getString(R.string.toastSending));
                lt.setBackgroundColor(activity.getResources().getColor(R.color.indigo_600));
                lt.setTextColor(activity.getResources().getColor(R.color.white));
                lt.show();
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {
                    String status = response.getString("status");
                    if (status.contains("status not ok")) {
                        lt.error();
                        Toast.makeText(activity, response.getString("error"), Toast.LENGTH_LONG).show();
                    } else {
                        lt.success();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(activity, activity.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                lt.error();
            }
        });
    }

    @NonNull
    private String getApiUrl(Activity activity) {
        return activity.getString(R.string.openfoodUrl);
    }
}