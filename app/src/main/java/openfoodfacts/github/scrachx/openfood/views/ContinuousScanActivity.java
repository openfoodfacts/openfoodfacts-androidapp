package openfoodfacts.github.scrachx.openfood.views;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct;
import openfoodfacts.github.scrachx.openfood.models.HistoryProductDao;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.views.product.ProductActivity;

public class ContinuousScanActivity extends android.support.v7.app.AppCompatActivity {

    private static HistoryProductDao mHistoryProductDao;
    @BindView(R.id.fab_status)
    FloatingActionButton fab_status;
    @BindView(R.id.quick_view)
    RelativeLayout quickView;
    @BindView(R.id.barcode_scanner)
    DecoratedBarcodeView barcodeView;
    @BindView(R.id.toggle_flash)
    ImageView toggleFlash;
    @BindView(R.id.toggle_beep)
    ImageView toggleBeep;
    @BindView(R.id.txt_product_not_complete)
    TextView txtProductIncomplete;
    @BindView(R.id.quickView_progress)
    ProgressBar progressBar;
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
    @BindView(R.id.quickView_textNutriScore)
    TextView textNutriScore;
    @BindView(R.id.quickView_imageProgress)
    ProgressBar imageProgress;
    @Inject
    OpenFoodAPIService client;
    private SharedPreferences.Editor editor;
    private BeepManager beepManager;
    private String lastText;
    private SharedPreferences sp;
    private boolean mFlash;
    private boolean mRing;
    private Disposable disposable;
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }
            if (mRing) {
                beepManager.playBeepSound();
            }

            OFFApplication.getAppComponent().inject(ContinuousScanActivity.this);
            lastText = result.getText();
            client.getFullProductByBarcodeSingle(lastText)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(a -> {
                        quickView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        productImage.setVisibility(View.GONE);
                        name.setVisibility(View.GONE);
                        brand.setVisibility(View.GONE);
                        quantity.setVisibility(View.GONE);
                        nutriScore.setVisibility(View.GONE);
                        productNotFound.setVisibility(View.GONE);
                        fab_status.setVisibility(View.GONE);
                        imageProgress.setVisibility(View.GONE);
                        textNutriScore.setVisibility(View.GONE);
                        txtProductIncomplete.setVisibility(View.GONE);
                    })
                    .subscribe(new SingleObserver<State>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable = d;
                        }

                        @Override
                        public void onSuccess(State state) {
                            progressBar.setVisibility(View.GONE);
                            if (state.getStatus() == 0) {
                                quickView.setOnClickListener(null);
                                productNotFound.setText(R.string.txtDialogsContent);
                                productNotFound.setVisibility(View.VISIBLE);
                                productImage.setVisibility(View.GONE);
                                name.setVisibility(View.GONE);
                                brand.setVisibility(View.GONE);
                                quantity.setVisibility(View.GONE);
                                nutriScore.setVisibility(View.GONE);
                                textNutriScore.setVisibility(View.GONE);
                                imageProgress.setVisibility(View.GONE);
                                txtProductIncomplete.setVisibility(View.INVISIBLE);
                                fab_status.setVisibility(View.VISIBLE);
                                fab_status.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                                fab_status.setImageDrawable(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.ic_add_a_photo));
                                fab_status.setOnClickListener(v -> {
                                    Intent intent = new Intent(ContinuousScanActivity.this, SaveProductOfflineActivity.class);
                                    State st = new State();
                                    Product pd = new Product();
                                    pd.setCode(lastText);
                                    st.setProduct(pd);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("state", st);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                });
                            } else {
                                Product product = state.getProduct();
                                new HistoryTask().doInBackground(product);
                                productNotFound.setVisibility(View.GONE);
                                productImage.setVisibility(View.VISIBLE);
                                name.setVisibility(View.VISIBLE);
                                brand.setVisibility(View.VISIBLE);
                                quantity.setVisibility(View.VISIBLE);
                                nutriScore.setVisibility(View.VISIBLE);
                                imageProgress.setVisibility(View.VISIBLE);
                                fab_status.setVisibility(View.VISIBLE);
                                if (product.getImageUrl() == null || product.getQuantity() == null ||
                                        product.getProductName() == null || product.getBrands() == null ||
                                        product.getNutritionGradeFr() == null) {
                                    txtProductIncomplete.setVisibility(View.VISIBLE);
                                    fab_status.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                                    fab_status.setImageDrawable(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.ic_mode_edit_black));
                                    fab_status.setOnClickListener(v -> {
                                        String url = getString(R.string.website) + "cgi/product.pl?type=edit&code=" + product.getCode();
                                        if (product.getUrl() != null) {
                                            url = " " + product.getUrl();
                                        }
                                        CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getBaseContext(), null);
                                        CustomTabActivityHelper.openCustomTab(ContinuousScanActivity.this, customTabsIntent, Uri.parse(url), new WebViewFallback());
                                    });
                                } else {
                                    txtProductIncomplete.setVisibility(View.INVISIBLE);
                                    fab_status.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_green)));
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
                                if (product.getImageUrl() != null) {
                                    Picasso.with(ContinuousScanActivity.this)
                                            .load(product.getImageUrl())
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
                                // Hide nutriScore from quickView if app flavour is not OFF
                                if (BuildConfig.FLAVOR.equals("off")) {
                                    nutriScore.setImageResource(Utils.getImageGrade(product.getNutritionGradeFr()));
                                    if (product.getNutritionGradeFr() == null) {
                                        textNutriScore.setVisibility(View.VISIBLE);
                                    } else {
                                        textNutriScore.setVisibility(View.GONE);
                                    }
                                } else {
                                    nutriScore.setVisibility(View.GONE);
                                    textNutriScore.setVisibility(View.GONE);
                                }
                                quickView.setOnClickListener(v -> {
                                    Intent intent = new Intent(ContinuousScanActivity.this, ProductActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("state", state);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                });
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            productNotFound.setText(R.string.errorWeb);
                            productNotFound.setVisibility(View.VISIBLE);
                            productImage.setVisibility(View.GONE);
                            name.setVisibility(View.GONE);
                            brand.setVisibility(View.GONE);
                            quantity.setVisibility(View.GONE);
                            nutriScore.setVisibility(View.GONE);
                            textNutriScore.setVisibility(View.GONE);
                            imageProgress.setVisibility(View.GONE);
                            fab_status.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {

        }
    };

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
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continuous_scan);
        ButterKnife.bind(this);
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mHistoryProductDao = Utils.getAppDaoSession(ContinuousScanActivity.this).getHistoryProductDao();

        sp = getSharedPreferences("camera", 0);
        mRing = sp.getBoolean("ring", false);
        mFlash = sp.getBoolean("flash", false);

        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.UPC_A,
                BarcodeFormat.UPC_E, BarcodeFormat.EAN_13, BarcodeFormat.EAN_8,
                BarcodeFormat.RSS_14, BarcodeFormat.CODE_39, BarcodeFormat.CODE_93,
                BarcodeFormat.CODE_128, BarcodeFormat.ITF);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.setStatusText(null);
        CameraSettings settings = barcodeView.getBarcodeView().getCameraSettings();
        settings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (mFlash) {
            barcodeView.setTorchOn();
            toggleFlash.setImageResource(R.drawable.ic_flash_on_white_24dp);
        }
        if (mRing) {
            toggleBeep.setImageResource(R.drawable.ic_volume_up_white_24dp);
        }
        barcodeView.decodeContinuous(callback);
        beepManager = new BeepManager(this);
    }

    @OnClick(R.id.toggle_camera)
    void toggleCamera() {
        CameraSettings settings = barcodeView.getBarcodeView().getCameraSettings();
        if (barcodeView.getBarcodeView().isPreviewActive()) {
            barcodeView.pause();
        }
        if (settings.getRequestedCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            settings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            settings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        barcodeView.getBarcodeView().setCameraSettings(settings);
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

    @OnClick(R.id.toggle_beep)
    void toggleBeep() {
        editor = sp.edit();
        if (mRing) {
            mRing = false;
            toggleBeep.setImageResource(R.drawable.ic_volume_off_white_24dp);
            editor.putBoolean("ring", false);
        } else {
            mRing = true;
            toggleBeep.setImageResource(R.drawable.ic_volume_up_white_24dp);
            editor.putBoolean("ring", true);
        }
        editor.apply();
    }

    private static class HistoryTask extends AsyncTask<Product, Void, Void> {
        @Override
        protected Void doInBackground(Product... products) {
            Product product = products[0];
            List<HistoryProduct> historyProducts = mHistoryProductDao.queryBuilder().where(HistoryProductDao.Properties.Barcode.eq(product.getCode())).list();
            HistoryProduct hp;
            if (historyProducts.size() == 1) {
                hp = historyProducts.get(0);
                hp.setLastSeen(new Date());
            } else {
                hp = new HistoryProduct(product.getProductName(), product.getBrands(), product.getImageSmallUrl(), product.getCode(), product
                        .getQuantity(), product.getNutritionGradeFr());
            }
            mHistoryProductDao.insertOrReplace(hp);
            return null;
        }
    }
}
