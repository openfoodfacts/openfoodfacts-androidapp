package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
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

        ingredientProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtIngredients) + "</b>" + ' ' + state.getProduct().getIngredientsText()));
        substanceProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtSubstances) + "</b>" + ' ' + state.getProduct().getAllergens()));
        String traces;
        if(state.getProduct().getCategories() == null){
            traces = state.getProduct().getTraces();
        }else{
            traces = state.getProduct().getTraces().replace(",", ", ");
        }
        traceProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtTraces) + "</b>" + ' ' + traces));
        additiveProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtAdditives) + "</b>" + ' ' + state.getProduct().getAdditivesTags().toString().replace("[", "").replace("]","")));
        palmOilProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtPalmOilProduct) + "</b>" + ' ' + state.getProduct().getIngredientsFromPalmOilTags().toString().replace("[","").replace("]","")));
        mayBeFromPalmOilProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtMayBeFromPalmOilProduct) + "</b>" + ' ' + state.getProduct().getIngredientsThatMayBeFromPalmOilTags().toString().replace("[","").replace("]","")));

        return rootView;
    }
}
