package openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity;
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

public class IngredientsWithTagDialogFragment extends DialogFragment {
    private static final String TAG_KEY = "tag";
    private static final String TYPE_KEY = "type";
    private static final String TYPE_NAME_KEY = "type_name";
    private static final String ICON_URL_KEY = "icon_url";
    private static final String COLOR_KEY = "color";
    private static final String NAME_KEY = "name";
    private static final String INGREDIENTS_IMAGE_URL_KEY = "ingredients_image_url";
    private static final String INGREDIENTS_KEY = "ingredients";
    private static final String MISSING_INGREDIENTS_KEY = "missing_ingredients";
    private static final String PHOTOS_TO_BE_VALIDATED_KEY = "photos_to_be_validated";
    private static final String AMBIGUOUS_INGREDIENT_KEY = "ambiguous_ingredient";
    private DialogInterface.OnDismissListener onDismissListener;

    public static IngredientsWithTagDialogFragment newInstance(Product product, AnalysisTagConfig config) {
        IngredientsWithTagDialogFragment frag = new IngredientsWithTagDialogFragment();
        Bundle args = new Bundle();
        args.putString(TAG_KEY, config.getAnalysisTag());
        args.putString(TYPE_KEY, config.getType());
        args.putString(TYPE_NAME_KEY, config.getTypeName());
        args.putString(ICON_URL_KEY, config.getIconUrl());
        args.putString(COLOR_KEY, config.getColor());
        args.putString(NAME_KEY, config.getName().getName());
        args.putString(INGREDIENTS_IMAGE_URL_KEY, product.getImageIngredientsUrl());

        if (product.getIngredients() == null || product.getIngredients().isEmpty()) {
            final List<String> statesTags = product.getStatesTags();
            boolean ingredientsToBeCompleted = false;
            boolean photosToBeValidated = false;

            for (String stateTag : statesTags) {
                if (stateTag.equals("en:ingredients-to-be-completed")) {
                    ingredientsToBeCompleted = true;
                } else if (stateTag.equals("en:photos-to-be-validated")) {
                    photosToBeValidated = true;
                }
            }

            if (ingredientsToBeCompleted && photosToBeValidated) {
                args.putBoolean(PHOTOS_TO_BE_VALIDATED_KEY, true);
            } else {
                args.putBoolean(MISSING_INGREDIENTS_KEY, true);
            }
        } else {
            String showIngredients = config.getName().getShowIngredients();
            if (showIngredients != null) {
                args.putSerializable(INGREDIENTS_KEY, getMatchingIngredientsText(product, showIngredients.split(":")));
            }
            List<String> ambiguousIngredient = product.getIngredients().stream()
                .filter(ingredientList -> ingredientList.containsKey(config.getType()) && ingredientList.containsValue("maybe"))
                .filter(ingredient -> ingredient.containsKey("text")).map(ingredient -> ingredient.get("text"))
                .collect(Collectors.toList());
            if (!ambiguousIngredient.isEmpty()) {
                args.putString(AMBIGUOUS_INGREDIENT_KEY, StringUtils.join(ambiguousIngredient, ","));
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

        final View rootView = inflater.inflate(R.layout.ingredients_with_tag, container);
        requireDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        requireDialog().getWindow().setGravity(Gravity.CENTER);

        if (getActivity() == null) {
            return rootView;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        final Bundle arguments = requireArguments();
        String tag = arguments.getString(TAG_KEY);
        String type = arguments.getString(TYPE_KEY);
        String typeName = arguments.getString(TYPE_NAME_KEY);
        String iconUrl = arguments.getString(ICON_URL_KEY);
        String color = arguments.getString(COLOR_KEY);
        String name = arguments.getString(NAME_KEY);
        String ingredients = arguments.getString(INGREDIENTS_KEY);
        String ambiguousIngredient = arguments.getString(AMBIGUOUS_INGREDIENT_KEY);

        AppCompatImageView icon = rootView.findViewById(R.id.icon);
        Utils.picassoBuilder(getActivity())
            .load(iconUrl)
            .into(icon);
        Drawable background = getResources().getDrawable(R.drawable.rounded_button);
        background.setColorFilter(Color.parseColor(color), android.graphics.PorterDuff.Mode.SRC_IN);
        rootView.findViewById(R.id.icon_frame).setBackground(background);

        ((AppCompatTextView) rootView.findViewById(R.id.title)).setText(name);

        SwitchCompat sc = rootView.findViewById(R.id.cb);
        sc.setText(getString(R.string.display_analysis_tag_status, typeName.toLowerCase()));
        sc.setChecked(prefs.getBoolean(type, true));
        sc.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(type, isChecked).apply());

        Spanned messageToBeShown = Html.fromHtml(getString(R.string.ingredients_in_this_product_are, name.toLowerCase()));
        AppCompatButton helpNeeded = rootView.findViewById(R.id.helpNeeded);
        boolean showHelpTranslate = tag != null && tag.contains("unknown");
        AppCompatImageView image = rootView.findViewById(R.id.image);
        if (arguments.getBoolean(PHOTOS_TO_BE_VALIDATED_KEY, false)) {
            messageToBeShown = Html.fromHtml(getString(R.string.unknown_status_missing_ingredients));
            image.setImageResource(R.drawable.ic_add_a_photo_dark_48dp);
            image.setOnClickListener(v -> goToAddPhoto());
            helpNeeded.setText(Html.fromHtml(getString(R.string.add_photo_to_extract_ingredients)));
            helpNeeded.setOnClickListener(v -> goToAddPhoto());
        } else if (tag != null && ambiguousIngredient != null) {
            messageToBeShown = Html.fromHtml(getString(R.string.unknown_status_ambiguous_ingredients, ambiguousIngredient));
            helpNeeded.setVisibility(View.GONE);
        } else if (showHelpTranslate && arguments.getBoolean(MISSING_INGREDIENTS_KEY, false)) {
            String ingredientsImageUrl = arguments.getString(INGREDIENTS_IMAGE_URL_KEY);
            Utils.picassoBuilder(getActivity())
                .load(ingredientsImageUrl)
                .into(image);
            image.setOnClickListener(v -> goToExtract());
            messageToBeShown = Html.fromHtml(getString(R.string.unknown_status_missing_ingredients));
            helpNeeded.setText(Html.fromHtml(getString(R.string.help_extract_ingredients, typeName.toLowerCase())));
            helpNeeded.setOnClickListener(v -> goToExtract());
            helpNeeded.setVisibility(View.VISIBLE);
        } else if (showHelpTranslate) {
            messageToBeShown = Html.fromHtml(getString(R.string.unknown_status_no_translation));
            helpNeeded.setText(Html.fromHtml(getString(R.string.help_translate_ingredients)));
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
            image.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(ingredients)) {
                messageToBeShown = Html.fromHtml(getString(R.string.ingredients_in_this_product, name.toLowerCase()) + ingredients);
            }
            helpNeeded.setVisibility(View.GONE);
        }
        AppCompatTextView message = rootView.findViewById(R.id.message);
        message.setText(messageToBeShown);

        rootView.findViewById(R.id.close).setOnClickListener(v -> dismiss());
        return rootView;
    }

    private void goToAddPhoto() {
        dismiss();
        if (getActivity() instanceof ContinuousScanActivity) {
            ((ContinuousScanActivity) getActivity()).showIngredientsTab(ProductViewActivity.ShowIngredientsAction.SEND_UPDATED);
        } else if (getActivity() instanceof ProductViewActivity) {
            ((ProductViewActivity) getActivity()).showIngredientsTab(ProductViewActivity.ShowIngredientsAction.SEND_UPDATED);
        }
    }

    private void goToExtract() {
        dismiss();
        if (getActivity() instanceof ContinuousScanActivity) {
            ((ContinuousScanActivity) getActivity()).showIngredientsTab(ProductViewActivity.ShowIngredientsAction.PERFORM_OCR);
        } else if (getActivity() instanceof ProductViewActivity) {
            ((ProductViewActivity) getActivity()).showIngredientsTab(ProductViewActivity.ShowIngredientsAction.PERFORM_OCR);
        }
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }
}
