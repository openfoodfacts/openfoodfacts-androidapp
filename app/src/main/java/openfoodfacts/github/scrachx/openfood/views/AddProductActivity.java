package openfoodfacts.github.scrachx.openfood.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.AddProductIngredientsFragment;
import openfoodfacts.github.scrachx.openfood.fragments.AddProductNutritionFactsFragment;
import openfoodfacts.github.scrachx.openfood.fragments.AddProductOverviewFragment;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.models.ToUploadProduct;
import openfoodfacts.github.scrachx.openfood.models.ToUploadProductDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.isExternalStorageWritable;

public class AddProductActivity extends AppCompatActivity {

    @Inject
    OpenFoodAPIService client;
    @BindView(R.id.overview_indicator)
    View overviewIndicator;
    @BindView(R.id.ingredients_indicator)
    View ingredientsIndicator;
    @BindView(R.id.nutrition_facts_indicator)
    View nutritionFactsIndicator;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    Map<String, String> productDetails = new HashMap<>();
    AddProductOverviewFragment addProductOverviewFragment = new AddProductOverviewFragment();
    AddProductIngredientsFragment addProductIngredientsFragment = new AddProductIngredientsFragment();
    AddProductNutritionFactsFragment addProductNutritionFactsFragment = new AddProductNutritionFactsFragment();
    private Product mProduct;
    private ToUploadProductDao mToUploadProductDao;
    private Disposable disposable;

    public static File getCameraPicLocation(Context context) {
        File cacheDir = context.getCacheDir();
        if (isExternalStorageWritable()) {
            cacheDir = context.getExternalCacheDir();
        }
        File dir = new File(cacheDir, "EasyImage");
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                Log.i(AddProductActivity.class.getSimpleName(), "Directory created");
            } else {
                Log.i(AddProductActivity.class.getSimpleName(), "Couldn't create directory");
            }
        }
        return dir;
    }

    public static void clearCachedCameraPic(Context context) {
        File[] files = getCameraPicLocation(context).listFiles();
        for (File file : files) {
            if (file.delete()) {
                Log.i(AddProductActivity.class.getSimpleName(), "Deleted cached photo");
            } else {
                Log.i(AddProductActivity.class.getSimpleName(), "Couldn't delete cached photo");
            }
        }
    }

    @OnPageChange(value = R.id.viewpager, callback = OnPageChange.Callback.PAGE_SELECTED)
    void onPageSelected(int position) {
        switch (position) {
            case 0:
                updateTimelineIndicator(1, 0, 0);
                break;
            case 1:
                updateTimelineIndicator(2, 1, 0);
                break;
            case 2:
                updateTimelineIndicator(2, 2, 1);
                break;
            default:
                updateTimelineIndicator(1, 0, 0);
        }
    }

    /**
     * This method is used to update the timeline.
     * 0 means inactive stage, 1 means active stage and 2 means completed stage
     *
     * @param overviewStage       change the state of overview indicator
     * @param ingredientsStage    change the state of ingredients indicator
     * @param nutritionFactsStage change the state of nutrition facts indicator
     */

    private void updateTimelineIndicator(int overviewStage, int ingredientsStage, int nutritionFactsStage) {

        switch (overviewStage) {
            case 0:
                overviewIndicator.setBackgroundResource(R.drawable.stage_inactive);
                break;
            case 1:
                overviewIndicator.setBackgroundResource(R.drawable.stage_active);
                break;
            case 2:
                overviewIndicator.setBackgroundResource(R.drawable.stage_complete);
                break;
        }

        switch (ingredientsStage) {
            case 0:
                ingredientsIndicator.setBackgroundResource(R.drawable.stage_inactive);
                break;
            case 1:
                ingredientsIndicator.setBackgroundResource(R.drawable.stage_active);
                break;
            case 2:
                ingredientsIndicator.setBackgroundResource(R.drawable.stage_complete);
                break;
        }

        switch (nutritionFactsStage) {
            case 0:
                nutritionFactsIndicator.setBackgroundResource(R.drawable.stage_inactive);
                break;
            case 1:
                nutritionFactsIndicator.setBackgroundResource(R.drawable.stage_active);
                break;
            case 2:
                nutritionFactsIndicator.setBackgroundResource(R.drawable.stage_complete);
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        OFFApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        ButterKnife.bind(this);
        setTitle(R.string.offline_product_addition_title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        final State state = (State) getIntent().getSerializableExtra("state");
        if (state != null) {
            mProduct = state.getProduct();
        } else {
            Toast.makeText(this, "Something went wrong when adding product", Toast.LENGTH_SHORT).show();
            finish();
        }
        mToUploadProductDao = Utils.getAppDaoSession(this).getToUploadProductDao();
        setupViewPager(viewPager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        clearCachedCameraPic(this);
    }

    private void setupViewPager(ViewPager viewPager) {
        ProductFragmentPagerAdapter adapterResult = new ProductFragmentPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();
        bundle.putSerializable("product", mProduct);
        addProductOverviewFragment.setArguments(bundle);
        addProductIngredientsFragment.setArguments(bundle);
        addProductNutritionFactsFragment.setArguments(bundle);
        adapterResult.addFragment(addProductOverviewFragment, "Overview");
        adapterResult.addFragment(addProductIngredientsFragment, "Ingredients");
        adapterResult.addFragment(addProductNutritionFactsFragment, "Nutrition Facts");
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapterResult);
    }

    private void saveProduct() {
        addProductOverviewFragment.getDetails();
        addProductIngredientsFragment.getDetails();
        addProductNutritionFactsFragment.getDetails();
        final SharedPreferences settings = getSharedPreferences("login", 0);
        final String login = settings.getString("user", "");
        final String password = settings.getString("pass", "");
        if (!login.isEmpty() && !password.isEmpty()) {
            productDetails.put("user_id", login);
            productDetails.put("password", password);
        }
        String code = productDetails.get("code");
        for (Map.Entry<String, String> entry : productDetails.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Log.d(key, value);
        }

        client.saveProductSingle(code, productDetails, "Basic b2ZmOm9mZg==")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<State>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        new MaterialDialog.Builder(AddProductActivity.this)
                                .title(R.string.toastSending)
                                .content("Please wait")
                                .progress(true, 0)
                                .show();
                    }

                    @Override
                    public void onSuccess(State state) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Product uploaded successfully", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        View view = toast.getView();
                        TextView textView = view.findViewById(android.R.id.message);
                        textView.setTextSize(18);
                        view.setBackgroundColor(getResources().getColor(R.color.green_500));
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.show();
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(AddProductActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

    }

    public void proceed() {
        switch (viewPager.getCurrentItem()) {
            case 0:
                viewPager.setCurrentItem(1, true);
                break;
            case 1:
                viewPager.setCurrentItem(2, true);
                break;
            case 2:
                if (addProductOverviewFragment.areRequiredFieldsEmpty()) {
                    viewPager.setCurrentItem(0, true);
                } else if (!addProductNutritionFactsFragment.isCheckPassed()) {
                    viewPager.setCurrentItem(2, true);
                } else {
                    saveProduct();
                }
                break;
        }
    }

    @OnClick(R.id.overview_indicator)
    void switchToOverviewPage() {
        viewPager.setCurrentItem(0, true);
    }

    @OnClick(R.id.ingredients_indicator)
    void switchToIngredientsPage() {
        viewPager.setCurrentItem(1, true);
    }

    @OnClick(R.id.nutrition_facts_indicator)
    void switchToNutritionFactsPage() {
        viewPager.setCurrentItem(2, true);
    }

    public void addToMap(String key, String value) {
        productDetails.put(key, value);

    }

    public void addToPhotoMap(ProductImage image, int position) {
        String lang = getProductLanguage();
        boolean ocr = false;
        Map<String, RequestBody> imgMap = new HashMap<>();
        imgMap.put("code", image.getCode());
        RequestBody imageField = RequestBody.create(MediaType.parse("text/plain"), image.getImageField().toString() + '_' + lang);
        imgMap.put("imagefield", imageField);
        if (image.getImguploadFront() != null)
            imgMap.put("imgupload_front\"; filename=\"front_" + lang + ".png\"", image.getImguploadFront());
        if (image.getImguploadIngredients() != null) {
            imgMap.put("imgupload_ingredients\"; filename=\"ingredients_" + lang + ".png\"", image.getImguploadIngredients());
            ocr = true;
        }
        if (image.getImguploadNutrition() != null)
            imgMap.put("imgupload_nutrition\"; filename=\"nutrition_" + lang + ".png\"", image.getImguploadNutrition());
        if (image.getImguploadOther() != null)
            imgMap.put("imgupload_other\"; filename=\"other_" + lang + ".png\"", image.getImguploadOther());
        // Attribute the upload to the connected user
        final SharedPreferences settings = getSharedPreferences("login", 0);
        final String login = settings.getString("user", "");
        final String password = settings.getString("pass", "");
        if (!login.isEmpty() && !password.isEmpty()) {
            imgMap.put("user_id", RequestBody.create(MediaType.parse("text/plain"), login));
            imgMap.put("password", RequestBody.create(MediaType.parse("text/plain"), password));
        }
        savePhoto(imgMap, image, position, ocr);
    }

    private void savePhoto(Map<String, RequestBody> imgMap, ProductImage image, int position, boolean ocr) {
        client.saveImageSingle(imgMap, "Basic b2ZmOm9mZg==")
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> showImageProgress(position))
                .subscribe(new SingleObserver<JsonNode>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(JsonNode jsonNode) {
                        String status = jsonNode.get("status").asText();
                        if (status.equals("status not ok")) {
                            String error = jsonNode.get("error").asText();
                            hideImageProgress(position, true, error);
                        } else {
                            hideImageProgress(position, false, null);
                            String imagefield = jsonNode.get("imagefield").asText();
                            String imgid = jsonNode.get("image").get("imgid").asText();
                            setPhoto(image, imagefield, imgid, ocr);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        // A network error happened
                        if (e instanceof IOException) {
                            hideImageProgress(position, true, "You seem offline, images will be uploaded when network is available");
                            Log.e(AddProductActivity.class.getSimpleName(), e.getMessage());
                            ToUploadProduct product = new ToUploadProduct(image.getBarcode(), image.getFilePath(), image.getImageField().toString());
                            mToUploadProductDao.insertOrReplace(product);

                        } else {
                            Log.i(this.getClass().getSimpleName(), e.getMessage());
                            Toast.makeText(AddProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setPhoto(ProductImage image, String imagefield, String imgid, boolean ocr) {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("imgid", imgid);
        queryMap.put("id", imagefield);
        client.editImageSingle(image.getBarcode(), queryMap, "Basic b2ZmOm9mZg==")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<JsonNode>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(JsonNode jsonNode) {
                        String status = jsonNode.get("status").asText();
                        if (status.equals("status ok")) {
                            if (ocr) {
                                performOCR(image, imagefield);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof IOException) {
                            if (ocr) {
                                View view = findViewById(android.R.id.content);
                                Snackbar.make(view, "No internet connection. Unable to extract ingredients", Snackbar.LENGTH_INDEFINITE)
                                        .setAction(R.string.txt_try_again, v -> setPhoto(image, imagefield, imgid, true)).show();
                            }
                        } else {
                            Log.i(this.getClass().getSimpleName(), e.getMessage());
                            Toast.makeText(AddProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void performOCR(ProductImage image, String imageField) {
        client.getIngredients(image.getBarcode(), imageField, "Basic b2ZmOm9mZg==")
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> addProductIngredientsFragment.showOCRProgress())
                .subscribe(new SingleObserver<JsonNode>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(JsonNode jsonNode) {
                        addProductIngredientsFragment.hideOCRProgress();
                        String status = jsonNode.get("status").toString();
                        if (status.equals("0")) {
                            String ocrResult = jsonNode.get("ingredients_text_from_image").asText();
                            addProductIngredientsFragment.setIngredients(status, ocrResult);
                        } else {
                            addProductIngredientsFragment.setIngredients(status, null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        addProductIngredientsFragment.hideOCRProgress();
                        if (e instanceof IOException) {
                            View view = findViewById(android.R.id.content);
                            Snackbar.make(view, "No internet connection. Unable to extract ingredients", Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.txt_try_again, v -> performOCR(image, imageField)).show();
                        } else {
                            Log.i(this.getClass().getSimpleName(), e.getMessage());
                            Toast.makeText(AddProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void hideImageProgress(int position, boolean errorUploading, String message) {
        switch (position) {
            case 0:
                addProductOverviewFragment.hideImageProgress(errorUploading, message);
                break;
            case 1:
                addProductIngredientsFragment.hideImageProgress(errorUploading, message);
                break;
            case 2:
                addProductNutritionFactsFragment.hideImageProgress(errorUploading, message);
        }
    }

    private void showImageProgress(int position) {
        switch (position) {
            case 0:
                addProductOverviewFragment.showImageProgress();
                break;
            case 1:
                addProductIngredientsFragment.showImageProgress();
                break;
            case 2:
                addProductNutritionFactsFragment.showImageProgress();
        }
    }

    public String getProductLanguage() {
        return productDetails.get("lang");
    }

    public void loadAutoSuggestion() {
        addProductIngredientsFragment.loadAutoSuggestions();
    }
}
