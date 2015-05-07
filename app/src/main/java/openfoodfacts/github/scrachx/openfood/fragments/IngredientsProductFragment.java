package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.State;

/**
 * Created by scotscriven on 04/05/15.
 */
public class IngredientsProductFragment extends Fragment {

    TextView ingredientProduct, substanceProduct, traceProduct, additiveProduct, palmOilProduct, mayBeFromPalmOilProduct;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ingredients_product,container,false);

        ingredientProduct = (TextView) rootView.findViewById(R.id.textIngredientProduct);
        substanceProduct = (TextView) rootView.findViewById(R.id.textSubstanceProduct);
        traceProduct = (TextView) rootView.findViewById(R.id.textTraceProduct);
        additiveProduct = (TextView) rootView.findViewById(R.id.textAdditiveProduct);
        palmOilProduct = (TextView) rootView.findViewById(R.id.textPalmOilProduct);
        mayBeFromPalmOilProduct = (TextView) rootView.findViewById(R.id.textMayBeFromPalmOilProduct);

        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

        ingredientProduct.setText(getString(R.string.txtIngredients) + ' ' + state.getProduct().getIngredientsText());
        substanceProduct.setText(getString(R.string.txtSubstances) + ' ' + state.getProduct().getAllergens());
        traceProduct.setText(getString(R.string.txtTraces) + ' ' + state.getProduct().getTraces());
        additiveProduct.setText(getString(R.string.txtAdditives) + ' ' + state.getProduct().getAdditivesTags().toString());
        palmOilProduct.setText(getString(R.string.txtPalmOilProduct) + ' ' + state.getProduct().getIngredientsFromPalmOilTags().toString());
        mayBeFromPalmOilProduct.setText(getString(R.string.txtMayBeFromPalmOilProduct) + ' ' + state.getProduct().getIngredientsThatMayBeFromPalmOilTags().toString());

        return rootView;
    }
}
