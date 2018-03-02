package openfoodfacts.github.scrachx.openfood.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.SendProductDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class SaveProductOfflineActivity extends BaseActivity {

    private final String[] mUnit = new String[1];
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.barcodeDoubleCheck)
    TextView barcodeText;
    @BindView(R.id.imageSaveFront)
    PhotoView imgSaveFront;
    @BindView(R.id.imageSaveNutrition)
    PhotoView imgSaveNutrition;
    @BindView(R.id.imageSaveIngredients)
    PhotoView imgSaveIngredients;
    @BindView(R.id.editTextName)
    EditText name;
    @BindView(R.id.editTextBrand)
    EditText brand;
    @BindView(R.id.editTextWeight)
    EditText weight;
    @BindView(R.id.spinnerUnitWeight)
    Spinner spinnerW;
    @BindView(R.id.buttonSaveProduct)
    Button save;
    @BindView(R.id.buttonTakePictureFront)
    ImageButton btnTakeFront;
    @BindView(R.id.buttonTakePictureIngredients)
    ImageButton btnTakeIngredients;
    @BindView(R.id.buttonTakePictureNutrition)
    ImageButton btnTakeNutrition;
    @BindView(R.id.message_container_card_view)
    CardView mContainerView;
    @BindView(R.id.message_dismiss_icon)
    ImageButton mDismissButton;

    PhotoViewAttacher mAttacherimgSaveFront;
    PhotoViewAttacher mAttacherimgSaveNutrition;
    PhotoViewAttacher mAttacherimageSaveIngredients;

    private SendProduct mProduct;
    private String mBarcode;
    private OpenFoodAPIClient api;
    private String imageTaken;
    private SendProductDao mSendProductDao;
    private SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_product_offline);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSendProductDao = Utils.getAppDaoSession(this).getSendProductDao();
        mSharedPref = getApplicationContext().getSharedPreferences("prefs", 0);
        boolean isMsgOnlyOnePhotoNecessaryDismissed = mSharedPref.getBoolean("is_msg_only_one_photo_necessary_dismissed", false);
        if (isMsgOnlyOnePhotoNecessaryDismissed) {
            mContainerView.setVisibility(View.GONE);
        }
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
                new MaterialDialog.Builder(this)
                        .title(R.string.action_about)
                        .content(R.string.permission_storage)
                        .neutralText(R.string.txtOk)
                        .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(SaveProductOfflineActivity.this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE))
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE);
            }
        }

        api = new OpenFoodAPIClient(this);
        mBarcode = getIntent().getStringExtra("barcode");
        barcodeText.append(" " + mBarcode);

        imgSaveFront.setVisibility(View.GONE);
        imgSaveIngredients.setVisibility(View.GONE);
        imgSaveNutrition.setVisibility(View.GONE);

        ArrayAdapter<CharSequence> adapterW = ArrayAdapter.createFromResource(this, R.array.units_array, R.layout.custom_spinner_item);
        adapterW.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerW.setAdapter(adapterW);

        List<SendProduct> sp = mSendProductDao.queryBuilder().where(SendProductDao.Properties.Barcode.eq(mBarcode)).list();

        if (sp.size() > 0) {
            mProduct = sp.get(0);
        }
        if (mProduct != null) {
            if (isNotEmpty(mProduct.getImgupload_front())) {
                imgSaveFront.setVisibility(View.VISIBLE);
                Picasso.with(this)
                        .load(new File(mProduct.getImgupload_front()))
                        .fit()
                        .centerCrop()
                        .into(imgSaveFront);
            }
            if (isNotBlank(mProduct.getImgupload_nutrition())) {
                imgSaveNutrition.setVisibility(View.VISIBLE);
                Picasso.with(this)
                        .load(new File(mProduct.getImgupload_nutrition()))
                        .fit()
                        .centerCrop()
                        .into(imgSaveNutrition);
            }
            if (isNotBlank(mProduct.getImgupload_ingredients())) {
                imgSaveIngredients.setVisibility(View.VISIBLE);
                Picasso.with(this)
                        .load(new File(mProduct.getImgupload_ingredients()))
                        .fit()
                        .centerCrop()
                        .into(imgSaveIngredients);
            }
            name.setText(mProduct.getName());
            brand.setText(mProduct.getBrands());
            weight.setText(mProduct.getWeight());
            ArrayAdapter unitAdapter = (ArrayAdapter) spinnerW.getAdapter();
            int spinnerPosition = unitAdapter.getPosition(mProduct.getWeight_unit());
            spinnerW.setSelection(spinnerPosition);
        } else {
            mProduct = new SendProduct();
            mProduct.setBarcode(mBarcode);
        }

        mProduct.setLang(Locale.getDefault().getLanguage());

        checkIfCameraInstalled();
    }

    /**
     * Check if there is a camera installed on the device. If there is not, then hide the views that
     * allow the user to take a photo.
     */
    private void checkIfCameraInstalled() {
        if (!Utils.isHardwareCameraInstalled(this)) {
            btnTakeFront.setVisibility(View.GONE);
            btnTakeNutrition.setVisibility(View.GONE);
            btnTakeIngredients.setVisibility(View.GONE);
        }
    }

    @OnItemSelected(value = R.id.spinnerUnitWeight, callback = OnItemSelected.Callback.ITEM_SELECTED)
    protected void onUnitSelected(int pos) {
        mUnit[0] = spinnerW.getItemAtPosition(pos).toString();
    }

    @OnClick(R.id.buttonFromGalleryFront)
    protected void onChooserWithGalleryFrontClicked() {
        EasyImage.openChooserWithGallery(this, "Images", 0);
        imageTaken = "front";
    }

    @OnClick(R.id.buttonFromGalleryIngredients)
    protected void onChooserWithGalleryIngredientsClicked() {
        EasyImage.openChooserWithGallery(this, "Images", 0);
        imageTaken = "ingredients";
    }

    @OnClick(R.id.buttonFromGalleryNutrition)
    protected void onChooserWithGalleryNutritionClicked() {
        EasyImage.openChooserWithGallery(this, "Images", 0);
        imageTaken = "nutrition";
    }

    @OnClick(R.id.buttonSaveProduct)
    protected void onSaveProduct() {
        Utils.hideKeyboard(this);

        save.setEnabled(false);
        save.setText(getString(R.string.saving));

        if (isBlank(mProduct.getImgupload_front())) {
            Toast.makeText(getApplicationContext(), R.string.txtPictureNeeded, Toast.LENGTH_LONG).show();
            save.setEnabled(true);
            save.setText(getString(R.string.txtSave));
            return;
        }

        mProduct.setBarcode(mBarcode);
        mProduct.setName(name.getText().toString());
        mProduct.setWeight(weight.getText().toString());
        mProduct.setWeight_unit(mUnit[0]);
        mProduct.setBrands(brand.getText().toString());

        final SharedPreferences settingsUser = getSharedPreferences("login", 0);
        String login = settingsUser.getString("user", "");
        String password = settingsUser.getString("pass", "");
        if (!login.isEmpty() && !password.isEmpty()) {
            mProduct.setUserId(login);
            mProduct.setPassword(password);
        }

        if (isNotEmpty(mProduct.getImgupload_front())) {
            Utils.compressImage(mProduct.getImgupload_front());
        }

        if (isNotBlank(mProduct.getImgupload_ingredients())) {
            Utils.compressImage(mProduct.getImgupload_ingredients());
        }

        if (isNotBlank(mProduct.getImgupload_nutrition())) {
            Utils.compressImage(mProduct.getImgupload_nutrition());
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            final Activity activity = this;
            api.post(this, mProduct, value -> {
                if (!value) {
                    mSendProductDao.insert(mProduct);
                    Toast.makeText(getApplicationContext(), R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("openOfflineEdit", true);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.product_sent, Toast.LENGTH_LONG).show();
                    api.getProduct(mProduct.getBarcode(), activity);
                }
                finish();
            });
        } else {
            mSendProductDao.insertOrReplace(mProduct);
            Toast.makeText(getApplicationContext(), R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("openOfflineEdit", true);
            startActivity(intent);
            finish();
        }
    }

    @OnClick(R.id.buttonTakePictureFront)
    protected void onTakePhotoFrontClicked() {
        if (ContextCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            imageTaken = "front";
            EasyImage.openCamera(this, 0);
        }
    }

    @OnClick(R.id.buttonTakePictureIngredients)
    protected void onTakePhotoIngredientsClicked() {
        if (ContextCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            imageTaken = "ingredients";
            EasyImage.openCamera(this, 0);
        }
    }

    @OnClick(R.id.buttonTakePictureNutrition)
    protected void onTakePhotoNutritionClicked() {
        if (ContextCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            imageTaken = "nutrition";
            EasyImage.openCamera(this, 0);
        }
    }

    @OnClick(R.id.message_dismiss_icon)
    protected void onMessageDismissClicked() {
        mContainerView.setVisibility(View.GONE);
        mSharedPref.edit().putBoolean("is_msg_only_one_photo_necessary_dismissed", true).apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                onPhotoReturned(new File(resultUri.getPath()));
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                CropImage.activity(Uri.fromFile(imageFiles.get(0))).setAllowFlipping(false)
                        .start(SaveProductOfflineActivity.this);

            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(SaveProductOfflineActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }

    private void onPhotoReturned(File photoFile) {
        if (imageTaken.equals("front")) {
            mProduct.setImgupload_front(photoFile.getAbsolutePath());
            imgSaveFront.setVisibility(View.VISIBLE);
            mAttacherimgSaveFront = new PhotoViewAttacher(imgSaveFront);
                Picasso.with(this)
                        .load(photoFile)
                        .into(imgSaveFront, new Callback() {
                            @Override
                            public void onSuccess() {
                                mAttacherimgSaveFront.update();
                            }

                            @Override
                            public void onError() {
                            }
                        });
            }
         else if (imageTaken.equals("nutrition")) {
            mProduct.setImgupload_nutrition(photoFile.getAbsolutePath());
            imgSaveNutrition.setVisibility(View.VISIBLE);
            mAttacherimgSaveNutrition = new PhotoViewAttacher(imgSaveNutrition);
            Picasso.with(this)
                    .load(photoFile)
                    .into(imgSaveNutrition, new Callback() {
                        @Override
                        public void onSuccess() {
                            mAttacherimgSaveNutrition.update();
                        }
                        @Override
                        public void onError() {
                        }
                    });
        } else if (imageTaken.equals("ingredients")) {
            mProduct.setImgupload_ingredients(photoFile.getAbsolutePath());
            imgSaveIngredients.setVisibility(View.VISIBLE);
            mAttacherimageSaveIngredients = new PhotoViewAttacher(imgSaveIngredients);
            Picasso.with(this)
                    .load(photoFile)
                    .into(imgSaveIngredients, new Callback() {
                        @Override
                        public void onSuccess() {
                            mAttacherimageSaveIngredients.update();
                        }
                        @Override
                        public void onError() {
                        }
                    });
        }
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
