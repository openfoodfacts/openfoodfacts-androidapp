package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.adapters.ImagesAdapter;


/**
 * @author prajwalm
 */

public class ProductPhotosFragment extends BaseFragment implements ImagesAdapter.OnImageClickInterface {

    private OpenFoodAPIClient openFoodAPIClient;
    private Product product;
    private ProductPhotosFragment mFragment;
    // A Array list to store image names
    private ArrayList<String> imageNames;
    private RecyclerView imagesRecycler;
    private ImagesAdapter adapter;
    private HashMap<String, String> imgMap;


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
        // initialize the arraylist
        imageNames = new ArrayList<>();
        imagesRecycler = view.findViewById(R.id.images_recycler);
        imgMap = new HashMap<>();


        openFoodAPIClient.getImages(product.getCode(), new OpenFoodAPIClient.OnImagesCallback() {
            @Override
            public void onImageResponse(boolean value, String response) {

                if (value && response != null) {

                    // a json object referring to base json object
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // a json object referring to images
                    JSONObject images = null;
                    try {
                        images = jsonObject.getJSONObject("product").getJSONObject("images");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    // loop through all the image names and store them in a array list
                    for (int i = 0; i < images.names().length(); i++) {

                        try {
                            imageNames.add(images.names().getString(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                    adapter = new ImagesAdapter(getContext(), imageNames, product.getCode(), ProductPhotosFragment.this::onImageClick);
                    imagesRecycler.setAdapter(adapter);
                    imagesRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));


                }


            }
        });

    }

    public void openFullScreen(String mUrlImage) {
        if (mUrlImage != null) {
            Intent intent = new Intent(getContext(), FullScreenImage.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageurl", mUrlImage);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    public void onImageClick(int position) {

        String baseUrlString = "https://static.openfoodfacts.org/images/products/";
        String barcodePattern = new StringBuilder(product.getCode())
                .insert(3, "/")
                .insert(7, "/")
                .insert(11, "/")
                .toString();
        String finalUrlString = baseUrlString + barcodePattern + "/" + imageNames.get(position) + ".jpg";

        imgMap.put("imgid", imageNames.get(position));
        imgMap.put("id", ProductImageField.OTHER.toString() + '_' + product.getLang());
       // imgMap.put("angle", "90");
       // imgMap.put("type","edit");
/*
        RequestBody barcode = RequestBody.create(MediaType.parse("text/plain"), product.getCode());
        RequestBody imageField = RequestBody.create(MediaType.parse("text/plain"), ProductImageField.INGREDIENTS.toString() + '_' + product.getLang());
        // RequestBody image = RequestBody.create(MediaType.parse("image/*"), finalUrlString);

        imgMap.put("code", barcode);
        imgMap.put("imagefield", imageField);
        //imgMap.put("imgupload_ingredients", image);

        // Attribute the upload to the connected user
        final SharedPreferences settings = getActivity().getSharedPreferences("login", 0);
        final String login = settings.getString("user", "");
        final String password = settings.getString("pass", "");
        if (!login.isEmpty() && !password.isEmpty()) {
            imgMap.put("user_id", RequestBody.create(MediaType.parse("text/plain"), login));
            imgMap.put("password", RequestBody.create(MediaType.parse("text/plain"), password));
        }*/

        /*openFoodAPIClient.editImage(product.getCode(), imgMap, new OpenFoodAPIClient.OnEditImageCallback() {
            @Override
            public void onEditResponse(boolean value, String response) {
                Toast.makeText(getContext(), response, Toast.LENGTH_LONG).show();
            }
        });*/


         openFullScreen(finalUrlString);

    }

}






        /*
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
    */


