package openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;

public class IngredientsWithTagDialogFragment extends DialogFragment {
    private SharedPreferences prefs;

    public static IngredientsWithTagDialogFragment newInstance(Product product, String tag, String value) {
        IngredientsWithTagDialogFragment frag = new IngredientsWithTagDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("ingredients", getMatchingIngredientsText(product, tag, value));
        args.putString("tag", tag);
        args.putString("value", value);
        frag.setArguments(args);
        return frag;
    }

    private static String getMatchingIngredientsText(Product product, String tag, String value) {
        ArrayList<String> matchingIngredients = new ArrayList<>();
        List<LinkedHashMap<String, String>> ingredients = product.getIngredients();
        for (LinkedHashMap<String, String> ingredient :
            ingredients) {
            if (ingredient.containsKey(tag)) {
                if (value.equals(ingredient.get(tag))) {
                    matchingIngredients.add(ingredient.get("text").replaceAll("_", ""));
                }
            }
        }

        if (matchingIngredients.size() == 0) {
            return null;
        }

        StringBuilder text = new StringBuilder(128);
        text.append(" <b>");
        text.append(matchingIngredients.get(0));
        for (int i = 1; i < matchingIngredients.size(); ++i) {
            text.append(", ");
            text.append(matchingIngredients.get(i));
        }
        text.append("</b>");

        return text.toString();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ingredients_with_tag, container);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() != null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            String ingredientsText = getArguments().getString("ingredients");
            String tag = getArguments().getString("tag");
            String value = getArguments().getString("value");

            int iconResId;
            int iconColor;
            int titleResId;
            int rbTextResId;
            String messageStr = null;
            String prefKey;
            switch (tag) {
                case "from_palm_oil":
                    if ("maybe".equals(value)) {
                        titleResId = R.string.maybe_from_palm_oil;
                        iconResId = R.drawable.ic_monkey_uncertain;
                        iconColor = ContextCompat.getColor(getActivity(), R.color.monkey_uncertain);
                    } else if ("unknown".equals(value)) {
                        titleResId = R.string.unknown_palm_oil;
                        iconResId = R.drawable.ic_monkey_uncertain;
                        iconColor = ContextCompat.getColor(getActivity(), R.color.monkey_unknown);
                        messageStr = getString(R.string.unknown_palm_oil_status);
                    } else if ("yes".equals(value)) {
                        titleResId = R.string.from_palm_oil;
                        iconResId = R.drawable.ic_monkey_unhappy;
                        iconColor = ContextCompat.getColor(getActivity(), R.color.monkey_sad);
                    } else {
                        titleResId = R.string.not_from_palm_oil;
                        iconResId = R.drawable.ic_monkey_happy;
                        iconColor = ContextCompat.getColor(getActivity(), R.color.monkey_happy);
                        messageStr = getString(R.string.this_product_is, getString(titleResId).toLowerCase());
                    }
                    rbTextResId = R.string.preference_display_palm_oil_status;
                    prefKey = "enablePalmOilStatusDisplay";
                    break;
                case "vegetarian":
                    iconResId = R.drawable.ic_egg;
                    if ("maybe".equals(value)) {
                        titleResId = R.string.maybe_vegetarian;
                        iconColor = ContextCompat.getColor(getActivity(), R.color.monkey_uncertain);
                    } else if ("unknown".equals(value)) {
                        titleResId = R.string.unknown_vegetarian;
                        iconColor = ContextCompat.getColor(getActivity(), R.color.monkey_unknown);
                        messageStr = getString(R.string.unknown_vegetarian_status);
                    } else if ("no".equals(value)) {
                        titleResId = R.string.non_vegetarian;
                        iconColor = ContextCompat.getColor(getActivity(), R.color.monkey_sad);
                    } else {
                        titleResId = R.string.vegetarian;
                        iconColor = ContextCompat.getColor(getActivity(), R.color.monkey_happy);
                        messageStr = getString(R.string.this_product_is, getString(titleResId).toLowerCase());
                    }
                    rbTextResId = R.string.preference_display_vegetarian_status;
                    prefKey = "enableVegetarianStatusDisplay";
                    break;
                case "vegan":
                    iconResId = R.drawable.ic_leaf;
                    if ("maybe".equals(value)) {
                        titleResId = R.string.maybe_vegan;
                        iconColor = ContextCompat.getColor(getActivity(), R.color.monkey_uncertain);
                    } else if ("unknown".equals(value)) {
                        titleResId = R.string.unknown_vegan;
                        iconColor = ContextCompat.getColor(getActivity(), R.color.monkey_unknown);
                        messageStr = getString(R.string.unknown_vegan_status);
                    } else if ("no".equals(value)) {
                        titleResId = R.string.non_vegan;
                        iconColor = ContextCompat.getColor(getActivity(), R.color.monkey_sad);
                    } else {
                        titleResId = R.string.vegan;
                        iconColor = ContextCompat.getColor(getActivity(), R.color.monkey_happy);
                        messageStr = getString(R.string.this_product_is, getString(titleResId).toLowerCase());
                    }
                    rbTextResId = R.string.preference_display_vegan_status;
                    prefKey = "enableVeganStatusDisplay";
                    break;
                default:
                    return;
            }

            AppCompatImageView icon = getView().findViewById(R.id.icon);
            icon.setImageResource(iconResId);
            icon.setColorFilter(iconColor, android.graphics.PorterDuff.Mode.SRC_IN);

            ((AppCompatTextView) getView().findViewById(R.id.title)).setText(titleResId);

            SwitchCompat sc = getView().findViewById(R.id.cb);
            sc.setText(rbTextResId);
            sc.setChecked(prefs.getBoolean(prefKey, true));
            sc.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(prefKey, isChecked).apply();
                buttonView.getContext().sendBroadcast(new Intent("action_pref_changed"));
            });

            if (messageStr == null) {
                messageStr = getString(R.string.ingredients_in_this_product, getString(titleResId).toLowerCase()) + ingredientsText;
            }
            AppCompatTextView message = getView().findViewById(R.id.message);
            message.setText(Html.fromHtml(messageStr));

            getView().findViewById(R.id.close).setOnClickListener(v -> dismiss());
        }
    }
}