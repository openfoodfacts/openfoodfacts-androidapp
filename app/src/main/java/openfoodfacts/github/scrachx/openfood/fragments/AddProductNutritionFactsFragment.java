package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.net.URI;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductNutritionFactsBinding;
import openfoodfacts.github.scrachx.openfood.images.PhotoReceiver;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.jobs.FileDownloader;
import openfoodfacts.github.scrachx.openfood.jobs.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.utils.CustomValidatingEditTextView;
import openfoodfacts.github.scrachx.openfood.utils.FileUtils;
import openfoodfacts.github.scrachx.openfood.utils.ProductUtils;
import openfoodfacts.github.scrachx.openfood.utils.QuantityParserUtil;
import openfoodfacts.github.scrachx.openfood.utils.Stringi18nUtils;
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.utils.ValueState;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;

import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.NUTRITION;

/**
 * @see R.layout#fragment_add_product_nutrition_facts
 */
public class AddProductNutritionFactsFragment extends BaseFragment implements PhotoReceiver {
    private static final String[] ALL_UNIT = {UnitUtils.UNIT_GRAM, UnitUtils.UNIT_MILLIGRAM, UnitUtils.UNIT_MICROGRAM, UnitUtils.UNIT_DV, UnitUtils.UNIT_IU};
    private static final String[] ALL_UNIT_SERVING = {UnitUtils.UNIT_GRAM, UnitUtils.UNIT_MILLIGRAM, UnitUtils.UNIT_MICROGRAM, UnitUtils.UNIT_LITER, UnitUtils.UNIT_MILLILITRE};
    private static final String[] UNIT = {UnitUtils.UNIT_GRAM, UnitUtils.UNIT_MILLIGRAM, UnitUtils.UNIT_MICROGRAM};
    private final NumberKeyListener keyListener = new NumberKeyListener() {
        public int getInputType() {
            return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        }

        @Override
        protected char[] getAcceptedChars() {
            return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ',', '.', '~', '<', '>'};
        }
    };
    private FragmentAddProductNutritionFactsBinding binding;
    private PhotoReceiverHandler photoReceiverHandler;
    //index list stores the index of other nutrients which are used.
    private Set<Integer> index = new HashSet<>();
    private Activity activity;
    private File photoFile;
    private String code;
    private OfflineSavedProduct mOfflineSavedProduct;
    private String imagePath;
    private Product product;
    private EditText lastEditText;
    private CustomValidatingEditTextView starchEditText;
    private Set<CustomValidatingEditTextView> allEditViews = Collections.emptySet();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddProductNutritionFactsBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnAddImageNutritionFacts.setOnClickListener(v -> addNutritionFactsImage());
        binding.btnEditImageNutritionFacts.setOnClickListener(v -> newNutritionFactsImage());
        binding.btnAdd.setOnClickListener(v -> next());
        binding.for100g100ml.setOnClickListener(v -> checkAfterCheckChange());
        binding.btnAddANutrient.setOnClickListener(v -> addNutrient());
        binding.salt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                autoCalculateSodiumValue();
            }
        });
        binding.sodium.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                autoCalculateSaltValue();
            }
        });
        binding.checkboxNoNutritionData.setOnCheckedChangeListener((buttonView, isChecked) -> onCheckedChanged(isChecked));


        photoReceiverHandler = new PhotoReceiverHandler(this);
        binding.btnAddANutrient.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_box_black_18dp, 0, 0, 0);
        Bundle b = getArguments();
        lastEditText = binding.alcohol;
        if (b != null) {
            boolean productEdited;
            product = (Product) b.getSerializable("product");
            mOfflineSavedProduct = (OfflineSavedProduct) b.getSerializable("edit_offline_product");
            productEdited = b.getBoolean(AddProductActivity.KEY_IS_EDITION);
            if (product != null) {
                code = product.getCode();
            }
            if (productEdited && product != null) {
                code = product.getCode();
                binding.btnAdd.setText(R.string.save_edits);
                preFillProductValues();
            } else if (mOfflineSavedProduct != null) {
                code = mOfflineSavedProduct.getBarcode();
                preFillValuesFromOffline();
            } else {
                binding.radioGroup.jumpDrawablesToCurrentState();
            }
        } else {
            Toast.makeText(activity, R.string.error_adding_nutrition_facts, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        binding.alcohol.setImeOptions(EditorInfo.IME_ACTION_DONE);
        binding.energy.requestFocus();
        allEditViews = new HashSet<>(Utils.getViewsByType((ViewGroup) view, CustomValidatingEditTextView.class));
        for (CustomValidatingEditTextView editText : allEditViews) {
            addValidListener(editText);
            checkValue(editText);
        }
        if (getActivity() instanceof AddProductActivity && ((AddProductActivity) getActivity()).getInitialValues() != null) {
            getAllDetails(((AddProductActivity) getActivity()).getInitialValues());
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

    @Nullable
    public AddProductActivity getAddProductActivity() {
        return (AddProductActivity) getActivity();
    }

    /**
     * Pre fill the fields of the product which are already present on the server.
     */
    private void preFillProductValues() {
        loadNutritionImage();
        if (product.getNoNutritionData() != null && product.getNoNutritionData().equalsIgnoreCase("on")) {
            binding.checkboxNoNutritionData.setChecked(true);
            binding.nutritionFactsLayout.setVisibility(View.GONE);
        }
        if (product.getNutritionDataPer() != null && !product.getNutritionDataPer().isEmpty()) {
            updateSelectedDataSize(product.getNutritionDataPer());
        }
        if (product.getServingSize() != null && !product.getServingSize().isEmpty()) {
            String servingSizeValue = product.getServingSize();
            // Splits the serving size into value and unit. Example: "15g" into "15" and "g"
            updateServingSizeFrom(servingSizeValue);
        }
        Nutriments nutriments = product.getNutriments();
        if (nutriments != null && getView() != null) {
            final List<CustomValidatingEditTextView> editViews = Utils.getViewsByType((ViewGroup) getView(), CustomValidatingEditTextView.class);
            for (CustomValidatingEditTextView view : editViews) {
                final String nutrientShortName = view.getEntryName();
                if (nutrientShortName.equals(binding.servingSize.getEntryName())) {
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

    /**
     * Load the nutrition image uploaded form AddProductActivity
     */
    public void loadNutritionImage() {
        if (getAddProductActivity() == null) {
            return;
        }
        photoFile = null;
        final String newImageNutritionUrl = product.getImageNutritionUrl(getAddProductActivity().getProductLanguageForEdition());
        if (newImageNutritionUrl != null && !newImageNutritionUrl.isEmpty()) {
            binding.imageProgress.setVisibility(View.VISIBLE);
            imagePath = newImageNutritionUrl;
            loadNutritionsImage(imagePath);
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
        final String modifier = nutriments.getModifier(nutrientShortName);
        if (modifier != null) {
            return modifier + nutriments.getValue(nutrientShortName);
        } else {
            return nutriments.getValue(nutrientShortName);
        }
    }

    private void updateServingSizeFrom(String servingSize) {
        String[] part = servingSize.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        binding.servingSize.setText(part[0]);
        if (part.length > 1) {
            binding.servingSize.getAttachedSpinner().setSelection(getPositionInServingUnitArray(part[1].trim()));
        }
    }

    /**
     * Pre fill the fields if the product is already present in SavedProductOffline db.
     */
    private void preFillValuesFromOffline() {
        HashMap<String, String> productDetails = mOfflineSavedProduct.getProductDetailsMap();
        if (productDetails != null) {
            if (productDetails.get(OfflineSavedProduct.KEYS.IMAGE_NUTRITION) != null) {
                imagePath = productDetails.get(OfflineSavedProduct.KEYS.IMAGE_NUTRITION);
                final String path = FileUtils.LOCALE_FILE_SCHEME + imagePath;
                binding.imageProgress.setVisibility(View.VISIBLE);
                loadNutritionsImage(path);
            }
            if (productDetails.get(OfflineSavedProduct.KEYS.PARAM_NO_NUTRITION_DATA) != null) {
                binding.checkboxNoNutritionDatanoNutritionData.setChecked(true);
                binding.nutritionFactsLayout.setVisibility(View.GONE);
            }
            if (productDetails.get(OfflineSavedProduct.KEYS.PARAM_NUTRITION_DATA_PER) != null) {
                String s = productDetails.get(OfflineSavedProduct.KEYS.PARAM_NUTRITION_DATA_PER);
                updateSelectedDataSize(s);
            }
            if (productDetails.get(OfflineSavedProduct.KEYS.PARAM_SERVING_SIZE) != null) {
                String servingSizeValue = productDetails.get(OfflineSavedProduct.KEYS.PARAM_SERVING_SIZE);
                // Splits the serving size into value and unit. Example: "15g" into "15" and "g"
                updateServingSizeFrom(servingSizeValue);
            }
            final List<CustomValidatingEditTextView> editViews = Utils.getViewsByType((ViewGroup) getView(), CustomValidatingEditTextView.class);
            for (CustomValidatingEditTextView view : editViews) {
                final String nutrientShortName = view.getEntryName();
                if (nutrientShortName.equals(binding.servingSize.getEntryName())) {
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
        binding.radioGroup.clearCheck();
        if (s.equals(ProductUtils.DEFAULT_NUTRITION_SIZE)) {
            binding.radioGroup.check(R.id.for100g_100ml);
        } else {
            binding.radioGroup.check(R.id.per_serving);
        }
        binding.radioGroup.jumpDrawablesToCurrentState();
    }

    /**
     * lads nutrition image into the ImageView
     *
     * @param path path of the image
     */
    private void loadNutritionsImage(String path) {
        Picasso.get()
            .load(path)
            .resize(dpsToPixels(50), dpsToPixels(50))
            .centerInside()
            .into(binding.btnAddImageNutritionFacts, new Callback() {
                @Override
                public void onSuccess() {
                    nutritionImageLoaded();
                }

                @Override
                public void onError(Exception ex) {
                    nutritionImageLoaded();
                }
            });
    }

    private void nutritionImageLoaded() {
        binding.imageProgress.setVisibility(View.GONE);
        binding.btnEditImageNutritionFacts.setVisibility(View.VISIBLE);
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
        return getSelectedUnit(Nutriments.ENERGY, binding.energy.getAttachedSpinner().getSelectedItemPosition());
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

    private int getPositionInServingUnitArray(String unit) {
        for (int i = 0; i < AddProductNutritionFactsFragment.ALL_UNIT_SERVING.length; i++) {
            if (ALL_UNIT_SERVING[i].equalsIgnoreCase(unit)) {
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

    void addNutritionFactsImage() {
        if (imagePath != null) {
            if (photoFile != null) {
                cropRotateImage(photoFile, getString(R.string.nutrition_facts_picture));
            } else {
                new FileDownloader(getContext()).download(imagePath, file -> {
                    photoFile = file;
                    cropRotateImage(photoFile, getString(R.string.nutrition_facts_picture));
                });
            }
        } else {
            newNutritionFactsImage();
        }
    }

    void newNutritionFactsImage() {
        doChooseOrTakePhotos(getString(R.string.nutrition_facts_picture));
    }

    @Override
    protected void doOnPhotosPermissionGranted() {
        newNutritionFactsImage();
    }

    void next() {
        Activity fragmentActivity = getActivity();
        if (fragmentActivity instanceof AddProductActivity) {
            ((AddProductActivity) fragmentActivity).proceed();
        }
    }

    void checkAfterCheckChange() {
        checkAllValues();
    }

    private void updateButtonState() {
        final boolean allValuesValid = isAllValuesValid();
        binding.globalValidationMsg.setVisibility(allValuesValid ? View.GONE : View.VISIBLE);
        binding.btnAdd.setEnabled(allValuesValid);
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
            checkValue(binding.carbohydrates);
        }
        if (binding.servingSize.getEntryName().equals(text.getEntryName())) {
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

    void autoCalculateSodiumValue() {
        if (activity.getCurrentFocus() == binding.salt) {

            Double saltValue = QuantityParserUtil.getDoubleValue(binding.salt, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
            if (saltValue != null) {
                String saltModifier = QuantityParserUtil.getModifier(binding.salt);
                double sodiumValue = UnitUtils.saltToSodium(saltValue);
                binding.sodium.setText(StringUtils.defaultString(saltModifier) + sodiumValue);
            }
        }
    }

    void autoCalculateSaltValue() {
        if (activity.getCurrentFocus() == binding.sodium) {
            Double sodiumValue = QuantityParserUtil.getDoubleValue(binding.sodium, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX);
            if (sodiumValue != null) {
                String sodiumModifier = QuantityParserUtil.getModifier(binding.sodium);
                double saltValue = UnitUtils.sodiumToSalt(sodiumValue);
                binding.salt.setText(StringUtils.defaultString(sodiumModifier) + saltValue);
            }
        }
    }

    void onCheckedChanged(boolean isChecked) {
        if (isChecked) {
            binding.nutritionFactsLayout.setVisibility(View.GONE);
        } else {
            binding.nutritionFactsLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * adds all the fields to the query map even those which are null or empty.
     */
    public void getAllDetails(Map<String, String> targetMap) {
        if (activity instanceof AddProductActivity) {
            if (binding.checkboxNoNutritionData.isChecked()) {
                targetMap.put(OfflineSavedProduct.KEYS.PARAM_NO_NUTRITION_DATA, "on");
            } else {
                if (isDataPer100()) {
                    targetMap.put(OfflineSavedProduct.KEYS.PARAM_NUTRITION_DATA_PER, ProductUtils.DEFAULT_NUTRITION_SIZE);
                } else if (isDataPerServing()) {
                    targetMap.put(OfflineSavedProduct.KEYS.PARAM_NUTRITION_DATA_PER, "serving");
                }
                if (binding.servingSize.getText() == null || binding.servingSize.getText().toString().isEmpty()) {
                    targetMap.put(OfflineSavedProduct.KEYS.PARAM_SERVING_SIZE, "");
                } else {
                    String servingSizeValue = binding.servingSize.getText().toString() + ObjectUtils.toString(binding.servingSize.getAttachedSpinner().getSelectedItem());
                    targetMap.put(OfflineSavedProduct.KEYS.PARAM_SERVING_SIZE, servingSizeValue);
                }
                for (CustomValidatingEditTextView editTextView : getAllEditTextView()) {
                    if (binding.servingSize.getEntryName().equals(editTextView.getEntryName())) {
                        continue;
                    }
                    addNutrientToMap(editTextView, targetMap);
                }
            }
        }
    }

    private boolean isDataPerServing() {
        return binding.radioGroup.getCheckedRadioButtonId() == R.id.per_serving;
    }

    /**
     * adds only those fields to the query map which are not empty.
     */
    public void getDetails(Map<String, String> targetMap) {
        if (activity instanceof AddProductActivity) {
            if (binding.checkboxNoNutritionData.isChecked()) {
                targetMap.put(OfflineSavedProduct.KEYS.PARAM_NO_NUTRITION_DATA, "on");
            } else {
                if (isDataPer100()) {
                    targetMap.put(OfflineSavedProduct.KEYS.PARAM_NUTRITION_DATA_PER, ProductUtils.DEFAULT_NUTRITION_SIZE);
                } else if (isDataPerServing()) {
                    targetMap.put(OfflineSavedProduct.KEYS.PARAM_NUTRITION_DATA_PER, "serving");
                }
                if (binding.servingSize.getText() != null && !binding.servingSize.getText().toString().isEmpty()) {
                    String servingSizeValue = binding.servingSize.getText().toString() + ObjectUtils.toString(binding.servingSize.getAttachedSpinner().getSelectedItem().toString());
                    targetMap.put(OfflineSavedProduct.KEYS.PARAM_SERVING_SIZE, servingSizeValue);
                }
                for (CustomValidatingEditTextView editTextView : getAllEditTextView()) {
                    if (binding.servingSize.getEntryName().equals(editTextView.getEntryName())) {
                        continue;
                    }
                    if (editTextView.getText() != null && !editTextView.getText().toString().isEmpty()) {
                        addNutrientToMap(editTextView, targetMap);
                    }
                }
            }
        }
    }

    private boolean hasUnit(CustomValidatingEditTextView editTextView) {
        String shortName = editTextView.getEntryName();
        return !Nutriments.PH.equals(shortName) && !Nutriments.ALCOHOL.equals(shortName);
    }

    private boolean isDataPer100() {
        return binding.radioGroup.getCheckedRadioButtonId() == R.id.for100g_100ml;
    }

    /**
     * Add nutients to the map by from the text enetered into EditText
     *
     * @param editTextView EditText with spinner for entering the nutients
     * @param targetMap map to enter the nutrient value recieved from edit texts
     */
    private void addNutrientToMap(CustomValidatingEditTextView editTextView, Map<String, String> targetMap) {
        String completeName = AddProductNutritionFactsData.getCompleteEntryName(editTextView);
        targetMap.put(completeName, editTextView.getText().toString());
        if (hasUnit(editTextView) && editTextView.getAttachedSpinner() != null) {
            targetMap.put(completeName + AddProductNutritionFactsData.SUFFIX_UNIT,
                getSelectedUnit(editTextView.getEntryName(), editTextView.getAttachedSpinner().getSelectedItemPosition()));
        }
    }

    private float getReferenceValueInGram() {
        float reference = 100;
        if (binding.radioGroup.getCheckedRadioButtonId() != R.id.for100g_100ml) {
            reference = QuantityParserUtil.getFloatValueOrDefault(binding.servingSize, QuantityParserUtil.EntryFormat.NO_PREFIX, reference);
            reference = UnitUtils.convertToGrams(reference, ALL_UNIT_SERVING[binding.servingSize.getAttachedSpinner().getSelectedItemPosition()]);
        }
        return reference;
    }

    void addNutrient() {
        String[] nutrients = getResources().getStringArray(R.array.nutrients_array);
        Stringi18nUtils.sortAlphabetically(nutrients, Collator.getInstance(Locale.getDefault()));

        new MaterialDialog.Builder(activity)
            .title(R.string.choose_nutrient)
            .items(nutrients)
            .itemsCallback((dialog, itemView, position, text) -> {
                if (!index.contains(position)) {
                    index.add(position);
                    final CustomValidatingEditTextView textView = addNutrientRow(position, text, false, null, 0);
                    allEditViews.add(textView);
                    addValidListener(textView);
                } else {
                    Toast.makeText(activity, getString(R.string.nutrient_already_added, nutrients[position]), Toast.LENGTH_SHORT).show();
                }
            })
            .show();
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
        TableRow.LayoutParams lpEditText = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpsToPixels(45));
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
        final TableRow.LayoutParams spinnerLayoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpsToPixels(35));
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
        binding.tableLayout.addView(nutrient);
        return editText;
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

    private boolean isCarbohydrateRelated(CustomValidatingEditTextView editText) {
        String entryName = editText.getEntryName();
        return binding.sugars.getEntryName().equals(entryName) || (starchEditText != null && entryName.equals(starchEditText.getEntryName()));
    }

    /**
     * Validate the value of carbohydrate using carbs value and sugar value
     *
     * @param editText CustomValidatingEditTextView for retrieving the value enterd by the user
     * @param value quality value with known prefix
     */
    private ValueState checkCarbohydrate(CustomValidatingEditTextView editText, float value) {
        if (!binding.carbohydrates.getEntryName().equals(editText.getEntryName())) {
            return ValueState.NOT_TESTED;
        }
        ValueState res = checkAsGram(editText, value);
        if (ValueState.NOT_VALID.equals(res)) {
            return res;
        }
        float carbsValue = QuantityParserUtil.getFloatValueOrDefault(binding.carbohydrates, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX, 0f);
        float sugarValue = QuantityParserUtil.getFloatValueOrDefault(binding.sugars, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX, 0f);
        // check that value of (sugar + starch) is not greater than value of carbohydrates
        //convert all the values to grams
        carbsValue = convertToGrams(carbsValue, binding.carbohydrates.getAttachedSpinner().getSelectedItemPosition());
        sugarValue = convertToGrams(sugarValue, binding.sugars.getAttachedSpinner().getSelectedItemPosition());
        double newStarch = convertToGrams(getStarchValue(), getStarchUnitSelectedIndex());
        if ((sugarValue + newStarch) > carbsValue) {
            binding.carbohydrates.showError(getString(R.string.error_in_carbohydrate_value));
            return ValueState.NOT_VALID;
        } else {
            return ValueState.VALID;
        }
    }

    /**
     * Validate serving size value entered by user
     */
    private ValueState checkPerServing(CustomValidatingEditTextView editText) {
        if (binding.servingSize.getEntryName().equals(editText.getEntryName())) {
            if (isDataPer100()) {
                return ValueState.VALID;
            }
            float value = QuantityParserUtil.getFloatValueOrDefault(binding.servingSize, QuantityParserUtil.EntryFormat.NO_PREFIX, 0);
            if (value <= 0) {
                editText.showError(getString(R.string.error_nutrient_serving_data));
                return ValueState.NOT_VALID;
            }
            return ValueState.VALID;
        }
        return ValueState.NOT_TESTED;
    }

    /**
     * Validate oh value according to Nutriments.PH
     *
     * @param editText CustomValidatingEditTextView for recieving value inputed from user
     * @param value quality value with known prefix
     */
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

    /**
     * Validate energy value entered by user
     */
    private ValueState checkEnergy(CustomValidatingEditTextView editTextView, float value) {
        if (binding.energy.getEntryName().equals(editTextView.getEntryName())) {
            float energyInKcal = UnitUtils.convertToKiloCalories(value, getSelectedEnergyUnit());
            if (binding.radioGroup.getCheckedRadioButtonId() != R.id.for100g_100ml) {
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

    /**
     * validate alcohol content entered by user
     */
    private ValueState checkAlcohol(CustomValidatingEditTextView editTextView, float value) {
        if (binding.alcohol.getEntryName().equals(editTextView.getEntryName())) {
            if (value > 100) {
                binding.alcohol.setText("100.0");
            }
            return ValueState.VALID;
        }
        return ValueState.NOT_TESTED;
    }

    public void showImageProgress() {
        if (!isAdded() || binding.imageProgress == null) {
            return;
        }
        binding.imageProgress.setVisibility(View.VISIBLE);
        binding.imageProgressText.setVisibility(View.VISIBLE);
        binding.btnAddImageNutritionFacts.setVisibility(View.INVISIBLE);
        binding.btnEditImageNutritionFacts.setVisibility(View.INVISIBLE);
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

    @Override
    public void onPhotoReturned(File newPhotoFile) {
        URI resultUri = newPhotoFile.toURI();
        imagePath = resultUri.getPath();

        photoFile = newPhotoFile;
        ProductImage image = new ProductImage(code, NUTRITION, newPhotoFile);
        image.setFilePath(resultUri.getPath());
        if (activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).addToPhotoMap(image, 2);
        }
        hideImageProgress(false, getString(R.string.image_uploaded_successfully));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data);
    }

    public void hideImageProgress(boolean errorInUploading, String message) {
        if (!isAdded() || binding.imageProgress == null) {
            return;
        }
        binding.imageProgress.setVisibility(View.GONE);
        binding.imageProgressText.setVisibility(View.GONE);
        binding.btnAddImageNutritionFacts.setVisibility(View.VISIBLE);
        binding.btnEditImageNutritionFacts.setVisibility(View.VISIBLE);
        if (!errorInUploading) {
            Picasso.get()
                .load(photoFile)
                .resize(dpsToPixels(50), dpsToPixels(50))
                .centerInside()
                .into(binding.btnAddImageNutritionFacts);
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
            if (binding.salt.getEntryName().equals(editTextView.getEntryName())) {
                binding.sodium.getAttachedSpinner().setSelection(binding.salt.getAttachedSpinner().getSelectedItemPosition());
            }
            if (binding.sodium.getEntryName().equals(editTextView.getEntryName())) {
                binding.salt.getAttachedSpinner().setSelection(binding.sodium.getAttachedSpinner().getSelectedItemPosition());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            checkValueAndRelated(editTextView);
        }
    }
}
