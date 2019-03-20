package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import butterknife.*;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.utils.QuantityParserUtil;
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import org.apache.commons.lang3.StringUtils;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    private static final String[] ALL_UNIT = {UnitUtils.UNIT_GRAM, UnitUtils.UNIT_MILLIGRAM, UnitUtils.UNIT_MICROGRAM, UnitUtils.UNIT_DV, UnitUtils.UNIT_IU};
    private static final String[] UNIT = {UnitUtils.UNIT_GRAM, UnitUtils.UNIT_MILLIGRAM, UnitUtils.UNIT_MICROGRAM};
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
    private static final String PARAM_SALT = "nutriment_salt";
    private static final String PARAM_SALT_UNIT = "nutriment_salt_unit";
    private static final String PARAM_SODIUM = "nutriment_sodium";
    private static final String PARAM_SODIUM_UNIT = "nutriment_sodium_unit";
    private static final String PARAM_ALCOHOL = "nutriment_alcohol";
    NumberKeyListener keyListener = new NumberKeyListener() {
        public int getInputType() {
            return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        }

        @Override
        protected char[] getAcceptedChars() {
            return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ',', '.', '~', '<', '>'};
        }
    };
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
    @BindView(R.id.energy_msg)
    TextView energy_msg;
    @BindView(R.id.energy)
    EditText energy;
    @BindView(R.id.spinner_energy_unit)
    Spinner energyUnit;
    @BindView(R.id.fat_msg)
    TextView fat_msg;
    @BindView(R.id.fat)
    EditText fat;
    @BindView(R.id.spinner_fat_unit)
    Spinner fatUnit;
    @BindView(R.id.saturated_fat_msg)
    TextView saturated_fat_msg;
    @BindView(R.id.saturated_fat)
    EditText saturatedFat;
    @BindView(R.id.spinner_saturated_fat_unit)
    Spinner saturatedFatUnit;
    @BindView(R.id.carbohydrate_msg)
    TextView carbohydrate_msg;
    @BindView(R.id.carbohydrate)
    EditText carbohydrate;
    @BindView(R.id.spinner_carbohydrate_unit)
    Spinner carbohydrateUnit;
    @BindView(R.id.sugar_msg)
    TextView sugar_msg;
    @BindView(R.id.sugar)
    EditText sugar;
    @BindView(R.id.spinner_sugar_unit)
    Spinner sugarUnit;
    @BindView(R.id.dietary_fibre_msg)
    TextView dietary_fibre_msg;
    @BindView(R.id.dietary_fibre)
    EditText dietaryFiber;
    @BindView(R.id.spinner_dietary_fiber_unit)
    Spinner dietaryFiberUnit;
    @BindView(R.id.proteins_msg)
    TextView proteins_msg;
    @BindView(R.id.proteins)
    EditText proteins;
    @BindView(R.id.spinner_proteins_unit)
    Spinner proteinsUnit;
    @BindView(R.id.salt_msg)
    TextView salt_msg;
    @BindView(R.id.salt)
    EditText salt;
    @BindView(R.id.spinner_salt_unit)
    Spinner saltUnit;
    @BindView(R.id.sodium_msg)
    TextView sodium_msg;
    @BindView(R.id.sodium)
    EditText sodium;
    @BindView(R.id.spinner_sodium_unit)
    Spinner sodiumUnit;
    @BindView(R.id.alcohol)
    EditText alcohol;
    @BindView(R.id.table_layout)
    TableLayout tableLayout;
    @BindView(R.id.btn_add)
    Button buttonAdd;
    //index list stores the index of other nutrients which are used.
    private List<Integer> index = new ArrayList<>();
    private Activity activity;
    private File photoFile;
    private String code;
    private double starchValue = 0.0;
    private int starchUnit;
    private OfflineSavedProduct mOfflineSavedProduct;
    private String imagePath;
    private boolean edit_product;
    private Product product;
    private boolean newImageSelected;
    private EditText lastEditText;

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
        lastEditText = alcohol;
        if (b != null) {
            product = (Product) b.getSerializable("product");
            mOfflineSavedProduct = (OfflineSavedProduct) b.getSerializable("edit_offline_product");
            edit_product = b.getBoolean("edit_product");
            if (product != null) {
                code = product.getCode();
            }
            if (edit_product && product != null) {
                code = product.getCode();
                buttonAdd.setText(R.string.save_edits);
                preFillProductValues();
            } else if (mOfflineSavedProduct != null) {
                code = mOfflineSavedProduct.getBarcode();
                preFillValues();
            }
        } else {
            Toast.makeText(activity, R.string.error_adding_nutrition_facts, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        alcohol.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    /**
     * Pre fill the fields of the product which are already present on the server.
     */
    private void preFillProductValues() {
        if (product.getImageNutritionUrl() != null && !product.getImageNutritionUrl().isEmpty()) {
            imageProgress.setVisibility(View.VISIBLE);
            imagePath = product.getImageNutritionUrl();
            Picasso.with(getContext())
                .load(product.getImageNutritionUrl())
                .resize(dpsToPixels(50), dpsToPixels(50))
                .centerInside()
                .into(imageNutritionFacts, new Callback() {
                    @Override
                    public void onSuccess() {
                        imageProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        imageProgress.setVisibility(View.GONE);
                    }
                });
        }
        if (product.getNoNutritionData() != null && product.getNoNutritionData().equalsIgnoreCase("on")) {
            noNutritionData.setChecked(true);
            nutritionFactsLayout.setVisibility(View.GONE);
        }
        if (product.getNutritionDataPer() != null && !product.getNutritionDataPer().isEmpty()) {
            String s = product.getNutritionDataPer();
            radioGroup.clearCheck();
            if (s.equals("100g")) {
                radioGroup.check(R.id.for100g_100ml);
            } else {
                radioGroup.check(R.id.per_serving);
            }
        }
        if (product.getServingSize() != null && !product.getServingSize().isEmpty()) {
            String servingSize = product.getServingSize();
            // Splits the serving size into value and unit. Example: "15g" into "15" and "g"
            String part[] = servingSize.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            serving_size.setText(part[0]);
            if (part.length > 1) {
                servingSizeUnit.setSelection(getPositionInAllUnitArray(part[1].trim()));
            }
        }
        Nutriments nutriments = product.getNutriments();
        if (nutriments != null) {
            if (nutriments.getValue(Nutriments.ENERGY) != null) {
                energy.setText(nutriments.getModifier(Nutriments.ENERGY));
                energy.append(nutriments.getValue(Nutriments.ENERGY));
                updateEnergyUnitSelector(nutriments.getUnit(Nutriments.ENERGY));
            }
            if (nutriments.getValue(Nutriments.FAT) != null) {
                fat.setText(nutriments.getModifier(Nutriments.FAT));
                fat.append(nutriments.getValue(Nutriments.FAT));
            }
            if (nutriments.getUnit(Nutriments.FAT) != null) {
                fatUnit.setSelection(getPositionInAllUnitArray(nutriments.getUnit(Nutriments.FAT)));
            }
            if (nutriments.getValue(Nutriments.SATURATED_FAT) != null) {
                saturatedFat.setText(nutriments.getModifier(Nutriments.SATURATED_FAT));
                saturatedFat.append(nutriments.getValue(Nutriments.SATURATED_FAT));
            }
            if (nutriments.getUnit(Nutriments.SATURATED_FAT) != null) {
                saturatedFatUnit.setSelection(getPositionInAllUnitArray(nutriments.getUnit(Nutriments.SATURATED_FAT)));
            }
            if (nutriments.getValue(Nutriments.CARBOHYDRATES) != null) {
                carbohydrate.setText(nutriments.getModifier(Nutriments.CARBOHYDRATES));
                carbohydrate.append(nutriments.getValue(Nutriments.CARBOHYDRATES));
            }
            if (nutriments.getUnit(Nutriments.CARBOHYDRATES) != null) {
                carbohydrateUnit.setSelection(getPositionInAllUnitArray(nutriments.getUnit(Nutriments.CARBOHYDRATES)));
            }
            if (nutriments.getValue(Nutriments.SUGARS) != null) {
                sugar.setText(nutriments.getModifier(Nutriments.SUGARS));
                sugar.append(nutriments.getValue(Nutriments.SUGARS));
            }
            if (nutriments.getUnit(Nutriments.SUGARS) != null) {
                sugarUnit.setSelection(getPositionInAllUnitArray(nutriments.getUnit(Nutriments.SUGARS)));
            }
            if (nutriments.getValue(Nutriments.FIBER) != null) {
                dietaryFiber.setText(nutriments.getModifier(Nutriments.FIBER));
                dietaryFiber.append(nutriments.getValue(Nutriments.FIBER));
            }
            if (nutriments.getUnit(Nutriments.FIBER) != null) {
                dietaryFiberUnit.setSelection(getPositionInAllUnitArray(nutriments.getUnit(Nutriments.FIBER)));
            }
            if (nutriments.getValue(Nutriments.PROTEINS) != null) {
                proteins.setText(nutriments.getModifier(Nutriments.PROTEINS));
                proteins.append(nutriments.getValue(Nutriments.PROTEINS));
            }
            if (nutriments.getUnit(Nutriments.PROTEINS) != null) {
                proteinsUnit.setSelection(getPositionInAllUnitArray(nutriments.getUnit(Nutriments.PROTEINS)));
            }
            if (nutriments.getValue(Nutriments.SALT) != null) {
                salt.clearFocus();
                salt.setText(nutriments.getModifier(Nutriments.SALT));
                salt.append(nutriments.getValue(Nutriments.SALT));
            }
            if (nutriments.getUnit(Nutriments.SALT) != null) {
                saltUnit.setSelection(getPositionInAllUnitArray(nutriments.getUnit(Nutriments.SALT)));
            }
            if (nutriments.getValue(Nutriments.SODIUM) != null) {
                sodium.clearFocus();
                sodium.setText(nutriments.getModifier(Nutriments.SODIUM));
                sodium.append(nutriments.getValue(Nutriments.SODIUM));
            }
            if (nutriments.getUnit(Nutriments.SODIUM) != null) {
                sodiumUnit.setSelection(getPositionInAllUnitArray(nutriments.getUnit(Nutriments.SODIUM)));
            }
            if (nutriments.getValue(Nutriments.ALCOHOL) != null) {
                alcohol.setText(nutriments.getModifier(Nutriments.ALCOHOL));
                alcohol.append(nutriments.getValue(Nutriments.ALCOHOL));
            }
            //set the values of all the other nutrients if defined and create new row in the tableLayout.
            for (int i = 0; i < PARAMS_OTHER_NUTRIENTS.length; i++) {
                String PARAMS_OTHER_NUTRIENT = PARAMS_OTHER_NUTRIENTS[i].substring(10);
                if (nutriments.getValue(PARAMS_OTHER_NUTRIENT) != null) {
                    int position = 0;
                    String value;
                    if (nutriments.getModifier(PARAMS_OTHER_NUTRIENT) != null) {
                        value = nutriments.getModifier(PARAMS_OTHER_NUTRIENT) + nutriments.getValue(PARAMS_OTHER_NUTRIENT);
                    } else {
                        value = nutriments.getValue(PARAMS_OTHER_NUTRIENT);
                    }

                    if (nutriments.getUnit(PARAMS_OTHER_NUTRIENT) != null) {
                        position = getPositionInAllUnitArray(nutriments.getUnit(PARAMS_OTHER_NUTRIENT));
                    }
                    index.add(i);
                    String nutrients[] = getResources().getStringArray(R.array.nutrients_array);
                    addNutrientRow(i, nutrients[i], true, value, position);
                }
            }
        }
    }

    /**
     * Pre fill the fields if the product is already present in SavedProductOffline db.
     */
    private void preFillValues() {
        HashMap<String, String> productDetails = mOfflineSavedProduct.getProductDetailsMap();
        if (productDetails != null) {
            if (productDetails.get("image_nutrition_facts") != null) {
                imagePath = productDetails.get("image_nutrition_facts");
                imageProgress.setVisibility(View.VISIBLE);
                Picasso.with(getContext())
                    .load("file://" + imagePath)
                    .resize(dpsToPixels(50), dpsToPixels(50))
                    .centerInside()
                    .into(imageNutritionFacts, new Callback() {
                        @Override
                        public void onSuccess() {
                            imageProgress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            imageProgress.setVisibility(View.GONE);
                        }
                    });
            }
            if (productDetails.get(PARAM_NO_NUTRITION_DATA) != null) {
                noNutritionData.setChecked(true);
                nutritionFactsLayout.setVisibility(View.GONE);
            }
            if (productDetails.get(PARAM_NUTRITION_DATA_PER) != null) {
                String s = productDetails.get(PARAM_NUTRITION_DATA_PER);
                radioGroup.clearCheck();
                if (s.equals("100g")) {
                    radioGroup.check(R.id.for100g_100ml);
                } else {
                    radioGroup.check(R.id.per_serving);
                }
            }
            if (productDetails.get(PARAM_SERVING_SIZE) != null) {
                String servingSize = productDetails.get(PARAM_SERVING_SIZE);
                // Splits the serving size into value and unit. Example: "15g" into "15" and "g"
                String part[] = servingSize.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                serving_size.setText(part[0]);
                if (part.length > 1) {
                    // serving size has the unit part
                    servingSizeUnit.setSelection(getPositionInAllUnitArray(part[1].trim()));
                }
            }
            if (productDetails.get(PARAM_ENERGY) != null) {
                energy.setText(productDetails.get(PARAM_ENERGY));
            }
            if (productDetails.get(PARAM_ENERGY_UNIT) != null) {
                updateEnergyUnitSelector(productDetails.get(PARAM_ENERGY_UNIT));
            }
            if (productDetails.get(PARAM_FAT) != null) {
                fat.setText(productDetails.get(PARAM_FAT));
            }
            if (productDetails.get(PARAM_FAT_UNIT) != null) {
                fatUnit.setSelection(getPositionInAllUnitArray(productDetails.get(PARAM_FAT_UNIT)));
            }
            if (productDetails.get(PARAM_SATURATED_FAT) != null) {
                saturatedFat.setText(productDetails.get(PARAM_SATURATED_FAT));
            }
            if (productDetails.get(PARAM_SATURATED_FAT_UNIT) != null) {
                saturatedFatUnit.setSelection(getPositionInAllUnitArray(productDetails.get(PARAM_SATURATED_FAT_UNIT)));
            }
            if (productDetails.get(PARAM_CARBOHYDRATE) != null) {
                carbohydrate.setText(productDetails.get(PARAM_CARBOHYDRATE));
            }
            if (productDetails.get(PARAM_CARBOHYDRATE_UNIT) != null) {
                carbohydrateUnit.setSelection(getPositionInAllUnitArray(productDetails.get(PARAM_CARBOHYDRATE_UNIT)));
            }
            if (productDetails.get(PARAM_SUGAR) != null) {
                sugar.setText(productDetails.get(PARAM_SUGAR));
            }
            if (productDetails.get(PARAM_SUGAR_UNIT) != null) {
                sugarUnit.setSelection(getPositionInAllUnitArray(productDetails.get(PARAM_SUGAR_UNIT)));
            }
            if (productDetails.get(PARAM_DIETARY_FIBER) != null) {
                dietaryFiber.setText(productDetails.get(PARAM_DIETARY_FIBER));
            }
            if (productDetails.get(PARAM_DIETARY_FIBER) != null) {
                dietaryFiberUnit.setSelection(getPositionInAllUnitArray(productDetails.get(PARAM_DIETARY_FIBER_UNIT)));
            }
            if (productDetails.get(PARAM_PROTEINS) != null) {
                proteins.setText(productDetails.get(PARAM_PROTEINS));
            }
            if (productDetails.get(PARAM_PROTEINS_UNIT) != null) {
                proteinsUnit.setSelection(getPositionInAllUnitArray(productDetails.get(PARAM_PROTEINS_UNIT)));
            }
            if (productDetails.get(PARAM_SALT) != null) {
                salt.clearFocus();
                salt.setText(productDetails.get(PARAM_SALT));
            }
            if (productDetails.get(PARAM_SALT_UNIT) != null) {
                saltUnit.setSelection(getPositionInAllUnitArray(productDetails.get(PARAM_SALT_UNIT)));
            }
            if (productDetails.get(PARAM_SODIUM) != null) {
                sodium.clearFocus();
                sodium.setText(productDetails.get(PARAM_SODIUM));
            }
            if (productDetails.get(PARAM_SODIUM_UNIT) != null) {
                sodiumUnit.setSelection(getPositionInAllUnitArray(productDetails.get(PARAM_SODIUM_UNIT)));
            }
            if (productDetails.get(PARAM_ALCOHOL) != null) {
                alcohol.setText(productDetails.get(PARAM_ALCOHOL));
            }
            //set the values of all the other nutrients if defined and create new row in the tableLayout.
            for (int i = 0; i < PARAMS_OTHER_NUTRIENTS.length; i++) {
                String paramsOtherNutrient = PARAMS_OTHER_NUTRIENTS[i];
                if (productDetails.get(paramsOtherNutrient) != null) {
                    int position = 0;
                    String value = productDetails.get(paramsOtherNutrient);
                    if (productDetails.get(paramsOtherNutrient + "_unit") != null) {
                        position = getPositionInAllUnitArray(productDetails.get(paramsOtherNutrient + "_unit"));
                    }
                    index.add(i);
                    String nutrients[] = getResources().getStringArray(R.array.nutrients_array);
                    addNutrientRow(i, nutrients[i], true, value, position);
                }
            }
        }
    }

    private void updateEnergyUnitSelector(String unit) {
        if (UnitUtils.ENERGY_KCAL.equalsIgnoreCase(unit)) {
            energyUnit.setSelection(0);
        } else {
            energyUnit.setSelection(1);
        }
    }

    private String getSelectedEnergyUnit() {
        String unit = UnitUtils.ENERGY_KJ;
        if (energyUnit.getSelectedItemPosition() == 0) {
            unit = UnitUtils.ENERGY_KCAL;
        }
        return unit;
    }

    /**
     * @param unit The unit corresponding to which the index is to be returned.
     * @return returns the index to be set to the spinner.
     */
    private int getPositionInAllUnitArray(String unit) {
        for (int i = 0; i < AddProductNutritionFactsFragment.ALL_UNIT.length; i++) {
            if (ALL_UNIT[i].equalsIgnoreCase(unit)) {
                return i;
            }
        }
        return 0;
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
        if (imagePath != null) {
            // nutrition facts image is already added. Open full screen image.
            Intent intent = new Intent(getActivity(), FullScreenImage.class);
            Bundle bundle = new Bundle();
            if (edit_product && !newImageSelected) {
                bundle.putString("imageurl", imagePath);
            } else {
                bundle.putString("imageurl", "file://" + imagePath);
            }
            intent.putExtras(bundle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(activity, imageNutritionFacts,
                        activity.getString(R.string.product_transition));
                startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }
        } else {
            // add nutrition facts image.
            if (ContextCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                EasyImage.openCamera(this, 0);
            }
        }
    }

    @OnLongClick(R.id.btnAddImageNutritionFacts)
    boolean newNutritionFactsImage() {
        if (ContextCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            EasyImage.openCamera(this, 0);
        }
        return true;
    }

    @OnClick(R.id.btn_add)
    void next() {
        Activity activity = getActivity();
        if (activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).proceed();
        }
    }

    @OnClick({R.id.for100g_100ml, R.id.per_serving})
    void checkAfterCheckChange(RadioButton radioButton) {
        checkCarbohydratesInBounds();
        checkDietaryFibreInBounds();
        checkEnergyInBounds();
        checkFatInBounds();
        checkProteinsInBounds();
        checkSugarInBounds();
        checkSaturatedFatInBounds();
        checkSodiumInBounds();
        checkSaltInBounds();
    }

    @OnTextChanged(value = R.id.serving_size, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void checkAfterServingSizeChange() {

        if (radioGroup.getCheckedRadioButtonId() != R.id.for100g_100ml) {
            checkCarbohydratesInBounds();
            checkDietaryFibreInBounds();
            checkEnergyInBounds();
            checkFatInBounds();
            checkProteinsInBounds();
            checkSugarInBounds();
            checkSaturatedFatInBounds();
            checkSodiumInBounds();
            checkSaltInBounds();
        }
    }

    @OnTextChanged(value = R.id.proteins, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void checkProteinsInBounds() {
        updateGramEntryState(proteins, proteinsUnit, proteins_msg);
    }

    private List<TextView> getErrorMessageView() {
        return Arrays.asList(
            energy_msg,
            fat_msg,
            saturated_fat_msg,
            carbohydrate_msg,
            sugar_msg,
            dietary_fibre_msg,
            proteins_msg,
            salt_msg,
            sodium_msg
        );
    }

    private boolean isAllEntriesValid() {
        for (TextView v : getErrorMessageView()) {
            if (v.getVisibility() == View.VISIBLE) {
                return false;
            }
        }
        return true;
    }

    private void updateButtonState() {
        buttonAdd.setEnabled(isAllEntriesValid());
    }

    private void updateGramEntryState(EditText text, Spinner gramOrMicroGramSelector, TextView errorText) {
        boolean isValid = isGramValueValid(text, gramOrMicroGramSelector);
        errorText.setVisibility(isValid ? View.GONE : View.VISIBLE);
        updateButtonState();
    }

    private boolean isGramValueValid(EditText text, Spinner gramOrMicroGrammSelector) {
        //if no value, we suppose it's valid
        if (QuantityParserUtil.isBlank(text)) {
            return true;
        }
        Float value = QuantityParserUtil.getFloatValue(text, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
        if (value == null) {
            return false;
        }
        return isValueValid(convertToGrams(value, gramOrMicroGrammSelector.getSelectedItemPosition()));
    }

    @OnTextChanged(value = R.id.dietary_fibre, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void checkDietaryFibreInBounds() {
        updateGramEntryState(dietaryFiber, dietaryFiberUnit, dietary_fibre_msg);
    }

    @OnTextChanged(value = R.id.sugar, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void checkSugarInBounds() {
        updateGramEntryState(sugar, sugarUnit, sugar_msg);
    }

    @OnTextChanged(value = R.id.carbohydrate, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void checkCarbohydratesInBounds() {
        updateGramEntryState(carbohydrate, carbohydrateUnit, carbohydrate_msg);
    }

    @OnTextChanged(value = R.id.saturated_fat, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void checkSaturatedFatInBounds() {
        updateGramEntryState(saturatedFat, saturatedFatUnit, saturated_fat_msg);
    }

    @OnTextChanged(value = R.id.fat, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void checkFatInBounds() {
        updateGramEntryState(fat, fatUnit, fat_msg);
    }

    @OnTextChanged(value = R.id.energy, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void checkEnergyInBounds() {
        boolean isValid = true;
        if (!QuantityParserUtil.isBlank(energy)) {
            Float value = QuantityParserUtil.getFloatValue(energy, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
            if (value == null) {
                isValid = false;
            } else {
                float energyInKcal = UnitUtils.convertToKiloCalories(value, getSelectedEnergyUnit());
                if (radioGroup.getCheckedRadioButtonId() != R.id.for100g_100ml) {
                    energyInKcal *= (100.0f / getReferenceValue());
                }
                isValid = (energyInKcal <= 2000.0f);
            }
        }
        energy_msg.setVisibility(isValid ? View.GONE : View.VISIBLE);
        updateButtonState();
    }

    @OnTextChanged(value = R.id.salt, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void autoCalculateSodiumValue() {
        if (activity.getCurrentFocus() == salt) {

            checkSaltInBounds();

            sodium.clearFocus();
            sodiumUnit.setSelection(saltUnit.getSelectedItemPosition());
            Double saltValue = QuantityParserUtil.getDoubleValue(salt, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
            if (saltValue != null) {
                String saltModifier = QuantityParserUtil.getModifier(salt);
                double sodiumValue = saltToSodium(saltValue);
                sodium.setText(StringUtils.defaultString(saltModifier) + sodiumValue);
            } else if (!QuantityParserUtil.isBlank(salt)) {
                //we clear the value
                //TODO to improve to keep the value
                salt.setText(null);
                sodium.setText(null);
            }
        }
    }

    private double saltToSodium(Double saltValue) {
        return saltValue * 0.39370078740157477;
    }

    @OnTextChanged(value = R.id.sodium, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void autoCalculateSaltValue() {
        if (activity.getCurrentFocus() == sodium) {

            checkSodiumInBounds();
            salt.clearFocus();
            Double sodiumValue = QuantityParserUtil.getDoubleValue(sodium, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
            if (sodiumValue != null) {
                String sodiumModifier = QuantityParserUtil.getModifier(sodium);
                double saltValue = sodiumToSalt(sodiumValue);
                salt.setText(StringUtils.defaultString(sodiumModifier) + saltValue);
            } else if (!QuantityParserUtil.isBlank(sodium)) {
                //we clear the value because sodium value is not correct
                //TODO to improve to keep the value
                salt.setText(null);
                sodium.setText(null);
            }
            saltUnit.setSelection(sodiumUnit.getSelectedItemPosition());
        }
    }

    private double sodiumToSalt(Double sodiumValue) {
        return sodiumValue * 2.54;
    }

    private void checkSaltInBounds() {
        updateGramEntryState(salt, saltUnit, salt_msg);
    }

    private void checkSodiumInBounds() {
        updateGramEntryState(sodium, sodiumUnit, sodium_msg);
    }

    @OnItemSelected(value = R.id.spinner_salt_unit, callback = OnItemSelected.Callback.ITEM_SELECTED)
    void autoSelectSodiumSpinner() {
        sodiumUnit.setSelection(saltUnit.getSelectedItemPosition());
    }

    @OnItemSelected(value = R.id.spinner_sodium_unit, callback = OnItemSelected.Callback.ITEM_SELECTED)
    void autoSelectSaltSpinner() {
        saltUnit.setSelection(sodiumUnit.getSelectedItemPosition());
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
        Double alcoholValue = QuantityParserUtil.getDoubleValue(alcohol, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
        if (alcoholValue != null && alcoholValue > 100) {
            alcohol.setText("100.0");
        }
    }

    public boolean nutritionCheckFailed() {
        if (QuantityParserUtil.isBlank(carbohydrate)) {
            return false;
        }
        Double carbsValue = QuantityParserUtil.getDoubleValue(carbohydrate, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
        Double sugarValue = QuantityParserUtil.getDoubleValue(sugar, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
        boolean failed = false;
        if (carbsValue == null) {
            carbohydrate.setError(getString(R.string.error_in_carbohydrate_value));
            failed = true;
        }
        if (QuantityParserUtil.isNotBlank(sugar) && sugarValue == null) {
            //TODO add a error text for sugar containing an incorrect value
            failed = true;
        }
        if (failed) {
            carbohydrate.requestFocus();
            return true;
        }
        // check that value of (sugar + starch) is not greater than value of carbohydrates
        //convert all the values to grams
        carbsValue = convertToGrams(carbsValue, carbohydrateUnit.getSelectedItemPosition());
        sugarValue = convertToGrams(sugarValue, sugarUnit.getSelectedItemPosition());
        double newStarch = convertToGrams(starchValue, starchUnit);
        if ((sugarValue + newStarch) > carbsValue) {
            carbohydrate.requestFocus();
            carbohydrate.setError(getString(R.string.error_in_carbohydrate_value));
            return true;
        } else {
            return false;
        }
    }

    /**
     * adds all the fields to the query map even those which are null or empty.
     */
    public void getAllDetails() {
        if (activity instanceof AddProductActivity) {
            if (noNutritionData.isChecked()) {
                ((AddProductActivity) activity).addToMap(PARAM_NO_NUTRITION_DATA, "on");
            } else {
                if (radioGroup.getCheckedRadioButtonId() == R.id.for100g_100ml) {
                    ((AddProductActivity) activity).addToMap(PARAM_NUTRITION_DATA_PER, "100g");
                } else if (radioGroup.getCheckedRadioButtonId() == R.id.per_serving) {
                    ((AddProductActivity) activity).addToMap(PARAM_NUTRITION_DATA_PER, "serving");
                }
                if (serving_size.getText().toString().isEmpty()) {
                    ((AddProductActivity) activity).addToMap(PARAM_SERVING_SIZE, "");
                } else {
                    String servingSize = serving_size.getText().toString() + UNIT[servingSizeUnit.getSelectedItemPosition()];
                    ((AddProductActivity) activity).addToMap(PARAM_SERVING_SIZE, servingSize);
                }

                String s = energy.getText().toString();
                String unit = getSelectedEnergyUnit();
                ((AddProductActivity) activity).addToMap(PARAM_ENERGY, s);
                ((AddProductActivity) activity).addToMap(PARAM_ENERGY_UNIT, unit);

                s = fat.getText().toString();
                unit = UNIT[fatUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_FAT, s);
                ((AddProductActivity) activity).addToMap(PARAM_FAT_UNIT, unit);

                s = saturatedFat.getText().toString();
                unit = UNIT[saturatedFatUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_SATURATED_FAT, s);
                ((AddProductActivity) activity).addToMap(PARAM_SATURATED_FAT_UNIT, unit);

                s = carbohydrate.getText().toString();
                unit = UNIT[carbohydrateUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_CARBOHYDRATE, s);
                ((AddProductActivity) activity).addToMap(PARAM_CARBOHYDRATE_UNIT, unit);

                s = sugar.getText().toString();
                unit = UNIT[sugarUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_SUGAR, s);
                ((AddProductActivity) activity).addToMap(PARAM_SUGAR_UNIT, unit);

                s = dietaryFiber.getText().toString();
                unit = UNIT[dietaryFiberUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_DIETARY_FIBER, s);
                ((AddProductActivity) activity).addToMap(PARAM_DIETARY_FIBER_UNIT, unit);

                s = proteins.getText().toString();
                unit = UNIT[proteinsUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_PROTEINS, s);
                ((AddProductActivity) activity).addToMap(PARAM_PROTEINS_UNIT, unit);

                s = salt.getText().toString();
                unit = UNIT[saltUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_SALT, s);
                ((AddProductActivity) activity).addToMap(PARAM_SALT_UNIT, unit);

                s = sodium.getText().toString();
                unit = UNIT[sodiumUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_SODIUM, s);
                ((AddProductActivity) activity).addToMap(PARAM_SODIUM_UNIT, unit);

                s = alcohol.getText().toString();
                ((AddProductActivity) activity).addToMap(PARAM_ALCOHOL, s);

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
                                ((AddProductActivity) activity).addToMap(PARAMS_OTHER_NUTRIENTS[id], editText.getText().toString());
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

    /**
     * adds only those fields to the query map which are not empty.
     */
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
                    ((AddProductActivity) activity).addToMap(PARAM_ENERGY, energy.getText().toString());
                    ((AddProductActivity) activity).addToMap(PARAM_ENERGY_UNIT, getSelectedEnergyUnit());
                }
                if (!fat.getText().toString().isEmpty()) {
                    ((AddProductActivity) activity).addToMap(PARAM_FAT, fat.getText().toString());
                    ((AddProductActivity) activity).addToMap(PARAM_FAT_UNIT, UNIT[fatUnit.getSelectedItemPosition()]);
                }
                if (!saturatedFat.getText().toString().isEmpty()) {
                    ((AddProductActivity) activity).addToMap(PARAM_SATURATED_FAT, saturatedFat.getText().toString());
                    ((AddProductActivity) activity).addToMap(PARAM_SATURATED_FAT_UNIT, UNIT[saturatedFatUnit.getSelectedItemPosition()]);
                }
                if (!carbohydrate.getText().toString().isEmpty()) {
                    ((AddProductActivity) activity).addToMap(PARAM_CARBOHYDRATE, carbohydrate.getText().toString());
                    ((AddProductActivity) activity).addToMap(PARAM_CARBOHYDRATE_UNIT, UNIT[carbohydrateUnit.getSelectedItemPosition()]);
                }
                if (!sugar.getText().toString().isEmpty()) {
                    ((AddProductActivity) activity).addToMap(PARAM_SUGAR, sugar.getText().toString());
                    ((AddProductActivity) activity).addToMap(PARAM_SUGAR_UNIT, UNIT[sugarUnit.getSelectedItemPosition()]);
                }
                if (!dietaryFiber.getText().toString().isEmpty()) {
                    ((AddProductActivity) activity).addToMap(PARAM_DIETARY_FIBER, dietaryFiber.getText().toString());
                    ((AddProductActivity) activity).addToMap(PARAM_DIETARY_FIBER_UNIT, UNIT[dietaryFiberUnit.getSelectedItemPosition()]);
                }
                if (!proteins.getText().toString().isEmpty()) {
                    ((AddProductActivity) activity).addToMap(PARAM_PROTEINS, proteins.getText().toString());
                    ((AddProductActivity) activity).addToMap(PARAM_PROTEINS_UNIT, UNIT[proteinsUnit.getSelectedItemPosition()]);
                }
                if (!salt.getText().toString().isEmpty()) {
                    ((AddProductActivity) activity).addToMap(PARAM_SALT, salt.getText().toString());
                    ((AddProductActivity) activity).addToMap(PARAM_SALT_UNIT, UNIT[saltUnit.getSelectedItemPosition()]);
                }
                if (!sodium.getText().toString().isEmpty()) {
                    ((AddProductActivity) activity).addToMap(PARAM_SODIUM, sodium.getText().toString());
                    ((AddProductActivity) activity).addToMap(PARAM_SODIUM_UNIT, UNIT[sodiumUnit.getSelectedItemPosition()]);
                }
                if (!alcohol.getText().toString().isEmpty()) {
                    ((AddProductActivity) activity).addToMap(PARAM_ALCOHOL, alcohol.getText().toString());
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
            .title(R.string.choose_nutrient)
            .items(R.array.nutrients_array)
            .itemsCallback((dialog, itemView, position, text) -> {
                if (!index.contains(position)) {
                    index.add(position);
                    addNutrientRow(position, text, false, null, 0);
                } else {
                    String nutrients[] = getResources().getStringArray(R.array.nutrients_array);
                    Toast.makeText(activity, getString(R.string.nutrient_already_added, nutrients[position]), Toast.LENGTH_SHORT).show();
                }
            })
            .show();
    }

    private boolean isValueValid(float a) {

        float reference = getReferenceValue();
        return (a <= reference);
    }

    private float getReferenceValue() {
        float reference = 100;
        if (radioGroup.getCheckedRadioButtonId() != R.id.for100g_100ml && QuantityParserUtil.containFloatValue(serving_size, QuantityParserUtil.EntryFormat.NO_PREFIX)) {
            reference = QuantityParserUtil.getFloatValue(serving_size, QuantityParserUtil.EntryFormat.NO_PREFIX);
            reference = convertToGrams(reference, servingSizeUnit.getSelectedItemPosition());
        }
        return reference;
    }

    /**
     * Converts a given quantity's unit to grams.
     *
     * @param a The value to be converted
     * @param index 1 represents milligrams, 2 represents micrograms
     * @return return the converted value
     */
    private float convertToGrams(float a, int index) {
        return UnitUtils.convertToGrams(a, UNIT[index]);
    }

    private double convertToGrams(double a, int index) {
        return UnitUtils.convertToGrams(a, UNIT[index]);
    }

    /**
     * Adds a new row in the tableLayout.
     *
     * @param position The index of the additional nutrient to add in the "PARAM_OTHER_NUTRIENTS" array.
     * @param text The hint text to be displayed in the EditText.
     * @param preFillValues true if the created row needs to be filled by a predefined value.
     * @param value This value will be set to the EditText. Required if 'preFillValues' is true.
     * @param unit_position This spinner will be set to this position. Required if 'preFillValues' is true.
     */
    private void addNutrientRow(int position, CharSequence text, boolean preFillValues, String value, int unit_position) {
        TableRow nutrient = new TableRow(activity);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, dpsToPixels(40));
        lp.topMargin = dpsToPixels(10);
        EditText editText = new EditText(activity);
        editText.setBackgroundResource(R.drawable.bg_edittext);
        editText.setHint(text);
        editText.setId(position);
        editText.setKeyListener(keyListener);
        lastEditText.setNextFocusDownId(editText.getId());
        lastEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        lastEditText = editText;
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setSingleLine();
        editText.setPadding(dpsToPixels(10), 0, dpsToPixels(10), 0);
        editText.setGravity(Gravity.CENTER_VERTICAL);
        editText.requestFocus();
        editText.setLayoutParams(lp);
        if (preFillValues) {
            editText.setText(value);
        }
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
        if (preFillValues) {
            spinner.setSelection(unit_position);
        }
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
                    if (QuantityParserUtil.isNotBlank(editText)) {
                        Double pHValue = QuantityParserUtil.getDoubleValue(editText, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);

                        if (pHValue == null) {
                            editText.setText(null);
                        } else {
                            double maxPhValue=14;
                            if (pHValue > maxPhValue || (pHValue >= maxPhValue && QuantityParserUtil.isModifierEqualsToGreaterThan(editText))) {
                                editText.setText(Double.toString(maxPhValue));
                            }
                        }
                    }
                }
            });
        } else if (PARAMS_OTHER_NUTRIENTS[position].equals("nutriment_starch")) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>
                (activity, android.R.layout.simple_spinner_item, activity.getResources().getStringArray(R.array.weights_array));
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);
            if (preFillValues) {
                spinner.setSelection(unit_position);
            }

            // save value of starch for checking later.
            registerStarchValueFromEditText(editText);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    registerStarchValueFromEditText(editText);
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

    private void registerStarchValueFromEditText(EditText editText) {
        Double newStarchValue = QuantityParserUtil.getDoubleValue(editText, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
        if (newStarchValue != null) {
            starchValue = newStarchValue;
        } else {
            editText.setText(null);
        }
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
                imagePath = resultUri.getPath();
                newImageSelected = true;
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
                CropImage.activity(Uri.fromFile(imageFiles.get(0)))
                    .setAllowFlipping(false)
                    .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
                    .setOutputUri(Utils.getOutputPicUri(getContext()))
                    .start(activity.getApplicationContext(), AddProductNutritionFactsFragment.this);
            }
        });
    }

    public void showImageProgress() {
        imageProgress.setVisibility(View.VISIBLE);
        imageProgressText.setVisibility(View.VISIBLE);
        imageNutritionFacts.setVisibility(View.INVISIBLE);
    }

    public void hideImageProgress(boolean errorInUploading, String message) {
        imageProgress.setVisibility(View.GONE);
        imageProgressText.setVisibility(View.GONE);
        imageNutritionFacts.setVisibility(View.VISIBLE);
        if (!errorInUploading) {
            Picasso.with(activity)
                .load(photoFile)
                .resize(dpsToPixels(50), dpsToPixels(50))
                .centerInside()
                .into(imageNutritionFacts);
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        }
    }
}
