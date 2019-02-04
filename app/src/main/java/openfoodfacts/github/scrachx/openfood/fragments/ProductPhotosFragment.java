package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

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
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;

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
import openfoodfacts.github.scrachx.openfood.views.adapters.ImagesAdapter;
import openfoodfacts.github.scrachx.openfood.views.product.ProductFragment;


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
        State state;
        if (intent != null && intent.getExtras() != null && intent.getExtras().getSerializable("state") != null) {
            state = (State) intent.getExtras().getSerializable("state");
        } else {
            state = ProductFragment.mState;
        }
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
                    JSONObject jsonObject = Utils.createJsonObject(response);

                    // a json object referring to images
                    JSONObject images = null;
                    try {
                        images = jsonObject.getJSONObject("product").getJSONObject("images");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (images != null) {
                        // loop through all the image names and store them in a array list
                        for (int i = 0; i < images.names().length(); i++) {

                            try {
                                // do not include images with contain nutrients,ingredients or other in their names
                                // as they are duplicate and do not load as well
                                if (images.names().getString(i).contains("n") ||
                                        images.names().getString(i).contains("f") ||
                                        images.names().getString(i).contains("i") ||
                                        images.names().getString(i).contains("o")) {

                                    continue;

                                }
                                imageNames.add(images.names().getString(i));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    }

                    //Check if user is logged in
                    SharedPreferences preferences = getActivity().getSharedPreferences("login", 0);
                    String login = preferences.getString("user", null);
                    if (login != null) {
                        adapter = new ImagesAdapter(getContext(), imageNames, product.getCode(), ProductPhotosFragment.this::onImageClick, product, true);
                    } else {
                        adapter = new ImagesAdapter(getContext(), imageNames, product.getCode(), ProductPhotosFragment.this::onImageClick, product, false);
                    }
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
        String barcodePattern = product.getCode();
        if (barcodePattern.length() > 8) {
            barcodePattern = new StringBuilder(product.getCode())
                    .insert(3, "/")
                    .insert(7, "/")
                    .insert(11, "/")
                    .toString();
        }

        String finalUrlString = baseUrlString + barcodePattern + "/" + imageNames.get(position) + ".jpg";

        imgMap.put("imgid", imageNames.get(position));
        imgMap.put("id", ProductImageField.OTHER.toString() + '_' + product.getLang());


        openFullScreen(finalUrlString);

    }


}






