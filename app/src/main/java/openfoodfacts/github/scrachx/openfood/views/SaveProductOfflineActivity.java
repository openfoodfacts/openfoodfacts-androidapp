package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.SaveOfflineSummaryFragment;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.SendProductDao;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients.IngredientsProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.nutrition_details.NutritionInfoProductFragment;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SaveProductOfflineActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.pager)
    ViewPager viewPager;
    @BindView(R.id.buttonScan)
    FloatingActionButton mButtonScan;
    @BindView(R.id.tabs)
    TabLayout tabLayout;

    private SendProduct mProduct;
    private String mBarcode;
    private OpenFoodAPIClient api;
    private SendProductDao mSendProductDao;
    private SaveOfflineSummaryFragment mSummerAdditionFragment = new SaveOfflineSummaryFragment();
    private IngredientsProductFragment mIngredientsProductFragment = new IngredientsProductFragment();
    private NutritionInfoProductFragment mNutritionInfoProductFragment = new NutritionInfoProductFragment();
    private boolean fromOfflineEdit;
    private boolean isSavingGoingOn = false;
    private SendProduct preProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getBoolean(R.bool.portrait_only)) {

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_save_product_offline);

        final State state = (State) getIntent().getExtras().getSerializable("state");
        final Product product = state.getProduct();
        fromOfflineEdit = (boolean) getIntent().getBooleanExtra("offlineEdit", false);

        preProduct = new SendProduct();
        mBarcode = product.getCode();
        api = new OpenFoodAPIClient(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mSendProductDao = Utils.getAppDaoSession(this).getSendProductDao();

        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
                new MaterialDialog.Builder(this)
                        .title(R.string.action_about)
                        .content(R.string.permission_storage)
                        .neutralText(R.string.txtOk)
                        .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(SaveProductOfflineActivity.this, new
                                String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE))
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, Utils
                        .MY_PERMISSIONS_REQUEST_STORAGE);
            }
        }

        List<SendProduct> sp = mSendProductDao.queryBuilder().where(SendProductDao.Properties.Barcode.eq(mBarcode)).list();

        if (sp.size() > 0) {
            mProduct = sp.get(0);
        } else {
            mProduct = new SendProduct();
        }
        preProduct.copy(mProduct);
        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);

    }


    private void setupViewPager(ViewPager viewPager) {
        String[] menuTitles = getResources().getStringArray(R.array.nav_drawer_items_product);

        ProductFragmentPagerAdapter adapterResult = new ProductFragmentPagerAdapter(getSupportFragmentManager());
        Bundle bn = new Bundle();
        bn.putSerializable("sendProduct", mProduct);
        mSummerAdditionFragment.setArguments(bn);
        mIngredientsProductFragment.setArguments(bn);
        mNutritionInfoProductFragment.setArguments(bn);
        adapterResult.addFragment(mSummerAdditionFragment, menuTitles[0]);
        adapterResult.addFragment(mIngredientsProductFragment, menuTitles[1]);
        if (BuildConfig.FLAVOR.equals("off")) {
            adapterResult.addFragment(mNutritionInfoProductFragment, menuTitles[3]);
        }
        if (BuildConfig.FLAVOR.equals("opff")) {
            adapterResult.addFragment(mNutritionInfoProductFragment, menuTitles[3]);
        }
        viewPager.setAdapter(adapterResult);
    }


    @OnClick(R.id.buttonScan)
    protected void OnScan() {
        if (Utils.isHardwareCameraInstalled(this)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    new MaterialDialog.Builder(this)
                            .title(R.string.action_about)
                            .content(R.string.permission_camera)
                            .neutralText(R.string.txtOk)
                            .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Utils
                                    .MY_PERMISSIONS_REQUEST_CAMERA))
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
                }
            } else {
                Intent intent = new Intent(this, ContinuousScanActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }


    protected void saveProduct(boolean isVisible) {
        isSavingGoingOn = true;
        Utils.hideKeyboard(this);

        mProduct = mSummerAdditionFragment.getProduct();

        if (isBlank(mProduct.getImgupload_front())) {

//            Toast.makeText(getApplicationContext(), R.string.txtPictureNeeded, Toast.LENGTH_LONG).show();
            if (isVisible)
                saveAlert();
            isSavingGoingOn = false;
            return;
        }
        if (isNotBlank(mIngredientsProductFragment.getIngredients()))
            mProduct.setImgupload_ingredients(mIngredientsProductFragment.getIngredients());
        if (isNotBlank(mNutritionInfoProductFragment.getNutrients()))
            mProduct.setImgupload_nutrition(mNutritionInfoProductFragment.getNutrients());
        mProduct.setLang(Locale.getDefault().getLanguage());

        if (preProduct.isEqual(mProduct)) {
            isSavingGoingOn = false;
            if (isVisible)
                finish();
            return;
        } else {
            preProduct.copy(mProduct);
        }

//       sending product with out images
        SendProduct uploadablePdt = new SendProduct();
        uploadablePdt.setBarcode(mProduct.getBarcode());
        uploadablePdt.setBarcode(mProduct.getBarcode());
        uploadablePdt.setBrands(mProduct.getBrands());
        uploadablePdt.setWeight(mProduct.getWeight());
        uploadablePdt.setWeight_unit(mProduct.getWeight_unit());
        uploadablePdt.setName(mProduct.getName());
        uploadablePdt.setUserId(mProduct.getUserId());
        uploadablePdt.setPassword(mProduct.getPassword());
        uploadablePdt.setLang(mProduct.getLang());

        if (Utils.isNetworkConnected(this)) {
            final Activity activity = this;
            api.post(this, uploadablePdt, value -> {
                if (!value) {
                    mSendProductDao.insertOrReplace(mProduct);
                    if (isVisible) {
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        if (!fromOfflineEdit) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("openOfflineEdit", true);
                            startActivity(intent);
                            finish();
                        } else {
                            finish();
                        }
                    }
                } else {
                    if (isVisible) {
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.product_sent, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        if (!fromOfflineEdit) {
                            api.getProduct(mProduct.getBarcode(), activity);
                            finish();
                        } else {
                            mSendProductDao.insertOrReplace(mProduct);
                            finish();
                        }
                    }
                }
                isSavingGoingOn = false;
            });
        } else {
            mSendProductDao.insertOrReplace(mProduct);
            if (isVisible) {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                if (!fromOfflineEdit) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("openOfflineEdit", true);
                    startActivity(intent);
                    finish();
                } else {
                    finish();
                }
            }
            isSavingGoingOn = false;
        }

    }

    private void saveAlert() {

        new MaterialDialog.Builder(this)
                .content(R.string.txtPictureNeededDialogContent)
                .positiveText(R.string.txtPictureNeededDialogYes)
                .negativeText(R.string.txtPictureNeededDialogNo)
                .onPositive((dialog, which) -> dialog.cancel())
                .onNegative((dialog, which) -> finish())
                .show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
//                isVisible=true;
                if (!isSavingGoingOn)
                    saveProduct(true);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
//        isVisible=true;
        if (!isSavingGoingOn)
            saveProduct(true);
    }

    @Override
    protected void onStop() {
        Log.d("stopOffline", "onStop: ");
//        isVisible=false;
        if (!isSavingGoingOn)
            saveProduct(false);
        super.onStop();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length <= 0 || grantResults[0] != PERMISSION_GRANTED) {
                    new MaterialDialog.Builder(this)
                            .title(R.string.permission_title)
                            .content(R.string.permission_denied)
                            .negativeText(R.string.txtNo)
                            .positiveText(R.string.txtYes)
                            .onPositive((dialog, which) -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .show();
                }
            case Utils.MY_PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length <= 0 || grantResults[0] != PERMISSION_GRANTED) {
                    new MaterialDialog.Builder(this)
                            .title(R.string.permission_title)
                            .content(R.string.permission_denied)
                            .negativeText(R.string.txtNo)
                            .positiveText(R.string.txtYes)
                            .onPositive((dialog, which) -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .show();
                }
            }
        }
    }


}
