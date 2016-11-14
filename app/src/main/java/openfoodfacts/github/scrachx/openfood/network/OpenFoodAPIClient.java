package openfoodfacts.github.scrachx.openfood.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.JsonNode;
import com.orm.SugarRecord;

import net.steamcrafted.loadtoast.LoadToast;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import okhttp3.OkHttpClient;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AllergenRestResponse;
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.views.ProductActivity;
import openfoodfacts.github.scrachx.openfood.views.SaveProductOfflineActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.INGREDIENT;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.NUTRITION;

public class OpenFoodAPIClient {

    private static final JacksonConverterFactory jacksonConverterFactory = JacksonConverterFactory.create();

    private final static OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .build();

    private final OpenFoodAPIService apiService;

    public OpenFoodAPIClient(Context context) {
        this(context.getString(R.string.openfoodUrl));
    }

    private OpenFoodAPIClient(String apiUrl) {
        apiService = new Retrofit.Builder()
                .baseUrl(apiUrl)
                .client(httpClient)
                .addConverterFactory(jacksonConverterFactory)
                .build()
                .create(OpenFoodAPIService.class);
    }

    /**
     * @return The API service to be able to use directly retrofit API mapping
     */
    public OpenFoodAPIService getAPIService() {
        return apiService;
    }

    public void getProduct(final String barcode, final Activity activity, final ZXingScannerView scannerView, final ZXingScannerView.ResultHandler rs){
        final LoadToast lt = getLoadToast(activity);

        apiService.getProductByBarcode(barcode).enqueue(new Callback<State>() {
            @Override
            public void onResponse(Call<State> call, Response<State> response) {
                // called when response HTTP status is "200 OK"
                State s = response.body();

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
                                    intent.putExtra("barcode", barcode);
                                    activity.startActivity(intent);
                                    activity.finish();
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    scannerView.resumeCameraPreview(rs);
                                }
                            })
                            .show();
                }else{
                    lt.success();
                    List<HistoryProduct> resHp = HistoryProduct.find(HistoryProduct.class, "barcode = ?", barcode);
                    HistoryProduct hp;
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

            }

            @Override
            public void onFailure(Call<State> call, Throwable t) {
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

    public void getProduct(final String barcode, final Activity activity) {
        final LoadToast lt = getLoadToast(activity);

        apiService.getProductByBarcode(barcode).enqueue(new Callback<State>() {
            @Override
            public void onResponse(Call<State> call, Response<State> response) {
                State s = response.body();

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

            }

            @Override
            public void onFailure(Call<State> call, Throwable t) {
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

    public void searchProduct(final String name, final Activity activity, final OnProductsCallback productsCallback) {
        final LoadToast lt = getLoadToast(activity);

        apiService.searchProductByName(name).enqueue(new Callback<Search>() {
            @Override
            public void onResponse(Call<Search> call, Response<Search> response) {
                if (!response.isSuccess()) {
                    productsCallback.onProductsResponse(false, null);
                    return;
                }

                Search s = response.body();
                if(Integer.valueOf(s.getCount()) == 0){
                    Toast.makeText(activity, R.string.txt_product_not_found, Toast.LENGTH_LONG).show();
                    lt.error();
                    productsCallback.onProductsResponse(false, null);
                }else{
                    lt.success();
                    productsCallback.onProductsResponse(true, s.getProducts());
                }
            }

            @Override
            public void onFailure(Call<Search> call, Throwable t) {
                Toast.makeText(activity, activity.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                lt.error();
                productsCallback.onProductsResponse(false, null);
            }
        });
    }

    public void getAllergens(final OnAllergensCallback onAllergensCallback) {
        apiService.getAllergens().enqueue(new Callback<AllergenRestResponse>() {
            @Override
            public void onResponse(Call<AllergenRestResponse> call, Response<AllergenRestResponse> response) {
                if (!response.isSuccess()) {
                    onAllergensCallback.onAllergensResponse(false);
                    return;
                }

                SugarRecord.saveInTx(response.body().getAllergens());

                onAllergensCallback.onAllergensResponse(true);
            }

            @Override
            public void onFailure(Call<AllergenRestResponse> call, Throwable t) {

            }
        });
    }

    public void post(final Activity activity, final SendProduct product, final OnProductSentCallback productSentCallback){
        final LoadToast lt = new LoadToast(activity);
        lt.setText(activity.getString(R.string.toastSending));
        lt.setBackgroundColor(activity.getResources().getColor(R.color.indigo_600));
        lt.setTextColor(activity.getResources().getColor(R.color.white));
        lt.show();

        apiService.saveProduct(product.getBarcode(), product.getName(), product.getBrands(), product.getUserId(), product.getPassword()).enqueue(new Callback<State>() {
            @Override
            public void onResponse(Call<State> call, Response<State> response) {
                if (!response.isSuccess() || response.body().getStatus() == 0) {
                    lt.error();
                    productSentCallback.onProductSentResponse(false);
                    return;
                }

                String imguploadFront = product.getImgupload_front();
                ProductImage image = new ProductImage(product.getBarcode(), FRONT, new File(imguploadFront));
                postImg(activity, image);

                String imguploadIngredients = product.getImgupload_ingredients();
                if (StringUtils.isNotEmpty(imguploadIngredients)) {
                    postImg(activity, new ProductImage(product.getBarcode(), INGREDIENT, new File(imguploadIngredients)));
                }

                String imguploadNutrition = product.getImgupload_nutrition();
                if (StringUtils.isNotBlank(imguploadNutrition)) {
                    postImg(activity, new ProductImage(product.getBarcode(), NUTRITION, new File(imguploadNutrition)));
                }

                lt.success();
                productSentCallback.onProductSentResponse(true);
            }

            @Override
            public void onFailure(Call<State> call, Throwable t) {
                lt.error();
                productSentCallback.onProductSentResponse(false);
            }
        });
    }

    public void postImg(final Context context, final ProductImage image) {
        final LoadToast lt = new LoadToast(context);
        lt.setText(context.getString(R.string.toastSending));
        lt.setBackgroundColor(context.getResources().getColor(R.color.indigo_600));
        lt.setTextColor(context.getResources().getColor(R.color.white));
        lt.show();

        apiService.saveImage(image.getCode(), image.getField(), image.getImguploadFront(), image.getImguploadIngredients(), image.getImguploadNutrition())
                .enqueue(new Callback<JsonNode>() {
            @Override
            public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
                if(!response.isSuccess()) {
                    Toast.makeText(context, context.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                    lt.error();
                }

                JsonNode body = response.body();
                if (body.get("status").asText().contains("status not ok")) {
                    Toast.makeText(context, body.get("error").asText(), Toast.LENGTH_LONG).show();
                    lt.error();
                } else {
                    lt.success();
                }
            }

            @Override
            public void onFailure(Call<JsonNode> call, Throwable t) {
                Toast.makeText(context, context.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                lt.error();
            }
        });
    }

    @NonNull
    private LoadToast getLoadToast(Activity activity) {
        final LoadToast lt = new LoadToast(activity);
        lt.setText(activity.getString(R.string.toast_retrieving));
        lt.setBackgroundColor(activity.getResources().getColor(R.color.indigo_600));
        lt.setTextColor(activity.getResources().getColor(R.color.white));
        lt.show();
        return lt;
    }

    public interface OnProductsCallback {

        void onProductsResponse(boolean isOk, List<Product> products);
    }

    public interface OnAllergensCallback {

        void onAllergensResponse(boolean value);
    }

    public interface OnProductSentCallback {
        void onProductSentResponse(boolean value);
    }
}
