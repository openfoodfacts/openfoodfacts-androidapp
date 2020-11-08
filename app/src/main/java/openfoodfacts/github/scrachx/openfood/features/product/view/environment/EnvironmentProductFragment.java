package openfoodfacts.github.scrachx.openfood.features.product.view.environment;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.io.File;

import io.reactivex.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentEnvironmentProductBinding;
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener;
import openfoodfacts.github.scrachx.openfood.features.ImagesManageActivity;
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity;
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity;
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductState;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.FragmentUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.PACKAGING;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class EnvironmentProductFragment extends BaseFragment {
    private static final int EDIT_PRODUCT_AFTER_LOGIN_REQUEST_CODE = 1;
    private CompositeDisposable disp;
    private PhotoReceiverHandler photoReceiverHandler;
    private FragmentEnvironmentProductBinding binding;
    private ProductState activityProductState;
    private String mUrlImage;
    private String barcode;
    private OpenFoodAPIClient api;
    /**
     * boolean to determine if image should be loaded or not
     **/
    private boolean isLowBatteryMode = false;
    private Product product;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disp = new CompositeDisposable();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        api = new OpenFoodAPIClient(requireActivity());
        binding = FragmentEnvironmentProductBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        disp.dispose();
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photoReceiverHandler = new PhotoReceiverHandler(this::loadPackagingPhoto);
        String langCode = LocaleHelper.getLanguage(getContext());
        activityProductState = FragmentUtils.requireStateFromArguments(this);

        binding.imageViewPackaging.setOnClickListener(this::openFullScreen);

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        if (Utils.isDisableImageLoad(requireContext()) && Utils.isBatteryLevelLow(requireContext())) {
            isLowBatteryMode = true;
        }

        product = activityProductState.getProduct();
        barcode = product.getCode();
        Nutriments nutriments = product.getNutriments();

        if (isNotBlank(product.getImagePackagingUrl(langCode))) {
            binding.packagingImagetipBox.setTipMessage(getString(R.string.onboarding_hint_msg, getString(R.string.image_edit_tip)));
            binding.packagingImagetipBox.loadToolTip();
            binding.addPhotoLabel.setVisibility(View.GONE);

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Utils.picassoBuilder(getContext())
                    .load(product.getImagePackagingUrl(langCode))
                    .into(binding.imageViewPackaging);
            } else {
                binding.imageViewPackaging.setVisibility(View.GONE);
            }
            mUrlImage = product.getImagePackagingUrl(langCode);
        }

        if (nutriments.contains(Nutriments.CARBON_FOOTPRINT)) {
            Nutriments.Nutriment carbonFootprintNutriment = nutriments.get(Nutriments.CARBON_FOOTPRINT);
            binding.textCarbonFootprint.setText(bold(getString(R.string.textCarbonFootprint)));
            binding.textCarbonFootprint.append(carbonFootprintNutriment.getFor100gInUnits());
            binding.textCarbonFootprint.append(carbonFootprintNutriment.getUnit());
        } else {
            binding.carbonFootprintCv.setVisibility(View.GONE);
        }

        if (product.getEnvironmentInfocard() != null && !product.getEnvironmentInfocard().isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.environmentInfoText.append(Html.fromHtml(product.getEnvironmentInfocard(), Html.FROM_HTML_MODE_COMPACT));
            } else {
                binding.environmentInfoText.append(Html.fromHtml(product.getEnvironmentInfocard()));
            }
        } else {
            binding.environmentInfoCv.setVisibility(View.GONE);
        }

        if (product.getRecyclingInstructionsToDiscard() != null && !product.getRecyclingInstructionsToDiscard().isEmpty()) {
            binding.recyclingInstructionToDiscard.setText(bold("Recycling instructions - To discard: "));
            binding.recyclingInstructionToDiscard.append(product.getRecyclingInstructionsToDiscard());
        } else {
            binding.recyclingInstructionsDiscardCv.setVisibility(View.GONE);
        }

        if (product.getRecyclingInstructionsToRecycle() != null && !product.getRecyclingInstructionsToRecycle().isEmpty()) {
            binding.recyclingInstructionToRecycle.setText(bold("Recycling instructions - To recycle:"));
            binding.recyclingInstructionToRecycle.append(product.getRecyclingInstructionsToRecycle());
        } else {
            binding.recyclingInstructionsRecycleCv.setVisibility(View.GONE);
        }

        refreshView(activityProductState);
    }

    @Override
    public void refreshView(ProductState productState) {
        super.refreshView(productState);
        activityProductState = productState;
    }

    private void openFullScreen(View v) {
        if (mUrlImage != null && activityProductState != null && activityProductState.getProduct() != null) {
            FullScreenActivityOpener.openForUrl(this, activityProductState.getProduct(), PACKAGING, mUrlImage, binding.imageViewPackaging);
        } else {
            newPackagingImage();
        }
    }

    public void newPackagingImage() {
        doChooseOrTakePhotos(getString(R.string.recycling_picture));
    }

    public void loadPackagingPhoto(File photoFile) {
        // Create a new instance of ProductImage so we can load to server
        ProductImage image = new ProductImage(barcode, PACKAGING, photoFile);
        image.setFilePath(photoFile.getAbsolutePath());
        // Load to server
        disp.add(api.postImg(image).subscribe());
        // Load into view
        binding.addPhotoLabel.setVisibility(View.GONE);
        mUrlImage = photoFile.getAbsolutePath();

        Picasso.get()
            .load(photoFile)
            .fit()
            .into(binding.imageViewPackaging);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data);
        if (requestCode == EDIT_PRODUCT_AFTER_LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK && isUserLoggedIn()) {
            startEditProduct();
        }
        if (ImagesManageActivity.isImageModified(requestCode, resultCode)
            && getActivity() instanceof ProductViewActivity) {
            ((ProductViewActivity) getActivity()).onRefresh();
        }
    }

    private void startEditProduct() {
        Intent intent = new Intent(getActivity(), ProductEditActivity.class);
        intent.putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, product);
        startActivity(intent);
    }
}
