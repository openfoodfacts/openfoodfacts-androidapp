package openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagConfig;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.views.ContinuousScanActivity;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.product.ProductActivity;

public class IngredientsWithTagDialogFragment extends DialogFragment {
    private SharedPreferences prefs;
    private DialogInterface.OnDismissListener onDismissListener;

    public static IngredientsWithTagDialogFragment newInstance(Product product, AnalysisTagConfig config) {
        IngredientsWithTagDialogFragment frag = new IngredientsWithTagDialogFragment();
        Bundle args = new Bundle();
        args.putString("tag", config.getAnalysisTag());
        args.putString("type", config.getType());
        args.putString("icon_url", config.getIconUrl());
        args.putString("color", config.getColor());
        args.putString("name", config.getName().getName());
        if (product.getIngredients() == null || product.getIngredients().isEmpty()) {
            args.putBoolean("missing_ingredients", true);
        } else {
            String showIngredients = config.getName().getShowIngredients();
            if (showIngredients != null) {
                args.putSerializable("ingredients", getMatchingIngredientsText(product, showIngredients.split(":")));
            }
        }
        frag.setArguments(args);
        return frag;
    }

    private static String getMatchingIngredientsText(Product product, String[] showIngredients) {
        ArrayList<String> matchingIngredients = new ArrayList<>();
        List<LinkedHashMap<String, String>> ingredients = product.getIngredients();
        for (LinkedHashMap<String, String> ingredient :
            ingredients) {
            if (showIngredients[1].equals(ingredient.get(showIngredients[0]))) {
                final String text = ingredient.get("text");
                if (text != null) {
                    matchingIngredients.add(text.toLowerCase().replace("_", ""));
                }
            }
        }

        if (matchingIngredients.isEmpty()) {
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
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setGravity(Gravity.CENTER);

        if (getActivity() != null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            String tag = getArguments().getString("tag");
            String type = getArguments().getString("type");
            String iconUrl = getArguments().getString("icon_url");
            String color = getArguments().getString("color");
            String name = getArguments().getString("name");
            String ingredients = getArguments().getString("ingredients");
            boolean missingIngredients = getArguments().getBoolean("missing_ingredients", false);

            AppCompatImageView icon = getView().findViewById(R.id.icon);
            Picasso.get()
                .load(iconUrl)
                .into(icon);
            Drawable background = getResources().getDrawable(R.drawable.rounded_button);
            background.setColorFilter(Color.parseColor(color), android.graphics.PorterDuff.Mode.SRC_IN);
            getView().findViewById(R.id.icon_frame).setBackground(background);

            ((AppCompatTextView) getView().findViewById(R.id.title)).setText(name);

            SwitchCompat sc = getView().findViewById(R.id.cb);
            sc.setText(getString(R.string.display_analysis_tag_status, type));
            sc.setChecked(prefs.getBoolean(type, true));
            sc.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(type, isChecked).apply());

            String messageStr = getString(R.string.ingredients_in_this_product_are, name.toLowerCase());
            AppCompatTextView helpNeeded = getView().findViewById(R.id.helpNeeded);
            boolean showHelpTranslate = tag != null && tag.contains("unknown");
            boolean showHelpExtract = showHelpTranslate && missingIngredients;
            if (showHelpExtract) {
                messageStr = getString(R.string.unknown_status_missing_ingredients);
                helpNeeded.setText(Html.fromHtml("<u>" + getString(R.string.help_extract_ingredients, name.toLowerCase()) + "</u>"));
                helpNeeded.setOnClickListener(v -> {
                    dismiss();
                    if (getActivity() instanceof ContinuousScanActivity) {
                        ((ContinuousScanActivity) getActivity()).showIngredientsTab();
                    } else if (getActivity() instanceof ProductActivity) {
                        ((ProductActivity) getActivity()).showIngredientsTab();
                    }
                });
                helpNeeded.setVisibility(View.VISIBLE);
            } else if (showHelpTranslate) {
                messageStr = getString(R.string.unknown_status_no_translation);
                helpNeeded.setText(Html.fromHtml("<u>" + getString(R.string.help_translate_ingredients) + "</u>"));
                helpNeeded.setOnClickListener(v -> {
                    CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
                    CustomTabActivityHelper.openCustomTab(
                        getActivity(),// activity
                        customTabsIntent,
                        Uri.parse(getString(R.string.help_translate_ingredients_link, Locale.getDefault().getLanguage())),
                        (activity, uri) -> {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(uri);
                            activity.startActivity(i);
                        }
                    );
                });
                helpNeeded.setVisibility(View.VISIBLE);
            } else {
                if (!TextUtils.isEmpty(ingredients)) {
                    messageStr = getString(R.string.ingredients_in_this_product, name.toLowerCase()) + ingredients;
                }
                helpNeeded.setVisibility(View.GONE);
            }
            AppCompatTextView message = getView().findViewById(R.id.message);
            message.setText(Html.fromHtml(messageStr));

            getView().findViewById(R.id.close).setOnClickListener(v -> dismiss());
        }
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }
}
