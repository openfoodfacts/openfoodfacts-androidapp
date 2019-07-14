package openfoodfacts.github.scrachx.openfood.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
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
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.*;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.FileUtils;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.isExternalStorageWritable;

public class AddProductActivity extends AppCompatActivity {
    private static final String KEY_USER_ID = "user_id";
    @SuppressWarnings("squid:S2068")
    private static final String KEY_PASSWORD = "password";
    public static final String PARAM_LANGUAGE = "lang";
    private static final String ADD_TAG = AddProductActivity.class.getSimpleName();
    public static final String UPLOADED_TO_SERVER = "uploadedToServer";
    public static final String MODIFY_NUTRITION_PROMPT = "modify_nutrition_prompt";
    public static final String MODIFY_CATEGORY_PROMPT = "modify_category_prompt";
    public static final String KEY_EDIT_PRODUCT = "edit_product";
    public static final String KEY_IS_EDITION = "is_edition";
    private final Map<String, String> productDetails = new HashMap<>();
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
    private AddProductOverviewFragment addProductOverviewFragment = new AddProductOverviewFragment();
    private AddProductIngredientsFragment addProductIngredientsFragment = new AddProductIngredientsFragment();
    private AddProductNutritionFactsFragment addProductNutritionFactsFragment = new AddProductNutritionFactsFragment();
    private AddProductPhotosFragment addProductPhotosFragment = new AddProductPhotosFragment();
    private Product mProduct;
    private ToUploadProductDao mToUploadProductDao;
    private OfflineSavedProductDao mOfflineSavedProductDao;
    private Disposable mainDisposable;
    private String[] imagesFilePath = new String[3];
    private OfflineSavedProduct offlineSavedProduct;
    private Map<String, String> initialValues;
    private Bundle mainBundle = new Bundle();
    private MaterialDialog materialDialog;
    private boolean imageFrontUploaded;
    private boolean imageIngredientsUploaded;
    private boolean imageNutritionFactsUploaded;
    private boolean editionMode;
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
                Log.i(ADD_TAG, "Directory created");
            } else {
                Log.i(ADD_TAG, "Couldn't create directory");
            }
        }
        return dir;
    }

    public static void clearCachedCameraPic(Context context) {
        File[] files = getCameraPicLocation(context).listFiles();
        for (File file : files) {
            if (file.delete()) {
                Log.i(ADD_TAG, "Deleted cached photo");
            } else {
                Log.i(ADD_TAG, "Couldn't delete cached photo");
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
     * @param overviewStage change the state of overview indicator
     * @param ingredientsStage change the state of ingredients indicator
     * @param nutritionFactsStage change the state of nutrition facts indicator
     */
    private void updateTimelineIndicator(int overviewStage, int ingredientsStage, int nutritionFactsStage) {

        updateTimeLine(overviewStage, overviewIndicator);
        updateTimeLine(ingredientsStage, ingredientsIndicator);
        updateTimeLine(nutritionFactsStage, nutritionFactsIndicator);
    }

    private static void updateTimeLine(int stage, View view) {
        switch (stage) {
            case 0:
                view.setBackgroundResource(R.drawable.stage_inactive);
                break;
            case 1:
                view.setBackgroundResource(R.drawable.stage_active);
                break;
            case 2:
                view.setBackgroundResource(R.drawable.stage_complete);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (offlineSavedProduct != null) {
            checkFields();
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
                checkFields();
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
        Product mEditProduct = (Product) getIntent().getSerializableExtra(KEY_EDIT_PRODUCT);

        if (getIntent().getBooleanExtra("perform_ocr", false)) {
            mainBundle.putBoolean("perform_ocr", true);
        }

        if (getIntent().getBooleanExtra("send_updated", false)) {
            mainBundle.putBoolean("send_updated", true);
        }

        if (state != null) {
            mProduct = state.getProduct();
            // Search if the barcode already exists in the OfflineSavedProducts db
            offlineSavedProduct = mOfflineSavedProductDao.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(mProduct.getCode())).unique();
        }
        if (mEditProduct != null) {
            setTitle(R.string.edit_product_title);
            mProduct = mEditProduct;
            editionMode = true;
            mainBundle.putBoolean(KEY_IS_EDITION, true);
            initialValues = new HashMap<>();
        } else if (offlineSavedProduct != null) {
            mainBundle.putSerializable("edit_offline_product", offlineSavedProduct);
            // Save the already existing images in productDetails for UI
            imagesFilePath[0] = offlineSavedProduct.getProductDetailsMap().get("image_front");
            imagesFilePath[1] = offlineSavedProduct.getProductDetailsMap().get("image_ingredients");
            imagesFilePath[2] = offlineSavedProduct.getProductDetailsMap().get("image_nutrition_facts");
            // get the status of images from productDetailsMap, whether uploaded or not
            imageFrontUploaded = "true".equals(offlineSavedProduct.getProductDetailsMap().get("image_front_uploaded"));
            imageIngredientsUploaded = "true".equals(offlineSavedProduct.getProductDetailsMap().get("image_ingredients_uploaded"));
            imageNutritionFactsUploaded = "true".equals(offlineSavedProduct.getProductDetailsMap().get("image_nutrition_facts_uploaded"));
        }
        if (state == null && offlineSavedProduct == null && mEditProduct == null) {
            Toast.makeText(this, R.string.error_adding_product, Toast.LENGTH_SHORT).show();
            finish();
        }
        setupViewPager(viewPager);
    }

    public Map<String, String> getInitialValues() {
        return initialValues;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (materialDialog != null && materialDialog.isShowing()) {
            materialDialog.dismiss();
        }
        if (mainDisposable != null && !mainDisposable.isDisposed()) {
            mainDisposable.dispose();
        }
        clearCachedCameraPic(this);
    }

    private void setupViewPager(ViewPager viewPager) {
        ProductFragmentPagerAdapter adapterResult = new ProductFragmentPagerAdapter(getSupportFragmentManager());
        mainBundle.putSerializable("product", mProduct);
        addProductOverviewFragment.setArguments(mainBundle);
        addProductIngredientsFragment.setArguments(mainBundle);
        adapterResult.addFragment(addProductOverviewFragment, "Overview");
        adapterResult.addFragment(addProductIngredientsFragment, "Ingredients");
        if (isNutritionDataAvailable()) {
            addProductNutritionFactsFragment.setArguments(mainBundle);
            adapterResult.addFragment(addProductNutritionFactsFragment, "Nutrition Facts");
        } else if (BuildConfig.FLAVOR.equals("obf") || BuildConfig.FLAVOR.equals("opf")) {
            nutritionFactsIndicatorText.setText(R.string.photos);
            addProductPhotosFragment.setArguments(mainBundle);
            adapterResult.addFragment(addProductPhotosFragment, "Photos");
        }
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapterResult);
    }

    private void saveProduct() {
        addProductOverviewFragment.getDetails();
        addProductIngredientsFragment.getDetails();
        if (isNutritionDataAvailable()) {
            addProductNutritionFactsFragment.getDetails(productDetails);
        }
        addLoginInfoInProductDetails();
        String code = productDetails.get("code");
        String fields = "link,quantity,image_ingredients_url,ingredients_text_" + getProductLanguageForEdition() + ",product_name_" + getProductLanguageForEdition();
        client.getProductByBarcodeSingle(code, fields, Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH))
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
                    materialDialog = builder.build();
                    materialDialog.show();
                }

                @Override
                public void onSuccess(State state) {
                    materialDialog.dismiss();
                    if (state.getStatus() == 0) {
                        // Product doesn't exist yet on the server. Add as it is.
                        checkFrontImageUploadStatus();
                    } else {
                        // Product already exists on the server. Compare values saved locally with the values existing on server.
                        ingredientsTextOnServer = state.getProduct().getIngredientsText(getProductLanguageForEdition());
                        productNameOnServer = state.getProduct().getProductName(getProductLanguageForEdition());
                        quantityOnServer = state.getProduct().getQuantity();
                        linkOnServer = state.getProduct().getManufactureUrl();
                        ingredientsImageOnServer = state.getProduct().getImageIngredientsUrl();
                        checkForExistingIngredients();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    materialDialog.dismiss();
                    saveProductOffline();
                }
            });
    }

    /**
     * Checks if ingredients already exist on server and compare it with the ingredients stored locally.
     */
    private void checkForExistingIngredients() {
        String lc = getLanguageFromDetails();
        if (ingredientsTextOnServer != null && !ingredientsTextOnServer.isEmpty() && productDetails.get("ingredients_text" + "_" + lc) != null) {
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
                    productDetails.remove("ingredients_text" + "_" + lc);
                    productDetails.remove("image_ingredients");
                    imagesFilePath[1] = null;
                    checkForExistingProductName();
                });
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
                ingredientsLocal.setText(productDetails.get("ingredients_text" + "_" + lc));
                ingredientsServer.setText(ingredientsTextOnServer);
                Picasso.get()
                    .load(ingredientsImageOnServer)
                    .error(R.drawable.placeholder_thumb)
                    .into(imageServer, new Callback() {
                        @Override
                        public void onSuccess() {
                            imageProgressServer.setVisibility(View.GONE);
                            // Add option to zoom image.
                            imageServer.setOnClickListener(v -> {
                                showFullscreen(ingredientsImageOnServer, imageServer);
                            });
                        }

                        @Override
                        public void onError(Exception ex) {
                            imageProgressServer.setVisibility(View.GONE);
                        }
                    });
                Picasso.get()
                    .load(FileUtils.LOCALE_FILE_SCHEME + imagesFilePath[1])
                    .error(R.drawable.placeholder_thumb)
                    .into(imageLocal, new Callback() {
                        @Override
                        public void onSuccess() {
                            imageProgressLocal.setVisibility(View.GONE);
                            // Add option to zoom image.
                            imageLocal.setOnClickListener(v -> {
                                showFullscreen(FileUtils.LOCALE_FILE_SCHEME + imagesFilePath[1], imageLocal);
                            });
                        }

                        @Override
                        public void onError(Exception ex) {
                            imageProgressLocal.setVisibility(View.GONE);
                        }
                    });
            }
        } else {
            checkForExistingProductName();
        }
    }

    public void showFullscreen(String s, ImageView imageLocal) {
        Intent intent = new Intent(AddProductActivity.this, ProductImageManagementActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("imageurl", s);
        intent.putExtras(bundle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(AddProductActivity.this, imageLocal,
                    getString(R.string.product_transition));
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    private String getLanguageFromDetails() {
        return productDetails.get(PARAM_LANGUAGE) != null ? productDetails.get(PARAM_LANGUAGE) : "en";
    }

    /**
     * Checks if product name already exist on server and compare it with the product name stored locally.
     */
    private void checkForExistingProductName() {
        String lc = getLanguageFromDetails();
        if (productNameOnServer != null && !productNameOnServer.isEmpty() && productDetails.get("product_name" + "_" + lc) != null) {
            new MaterialDialog.Builder(AddProductActivity.this)
                .title(R.string.product_name_overwrite)
                .content(getString(R.string.yours) + productDetails.get("product_name" + "_" + lc) + "\n" + getString(R.string.currently_on,
                    getString(R.string.app_name_long)) + productNameOnServer)
                .positiveText(R.string.choose_mine)
                .negativeText(R.string.keep_previous_version)
                .onPositive((dialog, which) -> {
                    dialog.dismiss();
                    checkForExistingQuantity();
                })
                .onNegative((dialog, which) -> {
                    dialog.dismiss();
                    productDetails.remove("product_name" + "_" + lc);
                    checkForExistingQuantity();
                })
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
                .content(getString(R.string.yours) + productDetails.get("quantity") + "\n" + getString(R.string.currently_on, getString(R.string.app_name_long)) + quantityOnServer)
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
                .content(getString(R.string.yours) + productDetails.get("link") + "\n" + getString(R.string.currently_on, getString(R.string.app_name_long)) + linkOnServer)
                .positiveText(R.string.choose_mine)
                .negativeText(R.string.keep_previous_version)
                .onPositive((dialog, which) -> {
                    dialog.dismiss();
                    checkFrontImageUploadStatus();
                })
                .onNegative((dialog, which) -> {
                    dialog.dismiss();
                    productDetails.remove("link");
                    checkFrontImageUploadStatus();
                })
                .build()
                .show();
        } else {
            checkFrontImageUploadStatus();
        }
    }

    /**
     * Upload and set the front image if it is not uploaded already.
     */
    private void checkFrontImageUploadStatus() {
        String code = productDetails.get("code");
        if (!imageFrontUploaded && imagesFilePath[0] != null && !imagesFilePath[0].isEmpty()) {
            // front image is not yet uploaded.
            File photoFile = new File(imagesFilePath[0]);
            Map<String, RequestBody> imgMap = new HashMap<>();
            RequestBody barcode = createTextPlain(code);
            RequestBody imageField = createTextPlain(ProductImageField.FRONT.toString() + '_' + getProductLanguageForEdition());
            RequestBody image = ProductImage.createImageRequest(photoFile);
            imgMap.put("code", barcode);
            imgMap.put("imagefield", imageField);
            imgMap.put("imgupload_front\"; filename=\"front_" + getProductLanguageForEdition() + ".png\"", image);

            // Attribute the upload to the connected user
            addLoginPasswordInfo(imgMap);

            client.saveImageSingle(imgMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<JsonNode>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                            .title(R.string.uploading_front_image)
                            .content(R.string.please_wait)
                            .cancelable(false)
                            .progress(true, 0);
                        materialDialog = builder.build();
                        materialDialog.show();
                    }

                    @Override
                    public void onSuccess(JsonNode jsonNode) {
                        String status = jsonNode.get("status").asText();
                        if (status.equals("status not ok")) {
                            materialDialog.dismiss();
                            String error = jsonNode.get("error").asText();
                            if (error.equals("This picture has already been sent.")) {
                                imageFrontUploaded = true;
                                checkIngredientsImageUploadStatus();
                            } else {
                                new MaterialDialog.Builder(AddProductActivity.this).title(R.string.uploading_front_image)
                                    .content(error).show();
                            }
                        } else {
                            imageFrontUploaded = true;
                            Map<String, String> queryMap = buildImageQueryMap(jsonNode);
                            client.editImageSingle(code, queryMap)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<JsonNode>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(JsonNode jsonNode) {
                                        materialDialog.dismiss();
                                        checkIngredientsImageUploadStatus();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        dialogNetworkIssueWhileUploadingImages();
                                    }
                                });
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialogNetworkIssueWhileUploadingImages();
                    }
                });
        } else {
            // front image is uploaded, check the status of ingredients image.
            checkIngredientsImageUploadStatus();
        }
    }

    private RequestBody createTextPlain(String code) {
        return RequestBody.create(MediaType.parse(OpenFoodAPIClient.TEXT_PLAIN), code);
    }

    /**
     * Upload and set the ingredients image if it is not uploaded already.
     */
    private void checkIngredientsImageUploadStatus() {
        String code = productDetails.get("code");
        if (!imageIngredientsUploaded && imagesFilePath[1] != null && !imagesFilePath[1].isEmpty()) {
            // ingredients image is not yet uploaded.
            File photoFile = new File(imagesFilePath[1]);
            Map<String, RequestBody> imgMap = createRequestBodyMap(code, ProductImageField.INGREDIENTS);
            RequestBody image = ProductImage.createImageRequest(photoFile);
            imgMap.put("imgupload_ingredients\"; filename=\"ingredients_" + getProductLanguageForEdition() + ".png\"", image);

            // Attribute the upload to the connected user
            addLoginPasswordInfo(imgMap);

            client.saveImageSingle(imgMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<JsonNode>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                            .title(R.string.uploading_ingredients_image)
                            .content(R.string.please_wait)
                            .cancelable(false)
                            .progress(true, 0);
                        materialDialog = builder.build();
                        materialDialog.show();
                    }

                    @Override
                    public void onSuccess(JsonNode jsonNode) {
                        String status = jsonNode.get("status").asText();
                        if (status.equals("status not ok")) {
                            materialDialog.dismiss();
                            String error = jsonNode.get("error").asText();
                            if (error.equals("This picture has already been sent.")) {
                                imageIngredientsUploaded = true;
                                checkNutritionFactsImageUploadStatus();
                            } else {
                                new MaterialDialog.Builder(AddProductActivity.this).title(R.string.uploading_ingredients_image)
                                    .content(error).show();
                            }
                        } else {
                            imageIngredientsUploaded = true;
                            Map<String, String> queryMap = buildImageQueryMap(jsonNode);
                            client.editImageSingle(code, queryMap)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<JsonNode>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(JsonNode jsonNode) {
                                        materialDialog.dismiss();
                                        checkNutritionFactsImageUploadStatus();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        dialogNetworkIssueWhileUploadingImages();
                                    }
                                });
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialogNetworkIssueWhileUploadingImages();
                    }
                });
        } else {
            // ingredients image is uploaded, check the status of nutrition facts image.
            checkNutritionFactsImageUploadStatus();
        }
    }

    private void addLoginPasswordInfo(Map<String, RequestBody> imgMap) {
        final SharedPreferences settings = getSharedPreferences("login", 0);
        final String login = settings.getString("user", "");
        final String password = settings.getString("pass", "");
        if (!login.isEmpty() && !password.isEmpty()) {
            imgMap.put(KEY_USER_ID, createTextPlain(login));
            imgMap.put(KEY_PASSWORD, createTextPlain(password));
        }
        imgMap.put("comment", createTextPlain(OpenFoodAPIClient.getCommentToUpload(login)));
    }

    public static Map<String, String> buildImageQueryMap(JsonNode jsonNode) {
        String imagefield = jsonNode.get("imagefield").asText();
        String imgid = jsonNode.get("image").get("imgid").asText();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("imgid", imgid);
        queryMap.put("id", imagefield);
        return queryMap;
    }

    /**
     * Upload and set the nutrition facts image if it is not uploaded already.
     */
    private void checkNutritionFactsImageUploadStatus() {
        String code = productDetails.get("code");
        if (!imageNutritionFactsUploaded && imagesFilePath[2] != null && !imagesFilePath[2].isEmpty()) {
            // nutrition facts image is not yet uploaded.
            File photoFile = new File(imagesFilePath[2]);
            Map<String, RequestBody> imgMap = createRequestBodyMap(code, ProductImageField.NUTRITION);
            RequestBody image = ProductImage.createImageRequest( photoFile);
            imgMap.put("imgupload_nutrition\"; filename=\"nutrition_" + getProductLanguageForEdition() + ".png\"", image);

            // Attribute the upload to the connected user
            addLoginPasswordInfo(imgMap);

            client.saveImageSingle(imgMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<JsonNode>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                            .title(R.string.uploading_nutrition_image)
                            .content(R.string.please_wait)
                            .cancelable(false)
                            .progress(true, 0);
                        materialDialog = builder.build();
                        materialDialog.show();
                    }

                    @Override
                    public void onSuccess(JsonNode jsonNode) {
                        String status = jsonNode.get("status").asText();
                        if (status.equals("status not ok")) {
                            materialDialog.dismiss();
                            String error = jsonNode.get("error").asText();
                            if (error.equals("This picture has already been sent.")) {
                                imageNutritionFactsUploaded = true;
                                addProductToServer();
                            } else {
                                new MaterialDialog.Builder(AddProductActivity.this).title(R.string.uploading_nutrition_image)
                                    .content(error).show();
                            }
                        } else {
                            imageNutritionFactsUploaded = true;
                            Map<String, String> queryMap = buildImageQueryMap(jsonNode);
                            client.editImageSingle(code, queryMap)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<JsonNode>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(JsonNode jsonNode) {
                                        materialDialog.dismiss();
                                        addProductToServer();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        dialogNetworkIssueWhileUploadingImages();
                                    }
                                });
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialogNetworkIssueWhileUploadingImages();
                    }
                });
        } else {
            // nutrition facts image is uploaded, upload the product to server.
            addProductToServer();
        }
    }

    private Map<String, RequestBody> createRequestBodyMap(String code, ProductImageField nutrition) {
        Map<String, RequestBody> imgMap = new HashMap<>();
        imgMap.put("code", createTextPlain(code));
        imgMap.put("imagefield", createTextPlain(nutrition.toString() + '_' + getProductLanguageForEdition()));
        return imgMap;
    }

    private void dialogNetworkIssueWhileUploadingImages() {
        materialDialog.dismiss();
        if (!editionMode) {
            saveProductOffline();
        } else {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                .title(R.string.device_offline_dialog_title)
                .positiveText(R.string.txt_try_again)
                .negativeText(R.string.dialog_cancel)
                .onPositive((dialog, which) -> checkFrontImageUploadStatus())
                .onNegative((dialog, which) -> dialog.dismiss());
            materialDialog = builder.build();
            materialDialog.show();
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
        final SharedPreferences settings = getSharedPreferences("login", 0);
        final String login = settings.getString("user", "");

        boolean productHasChange = true;
        if (editionMode && initialValues != null) {
            Map<String, String> newValues = new HashMap<>(productDetails);
            newValues.remove(KEY_USER_ID);
            newValues.remove(KEY_PASSWORD);
            productHasChange = !newValues.equals(initialValues);
        }
        if (productHasChange) {
            saveProductToServer(code, OpenFoodAPIClient.getCommentToUpload(login));
        } else {
            Log.i(ADD_TAG, "not saved because no changes detected");
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void saveProductToServer(String code, String comment) {
        Map<String, String> productValues = new HashMap<>(productDetails);
        //the default language should not be changed: we keep the original one:
        if (editionMode && StringUtils.isNotBlank(mProduct.getLang())) {
            productValues.put(PARAM_LANGUAGE, mProduct.getLang());
        }
        client.saveProductSingle(code, productValues, comment)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new SingleObserver<State>() {
                @Override
                public void onSubscribe(Disposable d) {
                    mainDisposable = d;
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                        .title(R.string.toastSending)
                        .content(R.string.please_wait)
                        .progress(true, 0)
                        .cancelable(false);
                    materialDialog = builder.build();
                    materialDialog.show();
                }

                @Override
                public void onSuccess(State state) {
                    materialDialog.dismiss();
                    Toast toast = Toast.makeText(OFFApplication.getInstance(), R.string.product_uploaded_successfully, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    View view = toast.getView();
                    TextView textView = view.findViewById(android.R.id.message);
                    textView.setTextSize(18);
                    view.setBackgroundColor(getResources().getColor(R.color.green_500));
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                    mOfflineSavedProductDao.deleteInTx(mOfflineSavedProductDao.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(code)).list());
                    Intent intent = new Intent();
                    intent.putExtra(UPLOADED_TO_SERVER, true);
                    setResult(RESULT_OK, intent);
                    finish();
                }

                @Override
                public void onError(Throwable e) {
                    materialDialog.dismiss();
                    Log.e(ADD_TAG, e.getMessage());
                    // A network error happened
                    if (e instanceof IOException) {
                        dialogNetworkIssueWhileUploadingImages();
                    }
                    // Not a network error
                    else {
                        if (!editionMode) {
                            Toast.makeText(AddProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            saveProductOffline();
                        } else {
                            MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                                .title(R.string.error_adding_product)
                                .positiveText(R.string.txt_try_again)
                                .negativeText(R.string.dialog_cancel)
                                .onPositive((dialog, which) -> checkFrontImageUploadStatus())
                                .onNegative((dialog, which) -> dialog.dismiss());
                            materialDialog = builder.build();
                            materialDialog.show();
                        }
                    }
                }
            });
    }

    /**
     * save the current product in the offline db
     */
    private void saveProductOffline() {
        // Add the images to the productDetails to display them in UI later.
        productDetails.put("image_front", imagesFilePath[0]);
        productDetails.put("image_ingredients", imagesFilePath[1]);
        productDetails.put("image_nutrition_facts", imagesFilePath[2]);
        // Add the status of images to the productDetails, whether uploaded or not
        if (imageFrontUploaded) {
            productDetails.put("image_front_uploaded", "true");
        }
        if (imageIngredientsUploaded) {
            productDetails.put("image_ingredients_uploaded", "true");
        }
        if (imageNutritionFactsUploaded) {
            productDetails.put("image_nutrition_facts_uploaded", "true");
        }
        OfflineSavedProduct offlineSavedProduct = new OfflineSavedProduct();
        offlineSavedProduct.setBarcode(productDetails.get("code"));
        offlineSavedProduct.setProductDetailsMap(productDetails);
        mOfflineSavedProductDao.insertOrReplace(offlineSavedProduct);
        Toast.makeText(OFFApplication.getInstance(), R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.putExtra(UPLOADED_TO_SERVER, false);
        setResult(RESULT_OK, intent);
        finish();
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
        if (!editionMode) {
            if (addProductOverviewFragment.areRequiredFieldsEmpty()) {
                viewPager.setCurrentItem(0, true);
            } else if (isNutritionDataAvailable() && addProductNutritionFactsFragment.containsInvalidValue()) {
                viewPager.setCurrentItem(2, true);
            } else {
                saveProduct();
            }
        } else {
            // edit mode, therefore do not check whether front image is empty or not however do check the nutrition facts values.
            if ((isNutritionDataAvailable()) && addProductNutritionFactsFragment.containsInvalidValue()) {
                viewPager.setCurrentItem(2, true);
            } else {
                saveEditedProduct();
            }
        }
    }

    private boolean isNutritionDataAvailable() {
        return BuildConfig.FLAVOR.equals("off") || BuildConfig.FLAVOR.equals("opff");
    }

    private void saveEditedProduct() {
        addProductOverviewFragment.getAllDetails(productDetails);
        addProductIngredientsFragment.getAllDetails(productDetails);
        if (isNutritionDataAvailable()) {
            addProductNutritionFactsFragment.getAllDetails(productDetails);
        }
        addLoginInfoInProductDetails();
        checkFrontImageUploadStatus();
    }

    private void addLoginInfoInProductDetails() {
        final SharedPreferences settings = getSharedPreferences("login", 0);
        final String login = settings.getString("user", "");
        final String password = settings.getString("pass", "");
        if (!login.isEmpty() && !password.isEmpty()) {
            productDetails.put(KEY_USER_ID, login);
            productDetails.put(KEY_PASSWORD, password);
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
        String lang = getProductLanguageForEdition();
        boolean ocr = false;
        Map<String, RequestBody> imgMap = new HashMap<>();
        imgMap.put("code", image.getCode());
        RequestBody imageField = createTextPlain(image.getImageField().toString() + '_' + lang);
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
        if (image.getImguploadOther() != null) {
            imgMap.put("imgupload_other\"; filename=\"other_" + lang + ".png\"", image.getImguploadOther());
        }
        // Attribute the upload to the connected user
        addLoginPasswordInfo(imgMap);
        savePhoto(imgMap, image, position, ocr);
    }

    private void savePhoto(Map<String, RequestBody> imgMap, ProductImage image, int position, boolean ocr) {
        client.saveImageSingle(imgMap)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(disposable -> showImageProgress(position))
            .subscribe(new SingleObserver<JsonNode>() {
                @Override
                public void onSubscribe(Disposable d) {
                    mainDisposable = d;
                }

                @Override
                public void onSuccess(JsonNode jsonNode) {
                    String status = jsonNode.get("status").asText();
                    if (status.equals("status not ok")) {
                        String error = jsonNode.get("error").asText();
                        final boolean alreadySent = error.equals("This picture has already been sent.");
                        if (alreadySent && ocr) {
                            hideImageProgress(position, false, getString(R.string.image_uploaded_successfully));
                            performOCR(image.getBarcode(), "ingredients_" + getProductLanguageForEdition());
                        } else {
                            hideImageProgress(position, true, error);
                        }
                        if (!alreadySent) {
                            new MaterialDialog.Builder(AddProductActivity.this).title(R.string.error_uploading_photo)
                                .content(error).show();
                        }
                    } else {
                        if (image.getImageField() == ProductImageField.FRONT) {
                            imageFrontUploaded = true;
                        } else if (image.getImageField() == ProductImageField.INGREDIENTS) {
                            imageIngredientsUploaded = true;
                        } else if (image.getImageField() == ProductImageField.NUTRITION) {
                            imageNutritionFactsUploaded = true;
                        }
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
                        Log.e(ADD_TAG, e.getMessage());
                        if (image.getImageField() == ProductImageField.OTHER) {
                            ToUploadProduct product = new ToUploadProduct(image.getBarcode(), image.getFilePath(), image.getImageField().toString());
                            mToUploadProductDao.insertOrReplace(product);
                        }
                    } else {
                        hideImageProgress(position, true, e.getMessage());
                        Log.i(this.getClass().getSimpleName(), e.getMessage());
                        Toast.makeText(OFFApplication.getInstance(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    if (ocr && status.equals("status ok")) {
                        performOCR(image.getBarcode(), imagefield);
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
                        Toast.makeText(OFFApplication.getInstance(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    public void performOCR(String code, String imageField) {
        client.getIngredients(code, imageField)
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
                            .setAction(R.string.txt_try_again, v -> performOCR(code, imageField)).show();
                    } else {
                        Log.i(this.getClass().getSimpleName(), e.getMessage(), e);
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

    public String getProductLanguageForEdition() {
        return productDetails.get(PARAM_LANGUAGE);
    }

    public void setProductLanguage(String languageCode) {
        addToMap(PARAM_LANGUAGE, languageCode);
    }

    public void updateLanguage() {
        addProductIngredientsFragment.loadIngredientsImage();
        addProductNutritionFactsFragment.loadNutritionImage();
    }

    public void setIngredients(String status, String ingredients) {
        addProductIngredientsFragment.setIngredients(status, ingredients);
    }
}
