package openbeautyfacts.github.scrachx.openfood.models;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.loopj.android.http.*;
import net.steamcrafted.loadtoast.LoadToast;
import java.io.IOException;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import openbeautyfacts.github.scrachx.openfood.R;
import openbeautyfacts.github.scrachx.openfood.network.FoodAPIRestClient;
import openbeautyfacts.github.scrachx.openfood.views.ProductActivity;
import openbeautyfacts.github.scrachx.openfood.views.SaveProductOfflineActivity;

public class FoodAPIRestClientUsage {

    public void getProduct(final String barcode, final Activity activity, final ZXingScannerView scannerView, final ZXingScannerView.ResultHandler rs){
        FoodAPIRestClient.getAsync("/api/v0/produit/"+barcode+".json", null, new AsyncHttpResponseHandler() {

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
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                // called when response HTTP status is "200 OK"
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    State s = objectMapper.readValue(responseBody, State.class);

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
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
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

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });

    }

    public void getProduct(final String barcode, final Activity activity){
        FoodAPIRestClient.getAsync("/api/v0/produit/"+barcode+".json", null, new AsyncHttpResponseHandler() {

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
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                // called when response HTTP status is "200 OK"
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    State s = objectMapper.readValue(responseBody, State.class);

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
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
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

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }

    public interface OnProductsCallback {
        public void onProductsResponse(boolean value, List<Product> products);
    }

    public void searchProduct(final String name, final Activity activity, final OnProductsCallback productsCallback) {
        FoodAPIRestClient.getAsync("/cgi/search.pl?search_terms="+name+"&search_simple=1&action=process&json=1", null, new AsyncHttpResponseHandler() {

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
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                // called when response HTTP status is "200 OK"
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        Search s = objectMapper.readValue(responseBody, Search.class);
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
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(activity, activity.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                lt.error();
                productsCallback.onProductsResponse(false, null);
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }

    public interface OnAllergensCallback {
        public void onAllergensResponse(boolean value);
    }

    public void getAllergens(final OnAllergensCallback onAllergensCallback) {
        FoodAPIRestClient.getAsync("/allergens.json", null, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                // called when response HTTP status is "200 OK"
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        JsonNode root = objectMapper.readTree(responseBody);
                        List<Allergen> la = objectMapper.readValue(root.path("tags").toString(), new TypeReference<List<Allergen>>() {});
                        for (Allergen a : la) {
                            Allergen al = new Allergen(a.getUrl(),a.getName(),a.getProducts(),a.getIdAllergen());
                            al.save();
                        }
                        onAllergensCallback.onAllergensResponse(true);
                    } catch (InvalidFormatException e) {
                        e.printStackTrace();
                        onAllergensCallback.onAllergensResponse(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    onAllergensCallback.onAllergensResponse(false);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }
}
