package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.validator.ChipifyingNachoValidator;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.images.PhotoReceiver;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.jobs.FileDownloader;
import openfoodfacts.github.scrachx.openfood.jobs.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.models.*;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.greenrobot.greendao.async.AsyncSession;

import java.io.File;
import java.net.URI;
import java.util.*;

import static com.hootsuite.nachos.terminator.ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.OTHER;

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
    private PhotoReceiverHandler photoReceiverHandler;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.btnAddImageFront)
    ImageView imageFront;
    @BindView(R.id.btnEditImageFront)
    View editImageFront;
    @BindView(R.id.imageProgress)
    ProgressBar imageProgress;
    @BindView(R.id.imageProgressText)
    TextView imageProgressText;
    @BindView(R.id.section_manufacturing_details)
    TextView sectionManufacturingDetails;
    @BindView(R.id.section_purchasing_details)
    TextView sectionPurchasingDetails;
    @BindView(R.id.barcode)
    TextView barcode;
    @BindView(R.id.language)
    TextView language;
    @BindView(R.id.name)
    EditText name;
    @BindView(R.id.quantity)
    EditText quantity;
    @BindView(R.id.brand)
    NachoTextView brand;
    @BindView(R.id.packaging)
    NachoTextView packaging;
    @BindView(R.id.categories)
    NachoTextView categories;
    @BindView(R.id.label)
    NachoTextView label;
    @BindView(R.id.period_of_time_after_opening)
    AutoCompleteTextView periodsAfterOpening;
    @BindView(R.id.period_of_time_after_opening_til)
    LinearLayout periodsAfterOpeningParent;
    @BindView(R.id.origin_of_ingredients)
    NachoTextView originOfIngredients;
    @BindView(R.id.origin_of_ingredients_til)
    LinearLayout originOfIngredientsParent;
    @BindView(R.id.manufacturing_place)
    EditText manufacturingPlace;
    @BindView(R.id.manufacturing_place_til)
    LinearLayout manufacturingPlaceParent;
    @BindView(R.id.emb_code)
    NachoTextView embCode;
    @BindView(R.id.emb_code_til)
    LinearLayout embCodeParent;
    @BindView(R.id.link)
    EditText link;
    @BindView(R.id.link_til)
    LinearLayout linkParent;
    @BindView(R.id.hint_link)
    View linkHint;
    @BindView(R.id.hint_link_2)
    View linkHint2;
    @BindView(R.id.country_where_purchased)
    NachoTextView countryWherePurchased;
    @BindView(R.id.country_where_purchased_til)
    LinearLayout countryWherePurchasedParent;
    @BindView(R.id.stores)
    NachoTextView stores;
    @BindView(R.id.stores_til)
    LinearLayout storesParent;
    @BindView(R.id.countries_where_sold)
    NachoTextView countriesWhereSold;
    @BindView(R.id.countries_where_sold_til)
    LinearLayout countriesWhereSoldParent;
    @BindView(R.id.btn_other_pictures)
    Button otherImage;
    @BindView(R.id.other_image_progress)
    ProgressBar otherImageProgress;
    @BindView(R.id.other_image_progress_text)
    TextView otherImageProgressText;
    private String languageCode;
    private String appLanguageCode;
    private Activity activity;
    private OfflineSavedProduct mOfflineSavedProduct;
    private TagDao mTagDao;
    private CategoryNameDao mCategoryNameDao;
    private LabelNameDao mLabelNameDao;
    private CountryNameDao mCountryNameDao;
    private Product product;
    private String code;
    private String mImageUrl;
    private boolean frontImage;
    private boolean editionMode;
    private File photoFile;
    private List<String> countries = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private List<String> category = new ArrayList<>();
    private boolean newImageSelected;
    @BindView(R.id.grey_line2)
    View greyLine2;
    @BindView(R.id.grey_line3)
    View greyLine3;
    @BindView(R.id.grey_line4)
    View greyLine4;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product_overview, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photoReceiverHandler = new PhotoReceiverHandler(this);
        otherImage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_a_photo_blue_18dp, 0, 0, 0);

        //checks the information about the prompt clicked and takes action accordingly
        if (getActivity().getIntent().getBooleanExtra(AddProductActivity.MODIFY_CATEGORY_PROMPT, false)) {
            categories.requestFocus();
        } else if (getActivity().getIntent().getBooleanExtra(AddProductActivity.MODIFY_NUTRITION_PROMPT, false)) {
            ((AddProductActivity) getActivity()).proceed();
        }
        appLanguageCode = LocaleHelper.getLanguage(activity);
        Bundle b = getArguments();
        if (b != null) {
            product = (Product) b.getSerializable("product");
            mOfflineSavedProduct = (OfflineSavedProduct) b.getSerializable("edit_offline_product");
            editionMode = b.getBoolean(AddProductActivity.KEY_IS_EDITION);
            barcode.setText(R.string.txtBarcode);
            language.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0);
            sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
            sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
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
                if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("fastAdditionMode", false)) {
                    enableFastAdditionMode(true);
                } else {
                    enableFastAdditionMode(false);
                }
            }

            barcode.append(" ");
            barcode.append(code);
            if (BuildConfig.FLAVOR.equals("obf") || BuildConfig.FLAVOR.equals("opf")) {
                otherImage.setVisibility(View.GONE);
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
        if(StringUtils.isBlank(languageCode)){
            setProductLanguage(appLanguageCode);
        }
    }

    /**
     * To enable fast addition mode
     *
     * @param isEnabled
     */
    private void enableFastAdditionMode(boolean isEnabled) {
        int visibility = View.VISIBLE;
        if (isEnabled) {
            visibility = View.GONE;
        }
        sectionManufacturingDetails.setVisibility(visibility);
        sectionPurchasingDetails.setVisibility(visibility);
        packaging.setVisibility(visibility);
        label.setVisibility(visibility);
        periodsAfterOpeningParent.setVisibility(visibility);
        changeVisibilityManufacturingSectionTo(visibility);
        changePurchasingSectionVisibilityTo(visibility);
        greyLine2.setVisibility(visibility);
        greyLine3.setVisibility(visibility);
        greyLine4.setVisibility(visibility);
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
            name.setText(product.getProductName());
        }
        if (product.getQuantity() != null && !product.getQuantity().isEmpty()) {
            quantity.setText(product.getQuantity());
        }
        if (product.getBrands() != null && !product.getBrands().isEmpty()) {
            List<String> chipValues = Arrays.asList(product.getBrands().split("\\s*,\\s*"));
            brand.setText(chipValues);
        }
        if (product.getPackaging() != null && !product.getPackaging().isEmpty()) {
            List<String> chipValues = Arrays.asList(product.getPackaging().split("\\s*,\\s*"));
            packaging.setText(chipValues);
        }
        if (product.getCategoriesTags() != null && !product.getCategoriesTags().isEmpty()) {
            List<String> categoriesTags = product.getCategoriesTags();
            final List<String> chipValues = new ArrayList<>();
            for (String tag : categoriesTags) {
                chipValues.add(getCategoryName(appLanguageCode, tag));
            }
            categories.setText(chipValues);
        }
        if (product.getLabelsTags() != null && !product.getLabelsTags().isEmpty()) {
            List<String> labelsTags = product.getLabelsTags();
            final List<String> chipValues = new ArrayList<>();
            for (String tag : labelsTags) {
                chipValues.add(getLabelName(appLanguageCode, tag));
            }
            label.setText(chipValues);
        }
        if (product.getOrigins() != null && !product.getOrigins().isEmpty()) {
            List<String> chipValues = Arrays.asList(product.getOrigins().split("\\s*,\\s*"));
            originOfIngredients.setText(chipValues);
        }
        if (product.getManufacturingPlaces() != null && !product.getManufacturingPlaces().isEmpty()) {
            manufacturingPlace.setText(product.getManufacturingPlaces());
        }
        if (product.getEmbTags() != null && !product.getEmbTags().toString().trim().equals("[]")) {
            String[] embTags = product.getEmbTags().toString().replace("[", "").replace("]", "").split(", ");
            final List<String> chipValues = new ArrayList<>();
            for (String embTag : embTags) {
                chipValues.add(getEmbCode(embTag));
            }
            embCode.setText(chipValues);
        }
        if (product.getManufactureUrl() != null && !product.getManufactureUrl().isEmpty()) {
            link.setText(product.getManufactureUrl());
        }
        if (product.getPurchasePlaces() != null && !product.getPurchasePlaces().isEmpty()) {
            List<String> chipValues = Arrays.asList(product.getPurchasePlaces().split("\\s*,\\s*"));
            countryWherePurchased.setText(chipValues);
        }
        if (product.getStores() != null && !product.getStores().isEmpty()) {
            List<String> chipValues = Arrays.asList(product.getStores().split("\\s*,\\s*"));
            stores.setText(chipValues);
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
            countriesWhereSold.setText(chipValues);
        }
        setProductLanguage(lang);
    }

    private void loadFrontImage(String language) {
        photoFile=null;
        final String imageFrontUrl = product.getImageFrontUrl(language);
        if (imageFrontUrl != null && !imageFrontUrl.isEmpty()) {

            mImageUrl = imageFrontUrl;
            imageProgress.setVisibility(View.VISIBLE);
            editImageFront.setVisibility(View.INVISIBLE);
            Picasso.with(getContext())
                .load(imageFrontUrl)
                .resize(dpsToPixels(50), dpsToPixels(50))
                .centerInside()
                .into(imageFront, new Callback() {
                    @Override
                    public void onSuccess() {
                        frontImageLoaded();
                    }

                    @Override
                    public void onError() {
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
                imageProgress.setVisibility(View.VISIBLE);
                editImageFront.setVisibility(View.INVISIBLE);
                mImageUrl = productDetails.get("image_front");
                Picasso.with(getContext())
                    .load(FileUtils.LOCALE_FILE_SCHEME+ mImageUrl)
                    .resize(dpsToPixels(50), dpsToPixels(50))
                    .centerInside()
                    .into(imageFront, new Callback() {
                        @Override
                        public void onSuccess() {
                            frontImageLoaded();
                        }

                        @Override
                        public void onError() {
                            frontImageLoaded();
                        }
                    });
            }
            if (productDetails.get(AddProductActivity.PARAM_LANGUAGE) != null) {
                setProductLanguage(productDetails.get(AddProductActivity.PARAM_LANGUAGE));
            }
            String lc = productDetails.get(AddProductActivity.PARAM_LANGUAGE) != null ? productDetails.get(AddProductActivity.PARAM_LANGUAGE) : "en";
            if (productDetails.get(PARAM_NAME + "_" + lc) != null) {
                name.setText(productDetails.get(PARAM_NAME + "_" + lc));
            } else if (productDetails.get(PARAM_NAME + "_" + "en") != null) {
                name.setText(productDetails.get(PARAM_NAME + "_" + "en"));
            }
            if (productDetails.get(PARAM_QUANTITY) != null) {
                quantity.setText(productDetails.get(PARAM_QUANTITY));
            }
            addChipsText(productDetails, PARAM_BRAND, brand);
            addChipsText(productDetails, PARAM_PACKAGING, packaging);
            addChipsText(productDetails, PARAM_CATEGORIES, categories);
            addChipsText(productDetails, PARAM_LABELS, label);
            addChipsText(productDetails, PARAM_ORIGIN, originOfIngredients);
            if (productDetails.get(PARAM_MANUFACTURING_PLACE) != null) {
                manufacturingPlace.setText(productDetails.get(PARAM_MANUFACTURING_PLACE));
            }
            addChipsText(productDetails, PARAM_EMB_CODE, embCode);
            if (productDetails.get(PARAM_LINK) != null) {
                link.setText(productDetails.get(PARAM_LINK));
            }
            addChipsText(productDetails, PARAM_PURCHASE, countryWherePurchased);
            addChipsText(productDetails, PARAM_STORE, stores);
            addChipsText(productDetails, PARAM_COUNTRIES, countriesWhereSold);
        }
    }

    private void frontImageLoaded() {
        editImageFront.setVisibility(View.VISIBLE);
        imageProgress.setVisibility(View.GONE);
    }

    private void addChipsText(HashMap<String, String> productDetails, String paramName, NachoTextView nachoTextView) {
        if (productDetails.get(paramName) != null) {
            List<String> chipValues = Arrays.asList(productDetails.get(paramName).split("\\s*,\\s*"));
            nachoTextView.setText(chipValues);
        }
    }

    private void initializeChips() {
        NachoTextView[] nachoTextViews = {brand, packaging, categories, label, originOfIngredients, embCode, countryWherePurchased, stores, countriesWhereSold};
        for (NachoTextView nachoTextView : nachoTextViews) {
            nachoTextView.addChipTerminator(',', BEHAVIOR_CHIPIFY_CURRENT_TOKEN);
            nachoTextView.setNachoValidator(new ChipifyingNachoValidator());
            nachoTextView.enableEditChipOnTouch(false, true);
        }
    }

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
            originOfIngredients.setAdapter(adapter);
            countryWherePurchased.setAdapter(adapter);
            countriesWhereSold.setAdapter(adapter);
            embCode.setAdapter(customAdapter);
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
            label.setAdapter(adapter);
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
            categories.setAdapter(adapter);
        });
        if (BuildConfig.FLAVOR.equals("obf")) {
            periodsAfterOpeningParent.setVisibility(View.VISIBLE);
            PeriodAfterOpeningAutoCompleteAdapter customAdapter = new PeriodAfterOpeningAutoCompleteAdapter(activity, android.R.layout.simple_dropdown_item_1line);
            periodsAfterOpening.setAdapter(customAdapter);
        }
    }

    private void setProductLanguage(String lang) {
        languageCode = lang;
        Locale current = LocaleHelper.getLocale(lang);
        language.setText(R.string.product_language);
        language.append(WordUtils.capitalize(current.getDisplayName(current)));
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
                        name.setText(getString(R.string.txtLoading));
                    }

                    @Override
                    public void onSuccess(State state) {
                        if (state.getStatus() == 1) {
                            if (state.getProduct().getProductName(lang) != null) {
                                if (languageCode.equals(lang)) {
                                    name.setText(state.getProduct().getProductName(lang));
                                    if (activity instanceof AddProductActivity) {
                                        getAddProductActivity().setIngredients("set", state.getProduct().getIngredientsText(lang));
                                        getAddProductActivity().updateLanguage();
                                    }
                                }
                            } else {
                                name.setText(null);
                                if (activity instanceof AddProductActivity) {
                                    (getAddProductActivity()).setIngredients("set", null);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        name.setText(null);
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

    @OnClick(R.id.btn_next)
    void next() {
        if (!areRequiredFieldsEmpty() && activity instanceof AddProductActivity) {
            (getAddProductActivity()).proceed();
        }
    }

    @OnClick(R.id.btnAddImageFront)
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

    @OnClick(R.id.btnEditImageFront)
    void newFrontImage() {
        // add front image.
        frontImage = true;
        doChooseOrTakePhotos(getString(R.string.set_img_front));
    }

    @OnClick(R.id.btn_other_pictures)
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
            targetMap.put(PARAM_NAME + "_" + lc, name.getText().toString());
            targetMap.put(PARAM_QUANTITY, quantity.getText().toString());
            targetMap.put(PARAM_BRAND.substring(4), getValues(brand));
            targetMap.put(PARAM_PACKAGING.substring(4), getValues(packaging));
            targetMap.put(PARAM_CATEGORIES.substring(4), getValues(categories));
            targetMap.put(PARAM_LABELS.substring(4), getValues(label));
            if (BuildConfig.FLAVOR.equals("obf")) {
                targetMap.put(PARAM_PERIODS_AFTER_OPENING, periodsAfterOpening.getText().toString());
            }
            if (mImageUrl != null) {
                targetMap.put("imageUrl", mImageUrl);
            }
            targetMap.put(PARAM_ORIGIN.substring(4), getValues(originOfIngredients));
            targetMap.put(PARAM_MANUFACTURING_PLACE.substring(4), manufacturingPlace.getText().toString());
            targetMap.put(PARAM_EMB_CODE.substring(4), getValues(embCode));
            targetMap.put(PARAM_LINK, link.getText().toString());
            targetMap.put(PARAM_PURCHASE.substring(4), getValues(countryWherePurchased));
            targetMap.put(PARAM_STORE.substring(4), getValues(stores));
            targetMap.put(PARAM_COUNTRIES.substring(4), getValues(countriesWhereSold));
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
            if (!name.getText().toString().isEmpty()) {
                String lc = (!languageCode.isEmpty()) ? languageCode : "en";
                addProductActivity.addToMap(PARAM_NAME + "_" + lc, name.getText().toString());
            }
            if (!quantity.getText().toString().isEmpty()) {
                addProductActivity.addToMap(PARAM_QUANTITY, quantity.getText().toString());
            }
            if (!brand.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_BRAND, getValues(brand));
            }
            if (!packaging.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_PACKAGING, getValues(packaging));
            }
            if (!categories.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_CATEGORIES, getValues(categories));
            }
            if (!label.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_LABELS, getValues(label));
            }
            if (!periodsAfterOpening.getText().toString().isEmpty()) {
                addProductActivity.addToMap(PARAM_PERIODS_AFTER_OPENING, periodsAfterOpening.getText().toString());
            }
            if (mImageUrl != null) {
                addProductActivity.addToMap("imageUrl", mImageUrl);
            }
            if (!originOfIngredients.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_ORIGIN, getValues(originOfIngredients));
            }
            if (!manufacturingPlace.getText().toString().isEmpty()) {
                addProductActivity.addToMap(PARAM_MANUFACTURING_PLACE, manufacturingPlace.getText().toString());
            }
            if (!embCode.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_EMB_CODE, getValues(embCode));
            }
            if (!link.getText().toString().isEmpty()) {
                addProductActivity.addToMap(PARAM_LINK, link.getText().toString());
            }
            if (!countryWherePurchased.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_PURCHASE, getValues(countryWherePurchased));
            }
            if (!stores.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_STORE, getValues(stores));
            }
            if (!countriesWhereSold.getChipValues().isEmpty()) {
                addProductActivity.addToMap(PARAM_COUNTRIES, getValues(countriesWhereSold));
            }
        }
    }

    /**
     * Chipifies all existing plain text in all the NachoTextViews.
     */
    private void chipifyAllUnterminatedTokens() {
        NachoTextView[] nachoTextViews = {brand, packaging, categories, label, originOfIngredients, embCode, countryWherePurchased, stores, countriesWhereSold};
        for (NachoTextView nachoTextView : nachoTextViews) {
            nachoTextView.chipifyAllUnterminatedTokens();
        }
    }

    private String getValues(NachoTextView nachoTextView) {
        List<String> list = nachoTextView.getChipValues();
        return StringUtils.join(list, ",");
    }

    @OnClick(R.id.section_manufacturing_details)
    void toggleManufacturingSectionVisibility() {
        if (manufacturingPlaceParent.getVisibility() != View.VISIBLE) {
            changeVisibilityManufacturingSectionTo(View.VISIBLE);
            originOfIngredients.requestFocus();
            sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up_grey_24dp, 0);
        } else {
            changeVisibilityManufacturingSectionTo(View.GONE);
            sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
        }
    }

    private void changeVisibilityManufacturingSectionTo(int visibility) {
        originOfIngredientsParent.setVisibility(visibility);
        manufacturingPlaceParent.setVisibility(visibility);
        embCodeParent.setVisibility(visibility);
        linkParent.setVisibility(visibility);
        linkHint.setVisibility(visibility);
        linkHint2.setVisibility(visibility);
    }

    @OnClick(R.id.section_purchasing_details)
    void togglePurchasingSectionVisibility() {
        if (storesParent.getVisibility() != View.VISIBLE) {
            changePurchasingSectionVisibilityTo(View.VISIBLE);
            countryWherePurchased.requestFocus();
            sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up_grey_24dp, 0);
        } else {
            changePurchasingSectionVisibilityTo(View.GONE);
            sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
        }
    }

    private void changePurchasingSectionVisibilityTo(int visibility) {
        countryWherePurchasedParent.setVisibility(visibility);
        storesParent.setVisibility(visibility);
        countriesWhereSoldParent.setVisibility(visibility);
    }

    @OnClick(R.id.hint_emb_code)
    void toastEmbCodeHint() {
        new MaterialDialog.Builder(activity)
            .content(R.string.hint_emb_codes)
            .positiveText(R.string.ok_button)
            .show();
    }

    @OnClick(R.id.hint_link)
    void searchProductLink() {
        String url = "https://www.google.com/search?q=" + code;
        if (!brand.getChipAndTokenValues().isEmpty()) {
            List<String> brandNames = brand.getChipAndTokenValues();
            url = url + " " + StringUtils.join(brandNames, " ");
        }
        if (!name.getText().toString().isEmpty()) {
            url = url + " " + name.getText().toString();
        }
        url = url + " " + getString(R.string.official_website);
        CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(activity.getBaseContext(), null);
        CustomTabActivityHelper.openCustomTab(activity, customTabsIntent, Uri.parse(url), new WebViewFallback());
    }

    @OnClick(R.id.hint_link_2)
    void scanProductLink() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setRequestCode(INTENT_INTEGRATOR_REQUEST_CODE);
        integrator.setPrompt(getString(R.string.scan_QR_code));
        integrator.initiateScan();
    }

    @OnClick(R.id.language)
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
                localeLabels[i] = WordUtils.capitalize(current.getDisplayName(current));
                finalLocalLabels.add(localeLabels[i]);
                finalLocalValues.add(localeValues[i]);
            }
        }
        new MaterialDialog.Builder(activity)
            .title(R.string.preference_choose_language_dialog_title)
            .items(finalLocalLabels)
            .itemsCallbackSingleChoice(selectedIndex, (dialog, view, which, text) -> {
                name.setText(null);
                if (activity instanceof AddProductActivity) {
                    (getAddProductActivity()).setIngredients("set", null);
                }
                setProductLanguage(finalLocalValues.get(which));
                return true;
            })
            .positiveText(R.string.ok_button)
            .show();
    }

    public boolean areRequiredFieldsEmpty() {
        if (mImageUrl == null || mImageUrl.equals("")) {
            Toast.makeText(OFFApplication.getInstance(), R.string.add_at_least_one_picture, Toast.LENGTH_SHORT).show();
            scrollView.fullScroll(View.FOCUS_UP);
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
                link.setText(result.getContents());
                link.requestFocus();
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            frontImage = true;
        }
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data);
    }

    public void showImageProgress() {
        imageProgress.setVisibility(View.VISIBLE);
        imageProgressText.setVisibility(View.VISIBLE);
        imageFront.setVisibility(View.INVISIBLE);
        editImageFront.setVisibility(View.INVISIBLE);
    }

    public void hideImageProgress(boolean errorInUploading, String message) {
        imageProgress.setVisibility(View.GONE);
        imageProgressText.setVisibility(View.GONE);
        imageFront.setVisibility(View.VISIBLE);
        editImageFront.setVisibility(View.VISIBLE);
        if (!errorInUploading) {
            Picasso.with(activity)
                .load(photoFile)
                .resize(dpsToPixels(50), dpsToPixels(50))
                .centerInside()
                .into(imageFront);
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    public void showOtherImageProgress() {
        otherImageProgress.setVisibility(View.VISIBLE);
        otherImageProgressText.setVisibility(View.VISIBLE);
        otherImageProgressText.setText(R.string.toastSending);
    }

    public void hideOtherImageProgress(boolean errorUploading, String message) {
        otherImageProgress.setVisibility(View.GONE);
        if (errorUploading) {
            otherImageProgressText.setVisibility(View.GONE);
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        } else {
            otherImageProgressText.setText(R.string.image_uploaded_successfully);
        }
    }
}
