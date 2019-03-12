package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.validator.ChipifyingNachoValidator;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.greenrobot.greendao.async.AsyncSession;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.CategoryNameDao;
import openfoodfacts.github.scrachx.openfood.models.CountryName;
import openfoodfacts.github.scrachx.openfood.models.CountryNameDao;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.LabelNameDao;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.models.Tag;
import openfoodfacts.github.scrachx.openfood.models.TagDao;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.adapters.EmbCodeAutoCompleteAdapter;
import openfoodfacts.github.scrachx.openfood.views.adapters.PeriodAfterOpeningAutoCompleteAdapter;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.hootsuite.nachos.terminator.ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.OTHER;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;

public class AddProductOverviewFragment extends BaseFragment {

    private static final String PARAM_NAME = "product_name";
    private static final String PARAM_BARCODE = "code";
    private static final String PARAM_QUANTITY = "quantity";
    private static final String PARAM_BRAND = "add_brands";
    private static final String PARAM_LANGUAGE = "lang";
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
    private static final String PARAM_EATING = "eating";
    private static final String PARAM_OTHER_INFORMATION = "other_information";
    private static final String PARAM_CONSERVATION_CONDITIONS = "conservation_conditions";
    private static final String PARAM_RECYCLING_INSTRUCTION_TO_DISCARD = "recycling_instructions_to_discard";
    private static final String PARAM_RECYCLING_INSTRUCTION_TO_RECYCLE = "recycling_instructions_to_recycle";
    private static final int INTENT_INTEGRATOR_REQUEST_CODE = 1;

    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.btnAddImageFront)
    ImageView imageFront;
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
    @BindView(R.id.cb_eating)
    CheckBox cbEating;
    @BindView(R.id.origin_of_ingredients)
    NachoTextView originOfIngredients;
    @BindView(R.id.manufacturing_place)
    EditText manufacturingPlace;
    @BindView(R.id.emb_code)
    NachoTextView embCode;
    //disabling fields in edit/add mode
    /*@BindView(R.id.other_info)
    EditText otherInfo;
    @BindView(R.id.conservationCond)
    EditText conservationCond;
    @BindView(R.id.recyclingInstructionToDiscard)
    EditText recyclingInstructionToDiscard;
    @BindView(R.id.recyclingInstructionToRecycle)
    EditText recyclingInstructionToRecycle;*/
    @BindView(R.id.link)
    EditText link;
    @BindView(R.id.country_where_purchased)
    NachoTextView countryWherePurchased;
    @BindView(R.id.stores)
    NachoTextView stores;
    @BindView(R.id.countries_where_sold)
    NachoTextView countriesWhereSold;
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
    private boolean edit_product;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
        //checks the information about the prompt clicked and takes action accordingly
        if(getActivity().getIntent().getBooleanExtra("modify_category_prompt", false)) {
            categories.requestFocus();
        }else if(getActivity().getIntent().getBooleanExtra("modify_nutrition_prompt", false)) {
            ((AddProductActivity) getActivity()).proceed();
        }
        Bundle b = getArguments();
        if (b != null) {
            product = (Product) b.getSerializable("product");
            mOfflineSavedProduct = (OfflineSavedProduct) b.getSerializable("edit_offline_product");
            edit_product = b.getBoolean("edit_product");
            appLanguageCode = LocaleHelper.getLanguage(activity);
            setProductLanguage(appLanguageCode);
            barcode.setText(R.string.txtBarcode);
            language.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0);
            sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
            sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
            if (product != null) {
                code = product.getCode();
            }
            if (edit_product && product != null) {
                code = product.getCode();
                preFillProductValues();
            } else if (mOfflineSavedProduct != null) {
                code = mOfflineSavedProduct.getBarcode();
                preFillValues();
            } else {
                //adittion
                if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("fastAdditionMode", false)) {
                    enableFastAdditionMode(true);
                } else {
                    enableFastAdditionMode(false);
                }
            }
            barcode.append(" " + code);
            if (BuildConfig.FLAVOR.equals("obf") || BuildConfig.FLAVOR.equals("opf")) {
                otherImage.setVisibility(View.GONE);
            }
            if(b.getBoolean("perform_ocr")) {
                ((AddProductActivity) activity).proceed();
            }
            if (b.getBoolean("send_updated")) {
                ((AddProductActivity) activity).proceed();
            }
        } else {
            Toast.makeText(activity, R.string.error_adding_product_details, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        link.setHint(Html.fromHtml("<small><small>" +
                getString(R.string.hint_product_URL) + "</small></small>"));
        initializeChips();
        loadAutoSuggestions();
        //disabling fields in edit/add mode
        //otherInfo.setEnabled(false);
        //.setEnabled(false);
        //recyclingInstructionToRecycle.setEnabled(false);
        //recyclingInstructionToDiscard.setEnabled(false);
    }

    /**
     * To enable fast addition mode
     *
     * @param isEnabled
     */
    private void enableFastAdditionMode(boolean isEnabled) {
        if (isEnabled) {
            sectionManufacturingDetails.setVisibility(View.GONE);
            sectionPurchasingDetails.setVisibility(View.GONE);
            packaging.setVisibility(View.GONE);
            label.setVisibility(View.GONE);
            periodsAfterOpening.setVisibility(View.GONE);
            originOfIngredients.setVisibility(View.GONE);
            manufacturingPlace.setVisibility(View.GONE);
            embCode.setVisibility(View.GONE);
            link.setVisibility(View.GONE);
            countryWherePurchased.setVisibility(View.GONE);
            stores.setVisibility(View.GONE);
            countriesWhereSold.setVisibility(View.GONE);
            otherImage.setVisibility(View.GONE);
            greyLine2.setVisibility(View.GONE);
            greyLine3.setVisibility(View.GONE);
            greyLine4.setVisibility(View.GONE);
            //disabling fields in edit/add mode
            //otherInfo.setVisibility(View.GONE);
            //conservationCond.setVisibility(View.GONE);
            //recyclingInstructionToDiscard.setVisibility(View.GONE);
            //recyclingInstructionToRecycle.setVisibility(View.GONE);
            periodsAfterOpening.setVisibility(View.GONE);
            cbEating.setVisibility(View.GONE);
        } else {
            sectionManufacturingDetails.setVisibility(View.VISIBLE);
            sectionPurchasingDetails.setVisibility(View.VISIBLE);
            packaging.setVisibility(View.VISIBLE);
            label.setVisibility(View.VISIBLE);
            periodsAfterOpening.setVisibility(View.VISIBLE);
            originOfIngredients.setVisibility(View.VISIBLE);
            manufacturingPlace.setVisibility(View.VISIBLE);
            embCode.setVisibility(View.VISIBLE);
            link.setVisibility(View.VISIBLE);
            countryWherePurchased.setVisibility(View.VISIBLE);
            stores.setVisibility(View.VISIBLE);
            countriesWhereSold.setVisibility(View.VISIBLE);
            otherImage.setVisibility(View.VISIBLE);
            greyLine2.setVisibility(View.VISIBLE);
            greyLine3.setVisibility(View.VISIBLE);
            greyLine4.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Pre fill the fields of the product which are already present on the server.
     */
    private void preFillProductValues() {
        mTagDao = Utils.getAppDaoSession(activity).getTagDao();
        mCategoryNameDao = Utils.getAppDaoSession(activity).getCategoryNameDao();
        mLabelNameDao = Utils.getAppDaoSession(activity).getLabelNameDao();
        mCountryNameDao = Utils.getAppDaoSession(activity).getCountryNameDao();
        if (product.getImageFrontUrl() != null && !product.getImageFrontUrl().isEmpty()) {
            mImageUrl = product.getImageFrontUrl();
            imageProgress.setVisibility(View.VISIBLE);
            Picasso.with(getContext())
                    .load(product.getImageFrontUrl())
                    .resize(dpsToPixels(), dpsToPixels())
                    .centerInside()
                    .into(imageFront, new Callback() {
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
        if (product.getLang() != null && !product.getLang().isEmpty()) {
            setProductLanguage(product.getLang());
        }
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
            String savedCountry = sharedPref.getString("user_country","");
            if (!savedCountry.isEmpty()) {
                chipValues.add(savedCountry);
            }
            countriesWhereSold.setText(chipValues);
        }
        //disabling fields in edit/add mode
        /*if (product.getOtherInformation() != null && !product.getOtherInformation().isEmpty()) {
            otherInfo.setText(product.getOtherInformation());
        }
        if (product.getConservationConditions() != null && !product.getConservationConditions().isEmpty()) {
            conservationCond.setText(product.getConservationConditions());
        }
        if (product.getRecyclingInstructionsToDiscard() != null && !product.getRecyclingInstructionsToDiscard().isEmpty()) {
            recyclingInstructionToDiscard.setText(product.getRecyclingInstructionsToDiscard());
        }
        if (product.getRecyclingInstructionsToRecycle() != null && !product.getRecyclingInstructionsToRecycle().isEmpty()) {
            recyclingInstructionToRecycle.setText(product.getRecyclingInstructionsToRecycle());
        }*/
    }

    /**
     * @param languageCode 2 letter language code. example hi, en etc.
     * @param tag          the complete tag. example en:india
     * @return returns the name of the country if found in the db or else returns the tag itself.
     */
    private String getCountryName(String languageCode, String tag) {
        CountryName countryName = mCountryNameDao.queryBuilder().where(CountryNameDao.Properties.CountyTag.eq(tag), CountryNameDao.Properties.LanguageCode.eq(languageCode)).unique();
        if (countryName != null) return countryName.getName();
        return tag;
    }

    /**
     * @param languageCode 2 letter language code. example de, en etc.
     * @param tag          the complete tag. example de:hoher-omega-3-gehalt
     * @return returns the name of the label if found in the db or else returns the tag itself.
     */
    private String getLabelName(String languageCode, String tag) {
        LabelName labelName = mLabelNameDao.queryBuilder().where(LabelNameDao.Properties.LabelTag.eq(tag), LabelNameDao.Properties.LanguageCode.eq(languageCode)).unique();
        if (labelName != null) return labelName.getName();
        return tag;
    }

    /**
     * @param languageCode 2 letter language code. example en, fr etc.
     * @param tag          the complete tag. example en:plant-based-foods-and-beverages
     * @return returns the name of the category (example Plant-based foods and beverages) if found in the db or else returns the tag itself.
     */
    private String getCategoryName(String languageCode, String tag) {
        CategoryName categoryName = mCategoryNameDao.queryBuilder().where(CategoryNameDao.Properties.CategoryTag.eq(tag), CategoryNameDao.Properties.LanguageCode.eq(languageCode)).unique();
        if (categoryName != null) return categoryName.getName();
        return tag;
    }

    private String getEmbCode(String embTag) {
        Tag tag = mTagDao.queryBuilder().where(TagDao.Properties.Id.eq(embTag)).unique();
        if (tag != null) return tag.getName();
        return embTag;
    }

    /**
     * Pre fill the fields if the product is already present in SavedProductOffline db.
     */
    private void preFillValues() {
        HashMap<String, String> productDetails = mOfflineSavedProduct.getProductDetailsMap();
        if (productDetails != null) {
            if (productDetails.get("image_front") != null) {
                imageProgress.setVisibility(View.VISIBLE);
                mImageUrl = productDetails.get("image_front");
                Picasso.with(getContext())
                        .load("file://" + mImageUrl)
                        .resize(dpsToPixels(), dpsToPixels())
                        .centerInside()
                        .into(imageFront, new Callback() {
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
            if (productDetails.get(PARAM_LANGUAGE) != null) {
                setProductLanguage(productDetails.get(PARAM_LANGUAGE));
            }
            String lc = productDetails.get(PARAM_LANGUAGE) != null ? productDetails.get(PARAM_LANGUAGE) : "en";
            if (productDetails.get(PARAM_NAME + "_" + lc) != null) {
                name.setText(productDetails.get(PARAM_NAME + "_" + lc));
            } else if (productDetails.get(PARAM_NAME + "_" + "en") != null) {
                name.setText(productDetails.get(PARAM_NAME + "_" + "en"));
            }
            if (productDetails.get(PARAM_QUANTITY) != null) {
                quantity.setText(productDetails.get(PARAM_QUANTITY));
            }
            if (productDetails.get(PARAM_BRAND) != null) {
                List<String> chipValues = Arrays.asList(productDetails.get(PARAM_BRAND).split("\\s*,\\s*"));
                brand.setText(chipValues);
            }
            if (productDetails.get(PARAM_PACKAGING) != null) {
                List<String> chipValues = Arrays.asList(productDetails.get(PARAM_PACKAGING).split("\\s*,\\s*"));
                packaging.setText(chipValues);
            }
            if (productDetails.get(PARAM_CATEGORIES) != null) {
                List<String> chipValues = Arrays.asList(productDetails.get(PARAM_CATEGORIES).split("\\s*,\\s*"));
                categories.setText(chipValues);
            }
            if (productDetails.get(PARAM_LABELS) != null) {
                List<String> chipValues = Arrays.asList(productDetails.get(PARAM_LABELS).split("\\s*,\\s*"));
                label.setText(chipValues);
            }
            if (productDetails.get(PARAM_ORIGIN) != null) {
                List<String> chipValues = Arrays.asList(productDetails.get(PARAM_ORIGIN).split("\\s*,\\s*"));
                originOfIngredients.setText(chipValues);
            }
            if (productDetails.get(PARAM_MANUFACTURING_PLACE) != null) {
                manufacturingPlace.setText(productDetails.get(PARAM_MANUFACTURING_PLACE));
            }
            if (productDetails.get(PARAM_EMB_CODE) != null) {
                List<String> chipValues = Arrays.asList(productDetails.get(PARAM_EMB_CODE).split("\\s*,\\s*"));
                embCode.setText(chipValues);
            }
            if (productDetails.get(PARAM_LINK) != null) {
                link.setText(productDetails.get(PARAM_LINK));
            }
            if (productDetails.get(PARAM_PURCHASE) != null) {
                List<String> chipValues = Arrays.asList(productDetails.get(PARAM_PURCHASE).split("\\s*,\\s*"));
                countryWherePurchased.setText(chipValues);
            }
            if (productDetails.get(PARAM_STORE) != null) {
                List<String> chipValues = Arrays.asList(productDetails.get(PARAM_STORE).split("\\s*,\\s*"));
                stores.setText(chipValues);
            }
            if (productDetails.get(PARAM_COUNTRIES) != null) {
                List<String> chipValues = Arrays.asList(productDetails.get(PARAM_COUNTRIES).split("\\s*,\\s*"));
                countriesWhereSold.setText(chipValues);
            }
            //disabling fields in edit/add mode
           /* if (productDetails.get(PARAM_OTHER_INFORMATION) != null) {
                otherInfo.setText(productDetails.get(PARAM_OTHER_INFORMATION));
            }
            if (productDetails.get(PARAM_CONSERVATION_CONDITIONS) != null) {
                conservationCond.setText(productDetails.get(PARAM_CONSERVATION_CONDITIONS));
            }
            if (productDetails.get(PARAM_RECYCLING_INSTRUCTION_TO_DISCARD) != null) {
                recyclingInstructionToDiscard.setText(productDetails.get(PARAM_RECYCLING_INSTRUCTION_TO_DISCARD));
            }
            if (productDetails.get(PARAM_RECYCLING_INSTRUCTION_TO_RECYCLE) != null) {
                recyclingInstructionToRecycle.setText(productDetails.get(PARAM_RECYCLING_INSTRUCTION_TO_RECYCLE));
            }*/
        }
    }

    private void initializeChips() {
        NachoTextView nachoTextViews[] = {brand, packaging, categories, label, originOfIngredients, embCode, countryWherePurchased, stores, countriesWhereSold};
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
            periodsAfterOpening.setVisibility(View.VISIBLE);
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
            ((AddProductActivity) activity).addToMap(PARAM_LANGUAGE, languageCode);
        }
        if (edit_product) {
            OpenFoodAPIService client = CommonApiManager.getInstance().getOpenFoodApiService();
            String fields = "ingredients_text_" + lang + ",product_name_" + lang;
            client.getExistingProductDetails(product.getCode(), fields, Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH))
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
                                            ((AddProductActivity) activity).setIngredients("set", state.getProduct().getIngredientsText(lang));
                                        }
                                    }
                                } else {
                                    name.setText(null);
                                    if (activity instanceof AddProductActivity) {
                                        ((AddProductActivity) activity).setIngredients("set", null);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @OnClick(R.id.btn_next)
    void next() {
        if (!areRequiredFieldsEmpty() && activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).proceed();
        }
    }

    @OnClick(R.id.btnAddImageFront)
    void addFrontImage() {
        if (mImageUrl != null) {
            // front image is already added. Open full screen image.
            Intent intent = new Intent(getActivity(), FullScreenImage.class);
            Bundle bundle = new Bundle();
            if (edit_product && !newImageSelected) {
                bundle.putString("imageurl", mImageUrl);
            } else {
                bundle.putString("imageurl", "file://" + mImageUrl);
            }
            intent.putExtras(bundle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(activity, imageFront,
                                activity.getString(R.string.product_transition));
                startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }
        } else {
            // add front image.
            frontImage = true;
            if (ContextCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                EasyImage.openCamera(this, 0);
            }
        }
    }

    @OnLongClick(R.id.btnAddImageFront)
    boolean newFrontImage() {
        // add front image.
        frontImage = true;
        if (ContextCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            EasyImage.openCamera(this, 0);
        }
        return true;
    }

    @OnClick(R.id.btn_other_pictures)
    void addOtherImage() {
        frontImage = false;
        if (ContextCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            EasyImage.openCamera(this, 0);
        }
    }

    /**
     * adds all the fields to the query map even those which are null or empty.
     */
    public void getAllDetails() {
        chipifyAllUnterminatedTokens();
        if (activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).addToMap(PARAM_BARCODE, code);
            ((AddProductActivity) activity).addToMap(PARAM_LANGUAGE, languageCode);
            ((AddProductActivity) activity).addToMap(PARAM_INTERFACE_LANGUAGE, appLanguageCode);
            String lc = (!languageCode.isEmpty()) ? languageCode : "en";
            ((AddProductActivity) activity).addToMap(PARAM_NAME + "_" + lc, name.getText().toString());
            ((AddProductActivity) activity).addToMap(PARAM_QUANTITY, quantity.getText().toString());
            ((AddProductActivity) activity).addToMap(PARAM_BRAND.substring(4), getValues(brand));
            ((AddProductActivity) activity).addToMap(PARAM_PACKAGING.substring(4), getValues(packaging));
            ((AddProductActivity) activity).addToMap(PARAM_CATEGORIES.substring(4), getValues(categories));
            ((AddProductActivity) activity).addToMap(PARAM_LABELS.substring(4), getValues(label));
            if (BuildConfig.FLAVOR.equals("obf")) {
                ((AddProductActivity) activity).addToMap(PARAM_PERIODS_AFTER_OPENING, periodsAfterOpening.getText().toString());
            }
            if(mImageUrl!=null){
                ((AddProductActivity) activity).addToMap("imageUrl", mImageUrl);
            }
            Boolean cbEatingChecked = cbEating.isChecked();
            ((AddProductActivity) activity).addToMap(PARAM_EATING, cbEatingChecked.toString());
            ((AddProductActivity) activity).addToMap(PARAM_ORIGIN.substring(4), getValues(originOfIngredients));
            ((AddProductActivity) activity).addToMap(PARAM_MANUFACTURING_PLACE.substring(4), manufacturingPlace.getText().toString());
            ((AddProductActivity) activity).addToMap(PARAM_EMB_CODE.substring(4), getValues(embCode));
            ((AddProductActivity) activity).addToMap(PARAM_LINK, link.getText().toString());
            ((AddProductActivity) activity).addToMap(PARAM_PURCHASE.substring(4), getValues(countryWherePurchased));
            ((AddProductActivity) activity).addToMap(PARAM_STORE.substring(4), getValues(stores));
            ((AddProductActivity) activity).addToMap(PARAM_COUNTRIES.substring(4), getValues(countriesWhereSold));
            //((AddProductActivity) activity).addToMap(PARAM_OTHER_INFORMATION, otherInfo.getText().toString());
            //((AddProductActivity) activity).addToMap(PARAM_CONSERVATION_CONDITIONS, conservationCond.getText().toString());
            //((AddProductActivity) activity).addToMap(PARAM_RECYCLING_INSTRUCTION_TO_DISCARD, recyclingInstructionToDiscard.getText().toString());
            //((AddProductActivity) activity).addToMap(PARAM_RECYCLING_INSTRUCTION_TO_RECYCLE, recyclingInstructionToRecycle.getText().toString());
        }

    }

    /**
     * adds only those fields to the query map which are not empty.
     */
    public void getDetails() {
        chipifyAllUnterminatedTokens();
        if (activity instanceof AddProductActivity) {
            if (!code.isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_BARCODE, code);
            }
            if (!appLanguageCode.isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_INTERFACE_LANGUAGE, appLanguageCode);
            }
            if (!languageCode.isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_LANGUAGE, languageCode);
            }
            if (!name.getText().toString().isEmpty()) {
                String lc = (!languageCode.isEmpty()) ? languageCode : "en";
                ((AddProductActivity) activity).addToMap(PARAM_NAME + "_" + lc, name.getText().toString());
            }
            if (!quantity.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_QUANTITY, quantity.getText().toString());
            }
            if (!brand.getChipValues().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_BRAND, getValues(brand));
            }
            if (!packaging.getChipValues().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_PACKAGING, getValues(packaging));
            }
            if (!categories.getChipValues().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_CATEGORIES, getValues(categories));
            }
            if (!label.getChipValues().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_LABELS, getValues(label));
            }
            if (!periodsAfterOpening.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_PERIODS_AFTER_OPENING, periodsAfterOpening.getText().toString());
            }
            if(mImageUrl!=null){
                ((AddProductActivity) activity).addToMap("imageUrl", mImageUrl);
            }
            Boolean cbEatingChecked = cbEating.isChecked();
            ((AddProductActivity) activity).addToMap(PARAM_EATING, cbEatingChecked.toString());
            if (!originOfIngredients.getChipValues().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_ORIGIN, getValues(originOfIngredients));
            }
            if (!manufacturingPlace.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_MANUFACTURING_PLACE, manufacturingPlace.getText().toString());
            }
            if (!embCode.getChipValues().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_EMB_CODE, getValues(embCode));
            }
            if (!link.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_LINK, link.getText().toString());
            }
            if (!countryWherePurchased.getChipValues().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_PURCHASE, getValues(countryWherePurchased));
            }
            if (!stores.getChipValues().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_STORE, getValues(stores));
            }
            if (!countriesWhereSold.getChipValues().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_COUNTRIES, getValues(countriesWhereSold));
            }
            /*if (!otherInfo.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_OTHER_INFORMATION, otherInfo.getText().toString());
            }
            if (!conservationCond.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_CONSERVATION_CONDITIONS, conservationCond.getText().toString());
            }
            if (!recyclingInstructionToRecycle.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_RECYCLING_INSTRUCTION_TO_RECYCLE, recyclingInstructionToRecycle.getText().toString());
            }
            if (!recyclingInstructionToDiscard.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_RECYCLING_INSTRUCTION_TO_DISCARD, recyclingInstructionToDiscard.getText().toString());
            }*/
        }
    }

    /**
     * Chipifies all existing plain text in all the NachoTextViews.
     */
    private void chipifyAllUnterminatedTokens() {
        NachoTextView nachoTextViews[] = {brand, packaging, categories, label, originOfIngredients, embCode, countryWherePurchased, stores, countriesWhereSold};
        for (NachoTextView nachoTextView : nachoTextViews) {
            nachoTextView.chipifyAllUnterminatedTokens();
        }
    }

    private String getValues(NachoTextView nachoTextView) {
        List<String> list = nachoTextView.getChipValues();
        return StringUtils.join(list, ',');
    }

    @OnClick(R.id.section_manufacturing_details)
    void toggleManufacturingSectionVisibility() {
        if (manufacturingPlace.getVisibility() != View.VISIBLE) {
            originOfIngredients.setVisibility(View.VISIBLE);
            manufacturingPlace.setVisibility(View.VISIBLE);
            embCode.setVisibility(View.VISIBLE);
            link.setVisibility(View.VISIBLE);
            originOfIngredients.requestFocus();
            sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up_grey_24dp, 0);
        } else {
            originOfIngredients.setVisibility(View.GONE);
            manufacturingPlace.setVisibility(View.GONE);
            embCode.setVisibility(View.GONE);
            link.setVisibility(View.GONE);
            sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
        }
    }

    @OnClick(R.id.section_purchasing_details)
    void togglePurchasingSectionVisibility() {
        if (stores.getVisibility() != View.VISIBLE) {
            countryWherePurchased.setVisibility(View.VISIBLE);
            stores.setVisibility(View.VISIBLE);
            countriesWhereSold.setVisibility(View.VISIBLE);
            countryWherePurchased.requestFocus();
            sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up_grey_24dp, 0);
        } else {
            countryWherePurchased.setVisibility(View.GONE);
            stores.setVisibility(View.GONE);
            countriesWhereSold.setVisibility(View.GONE);
            sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0);
        }
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
            url = url + " " + StringUtils.join(brandNames, ' ');
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
                        ((AddProductActivity) activity).setIngredients("set", null);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                photoFile = new File((resultUri.getPath()));
                ProductImage image;
                int position;
                if (frontImage) {
                    image = new ProductImage(code, FRONT, photoFile);
                    mImageUrl = photoFile.getAbsolutePath();
                    newImageSelected = true;
                    position = 0;
                } else {
                    image = new ProductImage(code, OTHER, photoFile);
                    position = 3;
                }
                image.setFilePath(resultUri.getPath());
                if (activity instanceof AddProductActivity) {
                    ((AddProductActivity) activity).addToPhotoMap(image, position);
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e("Crop image error", result.getError().toString());
            }
        } else if (requestCode == INTENT_INTEGRATOR_REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
            if (result.getContents() != null) {
                link.setText(result.getContents());
                link.requestFocus();
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
                        .start(activity.getApplicationContext(), AddProductOverviewFragment.this);
            }
        });
    }

    public void showImageProgress() {
        imageProgress.setVisibility(View.VISIBLE);
        imageProgressText.setVisibility(View.VISIBLE);
        imageFront.setVisibility(View.INVISIBLE);

    }

    public void hideImageProgress(boolean errorInUploading, String message) {
        imageProgress.setVisibility(View.GONE);
        imageProgressText.setVisibility(View.GONE);
        imageFront.setVisibility(View.VISIBLE);
        if (!errorInUploading) {
            Picasso.with(activity)
                    .load(photoFile)
                    .resize(dpsToPixels(), dpsToPixels())
                    .centerInside()
                    .into(imageFront);
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        }
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

    private int dpsToPixels() {
        // converts 50dp to equivalent pixels.
        final float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (50 * scale + 0.5f);
    }
}