package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
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
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Additive;
import openfoodfacts.github.scrachx.openfood.models.State;

public class IngredientsProductFragment extends BaseFragment {

    @BindView(R.id.textIngredientProduct) TextView ingredientsProduct;
    @BindView(R.id.textSubstanceProduct) TextView substanceProduct;
    @BindView(R.id.textTraceProduct) TextView traceProduct;
    @BindView(R.id.textAdditiveProduct) TextView additiveProduct;
    @BindView(R.id.textPalmOilProduct) TextView palmOilProduct;
    @BindView(R.id.textMayBeFromPalmOilProduct) TextView mayBeFromPalmOilProduct;
    @BindView(R.id.ingredientContainer) ViewGroup containerView;
    private State mState;

    public static final Pattern CODE_PATTERN = Pattern.compile("[eE][a-zA-Z0-9]+");
    public static final Pattern INGREDIENT_PATTERN = Pattern.compile("[a-zA-Z0-9(),àâçéèêëîïôûùüÿñæœ.-]+");
    public static final Pattern ALLERGEN_PATTERN = Pattern.compile("[a-zA-Z0-9àâçéèêëîïôûùüÿñæœ]+");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_ingredients_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        mState = (State) intent.getExtras().getSerializable("state");

        if(mState != null && mState.getProduct().getIngredientsText() != null) {
            SpannableStringBuilder txtIngredients = new SpannableStringBuilder(Html.fromHtml(mState.getProduct().getIngredientsText().replace("_","")));
            txtIngredients = setSpanBoldBetweenTokens(txtIngredients);
            if(!txtIngredients.toString().substring(txtIngredients.toString().indexOf(":")).trim().isEmpty()) {
                ingredientsProduct.setText(txtIngredients);
            } else {
                ingredientsProduct.setVisibility(View.GONE);
            }
        }

        if(!cleanAllergensString().trim().isEmpty()) {
            substanceProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtSubstances) + "</b>" + ' ' + cleanAllergensString()));
        } else {
            substanceProduct.setVisibility(View.GONE);
        }
        String traces;
        if (mState.getProduct().getCategories() == null) {
            traceProduct.setVisibility(View.GONE);
        } else {
            traces = mState.getProduct().getTraces().replace(",", ", ");
            if(traces.isEmpty()) {
                traceProduct.setVisibility(View.GONE);
            } else {
                traceProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtTraces) + "</b>" + ' ' + traces));
            }
        }
        if(!mState.getProduct().getAdditivesTags().toString().replace("[", "").replace("]", "").isEmpty()) {
            additiveProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtAdditives) + "</b>" + ' ' + mState.getProduct().getAdditivesTags().toString().replace("[", "").replace("]", "").replace("en:", " ").replace("fr:", " ")));
        } else {
            additiveProduct.setVisibility(View.GONE);
        }
        if (mState.getProduct().getIngredientsFromPalmOilN() == 0 && mState.getProduct().getIngredientsFromOrThatMayBeFromPalmOilN() == 0) {
            palmOilProduct.setVisibility(View.VISIBLE);
            palmOilProduct.setText(getString(R.string.txtPalm));
        } else {
            if (!mState.getProduct().getIngredientsFromPalmOilTags().toString().replace("[", "").replace("]", "").isEmpty()) {
                palmOilProduct.setVisibility(View.VISIBLE);
                palmOilProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtPalmOilProduct) + "</b>" + ' ' + mState.getProduct().getIngredientsFromPalmOilTags().toString().replace("[", "").replace("]", "")));
            }
            if (!mState.getProduct().getIngredientsThatMayBeFromPalmOilTags().toString().replace("[", "").replace("]", "").isEmpty()) {
                mayBeFromPalmOilProduct.setVisibility(View.VISIBLE);
                mayBeFromPalmOilProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtMayBeFromPalmOilProduct) + "</b>" + ' ' + mState.getProduct().getIngredientsThatMayBeFromPalmOilTags().toString().replace("[", "").replace("]", "")));
            }
        }
        SpannableStringBuilder txt = new SpannableStringBuilder(Html.fromHtml(mState.getProduct().getAdditivesTags().toString().replace("[", "").replace("]", "").replace("en:", " ").replace("fr:", " ")).toString());
        txt = setSpanClickBetweenTokens(txt, containerView);
        if(!txt.toString().trim().isEmpty()) {
            additiveProduct.setMovementMethod(LinkMovementMethod.getInstance());
            additiveProduct.setText(txt, TextView.BufferType.SPANNABLE);
        } else {
            additiveProduct.setVisibility(View.GONE);
        }

    }

    private SpannableStringBuilder setSpanClickBetweenTokens(CharSequence text, final View view) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        Matcher m = CODE_PATTERN.matcher(ssb);
        while (m.find()) {
            final String tm = m.group();
            final List<Additive> la = Additive.find(Additive.class, "code = ?", tm.toUpperCase());
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
                ssb.setSpan(clickableSpan, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        ssb.insert(0, Html.fromHtml("<b>" + getString(R.string.txtAdditives) + "</b>" + ' '));
        return ssb;
    }

    private SpannableStringBuilder setSpanBoldBetweenTokens(CharSequence text) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        Matcher m = INGREDIENT_PATTERN.matcher(ssb);
        while (m.find()) {
            final String tm = m.group();
            for (String l: cleanAllergensMultipleOccurrences()) {
                if(l.equalsIgnoreCase(tm.replaceAll("[(),.-]+", ""))) {
                    StyleSpan bold = new StyleSpan(android.graphics.Typeface.BOLD);
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
        if (mState.getProduct() == null) {
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
}
