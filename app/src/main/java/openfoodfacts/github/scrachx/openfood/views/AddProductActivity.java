package openfoodfacts.github.scrachx.openfood.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.AddProductIngredientsFragment;
import openfoodfacts.github.scrachx.openfood.fragments.AddProductNutritionFactsFragment;
import openfoodfacts.github.scrachx.openfood.fragments.AddProductOverviewFragment;
import openfoodfacts.github.scrachx.openfood.fragments.AddProductPhotosFragment;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProductDao;
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
    @BindView(R.id.text_nutrition_facts_indicator)
    TextView nutritionFactsIndicatorText;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    Map<String, String> productDetails = new HashMap<>();
    AddProductOverviewFragment addProductOverviewFragment = new AddProductOverviewFragment();
    AddProductIngredientsFragment addProductIngredientsFragment = new AddProductIngredientsFragment();
    AddProductNutritionFactsFragment addProductNutritionFactsFragment = new AddProductNutritionFactsFragment();
    AddProductPhotosFragment addProductPhotosFragment = new AddProductPhotosFragment();
    private Product mProduct;
    private ToUploadProductDao mToUploadProductDao;
    private OfflineSavedProductDao mOfflineSavedProductDao;
    private Disposable disposable;
    private String[] imagesFilePath = new String[3];
    private OfflineSavedProduct offlineSavedProduct;
    private Bundle bundle = new Bundle();
    private MaterialDialog dialog;

    // These fields are used to compare the existing values of a product already present on the server with the product which was saved offline and is being uploaded.
    private String ingredientsTextOnServer;
    private String productNameOnServer;
    private String quantityOnServer;
    private String linkOnServer;
    private String ingredientsImageOnServer;

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
    public void onBackPressed() {
        if (offlineSavedProduct != null) {
            saveProduct();
        } else {
            new MaterialDialog.Builder(this)
                    .content(R.string.save_product)
                    .positiveText(R.string.txtSave)
                    .negativeText(R.string.txtPictureNeededDialogNo)
                    .onPositive((dialog, which) -> checkFields())
                    .onNegative((dialog, which) -> super.onBackPressed())
                    .show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (offlineSavedProduct != null) {
                saveProduct();
            } else {
                new MaterialDialog.Builder(this)
                        .content(R.string.save_product)
                        .positiveText(R.string.txtSave)
                        .negativeText(R.string.txtPictureNeededDialogNo)
                        .onPositive((dialog, which) -> checkFields())
                        .onNegative((dialog, which) -> finish())
                        .show();
            }
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
        mToUploadProductDao = Utils.getAppDaoSession(this).getToUploadProductDao();
        mOfflineSavedProductDao = Utils.getAppDaoSession(this).getOfflineSavedProductDao();
        final State state = (State) getIntent().getSerializableExtra("state");
        offlineSavedProduct = (OfflineSavedProduct) getIntent().getSerializableExtra("edit_offline_product");
        if (state != null) {
            mProduct = state.getProduct();
            // Search if the barcode already exists in the OfflineSavedProducts db
            offlineSavedProduct = mOfflineSavedProductDao.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(mProduct.getCode())).unique();
        }
        if (offlineSavedProduct != null) {
            bundle.putSerializable("edit_offline_product", offlineSavedProduct);
            // Save the already existing images in productDetails for UI
            imagesFilePath[0] = offlineSavedProduct.getProductDetailsMap().get("image_front");
            imagesFilePath[1] = offlineSavedProduct.getProductDetailsMap().get("image_ingredients");
            imagesFilePath[2] = offlineSavedProduct.getProductDetailsMap().get("images_nutrition_facts");
        }
        if (state == null && offlineSavedProduct == null) {
            Toast.makeText(this, R.string.error_adding_product, Toast.LENGTH_SHORT).show();
            finish();
        }
        setupViewPager(viewPager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        clearCachedCameraPic(this);
    }

    private void setupViewPager(ViewPager viewPager) {
        ProductFragmentPagerAdapter adapterResult = new ProductFragmentPagerAdapter(getSupportFragmentManager());
        bundle.putSerializable("product", mProduct);
        addProductOverviewFragment.setArguments(bundle);
        addProductIngredientsFragment.setArguments(bundle);
        adapterResult.addFragment(addProductOverviewFragment, "Overview");
        adapterResult.addFragment(addProductIngredientsFragment, "Ingredients");
        if (BuildConfig.FLAVOR.equals("off") || BuildConfig.FLAVOR.equals("opff")) {
            addProductNutritionFactsFragment.setArguments(bundle);
            adapterResult.addFragment(addProductNutritionFactsFragment, "Nutrition Facts");
        } else if (BuildConfig.FLAVOR.equals("obf") || BuildConfig.FLAVOR.equals("opf")) {
            nutritionFactsIndicatorText.setText(R.string.photos);
            addProductPhotosFragment.setArguments(bundle);
            adapterResult.addFragment(addProductPhotosFragment, "Photos");
        }
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapterResult);
    }

    private void saveProduct() {
        addProductOverviewFragment.getDetails();
        addProductIngredientsFragment.getDetails();
        if (BuildConfig.FLAVOR.equals("off") || BuildConfig.FLAVOR.equals("opff")) {
            addProductNutritionFactsFragment.getDetails();
        }
        final SharedPreferences settings = getSharedPreferences("login", 0);
        final String login = settings.getString("user", "");
        final String password = settings.getString("pass", "");
        if (!login.isEmpty() && !password.isEmpty()) {
            productDetails.put("user_id", login);
            productDetails.put("password", password);
        }
        String code = productDetails.get("code");
        client.getExistingProductDetails(code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<State>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                                .title(R.string.toastSending)
                                .content(R.string.please_wait)
                                .cancelable(false)
                                .progress(true, 0);
                        dialog = builder.build();
                        dialog.show();
                    }

                    @Override
                    public void onSuccess(State state) {
                        dialog.dismiss();
                        if (state.getStatus() == 0) {
                            // Product doesn't exist yet on the server. Add as it is.
                            addProductToServer();
                        } else {
                            // Product already exists on the server. Compare values saved locally with the values existing on server.
                            ingredientsTextOnServer = state.getProduct().getIngredientsText();
                            productNameOnServer = state.getProduct().getProductName();
                            quantityOnServer = state.getProduct().getQuantity();
                            linkOnServer = state.getProduct().getManufactureUrl();
                            ingredientsImageOnServer = state.getProduct().getImageIngredientsUrl();
                            checkForExistingIngredients();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialog.dismiss();
                        // Add the images to the productDetails to display them in UI later.
                        productDetails.put("image_front", imagesFilePath[0]);
                        productDetails.put("image_ingredients", imagesFilePath[1]);
                        productDetails.put("image_nutrition_facts", imagesFilePath[2]);
                        OfflineSavedProduct offlineSavedProduct = new OfflineSavedProduct();
                        offlineSavedProduct.setBarcode(code);
                        offlineSavedProduct.setProductDetailsMap(productDetails);
                        mOfflineSavedProductDao.insertOrReplace(offlineSavedProduct);
                        Toast.makeText(AddProductActivity.this, R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.putExtra("uploadedToServer", false);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
    }

    /**
     * Checks if ingredients already exist on server and compare it with the ingredients stored locally.
     */
    private void checkForExistingIngredients() {
        if (ingredientsTextOnServer != null && !ingredientsTextOnServer.isEmpty() && productDetails.get("ingredients_text") != null) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                    .title(R.string.ingredients_overwrite)
                    .customView(R.layout.dialog_compare_ingredients, true)
                    .positiveText(R.string.choose_mine)
                    .negativeText(R.string.keep_previous_version)
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        checkForExistingProductName();
                    })
                    .onNegative((dialog, which) -> {
                        dialog.dismiss();
                        productDetails.remove("ingredients_text");
                        checkForExistingProductName();
                    })
                    .cancelable(false);
            MaterialDialog dialog = builder.build();
            dialog.show();
            View view = dialog.getCustomView();
            if (view != null) {
                ImageView imageLocal = view.findViewById(R.id.image_ingredients_local);
                ImageView imageServer = view.findViewById(R.id.image_ingredients_server);
                TextView ingredientsLocal = view.findViewById(R.id.txt_ingredients_local);
                TextView ingredientsServer = view.findViewById(R.id.txt_ingredients_server);
                ProgressBar imageProgressServer = view.findViewById(R.id.image_progress_server);
                ProgressBar imageProgressLocal = view.findViewById(R.id.image_progress_local);
                ingredientsLocal.setText(productDetails.get("ingredients_text"));
                ingredientsServer.setText(ingredientsTextOnServer);
                Picasso.with(this)
                        .load(ingredientsImageOnServer)
                        .error(R.drawable.placeholder_thumb)
                        .into(imageServer, new Callback() {
                            @Override
                            public void onSuccess() {
                                imageProgressServer.setVisibility(View.GONE);
                                // Add option to zoom image.
                                imageServer.setOnClickListener(v -> {
                                    Intent intent = new Intent(AddProductActivity.this, FullScreenImage.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("imageurl", ingredientsImageOnServer);
                                    intent.putExtras(bundle);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                                makeSceneTransitionAnimation(AddProductActivity.this, imageServer,
                                                        getString(R.string.product_transition));
                                        startActivity(intent, options.toBundle());
                                    } else {
                                        startActivity(intent);
                                    }
                                });
                            }

                            @Override
                            public void onError() {
                                imageProgressServer.setVisibility(View.GONE);
                            }
                        });
                Picasso.with(this)
                        .load("file://" + imagesFilePath[1])
                        .error(R.drawable.placeholder_thumb)
                        .into(imageLocal, new Callback() {
                            @Override
                            public void onSuccess() {
                                imageProgressLocal.setVisibility(View.GONE);
                                // Add option to zoom image.
                                imageLocal.setOnClickListener(v -> {
                                    Intent intent = new Intent(AddProductActivity.this, FullScreenImage.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("imageurl", "file://" + imagesFilePath[1]);
                                    intent.putExtras(bundle);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                                makeSceneTransitionAnimation(AddProductActivity.this, imageLocal,
                                                        getString(R.string.product_transition));
                                        startActivity(intent, options.toBundle());
                                    } else {
                                        startActivity(intent);
                                    }
                                });
                            }

                            @Override
                            public void onError() {
                                imageProgressLocal.setVisibility(View.GONE);
                            }
                        });
            }
        } else {
            checkForExistingProductName();
        }
    }

    /**
     * Checks if product name already exist on server and compare it with the product name stored locally.
     */
    private void checkForExistingProductName() {
        if (productNameOnServer != null && !productNameOnServer.isEmpty() && productDetails.get("product_name") != null) {
            new MaterialDialog.Builder(AddProductActivity.this)
                    .title(R.string.product_name_overwrite)
                    .content(getString(R.string.yours) + productDetails.get("product_name") + "\n" + getString(R.string.currently_on) + getString(R.string.app_name_long) + ": " + productNameOnServer)
                    .positiveText(R.string.choose_mine)
                    .negativeText(R.string.keep_previous_version)
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        checkForExistingQuantity();

                    })
                    .onNegative((dialog, which) -> {
                        dialog.dismiss();
                        productDetails.remove("product_name");
                        checkForExistingQuantity();
                    })
                    .cancelable(false)
                    .build()
                    .show();
        } else {
            checkForExistingQuantity();
        }
    }

    /**
     * Checks if quantity already exist on server and compare it with the quantity stored locally.
     */
    private void checkForExistingQuantity() {
        if (quantityOnServer != null && !quantityOnServer.isEmpty() && productDetails.get("quantity") != null) {
            new MaterialDialog.Builder(AddProductActivity.this)
                    .title(R.string.quantity_overwrite)
                    .content(getString(R.string.yours) + productDetails.get("quantity") + "\n" + getString(R.string.currently_on) + getString(R.string.app_name_long) + ": " + quantityOnServer)
                    .positiveText(R.string.choose_mine)
                    .negativeText(R.string.keep_previous_version)
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        checkForExistingLink();
                    })
                    .onNegative((dialog, which) -> {
                        dialog.dismiss();
                        productDetails.remove("quantity");
                        checkForExistingLink();
                    })
                    .cancelable(false)
                    .build()
                    .show();
        } else {
            checkForExistingLink();
        }
    }

    /**
     * Checks if link already exist on server and compare it with the link stored locally.
     */
    private void checkForExistingLink() {
        if (linkOnServer != null && !linkOnServer.isEmpty() && productDetails.get("link") != null) {
            new MaterialDialog.Builder(AddProductActivity.this)
                    .title(R.string.link_overwrite)
                    .content(getString(R.string.yours) + productDetails.get("link") + "\n" + getString(R.string.currently_on) + getString(R.string.app_name_long) + ": " + linkOnServer)
                    .positiveText(R.string.choose_mine)
                    .negativeText(R.string.keep_previous_version)
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        addProductToServer();
                    })
                    .onNegative((dialog, which) -> {
                        dialog.dismiss();
                        productDetails.remove("link");
                        addProductToServer();
                    })
                    .cancelable(false)
                    .build()
                    .show();
        } else {
            addProductToServer();
        }
    }

    /**
     * Performs network call and uploads the product to the server or stores it locally if there is no internet connection.
     */
    private void addProductToServer() {
        String code = productDetails.get("code");
        for (Map.Entry<String, String> entry : productDetails.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Log.d(key, value);
        }

        client.saveProductSingle(code, productDetails)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<State>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                                .title(R.string.toastSending)
                                .content(R.string.please_wait)
                                .progress(true, 0)
                                .cancelable(false);
                        dialog = builder.build();
                        dialog.show();
                    }

                    @Override
                    public void onSuccess(State state) {
                        dialog.dismiss();
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.product_uploaded_successfully, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        View view = toast.getView();
                        TextView textView = view.findViewById(android.R.id.message);
                        textView.setTextSize(18);
                        view.setBackgroundColor(getResources().getColor(R.color.green_500));
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.show();
                        mOfflineSavedProductDao.deleteInTx(mOfflineSavedProductDao.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(code)).list());
                        Intent intent = new Intent();
                        intent.putExtra("uploadedToServer", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialog.dismiss();
                        // Add the images to the productDetails to display them in UI later.
                        productDetails.put("image_front", imagesFilePath[0]);
                        productDetails.put("image_ingredients", imagesFilePath[1]);
                        productDetails.put("image_nutrition_facts", imagesFilePath[2]);
                        OfflineSavedProduct offlineSavedProduct = new OfflineSavedProduct();
                        offlineSavedProduct.setBarcode(code);
                        offlineSavedProduct.setProductDetailsMap(productDetails);
                        mOfflineSavedProductDao.insertOrReplace(offlineSavedProduct);
                        Toast.makeText(AddProductActivity.this, R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.putExtra("uploadedToServer", false);
                        setResult(RESULT_OK, intent);
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
                checkFields();
                break;
        }
    }

    private void checkFields() {
        if (addProductOverviewFragment.areRequiredFieldsEmpty()) {
            viewPager.setCurrentItem(0, true);
        } else if ((BuildConfig.FLAVOR.equals("off") || BuildConfig.FLAVOR.equals("opff")) && !addProductNutritionFactsFragment.isCheckPassed()) {
            viewPager.setCurrentItem(2, true);
        } else {
            saveProduct();
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
        if (image.getImguploadFront() != null) {
            imagesFilePath[0] = image.getFilePath();
            imgMap.put("imgupload_front\"; filename=\"front_" + lang + ".png\"", image.getImguploadFront());
        }
        if (image.getImguploadIngredients() != null) {
            imgMap.put("imgupload_ingredients\"; filename=\"ingredients_" + lang + ".png\"", image.getImguploadIngredients());
            ocr = true;
            imagesFilePath[1] = image.getFilePath();
        }
        if (image.getImguploadNutrition() != null) {
            imgMap.put("imgupload_nutrition\"; filename=\"nutrition_" + lang + ".png\"", image.getImguploadNutrition());
            imagesFilePath[2] = image.getFilePath();
        }
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
        client.saveImageSingle(imgMap)
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
                            if (error.equals("This picture has already been sent.") && ocr) {
                                hideImageProgress(position, false, getString(R.string.image_uploaded_successfully));
                                performOCR(image, "ingredients_" + getProductLanguage());
                            } else {
                                hideImageProgress(position, true, error);
                            }
                        } else {
                            hideImageProgress(position, false, getString(R.string.image_uploaded_successfully));
                            String imagefield = jsonNode.get("imagefield").asText();
                            String imgid = jsonNode.get("image").get("imgid").asText();
                            if (position != 3 && position != 4) {
                                // Not OTHER image
                                setPhoto(image, imagefield, imgid, ocr);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        // A network error happened
                        if (e instanceof IOException) {
                            hideImageProgress(position, false, getString(R.string.no_internet_connection));
                            Log.e(AddProductActivity.class.getSimpleName(), e.getMessage());
                            ToUploadProduct product = new ToUploadProduct(image.getBarcode(), image.getFilePath(), image.getImageField().toString());
                            mToUploadProductDao.insertOrReplace(product);

                        } else {
                            hideImageProgress(position, true, e.getMessage());
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
        client.editImageSingle(image.getBarcode(), queryMap)
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
                                View view = findViewById(R.id.coordinator_layout);
                                Snackbar.make(view, R.string.no_internet_unable_to_extract_ingredients, Snackbar.LENGTH_INDEFINITE)
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
        client.getIngredients(image.getBarcode(), imageField)
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
                            View view = findViewById(R.id.coordinator_layout);
                            Snackbar.make(view, R.string.no_internet_unable_to_extract_ingredients, Snackbar.LENGTH_INDEFINITE)
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
                break;
            case 3:
                addProductOverviewFragment.hideOtherImageProgress(errorUploading, message);
                break;
            case 4:
                addProductPhotosFragment.hideImageProgress(errorUploading, message);
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
                break;
            case 3:
                addProductOverviewFragment.showOtherImageProgress();
                break;
            case 4:
                addProductPhotosFragment.showImageProgress();
        }
    }

    public String getProductLanguage() {
        return productDetails.get("lang");
    }

    public void loadAutoSuggestion() {
        addProductIngredientsFragment.loadAutoSuggestions();
    }
}
