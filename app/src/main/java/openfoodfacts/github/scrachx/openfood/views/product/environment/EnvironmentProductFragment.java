package openfoodfacts.github.scrachx.openfood.views.product.environment;

import android.content.Intent;
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
import openfoodfacts.github.scrachx.openfood.views.product.ProductFragment;

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

    private State mState;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_environment_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        if(intent!=null && intent.getExtras()!=null && intent.getExtras().getSerializable("state")!=null)
            mState = (State) intent.getExtras().getSerializable("state");
        else
            mState = ProductFragment.mState;

        final Product product = mState.getProduct();
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

        refreshView(mState);
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);

    }

}
