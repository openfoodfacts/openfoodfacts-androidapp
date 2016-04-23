package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Additive;
import openfoodfacts.github.scrachx.openfood.models.State;

public class IngredientsProductFragment extends BaseFragment {

    @Bind(R.id.textIngredientProduct) TextView ingredientProduct;
    @Bind(R.id.textSubstanceProduct) TextView substanceProduct;
    @Bind(R.id.textTraceProduct) TextView traceProduct;
    @Bind(R.id.textAdditiveProduct) TextView additiveProduct;
    @Bind(R.id.textPalmOilProduct) TextView palmOilProduct;
    @Bind(R.id.textMayBeFromPalmOilProduct) TextView mayBeFromPalmOilProduct;

    @Bind(R.id.ingredientContainer) ViewGroup containerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_ingredients_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

        ingredientProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtIngredients) + "</b>" + ' ' + state.getProduct().getIngredientsText()));
        substanceProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtSubstances) + "</b>" + ' ' + state.getProduct().getAllergens()));
        String traces;
        if (state.getProduct().getCategories() == null) {
            traces = state.getProduct().getTraces();
        } else {
            traces = state.getProduct().getTraces().replace(",", ", ");
        }
        traceProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtTraces) + "</b>" + ' ' + traces));
        additiveProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtAdditives) + "</b>" + ' ' + state.getProduct().getAdditivesTags().toString().replace("[", "").replace("]", "").replace("en:", " ").replace("fr:", " ")));

        if (state.getProduct().getIngredientsFromPalmOilN() == 0 && state.getProduct().getIngredientsFromOrThatMayBeFromPalmOilN() == 0) {
            palmOilProduct.setVisibility(View.VISIBLE);
            palmOilProduct.setText(getString(R.string.txtPalm));
        } else {
            if (!state.getProduct().getIngredientsFromPalmOilTags().toString().replace("[", "").replace("]", "").isEmpty()) {
                palmOilProduct.setVisibility(View.VISIBLE);
                palmOilProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtPalmOilProduct) + "</b>" + ' ' + state.getProduct().getIngredientsFromPalmOilTags().toString().replace("[", "").replace("]", "")));
            }
            if (!state.getProduct().getIngredientsThatMayBeFromPalmOilTags().toString().replace("[", "").replace("]", "").isEmpty()) {
                mayBeFromPalmOilProduct.setVisibility(View.VISIBLE);
                mayBeFromPalmOilProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtMayBeFromPalmOilProduct) + "</b>" + ' ' + state.getProduct().getIngredientsThatMayBeFromPalmOilTags().toString().replace("[", "").replace("]", "")));
            }
        }
        SpannableStringBuilder txt = new SpannableStringBuilder(Html.fromHtml(state.getProduct().getAdditivesTags().toString().replace("[", "").replace("]", "").replace("en:", " ").replace("fr:", " ")).toString());
        txt = setSpanBetweenTokens(txt, containerView);
        additiveProduct.setMovementMethod(LinkMovementMethod.getInstance());
        additiveProduct.setText(txt, TextView.BufferType.SPANNABLE);
    }

    public SpannableStringBuilder setSpanBetweenTokens(CharSequence text, final View view) {

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
}
