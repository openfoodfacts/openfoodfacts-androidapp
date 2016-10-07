package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevels;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrientLevelListAdapter;

public class NutritionProductFragment extends BaseFragment {

    @BindView(R.id.imageGrade) ImageView img;
    @BindView(R.id.listNutrientLevels) ListView lv;
    @BindView(R.id.textServingSize) TextView serving;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_nutrition_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

        final Product product = state.getProduct();
        NutrientLevels nt = product.getNutrientLevels();
        ArrayList<NutrientLevelItem> levelItem = new ArrayList<>();

        if (nt == null || (nt.getFat() == null && nt.getSalt() == null
                && nt.getSaturatedFat() == null && nt.getSugars() == null)) {
            levelItem.add(new NutrientLevelItem(getString(R.string.txtNoData), R.drawable.error_image));
        } else {

            String fatTxt = Html.fromHtml("<b>" + getString(R.string.txtFat) + "</b>" + ' ' + localiseNutritionLevel(nt.getFat()) + " (" + product.getNutriments().getFat100g() + product.getNutriments().getFatUnit() + ")").toString();
            String saturatedFatTxt = Html.fromHtml("<b>" + getString(R.string.txtSaturatedFat) + "</b>" + ' ' + localiseNutritionLevel(nt.getSaturatedFat()) + " (" + product.getNutriments().getSaturatedFat100g() + product.getNutriments().getSaturatedFatUnit() + ")").toString();
            String sugarsTxt = Html.fromHtml("<b>" + getString(R.string.txtSugars) + "</b>" + ' ' + localiseNutritionLevel(nt.getSugars()) + " (" + product.getNutriments().getSugars100g() + product.getNutriments().getSugarsUnit() + ")").toString();
            String saltTxt = Html.fromHtml("<b>" + getString(R.string.txtSalt) + "</b>" + ' ' + localiseNutritionLevel(nt.getSalt()) + " (" + product.getNutriments().getSalt100g() + product.getNutriments().getSaltUnit() + ")").toString();

            levelItem.add(new NutrientLevelItem(fatTxt, getImageLevel(nt.getFat())));
            levelItem.add(new NutrientLevelItem(saturatedFatTxt, getImageLevel(nt.getSaturatedFat())));
            levelItem.add(new NutrientLevelItem(sugarsTxt, getImageLevel(nt.getSugars())));
            levelItem.add(new NutrientLevelItem(saltTxt, getImageLevel(nt.getSalt())));

            img.setImageResource(getImageGrade(product.getNutritionGradeFr()));
        }

        lv.setAdapter(new NutrientLevelListAdapter(getContext(), levelItem));

        String servingSize = product.getServingSize();
        if (TextUtils.isEmpty(servingSize)) {
            servingSize = getString(R.string.txtNoData);
        }
        serving.setText(Html.fromHtml("<b>" + getString(R.string.txtServingSize) + "</b>" + ' ' + servingSize));
    }

    private int getImageGrade(String grade) {
        int drawable;

        if (grade == null) {
            return R.drawable.ic_error;
        }

        switch (grade.toLowerCase()) {
            case "a":
                drawable = R.drawable.nnc_a;
                break;
            case "b":
                drawable = R.drawable.nnc_b;
                break;
            case "c":
                drawable = R.drawable.nnc_c;
                break;
            case "d":
                drawable = R.drawable.nnc_d;
                break;
            case "e":
                drawable = R.drawable.nnc_e;
                break;
            default:
                drawable = R.drawable.ic_error;
                break;
        }

        return drawable;
    }

    private int getImageLevel(String nutrient) {
        int drawable;

        if (nutrient == null) {
            return R.drawable.ic_error;
        }

        switch (nutrient.toLowerCase()) {
            case "moderate":
                drawable = R.drawable.ic_circle_yellow;
                break;
            case "low":
                drawable = R.drawable.ic_circle_green;
                break;
            case "high":
                drawable = R.drawable.ic_circle_red;
                break;
            default:
                drawable = R.drawable.ic_error;
                break;
        }

        return drawable;
    }

    /**
     *
     * @param nutritionAmount Either "low", "moderate" or "high"
     * @return The localised word for the nutrition amount. If nutritionAmount is neither low,
     * moderate nor high, return nutritionAmount
     */
    private String localiseNutritionLevel(String nutritionAmount){
        switch (nutritionAmount){
            case "low":
                return getString(R.string.txtNutritionLevelLow);
            case "moderate":
                return getString(R.string.txtNutritionLevelModerate);
            case "high":
                return getString(R.string.txtNutritionLevelHigh);
            default:
                return nutritionAmount;
        }
    }
}
