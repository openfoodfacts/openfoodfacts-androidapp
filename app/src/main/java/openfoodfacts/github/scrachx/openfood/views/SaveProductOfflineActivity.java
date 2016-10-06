package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.FoodUserClientUsage;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class SaveProductOfflineActivity extends BaseActivity {

    @BindView(R.id.imageSave) ImageView imgSave;
    @BindView(R.id.editTextName) EditText name;
    @BindView(R.id.editTextStores) EditText store;
    @BindView(R.id.editTextWeight) EditText weight;
    @BindView(R.id.spinnerImages) Spinner spinnerI;
    @BindView(R.id.spinnerUnitWeight) Spinner spinnerW;
    @BindView(R.id.buttonTakePicture) Button takePic;
    @BindView(R.id.buttonFromGallery) Button takeGallery;
    @BindView(R.id.buttonSaveProduct) Button save;

    private SendProduct mProduct = new SendProduct();
    private String mBarcode = null;
    private FoodUserClientUsage api;
    private final String[] mUnit = new String[1];
    private final String[] mImage = new String[1];
    private String loginS, passS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_product_offline);

        new MaterialDialog.Builder(this)
                .title(R.string.title_info_dialog)
                .content(R.string.new_offline_info)
                .positiveText(R.string.txtOk)
                .show();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new MaterialDialog.Builder(this)
                        .title(R.string.action_about)
                        .content(R.string.permission_storage)
                        .neutralText(R.string.txtOk)
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE);
            }
        }

        api = new FoodUserClientUsage();

        final SharedPreferences settingsUser = getSharedPreferences("login", 0);
        loginS = settingsUser.getString("user", "");
        passS = settingsUser.getString("pass", "");
        final SharedPreferences settings = getSharedPreferences("temp", 0);
        mBarcode = settings.getString("barcode", "");

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
            if(!mProduct.getImgupload_front().isEmpty()) {
                Picasso.with(this)
                        .load(mProduct.getImgupload_front())
                        .fit()
                        .centerCrop()
                        .into(imgSave);
            }
            name.setText(mProduct.getName());
            store.setText(mProduct.getStores());
            weight.setText(mProduct.getWeight());
            ArrayAdapter unitAdapter = (ArrayAdapter) spinnerW.getAdapter();
            int spinnerPosition = unitAdapter.getPosition(mProduct.getWeight_unit());
            spinnerW.setSelection(spinnerPosition);
            spinnerI.setSelection(0);
        }
    }
    @OnItemSelected(value = R.id.spinnerUnitWeight, callback = OnItemSelected.Callback.ITEM_SELECTED)
    protected void onUnitSelected(int pos) {
        mUnit[0] = spinnerW.getItemAtPosition(pos).toString();
    }

    @OnItemSelected(value = R.id.spinnerImages, callback = OnItemSelected.Callback.ITEM_SELECTED)
    protected void onImageSelected(int pos) {
        mImage[0] = spinnerI.getItemAtPosition(pos).toString();

        if(pos == 0) {
            if(!mProduct.getImgupload_front().isEmpty()) {
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
            if(!mProduct.getImgupload_nutrition().isEmpty()) {
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
            if(!mProduct.getImgupload_ingredients().isEmpty()) {
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
        if (!mProduct.getImgupload_front().isEmpty()) {
            RequestParams params = new RequestParams();
            params.put("code", mBarcode);
            if(!loginS.isEmpty() && !passS.isEmpty()) {
                params.put("user_id", loginS);
                params.put("password", passS);
            }
            params.put("product_name", name.getText().toString());
            params.put("quantity", weight.getText().toString() + " " + mUnit[0]);
            params.put("stores", store.getText().toString());
            params.put("comment", "added with the new Android app");

            Utils.compressImage(mProduct.getImgupload_ingredients());
            Utils.compressImage(mProduct.getImgupload_nutrition());
            Utils.compressImage(mProduct.getImgupload_front());

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if(isConnected) {
                api.post(this, params, mProduct.getImgupload_front().replace(".png", "_small.png"), mProduct.getImgupload_ingredients().replace(".png", "_small.png"),
                        mProduct.getImgupload_nutrition().replace(".png", "_small.png"), mBarcode,
                        new FoodUserClientUsage.OnProductSentCallback() {
                            @Override
                            public void onProductSentResponse(boolean value) {
                                if(!value) {
                                    if (mProduct != null) {
                                        mProduct.setBarcode(mBarcode);
                                        mProduct.setName(name.getText().toString());
                                        mProduct.setImgupload_front(mProduct.getImgupload_front());
                                        mProduct.setImgupload_ingredients(mProduct.getImgupload_ingredients());
                                        mProduct.setImgupload_nutrition(mProduct.getImgupload_nutrition());
                                        mProduct.setStores(store.getText().toString());
                                        mProduct.setWeight(weight.getText().toString());
                                        mProduct.setWeight_unit(mUnit[0]);
                                        mProduct.save();
                                    }
                                    Toast.makeText(getApplicationContext(), R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.product_sent, Toast.LENGTH_LONG).show();
                                }
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("openOfflineEdit",true);
                                startActivity(intent);
                                finish();
                            }
                        });
            } else {
                if (mProduct != null) {
                    mProduct.setBarcode(mBarcode);
                    mProduct.setName(name.getText().toString());
                    mProduct.setImgupload_front(mProduct.getImgupload_front());
                    mProduct.setImgupload_ingredients(mProduct.getImgupload_ingredients());
                    mProduct.setImgupload_nutrition(mProduct.getImgupload_nutrition());
                    mProduct.setStores(store.getText().toString());
                    mProduct.setWeight(weight.getText().toString());
                    mProduct.setWeight_unit(mUnit[0]);
                    mProduct.save();
                }
                Toast.makeText(getApplicationContext(), R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("openOfflineEdit",true);
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.txtPictureNeeded, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.buttonTakePicture)
    protected void onTakePhotoClicked() {
        EasyImage.openCamera(this, 0);
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
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                //Handle the image
                onPhotoReturned(imageFile);
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
            imgSave.setVisibility(View.VISIBLE);
        } else if(spinnerI.getSelectedItemPosition() == 1) {
            mProduct.setImgupload_nutrition(photoFile.getAbsolutePath());
            imgSave.setVisibility(View.VISIBLE);
        } else {
            mProduct.setImgupload_ingredients(photoFile.getAbsolutePath());
            imgSave.setVisibility(View.VISIBLE);
        }
        Picasso.with(this)
                .load(photoFile)
                .fit()
                .centerCrop()
                .into(imgSave);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utils.MY_PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
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
