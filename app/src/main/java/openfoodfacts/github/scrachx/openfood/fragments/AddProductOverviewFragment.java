package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;

import com.afollestad.materialdialogs.MaterialDialog;
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
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductOverviewBinding;
import openfoodfacts.github.scrachx.openfood.images.PhotoReceiver;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.jobs.FileDownloader;
import openfoodfacts.github.scrachx.openfood.jobs.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.CategoryNameDao;
import openfoodfacts.github.scrachx.openfood.models.CountryName;
import openfoodfacts.github.scrachx.openfood.models.CountryNameDao;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.LabelNameDao;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.models.Tag;
import openfoodfacts.github.scrachx.openfood.models.TagDao;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.FileUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.adapters.EmbCodeAutoCompleteAdapter;
import openfoodfacts.github.scrachx.openfood.views.adapters.PeriodAfterOpeningAutoCompleteAdapter;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;

import static com.hootsuite.nachos.terminator.ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.OTHER;

/**
 * Product Overview fragment of AddProductActivity
 */
public class AddProductOverviewFragment extends BaseFragment implements PhotoReceiver {
    private static final String PARAM_NAME = "product_name";
    private static final String PARAM_BARCODE = "code";
    private static final String PARAM_QUANTITY = "quantity";
    private static final String PARAM_BRAND = "add_brands";
    private static final String PARAM_INTERFACE_LANGUAGE = "lc";
    private static final String PARAM_PACKAGING = "add_packaging";
    private static final String PARAM_CATEGORIES = "add_categories";
    private static final String PARAM_LABELS = "add_labels";
    private static final String PARAM_PERIODS_AFTER_OPENING = "periods_after_opening";
    private static final String PARAM_ORIGIN = "add_origins";
    private static final String PARAM_MANUFACTURING_PLACE = "add_manufacturing_places";
    private static final String PARAM_EMB_CODE = "add_emb_codes";
    private static final String PARAM_LINK = "link";
    private static final String PARAM_PURCHASE = "add_purchase_places";
    private static final String PARAM_STORE = "add_stores";
    private static final String PARAM_COUNTRIES = "add_countries";
    private static final int INTENT_INTEGRATOR_REQUEST_CODE = 1;
    private Activity activity;
    private FragmentAddProductOverviewBinding binding;
    private String appLanguageCode;
    private List<String> category = new ArrayList<>();
    private String code;
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
    private boolean newImageSelected;
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
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photoReceiverHandler = new PhotoReceiverHandler(this);
        binding.btnOtherPictures.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_a_photo_blue_18dp, 0, 0, 0);

        binding.btnNext.setOnClickListener(v -> next());
        binding.btnAddImageFront.setOnClickListener(v -> addFrontImage());
        binding.btnEditImageFront.setOnClickListener(v -> newFrontImage());
        binding.btnOtherPictures.setOnClickListener(v -> addOtherImage());
        binding.btnOtherPictures.setOnClickListener(v -> addOtherImage());
        binding.sectionManufacturingDetails.setOnClickListener(v -> toggleManufacturingSectionVisibility());
        binding.sectionPurchasingDetails.setOnClickListener(v -> togglePurchasingSectionVisibility());
        binding.hintEmbCode.setOnClickListener(v -> toastEmbCodeHint());
        binding.hintLink.setOnClickListener(v -> searchProductLink());
        binding.hintLink2.setOnClickListener(v -> scanProductLink());
        binding.language.setOnClickListener(v -> selectProductLanguage());

        //checks the information about the prompt clicked and takes action accordingly
        if (getActivity().getIntent().getBooleanExtra(AddProductActivity.MODIFY_CATEGORY_PROMPT, false)) {
            binding.categories.requestFocus();
        } else if (getActivity().getIntent().getBooleanExtra(AddProductActivity.MODIFY_NUTRITION_PROMPT, false)) {
            ((AddProductActivity) getActivity()).proceed();
        }
        appLanguageCode = LocaleHelper.getLanguage(activity);
        Bundle b = getArguments();
        if (b != null) {
            product = (Product) b.getSerializable("product");
            mOfflineSavedProduct = (OfflineSavedProduct) b.getSerializable("edit_offline_product");
            editionMode = b.getBoolean(AddProductActivity.KEY_IS_EDITION);
            binding.barcode.setText(R.string.txtBarcode);
            binding.language.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0);
            binding.sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
            binding.sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
            if (product != null) {
                code = product.getCode();
            }
            if (editionMode && product != null) {
                code = product.getCode();
                String languageToUse = product.getLang();
                if (product.isLanguageSupported(appLanguageCode)) {
                    languageToUse = appLanguageCode;
                }
                preFillProductValues(languageToUse);
            } else if (mOfflineSavedProduct != null) {
                code = mOfflineSavedProduct.getBarcode();
                preFillValuesFromOffline();
            } else {
                //addition
                final boolean fastAdditionMode = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("fastAdditionMode", false);
                enableFastAdditionMode(fastAdditionMode);
            }

            binding.barcode.append(" ");
            binding.barcode.append(code);
            if (BuildConfig.FLAVOR.equals("obf") || BuildConfig.FLAVOR.equals("opf")) {
                binding.btnOtherPictures.setVisibility(View.GONE);
            }
            if (b.getBoolean("perform_ocr")) {
                (getAddProductActivity()).proceed();
            }
            if (b.getBoolean("send_updated")) {
                (getAddProductActivity()).proceed();
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
        mTagDao = Utils.getAppDaoSession(activity).getTagDao();
        mCategoryNameDao = Utils.getAppDaoSession(activity).getCategoryNameDao();
        mLabelNameDao = Utils.getAppDaoSession(activity).getLabelNameDao();
        mCountryNameDao = Utils.getAppDaoSession(activity).getCountryNameDao();
        if (product.getProductName() != null && !product.getProductName().isEmpty()) {
            binding.name.setText(product.getProductName());
        }
        if (product.getQuantity() != null && !product.getQuantity().isEmpty()) {
            binding.quantity.setText(product.getQuantity());
        }
        if (product.getBrands() != null && !product.getBrands().isEmpty()) {
            List<String> chipValues = Arrays.asList(product.getBrands().split("\\s*,\\s*"));
            binding.brand.setText(chipValues);
        }
        if (product.getPackaging() != null && !product.getPackaging().isEmpty()) {
            List<String> chipValues = Arrays.asList(product.getPackaging().split("\\s*,\\s*"));
            binding.packaging.setText(chipValues);
        }
        if (product.getCategoriesTags() != null && !product.getCategoriesTags().isEmpty()) {
            List<String> categoriesTags = product.getCategoriesTags();
            final List<String> chipValues = new ArrayList<>();
            for (String tag : categoriesTags) {
                chipValues.add(getCategoryName(appLanguageCode, tag));
            }
            binding.categories.setText(chipValues);
        }
        if (product.getLabelsTags() != null && !product.getLabelsTags().isEmpty()) {
            List<String> labelsTags = product.getLabelsTags();
            final List<String> chipValues = new ArrayList<>();
            for (String tag : labelsTags) {
                chipValues.add(getLabelName(appLanguageCode, tag));
            }
            binding.label.setText(chipValues);
        }
        if (product.getOrigins() != null && !product.getOrigins().isEmpty()) {
            List<String> chipValues = Arrays.asList(product.getOrigins().split("\\s*,\\s*"));
            binding.originOfIngredients.setText(chipValues);
        }
        if (product.getManufacturingPlaces() != null && !product.getManufacturingPlaces().isEmpty()) {
            binding.manufacturingPlace.setText(product.getManufacturingPlaces());
        }
        if (product.getEmbTags() != null && !product.getEmbTags().toString().trim().equals("[]")) {
            String[] embTags = product.getEmbTags().toString().replace("[", "").replace("]", "").split(", ");
            final List<String> chipValues = new ArrayList<>();
            for (String embTag : embTags) {
                chipValues.add(getEmbCode(embTag));
            }
            binding.embCode.setText(chipValues);
        }
        if (product.getManufactureUrl() != null && !product.getManufactureUrl().isEmpty()) {
            binding.link.setText(product.getManufactureUrl());
        }
        if (product.getPurchasePlaces() != null && !product.getPurchasePlaces().isEmpty()) {
            List<String> chipValues = Arrays.asList(product.getPurchasePlaces().split("\\s*,\\s*"));
            binding.countryWherePurchased.setText(chipValues);
        }
        if (product.getStores() != null && !product.getStores().isEmpty()) {
            List<String> chipValues = Arrays.asList(product.getStores().split("\\s*,\\s*"));
            binding.stores.setText(chipValues);
        }
        if (product.getCountriesTags() != null && !product.getCountriesTags().isEmpty()) {
            List<String> countriesTags = product.getCountriesTags();
            final List<String> chipValues = new ArrayList<>();
            for (String tag : countriesTags) {
                chipValues.add(getCountryName(appLanguageCode, tag));
            }
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
            if (productDetails.get("image_front") != null) {
                binding.imageProgress.setVisibility(View.VISIBLE);
                binding.btnEditImageFront.setVisibility(View.INVISIBLE);
                mImageUrl = productDetails.get("image_front");
                Picasso.get()
                    .load(FileUtils.LOCALE_FILE_SCHEME + mImageUrl)
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
            if (productDetails.get(AddProductActivity.PARAM_LANGUAGE) != null) {
                setProductLanguage(productDetails.get(AddProductActivity.PARAM_LANGUAGE));
            }
            String lc = productDetails.get(AddProductActivity.PARAM_LANGUAGE) != null ? productDetails.get(AddProductActivity.PARAM_LANGUAGE) : "en";
            if (productDetails.get(PARAM_NAME + "_" + lc) != null) {
                binding.name.setText(productDetails.get(PARAM_NAME + "_" + lc));
            } else if (productDetails.get(PARAM_NAME + "_" + "en") != null) {
                binding.name.setText(productDetails.get(PARAM_NAME + "_" + "en"));
            }
            if (productDetails.get(PARAM_QUANTITY) != null) {
                binding.quantity.setText(productDetails.get(PARAM_QUANTITY));
            }
            addChipsText(productDetails, PARAM_BRAND, binding.brand);
            addChipsText(productDetails, PARAM_PACKAGING, binding.packaging);
            addChipsText(productDetails, PARAM_CATEGORIES, binding.categories);
            addChipsText(productDetails, PARAM_LABELS, binding.label);
            addChipsText(productDetails, PARAM_ORIGIN, binding.originOfIngredients);
            if (productDetails.get(PARAM_MANUFACTURING_PLACE) != null) {
                binding.manufacturingPlace.setText(productDetails.get(PARAM_MANUFACTURING_PLACE));
            }
            addChipsText(productDetails, PARAM_EMB_CODE, binding.embCode);
            if (productDetails.get(PARAM_LINK) != null) {
                binding.link.setText(productDetails.get(PARAM_LINK));
            }
            addChipsText(productDetails, PARAM_PURCHASE, binding.countryWherePurchased);
            addChipsText(productDetails, PARAM_STORE, binding.stores);
            addChipsText(productDetails, PARAM_COUNTRIES, binding.countriesWhereSold);
        }
    }

    private void frontImageLoaded() {
        binding.btnEditImageFront.setVisibility(View.VISIBLE);
        binding.imageProgress.setVisibility(View.GONE);
    }

    private void addChipsText(HashMap<String, String> productDetails, String paramName, NachoTextView nachoTextView) {
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
    private void loadAutoSuggestions() {
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
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
            @SuppressWarnings("unchecked")
            List<CountryName> countryNames = (List<CountryName>) operation.getResult();
            countries.clear();
            for (int i = 0; i < countryNames.size(); i++) {
                countries.add(countryNames.get(i).getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_dropdown_item_1line, countries);
            EmbCodeAutoCompleteAdapter customAdapter = new EmbCodeAutoCompleteAdapter(activity, android.R.layout.simple_dropdown_item_1line);
            binding.originOfIngredients.setAdapter(adapter);
            binding.countryWherePurchased.setAdapter(adapter);
            binding.countriesWhereSold.setAdapter(adapter);
            binding.embCode.setAdapter(customAdapter);
        });
        asyncSessionLabels.setListenerMainThread(operation -> {
            @SuppressWarnings("unchecked")
            List<LabelName> labelNames = (List<LabelName>) operation.getResult();
            labels.clear();
            for (int i = 0; i < labelNames.size(); i++) {
                labels.add(labelNames.get(i).getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_dropdown_item_1line, labels);
            binding.label.setAdapter(adapter);
        });
        asyncSessionCategories.setListenerMainThread(operation -> {
            @SuppressWarnings("unchecked")
            List<CategoryName> categoryNames = (List<CategoryName>) operation.getResult();
            category.clear();
            for (int i = 0; i < categoryNames.size(); i++) {
                category.add(categoryNames.get(i).getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_dropdown_item_1line, category);
            binding.categories.setAdapter(adapter);
        });
        if (BuildConfig.FLAVOR.equals("obf")) {
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
            getAddProductActivity().setProductLanguage(languageCode);
        }
        if (editionMode) {
            loadFrontImage(lang);
            OpenFoodAPIService client = CommonApiManager.getInstance().getOpenFoodApiService();
            String fields = "ingredients_text_" + lang + ",product_name_" + lang;
            client.getProductByBarcodeSingle(product.getCode(), fields, Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<State>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        binding.name.setText(getString(R.string.txtLoading));
                    }

                    @Override
                    public void onSuccess(State state) {
                        if (state.getStatus() == 1) {
                            if (state.getProduct().getProductName(lang) != null) {
                                if (languageCode.equals(lang)) {
                                    binding.name.setText(state.getProduct().getProductName(lang));
                                    if (activity instanceof AddProductActivity) {
                                        getAddProductActivity().setIngredients("set", state.getProduct().getIngredientsText(lang));
                                        getAddProductActivity().updateLanguage();
                                    }
                                }
                            } else {
                                binding.name.setText(null);
                                if (activity instanceof AddProductActivity) {
                                    (getAddProductActivity()).setIngredients("set", null);
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

    private AddProductActivity getAddProductActivity() {
        return (AddProductActivity) activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    void next() {
        if (!areRequiredFieldsEmpty() && activity instanceof AddProductActivity) {
            (getAddProductActivity()).proceed();
        }
    }

    void addFrontImage() {
        if (mImageUrl != null) {
            frontImage = true;
            if (photoFile != null) {
                cropRotateImage(photoFile, getString(R.string.set_img_front));
            } else {
                new FileDownloader(getContext()).download(mImageUrl, file -> {
                    photoFile = file;
                    cropRotateImage(photoFile, getString(R.string.set_img_front));
                });
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
        if (activity instanceof AddProductActivity) {
            targetMap.put(PARAM_BARCODE, code);
            targetMap.put(AddProductActivity.PARAM_LANGUAGE, languageCode);
            targetMap.put(PARAM_INTERFACE_LANGUAGE, appLanguageCode);
            String lc = (!languageCode.isEmpty()) ? languageCode : "en";
            targetMap.put(PARAM_NAME + "_" + lc, binding.name.getText().toString());
            targetMap.put(PARAM_QUANTITY, binding.quantity.getText().toString());
            targetMap.put(PARAM_BRAND.substring(4), getValues(binding.brand));
            targetMap.put(PARAM_PACKAGING.substring(4), getValues(binding.packaging));
            targetMap.put(PARAM_CATEGORIES.substring(4), getValues(binding.categories));
            targetMap.put(PARAM_LABELS.substring(4), getValues(binding.label));
            if (BuildConfig.FLAVOR.equals("obf")) {
                targetMap.put(PARAM_PERIODS_AFTER_OPENING, binding.periodOfTimeAfterOpening.getText().toString());
            }
            if (mImageUrl != null) {
                targetMap.put("imageUrl", mImageUrl);
            }
            targetMap.put(PARAM_ORIGIN.substring(4), getValues(binding.originOfIngredients));
            targetMap.put(PARAM_MANUFACTURING_PLACE.substring(4), binding.manufacturingPlace.getText().toString());
            targetMap.put(PARAM_EMB_CODE.substring(4), getValues(binding.embCode));
            targetMap.put(PARAM_LINK, binding.link.getText().toString());
            targetMap.put(PARAM_PURCHASE.substring(4), getValues(binding.countryWherePurchased));
            targetMap.put(PARAM_STORE.substring(4), getValues(binding.stores));
            targetMap.put(PARAM_COUNTRIES.substring(4), getValues(binding.countriesWhereSold));
        }
    }

    /**
     * adds only those fields to the query map which are not empty.
     */
    public void getDetails() {
        chipifyAllUnterminatedTokens();
        if (activity instanceof AddProductActivity) {
            final AddProductActivity addProductActivity = (AddProductActivity) this.activity;
            if (!code.isEmpty()) {
                addProductActivity.addToMap(PARAM_BARCODE, code);
            }
            if (!appLanguageCode.isEmpty()) {
                addProductActivity.addToMap(PARAM_INTERFACE_LANGUAGE, appLanguageCode);
            }
            if (!languageCode.isEmpty()) {
                addProductActivity.addToMap(AddProductActivity.PARAM_LANGUAGE, languageCode);
            }
            if (!binding.name.getText().toString().isEmpty()) {
                String lc = (!languageCode.isEmpty()) ? languageCode : "en";
                addProductActivity.addToMap(PARAM_NAME + "_" + lc, binding.name.getText().toString());
            }
            if (!binding.quantity.getText().toString().isEmpty()) {
                addProductActivity.addToMap(PARAM_QUANTITY, binding.quantity.getText().toString());
            }
            if (!binding.brand.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_BRAND, getValues(binding.brand));
            }
            if (!binding.packaging.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_PACKAGING, getValues(binding.packaging));
            }
            if (!binding.categories.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_CATEGORIES, getValues(binding.categories));
            }
            if (!binding.label.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_LABELS, getValues(binding.label));
            }
            if (!binding.periodOfTimeAfterOpening.getText().toString().isEmpty()) {
                addProductActivity.addToMap(PARAM_PERIODS_AFTER_OPENING, binding.periodOfTimeAfterOpening.getText().toString());
            }
            if (mImageUrl != null) {
                addProductActivity.addToMap("imageUrl", mImageUrl);
            }
            if (!binding.originOfIngredients.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_ORIGIN, getValues(binding.originOfIngredients));
            }
            if (!binding.manufacturingPlace.getText().toString().isEmpty()) {
                addProductActivity.addToMap(PARAM_MANUFACTURING_PLACE, binding.manufacturingPlace.getText().toString());
            }
            if (!binding.embCode.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_EMB_CODE, getValues(binding.embCode));
            }
            if (!binding.link.getText().toString().isEmpty()) {
                addProductActivity.addToMap(PARAM_LINK, binding.link.getText().toString());
            }
            if (!binding.countryWherePurchased.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_PURCHASE, getValues(binding.countryWherePurchased));
            }
            if (!binding.stores.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_STORE, getValues(binding.stores));
            }
            if (!binding.countriesWhereSold.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_COUNTRIES, getValues(binding.countriesWhereSold));
            }
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

    void toggleManufacturingSectionVisibility() {
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
        ((View) binding.hintLink).setVisibility(visibility);
        ((View) binding.hintLink2).setVisibility(visibility);
    }

    void togglePurchasingSectionVisibility() {
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

    void toastEmbCodeHint() {
        new MaterialDialog.Builder(activity)
            .content(R.string.hint_emb_codes)
            .positiveText(R.string.ok_button)
            .show();
    }

    void searchProductLink() {
        String url = "https://www.google.com/search?q=" + code;
        if (!binding.brand.getChipAndTokenValues().isEmpty()) {
            List<String> brandNames = binding.brand.getChipAndTokenValues();
            url = url + " " + StringUtils.join(brandNames, " ");
        }
        if (!binding.name.getText().toString().isEmpty()) {
            url = url + " " + binding.name.getText().toString();
        }
        url = url + " " + getString(R.string.official_website);
        CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(activity.getBaseContext(), null);
        CustomTabActivityHelper.openCustomTab(activity, customTabsIntent, Uri.parse(url), new WebViewFallback());
    }

    void scanProductLink() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setRequestCode(INTENT_INTEGRATOR_REQUEST_CODE);
        integrator.setPrompt(getString(R.string.scan_QR_code));
        integrator.initiateScan();
    }

    void selectProductLanguage() {
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
                    (getAddProductActivity()).setIngredients("set", null);
                }
                setProductLanguage(finalLocalValues.get(which));
                return true;
            })
            .positiveText(R.string.ok_button)
            .show();
    }

    /**
     * Before moving next check if the required feilds are empty
     */
    public boolean areRequiredFieldsEmpty() {
        if (mImageUrl == null || mImageUrl.equals("")) {
            Toast.makeText(getContext(), R.string.add_at_least_one_picture, Toast.LENGTH_SHORT).show();
            if (binding.scrollView != null) {
                binding.scrollView.fullScroll(View.FOCUS_UP);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onPhotoReturned(File newPhotoFile) {
        URI resultUri = newPhotoFile.toURI();
        this.photoFile = newPhotoFile;
        ProductImage image;
        int position;
        if (frontImage) {
            image = new ProductImage(code, FRONT, newPhotoFile);
            mImageUrl = newPhotoFile.getAbsolutePath();
            newImageSelected = true;
            position = 0;
        } else {
            image = new ProductImage(code, OTHER, newPhotoFile);
            position = 3;
        }
        image.setFilePath(resultUri.getPath());
        if (activity instanceof AddProductActivity) {
            (getAddProductActivity()).addToPhotoMap(image, position);
        }
        hideImageProgress(false, StringUtils.EMPTY);
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
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
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
