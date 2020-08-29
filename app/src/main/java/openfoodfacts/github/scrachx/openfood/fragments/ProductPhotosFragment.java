package openfoodfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentProductPhotosBinding;
import openfoodfacts.github.scrachx.openfood.images.ImageNameJsonParser;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductState;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.FragmentUtils;
import openfoodfacts.github.scrachx.openfood.views.FullScreenActivityOpener;
import openfoodfacts.github.scrachx.openfood.views.adapters.ImagesAdapter;

/**
 * @author prajwalm
 * @see R.layout#fragment_product_photos
 */
public class ProductPhotosFragment extends BaseFragment {
    private FragmentProductPhotosBinding binding;
    private OpenFoodAPIClient openFoodAPIClient;
    private CompositeDisposable disp;
    private ImagesAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        openFoodAPIClient = new OpenFoodAPIClient(requireActivity());
        disp = new CompositeDisposable();
        binding = FragmentProductPhotosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        disp.dispose();
        binding = null;
        super.onDestroy();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ProductState productState = FragmentUtils.requireStateFromArguments(this);
        Product product = productState.getProduct();

        disp.add(openFoodAPIClient.getRawAPI().getProductImages(product.getCode())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(node -> {
                List<String> imageNames = ImageNameJsonParser.extractImagesNameSortedByUploadTimeDesc(node);

                //Check if user is logged in
                adapter = new ImagesAdapter(requireActivity(), product, isUserLoggedIn(), imageNames, position -> {
                    // Retrieves url of the image clicked to open FullScreenActivity
                    String barcodePattern = product.getCode();
                    if (barcodePattern.length() > 8) {
                        barcodePattern = new StringBuilder(product.getCode())
                            .insert(3, "/")
                            .insert(7, "/")
                            .insert(11, "/")
                            .toString();
                    }
                    openFullScreen(String.format("%s/images/products/%s/%s.jpg", BuildConfig.STATICURL, barcodePattern, imageNames.get(position)));
                });

                binding.imagesRecycler.setAdapter(adapter);
                binding.imagesRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
            }, e -> Log.e(ProductPhotosFragment.class.getSimpleName(), "cannot download images from server", e)));
    }

    /**
     * Call an intent to open full screen activity for a given image
     *
     * @param mUrlImage url of the image in FullScreenImage
     */
    public void openFullScreen(@Nullable String mUrlImage) {
        if (mUrlImage != null) {
            FullScreenActivityOpener.openZoom(requireActivity(), mUrlImage, null);
        }
    }
}






