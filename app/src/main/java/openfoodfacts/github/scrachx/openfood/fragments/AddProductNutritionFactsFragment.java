package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;

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

    private static final String ALL_UNIT[] = {"g", "mg", "µg", "% DV", "IU"};
    private static final String UNIT[] = {"g", "mg", "µg"};
    private static final String PARAM_NUTRITION_DATA_PER = "nutrition_data_per";
    private static final String PARAM_SERVING_SIZE = "serving_size";
    private static final String PARAM_ENERGY = "nutriment_energy";
    private static final String PARAM_ENERGY_UNIT = "nutriment_energy_unit";
    private static final String PARAM_FAT = "nutriment_fat";
    private static final String PARAM_FAT_UNIT = "nutriment_fat_unit";
    private static final String PARAM_SATURATED_FAT = "nutriment_saturated-fat";
    private static final String PARAM_SATURATED_FAT_UNIT = "nutriment_saturated-fat_unit";
    private static final String PARAM_CARBOHYDRATE = "nutriment_carbohydrates";
    private static final String PARAM_CARBOHYDRATE_UNIT = "nutriment_carbohydrates_unit";
    private static final String PARAM_SUGAR = "nutriment_sugars";
    private static final String PARAM_SUGAR_UNIT = "nutriment_sugars_unit";
    private static final String PARAM_DIETARY_FIBER = "nutriment_fiber";
    private static final String PARAM_DIETARY_FIBER_UNIT = "nutriment_fiber_unit";
    private static final String PARAM_PROTEINS = "nutriment_proteins";
    private static final String PARAM_PROTEINS_UNIT = "nutriment_proteins_unit";
    private static final String PARAM_SODIUM = "nutriment_sodium";
    private static final String PARAM_SODIUM_UNIT = "nutriment_sodium_unit";
    private static final String PARAM_ALCOHOL = "nutriment_alcohol";

    @BindView(R.id.radio_group)
    RadioGroup radioGroup;
    @BindView(R.id.serving_size)
    EditText serving_size;
    @BindView(R.id.spinner_serving_unit)
    Spinner servingSizeUnit;
    @BindView(R.id.energy)
    EditText energy;
    @BindView(R.id.spinner_energy_unit)
    Spinner energyUnit;
    @BindView(R.id.fat)
    EditText fat;
    @BindView(R.id.spinner_fat_unit)
    Spinner fatUnit;
    @BindView(R.id.saturated_fat)
    EditText saturatedFat;
    @BindView(R.id.spinner_saturated_fat_unit)
    Spinner saturatedFatUnit;
    @BindView(R.id.carbohydrate)
    EditText carbohydrate;
    @BindView(R.id.spinner_carbohydrate_unit)
    Spinner carbohydrateUnit;
    @BindView(R.id.sugar)
    EditText sugar;
    @BindView(R.id.spinner_sugar_unit)
    Spinner sugarUnit;
    @BindView(R.id.dietary_fibre)
    EditText dietaryFiber;
    @BindView(R.id.spinner_dietary_fiber_unit)
    Spinner dietaryFiberUnit;
    @BindView(R.id.proteins)
    EditText proteins;
    @BindView(R.id.spinner_proteins_unit)
    Spinner proteinsUnit;
    @BindView(R.id.salt)
    EditText salt;
    @BindView(R.id.spinner_salt_unit)
    Spinner saltUnit;
    @BindView(R.id.sodium)
    EditText sodium;
    @BindView(R.id.spinner_sodium_unit)
    Spinner sodiumUnit;
    @BindView(R.id.alcohol)
    EditText alcohol;
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
        salt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                double sodiumValue = 0;
                try {
                    sodiumValue = Double.valueOf(salt.getText().toString()) * 0.39370078740157477;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                sodium.setText(String.valueOf(sodiumValue));
                sodiumUnit.setSelection(saltUnit.getSelectedItemPosition());
            }
        });
        saltUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sodiumUnit.setSelection(saltUnit.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

    @OnClick(R.id.btn_add)
    void next() {
        Activity activity = getActivity();
        if (activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).proceed();
        }
    }

    public void getDetails() {
        if (activity instanceof AddProductActivity) {
            if (radioGroup.getCheckedRadioButtonId() == R.id.for100g_100ml) {
                ((AddProductActivity) activity).addToMap(PARAM_NUTRITION_DATA_PER, "100g");
            } else if (radioGroup.getCheckedRadioButtonId() == R.id.per_serving) {
                ((AddProductActivity) activity).addToMap(PARAM_NUTRITION_DATA_PER, "serving");
            }
            if (!serving_size.getText().toString().isEmpty()) {
                String servingSize = serving_size.getText().toString() + UNIT[servingSizeUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_SERVING_SIZE, servingSize);
            }
            if (!energy.getText().toString().isEmpty()) {
                String s = energy.getText().toString();
                String unit = "kj";
                if (energyUnit.getSelectedItemPosition() == 0)
                    unit = "kcal";
                else if (energyUnit.getSelectedItemPosition() == 1)
                    unit = "kj";
                ((AddProductActivity) activity).addToMap(PARAM_ENERGY, s);
                ((AddProductActivity) activity).addToMap(PARAM_ENERGY_UNIT, unit);
            }
            if (!fat.getText().toString().isEmpty()) {
                String s = fat.getText().toString();
                String unit = UNIT[fatUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_FAT, s);
                ((AddProductActivity) activity).addToMap(PARAM_FAT_UNIT, unit);
            }
            if (!saturatedFat.getText().toString().isEmpty()) {
                String s = saturatedFat.getText().toString();
                String unit = UNIT[saturatedFatUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_SATURATED_FAT, s);
                ((AddProductActivity) activity).addToMap(PARAM_SATURATED_FAT_UNIT, unit);
            }
            if (!carbohydrate.getText().toString().isEmpty()) {
                String s = carbohydrate.getText().toString();
                String unit = UNIT[carbohydrateUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_CARBOHYDRATE, s);
                ((AddProductActivity) activity).addToMap(PARAM_CARBOHYDRATE_UNIT, unit);
            }
            if (!sugar.getText().toString().isEmpty()) {
                String s = sugar.getText().toString();
                String unit = UNIT[sugarUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_SUGAR, s);
                ((AddProductActivity) activity).addToMap(PARAM_SUGAR_UNIT, unit);
            }
            if (!dietaryFiber.getText().toString().isEmpty()) {
                String s = dietaryFiber.getText().toString();
                String unit = UNIT[dietaryFiberUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_DIETARY_FIBER, s);
                ((AddProductActivity) activity).addToMap(PARAM_DIETARY_FIBER_UNIT, unit);
            }
            if (!proteins.getText().toString().isEmpty()) {
                String s = proteins.getText().toString();
                String unit = UNIT[proteinsUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_PROTEINS, s);
                ((AddProductActivity) activity).addToMap(PARAM_PROTEINS_UNIT, unit);
            }
            if (!sodium.getText().toString().isEmpty()) {
                String s = sodium.getText().toString();
                String unit = UNIT[sodiumUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_SODIUM, s);
                ((AddProductActivity) activity).addToMap(PARAM_SODIUM_UNIT, unit);
            }
            if (!alcohol.getText().toString().isEmpty()) {
                String s = alcohol.getText().toString();
                ((AddProductActivity) activity).addToMap(PARAM_ALCOHOL, s);
            }

            //get the values of all the other nutrients from the table
            for (int i = 0; i < tableLayout.getChildCount(); i++) {
                View view = tableLayout.getChildAt(i);
                if (view instanceof TableRow) {
                    TableRow tableRow = (TableRow) view;
                    int id = 0;
                    for (int j = 0; j < tableRow.getChildCount(); j++) {
                        View v = tableRow.getChildAt(j);
                        if (v instanceof EditText) {
                            EditText editText = (EditText) v;
                            id = editText.getId();
                            if (!editText.getText().toString().isEmpty()) {
                                ((AddProductActivity) activity).addToMap(PARAMS_OTHER_NUTRIENTS[id], editText.getText().toString());
                            }
                        }
                        if (v instanceof Spinner) {
                            Spinner spinner = (Spinner) v;
                            ((AddProductActivity) activity).addToMap(PARAMS_OTHER_NUTRIENTS[id] + "_unit", ALL_UNIT[spinner.getSelectedItemPosition()]);
                        }
                    }
                }
            }
        }
    }

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
