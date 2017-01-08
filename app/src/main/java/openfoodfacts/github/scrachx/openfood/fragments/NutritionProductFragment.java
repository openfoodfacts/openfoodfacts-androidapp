package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
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
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrientLevelListAdapter;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
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
        NutrientLevels nt = product.getNutrientLevels();
        ArrayList<NutrientLevelItem> levelItem = new ArrayList<>();

        if (nt == null || (nt.getFat() == null && nt.getSalt() == null
                && nt.getSaturatedFat() == null && nt.getSugars() == null)) {
            levelItem.add(new NutrientLevelItem(getString(R.string.txtNoData), R.drawable.error_image));
        } else {
            // prefetch the uri
            customTabActivityHelper = new CustomTabActivityHelper();
            customTabActivityHelper.setConnectionCallback(this);
            // currently only available in french translations
            nutritionScoreUri = Uri.parse("https://fr.openfoodfacts.org/score-nutritionnel-france");
            customTabActivityHelper.mayLaunchUrl(nutritionScoreUri, null, null);

            String fatTxt = getString(R.string.txtFat) + ' ' + Utils.localiseNutritionLevel(this.getContext(), nt.getFat()) + " (" + product.getNutriments().getFat100g() + product.getNutriments().getFatUnit() + ")";
            String saturatedFatTxt = getString(R.string.txtSaturatedFat) + ' ' + Utils.localiseNutritionLevel(this.getContext(), nt.getSaturatedFat()) + " (" + product.getNutriments().getSaturatedFat100g() + product.getNutriments().getSaturatedFatUnit() + ")";
            String sugarsTxt = getString(R.string.txtSugars)  + ' ' + Utils.localiseNutritionLevel(this.getContext(), nt.getSugars()) + " (" + product.getNutriments().getSugars100g() + product.getNutriments().getSugarsUnit() + ")";
            String saltTxt = getString(R.string.txtSalt) + ' ' + Utils.localiseNutritionLevel(this.getContext(), nt.getSalt()) + " (" + product.getNutriments().getSalt100g() + product.getNutriments().getSaltUnit() + ")";

            levelItem.add(new NutrientLevelItem(fatTxt, Utils.getImageLevel(nt.getFat())));
            levelItem.add(new NutrientLevelItem(saturatedFatTxt, Utils.getImageLevel(nt.getSaturatedFat())));
            levelItem.add(new NutrientLevelItem(sugarsTxt, Utils.getImageLevel(nt.getSugars())));
            levelItem.add(new NutrientLevelItem(saltTxt, Utils.getImageLevel(nt.getSalt())));

            img.setImageResource(Utils.getImageGrade(product.getNutritionGradeFr()));
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Bitmap icon = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_navigation_arrow_back)).getBitmap();

                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder(customTabActivityHelper.getSession())
                        .setShowTitle(true)
                        .setToolbarColor(getResources().getColor(R.color.indigo_400))
                        .setCloseButtonIcon(icon)
                        .build();

                CustomTabActivityHelper.openCustomTab(NutritionProductFragment.this.getActivity(), customTabsIntent, nutritionScoreUri, new WebViewFallback());
                }
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

        Nutriments nutriments = product.getNutriments();
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
