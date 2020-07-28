package openfoodfacts.github.scrachx.openfood.fragments;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName;
import openfoodfacts.github.scrachx.openfood.network.WikiDataApiClient;
import openfoodfacts.github.scrachx.openfood.utils.BottomScreenCommon;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.getColor;

public class CategoryProductHelper {
    private final WikiDataApiClient apiClient;
    private final BaseFragment baseFragment;
    private final List<CategoryName> categories;
    private final TextView categoryText;
    private boolean containsAlcohol;


    public CategoryProductHelper(TextView categoryText, List<CategoryName> categories, BaseFragment baseFragment,
                                 WikiDataApiClient apiClient) {
        this.categoryText = categoryText;
        this.categories = categories;
        this.baseFragment = baseFragment;
        this.apiClient = apiClient;
        this.containsAlcohol = false;

    }

    public void showCategories(){
        categoryText.setText(bold(baseFragment.getString(R.string.txtCategories)));
        categoryText.setMovementMethod(LinkMovementMethod.getInstance());
        categoryText.append(" ");
        categoryText.setClickable(true);
        categoryText.setMovementMethod(LinkMovementMethod.getInstance());

        if (categories.isEmpty()) {
            categoryText.setVisibility(View.GONE);
        } else {
            categoryText.setVisibility(View.VISIBLE);
            // Add all the categories to text view and link them to wikidata is possible
            for (int i = 0, lastIndex = categories.size() - 1; i <= lastIndex; i++) {
                CategoryName category = categories.get(i);
                CharSequence categoryName = getCategoriesTag(category);
                // Add category name to text view
                categoryText.append(categoryName);
                // Add a comma if not the last item
                if (i != lastIndex) {
                    categoryText.append(", ");
                }

                if (category.getCategoryTag() != null && category.getCategoryTag().equals("en:alcoholic-beverages")) {
                    containsAlcohol = true;
                }
            }
        }
    }

    private CharSequence getCategoriesTag(CategoryName category) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                if (category.getIsWikiDataIdPresent()) {
                    apiClient.doSomeThing(category.getWikiDataId(), result -> {
                        if (result != null) {
                            FragmentActivity activity = baseFragment.getActivity();

                            if (activity != null && !activity.isFinishing()) {
                                BottomScreenCommon.showBottomScreen(result, category,
                                    activity.getSupportFragmentManager());
                            }
                        } else {
                            ProductBrowsingListActivity.startActivity(baseFragment.getContext(),
                                category.getCategoryTag(),
                                category.getName(),
                                SearchType.CATEGORY);
                        }
                    });
                } else {
                    ProductBrowsingListActivity.startActivity(baseFragment.getContext(),
                        category.getCategoryTag(),
                        category.getName(),
                        SearchType.CATEGORY);
                }
            }
        };
        spannableStringBuilder.append(category.getName());
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        if (!category.isNotNull()) {
            StyleSpan iss = new StyleSpan(android.graphics.Typeface.ITALIC); //Span to make text italic
            spannableStringBuilder.setSpan(iss, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableStringBuilder;
    }

    public boolean getContainsAlcohol() {
        return containsAlcohol;
    }

    public void showAlcoholAlert(TextView alcoholAlertText){
        SpannableStringBuilder alcoholAlertString = new SpannableStringBuilder();

        Drawable alcoholAlertIcon = ContextCompat.getDrawable(baseFragment.getContext(),R.drawable.ic_alert_alcoholic_beverage);
        alcoholAlertIcon.setBounds(0,0,alcoholAlertIcon.getIntrinsicWidth(),alcoholAlertIcon.getIntrinsicHeight());

        ImageSpan alcoholAlertSpan = new ImageSpan(alcoholAlertIcon, DynamicDrawableSpan.ALIGN_BOTTOM);
        alcoholAlertString.append("- ");
        alcoholAlertString.setSpan(alcoholAlertSpan,0,1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        String riskAlcoholConsumption = baseFragment.getString(R.string.risk_alcohol_consumption);
        alcoholAlertString.append(riskAlcoholConsumption);
        alcoholAlertString.setSpan(new ForegroundColorSpan(getColor(baseFragment.getContext(),R.color.red)),
            alcoholAlertString.length() - riskAlcoholConsumption.length(),
            alcoholAlertString.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        alcoholAlertText.setVisibility(View.VISIBLE);
        alcoholAlertText.setText(alcoholAlertString);
    }

}
