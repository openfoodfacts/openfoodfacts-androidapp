package openfoodfacts.github.scrachx.openfood.views.product.environment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_environment_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        refreshView((State) intent.getExtras().getSerializable("state"));
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);

        final Product product = state.getProduct();
        Nutriments nutriments = product.getNutriments();

        if(nutriments != null) {
            Nutriments.Nutriment carbonFootprintNutriment = nutriments.get(Nutriments.CARBON_FOOTPRINT);
            carbonFootprint.setText(bold(getString(R.string.textCarbonFootprint)));
            carbonFootprint.append(carbonFootprintNutriment.getFor100g());
            carbonFootprint.append(carbonFootprintNutriment.getUnit());
        }
    }

}
