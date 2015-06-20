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
 * Created by scotscriven on 09/05/15.
 */
public class PalmProductFragment extends Fragment {

    private TextView palmOilProduct, mayBeFromPalmOilProduct;
    private ImageView imageOkNo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_palm_product,container,false);

        palmOilProduct = (TextView) rootView.findViewById(R.id.textPalmOilProduct);
        mayBeFromPalmOilProduct = (TextView) rootView.findViewById(R.id.textMayBeFromPalmOilProduct);
        imageOkNo = (ImageView) rootView.findViewById(R.id.imageOkNo);

        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

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
