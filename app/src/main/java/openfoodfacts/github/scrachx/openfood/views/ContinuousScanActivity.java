package openfoodfacts.github.scrachx.openfood.views;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.*;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.SwipeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;
import openfoodfacts.github.scrachx.openfood.views.product.ProductFragment;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

public class ContinuousScanActivity extends android.support.v7.app.AppCompatActivity {
    private static final int ADD_PRODUCT_ACTIVITY_REQUEST_CODE = 1;
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 2;
    private static HistoryProductDao mHistoryProductDao;
    private static final int PEEK_SMALL = 120;
    private static final int PEEK_LARGE = 150;
    @BindView(R.id.fab_status)
    FloatingActionButton fab_status;
    @BindView(R.id.quick_view)
    ConstraintLayout quickView;
    @BindView(R.id.barcode_scanner)
    DecoratedBarcodeView barcodeView;
    @BindView(R.id.imageForScreenshotGenerationOnly)
    ImageView imageForScreenshotGenerationOnly;
    @BindView(R.id.toggle_flash)
    ImageView toggleFlash;
    @BindView(R.id.button_more)
    ImageView moreOptions;
    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;
    @BindView(R.id.txt_product_not_complete)
    TextView txtProductIncomplete;
    @BindView(R.id.quickView_slideUpIndicator)
    View slideUpIndicator;
    @BindView(R.id.quickView_progress)
    ProgressBar progressBar;
    @BindView(R.id.quickView_progressText)
    TextView progressText;
    @BindView(R.id.quickView_productNotFound)
    TextView productNotFound;
    @BindView(R.id.quickView_image)
    ImageView productImage;
    @BindView(R.id.quickView_brand)
    TextView brand;
    @BindView(R.id.quickView_name)
    TextView name;
    @BindView(R.id.quickView_quantity)
    TextView quantity;
    @BindView(R.id.quickView_nutriScore)
    ImageView nutriScore;
    @BindView(R.id.quickView_novaGroup)
    ImageView novaGroup;
    @BindView(R.id.quick_view_co2_icon)
    ImageView co2Icon;
    @BindView(R.id.quickView_imageProgress)
    ProgressBar imageProgress;
    @BindView(R.id.quickView_searchByBarcode)
    EditText searchByBarcode;
    @BindView(R.id.quickView_details)
    RelativeLayout details;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;
    @Inject
    OpenFoodAPIService client;
    private OfflineSavedProductDao mOfflineSavedProductDao;
    private Product product;
    private ProductFragment productFragment;
    private SharedPreferences.Editor editor;
    private BeepManager beepManager;
    private String lastText;
    private SharedPreferences sp;
    private boolean mFlash;
    private boolean mRing;
    private boolean mAutofocus;
    private int cameraState;
    private Disposable disposable;
    private PopupMenu popup;
    private Handler handler;
    private Runnable runnable;
    private BottomSheetBehavior bottomSheetBehavior;
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            handler.removeCallbacks(runnable);
            if (result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }
            if (mRing) {
                beepManager.playBeepSound();
            }

            lastText = result.getText();
            if (!(isFinishing() || isDestroyed())) {
                findProduct(lastText, false);
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {

        }
    };
    private boolean productShowing = false;

    /**
     * Used by screenshot tests.
     * @param text
     */
    public void showProduct(String text){
        productShowing=true;
        barcodeView.setVisibility(View.GONE);
        barcodeView.pause();
        imageForScreenshotGenerationOnly.setVisibility(View.VISIBLE);
        findProduct(text,false);
    }

    /**
     * Makes network call and search for the product in the database
     *
     * @param lastText Barcode to be searched
     * @param newlyAdded true if the product is added using the product addition just now
     */
    private void findProduct(String lastText, boolean newlyAdded) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        client.getFullProductByBarcodeSingle(lastText, Utils.getUserAgent(Utils.HEADER_USER_AGENT_SCAN))
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(a -> {
                hideAllViews();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                fab_status.setVisibility(View.GONE);
                quickView.setOnClickListener(null);
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
                progressText.setText(getString(R.string.loading_product, lastText));
            })
            .subscribe(new SingleObserver<State>() {
                @Override
                public void onSubscribe(Disposable d) {
                    disposable = d;
                }

                @Override
                public void onSuccess(State state) {
                    progressBar.setVisibility(View.GONE);
                    progressText.setVisibility(View.GONE);
                    if (state.getStatus() == 0) {
                        hideAllViews();
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        quickView.setOnClickListener(v -> navigateToProductAddition(lastText));
                        String s = getString(R.string.product_not_found, lastText);
                        productNotFound.setText(s);
                        productNotFound.setVisibility(View.VISIBLE);
                        fab_status.setVisibility(View.VISIBLE);
                        fab_status.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                        fab_status.setImageDrawable(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.plus));
                        fab_status.setOnClickListener(v -> navigateToProductAddition(lastText));
                    } else {
                        product = state.getProduct();
                        if (getIntent().getBooleanExtra("compare_product", false)) {
                            Intent intent = new Intent(ContinuousScanActivity.this, ProductComparisonActivity.class);
                            intent.putExtra("product_found", true);
                            ArrayList<Product> productsToCompare = (ArrayList<Product>) getIntent().getExtras().get("products_to_compare");
                            if (productsToCompare.contains(product)) {
                                intent.putExtra("product_already_exists", true);
                            } else {
                                productsToCompare.add(product);
                            }
                            intent.putExtra("products_to_compare", productsToCompare);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                        new HistoryTask().doInBackground(product);
                        showAllViews();
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        productNotFound.setVisibility(View.GONE);
                        if (newlyAdded) {
                            txtProductIncomplete.setVisibility(View.INVISIBLE);
                            fab_status.setImageDrawable(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.ic_thumb_up_white_24dp));
                            fab_status.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_500)));
                            fab_status.setOnClickListener(null);
                        } else if (isProductIncomplete()) {
                            txtProductIncomplete.setVisibility(View.VISIBLE);
                            fab_status.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                            fab_status.setImageDrawable(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.ic_mode_edit_black));
                            fab_status.setOnClickListener(v -> {
                                final SharedPreferences settings = getSharedPreferences("login", 0);
                                final String login = settings.getString("user", "");
                                if (login.isEmpty()) {
                                    new MaterialDialog.Builder(ContinuousScanActivity.this)
                                        .title(R.string.sign_in_to_edit)
                                        .positiveText(R.string.txtSignIn)
                                        .negativeText(R.string.dialog_cancel)
                                        .onPositive((dialog, which) -> {
                                            Intent intent = new Intent(ContinuousScanActivity.this, LoginActivity.class);
                                            startActivityForResult(intent, LOGIN_ACTIVITY_REQUEST_CODE);
                                            dialog.dismiss();
                                        })
                                        .onNegative((dialog, which) -> dialog.dismiss())
                                        .build().show();
                                } else {
                                    Intent intent = new Intent(ContinuousScanActivity.this, AddProductActivity.class);
                                    intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, product);
                                    startActivityForResult(intent, ADD_PRODUCT_ACTIVITY_REQUEST_CODE);
                                }
                            });
                        } else {
                            txtProductIncomplete.setVisibility(View.INVISIBLE);
                            fab_status.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                            fab_status.setImageDrawable(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.ic_check_white_24dp));
                            fab_status.setOnClickListener(null);
                        }
                        if (product.getProductName() == null || product.getProductName().equals("")) {
                            name.setText(R.string.productNameNull);
                        } else {
                            name.setText(product.getProductName());
                        }
                        if (product.getBrands() == null || product.getBrands().equals("")) {
                            brand.setText(R.string.productBrandNull);
                        } else {
                            brand.setText(product.getBrands());
                        }
                        if (product.getQuantity() == null || product.getQuantity().equals("")) {
                            quantity.setText(R.string.productQuantityNull);
                        } else {
                            quantity.setText(product.getQuantity());
                        }
                        final String imageUrl = product.getImageUrl(LocaleHelper.getLanguage(getBaseContext()));
                        if (imageUrl != null) {
                            Picasso.with(ContinuousScanActivity.this)
                                .load(imageUrl)
                                .error(R.drawable.placeholder_thumb)
                                .into(productImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        imageProgress.setVisibility(View.GONE);
                                        showFirstScanTooltipIfNeeded();
                                    }

                                    @Override
                                    public void onError() {
                                        imageProgress.setVisibility(View.GONE);
                                        showFirstScanTooltipIfNeeded();
                                    }
                                });
                        } else {
                            showFirstScanTooltipIfNeeded();
                            productImage.setImageResource(R.drawable.placeholder_thumb);
                            imageProgress.setVisibility(View.GONE);
                        }
                        // Hide nutriScore from quickView if app flavour is OFF or there is no nutriscore
                        if (BuildConfig.FLAVOR.equals("off") && product.getNutritionGradeFr() != null) {
                            if (Utils.getImageGrade(product.getNutritionGradeFr()) != Utils.NO_DRAWABLE_RESOURCE) {
                                nutriScore.setVisibility(View.VISIBLE);
                                nutriScore.setImageResource(Utils.getImageGrade(product.getNutritionGradeFr()));
                            } else {
                                nutriScore.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            nutriScore.setVisibility(View.GONE);
                        }
                        // Hide nova group from quickView if app flavour is not OFF or there is no nova group
                        if (BuildConfig.FLAVOR.equals("off") && product.getNovaGroups() != null) {
                            final int novaGroupDrawable = Utils.getNovaGroupDrawable(product);
                            if (novaGroupDrawable != Utils.NO_DRAWABLE_RESOURCE) {
                                novaGroup.setImageResource(novaGroupDrawable);
                            } else {
                                novaGroup.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            novaGroup.setVisibility(View.GONE);
                        }
                        int environmentImpactResource = Utils.getImageEnvironmentImpact(product);
                        if (environmentImpactResource != Utils.NO_DRAWABLE_RESOURCE) {
                            co2Icon.setVisibility(View.VISIBLE);
                            co2Icon.setImageResource(environmentImpactResource);
                        } else {
                            co2Icon.setVisibility(View.INVISIBLE);
                        }
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fm.beginTransaction();
                            ProductFragment newProductFragment = new ProductFragment();

                            Bundle bundle = new Bundle();
                            bundle.putSerializable("state", state);

                            newProductFragment.setArguments(bundle);
                            fragmentTransaction.replace(R.id.frame_layout, newProductFragment);
                            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                            fragmentTransaction.commit();
                            productFragment=newProductFragment;
                            showFirstScanTooltipIfNeeded();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    try {
                        // A network error happened
                        if (e instanceof IOException) {
                            hideAllViews();
                            OfflineSavedProduct offlineSavedProduct = mOfflineSavedProductDao.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(lastText)).unique();
                            if (offlineSavedProduct != null) {
                                showOfflineSavedDetails(offlineSavedProduct);
                                fab_status.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                                fab_status.setImageDrawable(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.ic_mode_edit_black));
                            } else {
                                productNotFound.setText(getString(R.string.addProductOffline, lastText));
                                productNotFound.setVisibility(View.VISIBLE);
                                fab_status.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                                fab_status.setImageDrawable(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.fab_add));
                            }
                            fab_status.setVisibility(View.VISIBLE);
                            quickView.setOnClickListener(v -> navigateToProductAddition(lastText));
                            fab_status.setOnClickListener(v -> navigateToProductAddition(lastText));
                        } else {
                            progressBar.setVisibility(View.GONE);
                            progressText.setVisibility(View.GONE);
                            final Toast errorMessage = Toast.makeText(ContinuousScanActivity.this.getBaseContext(), R.string.txtConnectionError, Toast.LENGTH_LONG);
                            errorMessage.setGravity(Gravity.CENTER, 0, 0);
                            errorMessage.show();
                            Log.i(this.getClass().getSimpleName(), e.getMessage(), e);
                        }
                    } catch (Exception e1) {
                        Log.i(this.getClass().getSimpleName(), e1.getMessage(), e1);
                    }
                }
            });
    }

    private void showFirstScanTooltipIfNeeded() {
        final SharedPreferences sharedPreferences = getSharedPreferences(getClass().getSimpleName(), 0);
        boolean firstScan = sharedPreferences.getBoolean("firstScan", true);
        if (firstScan) {
            SharedPreferences.Editor firstScanEditor = sharedPreferences.edit();
            firstScanEditor.putBoolean("firstScan", false);
            firstScanEditor.apply();

            final Toast firstScanMessage = Toast.makeText(ContinuousScanActivity.this.getBaseContext(), R.string.first_scan_tooltip, Toast.LENGTH_LONG);
            Rect gvr = new Rect();
            quickView.getRootView().getGlobalVisibleRect(gvr);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fab_status.getLayoutParams();
            firstScanMessage.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, params.bottomMargin - BaseActivity.dpsToPixel(50, this));
            firstScanMessage.show();
        }
    }

    private void showOfflineSavedDetails(OfflineSavedProduct offlineSavedProduct) {
        showAllViews();
        HashMap<String, String> productDetails = offlineSavedProduct.getProductDetailsMap();
        String lc = productDetails.get("lang") != null ? productDetails.get("lang") : "en";
        if (productDetails.get("product_name_" + lc) != null) {
            name.setText(productDetails.get("product_name_" + lc));
        } else if (productDetails.get("product_name_en") != null) {
            name.setText(productDetails.get("product_name_en"));
        } else {
            name.setText(R.string.productNameNull);
        }
        if (productDetails.get("add_brands") == null || productDetails.get("add_brands").equals("")) {
            brand.setText(R.string.productBrandNull);
        } else {
            brand.setText(productDetails.get("add_brands"));
        }
        if (productDetails.get("quantity") == null || productDetails.get("quantity").equals("")) {
            quantity.setText(R.string.productQuantityNull);
        } else {
            quantity.setText(productDetails.get("quantity"));
        }
        if (productDetails.get("image_front") != null) {
            Picasso.with(ContinuousScanActivity.this)
                .load("file://" + productDetails.get("image_front"))
                .error(R.drawable.placeholder_thumb)
                .into(productImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        imageProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        imageProgress.setVisibility(View.GONE);
                    }
                });
        } else {
            productImage.setImageResource(R.drawable.placeholder_thumb);
            imageProgress.setVisibility(View.GONE);
        }
    }

    private void navigateToProductAddition(String lastText) {
        Intent intent = new Intent(ContinuousScanActivity.this, AddProductActivity.class);
        State st = new State();
        Product pd = new Product();
        pd.setCode(lastText);
        st.setProduct(pd);
        intent.putExtra("state", st);
        startActivityForResult(intent, ADD_PRODUCT_ACTIVITY_REQUEST_CODE);
    }

    private void showAllViews() {
        slideUpIndicator.setVisibility(View.VISIBLE);
        productImage.setVisibility(View.VISIBLE);
        name.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.VISIBLE);
        brand.setVisibility(View.VISIBLE);
        quantity.setVisibility(View.VISIBLE);
        imageProgress.setVisibility(View.VISIBLE);
        fab_status.setVisibility(View.VISIBLE);
    }

    private void hideAllViews() {
        searchByBarcode.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        slideUpIndicator.setVisibility(View.GONE);
        productImage.setVisibility(View.GONE);
        name.setVisibility(View.GONE);
        frameLayout.setVisibility(View.GONE);
        brand.setVisibility(View.GONE);
        quantity.setVisibility(View.GONE);
        nutriScore.setVisibility(View.GONE);
        novaGroup.setVisibility(View.GONE);
        co2Icon.setVisibility(View.GONE);
        productNotFound.setVisibility(View.GONE);
        fab_status.setVisibility(View.GONE);
        imageProgress.setVisibility(View.GONE);
        txtProductIncomplete.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        OFFApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continuous_scan);
        ButterKnife.bind(this);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Intent intent = new Intent(this, MainActivity.class);

        new SwipeDetector(barcodeView).setOnSwipeListener((v, swipeType) -> {
            if (swipeType == SwipeDetector.SwipeTypeEnum.TOP_TO_BOTTOM) {
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            }
        });

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
            (visibility -> {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // The system bars are visible.
                    hideSystemUI();
                }
            });

        handler = new Handler();
        runnable = () -> {
            if (productShowing) {
                return;
            }
            hideAllViews();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            searchByBarcode.setVisibility(View.VISIBLE);
            searchByBarcode.requestFocus();
        };
        handler.postDelayed(runnable, 15000);

        bottomSheetBehavior = BottomSheetBehavior.from(quickView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (!isNetworkAvailable() && newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    lastText = null;
                }
                if (searchByBarcode.getVisibility() == View.VISIBLE) {
                    bottomSheetBehavior.setPeekHeight(BaseActivity.dpsToPixel(PEEK_SMALL, ContinuousScanActivity.this));
                    bottomSheet.getLayoutParams().height = bottomSheetBehavior.getPeekHeight();
                    bottomSheet.requestLayout();
                } else {
                    bottomSheetBehavior.setPeekHeight(BaseActivity.dpsToPixel(PEEK_LARGE, ContinuousScanActivity.this));
                    bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                    bottomSheet.requestLayout();
                }
            }
            }

            float previousSlideOffset = 0;

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                float slideDelta = slideOffset - previousSlideOffset;
                if (searchByBarcode.getVisibility() != View.VISIBLE && progressBar.getVisibility() != View.VISIBLE) {
                    if (slideOffset > 0.01f || slideOffset < -0.01f) {
                        fab_status.setVisibility(View.GONE);
                        txtProductIncomplete.setVisibility(View.GONE);
                    } else {
                        fab_status.setVisibility(View.VISIBLE);
                        if (productNotFound.getVisibility() != View.VISIBLE && isProductIncomplete()) {
                            txtProductIncomplete.setVisibility(View.VISIBLE);
                        }
                    }
                    if (slideOffset > 0.01f) {
                        details.setVisibility(View.GONE);
                        barcodeView.pause();
                        if (slideDelta > 0 && productFragment != null) {
                            productFragment.bottomSheetWillGrow();
                            bottomNavigationView.setVisibility(View.GONE);
                        }
                    } else {
                        barcodeView.resume();
                        details.setVisibility(View.VISIBLE);
                        fab_status.setVisibility(View.VISIBLE);
                        bottomNavigationView.setVisibility(View.VISIBLE);
                        if (productNotFound.getVisibility() != View.VISIBLE && isProductIncomplete()) {
                            txtProductIncomplete.setVisibility(View.VISIBLE);
                        }                    }
                    previousSlideOffset = slideOffset;
            }
        });

        mHistoryProductDao = Utils.getAppDaoSession(ContinuousScanActivity.this).getHistoryProductDao();
        mOfflineSavedProductDao = Utils.getAppDaoSession(ContinuousScanActivity.this).getOfflineSavedProductDao();

        sp = getSharedPreferences("camera", 0);
        mRing = sp.getBoolean("ring", false);
        mFlash = sp.getBoolean("flash", false);
        mAutofocus = sp.getBoolean("focus", true);
        cameraState = sp.getInt("cameraState", 0);

        popup = new PopupMenu(this, moreOptions);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.UPC_A,
                BarcodeFormat.UPC_E, BarcodeFormat.EAN_13, BarcodeFormat.EAN_8,
                BarcodeFormat.RSS_14, BarcodeFormat.CODE_39, BarcodeFormat.CODE_93,
                BarcodeFormat.CODE_128, BarcodeFormat.ITF);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.setStatusText(null);
        CameraSettings settings = barcodeView.getBarcodeView().getCameraSettings();
        settings.setRequestedCameraId(cameraState);
        if (mFlash) {
            barcodeView.setTorchOn();
            toggleFlash.setImageResource(R.drawable.ic_flash_on_white_24dp);
        }
        if (mRing) {
            popup.getMenu().findItem(R.id.toggleBeep).setChecked(true);
        }
        if (mAutofocus) {
            settings.setAutoFocusEnabled(true);
            popup.getMenu().findItem(R.id.toggleAutofocus).setChecked(true);
        } else {
            settings.setAutoFocusEnabled(false);
        }
        barcodeView.decodeContinuous(callback);
        beepManager = new BeepManager(this);

        searchByBarcode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utils.hideKeyboard(this);
                hideSystemUI();
                if (searchByBarcode.getText().toString().isEmpty()) {
                    Toast.makeText(this, getString(R.string.txtBarcodeNotValid), Toast.LENGTH_SHORT).show();
                } else {
                    String barcodeText = searchByBarcode.getText().toString();
                    if (barcodeText.length() <= 2) {
                        Toast.makeText(this, getString(R.string.txtBarcodeNotValid), Toast.LENGTH_SHORT).show();
                    } else {
                        if (EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(barcodeText) && (!barcodeText.substring(0, 3).contains("977") || !barcodeText.substring(0, 3)
                                .contains("978") || !barcodeText.substring(0, 3).contains("979"))) {
                            lastText = barcodeText;
                            searchByBarcode.setVisibility(View.GONE);
                            findProduct(barcodeText, false);
                        } else {
                            searchByBarcode.requestFocus();
                            Toast.makeText(this, getString(R.string.txtBarcodeNotValid), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return true;
            }
            return false;
        });
        BottomNavigationListenerInstaller.install(bottomNavigationView, this, this);
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLocale()));
    }

    private boolean isProductIncomplete() {
        return product != null && (product.getImageFrontUrl() == null || product.getImageFrontUrl().equals("") ||
            product.getQuantity() == null || product.getQuantity().equals("") ||
            product.getProductName() == null || product.getProductName().equals("") ||
            product.getBrands() == null || product.getBrands().equals("") ||
            product.getIngredientsText() == null || product.getIngredientsText().equals(""));
    }

    void toggleCamera() {
        editor = sp.edit();
        CameraSettings settings = barcodeView.getBarcodeView().getCameraSettings();
        if (barcodeView.getBarcodeView().isPreviewActive()) {
            barcodeView.pause();
        }
        if (settings.getRequestedCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraState = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            cameraState = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        settings.setRequestedCameraId(cameraState);
        barcodeView.getBarcodeView().setCameraSettings(settings);
        editor.putInt("cameraState", cameraState);
        editor.apply();
        barcodeView.resume();
    }

    @OnClick(R.id.toggle_flash)
    void toggleFlash() {
        editor = sp.edit();
        if (mFlash) {
            barcodeView.setTorchOff();
            mFlash = false;
            toggleFlash.setImageResource(R.drawable.ic_flash_off_white_24dp);
            editor.putBoolean("flash", false);
        } else {
            barcodeView.setTorchOn();
            mFlash = true;
            toggleFlash.setImageResource(R.drawable.ic_flash_on_white_24dp);
            editor.putBoolean("flash", true);
        }
        editor.apply();
    }

    @OnClick(R.id.button_more)
    void moreSettings() {
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.toggleBeep:
                    editor = sp.edit();
                    if (mRing) {
                        mRing = false;
                        item.setChecked(false);
                        editor.putBoolean("ring", false);
                    } else {
                        mRing = true;
                        item.setChecked(true);
                        editor.putBoolean("ring", true);
                    }
                    editor.apply();
                    break;
                case R.id.toggleAutofocus:
                    if (barcodeView.getBarcodeView().isPreviewActive()) {
                        barcodeView.pause();
                    }
                    editor = sp.edit();
                    CameraSettings settings = barcodeView.getBarcodeView().getCameraSettings();
                    if (mAutofocus) {
                        mAutofocus = false;
                        settings.setAutoFocusEnabled(false);
                        item.setChecked(false);
                        editor.putBoolean("focus", false);
                    } else {
                        mAutofocus = true;
                        settings.setAutoFocusEnabled(true);
                        item.setChecked(true);
                        editor.putBoolean("focus", true);
                    }
                    barcodeView.getBarcodeView().setCameraSettings(settings);
                    barcodeView.resume();
                    editor.apply();
                    break;
                case R.id.troubleScanning:
                    hideAllViews();
                    handler.removeCallbacks(runnable);
                    quickView.setOnClickListener(null);
                    searchByBarcode.setText(null);
                    searchByBarcode.setVisibility(View.VISIBLE);
                    quickView.setVisibility(View.INVISIBLE);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    handler.postDelayed(() -> quickView.setVisibility(View.VISIBLE), 500);
                    searchByBarcode.requestFocus();
                    break;
                case R.id.toggleCamera:
                    toggleCamera();
                    break;
            }
            return true;
        });
        popup.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PRODUCT_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            boolean uploadedToServer = data.getBooleanExtra(AddProductActivity.UPLOADED_TO_SERVER, true);
            if (uploadedToServer) {
                findProduct(lastText, true);
            } else {
                // Not uploaded to server, saved locally
                OfflineSavedProduct offlineSavedProduct = mOfflineSavedProductDao.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(lastText)).unique();
                if (offlineSavedProduct != null) {
                    hideAllViews();
                    showOfflineSavedDetails(offlineSavedProduct);
                    fab_status.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                    fab_status.setImageDrawable(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.ic_mode_edit_black));
                }
            }
        } else if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(ContinuousScanActivity.this, AddProductActivity.class);
            intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, product);
            startActivityForResult(intent, ADD_PRODUCT_ACTIVITY_REQUEST_CODE);
        }
    }

    private static class HistoryTask extends AsyncTask<Product, Void, Void> {
        @Override
        protected Void doInBackground(Product... products) {
            OpenFoodAPIClient.addToHistory(mHistoryProductDao, products[0]);
            return null;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }
}
