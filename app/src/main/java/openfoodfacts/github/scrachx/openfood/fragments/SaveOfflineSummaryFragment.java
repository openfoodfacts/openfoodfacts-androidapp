package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.OTHER;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by jayanth on 8/3/18.
 */

public class SaveOfflineSummaryFragment extends BaseFragment {

    @BindView(R.id.imageViewFrontProductAddition)
    ImageView imageButtonFront;
    @BindView(R.id.textBarcodeProductAddition)
    TextView textBarcode;
    @BindView(R.id.textGenericNameProductAddition)
    EditText productName;
    @BindView(R.id.textQuantityProductAddition)
    EditText productQuantity;
    @BindView(R.id.textBrandProductAddition)
    EditText productBrand;
    @BindView(R.id.addPhotoLabelProductAddition)
    TextView addPhotoLabel;
    @BindView(R.id.buttonMorePicturesProductAddition)
    Button addMorePictures;
    @BindView(R.id.spinnerUnitWeightProductAddition)
    Spinner spinnerW;
    @BindView(R.id.message_container_card_view)
    CardView mContainerView;
    private String mUnit;
    private String mUrlImage;
    private boolean sendOther = false;
    private OpenFoodAPIClient api;
    private SaveOfflineSummaryFragment mFragment;
    private String barcode;
    private SendProduct mProduct;
    private SharedPreferences mSharedPref;
    private String[] items;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        api = new OpenFoodAPIClient(getActivity());
        mFragment = this;
        items = new String[]{getResources().getString(R.string.txtCamera), getResources().getString(R.string.txtGallery)};
        return createView(inflater, container, R.layout.fragment_offline_summary_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        final State state = (State) intent.getExtras().getSerializable("state");

        try {
            mProduct = (SendProduct) getArguments().getSerializable("sendProduct");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        final Product product = state.getProduct();

        mSharedPref = getActivity().getApplicationContext().getSharedPreferences("prefs", 0);
        boolean isMsgOnlyOnePhotoNecessaryDismissed = mSharedPref.getBoolean("is_msg_only_one_photo_necessary_dismissed", false);
        if (isMsgOnlyOnePhotoNecessaryDismissed) {
            mContainerView.setVisibility(View.GONE);
        }
        barcode = product.getCode();
        textBarcode.setText(bold(getString(R.string.txtBarcode)));
        textBarcode.append(" " + barcode);

        ArrayAdapter<CharSequence> adapterW = ArrayAdapter.createFromResource(getActivity(), R.array.units_array, R.layout.custom_spinner_item);
        adapterW.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerW.setAdapter(adapterW);

        if (isNotBlank(product.getImageUrl())) {
            addPhotoLabel.setVisibility(View.GONE);

            Picasso.with(view.getContext())
                    .load(product.getImageUrl())
                    .into(imageButtonFront);

            mUrlImage = product.getImageUrl();
        }

        if (mProduct != null) {
            if (isNotBlank(mProduct.getImgupload_front())) {
                addPhotoLabel.setVisibility(View.GONE);
                mUrlImage = mProduct.getImgupload_front();
                Picasso.with(getContext()).load("file://" + mUrlImage).config(Bitmap.Config.RGB_565).into(imageButtonFront);
            }
            productName.setText(mProduct.getName());
            productQuantity.setText(mProduct.getWeight());
            productBrand.setText(mProduct.getBrands());
            ArrayAdapter unitAdapter = (ArrayAdapter) spinnerW.getAdapter();
            int spinnerPosition = unitAdapter.getPosition(mProduct.getWeight_unit());
            spinnerW.setSelection(spinnerPosition);

        } else {
            mProduct = new SendProduct();
        }

        // if the device does not have a camera, hide the button
        try {
            if (!Utils.isHardwareCameraInstalled(getContext())) {
                addMorePictures.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), e.toString());
        }
        mProduct.setLang(Locale.getDefault().getLanguage());
    }


    @OnItemSelected(value = R.id.spinnerUnitWeightProductAddition, callback = OnItemSelected.Callback.ITEM_SELECTED)
    protected void onUnitSelected(int pos) {
        mUnit = spinnerW.getItemAtPosition(pos).toString();
    }


    @OnClick(R.id.message_dismiss_icon)
    protected void onMessageDismissClicked() {
        mContainerView.setVisibility(View.GONE);
        mSharedPref.edit().putBoolean("is_msg_only_one_photo_necessary_dismissed", true).apply();
    }


    @OnClick(R.id.imageViewFrontProductAddition)
    public void openFullScreen(View v) {
        if (mUrlImage != null) {
            Intent intent = new Intent(v.getContext(), FullScreenImage.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageurl", mUrlImage);
            intent.putExtras(bundle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), (View) imageButtonFront,
                                getActivity().getString(R.string.product_transition));
                startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }
        } else {
            // take a picture
            if (ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                sendOther = false;
                if (Utils.isHardwareCameraInstalled(getContext())) {
                    EasyImage.openCamera(this, 0);
                } else {
                    EasyImage.openGallery(getActivity(), 0, false);
                }
            }
        }
    }

    @OnClick(R.id.buttonMorePicturesProductAddition)
    public void takeMorePicture() {
        try {
            if (Utils.isHardwareCameraInstalled(getContext())) {
                if (ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                } else {
                    sendOther = true;
                    EasyImage.openCamera(this, 0);/*
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    startActivity(intent);*/
                }
            } else {
                if (ContextCompat.checkSelfPermission(this.getContext(), READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this.getContext(), WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this.getActivity(), new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE);
                } else {
                    sendOther = true;
                    EasyImage.openGallery(this, 0, false);
                }
            }
            if (ContextCompat.checkSelfPermission(this.getContext(), READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this.getContext(), WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), READ_EXTERNAL_STORAGE)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), WRITE_EXTERNAL_STORAGE)) {
                    new MaterialDialog.Builder(this.getContext())
                            .title(R.string.action_about)
                            .content(R.string.permission_storage)
                            .neutralText(R.string.txtOk)
                            .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(this.getActivity(), new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE))
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this.getActivity(), new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE);
                }
            }
        } catch (NullPointerException e) {
            Log.i(getClass().getSimpleName(), e.toString());
        }
    }

/*
    private void openImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.txtOptions);
        builder.setItems(items, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    EasyImage.openCamera(SaveOfflineSummaryFragment.this, 0);
                    break;
                case 1:
                    EasyImage.openGallery(SaveOfflineSummaryFragment.this, 0, false);
                    break;
            }
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }*/


    private void onPhotoReturned(File photoFile) {
        ProductImage image = new ProductImage(barcode, FRONT, photoFile);
        image.setFilePath(photoFile.getAbsolutePath());
        api.postImg(getContext(), image);
        addPhotoLabel.setVisibility(View.GONE);
        mUrlImage = photoFile.getAbsolutePath();
        mProduct.setImgupload_front(mUrlImage);

        Picasso.with(getContext()).load(photoFile).fit().into(imageButtonFront);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                if (!sendOther) {
                    onPhotoReturned(new File(resultUri.getPath()));
                } else {
                    ProductImage image = new ProductImage(barcode, OTHER, new File(resultUri.getPath()));
                    image.setFilePath(resultUri.getPath());
                    api.postImg(getContext(), image);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e("corp image error", result.getError().toString());
            }
        }
        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                CropImage.activity(Uri.fromFile(imageFiles.get(0))).setAllowFlipping(false)
                        .setOutputUri(Utils.getOutputPicUri(getContext())).start(getContext(), mFragment);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(getContext());
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }

    public SendProduct getProduct() {
        Utils.hideKeyboard(getActivity());
        mProduct.setBarcode(barcode);
        mProduct.setName(productName.getText().toString());
        mProduct.setWeight(productQuantity.getText().toString());
        mProduct.setWeight_unit(mUnit);
        mProduct.setBrands(productBrand.getText().toString());
        final SharedPreferences settingsUser = getActivity().getSharedPreferences("login", 0);
        String login = settingsUser.getString("user", "");
        String password = settingsUser.getString("pass", "");
        if (!login.isEmpty() && !password.isEmpty()) {
            mProduct.setUserId(login);
            mProduct.setPassword(password);
        }
        return mProduct;
    }
}
