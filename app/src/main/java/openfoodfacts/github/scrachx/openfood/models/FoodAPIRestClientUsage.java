package openfoodfacts.github.scrachx.openfood.models;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.FoodAPIRestClient;
import openfoodfacts.github.scrachx.openfood.utils.JsonUtils;
import openfoodfacts.github.scrachx.openfood.views.ProductActivity;
import openfoodfacts.github.scrachx.openfood.views.SaveProductOfflineActivity;

public class FoodAPIRestClientUsage {

    private final String apiUrl;

    public FoodAPIRestClientUsage(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public void getProduct(final String barcode, final Activity activity, final ZXingScannerView scannerView, final ZXingScannerView.ResultHandler rs){
        FoodAPIRestClient.get(apiUrl + "/api/v0/produit/"+barcode+".json", null, new AsyncHttpResponseHandler() {

            LoadToast lt = new LoadToast(activity);

            @Override
            public void onStart() {
                // called before request is started
                lt.setText(activity.getString(R.string.toast_retrieving));
                lt.setBackgroundColor(activity.getResources().getColor(R.color.indigo_600));
                lt.setTextColor(activity.getResources().getColor(R.color.white));
                lt.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // called when response HTTP status is "200 OK"
                try {
                    State s = JsonUtils.readFor(State.class).readValue(responseBody);

                    if(s.getStatus() == 0){
                        lt.error();
                        new MaterialDialog.Builder(activity)
                                .title(R.string.txtDialogsTitle)
                                .content(R.string.txtDialogsContent)
                                .positiveText(R.string.txtYes)
                                .negativeText(R.string.txtNo)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        SharedPreferences settings = activity.getSharedPreferences("temp", 0);
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putString("barcode", barcode);
                                        editor.apply();
                                        Intent intent = new Intent(activity, SaveProductOfflineActivity.class);
                                        activity.startActivity(intent);
                                        activity.finish();
                                    }

                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        scannerView.resumeCameraPreview(rs);
                                        return;
                                    }
                                })
                                .show();
                    }else{
                        lt.success();
                        List<HistoryProduct> resHp = HistoryProduct.find(HistoryProduct.class, "barcode = ?", barcode);
                        HistoryProduct hp = null;
                        if(resHp.size() == 1) {
                            hp = resHp.get(0);
                            hp.setLastSeen(new Date());
                        } else {
                            hp = new HistoryProduct(s.getProduct().getProductName(), s.getProduct().getBrands(), s.getProduct().getImageFrontUrl(), barcode);
                        }
                        hp.save();
                        Intent intent = new Intent(activity, ProductActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("state", s);
                        intent.putExtras(bundle);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                new MaterialDialog.Builder(activity)
                        .title(R.string.txtDialogsTitle)
                        .content(R.string.txtDialogsContent)
                        .positiveText(R.string.txtYes)
                        .negativeText(R.string.txtNo)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                Intent intent = new Intent(activity, SaveProductOfflineActivity.class);
                                intent.putExtra("barcode",barcode);
                                activity.startActivity(intent);
                                activity.finish();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                return;
                            }
                        })
                        .show();
                Toast.makeText(activity, activity.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                lt.error();
            }
        });

    }

    public void getProduct(final String barcode, final Activity activity){
        FoodAPIRestClient.get(apiUrl + "/api/v0/produit/"+barcode+".json", null, new AsyncHttpResponseHandler() {

            LoadToast lt = new LoadToast(activity);

            @Override
            public void onStart() {
                // called before request is started
                lt.setText(activity.getString(R.string.toast_retrieving));
                lt.setBackgroundColor(activity.getResources().getColor(R.color.indigo_600));
                lt.setTextColor(activity.getResources().getColor(R.color.white));
                lt.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // called when response HTTP status is "200 OK"
                try {
                    State s = JsonUtils.readFor(State.class).readValue(responseBody);

                    if(s.getStatus() == 0){
                        lt.error();
                        new MaterialDialog.Builder(activity)
                                .title(R.string.txtDialogsTitle)
                                .content(R.string.txtDialogsContent)
                                .positiveText(R.string.txtYes)
                                .negativeText(R.string.txtNo)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        Intent intent = new Intent(activity, SaveProductOfflineActivity.class);
                                        intent.putExtra("barcode",barcode);
                                        activity.startActivity(intent);
                                        activity.finish();
                                    }

                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        return;
                                    }
                                })
                                .show();
                    }else{
                        lt.success();
                        Intent intent = new Intent(activity, ProductActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("state", s);
                        intent.putExtras(bundle);
                        activity.startActivity(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                new MaterialDialog.Builder(activity)
                        .title(R.string.txtDialogsTitle)
                        .content(R.string.txtDialogsContent)
                        .positiveText(R.string.txtYes)
                        .negativeText(R.string.txtNo)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                Intent intent = new Intent(activity, SaveProductOfflineActivity.class);
                                intent.putExtra("barcode",barcode);
                                activity.startActivity(intent);
                                activity.finish();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                return;
                            }
                        })
                        .show();
                Toast.makeText(activity, activity.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                lt.error();
            }

        });
    }

    public interface OnProductsCallback {
        public void onProductsResponse(boolean value, List<Product> products);
    }

    public void searchProduct(final String name, final Activity activity, final OnProductsCallback productsCallback) {
        RequestParams params = new RequestParams();
        params.add("search_terms", name);
        params.add("action", "process");
        params.add("search_simple", "1");
        params.add("json", "1");

        FoodAPIRestClient.get(apiUrl + "/cgi/search.pl", params, new AsyncHttpResponseHandler() {

            LoadToast lt = new LoadToast(activity);

            @Override
            public void onStart() {
                // called before request is started
                lt.setText(activity.getString(R.string.toast_retrieving));
                lt.setBackgroundColor(activity.getResources().getColor(R.color.indigo_600));
                lt.setTextColor(activity.getResources().getColor(R.color.white));
                lt.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // called when response HTTP status is "200 OK"
                try {

                    try {
                        Search s = JsonUtils.readFor(Search.class).readValue(responseBody);
                        if(Integer.valueOf(s.getCount()) == 0){
                            Toast.makeText(activity, R.string.txt_product_not_found, Toast.LENGTH_LONG).show();
                            lt.error();
                            productsCallback.onProductsResponse(false, null);
                        }else{
                            lt.success();
                            productsCallback.onProductsResponse(true, s.getProducts());
                        }
                    } catch (InvalidFormatException e) {
                        lt.error();
                        productsCallback.onProductsResponse(false, null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    productsCallback.onProductsResponse(false, null);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(activity, activity.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                lt.error();
                productsCallback.onProductsResponse(false, null);
            }
        });
    }

    public interface OnAllergensCallback {
        void onAllergensResponse(boolean value);
    }

    public void getAllergens(final OnAllergensCallback onAllergensCallback) {
        FoodAPIRestClient.get(apiUrl + "/allergens.json", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // called when response HTTP status is "200 OK"
                try {
                    AllergenRestResponse restResponse = JsonUtils.readFor(AllergenRestResponse.class)
                            .readValue(responseBody);

                    for (Allergen allergen : restResponse.getAllergens()) {
                        allergen.save();
                    }
                    onAllergensCallback.onAllergensResponse(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    onAllergensCallback.onAllergensResponse(false);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }
}
