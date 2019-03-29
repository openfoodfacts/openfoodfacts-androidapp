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
import android.support.design.widget.TextInputLayout;
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
import openfoodfacts.github.scrachx.openfood.utils.*;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import org.apache.commons.lang3.StringUtils;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import java.io.File;
import java.util.*;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.NUTRITION;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;

public class AddProductNutritionFactsFragment extends BaseFragment {
    private static final String[] ALL_UNIT = {UnitUtils.UNIT_GRAM, UnitUtils.UNIT_MILLIGRAM, UnitUtils.UNIT_MICROGRAM, UnitUtils.UNIT_DV, UnitUtils.UNIT_IU};
    private static final String[] UNIT = {UnitUtils.UNIT_GRAM, UnitUtils.UNIT_MILLIGRAM, UnitUtils.UNIT_MICROGRAM};
    private static final String PARAM_NO_NUTRITION_DATA = "no_nutrition_data";
    private static final String PARAM_NUTRITION_DATA_PER = "nutrition_data_per";
    private static final String PARAM_SERVING_SIZE = "serving_size";
    private final NumberKeyListener keyListener = new NumberKeyListener() {
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
    CustomValidatingEditTextView serving_size;
    @BindView(R.id.energy)
    CustomValidatingEditTextView energy;
    @BindView(R.id.fat)
    CustomValidatingEditTextView fat;
    @BindView(R.id.saturated_fat)
    CustomValidatingEditTextView saturatedFat;
    @BindView(R.id.carbohydrates)
    CustomValidatingEditTextView carbohydrate;
    @BindView(R.id.sugars)
    CustomValidatingEditTextView sugar;
    @BindView(R.id.fiber)
    CustomValidatingEditTextView dietaryFiber;
    @BindView(R.id.proteins)
    CustomValidatingEditTextView proteins;
    @BindView(R.id.salt)
    CustomValidatingEditTextView salt;
    @BindView(R.id.sodium)
    CustomValidatingEditTextView sodium;
    @BindView(R.id.alcohol)
    CustomValidatingEditTextView alcohol;
    @BindView(R.id.table_layout)
    TableLayout tableLayout;
    @BindView(R.id.global_validation_msg)
    TextView globalValidationMsg;
    @BindView(R.id.btn_add)
    Button buttonAdd;
    //index list stores the index of other nutrients which are used.
    private Set<Integer> index = new HashSet<>();
    private Activity activity;
    private File photoFile;
    private String code;
    private OfflineSavedProduct mOfflineSavedProduct;
    private String imagePath;
    private boolean edit_product;
    private Product product;
    private boolean newImageSelected;
    private EditText lastEditText;
    private CustomValidatingEditTextView starchEditText;
    private Set<CustomValidatingEditTextView> allEditViews = Collections.emptySet();

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
                preFillValuesFromOffline();
            }
        } else {
            Toast.makeText(activity, R.string.error_adding_nutrition_facts, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        alcohol.setImeOptions(EditorInfo.IME_ACTION_DONE);
        energy.requestFocus();
        allEditViews = new HashSet<>(Utils.getViewsByType((ViewGroup) view, CustomValidatingEditTextView.class));
        for (CustomValidatingEditTextView editText : allEditViews) {
            addValidListener(editText);
            checkValue(editText);
        }
    }

    private void checkAllValues() {
        final Collection<CustomValidatingEditTextView> allEditText = getAllEditTextView();
        for (CustomValidatingEditTextView editText : allEditText) {
            checkValue(editText);
        }
    }

    private Collection<CustomValidatingEditTextView> getAllEditTextView() {
        return allEditViews;
    }

    private boolean isAllValuesValid() {
        final Collection<CustomValidatingEditTextView> allEditText = getAllEditTextView();
        for (CustomValidatingEditTextView editText : allEditText) {
            if (editText.hasError()) {
                return false;
            }
        }
        return true;
    }

    public boolean containsInvalidValue() {
        return !isAllValuesValid();
    }

    /**
     * Pre fill the fields of the product which are already present on the server.
     */
    private void preFillProductValues() {
        if (product.getImageNutritionUrl() != null && !product.getImageNutritionUrl().isEmpty()) {
            imageProgress.setVisibility(View.VISIBLE);
            imagePath = product.getImageNutritionUrl();
            loadNutritionsImage(imagePath);
        }
        if (product.getNoNutritionData() != null && product.getNoNutritionData().equalsIgnoreCase("on")) {
            noNutritionData.setChecked(true);
            nutritionFactsLayout.setVisibility(View.GONE);
        }
        if (product.getNutritionDataPer() != null && !product.getNutritionDataPer().isEmpty()) {
            updateSelectedDataSize(product.getNutritionDataPer());
        }
        if (product.getServingSize() != null && !product.getServingSize().isEmpty()) {
            String servingSize = product.getServingSize();
            // Splits the serving size into value and unit. Example: "15g" into "15" and "g"
            updateServingSizeFrom(servingSize);
        }
        Nutriments nutriments = product.getNutriments();
        if (nutriments != null) {
            final ArrayList<CustomValidatingEditTextView> editViews = Utils.getViewsByType((ViewGroup) getView(), CustomValidatingEditTextView.class);
            for (CustomValidatingEditTextView view : editViews) {
                final String nutrientShortName = view.getEntryName();
                if (nutrientShortName.equals(serving_size.getEntryName())) {
                    continue;
                }
                String value = getValueFromShortName(nutriments, nutrientShortName);
                if (value != null) {
                    view.setText(value);
                    if (view.getAttachedSpinner() != null) {
                        view.getAttachedSpinner().setSelection(getSelectedUnitFromShortName(nutriments, nutrientShortName));
                    }
                }
            }
            //set the values of all the other nutrients if defined and create new row in the tableLayout.
            for (int i = 0; i < AddProductNutritionFactsData.PARAMS_OTHER_NUTRIENTS.size(); i++) {
                String nutrientShortName = AddProductNutritionFactsData.getShortName(AddProductNutritionFactsData.PARAMS_OTHER_NUTRIENTS.get(i));
                if (nutriments.getValue(nutrientShortName) != null) {

                    String value = getValueFromShortName(nutriments, nutrientShortName);
                    int unitSelectedIndex = getSelectedUnitFromShortName(nutriments, nutrientShortName);
                    index.add(i);
                    String[] nutrients = getResources().getStringArray(R.array.nutrients_array);
                    addNutrientRow(i, nutrients[i], true, value, unitSelectedIndex);
                }
            }
        }
    }

    private int getSelectedUnitFromShortName(Nutriments nutriments, String nutrientShortName) {
        final String unit = nutriments.getUnit(nutrientShortName);
        return getSelectedUnit(nutrientShortName, unit);
    }

    private int getSelectedUnit(String nutrientShortName, String unit) {
        int unitSelectedIndex = 0;
        if (unit != null) {
            if (Nutriments.ENERGY.equals(nutrientShortName)) {
                unitSelectedIndex = getSelectedEnergyUnitIndex(unit);
            } else {
                unitSelectedIndex = getPositionInAllUnitArray(unit);
            }
        }
        return unitSelectedIndex;
    }

    private String getValueFromShortName(Nutriments nutriments, String nutrientShortName) {
        String value;
        if (nutriments.getModifier(nutrientShortName) != null) {
            value = nutriments.getModifier(nutrientShortName) + nutriments.getValue(nutrientShortName);
        } else {
            value = nutriments.getValue(nutrientShortName);
        }
        return value;
    }

    private void updateServingSizeFrom(String servingSize) {
        String[] part = servingSize.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        serving_size.setText(part[0]);
        if (part.length > 1) {
            serving_size.getAttachedSpinner().setSelection(getPositionInAllUnitArray(part[1].trim()));
        }
    }

    /**
     * Pre fill the fields if the product is already present in SavedProductOffline db.
     */
    private void preFillValuesFromOffline() {
        HashMap<String, String> productDetails = mOfflineSavedProduct.getProductDetailsMap();
        if (productDetails != null) {
            if (productDetails.get("image_nutrition_facts") != null) {
                imagePath = productDetails.get("image_nutrition_facts");
                final String path = "file://" + imagePath;
                imageProgress.setVisibility(View.VISIBLE);
                loadNutritionsImage(path);
            }
            if (productDetails.get(PARAM_NO_NUTRITION_DATA) != null) {
                noNutritionData.setChecked(true);
                nutritionFactsLayout.setVisibility(View.GONE);
            }
            if (productDetails.get(PARAM_NUTRITION_DATA_PER) != null) {
                String s = productDetails.get(PARAM_NUTRITION_DATA_PER);
                updateSelectedDataSize(s);
            }
            if (productDetails.get(PARAM_SERVING_SIZE) != null) {
                String servingSize = productDetails.get(PARAM_SERVING_SIZE);
                // Splits the serving size into value and unit. Example: "15g" into "15" and "g"
                updateServingSizeFrom(servingSize);
            }
            final ArrayList<CustomValidatingEditTextView> editViews = Utils.getViewsByType((ViewGroup) getView(), CustomValidatingEditTextView.class);
            for (CustomValidatingEditTextView view : editViews) {
                final String nutrientShortName = view.getEntryName();
                if (nutrientShortName.equals(serving_size.getEntryName())) {
                    continue;
                }
                final String nutrientCompleteName = AddProductNutritionFactsData.getCompleteEntryName(view);
                String value = productDetails.get(nutrientCompleteName);
                if (value != null) {
                    view.setText(value);
                    if (view.getAttachedSpinner() != null) {
                        view.getAttachedSpinner()
                            .setSelection(getSelectedUnit(nutrientShortName, productDetails.get(nutrientCompleteName + AddProductNutritionFactsData.SUFFIX_UNIT)));
                    }
                }
            }
            //set the values of all the other nutrients if defined and create new row in the tableLayout.
            for (int i = 0; i < AddProductNutritionFactsData.PARAMS_OTHER_NUTRIENTS.size(); i++) {
                String completeNutrientName = AddProductNutritionFactsData.PARAMS_OTHER_NUTRIENTS.get(i);
                if (productDetails.get(completeNutrientName) != null) {
                    int position = 0;
                    String value = productDetails.get(completeNutrientName);
                    if (productDetails.get(completeNutrientName + AddProductNutritionFactsData.SUFFIX_UNIT) != null) {
                        position = getPositionInAllUnitArray(productDetails.get(completeNutrientName + AddProductNutritionFactsData.SUFFIX_UNIT));
                    }
                    index.add(i);
                    String[] nutrients = getResources().getStringArray(R.array.nutrients_array);
                    addNutrientRow(i, nutrients[i], true, value, position);
                }
            }
        }
    }

    private void updateSelectedDataSize(String s) {
        radioGroup.clearCheck();
        if (s.equals(Nutriments.DEFAULT_NUTRITION_SIZE)) {
            radioGroup.check(R.id.for100g_100ml);
        } else {
            radioGroup.check(R.id.per_serving);
        }
    }

    private void loadNutritionsImage(String path) {
        Picasso.with(getContext())
            .load(path)
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

    private int getSelectedEnergyUnitIndex(String unit) {
        if (UnitUtils.ENERGY_KJ.equalsIgnoreCase(unit)) {
            return 1;
        }
        return 0;
    }

    private String getSelectedUnit(String nutrientShortName, int selectedIdx) {
        if (Nutriments.ENERGY.equals(nutrientShortName)) {
            String unit = UnitUtils.ENERGY_KJ;
            if (selectedIdx == 0) {
                unit = UnitUtils.ENERGY_KCAL;
            }
            return unit;
        }
        return ALL_UNIT[selectedIdx];
    }

    private String getSelectedEnergyUnit() {
        return getSelectedUnit(Nutriments.ENERGY, energy.getAttachedSpinner().getSelectedItemPosition());
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
            openCamera();
        }
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            EasyImage.openCamera(this, 0);
        }
    }

    @OnLongClick(R.id.btnAddImageNutritionFacts)
    boolean newNutritionFactsImage() {
        openCamera();
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
        checkAllValues();
    }

    private void updateButtonState() {
        final boolean allValuesValid = isAllValuesValid();
        globalValidationMsg.setVisibility(allValuesValid ? View.GONE : View.VISIBLE);
        buttonAdd.setEnabled(allValuesValid);
    }

    private ValueState checkValue(CustomValidatingEditTextView text, float value) {
        ValueState res = checkPh(text, value);
        if (res != ValueState.NOT_TESTED) {
            return res;
        }
        res = checkAlcohol(text, value);
        if (res != ValueState.NOT_TESTED) {
            return res;
        }
        res = checkEnergy(text, value);
        if (res != ValueState.NOT_TESTED) {
            return res;
        }
        res = checkCarbohydrate(text, value);
        if (res != ValueState.NOT_TESTED) {
            return res;
        }
        res = checkPerServing(text);
        if (res != ValueState.NOT_TESTED) {
            return res;
        }
        return checkAsGram(text, value);
    }

    private ValueState checkAsGram(CustomValidatingEditTextView text, float value) {
        float reference = getReferenceValueInGram();
        boolean valid = convertToGrams(value, text.getAttachedSpinner().getSelectedItemPosition()) <= reference;
        if (!valid) {
            text.showError(getString(R.string.max_nutrient_val_msg));
        }
        return valid ? ValueState.VALID : ValueState.NOT_VALID;
    }

    private void checkValue(CustomValidatingEditTextView text) {
        boolean wasValid = text.hasError();
        //if no value, we suppose it's valid
        if (QuantityParserUtil.isBlank(text)) {
            text.cancelError();
            //if per serving is set must be not blank
            checkPerServing(text);
        } else {
            Float value = QuantityParserUtil.getFloatValue(text, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
            if (value == null) {
                text.showError(getString(R.string.error_nutrient_entry));
            } else {
                final ValueState valueState = checkValue(text, value);
                if (valueState.equals(ValueState.VALID)) {
                    text.cancelError();
                }
            }
        }
        if (wasValid != text.isValid()) {
            updateButtonState();
        }
    }

    private void checkValueAndRelated(CustomValidatingEditTextView text) {
        checkValue(text);
        if (isCarbohydrateRelated(text)) {
            checkValue(carbohydrate);
        }
        if (serving_size.getEntryName().equals(text.getEntryName())) {
            checkAllValues();
        }
    }

    private void addValidListener(CustomValidatingEditTextView target) {
        ValidTextWatcher textWatcher = new ValidTextWatcher(target);
        target.addTextChangedListener(textWatcher);
        if (target.getAttachedSpinner() != null) {
            target.getAttachedSpinner().setOnItemSelectedListener(textWatcher);
        }
    }

    private class ValidTextWatcher implements TextWatcher, AdapterView.OnItemSelectedListener {
        private final CustomValidatingEditTextView editTextView;

        private ValidTextWatcher(CustomValidatingEditTextView editTextView) {
            this.editTextView = editTextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//nothing to do
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//nothing to do
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkValueAndRelated(editTextView);
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            checkValueAndRelated(editTextView);
            if (salt.getEntryName().equals(editTextView.getEntryName())) {
                sodium.getAttachedSpinner().setSelection(salt.getAttachedSpinner().getSelectedItemPosition());
            }
            if (sodium.getEntryName().equals(editTextView.getEntryName())) {
                salt.getAttachedSpinner().setSelection(sodium.getAttachedSpinner().getSelectedItemPosition());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            checkValueAndRelated(editTextView);
        }
    }

    @OnTextChanged(value = R.id.salt, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void autoCalculateSodiumValue() {
        if (activity.getCurrentFocus() == salt) {

            Double saltValue = QuantityParserUtil.getDoubleValue(salt, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
            if (saltValue != null) {
                String saltModifier = QuantityParserUtil.getModifier(salt);
                double sodiumValue = UnitUtils.saltToSodium(saltValue);
                sodium.setText(StringUtils.defaultString(saltModifier) + sodiumValue);
            }
        }
    }

    @OnTextChanged(value = R.id.sodium, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void autoCalculateSaltValue() {
        if (activity.getCurrentFocus() == sodium) {
            Double sodiumValue = QuantityParserUtil.getDoubleValue(sodium, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
            if (sodiumValue != null) {
                String sodiumModifier = QuantityParserUtil.getModifier(sodium);
                double saltValue = UnitUtils.sodiumToSalt(sodiumValue);
                salt.setText(StringUtils.defaultString(sodiumModifier) + saltValue);
            }
        }
    }

    @OnCheckedChanged(R.id.checkbox_no_nutrition_data)
    void onCheckedChanged(boolean isChecked) {
        if (isChecked) {
            nutritionFactsLayout.setVisibility(View.GONE);
        } else {
            nutritionFactsLayout.setVisibility(View.VISIBLE);
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
                if (isDataPer100()) {
                    ((AddProductActivity) activity).addToMap(PARAM_NUTRITION_DATA_PER, Nutriments.DEFAULT_NUTRITION_SIZE);
                } else if (isDataPerServing()) {
                    ((AddProductActivity) activity).addToMap(PARAM_NUTRITION_DATA_PER, "serving");
                }
                if (serving_size.getText().toString().isEmpty()) {
                    ((AddProductActivity) activity).addToMap(PARAM_SERVING_SIZE, "");
                } else {
                    String servingSize = serving_size.getText().toString() + UNIT[serving_size.getAttachedSpinner().getSelectedItemPosition()];
                    ((AddProductActivity) activity).addToMap(PARAM_SERVING_SIZE, servingSize);
                }
                for (CustomValidatingEditTextView editTextView : getAllEditTextView()) {
                    if (serving_size.getEntryName().equals(editTextView.getEntryName())) {
                        continue;
                    }
                    addNutrientToMap(editTextView);
                }
            }
        }
    }

    private boolean isDataPerServing() {
        return radioGroup.getCheckedRadioButtonId() == R.id.per_serving;
    }

    private boolean hasUnit(CustomValidatingEditTextView editTextView) {
        String shortName = editTextView.getEntryName();
        return !Nutriments.PH.equals(shortName) && !Nutriments.ALCOHOL.equals(shortName);
    }

    /**
     * adds only those fields to the query map which are not empty.
     */
    public void getDetails() {
        if (activity instanceof AddProductActivity) {
            if (noNutritionData.isChecked()) {
                ((AddProductActivity) activity).addToMap(PARAM_NO_NUTRITION_DATA, "on");
            } else {
                if (isDataPer100()) {
                    ((AddProductActivity) activity).addToMap(PARAM_NUTRITION_DATA_PER, Nutriments.DEFAULT_NUTRITION_SIZE);
                } else if (isDataPerServing()) {
                    ((AddProductActivity) activity).addToMap(PARAM_NUTRITION_DATA_PER, "serving");
                }
                if (!serving_size.getText().toString().isEmpty()) {
                    String servingSize = serving_size.getText().toString() + UNIT[serving_size.getAttachedSpinner().getSelectedItemPosition()];
                    ((AddProductActivity) activity).addToMap(PARAM_SERVING_SIZE, servingSize);
                }
                for (CustomValidatingEditTextView editTextView : getAllEditTextView()) {
                    if (serving_size.getEntryName().equals(editTextView.getEntryName())) {
                        continue;
                    }
                    if (!editTextView.getText().toString().isEmpty()) {
                        addNutrientToMap(editTextView);
                    }
                }
            }
        }
    }

    private void addNutrientToMap(CustomValidatingEditTextView editTextView) {
        String completeName = AddProductNutritionFactsData.getCompleteEntryName(editTextView);
        ((AddProductActivity) activity).addToMap(completeName, editTextView.getText().toString());
        if (hasUnit(editTextView) && editTextView.getAttachedSpinner() != null) {
            ((AddProductActivity) activity)
                .addToMap(completeName + AddProductNutritionFactsData.SUFFIX_UNIT,
                    getSelectedUnit(editTextView.getEntryName(), editTextView.getAttachedSpinner().getSelectedItemPosition()));
        }
    }

    private boolean isDataPer100() {
        return radioGroup.getCheckedRadioButtonId() == R.id.for100g_100ml;
    }

    @OnClick(R.id.btn_add_a_nutrient)
    void addNutrient() {
        new MaterialDialog.Builder(activity)
            .title(R.string.choose_nutrient)
            .items(R.array.nutrients_array)
            .itemsCallback((dialog, itemView, position, text) -> {
                if (!index.contains(position)) {
                    index.add(position);
                    final CustomValidatingEditTextView textView = addNutrientRow(position, text, false, null, 0);
                    allEditViews.add(textView);
                    addValidListener(textView);
                } else {
                    String[] nutrients = getResources().getStringArray(R.array.nutrients_array);
                    Toast.makeText(activity, getString(R.string.nutrient_already_added, nutrients[position]), Toast.LENGTH_SHORT).show();
                }
            })
            .show();
    }

    private float getReferenceValueInGram() {
        float reference = 100;
        if (radioGroup.getCheckedRadioButtonId() != R.id.for100g_100ml) {
            reference = QuantityParserUtil.getFloatValueOrDefault(serving_size, QuantityParserUtil.EntryFormat.NO_PREFIX, reference);
            reference = convertToGrams(reference, serving_size.getAttachedSpinner().getSelectedItemPosition());
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
        final String unit = ALL_UNIT[index];
        //can't be converted to grams.
        if (UnitUtils.UNIT_DV.equals(unit) || UnitUtils.UNIT_IU.equals(unit)) {
            return 0;
        }
        return UnitUtils.convertToGrams(a, unit);
    }

    /**
     * Adds a new row in the tableLayout.
     *
     * @param position The index of the additional nutrient to add in the "PARAM_OTHER_NUTRIENTS" array.
     * @param text The hint text to be displayed in the EditText.
     * @param preFillValues true if the created row needs to be filled by a predefined value.
     * @param value This value will be set to the EditText. Required if 'preFillValues' is true.
     * @param unitSelectedIndex This spinner will be set to this position. Required if 'preFillValues' is true.
     */
    private CustomValidatingEditTextView addNutrientRow(int position, CharSequence text, boolean preFillValues, String value, int unitSelectedIndex) {
        final String nutrientCompleteName = AddProductNutritionFactsData.PARAMS_OTHER_NUTRIENTS.get(position);

        TableRow nutrient = new TableRow(activity);
        nutrient.setPadding(0, dpsToPixels(10), 0, 0);

        CustomValidatingEditTextView editText = new CustomValidatingEditTextView(activity);
        editText.setBackgroundResource(R.drawable.bg_edittext_til);
        editText.setHint(text);
        editText.setId(position);
        final String nutrientShortName = AddProductNutritionFactsData.getShortName(nutrientCompleteName);
        editText.setEntryName(nutrientShortName);
        editText.setKeyListener(keyListener);
        lastEditText.setNextFocusDownId(editText.getId());
        lastEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        lastEditText = editText;
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setSingleLine();
        editText.setGravity(Gravity.CENTER_VERTICAL);
        editText.requestFocus();
        TableRow.LayoutParams lpEditText = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, dpsToPixels(45));
        lpEditText.setMargins(0, dpsToPixels(10), 0, 0);
        editText.setLayoutParams(lpEditText);
        if (preFillValues) {
            editText.setText(value);
        }
        TextInputLayout textInputLayout = new TextInputLayout(activity);
        textInputLayout.addView(editText);

        textInputLayout.setErrorTextAppearance(R.style.errorText);
        nutrient.addView(textInputLayout);

        Spinner spinner = new Spinner(activity);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
            (activity, android.R.layout.simple_spinner_item, activity.getResources().getStringArray(R.array.weight_all_units));
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setBackgroundResource(R.drawable.spinner_weights_grey);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setPadding(dpsToPixels(1), 0, 0, 0);
        final TableRow.LayoutParams spinnerLayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, dpsToPixels(35));
        spinnerLayoutParams.setMargins(dpsToPixels(8), dpsToPixels(16), dpsToPixels(8), dpsToPixels(6));
        spinner.setLayoutParams(spinnerLayoutParams);

        nutrient.addView(spinner);
        editText.setAttachedSpinner(spinner);
        editText.setTextInputLayout(textInputLayout);

        if (Nutriments.PH.equals(nutrientShortName)) {
            spinner.setVisibility(View.INVISIBLE);
        } else if (Nutriments.STARCH.equals(nutrientShortName)) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>
                (activity, android.R.layout.simple_spinner_item, activity.getResources().getStringArray(R.array.weights_array));
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);
            starchEditText = editText;
        }
        if (preFillValues) {
            spinner.setSelection(unitSelectedIndex);
        }
        tableLayout.addView(nutrient);
        return editText;
    }

    private boolean isCarbohydrateRelated(CustomValidatingEditTextView editText) {
        String entryName = editText.getEntryName();
        return sugar.getEntryName().equals(entryName) || (starchEditText != null && entryName.equals(starchEditText.getEntryName()));
    }

    private ValueState checkCarbohydrate(CustomValidatingEditTextView editText, float value) {
        if (!carbohydrate.getEntryName().equals(editText)) {
            return ValueState.NOT_TESTED;
        }
        ValueState res = checkAsGram(editText, value);
        if (ValueState.NOT_VALID.equals(res)) {
            return res;
        }
        float carbsValue = QuantityParserUtil.getFloatValueOrDefault(carbohydrate, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX, 0f);
        float sugarValue = QuantityParserUtil.getFloatValueOrDefault(sugar, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX, 0f);
        // check that value of (sugar + starch) is not greater than value of carbohydrates
        //convert all the values to grams
        carbsValue = convertToGrams(carbsValue, carbohydrate.getAttachedSpinner().getSelectedItemPosition());
        sugarValue = convertToGrams(sugarValue, sugar.getAttachedSpinner().getSelectedItemPosition());
        double newStarch = convertToGrams(getStarchValue(), getStarchUnitSelectedIndex());
        if ((sugarValue + newStarch) > carbsValue) {
            carbohydrate.requestFocus();
            carbohydrate.showError(getString(R.string.error_in_carbohydrate_value));
            return ValueState.NOT_VALID;
        } else {
            return ValueState.VALID;
        }
    }

    private ValueState checkPh(CustomValidatingEditTextView editText, float value) {
        if (Nutriments.PH.equals(editText.getEntryName())) {
            double maxPhValue = 14;
            if (value > maxPhValue || (value >= maxPhValue && QuantityParserUtil.isModifierEqualsToGreaterThan(editText))) {
                editText.setText(Double.toString(maxPhValue));
            }
            return ValueState.VALID;
        }
        return ValueState.NOT_TESTED;
    }

    private ValueState checkPerServing(CustomValidatingEditTextView editText) {
        if (serving_size.getEntryName().equals(editText.getEntryName())) {
            if (isDataPer100()) {
                return ValueState.VALID;
            }
            float value = QuantityParserUtil.getFloatValueOrDefault(serving_size, QuantityParserUtil.EntryFormat.NO_PREFIX, 0);
            if (value <= 0) {
                editText.showError(getString(R.string.error_nutrient_serving_data));
                return ValueState.NOT_VALID;
            }
            return ValueState.VALID;
        }
        return ValueState.NOT_TESTED;
    }

    private ValueState checkEnergy(CustomValidatingEditTextView editTextView, float value) {
        if (energy.getEntryName().equals(editTextView.getEntryName())) {
            float energyInKcal = UnitUtils.convertToKiloCalories(value, getSelectedEnergyUnit());
            if (radioGroup.getCheckedRadioButtonId() != R.id.for100g_100ml) {
                energyInKcal *= (100.0f / getReferenceValueInGram());
            }
            boolean isValid = (energyInKcal <= 2000.0f);
            if (!isValid) {
                editTextView.showError(getString(R.string.max_energy_val_msg));
            }
            return isValid ? ValueState.VALID : ValueState.NOT_VALID;
        }
        return ValueState.NOT_TESTED;
    }

    private ValueState checkAlcohol(CustomValidatingEditTextView editTextView, float value) {
        if (alcohol.getEntryName().equals(editTextView.getEntryName())) {
            if (value > 100) {
                alcohol.setText("100.0");
            }
            return ValueState.VALID;
        }
        return ValueState.NOT_TESTED;
    }

    private float getStarchValue() {
        if (starchEditText == null) {
            return 0;
        }
        final Float floatValue = QuantityParserUtil.getFloatValue(starchEditText, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
        return floatValue == null ? 0 : floatValue;
    }

    private int getStarchUnitSelectedIndex() {
        if (starchEditText == null) {
            return 0;
        }
        return starchEditText.getAttachedSpinner().getSelectedItemPosition();
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
                //nothing to do
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
