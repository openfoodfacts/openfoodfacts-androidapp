package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;

public class AddProductNutritionFactsFragment extends BaseFragment {

    private static final String[] PARAMS_OTHER_NUTRIENTS = {"nutriment_alpha-linolenic-acid",
            "nutriment_arachidic-acid",
            "nutriment_arachidonic-acid",
            "nutriment_behenic-acid",
            "nutriment_bicarbonate",
            "nutriment_biotin",
            "nutriment_butyric-acid",
            "nutriment_caffeine",
            "nutriment_calcium",
            "nutriment_capric-acid",
            "nutriment_caproic-acid",
            "nutriment_caprylic-acid",
            "nutriment_casein",
            "nutriment_cerotic-acid",
            "nutriment_chloride",
            "nutriment_cholesterol",
            "nutriment_chromium",
            "nutriment_copper",
            "nutriment_dihomo-gamma-linolenic-acid",
            "nutriment_docosahexaenoic-acid",
            "nutriment_eicosapentaenoic-acid",
            "nutriment_elaidic-acid",
            "nutriment_erucic-acid",
            "nutriment_fluoride",
            "nutriment_fructose",
            "nutriment_gamma-linolenic-acid",
            "nutriment_glucose",
            "nutriment_gondoic-acid",
            "nutriment_iodine",
            "nutriment_iron",
            "nutriment_lactose",
            "nutriment_lauric-acid",
            "nutriment_lignoceric-acid",
            "nutriment_linoleic-acid",
            "nutriment_magnesium",
            "nutriment_maltodextrins",
            "nutriment_maltose",
            "nutriment_manganese",
            "nutriment_mead-acid",
            "nutriment_melissic-acid",
            "nutriment_molybdenum",
            "nutriment_monounsaturated-fat",
            "nutriment_montanic-acid",
            "nutriment_myristic-acid",
            "nutriment_nervonic-acid",
            "nutriment_nucleotides",
            "nutriment_oleic-acid",
            "nutriment_omega-3-fat",
            "nutriment_omega-6-fat",
            "nutriment_omega-9-fat",
            "nutriment_palmitic-acid",
            "nutriment_pantothenic-acid",
            "nutriment_ph",
            "nutriment_phosphorus",
            "nutriment_polyols",
            "nutriment_polyunsaturated-fat",
            "nutriment_potassium",
            "nutriment_selenium",
            "nutriment_serum-proteins",
            "nutriment_silica",
            "nutriment_starch",
            "nutriment_stearic-acid",
            "nutriment_sucrose",
            "nutriment_taurine",
            "nutriment_trans-fat",
            "nutriment_vitamin-a",
            "nutriment_vitamin-b1",
            "nutriment_vitamin-b12",
            "nutriment_vitamin-b2",
            "nutriment_vitamin-b6",
            "nutriment_vitamin-b9",
            "nutriment_vitamin-c",
            "nutriment_vitamin-d",
            "nutriment_vitamin-e",
            "nutriment_vitamin-k",
            "nutriment_vitamin-pp",
            "nutriment_zinc"};
    @BindView(R.id.radio_group)
    RadioGroup radioGroup;
    @BindView(R.id.table_layout)
    TableLayout tableLayout;

    //index list stores the index of other nutrients which are used.
    private List<Integer> index = new ArrayList<>();
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product_nutrition_facts, container, false);
        ButterKnife.bind(this, view);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.for100g_100ml:
                    Toast.makeText(activity, "for 100g / 100ml", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.per_serving:
                    Toast.makeText(activity, "per serving", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

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

    /*@OnClick(R.id.btn_next)
    void next() {
        Activity activity = getActivity();
        if (activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).proceed();
        }
    }*/

    /*public void getDetails() {
        if (activity instanceof AddProductActivity) {
            if (!ingredients.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_INGREDIENTS, ingredients.getText().toString());
            }
            if (!traces.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_TRACES, traces.getText().toString());
            }
        }
    }*/

    @OnClick(R.id.btn_add_a_nutrient)
    void addNutrient() {
        new MaterialDialog.Builder(activity)
                .title("Choose a nutrient")
                .items(R.array.nutrients_array)
                .itemsCallback((dialog, itemView, position, text) -> {
                    Toast.makeText(activity, String.valueOf(position) + " : " + text, Toast.LENGTH_SHORT).show();
                    if (!index.contains(position)) {
                        index.add(position);
                        addNutrientRow(position, text);
                    }
                })
                .show();

    }

    private void addNutrientRow(int position, CharSequence text) {
        TableRow nutrient = new TableRow(activity);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, dpsToPixels(40));
        lp.topMargin = dpsToPixels(10);
        EditText editText = new EditText(activity);
        editText.setBackgroundResource(R.drawable.bg_edittext);
        editText.setHint(text);
        editText.setId(position);
        Toast.makeText(activity, PARAMS_OTHER_NUTRIENTS[editText.getId()], Toast.LENGTH_SHORT).show();
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setPadding(dpsToPixels(10), 0, dpsToPixels(10), 0);
        editText.setGravity(Gravity.CENTER_VERTICAL);
        editText.requestFocus();
        editText.setLayoutParams(lp);
        nutrient.addView(editText);

        Spinner spinner = new Spinner(activity);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                (activity, android.R.layout.simple_spinner_item, activity.getResources().getStringArray(R.array.weight_all_units));
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setBackgroundResource(R.drawable.spinner_weights_grey);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setPadding(dpsToPixels(1), 0, 0, 0);
        lp.setMargins(0, dpsToPixels(10), dpsToPixels(8), 0);
        spinner.setLayoutParams(lp);
        nutrient.addView(spinner);
        if (PARAMS_OTHER_NUTRIENTS[position].equals("nutriment_ph")) {
            spinner.setVisibility(View.INVISIBLE);
        }
        tableLayout.addView(nutrient);
    }

    private int dpsToPixels(int dps) {
        final float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }
}
