package openfoodfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.images.ImageNameJsonParser;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.FragmentUtils;
import openfoodfacts.github.scrachx.openfood.views.FullScreenActivityOpener;
import openfoodfacts.github.scrachx.openfood.views.adapters.ImagesAdapter;

/**
 * @author prajwalm
 * @see R.layout#fragment_product_photos
 */
public class ProductPhotosFragment extends BaseFragment implements ImagesAdapter.OnImageClickInterface {
    private OpenFoodAPIClient openFoodAPIClient;
    private Product product;
    private final CompositeDisposable disp = new CompositeDisposable();
    private RecyclerView imagesRecycler;
    private ImagesAdapter adapter;
    // A Array list to store image names
    private List<String> imageNames;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        openFoodAPIClient = new OpenFoodAPIClient(requireActivity());
        return inflater.inflate(R.layout.fragment_product_photos, container, false);
    }

    @Override
    public void onDestroy() {
        disp.dispose();
        super.onDestroy();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        State state = FragmentUtils.requireStateFromArguments(this);
        product = state.getProduct();
        // initialize the arraylist
        imageNames = new ArrayList<>();
        imagesRecycler = view.findViewById(R.id.imagesRecycler);

        disp.add(openFoodAPIClient.getRawAPI().getProductImagesSingle(product.getCode())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(node -> {
                imageNames = ImageNameJsonParser.extractImagesNameSortedByUploadTimeDesc(node);

                //Check if user is logged in
                adapter = new ImagesAdapter(getContext(), imageNames, product.getCode(), ProductPhotosFragment.this, product, isUserLoggedIn());

                imagesRecycler.setAdapter(adapter);
                imagesRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
            }, e -> Log.e(ProductPhotosFragment.class.getSimpleName(), "cannot download images from server", e)));
    }

    /**
     * Call an intent to open full screen activity for a given image
     *
     * @param mUrlImage url of the image in FullScreenImage
     */
    public void openFullScreen(String mUrlImage) {
        if (mUrlImage != null) {
            FullScreenActivityOpener.openZoom(requireActivity(), mUrlImage, null);
        }
    }

    /**
     * retrieves url of the imae clicked to open FullScreenActivity
     *
     * @param position position of the image clicked
     */
    @Override
    public void onImageClick(int position) {

        String baseUrlString = BuildConfig.STATICURL + "/images/products/";
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






