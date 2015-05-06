package openfoodfacts.github.scrachx.openfood.models;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.loopj.android.http.*;

import net.steamcrafted.loadtoast.LoadToast;

import org.apache.http.Header;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.FoodAPIRestClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.utils.MyNullKeySerializer;
import openfoodfacts.github.scrachx.openfood.views.ProductActivity;

/**
 * Created by scotscriven on 30/04/15.
 */
public class FoodAPIRestClientUsage {

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
