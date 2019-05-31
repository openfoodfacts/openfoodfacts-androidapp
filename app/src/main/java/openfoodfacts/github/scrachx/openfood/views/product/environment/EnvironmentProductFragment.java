package openfoodfacts.github.scrachx.openfood.views.product.environment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;

public class EnvironmentProductFragment extends BaseFragment {

    @BindView(R.id.textCarbonFootprint)
    TextView carbonFootprint;
    @BindView(R.id.environment_info_text)
    TextView environmentInfoText;
    @BindView(R.id.recyclingInstructionToDiscard)
    TextView recyclingInstructionToDiscardText;
    @BindView(R.id.recyclingInstructionToRecycle)
    TextView recyclingInstructionToRecycleText;
    @BindView(R.id.recycling_instructions_discard_cv)
    CardView recyclingInstructionsToDiscardCv;
    @BindView(R.id.recycling_instructions_recycle_cv)
    CardView recyclingInstructionsToRecycleCv;
    @BindView(R.id.carbon_footprint_cv)
    CardView carbonFootprintCardView;

    private State activityState;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_environment_product);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activityState =getStateFromActivityIntent();

        final Product product = activityState.getProduct();
        Nutriments nutriments = product.getNutriments();

        if(nutriments != null && nutriments.contains(Nutriments.CARBON_FOOTPRINT)) {
            Nutriments.Nutriment carbonFootprintNutriment = nutriments.get(Nutriments.CARBON_FOOTPRINT);
            carbonFootprint.setText(bold(getString(R.string.textCarbonFootprint)));
            carbonFootprint.append(carbonFootprintNutriment.getFor100gInUnits());
            carbonFootprint.append(carbonFootprintNutriment.getUnit());
        } else {
            carbonFootprintCardView.setVisibility(View.GONE);
        }

        if (product.getEnvironmentInfocard() != null && !product.getEnvironmentInfocard().isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                environmentInfoText.append(Html.fromHtml(product.getEnvironmentInfocard(), Html.FROM_HTML_MODE_COMPACT));
            } else {
                environmentInfoText.append(Html.fromHtml(product.getEnvironmentInfocard()));
            }
        }

        if(product.getRecyclingInstructionsToDiscard() != null && !product.getRecyclingInstructionsToDiscard().isEmpty()) {
            recyclingInstructionToDiscardText.setText(bold("Recycling instructions - To discard: "));
            recyclingInstructionToDiscardText.append(product.getRecyclingInstructionsToDiscard());
        } else {
            recyclingInstructionsToDiscardCv.setVisibility(View.GONE);
        }

        if (product.getRecyclingInstructionsToRecycle() != null && !product.getRecyclingInstructionsToRecycle().isEmpty()) {
            recyclingInstructionToRecycleText.setText(bold("Recycling instructions - To recycle:"));
            recyclingInstructionToRecycleText.append(product.getRecyclingInstructionsToRecycle());
        } else {
            recyclingInstructionsToRecycleCv.setVisibility(View.GONE);
        }

        refreshView(activityState);
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);
        activityState = state;
    }

}
