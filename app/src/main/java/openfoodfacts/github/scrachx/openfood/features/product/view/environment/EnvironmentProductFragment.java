package openfoodfacts.github.scrachx.openfood.features.product.view.environment;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentEnvironmentProductBinding;
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener;
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductState;
import openfoodfacts.github.scrachx.openfood.utils.FragmentUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.PACKAGING;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class EnvironmentProductFragment extends BaseFragment {
    private FragmentEnvironmentProductBinding binding;
    private ProductState activityProductState;
    private String mUrlImage;
    /**
     * boolean to determine if image should be loaded or not
     **/
    private boolean isLowBatteryMode = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEnvironmentProductBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String langCode = LocaleHelper.getLanguage(getContext());
        activityProductState = FragmentUtils.requireStateFromArguments(this);

        binding.imageViewPackaging.setOnClickListener(v -> openFullScreen());

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        if (Utils.isDisableImageLoad(requireContext()) && Utils.isBatteryLevelLow(requireContext())) {
            isLowBatteryMode = true;
        }

        final Product product = activityProductState.getProduct();
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

    private void openFullScreen() {
        if (mUrlImage != null && activityProductState != null && activityProductState.getProduct() != null) {
            FullScreenActivityOpener.openForUrl(this, activityProductState.getProduct(), PACKAGING, mUrlImage, binding.imageViewPackaging);
        } else {
            newPackagingImage();
        }
    }

    public void newPackagingImage() {
        doChooseOrTakePhotos(getString(R.string.recycling_picture));
    }
}
