package openfoodfacts.github.scrachx.openfood.models;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.loopj.android.http.*;
import net.steamcrafted.loadtoast.LoadToast;
import java.io.IOException;
import java.util.List;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.FoodAPIRestClient;
import openfoodfacts.github.scrachx.openfood.views.ProductActivity;
import openfoodfacts.github.scrachx.openfood.views.SaveProductOfflineActivity;

public class FoodAPIRestClientUsage {

    public void getProduct(final String barcode, final Activity activity, final ZXingScannerView scannerView){
        FoodAPIRestClient.get("api/v0/produit/"+barcode+".json", null, new AsyncHttpResponseHandler() {

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
                                        scannerView.startCamera();
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
        FoodAPIRestClient.get("api/v0/produit/"+barcode+".json", null, new AsyncHttpResponseHandler() {

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
        FoodAPIRestClient.get("/cgi/search.pl?search_terms="+name+"&search_simple=1&action=process&json=1", null, new AsyncHttpResponseHandler() {

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
                            lt.setText(activity.getResources().getString(R.string.txtInfoLoginNo));
                            lt.error();
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

}
