package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
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
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class NutritionProductFragment extends BaseFragment implements CustomTabActivityHelper.ConnectionCallback {

    @BindView(R.id.imageGrade) ImageView img;
    @BindView(R.id.listNutrientLevels) ListView lv;
    @BindView(R.id.textServingSize) TextView serving;
    @BindView(R.id.textCarbonFootprint) TextView carbonFootprint;
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
        NutrimentLevel fat = nutrientLevels.getFat();
        NutrimentLevel saturatedFat = nutrientLevels.getSaturatedFat();
        NutrimentLevel sugars = nutrientLevels.getSugars();
        NutrimentLevel salt = nutrientLevels.getSalt();

        if (fat == null && salt == null && saturatedFat == null && sugars == null) {
            levelItem.add(new NutrientLevelItem(getString(R.string.txtNoData), "", "", R.drawable.error_image));
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
                levelItem.add(new NutrientLevelItem(getString(R.string.txtFat), getRoundNumber(nutriments.getFat100g()) + " " + nutriments.getFatUnit(), fatNutrimentLevel, fat.getImageLevel()));
            }

            if (saturatedFat != null) {
                String saturatedFatLocalize = saturatedFat.getLocalize(context);
                String saturatedFatValue = getRoundNumber(nutriments.getSaturatedFat100g()) + " " + nutriments.getSaturatedFatUnit();
                levelItem.add(new NutrientLevelItem(getString(R.string.txtSaturatedFat), saturatedFatValue, saturatedFatLocalize, saturatedFat.getImageLevel()));
            }

            if (sugars != null) {
                String sugarsLocalize = sugars.getLocalize(context);
                String sugarsValue = getRoundNumber(nutriments.getSugars100g()) + " " + nutriments.getSugarsUnit();
                levelItem.add(new NutrientLevelItem(getString(R.string.txtSugars), sugarsValue, sugarsLocalize, sugars.getImageLevel()));
            }

            if (salt != null) {
                String saltLocalize = salt.getLocalize(context);
                String saltValue = getRoundNumber(nutriments.getSalt100g()) + " " + nutriments.getSaltUnit();
                levelItem.add(new NutrientLevelItem(getString(R.string.txtSalt), saltValue, saltLocalize, salt.getImageLevel()));
            }

            img.setImageResource(Utils.getImageGrade(product.getNutritionGradeFr()));
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

        if (isEmpty(nutriments.getCarbonFootprint100g())) {
            carbonFootprint.setVisibility(View.GONE);
        } else {
            carbonFootprint.append(bold(getString(R.string.textCarbonFootprint)));
            carbonFootprint.append(nutriments.getCarbonFootprint100g());
            carbonFootprint.append(nutriments.getCarbonFootprintUnit());
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
