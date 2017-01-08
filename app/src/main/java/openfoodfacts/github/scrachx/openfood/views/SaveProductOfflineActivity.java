package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
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
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
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
    @BindView(R.id.imageSave) ImageView imgSave;
    @BindView(R.id.editTextName) EditText name;
    @BindView(R.id.editTextBrand) EditText brand;
    @BindView(R.id.editTextWeight) EditText weight;
    @BindView(R.id.spinnerImages) Spinner spinnerI;
    @BindView(R.id.spinnerUnitWeight) Spinner spinnerW;
    @BindView(R.id.buttonTakePicture) Button takePic;
    @BindView(R.id.buttonFromGallery) Button takeGallery;
    @BindView(R.id.buttonSaveProduct) Button save;

    private SendProduct mProduct;
    private String mBarcode;
    private OpenFoodAPIClient api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_product_offline);

        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
                new MaterialDialog.Builder(this)
                        .title(R.string.action_about)
                        .content(R.string.permission_storage)
                        .neutralText(R.string.txtOk)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ActivityCompat.requestPermissions(SaveProductOfflineActivity.this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE);
            }
        }

        api = new OpenFoodAPIClient(this);
        mBarcode = getIntent().getStringExtra("barcode");

        EasyImage.configuration(this)
                .setImagesFolderName("OFF_Images")
                .saveInAppExternalFilesDir()
                .setCopyExistingPicturesToPublicLocation(true);

        imgSave.setVisibility(View.GONE);

        ArrayAdapter<CharSequence> adapterW = ArrayAdapter.createFromResource(this, R.array.units_array, R.layout.custom_spinner_item);
        adapterW.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerW.setAdapter(adapterW);

        ArrayAdapter<CharSequence> adapterI = ArrayAdapter.createFromResource(this, R.array.images_array, R.layout.custom_spinner_item);
        adapterI.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerI.setAdapter(adapterI);

        List<SendProduct> sp = SendProduct.find(SendProduct.class, "barcode = ?", mBarcode);
        if (sp.size() > 0) {
            mProduct = sp.get(0);
        }
        if(mProduct != null) {
            if(isNotEmpty(mProduct.getImgupload_front())) {
                Picasso.with(this)
                        .load(mProduct.getImgupload_front())
                        .fit()
                        .centerCrop()
                        .into(imgSave);
            }
            name.setText(mProduct.getName());
            brand.setText(mProduct.getBrands());
            weight.setText(mProduct.getWeight());
            ArrayAdapter unitAdapter = (ArrayAdapter) spinnerW.getAdapter();
            int spinnerPosition = unitAdapter.getPosition(mProduct.getWeight_unit());
            spinnerW.setSelection(spinnerPosition);
            spinnerI.setSelection(0);
        } else {
            mProduct = new SendProduct();
            mProduct.setBarcode(mBarcode);
        }

        new MaterialDialog.Builder(this)
                .title(R.string.title_info_dialog)
                .content(R.string.new_offline_info)
                .positiveText(R.string.txtOk)
                .show();
    }

    @OnItemSelected(value = R.id.spinnerUnitWeight, callback = OnItemSelected.Callback.ITEM_SELECTED)
    protected void onUnitSelected(int pos) {
        mUnit[0] = spinnerW.getItemAtPosition(pos).toString();
    }

    @OnItemSelected(value = R.id.spinnerImages, callback = OnItemSelected.Callback.ITEM_SELECTED)
    protected void onImageSelected(int pos) {
        if(pos == 0) {
            if(isNotBlank(mProduct.getImgupload_front())) {
                imgSave.setVisibility(View.VISIBLE);
                Picasso.with(this)
                        .load(new File(mProduct.getImgupload_front()))
                        .fit()
                        .centerCrop()
                        .into(imgSave);
            } else {
                imgSave.setVisibility(View.GONE);
            }
        } else if(pos == 1) {
            if(isNotBlank(mProduct.getImgupload_nutrition())) {
                imgSave.setVisibility(View.VISIBLE);
                Picasso.with(this)
                        .load(new File(mProduct.getImgupload_nutrition()))
                        .fit()
                        .centerCrop()
                        .into(imgSave);
            } else {
                imgSave.setVisibility(View.GONE);
            }
        } else {
            if(isNotBlank(mProduct.getImgupload_ingredients())) {
                imgSave.setVisibility(View.VISIBLE);
                Picasso.with(this)
                        .load(new File(mProduct.getImgupload_ingredients()))
                        .fit()
                        .centerCrop()
                        .into(imgSave);
            } else {
                imgSave.setVisibility(View.GONE);
            }
        }

    }

    @OnClick(R.id.buttonFromGallery)
    protected void onChooserWithGalleryClicked() {
        EasyImage.openChooserWithGallery(this, "Images", 0);
    }

    @OnClick(R.id.buttonSaveProduct)
    protected void onSaveProduct() {
        Utils.hideKeyboard(this);

        if (isBlank(mProduct.getImgupload_front())) {
            Toast.makeText(getApplicationContext(), R.string.txtPictureNeeded, Toast.LENGTH_LONG).show();
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

        Utils.compressImage(mProduct.getImgupload_front());

        if (isNotBlank(mProduct.getImgupload_ingredients())) {
            Utils.compressImage(mProduct.getImgupload_ingredients());
        }

        if(isNotBlank(mProduct.getImgupload_nutrition())) {
            Utils.compressImage(mProduct.getImgupload_nutrition());
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            final Activity activity = this;
            api.post(this, mProduct, new OpenFoodAPIClient.OnProductSentCallback() {
                @Override
                public void onProductSentResponse(boolean value) {
                    if (!value) {
                        mProduct.save();
                        Toast.makeText(getApplicationContext(), R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("openOfflineEdit", true);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.product_sent, Toast.LENGTH_LONG).show();
                        api.getProduct(mProduct.getBarcode(), activity);
                    }
                    finish();
                }
            });
        } else {
            mProduct.save();
            Toast.makeText(getApplicationContext(), R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("openOfflineEdit", true);
            startActivity(intent);
            finish();
        }
    }

    @OnClick(R.id.buttonTakePicture)
    protected void onTakePhotoClicked() {
        if (ContextCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            EasyImage.openCamera(this, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                onPhotoReturned(imageFiles.get(0));
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
        if(spinnerI.getSelectedItemPosition() == 0) {
            mProduct.setImgupload_front(photoFile.getAbsolutePath());
        } else if(spinnerI.getSelectedItemPosition() == 1) {
            mProduct.setImgupload_nutrition(photoFile.getAbsolutePath());
        } else {
            mProduct.setImgupload_ingredients(photoFile.getAbsolutePath());
        }

        imgSave.setVisibility(View.VISIBLE);

        Picasso.with(this)
                .load(photoFile)
                .fit()
                .centerCrop()
                .into(imgSave);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
            case Utils.MY_PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length <= 0 || grantResults[0] != PERMISSION_GRANTED) {
                    new MaterialDialog.Builder(this)
                            .title(R.string.permission_title)
                            .content(R.string.permission_denied)
                            .negativeText(R.string.txtNo)
                            .positiveText(R.string.txtYes)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            })
                            .show();
                }
            }
        }
    }
}
