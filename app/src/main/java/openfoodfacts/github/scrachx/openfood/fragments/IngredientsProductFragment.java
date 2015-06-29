package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.State;

/**
 * Created by scotscriven on 04/05/15.
 */
public class IngredientsProductFragment extends Fragment {

    TextView ingredientProduct, substanceProduct, traceProduct, additiveProduct;

    private TextView palmOilProduct, mayBeFromPalmOilProduct;
    private ImageView imageOkNo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ingredients_product,container,false);

        ingredientProduct = (TextView) rootView.findViewById(R.id.textIngredientProduct);
        substanceProduct = (TextView) rootView.findViewById(R.id.textSubstanceProduct);
        traceProduct = (TextView) rootView.findViewById(R.id.textTraceProduct);
        additiveProduct = (TextView) rootView.findViewById(R.id.textAdditiveProduct);

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

        // Code for palm oil (and additional labels/awards/certs, in the future)
        palmOilProduct = (TextView) rootView.findViewById(R.id.textPalmOilProduct);
        mayBeFromPalmOilProduct = (TextView) rootView.findViewById(R.id.textMayBeFromPalmOilProduct);
        imageOkNo = (ImageView) rootView.findViewById(R.id.imageOkNo);

        if(state.getProduct().getIngredientsFromPalmOilN() == 0 && state.getProduct().getIngredientsFromOrThatMayBeFromPalmOilN() == 0){
            imageOkNo.setImageResource(R.drawable.ok);
            palmOilProduct.setVisibility(View.VISIBLE);
            palmOilProduct.setText(getString(R.string.txtPalm));
        } else {
            imageOkNo.setImageResource(R.drawable.no);
            if(!state.getProduct().getIngredientsFromPalmOilTags().toString().replace("[", "").replace("]", "").isEmpty()) {
                palmOilProduct.setVisibility(View.VISIBLE);
                palmOilProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtPalmOilProduct) + "</b>" + ' ' + state.getProduct().getIngredientsFromPalmOilTags().toString().replace("[", "").replace("]", "")));
            }
            if(!state.getProduct().getIngredientsThatMayBeFromPalmOilTags().toString().replace("[", "").replace("]", "").isEmpty()) {
                mayBeFromPalmOilProduct.setVisibility(View.VISIBLE);
                mayBeFromPalmOilProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtMayBeFromPalmOilProduct) + "</b>" + ' ' + state.getProduct().getIngredientsThatMayBeFromPalmOilTags().toString().replace("[","").replace("]","")));
            }
        }
        

        return rootView;
    }
}
