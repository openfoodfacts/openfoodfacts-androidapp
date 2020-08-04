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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.hootsuite.nachos.validator.ChipifyingNachoValidator;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringUtils;
import org.greenrobot.greendao.async.AsyncSession;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductIngredientsBinding;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenNameDao;
import openfoodfacts.github.scrachx.openfood.network.ApiFields;
import openfoodfacts.github.scrachx.openfood.utils.EditTextUtils;
import openfoodfacts.github.scrachx.openfood.utils.FileDownloader;
import openfoodfacts.github.scrachx.openfood.utils.FileUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

import static com.hootsuite.nachos.terminator.ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.INGREDIENTS;

/**
 * Fragment for Add Product Ingredients
 *
 * @see R.layout#fragment_add_product_ingredients
 */
public class AddProductIngredientsFragment extends BaseFragment {
    private FragmentAddProductIngredientsBinding binding;
    private PhotoReceiverHandler photoReceiverHandler;
    private AllergenNameDao mAllergenNameDao;
    private Activity activity;
    private File photoFile;
    private String code;
    private List<String> allergens = new ArrayList<>();
    private OfflineSavedProduct mOfflineSavedProduct;
    private HashMap<String, String> productDetails = new HashMap<>();
    private CompositeDisposable disp = new CompositeDisposable();
    private String imagePath;
    private boolean editProduct;
    private Product product;
    private boolean newImageSelected;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddProductIngredientsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photoReceiverHandler = new PhotoReceiverHandler(newPhotoFile -> {
            final URI uri = newPhotoFile.toURI();
            imagePath = uri.getPath();
            newImageSelected = true;
            this.photoFile = newPhotoFile;
            ProductImage image = new ProductImage(code, INGREDIENTS, newPhotoFile);
            image.setFilePath(uri.getPath());
            if (activity instanceof AddProductActivity) {
                ((AddProductActivity) activity).addToPhotoMap(image, 1);
            }
            hideImageProgress(false, getString(R.string.image_uploaded_successfully));
        });
        binding.btnExtractIngredients.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_compare_arrows_black_18dp, 0, 0, 0);
        final Intent intent = getActivity() == null ? null : getActivity().getIntent();
        if (intent != null && intent.getBooleanExtra(AddProductActivity.MODIFY_NUTRITION_PROMPT, false) && !intent
            .getBooleanExtra(AddProductActivity.MODIFY_CATEGORY_PROMPT, false)) {
            ((AddProductActivity) getActivity()).proceed();
        }

        binding.btnAddImageIngredients.setOnClickListener(v -> addIngredientsImage());
        binding.btnEditImageIngredients.setOnClickListener(v -> onClickBtnEditImageIngredients());
        binding.btnNext.setOnClickListener(v -> next());
        binding.btnLooksGood.setOnClickListener(v -> ingredientsVerified());
        binding.btnSkipIngredients.setOnClickListener(v -> skipIngredients());
        binding.btnExtractIngredients.setOnClickListener(v -> onClickExtractIngredients());

        binding.ingredientsList.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Ignored
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Ignored
            }

            @Override
            public void afterTextChanged(Editable s) {
                toggleExtractIngredientsButtonVisibility();
            }
        });

        Bundle b = getArguments();
        if (b != null) {
            mAllergenNameDao = Utils.getDaoSession().getAllergenNameDao();
            product = (Product) b.getSerializable("product");
            mOfflineSavedProduct = (OfflineSavedProduct) b.getSerializable("edit_offline_product");
            editProduct = b.getBoolean(AddProductActivity.KEY_IS_EDITING);
            if (product != null) {
                code = product.getCode();
            }
            if (editProduct && product != null) {
                code = product.getCode();
                preFillProductValues();
            } else if (mOfflineSavedProduct != null) {
                code = mOfflineSavedProduct.getBarcode();
                preFillValuesForOffline();
            } else {
                //addition
                final boolean enabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("fastAdditionMode", false);
                enableFastAdditionMode(enabled);
            }
            if (b.getBoolean("perform_ocr")) {
                onClickExtractIngredients();
            }
            if (b.getBoolean("send_updated")) {
                onClickBtnEditImageIngredients();
            }
        } else {
            Toast.makeText(activity, R.string.error_adding_ingredients, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        if (EditTextUtils.isEmpty(binding.ingredientsList) && getImageIngredients() != null && !getImageIngredients().isEmpty()) {
            binding.btnExtractIngredients.setVisibility(View.VISIBLE);
            imagePath = getImageIngredients();
        } else if (editProduct && EditTextUtils.isEmpty(binding.ingredientsList) && product.getImageIngredientsUrl() != null && !product.getImageIngredientsUrl().isEmpty()) {
            binding.btnExtractIngredients.setVisibility(View.VISIBLE);
        }
        loadAutoSuggestions();
        if (getActivity() instanceof AddProductActivity && ((AddProductActivity) getActivity()).getInitialValues() != null) {
            getAllDetails(((AddProductActivity) getActivity()).getInitialValues());
        }
    }

    private String getImageIngredients() {
        return productDetails.get(ApiFields.Keys.IMAGE_INGREDIENTS);
    }

    @Nullable
    private AddProductActivity getAddProductActivity() {
        return (AddProductActivity) getActivity();
    }

    private List<String> extractTracesChipValues(Product product) {
        if (product == null || product.getTracesTags() == null) {
            return new ArrayList<>();
        }
        List<String> tracesTags = product.getTracesTags();
        final List<String> chipValues = new ArrayList<>();
        final String appLanguageCode = LocaleHelper.getLanguage(activity);
        for (String tag : tracesTags) {
            chipValues.add(getTracesName(appLanguageCode, tag));
        }
        return chipValues;
    }

    /**
     * Pre fill the fields of the product which are already present on the server.
     */
    private void preFillProductValues() {
        loadIngredientsImage();
        if (product.getIngredientsText() != null && !product.getIngredientsText().isEmpty()) {
            binding.ingredientsList.setText(product.getIngredientsText());
        }
        if (product.getTracesTags() != null && !product.getTracesTags().isEmpty()) {
            List<String> chipValues = extractTracesChipValues(product);
            binding.traces.setText(chipValues);
        }
    }

    /**
     * Load ingredients image on the image view
     */
    public void loadIngredientsImage() {
        if (getAddProductActivity() == null) {
            return;
        }
        final String newImageIngredientsUrl = product.getImageIngredientsUrl(getAddProductActivity().getProductLanguageForEdition());
        photoFile = null;
        if (newImageIngredientsUrl != null && !newImageIngredientsUrl.isEmpty()) {
            binding.imageProgress.setVisibility(View.VISIBLE);
            imagePath = newImageIngredientsUrl;
            Picasso.get()
                .load(newImageIngredientsUrl)
                .resize(dps50ToPixels(), dps50ToPixels())
                .centerInside()
                .into(binding.btnAddImageIngredients, new Callback() {
                    @Override
                    public void onSuccess() {
                        imageLoaded();
                    }

                    @Override
                    public void onError(Exception ex) {
                        imageLoaded();
                    }
                });
        }
    }

    /**
     * Set visibility parameters when image is loaded
     */
    private void imageLoaded() {
        binding.btnEditImageIngredients.setVisibility(View.VISIBLE);
        binding.imageProgress.setVisibility(View.GONE);
    }

    /**
     * returns alergen name from tag
     *
     * @param languageCode language in which additive name and tag are written
     * @param tag Tag associated with the allergen
     */
    private String getTracesName(String languageCode, String tag) {
        AllergenName allergenName = mAllergenNameDao.queryBuilder().where(AllergenNameDao.Properties.AllergenTag.eq(tag), AllergenNameDao.Properties.LanguageCode.eq(languageCode))
            .unique();
        if (allergenName != null) {
            return allergenName.getName();
        }
        return tag;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disp.dispose();
        binding = null;
    }

    /**
     * To enable fast addition mode
     */
    private void enableFastAdditionMode(boolean isEnabled) {
        if (isEnabled) {
            binding.traces.setVisibility(View.GONE);
            binding.sectionTraces.setVisibility(View.GONE);
            binding.hintTraces.setVisibility(View.GONE);
            binding.greyLine2.setVisibility(View.GONE);
        } else {
            binding.traces.setVisibility(View.VISIBLE);
            binding.sectionTraces.setVisibility(View.VISIBLE);
            binding.hintTraces.setVisibility(View.VISIBLE);
            binding.greyLine2.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Pre fill the fields if the product is already present in SavedProductOffline db.
     */
    private void preFillValuesForOffline() {
        productDetails = mOfflineSavedProduct.getProductDetailsMap();
        if (productDetails != null) {
            if (getImageIngredients() != null) {
                binding.imageProgress.setVisibility(View.VISIBLE);
                Picasso.get()
                    .load(FileUtils.LOCALE_FILE_SCHEME + getImageIngredients())
                    .resize(dps50ToPixels(), dps50ToPixels())
                    .centerInside()
                    .into(binding.btnAddImageIngredients, new Callback() {
                        @Override
                        public void onSuccess() {
                            binding.imageProgress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception ex) {
                            binding.imageProgress.setVisibility(View.GONE);
                        }
                    });
            }
            String ingredientsText = mOfflineSavedProduct.getIngredients();
            if (!TextUtils.isEmpty(ingredientsText)) {
                binding.ingredientsList.setText(ingredientsText);
            }
            if (productDetails.get(ApiFields.Keys.ADD_TRACES) != null) {
                List<String> chipValues = Arrays.asList(productDetails.get(ApiFields.Keys.ADD_TRACES).split("\\s*,\\s*"));
                binding.traces.setText(chipValues);
            }
        }
    }

    /**
     * Automatically load suggestions for allergen names
     */
    private void loadAutoSuggestions() {
        DaoSession daoSession = OFFApplication.getDaoSession();
        AsyncSession asyncSessionAllergens = daoSession.startAsyncSession();
        AllergenNameDao allergenNameDao = daoSession.getAllergenNameDao();
        final String appLanguageCode = LocaleHelper.getLanguage(activity);
        asyncSessionAllergens.queryList(allergenNameDao.queryBuilder()
            .where(AllergenNameDao.Properties.LanguageCode.eq(appLanguageCode))
            .orderDesc(AllergenNameDao.Properties.Name).build());

        asyncSessionAllergens.setListenerMainThread(operation -> {
            @SuppressWarnings("unchecked")
            List<AllergenName> allergenNames = (List<AllergenName>) operation.getResult();
            allergens.clear();
            for (AllergenName allergenName : allergenNames) {
                allergens.add(allergenName.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_dropdown_item_1line, allergens);
            binding.traces.addChipTerminator(',', BEHAVIOR_CHIPIFY_CURRENT_TOKEN);
            binding.traces.setNachoValidator(new ChipifyingNachoValidator());
            binding.traces.enableEditChipOnTouch(false, true);
            binding.traces.setAdapter(adapter);
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    private void addIngredientsImage() {
        if (imagePath != null) {
            if (photoFile != null) {
                cropRotateImage(photoFile, getString(R.string.ingredients_picture));
            } else {
                disp.add(FileDownloader.download(requireContext(), imagePath)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file -> {
                        photoFile = file;
                        cropRotateImage(photoFile, getString(R.string.ingredients_picture));
                    }));
            }
        } else {
            onClickBtnEditImageIngredients();
        }
    }

    void onClickBtnEditImageIngredients() {
        doChooseOrTakePhotos(getString(R.string.ingredients_picture));
    }

    @Override
    protected void doOnPhotosPermissionGranted() {
        onClickBtnEditImageIngredients();
    }

    void next() {
        Activity fragmentActivity = getActivity();
        if (fragmentActivity instanceof AddProductActivity) {
            ((AddProductActivity) fragmentActivity).proceed();
        }
    }

    void ingredientsVerified() {
        binding.ingredientsListVerified.setVisibility(View.VISIBLE);
        binding.traces.requestFocus();
        binding.btnLooksGood.setVisibility(View.GONE);
        binding.btnSkipIngredients.setVisibility(View.GONE);
    }

    void skipIngredients() {
        binding.ingredientsList.setText(null);
        binding.btnSkipIngredients.setVisibility(View.GONE);
        binding.btnLooksGood.setVisibility(View.GONE);
    }

    void onClickExtractIngredients() {
        if (activity instanceof AddProductActivity) {
            if (imagePath != null && (!editProduct || newImageSelected)) {
                photoFile = new File(imagePath);
                ProductImage image = new ProductImage(code, INGREDIENTS, photoFile);
                image.setFilePath(imagePath);
                ((AddProductActivity) activity).addToPhotoMap(image, 1);
            } else if (imagePath != null) {
                ((AddProductActivity) activity).performOCR(code, "ingredients_" + ((AddProductActivity) activity).getProductLanguageForEdition());
            }
        }
    }

    private void toggleExtractIngredientsButtonVisibility() {
        if (EditTextUtils.isEmpty(binding.ingredientsList)) {
            binding.btnExtractIngredients.setVisibility(View.VISIBLE);
        } else {
            binding.btnExtractIngredients.setVisibility(View.GONE);
        }
    }

    /**
     * adds all the fields to the query map even those which are null or empty.
     */
    public void getAllDetails(Map<String, String> targetMap) {
        binding.traces.chipifyAllUnterminatedTokens();
        if (activity instanceof AddProductActivity) {
            String languageCode = ((AddProductActivity) activity).getProductLanguageForEdition();
            String lc = (!languageCode.isEmpty()) ? languageCode : "en";
            targetMap.put(ApiFields.Keys.lcIngredientsKey(lc), binding.ingredientsList.getText().toString());
            List<String> list = binding.traces.getChipValues();
            String string = StringUtils.join(list, ",");
            targetMap.put(ApiFields.Keys.ADD_TRACES.substring(4), string);
        }
    }

    /**
     * adds only those fields to the query map which are not empty and have changed.
     */
    public void addUpdatedFieldsTomap(Map<String, String> targetMap) {
        binding.traces.chipifyAllUnterminatedTokens();
        if (!(activity instanceof AddProductActivity)) {
            return;
        }
        if (EditTextUtils.isNotEmpty(binding.ingredientsList) && EditTextUtils.isDifferent(binding.ingredientsList, product != null ? product.getIngredientsText() : null)) {
            String languageCode = ((AddProductActivity) activity).getProductLanguageForEdition();
            String lc = (!languageCode.isEmpty()) ? languageCode : "en";
            targetMap.put(ApiFields.Keys.lcIngredientsKey(lc), binding.ingredientsList.getText().toString());
        }
        if (!binding.traces.getChipValues().isEmpty() && EditTextUtils.areChipsDifferent(binding.traces, extractTracesChipValues(product))) {
            String string = StringUtils.join(binding.traces.getChipValues(), ",");
            targetMap.put(ApiFields.Keys.ADD_TRACES, string);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data);
    }

    /**
     * Displays progress bar and hides other views util image is still loading
     */
    public void showImageProgress() {
        if (!isAdded()) {
            return;
        }
        binding.imageProgress.setVisibility(View.VISIBLE);
        binding.imageProgressText.setVisibility(View.VISIBLE);
        binding.imageProgressText.setText(R.string.toastSending);
        binding.btnAddImageIngredients.setVisibility(View.INVISIBLE);
        binding.btnEditImageIngredients.setVisibility(View.INVISIBLE);
    }

    /**
     * After image is loaded hide image progress
     *
     * @param errorInUploading boolean variable is true, if there is an error while showing image
     * @param message error message in case of failure to display image
     */
    public void hideImageProgress(boolean errorInUploading, String message) {
        if (!isAdded()) {
            return;
        }
        binding.imageProgress.setVisibility(View.INVISIBLE);
        binding.imageProgressText.setVisibility(View.GONE);
        binding.btnAddImageIngredients.setVisibility(View.VISIBLE);
        binding.btnEditImageIngredients.setVisibility(View.VISIBLE);
        if (!errorInUploading) {
            Picasso.get()
                .load(photoFile)
                .resize(dps50ToPixels(), dps50ToPixels())
                .centerInside()
                .into(binding.btnAddImageIngredients);
        }
    }

    /**
     * Display the list of ingredients based on the result from ocr of IngredientsList photo
     *
     * @param status status of ocr, in case of proper OCR it returns "set" or "0"
     * @param ocrResult resultant string obtained after OCR of image
     */
    public void setIngredients(String status, String ocrResult) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            switch (status) {
                case "set":
                    binding.ingredientsList.setText(ocrResult);
                    loadIngredientsImage();
                    break;
                case "0":
                    binding.ingredientsList.setText(ocrResult);
                    binding.btnLooksGood.setVisibility(View.VISIBLE);
                    binding.btnSkipIngredients.setVisibility(View.VISIBLE);
                    break;
                default:
                    Toast.makeText(activity, R.string.unable_to_extract_ingredients, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public void showOCRProgress() {
        binding.btnExtractIngredients.setVisibility(View.GONE);
        binding.ingredientsList.setText(null);
        binding.ocrProgress.setVisibility(View.VISIBLE);
        binding.ocrProgressText.setVisibility(View.VISIBLE);
    }

    public void hideOCRProgress() {
        binding.ocrProgress.setVisibility(View.GONE);
        binding.ocrProgressText.setVisibility(View.GONE);
    }

    private int dps50ToPixels() {
        return dpsToPixels(50);
    }
}
