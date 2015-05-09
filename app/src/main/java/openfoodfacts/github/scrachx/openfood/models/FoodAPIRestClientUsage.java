package openfoodfacts.github.scrachx.openfood.models;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.loopj.android.http.*;
import net.steamcrafted.loadtoast.LoadToast;
import org.apache.http.Header;
import java.io.IOException;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.FoodAPIRestClient;
import openfoodfacts.github.scrachx.openfood.utils.MyNullKeySerializer;
import openfoodfacts.github.scrachx.openfood.views.ProductActivity;
import openfoodfacts.github.scrachx.openfood.views.SaveProductOffline;

/**
 * Created by scotscriven on 30/04/15.
 */
public class FoodAPIRestClientUsage {

    public void getProduct(final String barcode, final Activity activity, final ZXingScannerView scannerView){
        FoodAPIRestClient.get(barcode+".json", null, new AsyncHttpResponseHandler() {

            LoadToast lt = new LoadToast(activity);

            @Override
            public void onStart() {
                // called before request is started
                lt.setText(activity.getString(R.string.toast_retrieving));
                lt.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
                    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    objectMapper.getSerializerProvider().setNullKeySerializer(new MyNullKeySerializer());
                    State s = objectMapper.readValue(response, State.class);

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
                                        Intent intent = new Intent(activity, SaveProductOffline.class);
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
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Toast.makeText(activity, activity.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                lt.error();
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });

    }

    public void getProduct(String barcode, final Activity activity){
        FoodAPIRestClient.get(barcode+".json", null, new AsyncHttpResponseHandler() {

            LoadToast lt = new LoadToast(activity);

            @Override
            public void onStart() {
                // called before request is started
                lt.setText(activity.getString(R.string.toast_retrieving));
                lt.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
                    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    objectMapper.getSerializerProvider().setNullKeySerializer(new MyNullKeySerializer());
                    State s = objectMapper.readValue(response, State.class);

                    if(s.getStatus() == 0){
                        lt.error();
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
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.v("Error : ", "Can't fetch data");
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });

    }

}
