package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevels;
import openfoodfacts.github.scrachx.openfood.models.NutrimentLevel;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrientLevelListAdapter;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber;

public class NutritionProductFragment extends BaseFragment implements CustomTabActivityHelper.ConnectionCallback {

    @BindView(R.id.imageGrade) ImageView img;
    @BindView(R.id.listNutrientLevels) ListView lv;
    @BindView(R.id.textServingSize) TextView serving;
    @BindView(R.id.textCarbonFootprint) TextView carbonFootprint;
    @BindView(R.id.textNutrientTxt) TextView textNutrientTxt;
    private CustomTabActivityHelper customTabActivityHelper;
    private Uri nutritionScoreUri;

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
        List<NutrientLevelItem> levelItem = new ArrayList<>();

        Nutriments nutriments = product.getNutriments();

        NutrientLevels nutrientLevels = product.getNutrientLevels();
        NutrimentLevel fat = null;
        NutrimentLevel saturatedFat = null;
        NutrimentLevel sugars = null;
        NutrimentLevel salt = null;
        if(nutrientLevels != null) {
            fat = nutrientLevels.getFat();
            saturatedFat = nutrientLevels.getSaturatedFat();
            sugars = nutrientLevels.getSugars();
            salt = nutrientLevels.getSalt();
        }

        if (fat == null && salt == null && saturatedFat == null && sugars == null) {
            textNutrientTxt.append(" "+getString(R.string.txtNoData));
            levelItem.add(new NutrientLevelItem("", "", "", 0));
        } else {
            // prefetch the uri
            customTabActivityHelper = new CustomTabActivityHelper();
            customTabActivityHelper.setConnectionCallback(this);
            // currently only available in french translations
            nutritionScoreUri = Uri.parse("https://fr.openfoodfacts.org/score-nutritionnel-france");
            customTabActivityHelper.mayLaunchUrl(nutritionScoreUri, null, null);

            Context context = this.getContext();

            if (fat != null) {
                String fatNutrimentLevel = fat.getLocalize(context);
                Nutriments.Nutriment nutriment = nutriments.get(Nutriments.FAT);
                levelItem.add(new NutrientLevelItem(getString(R.string.txtFat), getRoundNumber(nutriment.getFor100g()) + " " + nutriment.getUnit(), fatNutrimentLevel, fat.getImageLevel()));
            }

            if (saturatedFat != null) {
                String saturatedFatLocalize = saturatedFat.getLocalize(context);
                Nutriments.Nutriment nutriment = nutriments.get(Nutriments.SATURATED_FAT);
                String saturatedFatValue = getRoundNumber(nutriment.getFor100g()) + " " + nutriment.getUnit();
                levelItem.add(new NutrientLevelItem(getString(R.string.txtSaturatedFat), saturatedFatValue, saturatedFatLocalize, saturatedFat.getImageLevel()));
            }

            if (sugars != null) {
                String sugarsLocalize = sugars.getLocalize(context);
                Nutriments.Nutriment nutriment = nutriments.get(Nutriments.SUGARS);
                String sugarsValue = getRoundNumber(nutriment.getFor100g()) + " " + nutriment.getUnit();
                levelItem.add(new NutrientLevelItem(getString(R.string.txtSugars), sugarsValue, sugarsLocalize, sugars.getImageLevel()));
            }

            if (salt != null) {
                String saltLocalize = salt.getLocalize(context);
                Nutriments.Nutriment nutriment = nutriments.get(Nutriments.SALT);
                String saltValue = getRoundNumber(nutriment.getFor100g()) + " " + nutriment.getUnit();
                levelItem.add(new NutrientLevelItem(getString(R.string.txtSalt), saltValue, saltLocalize, salt.getImageLevel()));
            }

            img.setImageDrawable(ContextCompat.getDrawable(context, Utils.getImageGrade(product.getNutritionGradeFr())));
            img.setOnClickListener(view1 -> {
            CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());

            CustomTabActivityHelper.openCustomTab(NutritionProductFragment.this.getActivity(), customTabsIntent, nutritionScoreUri, new WebViewFallback());
            });
        }

        lv.setAdapter(new NutrientLevelListAdapter(getContext(), levelItem));

        if (TextUtils.isEmpty(product.getServingSize())) {
            serving.setVisibility(View.GONE);
        } else {
            serving.append(bold(getString(R.string.txtServingSize)));
            serving.append(" ");
            serving.append(product.getServingSize());
        }

        if (!nutriments.contains(Nutriments.CARBON_FOOTPRINT)) {
            carbonFootprint.setVisibility(View.GONE);
        } else {
            Nutriments.Nutriment carbonFootprintNutriment = nutriments.get(Nutriments.CARBON_FOOTPRINT);
            carbonFootprint.append(bold(getString(R.string.textCarbonFootprint)));
            carbonFootprint.append(carbonFootprintNutriment.getFor100g());
            carbonFootprint.append(carbonFootprintNutriment.getUnit());
        }
    }

    @Override
    public void onCustomTabsConnected() {
        img.setClickable(true);
    }

    @Override
    public void onCustomTabsDisconnected() {
        img.setClickable(false);
    }
}
