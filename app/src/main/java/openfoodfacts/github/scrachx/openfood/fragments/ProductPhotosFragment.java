package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.splash.ISplashPresenter;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.INGREDIENTS;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.NUTRITION;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.prependIfMissing;


public class ProductPhotosFragment extends BaseFragment {

    private OpenFoodAPIClient openFoodAPIClient;
    private Product product;
    @BindView(R.id.imageProductTwo)
    ImageView imageTwo;
    @BindView(R.id.imageProductThree)
    ImageView imageThree;
    @BindView(R.id.imageProductViewFront)
    ImageView imageFront;
    private ProductPhotosFragment mFragment;
    private String mUrlImageFront;
    private String mUrlImageTwo;
    private String mUrlImageThree;
    private int count;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        openFoodAPIClient = new OpenFoodAPIClient(getActivity());
        return createView(inflater, container, R.layout.fragment_product_photos);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        final State state = (State) intent.getExtras().getSerializable("state");
        product = state.getProduct();
        mFragment = this;

        if (isNotBlank(product.getImageFrontUrl())) {

            Picasso.with(view.getContext()).
                    load(product.getImageFrontUrl()).
                    into(imageFront);
            mUrlImageFront = product.getImageFrontUrl();

        }

        if (isNotBlank(product.getImageIngredientsUrl())) {

            Picasso.with(view.getContext()).
                    load(product.getImageIngredientsUrl()).
                    into(imageTwo);

            mUrlImageTwo = product.getImageIngredientsUrl();
        }

        if (isNotBlank(product.getImageNutritionUrl())) {

            Picasso.with(view.getContext()).
                    load(product.getImageNutritionUrl()).
                    into(imageThree);

            mUrlImageThree = product.getImageNutritionUrl();

        }

        imageFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFullScreen(view, mUrlImageFront);
                count = 0;
            }
        });

        imageTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFullScreen(view, mUrlImageTwo);
                count = 1;
            }
        });

        imageThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFullScreen(view, mUrlImageThree);
                count = 2;
            }
        });

    }


    public void openFullScreen(View v, String mUrlImage) {
        if (mUrlImage != null) {
            Intent intent = new Intent(v.getContext(), FullScreenImage.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageurl", mUrlImage);
            intent.putExtras(bundle);
            startActivity(intent);
        } else {
            // take a picture
            if (ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                EasyImage.openCamera(this, 0);
            }
        }
    }

    private void onPhotoReturned(File photoFile) {

        switch (count) {
            case 0:

                ProductImage imageOne = new ProductImage(product.getCode(), FRONT, photoFile);
                imageOne.setFilePath(photoFile.getAbsolutePath());
                openFoodAPIClient.postImg(getContext(), imageOne);
                mUrlImageFront = photoFile.getAbsolutePath();
                Picasso.with(getContext())
                        .load(photoFile)
                        .fit()
                        .into(imageFront);
                break;

            case 1:

                ProductImage imageIngredients = new ProductImage(product.getCode(), INGREDIENTS, photoFile);
                imageIngredients.setFilePath(photoFile.getAbsolutePath());
                openFoodAPIClient.postImg(getContext(), imageIngredients);
                mUrlImageTwo = photoFile.getAbsolutePath();
                Picasso.with(getContext())
                        .load(photoFile)
                        .fit()
                        .into(imageTwo);

                break;

            case 2:

                ProductImage imageNutrients = new ProductImage(product.getCode(), NUTRITION, photoFile);
                imageNutrients.setFilePath(photoFile.getAbsolutePath());
                openFoodAPIClient.postImg(getContext(), imageNutrients);
                mUrlImageThree = photoFile.getAbsolutePath();
                Picasso.with(getContext())
                        .load(photoFile)
                        .fit()
                        .into(imageThree);

                break;


        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                CropImage.activity(Uri.fromFile(imageFiles.get(0)))
                        .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
                        .setAllowFlipping(false)
                        .start(getContext(), mFragment);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length <= 0 || grantResults[0] != PERMISSION_GRANTED) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.permission_title)
                            .content(R.string.permission_denied)
                            .negativeText(R.string.txtNo)
                            .positiveText(R.string.txtYes)
                            .onPositive((dialog, which) -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .show();
                } else {
                    EasyImage.openCamera(this, 0);
                }
            }
        }
    }

    private void loadImage(ImageButton view, File photoFile) {
        Picasso.with(getContext())
                .load(photoFile)
                .fit()
                .into(view);
    }

}
