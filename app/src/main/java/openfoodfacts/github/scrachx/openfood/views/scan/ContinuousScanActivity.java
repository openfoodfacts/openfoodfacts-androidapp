/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.views.scan;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import com.mikepenz.iconics.IconicsColor;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.IconicsSize;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.AppFlavors;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityContinuousScanBinding;
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcode;
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcodeDao;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductState;
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProductDao;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenHelper;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig;
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent;
import openfoodfacts.github.scrachx.openfood.network.ApiFields;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.OfflineProductService;
import openfoodfacts.github.scrachx.openfood.utils.ProductUtils;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.ImagesManagementActivity;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.ProductComparisonActivity;
import openfoodfacts.github.scrachx.openfood.views.listeners.CommonBottomListenerInstaller;
import openfoodfacts.github.scrachx.openfood.views.product.ProductActivity;
import openfoodfacts.github.scrachx.openfood.views.product.ProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis.IngredientsWithTagDialogFragment;
import openfoodfacts.github.scrachx.openfood.views.product.summary.IngredientAnalysisTagsAdapter;
import openfoodfacts.github.scrachx.openfood.views.product.summary.SummaryProductPresenter;
import openfoodfacts.github.scrachx.openfood.views.product.summary.SummaryProductPresenterView;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT;

public class ContinuousScanActivity extends AppCompatActivity {
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 2;
    public static final List<BarcodeFormat> BARCODE_FORMATS = Arrays.asList(
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.RSS_14,
        BarcodeFormat.CODE_39,
        BarcodeFormat.CODE_93,
        BarcodeFormat.CODE_128,
        BarcodeFormat.ITF
    );
    public static final String INTENT_KEY_COMPARE = "compare_product";
    public static final String INTENT_KEY_PRODUCTS_TO_COMPARE = "products_to_compare";
    public static final String SETTING_RING = "ring";
    public static final String SETTING_FLASH = "flash";
    public static final String SETTING_FOCUS = "focus";
    public static final String LOG_TAG = ContinuousScanActivity.class.getSimpleName();
    private BeepManager beepManager;
    private ActivityContinuousScanBinding binding;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private final TextView.OnEditorActionListener barcodeInputListener = new BarcodeInputListener();
    private final BarcodeCallback barcodeScanCallback = new BarcodeScannerCallback();
    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;
    private int cameraState;
    private OpenFoodAPIClient client;
    @NonNull
    private VectorDrawableCompat errorDrawable;
    private Disposable productDisp;
    private boolean isAnalysisTagsEmpty = true;
    private String lastBarcode;
    private boolean autoFocusActive;
    private boolean beepActive;
    private InvalidBarcodeDao mInvalidBarcodeDao;
    private OfflineSavedProductDao mOfflineSavedProductDao;
    private OfflineSavedProduct offlineSavedProduct;
    private Product product;
    private ProductFragment productFragment;
    private SharedPreferences cameraPref;
    private int peekLarge;
    private int peekSmall;
    private PopupMenu popupMenu;
    private boolean productShowing = false;
    private boolean flashActive;
    private SummaryProductPresenter summaryProductPresenter;
    private Disposable hintBarcodeDisp;
    private CompositeDisposable commonDisp;

    /**
     * Used by screenshot tests.
     *
     * @param barcode barcode to serach
     */
    @SuppressWarnings("unused")
    public void showProduct(String barcode) {
        productShowing = true;
        binding.barcodeScanner.setVisibility(GONE);
        binding.barcodeScanner.pause();
        binding.imageForScreenshotGenerationOnly.setVisibility(VISIBLE);
        setShownProduct(barcode);
    }

    /**
     * Makes network call and search for the product in the database
     *
     * @param barcode Barcode to be searched
     */
    private void setShownProduct(String barcode) {
        if (isFinishing()) {
            return;
        }
        // Dispose the previous call if not ended.
        if (productDisp != null && !productDisp.isDisposed()) {
            productDisp.dispose();
        }
        if (summaryProductPresenter != null) {
            summaryProductPresenter.dispose();
        }

        // First, try to show if we have an offline saved product in the db
        offlineSavedProduct = OfflineProductService.getOfflineProductByBarcode(barcode);
        if (offlineSavedProduct != null) {
            showOfflineSavedDetails(offlineSavedProduct);
        }

        // Then query the online db
        productDisp = client.getProductStateFull(barcode, Utils.HEADER_USER_AGENT_SCAN)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(disposable -> {
                hideAllViews();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                binding.quickView.setOnClickListener(null);
                binding.quickViewProgress.setVisibility(VISIBLE);
                binding.quickViewProgressText.setVisibility(VISIBLE);
                binding.quickViewProgressText.setText(getString(R.string.loading_product, barcode));
            })
            .subscribe((ProductState productState) -> {
                //clear product tags
                isAnalysisTagsEmpty = true;
                binding.quickViewTags.setAdapter(null);

                binding.quickViewProgress.setVisibility(GONE);
                binding.quickViewProgressText.setVisibility(GONE);
                if (productState.getStatus() == 0) {
                    tryDisplayOffline(offlineSavedProduct, barcode, R.string.product_not_found);
                } else {
                    product = productState.getProduct();
                    if (getIntent().getBooleanExtra(INTENT_KEY_COMPARE, false)) {
                        Intent intent = new Intent(ContinuousScanActivity.this, ProductComparisonActivity.class);
                        intent.putExtra("product_found", true);
                        ArrayList<Product> productsToCompare = (ArrayList<Product>) getIntent().getExtras().getSerializable(INTENT_KEY_PRODUCTS_TO_COMPARE);
                        if (productsToCompare.contains(product)) {
                            intent.putExtra("product_already_exists", true);
                        } else {
                            productsToCompare.add(product);
                        }
                        intent.putExtra(INTENT_KEY_PRODUCTS_TO_COMPARE, productsToCompare);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }

                    productDisp = client.addToHistory(product).subscribeOn(Schedulers.io()).subscribe();

                    showAllViews();
                    binding.txtProductCallToAction.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    binding.txtProductCallToAction.setBackground(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.rounded_quick_view_text));
                    binding.txtProductCallToAction.setText(isProductIncomplete() ? R.string.product_not_complete : R.string.scan_tooltip);
                    binding.txtProductCallToAction.setVisibility(VISIBLE);

                    setupSummary(product);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showProductFullScreen();
                    binding.quickViewProductNotFound.setVisibility(GONE);
                    binding.quickViewProductNotFoundButton.setVisibility(GONE);

                    if (offlineSavedProduct != null && !TextUtils.isEmpty(offlineSavedProduct.getName())) {
                        binding.quickViewName.setText(offlineSavedProduct.getName());
                    } else if (product.getProductName() == null || product.getProductName().equals("")) {
                        binding.quickViewName.setText(R.string.productNameNull);
                    } else {
                        binding.quickViewName.setText(product.getProductName());
                    }
                    List<String> addTags = product.getAdditivesTags();
                    if (!addTags.isEmpty()) {
                        binding.quickViewAdditives.setText(getResources().getQuantityString(R.plurals.productAdditives, addTags.size(), addTags.size()));
                    } else if (product.getStatesTags().contains("en:ingredients-completed")) {
                        binding.quickViewAdditives.setText(getString(R.string.productAdditivesNone));
                    } else {
                        binding.quickViewAdditives.setText(getString(R.string.productAdditivesUnknown));
                    }

                    final String imageUrl = Utils.firstNotEmpty(offlineSavedProduct != null ? offlineSavedProduct.getImageFrontLocalUrl() : null,
                        product.getImageUrl(LocaleHelper.getLanguage(getBaseContext())));
                    if (imageUrl != null) {
                        try {
                            Picasso.get()
                                .load(imageUrl)
                                .error(errorDrawable)
                                .into(binding.quickViewImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        if (binding != null) {
                                            binding.quickViewImageProgress.setVisibility(GONE);
                                        }
                                    }

                                    @Override
                                    public void onError(Exception ex) {
                                        if (binding != null) {
                                            binding.quickViewImageProgress.setVisibility(GONE);
                                        }
                                    }
                                });
                        } catch (IllegalStateException e) {
                            //could happen if Picasso is not instantiated correctly...
                            Log.w(LOG_TAG, e.getMessage(), e);
                        }
                    } else {
                        binding.quickViewImage.setImageDrawable(errorDrawable);
                        binding.quickViewImageProgress.setVisibility(GONE);
                    }
                    // Hide nutriScore from quickView if app flavour is not OFF or there is no nutriscore
                    if (AppFlavors.isFlavors(AppFlavors.OFF) && product.getNutritionGradeTag() != null) {
                        if (Utils.getImageGrade(product.getNutritionGradeTag()) != Utils.NO_DRAWABLE_RESOURCE) {
                            binding.quickViewNutriScore.setVisibility(VISIBLE);
                            binding.quickViewNutriScore.setImageResource(Utils.getImageGrade(product.getNutritionGradeFr()));
                        } else {
                            binding.quickViewNutriScore.setVisibility(INVISIBLE);
                        }
                    } else {
                        binding.quickViewNutriScore.setVisibility(GONE);
                    }
                    // Hide nova group from quickView if app flavour is not OFF or there is no nova group
                    if (AppFlavors.isFlavors(AppFlavors.OFF) && product.getNovaGroups() != null) {
                        final int novaGroupDrawable = Utils.getNovaGroupDrawable(product);
                        if (novaGroupDrawable != Utils.NO_DRAWABLE_RESOURCE) {
                            binding.quickViewNovaGroup.setVisibility(VISIBLE);
                            binding.quickViewAdditives.setVisibility(VISIBLE);
                            binding.quickViewNovaGroup.setImageResource(novaGroupDrawable);
                        } else {
                            binding.quickViewNovaGroup.setVisibility(INVISIBLE);
                        }
                    } else {
                        binding.quickViewNovaGroup.setVisibility(GONE);
                    }
                    int environmentImpactResource = Utils.getImageEnvironmentImpact(product);
                    if (environmentImpactResource != Utils.NO_DRAWABLE_RESOURCE) {
                        binding.quickViewCo2Icon.setVisibility(VISIBLE);
                        binding.quickViewCo2Icon.setImageResource(environmentImpactResource);
                    } else {
                        binding.quickViewCo2Icon.setVisibility(INVISIBLE);
                    }
                    ProductFragment newProductFragment = ProductFragment.newInstance(productState);

                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, newProductFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                    productFragment = newProductFragment;
                }
            }, (Throwable e) -> {
                try {
                    // A network error happened
                    if (e instanceof IOException) {
                        hideAllViews();
                        OfflineSavedProduct offlineSavedProduct = mOfflineSavedProductDao.queryBuilder()
                            .where(OfflineSavedProductDao.Properties.Barcode.eq(barcode))
                            .unique();
                        tryDisplayOffline(offlineSavedProduct, barcode, R.string.addProductOffline);
                        binding.quickView.setOnClickListener(v -> navigateToProductAddition(barcode));
                    } else {
                        binding.quickViewProgress.setVisibility(GONE);
                        binding.quickViewProgressText.setVisibility(GONE);
                        final Toast errorMessage = Toast.makeText(this, R.string.txtConnectionError, Toast.LENGTH_LONG);
                        errorMessage.setGravity(Gravity.CENTER, 0, 0);
                        errorMessage.show();
                        Log.i(LOG_TAG, e.getMessage(), e);
                    }
                } catch (Exception err) {
                    Log.w(LOG_TAG, err.getMessage(), err);
                }
            });
    }

    private void tryDisplayOffline(@Nullable OfflineSavedProduct offlineSavedProduct, @NonNull String barcode, @StringRes int errorMsg) {
        if (offlineSavedProduct != null) {
            showOfflineSavedDetails(offlineSavedProduct);
        } else {
            showProductNotFound(getString(errorMsg, barcode));
        }
    }

    private void setupSummary(Product product) {
        binding.callToActionImageProgress.setVisibility(VISIBLE);
        summaryProductPresenter = new SummaryProductPresenter(product, new SummaryProductPresenterView() {
            @Override
            public void showAllergens(List<AllergenName> allergens) {
                final AllergenHelper.Data data = AllergenHelper.computeUserAllergen(product, allergens);
                binding.callToActionImageProgress.setVisibility(GONE);
                if (data.isEmpty()) {
                    return;
                }
                final IconicsDrawable iconicsDrawable = new IconicsDrawable(ContinuousScanActivity.this, GoogleMaterial.Icon.gmd_warning)
                    .color(IconicsColor.colorInt(ContextCompat.getColor(ContinuousScanActivity.this, R.color.white)))
                    .size(IconicsSize.dp(24));
                binding.txtProductCallToAction.setCompoundDrawablesWithIntrinsicBounds(iconicsDrawable, null, null, null);
                binding.txtProductCallToAction.setBackground(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.rounded_quick_view_text_warn));
                if (data.isIncomplete()) {
                    binding.txtProductCallToAction.setText(R.string.product_incomplete_message);
                } else {
                    String text = String.format("%s\n", getResources().getString(R.string.product_allergen_prompt)) +
                        StringUtils.join(data.getAllergens(), ", ");
                    binding.txtProductCallToAction.setText(text);
                }
            }

            @Override
            public void showAnalysisTags(List<AnalysisTagConfig> analysisTags) {
                super.showAnalysisTags(analysisTags);

                if (analysisTags.isEmpty()) {
                    binding.quickViewTags.setVisibility(GONE);
                    isAnalysisTagsEmpty = true;
                    return;
                }

                binding.quickViewTags.setVisibility(VISIBLE);
                isAnalysisTagsEmpty = false;

                IngredientAnalysisTagsAdapter adapter = new IngredientAnalysisTagsAdapter(ContinuousScanActivity.this, analysisTags);
                adapter.setOnItemClickListener((view, position) -> {
                    IngredientsWithTagDialogFragment fragment = IngredientsWithTagDialogFragment
                        .newInstance(product, (AnalysisTagConfig) view.getTag(R.id.analysis_tag_config));
                    fragment.show(getSupportFragmentManager(), "fragment_ingredients_with_tag");

                    fragment.setOnDismissListener(dialog -> adapter.filterVisibleTags());
                });
                binding.quickViewTags.setAdapter(adapter);
            }
        });
        summaryProductPresenter.loadAllergens(() -> binding.callToActionImageProgress.setVisibility(GONE));
        summaryProductPresenter.loadAnalysisTags();
    }

    private void showProductNotFound(String text) {
        hideAllViews();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        binding.quickView.setOnClickListener(v -> navigateToProductAddition(lastBarcode));
        binding.quickViewProductNotFound.setText(text);
        binding.quickViewProductNotFound.setVisibility(VISIBLE);
        binding.quickViewProductNotFoundButton.setVisibility(VISIBLE);
        binding.quickViewProductNotFoundButton.setOnClickListener(v -> navigateToProductAddition(lastBarcode));
    }

    private void showProductFullScreen() {
        bottomSheetBehavior.setPeekHeight(peekLarge);
        binding.quickView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        binding.quickView.requestLayout();
        binding.quickView.getRootView().requestLayout();
    }

    private void showOfflineSavedDetails(@NonNull OfflineSavedProduct offlineSavedProduct) {
        showAllViews();
        String pName = offlineSavedProduct.getName();
        if (!TextUtils.isEmpty(pName)) {
            binding.quickViewName.setText(pName);
        } else {
            binding.quickViewName.setText(R.string.productNameNull);
        }

        String imageFront = offlineSavedProduct.getImageFrontLocalUrl();

        if (!TextUtils.isEmpty(imageFront)) {
            Picasso.get()
                .load(imageFront)
                .error(errorDrawable)
                .into(binding.quickViewImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        binding.quickViewImageProgress.setVisibility(GONE);
                    }

                    @Override
                    public void onError(Exception ex) {
                        binding.quickViewImageProgress.setVisibility(GONE);
                    }
                });
        } else {
            binding.quickViewImage.setImageDrawable(errorDrawable);
            binding.quickViewImageProgress.setVisibility(GONE);
        }

        binding.txtProductCallToAction.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        binding.txtProductCallToAction.setBackground(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.rounded_quick_view_text));
        binding.txtProductCallToAction.setText(R.string.product_not_complete);
        binding.txtProductCallToAction.setVisibility(VISIBLE);
        binding.quickViewSlideUpIndicator.setVisibility(GONE);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void navigateToProductAddition(String productBarcode) {
        Product pd = new Product();
        pd.setCode(productBarcode);
        navigateToProductAddition(pd);
    }

    private void navigateToProductAddition(Product product) {
        Intent intent = new Intent(ContinuousScanActivity.this, AddProductActivity.class);
        intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, product);
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                setShownProduct(lastBarcode);
            }
        }).launch(intent);
    }

    private void showAllViews() {
        binding.quickViewSlideUpIndicator.setVisibility(VISIBLE);
        binding.quickViewImage.setVisibility(VISIBLE);
        binding.quickViewName.setVisibility(VISIBLE);
        binding.frameLayout.setVisibility(VISIBLE);
        binding.quickViewAdditives.setVisibility(VISIBLE);
        binding.quickViewImageProgress.setVisibility(VISIBLE);
        if (!isAnalysisTagsEmpty) {
            binding.quickViewTags.setVisibility(VISIBLE);
        } else {
            binding.quickViewTags.setVisibility(GONE);
        }
    }

    private void hideAllViews() {
        binding.quickViewSearchByBarcode.setVisibility(GONE);
        binding.quickViewProgress.setVisibility(GONE);
        binding.quickViewProgressText.setVisibility(GONE);
        binding.quickViewSlideUpIndicator.setVisibility(GONE);
        binding.quickViewImage.setVisibility(GONE);
        binding.quickViewName.setVisibility(GONE);
        binding.frameLayout.setVisibility(GONE);
        binding.quickViewAdditives.setVisibility(GONE);
        binding.quickViewNutriScore.setVisibility(GONE);
        binding.quickViewNovaGroup.setVisibility(GONE);
        binding.quickViewCo2Icon.setVisibility(GONE);
        binding.quickViewProductNotFound.setVisibility(GONE);
        binding.quickViewProductNotFoundButton.setVisibility(GONE);
        binding.quickViewImageProgress.setVisibility(GONE);
        binding.txtProductCallToAction.setVisibility(GONE);
        binding.quickViewTags.setVisibility(GONE);
    }

    @Override
    protected void onDestroy() {
        if (summaryProductPresenter != null) {
            summaryProductPresenter.dispose();
        }

        // Dispose all RxJava disposable
        if (productDisp != null) {
            productDisp.dispose();
        }
        if (hintBarcodeDisp != null) {
            hintBarcodeDisp.dispose();
        }
        commonDisp.dispose();

        // Remove bottom sheet callback as it uses binding
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback);

        binding = null;
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onPause() {
        binding.barcodeScanner.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CommonBottomListenerInstaller.selectNavigationItem(binding.bottomNavigation.bottomNavigation, R.id.scan_bottom_nav);
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            binding.barcodeScanner.resume();
        }
    }

    @Subscribe
    public void onEventBusProductNeedsRefreshEvent(@NonNull ProductNeedsRefreshEvent event) {
        if (event.getBarcode().equals(lastBarcode)) {
            setShownProduct(lastBarcode);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //status bar will remain visible if user presses home and then reopens the activity
        // hence hiding status bar again
        hideSystemUI();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        OFFApplication.getAppComponent().inject(this);
        client = new OpenFoodAPIClient(this);
        commonDisp = new CompositeDisposable();
        super.onCreate(savedInstanceState);

        binding = ActivityContinuousScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toggleFlash.setOnClickListener(v -> toggleFlash());
        binding.buttonMore.setOnClickListener(v -> showMoreSettings());

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        peekLarge = getResources().getDimensionPixelSize(R.dimen.scan_summary_peek_large);
        peekSmall = getResources().getDimensionPixelSize(R.dimen.scan_summary_peek_small);
        errorDrawable = Objects.requireNonNull(VectorDrawableCompat.create(getResources(), R.drawable.ic_product_silhouette, null));

        binding.quickViewTags.setNestedScrollingEnabled(false);

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
            (visibility -> {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // The system bars are visible.
                    hideSystemUI();
                }
            });

        hintBarcodeDisp = Completable.timer(15, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete(() -> {
                if (productShowing) {
                    return;
                }
                hideAllViews();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                binding.quickViewSearchByBarcode.setVisibility(VISIBLE);
                binding.quickViewSearchByBarcode.requestFocus();
            }).subscribe();

        bottomSheetBehavior = BottomSheetBehavior.from(binding.quickView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetCallback = new QuickViewCallback();
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);

        mInvalidBarcodeDao = Utils.getDaoSession().getInvalidBarcodeDao();
        mOfflineSavedProductDao = Utils.getDaoSession().getOfflineSavedProductDao();

        cameraPref = getSharedPreferences("camera", 0);
        beepActive = cameraPref.getBoolean(SETTING_RING, false);
        flashActive = cameraPref.getBoolean(SETTING_FLASH, false);
        autoFocusActive = cameraPref.getBoolean(SETTING_FOCUS, true);
        cameraState = cameraPref.getInt("cameraState", 0);

        // Setup barcode scanner
        binding.barcodeScanner.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(BARCODE_FORMATS));
        binding.barcodeScanner.setStatusText(null);
        CameraSettings settings = binding.barcodeScanner.getBarcodeView().getCameraSettings();
        settings.setRequestedCameraId(cameraState);
        settings.setAutoFocusEnabled(autoFocusActive);

        // Setup popup menu
        setupPopupMenu();

        // Start continuous scanner
        binding.barcodeScanner.decodeContinuous(barcodeScanCallback);
        beepManager = new BeepManager(this);

        binding.quickViewSearchByBarcode.setOnEditorActionListener(barcodeInputListener);

        CommonBottomListenerInstaller.install(this, binding.bottomNavigation.bottomNavigation);
    }

    private void setupPopupMenu() {
        popupMenu = new PopupMenu(this, binding.buttonMore);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        if (flashActive) {
            binding.barcodeScanner.setTorchOn();
            binding.toggleFlash.setImageResource(R.drawable.ic_flash_on_white_24dp);
        }
        if (beepActive) {
            popupMenu.getMenu().findItem(R.id.toggleBeep).setChecked(true);
        }
        if (autoFocusActive) {
            popupMenu.getMenu().findItem(R.id.toggleAutofocus).setChecked(true);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onCreate(newBase));
    }

    private boolean isProductIncomplete() {
        if (product == null) {
            return false;
        }
        return product.getImageFrontUrl() == null
            || product.getImageFrontUrl().equals("")
            || product.getQuantity() == null
            || product.getQuantity().equals("")
            || product.getProductName() == null
            || product.getProductName().equals("")
            || product.getBrands() == null
            || product.getBrands().equals("")
            || product.getIngredientsText() == null
            || product.getIngredientsText().equals("");
    }

    private void toggleCamera() {
        SharedPreferences.Editor editor = cameraPref.edit();
        CameraSettings settings = binding.barcodeScanner.getBarcodeView().getCameraSettings();
        if (binding.barcodeScanner.getBarcodeView().isPreviewActive()) {
            binding.barcodeScanner.pause();
        }
        if (settings.getRequestedCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraState = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            cameraState = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        settings.setRequestedCameraId(cameraState);
        binding.barcodeScanner.getBarcodeView().setCameraSettings(settings);
        editor.putInt("cameraState", cameraState);
        editor.apply();
        binding.barcodeScanner.resume();
    }

    private void toggleFlash() {
        SharedPreferences.Editor editor = cameraPref.edit();
        if (flashActive) {
            binding.barcodeScanner.setTorchOff();
            flashActive = false;
            binding.toggleFlash.setImageResource(R.drawable.ic_flash_off_white_24dp);
            editor.putBoolean(SETTING_FLASH, false);
        } else {
            binding.barcodeScanner.setTorchOn();
            flashActive = true;
            binding.toggleFlash.setImageResource(R.drawable.ic_flash_on_white_24dp);
            editor.putBoolean(SETTING_FLASH, true);
        }
        editor.apply();
    }

    private void showMoreSettings() {
        popupMenu.setOnMenuItemClickListener(item -> {
            SharedPreferences.Editor editor;
            switch (item.getItemId()) {
                case R.id.toggleBeep:
                    editor = cameraPref.edit();

                    beepActive = !beepActive;
                    item.setChecked(beepActive);
                    editor.putBoolean(SETTING_RING, beepActive);

                    editor.apply();
                    break;
                case R.id.toggleAutofocus:
                    if (binding.barcodeScanner.getBarcodeView().isPreviewActive()) {
                        binding.barcodeScanner.pause();
                    }
                    editor = cameraPref.edit();
                    CameraSettings settings = binding.barcodeScanner.getBarcodeView().getCameraSettings();

                    autoFocusActive = !autoFocusActive;
                    settings.setAutoFocusEnabled(autoFocusActive);
                    item.setChecked(autoFocusActive);
                    editor.putBoolean(SETTING_FOCUS, autoFocusActive);

                    binding.barcodeScanner.getBarcodeView().setCameraSettings(settings);
                    binding.barcodeScanner.resume();
                    editor.apply();
                    break;
                case R.id.troubleScanning:
                    hideAllViews();
                    if (hintBarcodeDisp != null) {
                        hintBarcodeDisp.dispose();
                    }
                    binding.quickView.setOnClickListener(null);
                    binding.quickViewSearchByBarcode.setText(null);
                    binding.quickViewSearchByBarcode.setVisibility(VISIBLE);
                    binding.quickView.setVisibility(INVISIBLE);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    commonDisp.add(Completable.timer(500, TimeUnit.MILLISECONDS)
                        .doOnComplete(() -> binding.quickView.setVisibility(VISIBLE))
                        .subscribeOn(AndroidSchedulers.mainThread()).subscribe());

                    binding.quickViewSearchByBarcode.requestFocus();
                    break;
                case R.id.toggleCamera:
                    toggleCamera();
                    break;
                default:
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    /**
     * Overridden to collapse bottom view after a back action from edit form.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagesManagementActivity.REQUEST_EDIT_IMAGE && (resultCode == RESULT_OK || resultCode == RESULT_CANCELED)) {
            setShownProduct(lastBarcode);
        } else if (resultCode == RESULT_OK && requestCode == LOGIN_ACTIVITY_REQUEST_CODE) {
            navigateToProductAddition(product);
        }
    }

    public void collapseBottomSheet() {
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    public void showIngredientsTab(ProductActivity.ShowIngredientsAction action) {
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        productFragment.showIngredientsTab(action);
    }

    private class QuickViewCallback extends BottomSheetBehavior.BottomSheetCallback {
        private float previousSlideOffset = 0;

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehavior.STATE_HIDDEN:
                    lastBarcode = null;
                    binding.txtProductCallToAction.setVisibility(GONE);
                    break;
                case BottomSheetBehavior.STATE_COLLAPSED:
                    binding.barcodeScanner.resume();
                    break;
                case BottomSheetBehavior.STATE_DRAGGING:
                    if (product == null) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                    break;
            }
            if (binding.quickViewSearchByBarcode.getVisibility() == VISIBLE) {
                bottomSheetBehavior.setPeekHeight(peekSmall);
                bottomSheet.getLayoutParams().height = bottomSheetBehavior.getPeekHeight();
            } else {
                bottomSheetBehavior.setPeekHeight(peekLarge);
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            bottomSheet.requestLayout();
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            float slideDelta = slideOffset - previousSlideOffset;
            if (binding.quickViewSearchByBarcode.getVisibility() != VISIBLE && binding.quickViewProgress.getVisibility() != VISIBLE) {
                if (slideOffset > 0.01f || slideOffset < -0.01f) {
                    binding.txtProductCallToAction.setVisibility(GONE);
                } else {
                    if (binding.quickViewProductNotFound.getVisibility() != VISIBLE) {
                        binding.txtProductCallToAction.setVisibility(VISIBLE);
                    }
                }
                if (slideOffset > 0.01f) {
                    binding.quickViewDetails.setVisibility(GONE);
                    binding.quickViewTags.setVisibility(GONE);
                    binding.barcodeScanner.pause();
                    if (slideDelta > 0 && productFragment != null) {
                        productFragment.bottomSheetWillGrow();
                        binding.bottomNavigation.bottomNavigation.setVisibility(GONE);
                    }
                } else {
                    binding.barcodeScanner.resume();
                    binding.quickViewDetails.setVisibility(VISIBLE);
                    if (!isAnalysisTagsEmpty) {
                        binding.quickViewTags.setVisibility(VISIBLE);
                    } else {
                        binding.quickViewTags.setVisibility(GONE);
                    }
                    binding.bottomNavigation.bottomNavigation.setVisibility(VISIBLE);
                    if (binding.quickViewProductNotFound.getVisibility() != VISIBLE) {
                        binding.txtProductCallToAction.setVisibility(VISIBLE);
                    }
                }
            }
            previousSlideOffset = slideOffset;
        }
    }

    private class BarcodeInputListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            // When user search from "having trouble" edit text
            if (actionId != EditorInfo.IME_ACTION_SEARCH) {
                return false;
            }

            Utils.hideKeyboard(ContinuousScanActivity.this);

            ContinuousScanActivity.this.hideSystemUI();

            // Check for barcode validity
            if (!textView.getText().toString().isEmpty()) {
                String barcodeText = textView.getText().toString();

                // For debug only: the barcode 1 is used for test
                if (((barcodeText.length() > 2) || ApiFields.Defaults.DEBUG_BARCODE.equals(barcodeText))
                    && ProductUtils.isBarcodeValid(barcodeText)) {

                    lastBarcode = barcodeText;
                    textView.setVisibility(GONE);
                    ContinuousScanActivity.this.setShownProduct(barcodeText);
                    return true;
                }
            }
            textView.requestFocus();
            Snackbar.make(binding.getRoot(), ContinuousScanActivity.this.getString(R.string.txtBarcodeNotValid), LENGTH_SHORT).show();
            return true;
        }
    }

    private class BarcodeScannerCallback implements BarcodeCallback {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (hintBarcodeDisp != null) {
                hintBarcodeDisp.dispose();
            }
            if (result.getText() == null || result.getText().isEmpty() || result.getText().equals(lastBarcode)) {
                // Prevent duplicate scans
                return;
            }
            InvalidBarcode invalidBarcode = mInvalidBarcodeDao.queryBuilder()
                .where(InvalidBarcodeDao.Properties.Barcode.eq(result.getText())).unique();
            if (invalidBarcode != null) {
                // scanned barcode is in the list of invalid barcodes, do nothing
                return;
            }

            if (beepActive) {
                beepManager.playBeepSound();
            }

            lastBarcode = result.getText();
            if (!(isFinishing())) {
                setShownProduct(lastBarcode);
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            // Here possible results are useless but we must implement this
        }
    }
}
