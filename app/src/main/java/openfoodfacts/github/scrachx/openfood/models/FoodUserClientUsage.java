package openfoodfacts.github.scrachx.openfood.models;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.widget.ListView;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import net.steamcrafted.loadtoast.LoadToast;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.OfflineEditFragment;
import openfoodfacts.github.scrachx.openfood.network.FoodUserClient;
import openfoodfacts.github.scrachx.openfood.views.adapters.SaveListAdapter;

/**
 * Created by scotscriven on 10/05/15.
 */
public class FoodUserClientUsage {

    public void post(final Activity activity, RequestParams params, final String img, final String barcode, final ListView lv, final int pos, final ArrayList<SaveItem> saveItems){
        FoodUserClient.post("/cgi/product_jqm2.pl", params, new JsonHttpResponseHandler() {

            LoadToast lt = new LoadToast(activity);

            @Override
            public void onStart () {
                // called before request is started
                lt.setText(activity.getString(R.string.toastSending));
                lt.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {
                    int status = response.getInt("status");
                    if (status == 0) {
                        lt.error();
                        Toast.makeText(activity, response.getString("error"), Toast.LENGTH_LONG).show();
                    } else {
                        lt.success();
                        RequestParams paramsImg = new RequestParams();
                        File myFile = new File(img);
                        long length = myFile.length();
                        paramsImg.put("code", barcode);
                        paramsImg.put("imagefield", "front");
                        try {
                            paramsImg.put("imgupload_front", myFile);
                        } catch(FileNotFoundException e) {e.getMessage();}
                        postImg(activity, paramsImg, barcode, length, lv, pos, saveItems);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(activity, activity.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                lt.error();
            }

            @Override
            public void onRetry ( int retryNo){
                // called when request is retried
                }
            });
    }

    public void postImg(final Activity activity, final RequestParams params, final String barcode, final long length, final ListView lv, final int pos, final ArrayList<SaveItem> saveItems){
        FoodUserClient.post("/cgi/product_image_upload.pl", params, new JsonHttpResponseHandler() {

            LoadToast lt = new LoadToast(activity);

            @Override
            public void onStart () {
                // called before request is started
                lt.setText(activity.getString(R.string.toastSending));
                lt.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {
                    String status = response.getString("status");
                    if (status.contains("status not ok")) {
                        lt.error();
                        Toast.makeText(activity, response.getString("error"), Toast.LENGTH_LONG).show();
                    } else {
                        lt.success();
                        SendProduct.deleteAll(SendProduct.class, "barcode = ?", barcode);
                        SaveListAdapter sl = (SaveListAdapter) lv.getAdapter();
                        saveItems.remove(pos);
                        sl.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(activity, activity.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                lt.error();
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {

            }

            @Override
            public void onRetry ( int retryNo){
                // called when request is retried
            }
        });

    }

}
