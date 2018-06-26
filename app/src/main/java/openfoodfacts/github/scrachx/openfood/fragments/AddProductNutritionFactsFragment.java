package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.NUTRITION;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;

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
            "nutriment_vitamin-pp",
            "nutriment_vitamin-b6",
            "nutriment_vitamin-b9",
            "nutriment_vitamin-c",
            "nutriment_vitamin-d",
            "nutriment_vitamin-e",
            "nutriment_vitamin-k",
            "nutriment_zinc"};

    private static final String ALL_UNIT[] = {"g", "mg", "µg", "% DV", "IU"};
    private static final String UNIT[] = {"g", "mg", "µg"};
    private static final String PARAM_NO_NUTRITION_DATA = "no_nutrition_data";
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

    @BindView(R.id.checkbox_no_nutrition_data)
    CheckBox noNutritionData;
    @BindView(R.id.nutrition_facts_layout)
    ConstraintLayout nutritionFactsLayout;
    @BindView(R.id.btnAddImageNutritionFacts)
    ImageView imageNutritionFacts;
    @BindView(R.id.imageProgress)
    ProgressBar imageProgress;
    @BindView(R.id.imageProgressText)
    TextView imageProgressText;
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
    private File photoFile;
    private String code;
    private double starchValue = 0.0;
    private int starchUnit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product_nutrition_facts, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle b = getArguments();
        if (b != null) {
            Product product = (Product) b.getSerializable("product");
            if (product != null) {
                code = product.getCode();
            }
        } else {
            Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show();
            activity.finish();
        }
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

    @OnClick(R.id.btnAddImageNutritionFacts)
    void addNutritionFactsImage() {
        if (ContextCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            EasyImage.openCamera(this, 0);
        }
    }

    @OnClick(R.id.btn_add)
    void next() {
        Activity activity = getActivity();
        if (activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).proceed();
        }
    }

    @OnTextChanged(value = R.id.salt, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTextChanged() {
        double sodiumValue = 0;
        try {
            sodiumValue = Double.valueOf(salt.getText().toString()) * 0.39370078740157477;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        sodium.setText(String.valueOf(sodiumValue));
        sodiumUnit.setSelection(saltUnit.getSelectedItemPosition());
    }

    @OnItemSelected(value = R.id.spinner_salt_unit, callback = OnItemSelected.Callback.ITEM_SELECTED)
    void onItemSelected() {
        sodiumUnit.setSelection(saltUnit.getSelectedItemPosition());
    }

    @OnCheckedChanged(R.id.checkbox_no_nutrition_data)
    void onCheckedChanged(boolean isChecked) {
        if (isChecked) {
            nutritionFactsLayout.setVisibility(View.GONE);
        } else {
            nutritionFactsLayout.setVisibility(View.VISIBLE);
        }
    }

    // checks alcohol % value and ensures it is not greater than 100%
    @OnTextChanged(value = R.id.alcohol, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void checkValue() {
        try {
            double alcoholValue = Double.valueOf(alcohol.getText().toString());
            if (alcoholValue > 100) {
                alcoholValue = 100.0;
                alcohol.setText(String.valueOf(alcoholValue));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public boolean isCheckPassed() {
        // check that value of (sugar + starch) is not greater than value of carbohydrates
        if (!carbohydrate.getText().toString().isEmpty()) {
            Double carbsValue, sugarValue = 0.0;
            int carbsUnit, sugar_Unit;
            try {
                carbsValue = Double.valueOf(carbohydrate.getText().toString());
                if (!sugar.getText().toString().isEmpty())
                    sugarValue = Double.valueOf(sugar.getText().toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return false;
            }
            carbsUnit = carbohydrateUnit.getSelectedItemPosition();
            sugar_Unit = sugarUnit.getSelectedItemPosition();
            //convert all the values to grams
            switch (carbsUnit) {
                case 1:
                    carbsValue /= 1000;
                    break;
                case 2:
                    carbsValue /= 1000000;
                    break;
            }
            switch (sugar_Unit) {
                case 1:
                    sugarValue /= 1000;
                    break;
                case 2:
                    sugarValue /= 1000000;
                    break;
            }
            switch (starchUnit) {
                case 1:
                    starchValue /= 1000;
                    break;
                case 2:
                    starchValue /= 1000000;
            }
            if ((sugarValue + starchValue) > carbsValue) {
                carbohydrate.requestFocus();
                carbohydrate.setError("Carbohydrate can't be less than the sum of sugar and starch");
                return false;
            } else
                return true;
        }
        return true;
    }

    public void getDetails() {
        if (activity instanceof AddProductActivity) {
            if (noNutritionData.isChecked()) {
                ((AddProductActivity) activity).addToMap(PARAM_NO_NUTRITION_DATA, "on");
            } else {
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
            // checks pH value and ensures it is not greater than 14.0
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        double pHValue = Double.valueOf(editText.getText().toString());
                        if (pHValue > 14) {
                            pHValue = 14;
                            editText.setText(String.valueOf(pHValue));
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (PARAMS_OTHER_NUTRIENTS[position].equals("nutriment_starch")) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>
                    (activity, android.R.layout.simple_spinner_item, activity.getResources().getStringArray(R.array.weights_array));
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);

            // save value of starch for checking later.
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        starchValue = Double.valueOf(editText.getText().toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            });
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    starchUnit = spinner.getSelectedItemPosition();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        tableLayout.addView(nutrient);
    }

    private int dpsToPixels(int dps) {
        final float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                photoFile = new File((resultUri.getPath()));
                ProductImage image = new ProductImage(code, NUTRITION, photoFile);
                image.setFilePath(resultUri.getPath());
                if (activity instanceof AddProductActivity) {
                    ((AddProductActivity) activity).addToPhotoMap(image, 2);
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e("Crop image error", result.getError().toString());
            }
        }
        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                CropImage.activity(Uri.fromFile(imageFiles.get(0))).setAllowFlipping(false)
                        .setOutputUri(Utils.getOutputPicUri(getContext())).start(activity.getApplicationContext(), AddProductNutritionFactsFragment.this);
            }
        });
    }

    public void showImageProgress() {
        imageProgress.setVisibility(View.VISIBLE);
        imageProgressText.setVisibility(View.VISIBLE);
        imageNutritionFacts.setVisibility(View.INVISIBLE);
    }

    public void hideImageProgress(boolean errorInUploading) {
        imageProgress.setVisibility(View.GONE);
        imageProgressText.setVisibility(View.GONE);
        imageNutritionFacts.setVisibility(View.VISIBLE);
        if (!errorInUploading) {
            Picasso.with(activity)
                    .load(photoFile)
                    .into(imageNutritionFacts);
            Toast.makeText(activity, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "You seem offline, images will be uploaded when network is available", Toast.LENGTH_SHORT).show();
        }
    }
}
