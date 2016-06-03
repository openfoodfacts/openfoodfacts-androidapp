package openbeautyfacts.github.scrachx.openfood.fragments;

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
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import openbeautyfacts.github.scrachx.openfood.R;
import openbeautyfacts.github.scrachx.openfood.models.Additive;
import openbeautyfacts.github.scrachx.openfood.models.State;

public class IngredientsProductFragment extends BaseFragment {

    @Bind(R.id.textIngredientProduct) TextView ingredientProduct;
    @Bind(R.id.textSubstanceProduct) TextView substanceProduct;
    @Bind(R.id.textTraceProduct) TextView traceProduct;
    @Bind(R.id.textAdditiveProduct) TextView additiveProduct;
    @Bind(R.id.textPalmOilProduct) TextView palmOilProduct;
    @Bind(R.id.textMayBeFromPalmOilProduct) TextView mayBeFromPalmOilProduct;
    @Bind(R.id.ingredientContainer) ViewGroup containerView;
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

        SpannableStringBuilder txtIngredients = new SpannableStringBuilder(Html.fromHtml(mState.getProduct().getIngredientsText().replace("_","")));
        txtIngredients = setSpanBoldBetweenTokens(txtIngredients);
        ingredientProduct.setText(txtIngredients);

        substanceProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtSubstances) + "</b>" + ' ' + cleanAllergensString()));
        String traces;
        if (mState.getProduct().getCategories() == null) {
            traces = mState.getProduct().getTraces();
        } else {
            traces = mState.getProduct().getTraces().replace(",", ", ");
        }
        traceProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtTraces) + "</b>" + ' ' + traces));
        additiveProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtAdditives) + "</b>" + ' ' + mState.getProduct().getAdditivesTags().toString().replace("[", "").replace("]", "").replace("en:", " ").replace("fr:", " ")));

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
        additiveProduct.setMovementMethod(LinkMovementMethod.getInstance());
        additiveProduct.setText(txt, TextView.BufferType.SPANNABLE);
    }

    private SpannableStringBuilder setSpanClickBetweenTokens(CharSequence text, final View view) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        Pattern p = Pattern.compile("[eE][a-zA-Z0-9]+");
        Matcher m = p.matcher(ssb);
        while (m.find()) {
            final String tm = m.group();
            final List<Additive> la = Additive.find(Additive.class, "code = ?", tm.toUpperCase());
            if (la.size() == 2) {
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View v) {
                        Additive a;
                        if (Locale.getDefault().getLanguage().contains("fr")) {
                            a = la.get(0);
                        } else {
                            a = la.get(1);
                        }
                        new MaterialDialog.Builder(view.getContext())
                                .title(a.getCode() + " : " + a.getName())
                                .content(a.getRisk().toUpperCase())
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
        Pattern p = Pattern.compile("[a-zA-Z0-9(),àâçéèêëîïôûùüÿñæœ.-]+");
        Matcher m = p.matcher(ssb);
        while (m.find()) {
            final String tm = m.group();
            for (String l:cleanAllergensMultipleOccurence()) {
                if(l.toLowerCase().equals(tm.toLowerCase().replaceAll("[(),.-]+", ""))) {
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

    private List<String> cleanAllergensMultipleOccurence() {
        List<String> list = new ArrayList<>();
        Pattern p = Pattern.compile("[a-zA-Z0-9àâçéèêëîïôûùüÿñæœ]+");
        Matcher m = p.matcher(mState.getProduct().getAllergens().replace(",", ""));
        while (m.find()) {
            final String tma = m.group();
            boolean canAdd = true;
            if(list.size() == 0) list.add(tma);
            for (int i = 0; i < list.size(); i++) {
                if(list.get(i).toLowerCase().equals(tma.toLowerCase())) canAdd = false;
            }
            if(canAdd) {
                list.add(tma);
            }
        }
        return list;
    }

    private String cleanAllergensString() {
        StringBuilder allergens = new StringBuilder("");
        for (String l:cleanAllergensMultipleOccurence()) {
            allergens.append(l + " ");
        }
        return allergens.toString();
    }
}
