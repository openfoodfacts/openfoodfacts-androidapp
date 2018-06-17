package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.text.WordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;

public class AddProductOverviewFragment extends BaseFragment {

    private static final String PARAM_NAME = "product_name";
    private static final String PARAM_BARCODE = "code";
    private static final String PARAM_QUANTITY = "quantity";
    private static final String UNIT[] = {"g", "mg", "kg", "l", "ml", "cl", "fl oz"};
    private static final String PARAM_BRAND = "brands";
    private static final String PARAM_LANGUAGE = "lang";
    private static final String PARAM_PACKAGING = "packaging";
    private static final String PARAM_CATEGORIES = "categories";
    private static final String PARAM_LABELS = "labels";
    private static final String PARAM_ORIGIN = "origins";
    private static final String PARAM_EMB_CODE = "emb_codes_tag";
    private static final String PARAM_LINK = "link";
    private static final String PARAM_PURCHASE = "purchase_places";
    private static final String PARAM_STORE = "stores";
    private static final String PARAM_COUNTRIES = "countries";

    @BindView(R.id.section_manufacturing_details)
    TextView sectionManufacturingDetails;
    @BindView(R.id.section_purchasing_details)
    TextView sectionPurchasingDetails;
    @BindView(R.id.barcode)
    TextView barcode;
    @BindView(R.id.language)
    TextView language;
    @BindView(R.id.name)
    EditText name;
    @BindView(R.id.quantity)
    EditText quantity;
    @BindView(R.id.spinner_weight_unit)
    Spinner quantityUnit;
    @BindView(R.id.brand)
    EditText brand;
    @BindView(R.id.packaging)
    EditText packaging;
    @BindView(R.id.categories)
    EditText categories;
    @BindView(R.id.label)
    EditText label;
    @BindView(R.id.origin_of_ingredients)
    EditText originOfIngredients;
    @BindView(R.id.manufacturing_place)
    EditText manufacturingPlace;
    @BindView(R.id.emb_code)
    EditText embCode;
    @BindView(R.id.link)
    EditText link;
    @BindView(R.id.country_where_purchased)
    EditText countryWherePurchased;
    @BindView(R.id.stores)
    EditText stores;
    @BindView(R.id.countries_where_sold)
    EditText countriesWhereSold;
    private String languageCode;
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product_overview, container, false);
        ButterKnife.bind(this, view);
        String currentLang = LocaleHelper.getLanguage(activity);
        setProductLanguage(currentLang);
        return view;
    }

    private void setProductLanguage(String lang) {
        languageCode = lang;
        Locale current = LocaleHelper.getLocale(lang);
        language.setText("Product language : ");
        language.append(WordUtils.capitalize(current.getDisplayName(current)));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @OnClick(R.id.btn_next)
    void next() {
        if (activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).proceed();
        }
    }

    @OnClick(R.id.btnAddImageFront)
    void addFrontImage() {
        Toast.makeText(getContext(), "Perform network call for uploading pic", Toast.LENGTH_SHORT).show();
    }

    public void getDetails() {
        if (activity instanceof AddProductActivity) {
            if (!barcode.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_BARCODE, barcode.getText().toString());
            }
            if (!languageCode.isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_LANGUAGE, languageCode);
            }
            if (!name.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_NAME, name.getText().toString());
            }
            if (!quantity.getText().toString().isEmpty()) {
                String qty = quantity.getText().toString() + UNIT[quantityUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_QUANTITY, qty);
            }
            if (!brand.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_BRAND, brand.getText().toString());
            }
            if (!packaging.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_PACKAGING, packaging.getText().toString());
            }
            if (!categories.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_CATEGORIES, categories.getText().toString());
            }
            if (!label.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_LABELS, label.getText().toString());
            }
            if (!originOfIngredients.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_ORIGIN, originOfIngredients.getText().toString());
            }
            if (!embCode.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_EMB_CODE, embCode.getText().toString());
            }
            if (!link.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_LINK, link.getText().toString());
            }
            if (!countryWherePurchased.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_PURCHASE, countryWherePurchased.getText().toString());
            }
            if (!stores.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_STORE, stores.getText().toString());
            }
            if (!countriesWhereSold.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_COUNTRIES, countriesWhereSold.getText().toString());
            }
        }
    }

    @OnClick(R.id.section_manufacturing_details)
    void toggleManufacturingSectionVisibility() {
        if (manufacturingPlace.getVisibility() != View.VISIBLE) {
            originOfIngredients.setVisibility(View.VISIBLE);
            manufacturingPlace.setVisibility(View.VISIBLE);
            embCode.setVisibility(View.VISIBLE);
            link.setVisibility(View.VISIBLE);
            originOfIngredients.requestFocus();
            sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up_grey_24dp, 0);
        } else {
            originOfIngredients.setVisibility(View.GONE);
            manufacturingPlace.setVisibility(View.GONE);
            embCode.setVisibility(View.GONE);
            link.setVisibility(View.GONE);
            sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
        }
    }

    @OnClick(R.id.section_purchasing_details)
    void togglePurchasingSectionVisibility() {
        if (stores.getVisibility() != View.VISIBLE) {
            countryWherePurchased.setVisibility(View.VISIBLE);
            stores.setVisibility(View.VISIBLE);
            countriesWhereSold.setVisibility(View.VISIBLE);
            countryWherePurchased.requestFocus();
            sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up_grey_24dp, 0);
        } else {
            countryWherePurchased.setVisibility(View.GONE);
            stores.setVisibility(View.GONE);
            countriesWhereSold.setVisibility(View.GONE);
            sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
        }
    }

    @OnClick(R.id.hint_emb_code)
    void toastEmbCodeHint() {
        Toast.makeText(activity, "In Europe, code in an ellipse with the 2 country initials followed by a number and CE.\n" +
                "Examples: EMB 53062, FR 62.448.034 CE, 84 R 20, 33 RECOLTANT 522, FSSL 10013011001409", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.language)
    void selectProductLanguage() {
        String[] localeValues = activity.getResources().getStringArray(R.array.languages_array);
        String[] localeLabels = new String[localeValues.length];
        List<String> finalLocalValues = new ArrayList<>();
        List<String> finalLocalLabels = new ArrayList<>();
        int selectedIndex = 0;
        for (int i = 0; i < localeValues.length; i++) {
            if (localeValues[i].equals(languageCode)) {
                selectedIndex = i;
            }
            Locale current = LocaleHelper.getLocale(localeValues[i]);
            if (current != null) {
                localeLabels[i] = WordUtils.capitalize(current.getDisplayName(current));
                finalLocalLabels.add(localeLabels[i]);
                finalLocalValues.add(localeValues[i]);
            }
        }
        new MaterialDialog.Builder(activity)
                .title(R.string.preference_choose_language_dialog_title)
                .items(finalLocalLabels)
                .itemsCallbackSingleChoice(selectedIndex, (dialog, view, which, text) -> {
                    setProductLanguage(finalLocalValues.get(which));
                    return true;
                })
                .positiveText(R.string.ok_button)
                .show();
    }
}
