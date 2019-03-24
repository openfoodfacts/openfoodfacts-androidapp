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
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.models.ToUploadProduct;
import openfoodfacts.github.scrachx.openfood.models.ToUploadProductDao;
import openfoodfacts.github.scrachx.openfood.models.YourListedProduct;
import openfoodfacts.github.scrachx.openfood.models.YourListedProductDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;

import static openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService.PRODUCT_API_COMMENT;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.isExternalStorageWritable;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class AddProductActivity extends AppCompatActivity {

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
    private boolean image_front_uploaded;
    private boolean image_ingredients_uploaded;
    private boolean image_nutrition_facts_uploaded;
    private boolean edit_product;

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
        Product mEditProduct = (Product) getIntent().getSerializableExtra("edit_product");

        if(getIntent().getBooleanExtra("perform_ocr",false)) {
            bundle.putBoolean("perform_ocr",true);
        }

        if (getIntent().getBooleanExtra("send_updated", false)) {
            bundle.putBoolean("send_updated", true);
        }

        if (state != null) {
            mProduct = state.getProduct();
            // Search if the barcode already exists in the OfflineSavedProducts db
            offlineSavedProduct = mOfflineSavedProductDao.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(mProduct.getCode())).unique();
        }
        if (mEditProduct != null) {
            setTitle(R.string.edit_product_title);
            mProduct = mEditProduct;
            edit_product = true;
            bundle.putBoolean("edit_product", true);
        } else if (offlineSavedProduct != null) {
            bundle.putSerializable("edit_offline_product", offlineSavedProduct);
            // Save the already existing images in productDetails for UI
            imagesFilePath[0] = offlineSavedProduct.getProductDetailsMap().get("image_front");
            imagesFilePath[1] = offlineSavedProduct.getProductDetailsMap().get("image_ingredients");
            imagesFilePath[2] = offlineSavedProduct.getProductDetailsMap().get("image_nutrition_facts");
            // get the status of images from productDetailsMap, whether uploaded or not
            String image_front_status = offlineSavedProduct.getProductDetailsMap().get("image_front_uploaded");
            String image_ingredients_status = offlineSavedProduct.getProductDetailsMap().get("image_ingredients_uploaded");
            String image_nutrition_facts_status = offlineSavedProduct.getProductDetailsMap().get("image_nutrition_facts_uploaded");
            image_front_uploaded = image_front_status != null && image_front_status.equals("true");
            image_ingredients_uploaded = image_ingredients_status != null && image_ingredients_status.equals("true");
            image_nutrition_facts_uploaded = image_nutrition_facts_status != null && image_nutrition_facts_status.equals("true");
        }
        if (state == null && offlineSavedProduct == null && mEditProduct == null) {
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
        if (isNutritionDataAvailable()) {
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
        if (isNutritionDataAvailable()) {
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
        String fields = "link,quantity,image_ingredients_url,ingredients_text_" + getProductLanguage() + ",product_name_" + getProductLanguage();
        client.getExistingProductDetails(code, fields, Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH))
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
                            checkFrontImageUploadStatus();
                        } else {
                            // Product already exists on the server. Compare values saved locally with the values existing on server.
                            ingredientsTextOnServer = state.getProduct().getIngredientsText(getProductLanguage());
                            productNameOnServer = state.getProduct().getProductName(getProductLanguage());
                            quantityOnServer = state.getProduct().getQuantity();
                            linkOnServer = state.getProduct().getManufactureUrl();
                            ingredientsImageOnServer = state.getProduct().getImageIngredientsUrl();
                            checkForExistingIngredients();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialog.dismiss();
                        saveProductOffline();
                    }
                });
        if(productDetails.get("eating").equals("true")){
            //add product to eaten list
            addProductToList(1L,getString(R.string.txt_eaten_products));
        }
    }

    /**
     * Checks if ingredients already exist on server and compare it with the ingredients stored locally.
     */
    private void checkForExistingIngredients() {
        String lc = productDetails.get("lang") != null ? productDetails.get("lang") : "en";
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
        String lc = productDetails.get("lang") != null ? productDetails.get("lang") : "en";
        if (productNameOnServer != null && !productNameOnServer.isEmpty() && productDetails.get("product_name" + "_" + lc) != null) {
            new MaterialDialog.Builder(AddProductActivity.this)
                    .title(R.string.product_name_overwrite)
                    .content(getString(R.string.yours) + productDetails.get("product_name" + "_" + lc) + "\n" + getString(R.string.currently_on, getString(R.string.app_name_long)) + productNameOnServer)
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
        if (!image_front_uploaded && imagesFilePath[0] != null && !imagesFilePath[0].isEmpty()) {
            // front image is not yet uploaded.
            File photoFile = new File(imagesFilePath[0]);
            Map<String, RequestBody> imgMap = new HashMap<>();
            RequestBody barcode = RequestBody.create(MediaType.parse("text/plain"), code);
            RequestBody imageField = RequestBody.create(MediaType.parse("text/plain"), ProductImageField.FRONT.toString() + '_' + getProductLanguage());
            RequestBody image = RequestBody.create(MediaType.parse("image/*"), photoFile);
            imgMap.put("code", barcode);
            imgMap.put("imagefield", imageField);
            imgMap.put("imgupload_front\"; filename=\"front_" + getProductLanguage() + ".png\"", image);

            // Attribute the upload to the connected user
            final SharedPreferences settings = getSharedPreferences("login", 0);
            final String login = settings.getString("user", "");
            final String password = settings.getString("pass", "");
            if (!login.isEmpty() && !password.isEmpty()) {
                imgMap.put("user_id", RequestBody.create(MediaType.parse("text/plain"), login));
                imgMap.put("password", RequestBody.create(MediaType.parse("text/plain"), password));
            }

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
                            dialog = builder.build();
                            dialog.show();
                        }

                        @Override
                        public void onSuccess(JsonNode jsonNode) {
                            String status = jsonNode.get("status").asText();
                            if (status.equals("status not ok")) {
                                dialog.dismiss();
                                String error = jsonNode.get("error").asText();
                                if (error.equals("This picture has already been sent.")) {
                                    image_front_uploaded = true;
                                    checkIngredientsImageUploadStatus();
                                }
                            } else {
                                image_front_uploaded = true;
                                String imagefield = jsonNode.get("imagefield").asText();
                                String imgid = jsonNode.get("image").get("imgid").asText();
                                Map<String, String> queryMap = new HashMap<>();
                                queryMap.put("imgid", imgid);
                                queryMap.put("id", imagefield);
                                client.editImageSingle(code, queryMap)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SingleObserver<JsonNode>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {

                                            }

                                            @Override
                                            public void onSuccess(JsonNode jsonNode) {
                                                dialog.dismiss();
                                                checkIngredientsImageUploadStatus();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                dialog.dismiss();
                                                if (!edit_product) {
                                                    saveProductOffline();
                                                } else {
                                                    MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                                                            .title(R.string.device_offline_dialog_title)
                                                            .positiveText(R.string.txt_try_again)
                                                            .negativeText(R.string.dialog_cancel)
                                                            .onPositive((dialog, which) -> checkFrontImageUploadStatus())
                                                            .onNegative((dialog, which) -> dialog.dismiss());
                                                    dialog = builder.build();
                                                    dialog.show();
                                                }
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            dialog.dismiss();
                            if (!edit_product) {
                                saveProductOffline();
                            } else {
                                MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                                        .title(R.string.device_offline_dialog_title)
                                        .positiveText(R.string.txt_try_again)
                                        .negativeText(R.string.dialog_cancel)
                                        .onPositive((dialog, which) -> checkFrontImageUploadStatus())
                                        .onNegative((dialog, which) -> dialog.dismiss());
                                dialog = builder.build();
                                dialog.show();
                            }
                        }
                    });
        } else {
            // front image is uploaded, check the status of ingredients image.
            checkIngredientsImageUploadStatus();
        }
    }

    /**
     * Upload and set the ingredients image if it is not uploaded already.
     */
    private void checkIngredientsImageUploadStatus() {
        String code = productDetails.get("code");
        if (!image_ingredients_uploaded && imagesFilePath[1] != null && !imagesFilePath[1].isEmpty()) {
            // ingredients image is not yet uploaded.
            File photoFile = new File(imagesFilePath[1]);
            Map<String, RequestBody> imgMap = new HashMap<>();
            RequestBody barcode = RequestBody.create(MediaType.parse("text/plain"), code);
            RequestBody imageField = RequestBody.create(MediaType.parse("text/plain"), ProductImageField.INGREDIENTS.toString() + '_' + getProductLanguage());
            RequestBody image = RequestBody.create(MediaType.parse("image/*"), photoFile);
            imgMap.put("code", barcode);
            imgMap.put("imagefield", imageField);
            imgMap.put("imgupload_ingredients\"; filename=\"ingredients_" + getProductLanguage() + ".png\"", image);

            // Attribute the upload to the connected user
            final SharedPreferences settings = getSharedPreferences("login", 0);
            final String login = settings.getString("user", "");
            final String password = settings.getString("pass", "");
            if (!login.isEmpty() && !password.isEmpty()) {
                imgMap.put("user_id", RequestBody.create(MediaType.parse("text/plain"), login));
                imgMap.put("password", RequestBody.create(MediaType.parse("text/plain"), password));
            }

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
                            dialog = builder.build();
                            dialog.show();
                        }

                        @Override
                        public void onSuccess(JsonNode jsonNode) {
                            String status = jsonNode.get("status").asText();
                            if (status.equals("status not ok")) {
                                dialog.dismiss();
                                String error = jsonNode.get("error").asText();
                                if (error.equals("This picture has already been sent.")) {
                                    image_ingredients_uploaded = true;
                                    checkNutritionFactsImageUploadStatus();
                                }
                            } else {
                                image_ingredients_uploaded = true;
                                String imagefield = jsonNode.get("imagefield").asText();
                                String imgid = jsonNode.get("image").get("imgid").asText();
                                Map<String, String> queryMap = new HashMap<>();
                                queryMap.put("imgid", imgid);
                                queryMap.put("id", imagefield);
                                client.editImageSingle(code, queryMap)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SingleObserver<JsonNode>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {

                                            }

                                            @Override
                                            public void onSuccess(JsonNode jsonNode) {
                                                dialog.dismiss();
                                                checkNutritionFactsImageUploadStatus();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                dialog.dismiss();
                                                if (!edit_product) {
                                                    saveProductOffline();
                                                } else {
                                                    MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                                                            .title(R.string.device_offline_dialog_title)
                                                            .positiveText(R.string.txt_try_again)
                                                            .negativeText(R.string.dialog_cancel)
                                                            .onPositive((dialog, which) -> checkFrontImageUploadStatus())
                                                            .onNegative((dialog, which) -> dialog.dismiss());
                                                    dialog = builder.build();
                                                    dialog.show();
                                                }
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            dialog.dismiss();
                            if (!edit_product) {
                                saveProductOffline();
                            } else {
                                MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                                        .title(R.string.device_offline_dialog_title)
                                        .positiveText(R.string.txt_try_again)
                                        .negativeText(R.string.dialog_cancel)
                                        .onPositive((dialog, which) -> checkFrontImageUploadStatus())
                                        .onNegative((dialog, which) -> dialog.dismiss());
                                dialog = builder.build();
                                dialog.show();
                            }
                        }
                    });
        } else {
            // ingredients image is uploaded, check the status of nutrition facts image.
            checkNutritionFactsImageUploadStatus();
        }
    }

    /**
     * Upload and set the nutrition facts image if it is not uploaded already.
     */
    private void checkNutritionFactsImageUploadStatus() {
        String code = productDetails.get("code");
        if (!image_nutrition_facts_uploaded && imagesFilePath[2] != null && !imagesFilePath[2].isEmpty()) {
            // nutrition facts image is not yet uploaded.
            File photoFile = new File(imagesFilePath[2]);
            Map<String, RequestBody> imgMap = new HashMap<>();
            RequestBody barcode = RequestBody.create(MediaType.parse("text/plain"), code);
            RequestBody imageField = RequestBody.create(MediaType.parse("text/plain"), ProductImageField.NUTRITION.toString() + '_' + getProductLanguage());
            RequestBody image = RequestBody.create(MediaType.parse("image/*"), photoFile);
            imgMap.put("code", barcode);
            imgMap.put("imagefield", imageField);
            imgMap.put("imgupload_nutrition\"; filename=\"nutrition_" + getProductLanguage() + ".png\"", image);

            // Attribute the upload to the connected user
            final SharedPreferences settings = getSharedPreferences("login", 0);
            final String login = settings.getString("user", "");
            final String password = settings.getString("pass", "");
            if (!login.isEmpty() && !password.isEmpty()) {
                imgMap.put("user_id", RequestBody.create(MediaType.parse("text/plain"), login));
                imgMap.put("password", RequestBody.create(MediaType.parse("text/plain"), password));
            }

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
                            dialog = builder.build();
                            dialog.show();
                        }

                        @Override
                        public void onSuccess(JsonNode jsonNode) {
                            String status = jsonNode.get("status").asText();
                            if (status.equals("status not ok")) {
                                dialog.dismiss();
                                String error = jsonNode.get("error").asText();
                                if (error.equals("This picture has already been sent.")) {
                                    image_nutrition_facts_uploaded = true;
                                    addProductToServer();
                                }
                            } else {
                                image_nutrition_facts_uploaded = true;
                                String imagefield = jsonNode.get("imagefield").asText();
                                String imgid = jsonNode.get("image").get("imgid").asText();
                                Map<String, String> queryMap = new HashMap<>();
                                queryMap.put("imgid", imgid);
                                queryMap.put("id", imagefield);
                                client.editImageSingle(code, queryMap)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SingleObserver<JsonNode>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {

                                            }

                                            @Override
                                            public void onSuccess(JsonNode jsonNode) {
                                                dialog.dismiss();
                                                addProductToServer();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                dialog.dismiss();
                                                if (!edit_product) {
                                                    saveProductOffline();
                                                } else {
                                                    MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                                                            .title(R.string.device_offline_dialog_title)
                                                            .positiveText(R.string.txt_try_again)
                                                            .negativeText(R.string.dialog_cancel)
                                                            .onPositive((dialog, which) -> checkFrontImageUploadStatus())
                                                            .onNegative((dialog, which) -> dialog.dismiss());
                                                    dialog = builder.build();
                                                    dialog.show();
                                                }
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            dialog.dismiss();
                            if (!edit_product) {
                                saveProductOffline();
                            } else {
                                MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                                        .title(R.string.device_offline_dialog_title)
                                        .positiveText(R.string.txt_try_again)
                                        .negativeText(R.string.dialog_cancel)
                                        .onPositive((dialog, which) -> checkFrontImageUploadStatus())
                                        .onNegative((dialog, which) -> dialog.dismiss());
                                dialog = builder.build();
                                dialog.show();
                            }
                        }
                    });
        } else {
            // nutrition facts image is uploaded, upload the product to server.
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
        final SharedPreferences settings = getSharedPreferences("login", 0);
        final String login = settings.getString("user", "");

        String comment = PRODUCT_API_COMMENT + " " + Utils.getVersionName(this);
        if (login.isEmpty()) {
            comment += " ( Added by " + Installation.id(this) + " )";
        }

        client.saveProductSingle(code, productDetails, comment)
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
                        intent.putExtra("uploadedToServer", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialog.dismiss();
                        Log.e(AddProductActivity.class.getSimpleName(), e.getMessage());
                        // A network error happened
                        if (e instanceof IOException) {
                            if (!edit_product) {
                                saveProductOffline();
                            } else {
                                MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                                        .title(R.string.device_offline_dialog_title)
                                        .positiveText(R.string.txt_try_again)
                                        .negativeText(R.string.dialog_cancel)
                                        .onPositive((dialog, which) -> checkFrontImageUploadStatus())
                                        .onNegative((dialog, which) -> dialog.dismiss());
                                dialog = builder.build();
                                dialog.show();
                            }
                        }
                        // Not a network error
                        else {
                            if (!edit_product) {
                                Toast.makeText(AddProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                saveProductOffline();
                            } else {
                                MaterialDialog.Builder builder = new MaterialDialog.Builder(AddProductActivity.this)
                                        .title(R.string.error_adding_product)
                                        .positiveText(R.string.txt_try_again)
                                        .negativeText(R.string.dialog_cancel)
                                        .onPositive((dialog, which) -> checkFrontImageUploadStatus())
                                        .onNegative((dialog, which) -> dialog.dismiss());
                                dialog = builder.build();
                                dialog.show();
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
        if (image_front_uploaded) {
            productDetails.put("image_front_uploaded", "true");
        }
        if (image_ingredients_uploaded) {
            productDetails.put("image_ingredients_uploaded", "true");
        }
        if (image_nutrition_facts_uploaded) {
            productDetails.put("image_nutrition_facts_uploaded", "true");
        }
        OfflineSavedProduct offlineSavedProduct = new OfflineSavedProduct();
        offlineSavedProduct.setBarcode(productDetails.get("code"));
        offlineSavedProduct.setProductDetailsMap(productDetails);
        mOfflineSavedProductDao.insertOrReplace(offlineSavedProduct);
        Toast.makeText(OFFApplication.getInstance(), R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.putExtra("uploadedToServer", false);
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
        if (!edit_product) {
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
        addProductOverviewFragment.getAllDetails();
        addProductIngredientsFragment.getAllDetails();
        if (isNutritionDataAvailable()) {
            addProductNutritionFactsFragment.getAllDetails();
        }
        final SharedPreferences settings = getSharedPreferences("login", 0);
        final String login = settings.getString("user", "");
        final String password = settings.getString("pass", "");
        if (!login.isEmpty() && !password.isEmpty()) {
            productDetails.put("user_id", login);
            productDetails.put("password", password);
        }
        checkFrontImageUploadStatus();

        if(productDetails.get("eating").equals("true")){
            addProductToList(1L,getString(R.string.txt_eaten_products));

        }
    }

    private void addProductToList(Long listId,String listName){
        String barcode=productDetails.get("code");
        String languageCode=productDetails.get("lang");
        String lc = (!languageCode.isEmpty()) ? languageCode : "en";
        String productName=productDetails.get("product_name"+"_"+lc);
        StringBuilder stringBuilder = new StringBuilder();
        if (isNotEmpty(productDetails.get("brands"))) {
            stringBuilder.append(capitalize(productDetails.get("brands").split(",")[0].trim()));
        }
        if (isNotEmpty(productDetails.get("quantity"))) {
            stringBuilder.append(" - ").append(productDetails.get("quantity"));
        }
        String productDetailsString=stringBuilder.toString();
        String imageUrl=productDetails.get("imageUrl");
        YourListedProductDao yourListedProductsDao=Utils.getAppDaoSession(this).getYourListedProductDao();
        YourListedProduct product=new YourListedProduct();
        product.setBarcode(barcode);
        product.setListId(listId);
        product.setListName(listName);
        product.setProductName(productName);
        product.setProductDetails(productDetailsString);
        product.setImageUrl(imageUrl);
        yourListedProductsDao.insertOrReplace(product);
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
                                performOCR(image.getBarcode(), "ingredients_" + getProductLanguage());
                            } else {
                                hideImageProgress(position, true, error);
                            }
                        } else {
                            if (image.getImageField() == ProductImageField.FRONT) {
                                image_front_uploaded = true;
                            } else if (image.getImageField() == ProductImageField.INGREDIENTS) {
                                image_ingredients_uploaded = true;
                            } else if (image.getImageField() == ProductImageField.NUTRITION) {
                                image_nutrition_facts_uploaded = true;
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
                            Log.e(AddProductActivity.class.getSimpleName(), e.getMessage());
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
                        if (status.equals("status ok")) {
                            if (ocr) {
                                performOCR(image.getBarcode(), imagefield);
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

    public void setIngredients(String status, String ingredients) {
        addProductIngredientsFragment.setIngredients(status, ingredients);
    }
}
