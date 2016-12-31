package openfoodfacts.github.scrachx.openfood.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrimentItem;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutritionInfoAdapter;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class NutritionInfoProductFragment extends BaseFragment {

    @BindView(R.id.gridView)
    GridView mGv;
    @BindView(R.id.textPerPortion)
    TextView mTextPerPortion;
    @BindView(R.id.imageViewNutritionFullNut)
    ImageView mImageNutritionFull;

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
        Nutriments nt = product.getNutriments();
        ArrayList<NutrimentItem> nutrimentItemList = new ArrayList<>();

        if (isNotEmpty(product.getServingSize())) {
            mTextPerPortion.setText(getString(R.string.nutriment_serving_size) + " " +product.getServingSize());
        }

        if (isNotEmpty(product.getImageNutritionUrl())) {
            Picasso.with(view.getContext())
                    .load(product.getImageNutritionUrl())
                    .into(mImageNutritionFull);
            mUrlImage = product.getImageNutritionUrl();
        }

        if (nt != null) {
            if(isNotEmpty(nt.getCarbohydratesServing())) {
                nutrimentItemList.add(new NutrimentItem(getString(R.string.nutrition_carbohydrate_short_name), nt.getCarbohydratesServing(), R.color.amber_800));
            }
            if(isNotEmpty(getEnergy(nt))) {
                nutrimentItemList.add(new NutrimentItem(getString(R.string.nutrition_energy_short_name) + " (kcal)", getEnergy(nt), R.color.blue_400));
            }
            if(isNotEmpty(nt.getFatServing())) {
                nutrimentItemList.add(new NutrimentItem(getString(R.string.nutrition_fat_short_name), nt.getFatServing(), R.color.blue_grey_500));
            }
            if(isNotEmpty(nt.getFiberServing())) {
                nutrimentItemList.add(new NutrimentItem(getString(R.string.nutrition_fiber_short_name), nt.getFiberServing(), R.color.blue_300));
            }
            if(isNotEmpty(nt.getProteinsServing())) {
                nutrimentItemList.add(new NutrimentItem(getString(R.string.nutrition_proteins_short_name), nt.getProteinsServing(), R.color.yellow_800));
            }
            if(isNotEmpty(nt.getSaltServing())) {
                nutrimentItemList.add(new NutrimentItem(getString(R.string.nutrition_salt_short_name), nt.getSaltServing(), R.color.teal_800));
            }
            if(isNotEmpty(nt.getSaturatedFatServing())) {
                nutrimentItemList.add(new NutrimentItem(getString(R.string.nutrition_satured_fat_short_name), nt.getSaturatedFatServing(), R.color.red_600));
            }
            if(isNotEmpty(nt.getSodiumServing())) {
                nutrimentItemList.add(new NutrimentItem(getString(R.string.nutrition_sodium_short_name), nt.getSodiumServing(), R.color.purple_500));
            }
            if(isNotEmpty(nt.getSugarsServing())) {
                nutrimentItemList.add(new NutrimentItem(getString(R.string.nutrition_sugars_short_name), nt.getSugarsServing(), R.color.cyan_600));
            }
         }

        mGv.setAdapter(new NutritionInfoAdapter(getContext(), nutrimentItemList));
    }

    private String getEnergy(Nutriments nt) {
        if (isNotEmpty(nt.getEnergyServing())) {
            try {
                int energyKcal;
                if (nt.getEnergyServing().isEmpty()) {
                    energyKcal = 0;
                } else {
                    energyKcal = convertKjToKcal(Integer.parseInt(nt.getEnergyServing()));
                }
                return String.valueOf(energyKcal);
            } catch (NumberFormatException e) {
            }
        }
        return  null;
    }

    private int convertKjToKcal(int kj) {
        if (kj != 0) {
            return Double.valueOf(((double) kj) / 4.1868d).intValue();
        }
        return -1;
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
