package openfoodfacts.github.scrachx.openfood.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrimentItem;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrimentsRecyclerViewAdapter;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;
import static android.text.TextUtils.isEmpty;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class NutritionInfoProductFragment extends BaseFragment {

    @BindView(R.id.textPerPortion)
    TextView mTextPerPortion;
    @BindView(R.id.imageViewNutritionFullNut)
    ImageView mImageNutritionFull;
    @BindView(R.id.nutriments_recycler_view)
    RecyclerView nutrimentsRecyclerView;

    private String mUrlImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_nutrition_info_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

        final Product product = state.getProduct();
        Nutriments nutriments = product.getNutriments();
        List<NutrimentItem> nutrimentItems = new ArrayList<>();

        if (isNotEmpty(product.getServingSize())) {
            mTextPerPortion.setText(getString(R.string.nutriment_serving_size) + " " +product.getServingSize());
        }

        if (isNotEmpty(product.getImageNutritionUrl())) {
            Picasso.with(view.getContext())
                    .load(product.getImageNutritionUrl())
                    .into(mImageNutritionFull);
            mUrlImage = product.getImageNutritionUrl();
        }

        if (nutriments != null) {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            nutrimentsRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            nutrimentsRecyclerView.setLayoutManager(mLayoutManager);

            // use VERTICAL divider
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(nutrimentsRecyclerView.getContext(), VERTICAL);
            nutrimentsRecyclerView.addItemDecoration(dividerItemDecoration);

            if(isNotEmpty(nutriments.getCarbohydratesServing())) {
                String servingValue = getRoundNumber(nutriments.getCarbohydratesServing());
                String value = getRoundNumber(nutriments.getCarbohydrates100g());
                nutrimentItems.add(new NutrimentItem(getString(R.string.nutrition_carbohydrate), value, servingValue, nutriments.getCarbohydratesUnit()));
            }

            if(isNotEmpty(nutriments.getEnergyServing())) {
                String servingEnergy = getEnergy(nutriments.getEnergyServing());
                String energy = getEnergy(nutriments.getEnergy100g());
                nutrimentItems.add(new NutrimentItem(getString(R.string.nutrition_energy_short_name), energy, servingEnergy, "kcal"));
            }

            if(isNotEmpty(nutriments.getFatServing())) {
                String servingValue = getRoundNumber(nutriments.getFatServing());
                String value = getRoundNumber(nutriments.getFat100g());
                nutrimentItems.add(new NutrimentItem(getString(R.string.nutrition_fat), value, servingValue, nutriments.getFatUnit()));
            }

            if(isNotEmpty(nutriments.getFiberServing())) {
                String servingValue = getRoundNumber(nutriments.getFiberServing());
                String value = getRoundNumber(nutriments.getFiber100g());
                nutrimentItems.add(new NutrimentItem(getString(R.string.nutrition_fiber), value, servingValue, nutriments.getFiberUnit()));
            }

            if(isNotEmpty(nutriments.getProteinsServing())) {
                String value = getRoundNumber(nutriments.getProteins100g());
                String servingValue = getRoundNumber(nutriments.getProteinsServing());
                nutrimentItems.add(new NutrimentItem(getString(R.string.nutrition_proteins), value, servingValue, nutriments.getProteinsUnit()));
            }

            if(isNotEmpty(nutriments.getSaltServing())) {
                String value = getRoundNumber(nutriments.getSalt100g());
                String servingValue = getRoundNumber(nutriments.getSaltServing());
                nutrimentItems.add(new NutrimentItem(getString(R.string.nutrition_salt), value, servingValue, nutriments.getSaltUnit()));
            }

            if(isNotEmpty(nutriments.getSaturatedFatServing())) {
                String value = getRoundNumber(nutriments.getSaturatedFat100g());
                String servingValue = getRoundNumber(nutriments.getSaturatedFatServing());
                nutrimentItems.add(new NutrimentItem(getString(R.string.nutrition_satured_fat), value, servingValue, nutriments.getSaturatedFatUnit()));
            }

            if(isNotEmpty(nutriments.getSodiumServing())) {
                String value = getRoundNumber(nutriments.getSodium100g());
                String servingValue = getRoundNumber(nutriments.getSodiumServing());
                nutrimentItems.add(new NutrimentItem(getString(R.string.nutrition_sodium), value, servingValue, nutriments.getSodiumUnit()));
            }

            if(isNotEmpty(nutriments.getSugarsServing())) {
                String value = getRoundNumber(nutriments.getSugars100g());
                String servingValue = getRoundNumber(nutriments.getSugarsServing());
                nutrimentItems.add(new NutrimentItem(getString(R.string.nutrition_sugars), value, servingValue, nutriments.getSugarsUnit()));
            }

            RecyclerView.Adapter adapter = new NutrimentsRecyclerViewAdapter(nutrimentItems);
            nutrimentsRecyclerView.setAdapter(adapter);
         }
    }

    private String getEnergy(String value) {
        String defaultValue = "0";
        if (defaultValue.equals(value) || isEmpty(value)) {
            return defaultValue;
        }

        try {
            int energyKcal = convertKjToKcal(Integer.parseInt(value));
            return String.valueOf(energyKcal);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int convertKjToKcal(int kj) {
        return kj != 0 ? Double.valueOf(((double) kj) / 4.1868d).intValue() : -1;
    }

    @OnClick(R.id.imageViewNutritionFullNut)
    public void openFullScreen(View v) {
        Intent intent = new Intent(v.getContext(), FullScreenImage.class);
        Bundle bundle = new Bundle();
        bundle.putString("imageurl", mUrlImage);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
