package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.BottomScreenCommon;
import openfoodfacts.github.scrachx.openfood.network.WikidataApiClient;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;

import java.util.List;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.getColor;

public class AdditiveFragmentHelper {
    private AdditiveFragmentHelper(){
        //helper class
    }

    public static void showAdditives(List<AdditiveName> additives, TextView additiveProduct, final WikidataApiClient apiClientForWikiData, BaseFragment fragment) {
        additiveProduct.setText(bold(fragment.getString(R.string.txtAdditives)));
        additiveProduct.setMovementMethod(LinkMovementMethod.getInstance());
        additiveProduct.append(" ");
        additiveProduct.append("\n");
        additiveProduct.setClickable(true);
        additiveProduct.setMovementMethod(LinkMovementMethod.getInstance());

        for (int i = 0; i < additives.size() - 1; i++) {
            additiveProduct.append(getAdditiveTag(additives.get(i),apiClientForWikiData,fragment));
            additiveProduct.append("\n");
        }

        additiveProduct.append(getAdditiveTag(additives.get(additives.size() - 1),apiClientForWikiData,fragment));
    }

    private static CharSequence getAdditiveTag(AdditiveName additive, final WikidataApiClient apiClientForWikiData, BaseFragment fragment) {
        FragmentActivity activity=fragment.getActivity();
        Context context=fragment.getContext();
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (additive.getIsWikiDataIdPresent()) {
                    apiClientForWikiData.doSomeThing(additive.getWikiDataId(), (value, result) -> {
                        if (value) {
                            if (activity != null && !activity.isFinishing()) {
                                BottomScreenCommon.showBottomScreen(result, additive,
                                    activity.getSupportFragmentManager());
                            }
                        } else {
                            if (additive.hasOverexposureData()) {
                                if (activity != null && !activity.isFinishing()) {
                                    BottomScreenCommon.showBottomScreen(result, additive,
                                        activity.getSupportFragmentManager());
                                }
                            } else {
                                ProductBrowsingListActivity.startActivity(context, additive.getAdditiveTag(), additive.getName(), SearchType.ADDITIVE);
                            }
                        }
                    });
                } else {
                    if (additive.hasOverexposureData()) {
                        if (activity != null && !activity.isFinishing()) {
                            BottomScreenCommon.showBottomScreen(null, additive,
                                activity.getSupportFragmentManager());
                        }
                    } else {
                        ProductBrowsingListActivity.startActivity(context, additive.getAdditiveTag(), additive.getName(), SearchType.ADDITIVE);
                    }
                }
            }
        };

        spannableStringBuilder.append(additive.getName());
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        // if the additive has an overexposure risk ("high" or "moderate") then append the warning message to it
        if (additive.hasOverexposureData()) {
            boolean isHighRisk = "high".equalsIgnoreCase(additive.getOverexposureRisk());
            Drawable riskIcon;
            String riskWarningStr;
            int riskWarningColor;
            if (isHighRisk) {
                riskIcon = ContextCompat.getDrawable(context, R.drawable.ic_additive_high_risk);
                riskWarningStr = fragment.getString(R.string.overexposure_high);
                riskWarningColor = getColor(context, R.color.overexposure_high);
            } else {
                riskIcon = ContextCompat.getDrawable(context, R.drawable.ic_additive_moderate_risk);
                riskWarningStr = fragment.getString(R.string.overexposure_moderate);
                riskWarningColor = getColor(context, R.color.overexposure_moderate);
            }
            riskIcon.setBounds(0, 0, riskIcon.getIntrinsicWidth(), riskIcon.getIntrinsicHeight());
            ImageSpan iconSpan = new ImageSpan(riskIcon, ImageSpan.ALIGN_BOTTOM);

            spannableStringBuilder.append(" - "); // this will be replaced with the risk icon
            spannableStringBuilder.setSpan(iconSpan, spannableStringBuilder.length() - 2, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

            spannableStringBuilder.append(riskWarningStr);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(riskWarningColor), spannableStringBuilder.length() - riskWarningStr.length(), spannableStringBuilder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableStringBuilder;
    }
}
