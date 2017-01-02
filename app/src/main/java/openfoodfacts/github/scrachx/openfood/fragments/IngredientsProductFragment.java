package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Additive;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class IngredientsProductFragment extends BaseFragment {

    public static final Pattern INGREDIENT_PATTERN = Pattern.compile("[a-zA-Z0-9(),àâçéèêëîïôûùüÿñæœ.-]+");
    public static final Pattern ALLERGEN_PATTERN = Pattern.compile("[a-zA-Z0-9àâçéèêëîïôûùüÿñæœ]+");
    @BindView(R.id.textIngredientProduct) TextView ingredientsProduct;
    @BindView(R.id.textSubstanceProduct) TextView substanceProduct;
    @BindView(R.id.textTraceProduct) TextView traceProduct;
    @BindView(R.id.textAdditiveProduct) TextView additiveProduct;
    @BindView(R.id.textPalmOilProduct) TextView palmOilProduct;
    @BindView(R.id.textMayBeFromPalmOilProduct) TextView mayBeFromPalmOilProduct;
    @BindView(R.id.ingredientContainer) ViewGroup containerView;
    @BindView(R.id.imageViewNutritionFullIng) ImageView mImageNutritionFullIng;
    private String mUrlImage;
    private State mState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_ingredients_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        mState = (State) intent.getExtras().getSerializable("state");

        final Product product = mState.getProduct();

        if (isNotEmpty(product.getImageIngredientsUrl())) {
            Picasso.with(view.getContext())
                    .load(product.getImageIngredientsUrl())
                    .into(mImageNutritionFullIng);
            mUrlImage = product.getImageIngredientsUrl();
        } else {
            mImageNutritionFullIng.setVisibility(View.GONE);
        }

        if(mState != null && product.getIngredientsText() != null) {
            //TODO The API doesn't return ingredients text with the _ token. the replace method could be removed
            String txtIngredients = product.getIngredientsText().replace("_","").trim();
            if(!txtIngredients.isEmpty()) {
                String ingredientsValue = setSpanBoldBetweenTokens(txtIngredients).toString();
                ingredientsProduct.setText(ingredientsValue);
            } else {
                ingredientsProduct.setVisibility(View.GONE);
            }
        }

        if(!cleanAllergensString().trim().isEmpty()) {
            substanceProduct.append(bold(getString(R.string.txtSubstances)));
            substanceProduct.append(" ");
            substanceProduct.append(cleanAllergensString());
        } else {
            substanceProduct.setVisibility(View.GONE);
        }

        String traces;
        if (product.getTraces() == null) {
            traceProduct.setVisibility(View.GONE);
        } else {
            traces = product.getTraces().replace(",", ", ");
            if(traces.isEmpty()) {
                traceProduct.setVisibility(View.GONE);
            } else {
                traceProduct.append(bold(getString(R.string.txtTraces)));
                traceProduct.append(" ");
                traceProduct.append(traces);
            }
        }

        if(!product.getAdditivesTags().isEmpty()) {
            additiveProduct.setMovementMethod(LinkMovementMethod.getInstance());
            additiveProduct.append(bold(getString(R.string.txtAdditives)));
            additiveProduct.append(" ");

            for (String tag : product.getAdditivesTags()) {
                String tagWithoutLocale = tag.replaceAll("(en:|fr:)", "");
                additiveProduct.append(getSpanTag(tagWithoutLocale, view));
            }
        } else {
            additiveProduct.setVisibility(View.GONE);
        }

        if (product.getIngredientsFromPalmOilN() == 0 && product.getIngredientsFromOrThatMayBeFromPalmOilN() == 0) {
            palmOilProduct.setVisibility(View.GONE);
            mayBeFromPalmOilProduct.setVisibility(View.GONE);
        } else {
            if (!product.getIngredientsFromPalmOilTags().isEmpty()) {
                palmOilProduct.append(bold(getString(R.string.txtPalmOilProduct)));
                palmOilProduct.append(" ");
                palmOilProduct.append(product.getIngredientsFromPalmOilTags().toString().replaceAll("[\\[,\\]]", ""));
            } else {
                palmOilProduct.setVisibility(View.GONE);
            }
            if (!product.getIngredientsThatMayBeFromPalmOilTags().isEmpty()) {
                mayBeFromPalmOilProduct.append(bold(getString(R.string.txtMayBeFromPalmOilProduct)));
                mayBeFromPalmOilProduct.append(" ");
                mayBeFromPalmOilProduct.append(product.getIngredientsThatMayBeFromPalmOilTags().toString().replaceAll("[\\[,\\]]", ""));
            } else {
                mayBeFromPalmOilProduct.setVisibility(View.GONE);
            }
        }
    }

    private CharSequence getSpanTag(String tag, final View view) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder();
        final List<Additive> la = Additive.find(Additive.class, "code = ?", tag.toUpperCase());
        if (la.size() >= 1) {
            final Additive additive = la.get(0);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(view.getContext())
                            .title(additive.getCode() + " : " + additive.getName())
                            .content(additive.getRisk().toUpperCase())
                            .positiveText(R.string.txtOk)
                            .show();
                }
            };
            ssb.append(tag);
            ssb.setSpan(clickableSpan, 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(" ");
        }
        return ssb;
    }

    private SpannableStringBuilder setSpanBoldBetweenTokens(CharSequence text) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        Matcher m = INGREDIENT_PATTERN.matcher(ssb);
        while (m.find()) {
            final String tm = m.group();
            for (String l: cleanAllergensMultipleOccurrences()) {
                if(l.equalsIgnoreCase(tm.replaceAll("[(),.-]+", ""))) {
                    StyleSpan bold = new StyleSpan(Typeface.BOLD);
                    if(tm.contains("(")) {
                        ssb.setSpan(bold, m.start()+1, m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else if(tm.contains(")")) {
                        ssb.setSpan(bold, m.start(), m.end()-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        ssb.setSpan(bold, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
        ssb.insert(0, Html.fromHtml("<b>" + getString(R.string.txtIngredients) + "</b>" + ' '));
        return ssb;
    }

    private List<String> cleanAllergensMultipleOccurrences() {
        if (mState.getProduct() == null || mState.getProduct().getAllergens() == null) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>();
        Matcher m = ALLERGEN_PATTERN.matcher(mState.getProduct().getAllergens().replace(",", ""));
        while (m.find()) {
            final String tma = m.group();
            boolean canAdd = true;
            if(list.size() == 0) {
                list.add(tma);
            }
            for (int i = 0; i < list.size(); i++) {
                if(tma.equalsIgnoreCase(list.get(i))){
                    canAdd = false;
                }
            }
            if(canAdd) {
                list.add(tma);
            }
        }
        return list;
    }

    private String cleanAllergensString() {
        StringBuilder allergens = new StringBuilder();
        for (String l: cleanAllergensMultipleOccurrences()) {
            allergens.append(l).append(' ');
        }
        return allergens.toString();
    }

    @OnClick(R.id.imageViewNutritionFullIng)
    public void openFullScreen(View v) {
        Intent intent = new Intent(v.getContext(), FullScreenImage.class);
        Bundle bundle = new Bundle();
        bundle.putString("imageurl", mUrlImage);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
