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
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author prajwalm
 */
public class ProductPhotosFragment extends BaseFragment implements ImagesAdapter.OnImageClickInterface {
    private OpenFoodAPIClient openFoodAPIClient;
    private Product product;
    // A Array list to store image names
    private ArrayList<String> imageNames;
    private RecyclerView imagesRecycler;
    private ImagesAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        openFoodAPIClient = new OpenFoodAPIClient(getActivity());
        return createView(inflater, container, R.layout.fragment_product_photos);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        State state=getStateFromActivityIntent();
        product = state.getProduct();
        // initialize the arraylist
        imageNames = new ArrayList<>();
        imagesRecycler = view.findViewById(R.id.images_recycler);

        openFoodAPIClient.getImages(product.getCode(), (value, response) -> {

            if (value && response != null) {

                // a json object referring to base json object
                JSONObject jsonObject = Utils.createJsonObject(response);

                // a json object referring to images
                JSONObject images = null;
                try {
                    images = jsonObject.getJSONObject("product").getJSONObject("images");
                } catch (JSONException e) {
                    Log.w(ProductPhotosFragment.class.getSimpleName(), "can't get product / images in json", e);
                }
                if (images != null) {
                    final JSONArray names = images.names();
                    if (names != null) {
                        // loop through all the image names and store them in a array list
                        for (int i = 0; i < names.length(); i++) {
                            try {
                                // do not include images with contain nutrients,ingredients or other in their names
                                // as they are duplicate and do not load as well
                                final String namesString = names.getString(i);
                                if (namesString.contains("n") ||
                                    namesString.contains("f") ||
                                    namesString.contains("i") ||
                                    namesString.contains("o")) {

                                    continue;
                                }
                                imageNames.add(namesString);
                            } catch (JSONException e) {
                                Log.w(ProductPhotosFragment.class.getSimpleName(), "can't get product / images in json", e);
                            }
                        }
                    }
                }

                //Check if user is logged in
                adapter = new ImagesAdapter(getContext(), imageNames, product.getCode(), ProductPhotosFragment.this::onImageClick, product, isUserLoggedIn());
                imagesRecycler.setAdapter(adapter);
                imagesRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
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
        openFullScreen(baseUrlString + barcodePattern + "/" + imageNames.get(position) + ".jpg");
    }
}






