package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableStringBuilder;
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

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
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

        List<String> allergens = getAllergens();

        if(mState != null && product.getIngredientsText() != null) {
            SpannableStringBuilder txtIngredients = new SpannableStringBuilder(Html.fromHtml(product.getIngredientsText().replace("_","")));
            txtIngredients = setSpanBoldBetweenTokens(txtIngredients, allergens);
            if(!txtIngredients.toString().substring(txtIngredients.toString().indexOf(":")).trim().isEmpty()) {
                ingredientsProduct.setText(txtIngredients);
            } else {
                ingredientsProduct.setVisibility(View.GONE);
            }
        }

        if(!allergens.isEmpty()) {
            substanceProduct.append(bold(getString(R.string.txtSubstances)));
            substanceProduct.append(" ");
            for (String allergen : allergens) {
                substanceProduct.append(allergen);
                substanceProduct.append(" ");
            }
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
            ssb.setSpan(clickableSpan, 0, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(" ");
        }
        return ssb;
    }

    private SpannableStringBuilder setSpanBoldBetweenTokens(CharSequence text, List<String> allergens) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        Matcher m = INGREDIENT_PATTERN.matcher(ssb);
        while (m.find()) {
            final String tm = m.group();
            final String allergenValue = tm.replaceAll("[(),.-]+", "");

            for (String allergen: allergens) {
                if(allergen.equalsIgnoreCase(allergenValue)) {
                    int start = m.start();
                    int end = m.end();

                    if(tm.contains("(")) {
                        start += 1;
                    } else if(tm.contains(")")) {
                        end -= 1;
                    }

                    ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        ssb.insert(0, Html.fromHtml("<b>" + getString(R.string.txtIngredients) + "</b>" + ' '));
        return ssb;
    }

    private List<String> getAllergens() {
        if (mState.getProduct() == null || mState.getProduct().getAllergens() == null) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>();
        Matcher m = ALLERGEN_PATTERN.matcher(mState.getProduct().getAllergens().replace(",", ""));
        while (m.find()) {
            final String tma = m.group();
            boolean canAdd = true;

            for (String allergen : list) {
                if(tma.equalsIgnoreCase(allergen)){
                    canAdd = false;
                    break;
                }
            }

            if (canAdd) {
                list.add(tma);
            }
        }
        return list;
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
