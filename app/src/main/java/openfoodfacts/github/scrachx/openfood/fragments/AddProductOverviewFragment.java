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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.validator.ChipifyingNachoValidator;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.lang.StringUtils;
import org.greenrobot.greendao.async.AsyncSession;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.AppFlavors;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductOverviewBinding;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductState;
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryNameDao;
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryName;
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameDao;
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelName;
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameDao;
import openfoodfacts.github.scrachx.openfood.models.entities.tag.Tag;
import openfoodfacts.github.scrachx.openfood.models.entities.tag.TagDao;
import openfoodfacts.github.scrachx.openfood.network.ApiFields;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI;
import openfoodfacts.github.scrachx.openfood.utils.EditTextUtils;
import openfoodfacts.github.scrachx.openfood.utils.FileDownloader;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.adapters.autocomplete.EmbCodeAutoCompleteAdapter;
import openfoodfacts.github.scrachx.openfood.views.adapters.autocomplete.PeriodAfterOpeningAutoCompleteAdapter;

import static com.hootsuite.nachos.terminator.ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.OTHER;

/**
 * Product Overview fragment of AddProductActivity
 */
public class AddProductOverviewFragment extends BaseFragment {
    private static final int INTENT_INTEGRATOR_REQUEST_CODE = 1;
    private Activity activity;
    private FragmentAddProductOverviewBinding binding;
    private String appLanguageCode;
    private List<String> categories = new ArrayList<>();
    private String barcode;
    private List<String> countries = new ArrayList<>();
    private boolean editionMode;
    private boolean frontImage;
    private List<String> labels = new ArrayList<>();
    private String languageCode;
    private CategoryNameDao mCategoryNameDao;
    private CountryNameDao mCountryNameDao;
    private String mImageUrl;
    private LabelNameDao mLabelNameDao;
    private OfflineSavedProduct mOfflineSavedProduct;
    private TagDao mTagDao;
    private CompositeDisposable disp = new CompositeDisposable();
    private File photoFile;
    private PhotoReceiverHandler photoReceiverHandler;
    private Product product;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddProductOverviewBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disp.dispose();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTagDao = Utils.getDaoSession().getTagDao();
        mCategoryNameDao = Utils.getDaoSession().getCategoryNameDao();
        mLabelNameDao = Utils.getDaoSession().getLabelNameDao();
        mCountryNameDao = Utils.getDaoSession().getCountryNameDao();

        photoReceiverHandler = new PhotoReceiverHandler(newPhotoFile -> {
            URI resultUri = newPhotoFile.toURI();
            this.photoFile = newPhotoFile;
            ProductImage image;
            int position;
            if (frontImage) {
                image = new ProductImage(barcode, FRONT, newPhotoFile);
                mImageUrl = newPhotoFile.getAbsolutePath();
                position = 0;
            } else {
                image = new ProductImage(barcode, OTHER, newPhotoFile);
                position = 3;
            }
            image.setFilePath(resultUri.getPath());
            if (activity instanceof AddProductActivity) {
                ((AddProductActivity) activity).addToPhotoMap(image, position);
            }
            hideImageProgress(false, StringUtils.EMPTY);
        });
        binding.btnOtherPictures.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_a_photo_blue_18dp, 0, 0, 0);

        binding.btnNext.setOnClickListener(v -> next());
        binding.btnAddImageFront.setOnClickListener(v -> addFrontImage());
        binding.btnEditImageFront.setOnClickListener(v -> newFrontImage());
        binding.btnOtherPictures.setOnClickListener(v -> addOtherImage());
        binding.sectionManufacturingDetails.setOnClickListener(v -> toggleManufacturingSectionVisibility());
        binding.sectionPurchasingDetails.setOnClickListener(v -> togglePurchasingSectionVisibility());
        binding.hintEmbCode.setOnClickListener(v -> toastEmbCodeHint());
        binding.hintLink.setOnClickListener(v -> searchProductLink());
        binding.hintLink2.setOnClickListener(v -> scanProductLink());
        binding.language.setOnClickListener(v -> selectProductLanguage());

        //checks the information about the prompt clicked and takes action accordingly
        if (requireActivity().getIntent().getBooleanExtra(AddProductActivity.MODIFY_CATEGORY_PROMPT, false)) {
            binding.categories.requestFocus();
        } else if (requireActivity().getIntent().getBooleanExtra(AddProductActivity.MODIFY_NUTRITION_PROMPT, false)) {
            ((AddProductActivity) requireActivity()).proceed();
        }
        appLanguageCode = LocaleHelper.getLanguage(activity);
        Bundle b = getArguments();
        if (b != null) {
            product = (Product) b.getSerializable("product");
            mOfflineSavedProduct = (OfflineSavedProduct) b.getSerializable("edit_offline_product");
            editionMode = b.getBoolean(AddProductActivity.KEY_IS_EDITING);
            binding.barcode.setText(R.string.txtBarcode);
            binding.language.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0);
            binding.sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
            binding.sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
            if (product != null) {
                barcode = product.getCode();
            }
            if (editionMode && product != null) {
                barcode = product.getCode();
                String languageToUse = product.getLang();
                if (product.isLanguageSupported(appLanguageCode)) {
                    languageToUse = appLanguageCode;
                }
                preFillProductValues(languageToUse);
            } else if (mOfflineSavedProduct != null) {
                barcode = mOfflineSavedProduct.getBarcode();
                preFillValuesFromOffline();
            } else {
                //addition
                final boolean fastAdditionMode = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("fastAdditionMode", false);
                enableFastAdditionMode(fastAdditionMode);
            }

            binding.barcode.append(" ");
            binding.barcode.append(barcode);
            if (BuildConfig.FLAVOR.equals("obf") || BuildConfig.FLAVOR.equals("opf")) {
                binding.btnOtherPictures.setVisibility(View.GONE);
            }
            if (b.getBoolean("perform_ocr")) {
                ((AddProductActivity) activity).proceed();
            }
            if (b.getBoolean("send_updated")) {
                ((AddProductActivity) activity).proceed();
            }
        } else {
            Toast.makeText(activity, R.string.error_adding_product_details, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        initializeChips();
        loadAutoSuggestions();
        if (getActivity() instanceof AddProductActivity && ((AddProductActivity) getActivity()).getInitialValues() != null) {
            getAllDetails(((AddProductActivity) getActivity()).getInitialValues());
        }
        if (StringUtils.isBlank(languageCode)) {
            setProductLanguage(appLanguageCode);
        }
    }

    /**
     * To enable fast addition mode
     *
     * @param enable
     */
    private void enableFastAdditionMode(boolean enable) {
        int visibility = View.VISIBLE;
        if (enable) {
            visibility = View.GONE;
        }
        binding.sectionManufacturingDetails.setVisibility(visibility);
        binding.sectionPurchasingDetails.setVisibility(visibility);
        binding.packaging.setVisibility(visibility);
        binding.label.setVisibility(visibility);
        binding.periodOfTimeAfterOpeningTil.setVisibility(visibility);
        changeVisibilityManufacturingSectionTo(visibility);
        changePurchasingSectionVisibilityTo(visibility);
        binding.greyLine2.setVisibility(visibility);
        binding.greyLine3.setVisibility(visibility);
        binding.greyLine4.setVisibility(visibility);
    }

    /**
     * Pre fill the fields of the product which are already present on the server.
     */
    private void preFillProductValues(String lang) {
        if (product.getProductName() != null && !product.getProductName().isEmpty()) {
            binding.name.setText(product.getProductName());
        }
        if (product.getQuantity() != null && !product.getQuantity().isEmpty()) {
            binding.quantity.setText(product.getQuantity());
        }
        if (product.getBrands() != null && !product.getBrands().isEmpty()) {
            binding.brand.setText(extractProductBrandsChipsValues(product));
        }
        if (product.getPackaging() != null && !product.getPackaging().isEmpty()) {
            binding.packaging.setText(extractProductPackagingChipsValues(product));
        }
        if (product.getCategoriesTags() != null && !product.getCategoriesTags().isEmpty()) {
            binding.categories.setText(extractProductCategoriesChipsValues(product));
        }
        if (product.getLabelsTags() != null && !product.getLabelsTags().isEmpty()) {
            binding.label.setText(extractProductTagsChipsValues(product));
        }
        if (product.getOrigins() != null && !product.getOrigins().isEmpty()) {
            binding.originOfIngredients.setText(extractProductOriginsChipsValues(product));
        }
        if (product.getManufacturingPlaces() != null && !product.getManufacturingPlaces().isEmpty()) {
            binding.manufacturingPlace.setText(product.getManufacturingPlaces());
        }
        if (product.getEmbTags() != null && !product.getEmbTags().toString().trim().equals("[]")) {
            binding.embCode.setText(extractProductEmbTagsChipsValues(product));
        }
        if (product.getManufactureUrl() != null && !product.getManufactureUrl().isEmpty()) {
            binding.link.setText(product.getManufactureUrl());
        }
        if (product.getPurchasePlaces() != null && !product.getPurchasePlaces().isEmpty()) {
            binding.countryWherePurchased.setText(extractProductPurchasePlaces(product));
        }
        if (product.getStores() != null && !product.getStores().isEmpty()) {
            binding.stores.setText(extractProductStoresChipValues(product));
        }
        if (product.getCountriesTags() != null && !product.getCountriesTags().isEmpty()) {
            List<String> chipValues = extractProductCountriesTagsChipValues(product);
            //Also add the country set by the user in preferences
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            String savedCountry = sharedPref.getString(LocaleHelper.USER_COUNTRY_PREFERENCE_KEY, "");
            if (!savedCountry.isEmpty()) {
                chipValues.add(savedCountry);
            }
            binding.countriesWhereSold.setText(chipValues);
        }
        setProductLanguage(lang);
    }

    private List<String> extractProductCountriesTagsChipValues(Product product) {
        if (product == null || product.getCountriesTags() == null) {
            return new ArrayList<>();
        }
        List<String> countriesTags = product.getCountriesTags();
        final List<String> chipValues = new ArrayList<>();
        for (String tag : countriesTags) {
            chipValues.add(getCountryName(appLanguageCode, tag));
        }
        return chipValues;
    }

    private List<String> extractProductStoresChipValues(Product product) {
        if (product == null || product.getStores() == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(product.getStores().split("\\s*,\\s*"));
    }

    private List<String> extractProductPurchasePlaces(Product product) {
        if (product == null || product.getPurchasePlaces() == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(product.getPurchasePlaces().split("\\s*,\\s*"));
    }

    private List<String> extractProductEmbTagsChipsValues(Product product) {
        if (product == null || product.getEmbTags() == null) {
            return new ArrayList<>();
        }
        String[] embTags = product.getEmbTags().toString().replace("[", "").replace("]", "").split(", ");
        final List<String> chipValues = new ArrayList<>();
        for (String embTag : embTags) {
            chipValues.add(getEmbCode(embTag));
        }
        return chipValues;
    }

    private List<String> extractProductOriginsChipsValues(Product product) {
        if (product == null || product.getOrigins() == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(product.getOrigins().split("\\s*,\\s*"));
    }

    private List<String> extractProductTagsChipsValues(Product product) {
        if (product == null || product.getLabelsTags() == null) {
            return new ArrayList<>();
        }
        List<String> labelsTags = product.getLabelsTags();
        final List<String> chipValues = new ArrayList<>();
        for (String tag : labelsTags) {
            chipValues.add(getLabelName(appLanguageCode, tag));
        }
        return chipValues;
    }

    private List<String> extractProductCategoriesChipsValues(Product product) {
        if (product == null || product.getCategoriesTags() == null) {
            return new ArrayList<>();
        }
        List<String> categoriesTags = product.getCategoriesTags();
        final List<String> chipValues = new ArrayList<>();
        for (String tag : categoriesTags) {
            chipValues.add(getCategoryName(appLanguageCode, tag));
        }
        return chipValues;
    }

    private List<String> extractProductPackagingChipsValues(Product product) {
        if (product == null || product.getPackaging() == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(product.getPackaging().split("\\s*,\\s*"));
    }

    private List<String> extractProductBrandsChipsValues(Product product) {
        if (product == null || product.getBrands() == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(product.getBrands().split("\\s*,\\s*"));
    }

    /**
     * Loads front image of the product into the imageview
     *
     * @param language language used for adding product
     */
    private void loadFrontImage(String language) {
        photoFile = null;
        final String imageFrontUrl = product.getImageFrontUrl(language);
        if (imageFrontUrl != null && !imageFrontUrl.isEmpty()) {

            mImageUrl = imageFrontUrl;
            binding.imageProgress.setVisibility(View.VISIBLE);
            binding.btnEditImageFront.setVisibility(View.INVISIBLE);
            Picasso.get()
                .load(imageFrontUrl)
                .resize(dpsToPixels(50), dpsToPixels(50))
                .centerInside()
                .into(binding.btnAddImageFront, new Callback() {
                    @Override
                    public void onSuccess() {
                        frontImageLoaded();
                    }

                    @Override
                    public void onError(Exception ex) {
                        frontImageLoaded();
                    }
                });
        }
    }

    /**
     * @param languageCode 2 letter language code. example hi, en etc.
     * @param tag the complete tag. example en:india
     * @return returns the name of the country if found in the db or else returns the tag itself.
     */
    private String getCountryName(String languageCode, String tag) {
        CountryName countryName = mCountryNameDao.queryBuilder().where(CountryNameDao.Properties.CountyTag.eq(tag), CountryNameDao.Properties.LanguageCode.eq(languageCode))
            .unique();
        if (countryName != null) {
            return countryName.getName();
        }
        return tag;
    }

    /**
     * @param languageCode 2 letter language code. example de, en etc.
     * @param tag the complete tag. example de:hoher-omega-3-gehalt
     * @return returns the name of the label if found in the db or else returns the tag itself.
     */
    private String getLabelName(String languageCode, String tag) {
        LabelName labelName = mLabelNameDao.queryBuilder().where(LabelNameDao.Properties.LabelTag.eq(tag), LabelNameDao.Properties.LanguageCode.eq(languageCode)).unique();
        if (labelName != null) {
            return labelName.getName();
        }
        return tag;
    }

    /**
     * @param languageCode 2 letter language code. example en, fr etc.
     * @param tag the complete tag. example en:plant-based-foods-and-beverages
     * @return returns the name of the category (example Plant-based foods and beverages) if found in the db or else returns the tag itself.
     */
    private String getCategoryName(String languageCode, String tag) {
        CategoryName categoryName = mCategoryNameDao.queryBuilder().where(CategoryNameDao.Properties.CategoryTag.eq(tag), CategoryNameDao.Properties.LanguageCode.eq(languageCode))
            .unique();
        if (categoryName != null) {
            return categoryName.getName();
        }
        return tag;
    }

    private String getEmbCode(String embTag) {
        Tag tag = mTagDao.queryBuilder().where(TagDao.Properties.Id.eq(embTag)).unique();
        if (tag != null) {
            return tag.getName();
        }
        return embTag;
    }

    /**
     * Pre fill the fields if the product is already present in SavedProductOffline db.
     */
    private void preFillValuesFromOffline() {
        HashMap<String, String> productDetails = mOfflineSavedProduct.getProductDetailsMap();
        if (productDetails != null) {
            if (mOfflineSavedProduct.getImageFrontLocalUrl() != null) {
                binding.imageProgress.setVisibility(View.VISIBLE);
                binding.btnEditImageFront.setVisibility(View.INVISIBLE);
                mImageUrl = mOfflineSavedProduct.getImageFrontLocalUrl();
                Picasso.get()
                    .load(mImageUrl)
                    .resize(dpsToPixels(50), dpsToPixels(50))
                    .centerInside()
                    .into(binding.btnAddImageFront, new Callback() {
                        @Override
                        public void onSuccess() {
                            frontImageLoaded();
                        }

                        @Override
                        public void onError(Exception ex) {
                            frontImageLoaded();
                        }
                    });
            }
            String offLineProductLanguage = mOfflineSavedProduct.getLanguage();
            if (!TextUtils.isEmpty(offLineProductLanguage)) {
                setProductLanguage(offLineProductLanguage);
            }
            String offlineProductName = mOfflineSavedProduct.getName();
            if (!TextUtils.isEmpty(offlineProductName)) {
                binding.name.setText(offlineProductName);
            }
            if (productDetails.get(ApiFields.Keys.QUANTITY) != null) {
                binding.quantity.setText(productDetails.get(ApiFields.Keys.QUANTITY));
            }
            prefillChip(productDetails, ApiFields.Keys.BRANDS, binding.brand);
            prefillChip(productDetails, ApiFields.Keys.PACKAGING, binding.packaging);
            prefillChip(productDetails, ApiFields.Keys.CATEGORIES, binding.categories);
            prefillChip(productDetails, ApiFields.Keys.LABELS, binding.label);
            prefillChip(productDetails, ApiFields.Keys.ORIGINS, binding.originOfIngredients);
            if (productDetails.get(ApiFields.Keys.MANUFACTURING_PLACES) != null) {
                binding.manufacturingPlace.setText(productDetails.get(ApiFields.Keys.MANUFACTURING_PLACES));
            }
            prefillChip(productDetails, ApiFields.Keys.EMB_CODES, binding.embCode);
            if (productDetails.get(ApiFields.Keys.LINK) != null) {
                binding.link.setText(productDetails.get(ApiFields.Keys.LINK));
            }
            prefillChip(productDetails, ApiFields.Keys.ADD_PURCHASE, binding.countryWherePurchased);
            prefillChip(productDetails, ApiFields.Keys.ADD_STORES, binding.stores);
            prefillChip(productDetails, ApiFields.Keys.ADD_COUNTRIES, binding.countriesWhereSold);
        }
    }

    private void frontImageLoaded() {
        binding.btnEditImageFront.setVisibility(View.VISIBLE);
        binding.imageProgress.setVisibility(View.GONE);
    }

    private void prefillChip(@NonNull Map<String, String> productDetails, @NonNull String paramName, @NonNull NachoTextView nachoTextView) {
        if (productDetails.get(paramName) != null) {
            List<String> chipValues = Arrays.asList(productDetails.get(paramName).split("\\s*,\\s*"));
            nachoTextView.setText(chipValues);
        }
    }

    private void initializeChips() {
        NachoTextView[] nachoTextViews = {binding.brand, binding.packaging, binding.categories, binding.label, binding.originOfIngredients, binding.embCode, binding.countryWherePurchased, binding.stores, binding.countriesWhereSold};
        for (NachoTextView nachoTextView : nachoTextViews) {
            nachoTextView.addChipTerminator(',', BEHAVIOR_CHIPIFY_CURRENT_TOKEN);
            nachoTextView.setNachoValidator(new ChipifyingNachoValidator());
            nachoTextView.enableEditChipOnTouch(false, true);
        }
    }

    /**
     * Auto load suggestions into various NachoTextViews
     */
    @SuppressWarnings("unchecked")
    private void loadAutoSuggestions() {
        DaoSession daoSession = OFFApplication.getDaoSession();
        AsyncSession asyncSessionCountries = daoSession.startAsyncSession();
        AsyncSession asyncSessionLabels = daoSession.startAsyncSession();
        AsyncSession asyncSessionCategories = daoSession.startAsyncSession();
        LabelNameDao labelNameDao = daoSession.getLabelNameDao();
        CountryNameDao countryNameDao = daoSession.getCountryNameDao();
        CategoryNameDao categoryNameDao = daoSession.getCategoryNameDao();

        asyncSessionCountries.queryList(countryNameDao.queryBuilder()
            .where(CountryNameDao.Properties.LanguageCode.eq(appLanguageCode))
            .orderDesc(CountryNameDao.Properties.Name).build());
        asyncSessionLabels.queryList(labelNameDao.queryBuilder()
            .where(LabelNameDao.Properties.LanguageCode.eq(appLanguageCode))
            .orderDesc(LabelNameDao.Properties.Name).build());
        asyncSessionCategories.queryList(categoryNameDao.queryBuilder()
            .where(CategoryNameDao.Properties.LanguageCode.eq(appLanguageCode))
            .orderDesc(CategoryNameDao.Properties.Name).build());

        asyncSessionCountries.setListenerMainThread(operation -> {
            countries.clear();
            for (CountryName name : (List<CountryName>) operation.getResult()) {
                countries.add(name.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_dropdown_item_1line, countries);
            EmbCodeAutoCompleteAdapter embAdapter = new EmbCodeAutoCompleteAdapter(activity, android.R.layout.simple_dropdown_item_1line);
            binding.originOfIngredients.setAdapter(adapter);
            binding.countryWherePurchased.setAdapter(adapter);
            binding.countriesWhereSold.setAdapter(adapter);
            binding.embCode.setAdapter(embAdapter);
        });
        asyncSessionLabels.setListenerMainThread(operation -> {
            labels.clear();
            for (LabelName name : (List<LabelName>) operation.getResult()) {
                labels.add(name.getName());
            }
            binding.label.setAdapter(new ArrayAdapter<>(activity,
                android.R.layout.simple_dropdown_item_1line, labels));
        });
        asyncSessionCategories.setListenerMainThread(operation -> {
            categories.clear();
            for (CategoryName name : (List<CategoryName>) operation.getResult()) {
                categories.add(name.getName());
            }
            binding.categories.setAdapter(new ArrayAdapter<>(activity,
                android.R.layout.simple_dropdown_item_1line, categories));
        });
        if (AppFlavors.isFlavors(AppFlavors.OBF)) {
            binding.periodOfTimeAfterOpeningTil.setVisibility(View.VISIBLE);
            PeriodAfterOpeningAutoCompleteAdapter customAdapter = new PeriodAfterOpeningAutoCompleteAdapter(activity, android.R.layout.simple_dropdown_item_1line);
            binding.periodOfTimeAfterOpening.setAdapter(customAdapter);
        }
    }

    /**
     * Set language of the product to the language entered
     *
     * @param lang language code
     */
    private void setProductLanguage(String lang) {
        languageCode = lang;
        Locale current = LocaleHelper.getLocale(lang);
        binding.language.setText(R.string.product_language);
        binding.language.append(StringUtils.capitalize(current.getDisplayName(current)));
        if (activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).setProductLanguage(languageCode);
        }
        if (editionMode) {
            loadFrontImage(lang);
            ProductsAPI client = CommonApiManager.getInstance().getProductsApi();
            String fields = "ingredients_text_" + lang + ",product_name_" + lang;
            client.getProductByBarcodeSingle(product.getCode(), fields, Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ProductState>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        binding.name.setText(getString(R.string.txtLoading));
                    }

                    @Override
                    public void onSuccess(ProductState productState) {
                        if (productState.getStatus() == 1) {
                            if (productState.getProduct().getProductName(lang) != null) {
                                if (languageCode.equals(lang)) {
                                    binding.name.setText(productState.getProduct().getProductName(lang));
                                    if (activity instanceof AddProductActivity) {
                                        ((AddProductActivity) activity).setIngredients("set", productState.getProduct().getIngredientsText(lang));
                                        ((AddProductActivity) activity).updateLanguage();
                                    }
                                }
                            } else {
                                binding.name.setText(null);
                                if (activity instanceof AddProductActivity) {
                                    ((AddProductActivity) activity).setIngredients("set", null);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        binding.name.setText(null);
                    }
                });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    void next() {
        if (!areRequiredFieldsEmpty() && activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).proceed();
        }
    }

    void addFrontImage() {
        if (mImageUrl != null) {
            frontImage = true;
            if (photoFile != null) {
                cropRotateImage(photoFile, getString(R.string.set_img_front));
            } else {
                disp.add(FileDownloader.download(requireContext(), mImageUrl)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file -> {
                        photoFile = file;
                        cropRotateImage(photoFile, getString(R.string.set_img_front));
                    }));
            }
        } else {
            newFrontImage();
        }
    }

    void newFrontImage() {
        // add front image.
        frontImage = true;
        doChooseOrTakePhotos(getString(R.string.set_img_front));
    }

    void addOtherImage() {
        frontImage = false;
        doChooseOrTakePhotos(getString(R.string.take_more_pictures));
    }

    @Override
    protected void doOnPhotosPermissionGranted() {
        if (frontImage) {
            addOtherImage();
        } else {
            addFrontImage();
        }
    }

    /**
     * adds all the fields to the query map even those which are null or empty.
     */
    public void getAllDetails(Map<String, String> targetMap) {
        chipifyAllUnterminatedTokens();
        if (!(activity instanceof AddProductActivity)) {
            return;
        }
        targetMap.put(ApiFields.Keys.BARCODE, barcode);
        targetMap.put(ApiFields.Keys.LANG, languageCode);
        targetMap.put(ApiFields.Keys.LC, appLanguageCode);
        String lc = (!languageCode.isEmpty()) ? languageCode : "en";
        targetMap.put(ApiFields.Keys.lcProductNameKey(lc), binding.name.getText().toString());
        targetMap.put(ApiFields.Keys.QUANTITY, binding.quantity.getText().toString());
        targetMap.put(ApiFields.Keys.BRANDS, getValues(binding.brand));
        targetMap.put(ApiFields.Keys.PACKAGING, getValues(binding.packaging));
        targetMap.put(ApiFields.Keys.CATEGORIES, getValues(binding.categories));
        targetMap.put(ApiFields.Keys.LABELS, getValues(binding.label));
        if (AppFlavors.isFlavors(AppFlavors.OBF)) {
            targetMap.put(ApiFields.Keys.PERIODS_AFTER_OPENING, binding.periodOfTimeAfterOpening.getText().toString());
        }
        if (mImageUrl != null) {
            targetMap.put("imageUrl", mImageUrl);
        }
        targetMap.put(ApiFields.Keys.ORIGINS, getValues(binding.originOfIngredients));
        targetMap.put(ApiFields.Keys.MANUFACTURING_PLACES, binding.manufacturingPlace.getText().toString());
        targetMap.put(ApiFields.Keys.EMB_CODES, getValues(binding.embCode));
        targetMap.put(ApiFields.Keys.LINK, binding.link.getText().toString());
        targetMap.put(ApiFields.Keys.PURCHASE_PLACES, getValues(binding.countryWherePurchased));
        targetMap.put(ApiFields.Keys.STORES, getValues(binding.stores));
        targetMap.put(ApiFields.Keys.COUNTRIES, getValues(binding.countriesWhereSold));
    }

    /**
     * adds only those fields to the query map which have changed.
     */
    public void addUpdatedFieldsToMap(Map<String, String> targetMap) {
        chipifyAllUnterminatedTokens();
        // Check for activity
        if (!(activity instanceof AddProductActivity)) {
            return;
        }
        if (!TextUtils.isEmpty(barcode)) {
            targetMap.put(ApiFields.Keys.BARCODE, barcode);
        }
        if (!TextUtils.isEmpty(appLanguageCode)) {
            targetMap.put(ApiFields.Keys.LC, appLanguageCode);
        }
        if (!TextUtils.isEmpty(languageCode)) {
            targetMap.put(ApiFields.Keys.LANG, languageCode);
        }
        String lc = (!TextUtils.isEmpty(languageCode)) ? languageCode : "en";
        if (EditTextUtils.isNotEmpty(binding.name) && EditTextUtils.isDifferent(binding.name, product != null ? product.getProductName(lc) : null)) {
            targetMap.put(ApiFields.Keys.lcProductNameKey(lc), binding.name.getText().toString());
        }
        if (EditTextUtils.isNotEmpty(binding.quantity) && EditTextUtils.isDifferent(binding.quantity, product != null ? product.getQuantity() : null)) {
            targetMap.put(ApiFields.Keys.QUANTITY, binding.quantity.getText().toString());
        }
        if (EditTextUtils.areChipsDifferent(binding.brand, extractProductBrandsChipsValues(product))) {
            targetMap.put(ApiFields.Keys.BRANDS, getValues(binding.brand));
        }
        if (EditTextUtils.areChipsDifferent(binding.packaging, extractProductPackagingChipsValues(product))) {
            targetMap.put(ApiFields.Keys.PACKAGING, getValues(binding.packaging));
        }
        if (EditTextUtils.areChipsDifferent(binding.categories, extractProductCategoriesChipsValues(product))) {
            targetMap.put(ApiFields.Keys.CATEGORIES, getValues(binding.categories));
        }
        if (EditTextUtils.areChipsDifferent(binding.label, extractProductTagsChipsValues(product))) {
            targetMap.put(ApiFields.Keys.LABELS, getValues(binding.label));
        }
        if (EditTextUtils.isNotEmpty(binding.periodOfTimeAfterOpening)) {
            targetMap.put(ApiFields.Keys.PERIODS_AFTER_OPENING, binding.periodOfTimeAfterOpening.getText().toString());
        }
        if (mImageUrl != null) {
            targetMap.put("imageUrl", mImageUrl);
        }
        if (EditTextUtils.areChipsDifferent(binding.originOfIngredients, extractProductOriginsChipsValues(product))) {
            targetMap.put(ApiFields.Keys.ORIGINS, getValues(binding.originOfIngredients));
        }
        if (EditTextUtils.isNotEmpty(binding.manufacturingPlace) && EditTextUtils
            .isDifferent(binding.manufacturingPlace, product != null ? product.getManufacturingPlaces() : null)) {
            targetMap.put(ApiFields.Keys.MANUFACTURING_PLACES, binding.manufacturingPlace.getText().toString());
        }
        if (EditTextUtils.areChipsDifferent(binding.embCode, extractProductEmbTagsChipsValues(product))) {
            targetMap.put(ApiFields.Keys.EMB_CODES, getValues(binding.embCode));
        }
        if (EditTextUtils.isNotEmpty(binding.link) && EditTextUtils.isDifferent(binding.link, product != null ? product.getManufactureUrl() : null)) {
            targetMap.put(ApiFields.Keys.LINK, binding.link.getText().toString());
        }
        if (EditTextUtils.areChipsDifferent(binding.countryWherePurchased, extractProductPurchasePlaces(product))) {
            targetMap.put(ApiFields.Keys.PURCHASE_PLACES, getValues(binding.countryWherePurchased));
        }
        if (EditTextUtils.areChipsDifferent(binding.stores, extractProductStoresChipValues(product))) {
            targetMap.put(ApiFields.Keys.STORES, getValues(binding.stores));
        }
        if (EditTextUtils.areChipsDifferent(binding.countriesWhereSold,
            extractProductCountriesTagsChipValues(product))) {
            targetMap.put(ApiFields.Keys.COUNTRIES, getValues(binding.countriesWhereSold));
        }
    }

    /**
     * Chipifies all existing plain text in all the NachoTextViews.
     */
    private void chipifyAllUnterminatedTokens() {
        NachoTextView[] nachoTextViews = {binding.brand, binding.packaging, binding.categories, binding.label, binding.originOfIngredients, binding.embCode, binding.countryWherePurchased, binding.stores, binding.countriesWhereSold};
        for (NachoTextView nachoTextView : nachoTextViews) {
            if (nachoTextView != null) {
                nachoTextView.chipifyAllUnterminatedTokens();
            }
        }
    }

    private String getValues(NachoTextView nachoTextView) {
        List<String> list = nachoTextView.getChipValues();
        return StringUtils.join(list, ",");
    }

    private void toggleManufacturingSectionVisibility() {
        if (binding.manufacturingPlaceTil.getVisibility() != View.VISIBLE) {
            changeVisibilityManufacturingSectionTo(View.VISIBLE);
            binding.originOfIngredients.requestFocus();
            binding.sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up_grey_24dp, 0);
        } else {
            changeVisibilityManufacturingSectionTo(View.GONE);
            binding.sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
        }
    }

    private void changeVisibilityManufacturingSectionTo(int visibility) {
        binding.originOfIngredientsTil.setVisibility(visibility);
        binding.manufacturingPlaceTil.setVisibility(visibility);
        binding.embCodeTil.setVisibility(visibility);
        binding.linkTil.setVisibility(visibility);
        binding.hintLink.setVisibility(visibility);
        binding.hintLink2.setVisibility(visibility);
    }

    private void togglePurchasingSectionVisibility() {
        if (binding.storesTil.getVisibility() != View.VISIBLE) {
            changePurchasingSectionVisibilityTo(View.VISIBLE);
            binding.countryWherePurchased.requestFocus();
            binding.sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up_grey_24dp, 0);
        } else {
            changePurchasingSectionVisibilityTo(View.GONE);
            binding.sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
        }
    }

    private void changePurchasingSectionVisibilityTo(int visibility) {
        binding.countryWherePurchasedTil.setVisibility(visibility);
        binding.storesTil.setVisibility(visibility);
        binding.countriesWhereSoldTil.setVisibility(visibility);
    }

    private void toastEmbCodeHint() {
        new MaterialDialog.Builder(activity)
            .content(R.string.hint_emb_codes)
            .positiveText(R.string.ok_button)
            .show();
    }

    private void searchProductLink() {
        String url = "https://www.google.com/search?q=" + barcode;
        if (!binding.brand.getChipAndTokenValues().isEmpty()) {
            List<String> brandNames = binding.brand.getChipAndTokenValues();
            url = url + " " + StringUtils.join(brandNames, " ");
        }
        if (EditTextUtils.isNotEmpty(binding.name)) {
            url = url + " " + EditTextUtils.getContent(binding.name);
        }
        url = url + " " + getString(R.string.official_website);
        CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(activity.getBaseContext(), null);
        CustomTabActivityHelper.openCustomTab(activity, customTabsIntent, Uri.parse(url), new WebViewFallback());
    }

    private void scanProductLink() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setRequestCode(INTENT_INTEGRATOR_REQUEST_CODE);
        integrator.setPrompt(getString(R.string.scan_QR_code));
        integrator.initiateScan();
    }

    private void selectProductLanguage() {
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
                localeLabels[i] = StringUtils.capitalize(current.getDisplayName(current));
                finalLocalLabels.add(localeLabels[i]);
                finalLocalValues.add(localeValues[i]);
            }
        }
        new MaterialDialog.Builder(activity)
            .title(R.string.preference_choose_language_dialog_title)
            .items(finalLocalLabels)
            .itemsCallbackSingleChoice(selectedIndex, (dialog, view, which, text) -> {
                binding.name.setText(null);
                if (activity instanceof AddProductActivity) {
                    ((AddProductActivity) activity).setIngredients("set", null);
                }
                setProductLanguage(finalLocalValues.get(which));
                return true;
            })
            .positiveText(R.string.ok_button)
            .show();
    }

    /**
     * Before moving next check if the required fields are empty
     */
    public boolean areRequiredFieldsEmpty() {
        if (mImageUrl == null || mImageUrl.equals("")) {
            Snackbar.make(binding.getRoot(), R.string.add_at_least_one_picture, BaseTransientBottomBar.LENGTH_SHORT).show();
            binding.scrollView.fullScroll(View.FOCUS_UP);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_INTEGRATOR_REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
            if (result.getContents() != null) {
                binding.link.setText(result.getContents());
                binding.link.requestFocus();
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            frontImage = true;
        }
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data);
    }

    public void showImageProgress() {
        if (!isAdded()) {
            return;
        }
        binding.imageProgress.setVisibility(View.VISIBLE);
        binding.imageProgressText.setVisibility(View.VISIBLE);
        binding.btnAddImageFront.setVisibility(View.INVISIBLE);
        binding.btnEditImageFront.setVisibility(View.INVISIBLE);
    }

    public void hideImageProgress(boolean errorInUploading, String message) {
        if (!isAdded()) {
            return;
        }
        binding.imageProgress.setVisibility(View.GONE);
        binding.imageProgressText.setVisibility(View.GONE);
        binding.btnAddImageFront.setVisibility(View.VISIBLE);
        binding.btnEditImageFront.setVisibility(View.VISIBLE);
        if (!errorInUploading) {
            Picasso.get()
                .load(photoFile)
                .resize(dpsToPixels(50), dpsToPixels(50))
                .centerInside()
                .into(binding.btnAddImageFront);
        }
    }

    public void showOtherImageProgress() {
        binding.otherImageProgress.setVisibility(View.VISIBLE);
        binding.otherImageProgressText.setVisibility(View.VISIBLE);
        binding.otherImageProgressText.setText(R.string.toastSending);
    }

    public void hideOtherImageProgress(boolean errorUploading, String message) {
        binding.otherImageProgress.setVisibility(View.GONE);
        if (errorUploading) {
            binding.otherImageProgressText.setVisibility(View.GONE);
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        } else {
            binding.otherImageProgressText.setText(R.string.image_uploaded_successfully);
        }
    }
}
