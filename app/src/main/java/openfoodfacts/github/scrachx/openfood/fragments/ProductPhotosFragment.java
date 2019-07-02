package openfoodfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.FullScreenActivityOpener;
import openfoodfacts.github.scrachx.openfood.views.adapters.ImagesAdapter;

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
        imagesRecycler = view.findViewById(R.id.imagesRecycler);

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
            FullScreenActivityOpener.openZoom(this,mUrlImage,null);
        }
    }

    @Override
    public void onImageClick(int position) {

        String baseUrlString = BuildConfig.STATICURL+ "/images/products/";
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






