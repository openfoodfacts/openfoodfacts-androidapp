package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrientLevelListAdapter;

/**
 * Created by scotscriven on 04/05/15.
 */
public class NutritionProductFragment extends Fragment {

    private ImageView img;
    private ListView lv;
    private ArrayList<NutrientLevelItem> levelItem;
    private NutrientLevelListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nutrition_product,container,false);

        img = (ImageView) rootView.findViewById(R.id.imageGrade);
        lv = (ListView) rootView.findViewById(R.id.listNutrientLevels);

        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

        String salt = state.getProduct().getNutrientLevels().getSalt();
        String fat = state.getProduct().getNutrientLevels().getFat();
        String sugars = state.getProduct().getNutrientLevels().getSugars();
        String saturedFat = state.getProduct().getNutrientLevels().getSaturatedFat();

        levelItem = new ArrayList<NutrientLevelItem>();
        levelItem.add(new NutrientLevelItem(salt,getImageLevel(salt)));
        levelItem.add(new NutrientLevelItem(fat,getImageLevel(fat)));
        levelItem.add(new NutrientLevelItem(sugars, getImageLevel(sugars)));
        levelItem.add(new NutrientLevelItem(saturedFat,getImageLevel(saturedFat)));
        System.out.println(levelItem.toString());

        adapter = new NutrientLevelListAdapter(rootView.getContext(),levelItem);
        lv.setAdapter(adapter);

        img.setImageResource(getImageGrade(state.getProduct().getNutritionGradeFr()));

        return rootView;
    }

    public int getImageGrade(String grade){
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
        return R.drawable.ic_error;
    }

    public int getImageLevel(String nutrient){
        if(nutrient.compareToIgnoreCase("moderate") == 0){
            return R.drawable.ic_circle_yellow;
        }else if(nutrient.compareToIgnoreCase("low") == 0){
            return R.drawable.ic_circle_green;
        }else if(nutrient.compareToIgnoreCase("high") == 0){
            return R.drawable.ic_circle_red;
        }
        return R.drawable.ic_error;
    }
}
