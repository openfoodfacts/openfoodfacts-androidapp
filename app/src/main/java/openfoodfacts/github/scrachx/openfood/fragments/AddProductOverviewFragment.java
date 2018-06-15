package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;

public class AddProductOverviewFragment extends BaseFragment {

    private static final String PARAM_NAME = "product_name";
    private static final String PARAM_BARCODE = "code";
    private static final String PARAM_QUANTITY = "quantity";
    private static final String PARAM_BRAND = "brands";

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
    @BindView(R.id.brand)
    EditText brand;
    @BindView(R.id.packaging)
    EditText packaging;
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
        return view;
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
            if (!name.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_NAME, name.getText().toString());
            }
            if (!quantity.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_QUANTITY, quantity.getText().toString());
            }
            if (!brand.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_BRAND, brand.getText().toString());
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
}
