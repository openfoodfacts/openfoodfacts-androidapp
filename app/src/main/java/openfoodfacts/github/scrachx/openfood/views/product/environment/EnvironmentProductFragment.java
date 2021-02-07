package openfoodfacts.github.scrachx.openfood.views.product.environment;

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
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductState;
import openfoodfacts.github.scrachx.openfood.utils.FragmentUtils;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;

public class EnvironmentProductFragment extends BaseFragment {
    private FragmentEnvironmentProductBinding binding;
    private ProductState activityProductState;

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
        activityProductState = FragmentUtils.requireStateFromArguments(this);

        final Product product = activityProductState.getProduct();
        Nutriments nutriments = product.getNutriments();

        if (nutriments != null && nutriments.contains(Nutriments.CARBON_FOOTPRINT)) {
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
}
