package openfoodfacts.github.scrachx.openfood.features.additives;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.function.Consumer;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity;
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment;
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName;
import openfoodfacts.github.scrachx.openfood.network.WikiDataApiClient;
import openfoodfacts.github.scrachx.openfood.utils.BottomScreenCommon;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.getColor;

/**
 * Helper class for additive fragment
 */
public class AdditiveFragmentHelper {
    private AdditiveFragmentHelper() {
        //helper class
    }

    /**
     * Show names of all additives on the TextView
     *
     * @param additives list of additive names
     * @param additiveProduct TextView which displays additive names
     * @param apiClientForWikiData object of WikidataApiClient
     */
    public static void showAdditives(@NonNull List<AdditiveName> additives, @NonNull TextView additiveProduct, final WikiDataApiClient apiClientForWikiData,
                                     @NonNull BaseFragment fragment) {
        additiveProduct.setText(bold(fragment.getString(R.string.txtAdditives)));
        additiveProduct.setMovementMethod(LinkMovementMethod.getInstance());
        additiveProduct.append(" ");
        additiveProduct.append("\n");
        additiveProduct.setClickable(true);
        additiveProduct.setMovementMethod(LinkMovementMethod.getInstance());

        for (int i = 0; i < additives.size() - 1; i++) {
            additiveProduct.append(getAdditiveTag(additives.get(i), apiClientForWikiData, fragment));
            additiveProduct.append("\n");
        }

        additiveProduct.append(getAdditiveTag(additives.get(additives.size() - 1), apiClientForWikiData, fragment));
    }

    /**
     * Returns additive tag from additive name using WikidataApiClient
     *
     * @param additive name of the additive
     * @param apiClientForWikiData object of WikidataApiClient
     * @param fragment holds a reference to the calling fragment
     **/
    private static CharSequence getAdditiveTag(AdditiveName additive, final WikiDataApiClient apiClientForWikiData, BaseFragment fragment) {
        FragmentActivity activity = fragment.requireActivity();
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                if (additive.getIsWikiDataIdPresent()) {
                    apiClientForWikiData.doSomeThing(additive.getWikiDataId(), getOnWikiResponse(activity, additive));
                } else {
                    onWikiNoResponse(additive, activity);
                }
            }
        };

        spannableStringBuilder.append(additive.getName());
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        // if the additive has an overexposure risk ("high" or "moderate") then append the warning message to it
        if (additive.hasOverexposureData()) {
            final boolean isHighRisk = "high".equalsIgnoreCase(additive.getOverexposureRisk());
            Drawable riskIcon;
            String riskWarningStr;
            int riskWarningColor;
            if (isHighRisk) {
                riskIcon = ContextCompat.getDrawable(activity, R.drawable.ic_additive_high_risk);
                riskWarningStr = fragment.getString(R.string.overexposure_high);
                riskWarningColor = getColor(activity, R.color.overexposure_high);
            } else {
                riskIcon = ContextCompat.getDrawable(activity, R.drawable.ic_additive_moderate_risk);
                riskWarningStr = fragment.getString(R.string.overexposure_moderate);
                riskWarningColor = getColor(activity, R.color.overexposure_moderate);
            }
            riskIcon.setBounds(0, 0, riskIcon.getIntrinsicWidth(), riskIcon.getIntrinsicHeight());
            ImageSpan iconSpan = new ImageSpan(riskIcon, DynamicDrawableSpan.ALIGN_BOTTOM);

            spannableStringBuilder.append(" - "); // this will be replaced with the risk icon
            spannableStringBuilder.setSpan(iconSpan, spannableStringBuilder.length() - 2, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

            spannableStringBuilder.append(riskWarningStr);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(riskWarningColor), spannableStringBuilder.length() - riskWarningStr.length(), spannableStringBuilder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableStringBuilder;
    }

    private static void onWikiNoResponse(AdditiveName additive, FragmentActivity activity) {
        if (additive.hasOverexposureData()) {
            if (activity != null && !activity.isFinishing()) {
                BottomScreenCommon.showBottomSheet(null, additive,
                    activity.getSupportFragmentManager());
            }
        } else {
            ProductSearchActivity.start(activity, additive.getAdditiveTag(), additive.getName(), SearchType.ADDITIVE);
        }
    }

    private static Consumer<JsonNode> getOnWikiResponse(FragmentActivity activity, AdditiveName additive) {
        return result -> {
            if (result != null) {
                if (activity != null && !activity.isFinishing()) {
                    BottomScreenCommon.showBottomSheet(result, additive,
                        activity.getSupportFragmentManager());
                }
            } else {
                if (additive.hasOverexposureData()) {
                    if (activity != null && !activity.isFinishing()) {
                        BottomScreenCommon.showBottomSheet(result, additive,
                            activity.getSupportFragmentManager());
                    }
                } else {
                    ProductSearchActivity.start(activity, additive.getAdditiveTag(), additive.getName(), SearchType.ADDITIVE);
                }
            }
        };
    }
}
