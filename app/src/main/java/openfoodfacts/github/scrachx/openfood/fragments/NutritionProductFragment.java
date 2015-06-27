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
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevels;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrientLevelListAdapter;

/**
 * Created by scotscriven on 04/05/15.
 */
public class NutritionProductFragment extends Fragment {

    private ImageView img;
    private ListView lv;
    private TextView serving;
    private ArrayList<NutrientLevelItem> levelItem;
    private NutrientLevelListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nutrition_product,container,false);

        img = (ImageView) rootView.findViewById(R.id.imageGrade);
        lv = (ListView) rootView.findViewById(R.id.listNutrientLevels);
        serving = (TextView) rootView.findViewById(R.id.textServingSize);

        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

        NutrientLevels nt = state.getProduct().getNutrientLevels();
        if(nt == null){
            levelItem = new ArrayList<NutrientLevelItem>();
            levelItem.add(new NutrientLevelItem(getString(R.string.txtNoData),R.drawable.error_image));
            adapter = new NutrientLevelListAdapter(rootView.getContext(),levelItem);
            lv.setAdapter(adapter);
        }else{
            String saltTxt = Html.fromHtml("<b>" + getString(R.string.txtSalt) + "</b>" + ' ' + nt.getSalt() + " (" + state.getProduct().getNutriments().getSalt100g() + state.getProduct().getNutriments().getSaltUnit() + ")").toString();
            String fatTxt = Html.fromHtml("<b>" + getString(R.string.txtFat) + "</b>" + ' ' + nt.getFat() + " (" + state.getProduct().getNutriments().getFat100g() + state.getProduct().getNutriments().getFatUnit() + ")").toString();
            String sugarsTxt = Html.fromHtml("<b>" + getString(R.string.txtSugars) + "</b>" + ' ' + nt.getSugars() + " (" + state.getProduct().getNutriments().getSugars100g() + state.getProduct().getNutriments().getSugarsUnit() + ")").toString();
            String saturatedFatTxt = Html.fromHtml("<b>" + getString(R.string.txtSaturatedFat) + "</b>" + ' ' + nt.getSaturatedFat() + " (" + state.getProduct().getNutriments().getSaturatedFat100g() + state.getProduct().getNutriments().getSaturatedFatUnit() + ")").toString();

            String saltImg = nt.getSalt();
            String fatImg = nt.getFat();
            String sugarsImg = nt.getSugars();
            String saturatedFatImg = nt.getSaturatedFat();

            levelItem = new ArrayList<NutrientLevelItem>();
            levelItem.add(new NutrientLevelItem(saltTxt,getImageLevel(saltImg)));
            levelItem.add(new NutrientLevelItem(fatTxt,getImageLevel(fatImg)));
            levelItem.add(new NutrientLevelItem(sugarsTxt, getImageLevel(sugarsImg)));
            levelItem.add(new NutrientLevelItem(saturatedFatTxt,getImageLevel(saturatedFatImg)));

            adapter = new NutrientLevelListAdapter(rootView.getContext(),levelItem);
            lv.setAdapter(adapter);
            img.setImageResource(getImageGrade(state.getProduct().getNutritionGradeFr()));
        }

        serving.setText(Html.fromHtml("<b>" + getString(R.string.txtServingSize) + "</b>" + ' ' + state.getProduct().getServingSize()));

        return rootView;
    }

    public int getImageGrade(String grade){
        if(grade != null){
            grade.toLowerCase();
            if(grade.compareToIgnoreCase("a") == 0){
                return R.drawable.nnc_a;
            }else if(grade.compareToIgnoreCase("b") == 0){
                return R.drawable.nnc_b;
            }else if(grade.compareToIgnoreCase("c") == 0){
                return R.drawable.nnc_c;
            }else if(grade.compareToIgnoreCase("d") == 0){
                return R.drawable.nnc_d;
            }else if(grade.compareToIgnoreCase("e") == 0){
                return R.drawable.nnc_e;
            }
        }
        return R.drawable.ic_error;
    }

    public int getImageLevel(String nutrient){
        if(nutrient != null){
            if(nutrient.compareToIgnoreCase("moderate") == 0){
                return R.drawable.ic_circle_yellow;
            }else if(nutrient.compareToIgnoreCase("low") == 0){
                return R.drawable.ic_circle_green;
            }else if(nutrient.compareToIgnoreCase("high") == 0){
                return R.drawable.ic_circle_red;
            }
        }
        return R.drawable.ic_error;
    }
}
