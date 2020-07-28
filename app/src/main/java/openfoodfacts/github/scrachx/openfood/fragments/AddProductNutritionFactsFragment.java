/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.net.URI;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductNutritionFactsBinding;
import openfoodfacts.github.scrachx.openfood.images.PhotoReceiver;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Nutriments.Nutriment;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.Units;
import openfoodfacts.github.scrachx.openfood.network.ApiFields;
import openfoodfacts.github.scrachx.openfood.utils.CustomValidatingEditTextView;
import openfoodfacts.github.scrachx.openfood.utils.EditTextUtils;
import openfoodfacts.github.scrachx.openfood.utils.FileDownloader;
import openfoodfacts.github.scrachx.openfood.utils.FileUtils;
import openfoodfacts.github.scrachx.openfood.utils.Modifier;
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler;
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
    private static final String[] NUTRIENTS_UNITS = {Units.UNIT_GRAM, Units.UNIT_MILLIGRAM, Units.UNIT_MICROGRAM, Units.UNIT_DV, UnitUtils.UNIT_IU};
    private static final String[] SERVING_UNITS = {Units.UNIT_GRAM, Units.UNIT_MILLIGRAM, Units.UNIT_MICROGRAM, Units.UNIT_LITER, Units.UNIT_MILLILITRE};
    private final NumberKeyListener keyListener = new NumberKeyListener() {
        public int getInputType() {
            return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        }

        @Override
        protected char[] getAcceptedChars() {
            return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ',', '.'};
        }
    };
    private FragmentAddProductNutritionFactsBinding binding;
    private PhotoReceiverHandler photoReceiverHandler;
    //index list stores the index of other nutrients which are used.
    private Set<Integer> index = new HashSet<>();
    private Activity activity;
    private File photoFile;
    private String productCode;
    private OfflineSavedProduct mOfflineSavedProduct;
    private String imagePath;
    private CompositeDisposable disp = new CompositeDisposable();
    private Product product;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disp.dispose();
        binding = null;
    }

    private EditText lastEditText;
    private CustomValidatingEditTextView starchEditText;
    private Set<CustomValidatingEditTextView> allEditViews = Collections.emptySet();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddProductNutritionFactsBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnAddImageNutritionFacts.setOnClickListener(v -> addNutritionFactsImage());
        binding.btnEditImageNutritionFacts.setOnClickListener(v -> newNutritionFactsImage());
        binding.btnAdd.setOnClickListener(v -> next());
        binding.for100g100ml.setOnClickListener(v -> checkAllValues());
        binding.btnAddANutrient.setOnClickListener(v -> displayAddNutrientDialog());

        binding.salt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSodiumValue();
            }
        });
        binding.spinnerSaltComp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSodiumMod();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // This is not possible
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
                updateSaltValue();
            }
        });
        binding.spinnerSodiumComp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSaltMod();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // This is not possible
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
            productEdited = b.getBoolean(AddProductActivity.KEY_IS_EDITING);
            if (product != null) {
                productCode = product.getCode();
            }
            if (productEdited && product != null) {
                productCode = product.getCode();
                binding.btnAdd.setText(R.string.save_edits);
                preFillProductValues();
            } else if (mOfflineSavedProduct != null) {
                productCode = mOfflineSavedProduct.getBarcode();
                preFillValuesFromOffline();
            } else {
                binding.radioGroup.jumpDrawablesToCurrentState();
            }
        } else {
            Toast.makeText(activity, R.string.error_adding_nutrition_facts, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        binding.alcohol.setImeOptions(EditorInfo.IME_ACTION_DONE);
        binding.energyKcal.requestFocus();
        allEditViews = new HashSet<>(Utils.getViewsByType((ViewGroup) view, CustomValidatingEditTextView.class));
        for (CustomValidatingEditTextView editText : allEditViews) {
            addValidListener(editText);
            checkValue(editText);
        }
        if (getActivity() instanceof AddProductActivity && ((AddProductActivity) getActivity()).getInitialValues() != null) {
            addAllFieldsToMap(((AddProductActivity) getActivity()).getInitialValues());
        }
    }

    private void updateSodiumMod() {
        binding.spinnerSodiumComp.setSelection(binding.spinnerSaltComp.getSelectedItemPosition());
    }

    private void updateSaltMod() {
        binding.spinnerSaltComp.setSelection(binding.spinnerSodiumComp.getSelectedItemPosition());
    }

    private void checkAllValues() {
        for (CustomValidatingEditTextView editText : getAllEditTextView()) {
            checkValue(editText);
        }
    }

    private Collection<CustomValidatingEditTextView> getAllEditTextView() {
        return allEditViews;
    }

    private boolean isAllValuesValid() {
        for (CustomValidatingEditTextView editText : getAllEditTextView()) {
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

        // Set no nutrition data checkbox
        if (product.getNoNutritionData() != null && product.getNoNutritionData().equalsIgnoreCase("on")) {
            binding.checkboxNoNutritionData.setChecked(true);
            binding.nutritionFactsLayout.setVisibility(View.GONE);
        }

        // Set nutrition data per
        if (product.getNutritionDataPer() != null && !product.getNutritionDataPer().isEmpty()) {
            updateSelectedDataPer(product.getNutritionDataPer());
        }

        // Set serving size
        if (product.getServingSize() != null && !product.getServingSize().isEmpty()) {
            String servingSizeValue = product.getServingSize();
            // Splits the serving size into value and unit. Example: "15g" into "15" and "g"
            updateServingSizeFrom(servingSizeValue);
        }

        final Nutriments nutriments = product.getNutriments();
        if (nutriments == null || getView() == null) {
            return;
        }
        binding.energyKj.setText(nutriments.getEnergyKjValue(isDataPerServing()));
        binding.energyKcal.setText(nutriments.getEnergyKcalValue(isDataPerServing()));

        // Fill default nutriments fields
        final List<CustomValidatingEditTextView> editViews = Utils.getViewsByType((ViewGroup) getView(), CustomValidatingEditTextView.class);
        for (CustomValidatingEditTextView view : editViews) {
            final String nutrientShortName = view.getEntryName();
            // Skip serving size and energy view, we already filled them
            if (view == binding.servingSize || view == binding.energyKcal || view == binding.energyKj) {
                continue;
            }

            // Get the value
            String value = isDataPer100g() ? nutriments.get100g(nutrientShortName) : nutriments.getServing(nutrientShortName);
            if (value.isEmpty()) {
                continue;
            }
            view.setText(value);
            if (view.getUnitSpinner() != null) {
                view.getUnitSpinner().setSelection(getSelectedUnitFromShortName(nutriments, nutrientShortName));
            }
            if (view.getModSpinner() != null) {
                view.getModSpinner().setSelection(getSelectedModifierFromShortName(nutriments, nutrientShortName));
            }
        }

        // Set the values of all the other nutrients if defined and create new row in the tableLayout.
        for (int i = 0; i < AddProductNutritionFactsData.PARAMS_OTHER_NUTRIENTS.size(); i++) {
            String nutrientShortName = AddProductNutritionFactsData.getShortName(AddProductNutritionFactsData.PARAMS_OTHER_NUTRIENTS.get(i));
            String value = isDataPer100g() ? nutriments.get100g(nutrientShortName) : nutriments.getServing(nutrientShortName);
            if (value.isEmpty()) {
                continue;
            }
            int unitIndex = getSelectedUnitFromShortName(nutriments, nutrientShortName);
            int modIndex = getSelectedModifierFromShortName(nutriments, nutrientShortName);
            index.add(i);
            String[] nutrients = getResources().getStringArray(R.array.nutrients_array);
            addNutrientRow(i, nutrients[i], true, value, unitIndex, modIndex);
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
        if (newImageNutritionUrl == null || newImageNutritionUrl.isEmpty()) {
            return;
        }
        binding.imageProgress.setVisibility(View.VISIBLE);
        imagePath = newImageNutritionUrl;
        loadNutritionsImage(imagePath);
    }

    private int getSelectedUnitFromShortName(Nutriments nutriments, String nutrientShortName) {
        final String unit = nutriments.getUnit(nutrientShortName);
        return getSelectedUnit(nutrientShortName, unit);
    }

    private int getSelectedUnit(String nutrientShortName, String unit) {
        int unitSelectedIndex = 0;
        if (unit != null) {
            if (Nutriments.ENERGY_KCAL.equals(nutrientShortName) || Nutriments.ENERGY_KJ.equals(nutrientShortName)) {
                throw new IllegalArgumentException("nutrient cannot be energy");
            } else {
                unitSelectedIndex = getPositionInAllUnitArray(unit);
            }
        }
        return unitSelectedIndex;
    }

    private int getSelectedModifierFromShortName(Nutriments nutriments, String nutrientShortName) {
        final String mod = nutriments.getModifier(nutrientShortName);
        return getPositionInModifierArray(mod);
    }

    private void updateServingSizeFrom(String servingSize) {
        String[] part = servingSize.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        binding.servingSize.setText(part[0]);
        if (part.length > 1) {
            binding.servingSize.getUnitSpinner().setSelection(getPositionInServingUnitArray(part[1].trim()));
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
                final String path = FileUtils.LOCALE_FILE_SCHEME + imagePath;
                binding.imageProgress.setVisibility(View.VISIBLE);
                loadNutritionsImage(path);
            }
            if (productDetails.get(ApiFields.Keys.NO_NUTRITION_DATA) != null) {
                binding.checkboxNoNutritionData.setChecked(true);
                binding.nutritionFactsLayout.setVisibility(View.GONE);
            }
            if (productDetails.get(ApiFields.Keys.NUTRITION_DATA_PER) != null) {
                String nutritionDataPer = productDetails.get(ApiFields.Keys.NUTRITION_DATA_PER);
                // can be "100g" or "serving"
                updateSelectedDataPer(nutritionDataPer);
            }
            if (productDetails.get(ApiFields.Keys.SERVING_SIZE) != null) {
                String servingSizeValue = productDetails.get(ApiFields.Keys.SERVING_SIZE);
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
                    if (view.getUnitSpinner() != null) {
                        view.getUnitSpinner()
                            .setSelection(getSelectedUnit(nutrientShortName, productDetails.get(nutrientCompleteName + ApiFields.Suffix.UNIT)));
                    }
                }
            }
            //set the values of all the other nutrients if defined and create new row in the tableLayout.
            for (int i = 0; i < AddProductNutritionFactsData.PARAMS_OTHER_NUTRIENTS.size(); i++) {
                String completeNutrientName = AddProductNutritionFactsData.PARAMS_OTHER_NUTRIENTS.get(i);
                if (productDetails.get(completeNutrientName) != null) {
                    int unitIndex = 0;
                    int modIndex = 0;
                    String value = productDetails.get(completeNutrientName);
                    if (productDetails.get(completeNutrientName + ApiFields.Suffix.UNIT) != null) {
                        unitIndex = getPositionInAllUnitArray(productDetails.get(completeNutrientName + ApiFields.Suffix.UNIT));
                    }
                    if (productDetails.get(completeNutrientName + ApiFields.Suffix.MODIFIER) != null) {
                        modIndex = getPositionInAllUnitArray(productDetails.get(completeNutrientName + ApiFields.Suffix.MODIFIER));
                    }

                    index.add(i);
                    String[] nutrients = getResources().getStringArray(R.array.nutrients_array);
                    addNutrientRow(i, nutrients[i], true, value, unitIndex, modIndex);
                }
            }
        }
    }

    private void updateSelectedDataPer(@NonNull String value) {
        binding.radioGroup.clearCheck();
        if (value.equals(ApiFields.Defaults.NUTRITION_DATA_PER_100G)) {
            binding.radioGroup.check(R.id.for100g_100ml);
        } else if (value.equals(ApiFields.Defaults.NUTRITION_DATA_PER_SERVING)) {
            binding.radioGroup.check(R.id.per_serving);
        } else {
            throw new IllegalArgumentException("value is neither 100g nor serving");
        }
        binding.radioGroup.jumpDrawablesToCurrentState();
    }

    /**
     * Loads nutrition image into the ImageView
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
                    afterNutritionImgLoaded();
                }

                @Override
                public void onError(Exception ex) {
                    afterNutritionImgLoaded();
                }
            });
    }

    private void afterNutritionImgLoaded() {
        binding.imageProgress.setVisibility(View.GONE);
        binding.btnEditImageNutritionFacts.setVisibility(View.VISIBLE);
    }

    private String getSelectedUnit(int selectedIdx) {
        return NUTRIENTS_UNITS[selectedIdx];
    }

    /**
     * @param unit The unit corresponding to which the index is to be returned.
     * @return returns the index to be set to the spinner.
     */
    private int getPositionInAllUnitArray(String unit) {
        for (int i = 0; i < AddProductNutritionFactsFragment.NUTRIENTS_UNITS.length; i++) {
            if (NUTRIENTS_UNITS[i].equalsIgnoreCase(unit)) {
                return i;
            }
        }
        return 0;
    }

    private int getPositionInModifierArray(String mod) {
        for (int i = 0; i < Modifier.MODIFIERS.length; i++) {
            if (Modifier.MODIFIERS[i].equals(mod)) {
                return i;
            }
        }
        return 0;
    }

    private int getPositionInServingUnitArray(String unit) {
        for (int i = 0; i < AddProductNutritionFactsFragment.SERVING_UNITS.length; i++) {
            if (SERVING_UNITS[i].equalsIgnoreCase(unit)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    void addNutritionFactsImage() {
        if (imagePath != null) {
            if (photoFile != null) {
                cropRotateImage(photoFile, getString(R.string.nutrition_facts_picture));
            } else {
                disp.add(FileDownloader.download(requireContext(), imagePath)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file -> {
                        photoFile = file;
                        cropRotateImage(photoFile, getString(R.string.nutrition_facts_picture));
                    }));
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

    private void updateNextBtnState() {
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
        res = checkEnergyField(text, value);
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
        boolean valid = convertToGrams(value, text.getUnitSpinner().getSelectedItemPosition()) <= reference;
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
            Float value = QuantityParserUtil.getFloatValue(text);
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
            updateNextBtnState();
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
        if (target.getUnitSpinner() != null) {
            target.getUnitSpinner().setOnItemSelectedListener(textWatcher);
        }
    }

    void updateSodiumValue() {
        if (activity.getCurrentFocus() == binding.salt) {

            Double saltValue = QuantityParserUtil.getDoubleValue(binding.salt);
            if (saltValue != null) {
                double sodiumValue = UnitUtils.saltToSodium(saltValue);
                binding.sodium.setText(String.valueOf(sodiumValue));
            }
        }
    }

    void updateSaltValue() {
        if (activity.getCurrentFocus() == binding.sodium) {
            Double sodiumValue = QuantityParserUtil.getDoubleValue(binding.sodium);
            if (sodiumValue != null) {
                double saltValue = UnitUtils.sodiumToSalt(sodiumValue);
                binding.salt.setText(String.valueOf(saltValue));
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
     * adds only those fields to the query map which are not empty.
     */
    public void addUpdatedFieldsToMap(Map<String, String> targetMap) {

        // Add no nutrition data entry to map
        if (binding.checkboxNoNutritionData.isChecked()) {
            targetMap.put(ApiFields.Keys.NO_NUTRITION_DATA, "on");
        } else {
            addNutrientsModeToMap(targetMap);
        }

        // Add serving size entry to map if it has been changed
        if (EditTextUtils.isNotEmpty(binding.servingSize)) {
            String servingSizeValue = EditTextUtils.getContent(binding.servingSize) + ObjectUtils
                .toString(binding.servingSize.getUnitSpinner().getSelectedItem().toString());
            if (product == null || !servingSizeValue.equals(product.getServingSize())) {
                targetMap.put(ApiFields.Keys.SERVING_SIZE, servingSizeValue);
            }
        }

        // For every nutrition field add it to map if updated
        for (CustomValidatingEditTextView editTextView : getAllEditTextView()) {
            if (binding.servingSize.getEntryName().equals(editTextView.getEntryName())) {
                continue;
            }
            if (editTextView.getText() != null && EditTextUtils.isNotEmpty(editTextView)) {
                addNutrientToMapIfUpdated(editTextView, targetMap);
            }
        }
    }

    /**
     * adds all the fields to the query map even those which are null or empty.
     */
    public void addAllFieldsToMap(Map<String, String> targetMap) {
        if (!(activity instanceof AddProductActivity)) {
            return;
        }

        final boolean noData = binding.checkboxNoNutritionData.isChecked();
        if (noData) {
            targetMap.put(ApiFields.Keys.NO_NUTRITION_DATA, "on");
            return;
        }

        String servingSizeValue;
        if (binding.servingSize.getText() == null || binding.servingSize.getText().toString().isEmpty()) {
            servingSizeValue = StringUtils.EMPTY;
        } else {
            servingSizeValue = binding.servingSize.getText().toString() + ObjectUtils.toString(binding.servingSize.getUnitSpinner().getSelectedItem());
        }
        targetMap.put(ApiFields.Keys.SERVING_SIZE, servingSizeValue);

        for (CustomValidatingEditTextView editTextView : getAllEditTextView()) {
            if (binding.servingSize.getEntryName().equals(editTextView.getEntryName())) {
                continue;
            }
            addNutrientToMap(editTextView, targetMap);
        }
    }

    /**
     * Add nutrients to the map by from the text entered into EditText, only if the value has been edited
     *
     * @param editTextView EditText with spinner for entering the nutients
     * @param targetMap map to enter the nutrient value recieved from edit texts
     */
    private void addNutrientToMapIfUpdated(CustomValidatingEditTextView editTextView, Map<String, String> targetMap) {

        Nutriments productNutriments = product != null ? product.getNutriments() : new Nutriments();

        String shortName = editTextView.getEntryName();

        Nutriment oldProductNutriment = productNutriments.get(shortName);
        String oldValue = null;
        String oldUnit = null;
        String oldMod = null;
        if (oldProductNutriment != null) {
            oldUnit = oldProductNutriment.getUnit();
            oldMod = oldProductNutriment.getModifier();
            if (isDataPer100g()) {
                oldValue = oldProductNutriment.getFor100gInUnits();
            } else if (isDataPerServing()) {
                oldValue = oldProductNutriment.getForServingInUnits();
            }
        }

        boolean valueHasBeenUpdated = EditTextUtils.isDifferent(editTextView, oldValue);

        String newUnit = null;
        String newMod = null;
        if (EditTextUtils.hasUnit(editTextView) && editTextView.getUnitSpinner() != null) {
            newUnit = getSelectedUnit(editTextView.getUnitSpinner().getSelectedItemPosition());
        }
        if (editTextView.getModSpinner() != null) {
            newMod = editTextView.getModSpinner().getSelectedItem().toString();
        }
        boolean unitHasBeenUpdated = oldUnit == null || !oldUnit.equals(newUnit);
        boolean modHasBeenUpdated = oldMod == null || !oldMod.equals(newMod);

        if (valueHasBeenUpdated || unitHasBeenUpdated || modHasBeenUpdated) {
            addNutrientToMap(editTextView, targetMap);
        }
    }

    /**
     * Add nutrients to the map by from the text entered into EditText
     *
     * @param editTextView EditText with spinner for entering the nutrients
     * @param targetMap map to enter the nutrient value received from edit texts
     */
    private void addNutrientToMap(@NonNull CustomValidatingEditTextView editTextView, @NonNull Map<String, String> targetMap) {
        // For impl reference, see https://wiki.openfoodfacts.org/Nutrients_handling_in_Open_Food_Facts#Data_display
        final String fieldName = AddProductNutritionFactsData.getCompleteEntryName(editTextView);

        // Add unit field {nutrient-id}_unit to map
        if (EditTextUtils.hasUnit(editTextView) && editTextView.getUnitSpinner() != null) {
            final String selectedUnit = getSelectedUnit(editTextView.getUnitSpinner().getSelectedItemPosition());
            targetMap.put(fieldName + ApiFields.Suffix.UNIT, Html.escapeHtml(selectedUnit));
        }

        // Take modifier from attached spinner, add to value if not the default one
        String mod = "";
        if (editTextView.getModSpinner() != null) {
            String selectedMod = editTextView.getModSpinner().getSelectedItem().toString();
            if (!Modifier.DEFAULT_MODIFIER.equals(selectedMod)) {
                mod = selectedMod;
            }
        }
        // The suffix can either be _serving or _100g depending on user input
        final String value = Objects.requireNonNull(editTextView.getText()).toString();
        targetMap.put(fieldName, mod + value);
    }

    private void addNutrientsModeToMap(@NonNull Map<String, String> targetMap) {
        if (isDataPer100g()) {
            targetMap.put(ApiFields.Keys.NUTRITION_DATA_PER, ApiFields.Defaults.NUTRITION_DATA_PER_100G);
        } else if (isDataPerServing()) {
            targetMap.put(ApiFields.Keys.NUTRITION_DATA_PER, ApiFields.Defaults.NUTRITION_DATA_PER_SERVING);
        }
    }

    private boolean isDataPerServing() {
        return binding.radioGroup.getCheckedRadioButtonId() == R.id.per_serving;
    }

    private boolean isDataPer100g() {
        return binding.radioGroup.getCheckedRadioButtonId() == R.id.for100g_100ml;
    }

    private float getReferenceValueInGram() {
        float reference = 100;
        if (binding.radioGroup.getCheckedRadioButtonId() != R.id.for100g_100ml) {
            reference = QuantityParserUtil.getFloatValueOrDefault(binding.servingSize, reference);
            reference = UnitUtils.convertToGrams(reference, SERVING_UNITS[binding.servingSize.getUnitSpinner().getSelectedItemPosition()]);
        }
        return reference;
    }

    void displayAddNutrientDialog() {
        final List<String> origNutrients = Arrays.asList(getResources().getStringArray(R.array.nutrients_array));
        List<String> nutrients = new ArrayList<>(origNutrients);

        for (int i : index) {
            nutrients.remove(origNutrients.get(i));
        }

        Stringi18nUtils.sortAlphabetically(nutrients, Collator.getInstance(Locale.getDefault()));

        new MaterialDialog.Builder(activity)
            .title(R.string.choose_nutrient)
            .items(nutrients)
            .itemsCallback((dialog, itemView, position, text) -> {
                index.add(origNutrients.indexOf(text));
                final CustomValidatingEditTextView textView = addNutrientRow(origNutrients.indexOf(text), text.toString());
                allEditViews.add(textView);
                addValidListener(textView);
            })
            .show();
    }

    /**
     * Adds a new row in the tableLayout.
     */
    private CustomValidatingEditTextView addNutrientRow(int position, String text) {
        return addNutrientRow(position, text, false, null, 0, 0);
    }

    /**
     * Adds a new row in the tableLayout.
     *
     * @param index The index of the additional nutrient to add in the "PARAM_OTHER_NUTRIENTS" array.
     * @param hint The hint text to be displayed in the EditText.
     * @param preFillValues true if the created row needs to be filled by a predefined value.
     * @param value This value will be set to the EditText. Required if 'preFillValues' is true.
     * @param unitSelectedIndex This spinner will be set to this position. Required if 'preFillValues' is true.
     */
    private CustomValidatingEditTextView addNutrientRow(
        int index,
        String hint,
        boolean preFillValues,
        String value,
        int unitSelectedIndex,
        int modSelectedIndex
    ) {
        final String nutrientCompleteName = AddProductNutritionFactsData.PARAMS_OTHER_NUTRIENTS.get(index);

        final TableRow rowView = (TableRow) getLayoutInflater().inflate(R.layout.nutrition_facts_table_row, binding.tableLayout, false);

        CustomValidatingEditTextView editText = rowView.findViewById(R.id.value);
        editText.setHint(hint);
        final String nutrientShortName = AddProductNutritionFactsData.getShortName(nutrientCompleteName);
        editText.setEntryName(nutrientShortName);
        editText.setKeyListener(keyListener);
        lastEditText.setNextFocusDownId(editText.getId());
        lastEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        lastEditText = editText;
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.requestFocus();

        if (preFillValues) {
            editText.setText(value);
        }

        // Setup unit spinner
        final Spinner unitSpinner = rowView.findViewById(R.id.spinner_unit);
        final Spinner modSpinner = rowView.findViewById(R.id.spinner_mod);

        if (Nutriments.PH.equals(nutrientShortName)) {
            unitSpinner.setVisibility(View.INVISIBLE);
        } else if (Nutriments.STARCH.equals(nutrientShortName)) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>
                (activity, android.R.layout.simple_spinner_item, activity.getResources().getStringArray(R.array.weights_array));
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unitSpinner.setAdapter(arrayAdapter);
            starchEditText = editText;
        }

        if (preFillValues) {
            unitSpinner.setSelection(unitSelectedIndex);
            modSpinner.setSelection(modSelectedIndex);
        }

        binding.tableLayout.addView(rowView);
        return editText;
    }

    /**
     * Converts a given quantity's unit to grams.
     *
     * @param value The value to be converted
     * @param index 1 represents milligrams, 2 represents micrograms
     * @return return the converted value
     */
    private float convertToGrams(float value, int index) {
        final String unit = NUTRIENTS_UNITS[index];
        //can't be converted to grams.
        if (Units.UNIT_DV.equals(unit) || UnitUtils.UNIT_IU.equals(unit)) {
            return 0;
        }
        return UnitUtils.convertToGrams(value, unit);
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
        float carbsValue = QuantityParserUtil.getFloatValueOrDefault(binding.carbohydrates, 0f);
        float sugarValue = QuantityParserUtil.getFloatValueOrDefault(binding.sugars, 0f);
        // check that value of (sugar + starch) is not greater than value of carbohydrates
        //convert all the values to grams
        carbsValue = convertToGrams(carbsValue, binding.carbohydrates.getUnitSpinner().getSelectedItemPosition());
        sugarValue = convertToGrams(sugarValue, binding.sugars.getUnitSpinner().getSelectedItemPosition());
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
            if (isDataPer100g()) {
                return ValueState.VALID;
            }
            float value = QuantityParserUtil.getFloatValueOrDefault(binding.servingSize, 0);
            if (value <= 0) {
                editText.showError(getString(R.string.error_nutrient_serving_data));
                return ValueState.NOT_VALID;
            }
            return ValueState.VALID;
        }
        return ValueState.NOT_TESTED;
    }

    /**
     * Validate oh value according to {@link Nutriments#PH}
     *
     * @param editText {@link CustomValidatingEditTextView} to get the value inputted from user
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
    private ValueState checkEnergyField(CustomValidatingEditTextView editTextView, float value) {
        if (editTextView.getEntryName().equals(binding.energyKcal.getEntryName())) {
            float energyInKcal = value;
            if (binding.radioGroup.getCheckedRadioButtonId() != R.id.for100g_100ml) {
                energyInKcal *= (100.0f / getReferenceValueInGram());
            }
            boolean isValid = (energyInKcal <= 2000f);
            if (!isValid) {
                editTextView.showError(getString(R.string.max_energy_val_msg));
            }
            return isValid ? ValueState.VALID : ValueState.NOT_VALID;
        } else if (editTextView.getEntryName().equals(binding.energyKj.getEntryName())) {
            float energyInKj = value;
            if (binding.radioGroup.getCheckedRadioButtonId() != R.id.for100g_100ml) {
                energyInKj *= (100.0f / getReferenceValueInGram());
            }
            boolean isValid = (energyInKj <= 8368000f);
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
        if (!isAdded()) {
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
        final Float floatValue = QuantityParserUtil.getFloatValue(starchEditText);
        return floatValue == null ? 0 : floatValue;
    }

    private int getStarchUnitSelectedIndex() {
        if (starchEditText == null) {
            return 0;
        }
        return starchEditText.getUnitSpinner().getSelectedItemPosition();
    }

    @Override
    public void onPhotoReturned(File newPhotoFile) {
        URI resultUri = newPhotoFile.toURI();
        imagePath = resultUri.getPath();

        photoFile = newPhotoFile;
        ProductImage image = new ProductImage(productCode, NUTRITION, newPhotoFile);
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
        if (!isAdded()) {
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

    class ValidTextWatcher implements TextWatcher, AdapterView.OnItemSelectedListener {
        private final CustomValidatingEditTextView editTextView;

        ValidTextWatcher(CustomValidatingEditTextView editTextView) {
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
                binding.sodium.getUnitSpinner().setSelection(binding.salt.getUnitSpinner().getSelectedItemPosition());
            }
            if (binding.sodium.getEntryName().equals(editTextView.getEntryName())) {
                binding.salt.getUnitSpinner().setSelection(binding.sodium.getUnitSpinner().getSelectedItemPosition());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            checkValueAndRelated(editTextView);
        }
    }
}
