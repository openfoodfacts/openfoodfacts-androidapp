package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.validator.ChipifyingNachoValidator;
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
    private static final String UNIT[] = {"", "g", "mg", "kg", "l", "ml", "cl", "fl oz"};
    private static final String PARAM_BRAND = "add_brands";
    private static final String PARAM_LANGUAGE = "lang";
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
    @BindView(R.id.spinner_weight_unit)
    Spinner quantityUnit;
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
    @BindView(R.id.origin_of_ingredients)
    NachoTextView originOfIngredients;
    @BindView(R.id.manufacturing_place)
    EditText manufacturingPlace;
    @BindView(R.id.emb_code)
    NachoTextView embCode;
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
    private Activity activity;
    private OfflineSavedProduct mOfflineSavedProduct;
    private String code;
    private String mImageUrl;
    private boolean frontImage;
    private File photoFile;
    private List<String> countries = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private List<String> category = new ArrayList<>();

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
        Bundle b = getArguments();
        if (b != null) {
            Product product = (Product) b.getSerializable("product");
            mOfflineSavedProduct = (OfflineSavedProduct) b.getSerializable("edit_offline_product");
            String currentLang = LocaleHelper.getLanguage(activity);
            setProductLanguage(currentLang);
            barcode.setText(R.string.txtBarcode);
            if (product != null) {
                code = product.getCode();
            }
            if (mOfflineSavedProduct != null) {
                code = mOfflineSavedProduct.getBarcode();
                preFillValues();
            }
            barcode.append(" " + code);
            if (BuildConfig.FLAVOR.equals("obf") || BuildConfig.FLAVOR.equals("opf")) {
                otherImage.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(activity, "Something went wrong while trying to add product details", Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        link.setHint(Html.fromHtml("<small><small>" +
                "Link of the official page of the product" + "</small></small>"));
        initializeChips();
        loadAutoSuggestions();
    }

    /**
     * Pre fill the fields if the product is already present in SavedProductOffline db.
     */
    private void preFillValues() {
        HashMap<String, String> productDetails = mOfflineSavedProduct.getProductDetailsMap();
        if (productDetails != null) {
            if (productDetails.get("image_front") != null) {
                mImageUrl = productDetails.get("image_front");
                Picasso.with(getContext())
                        .load("file://" + mImageUrl)
                        .into(imageFront);
            }
            if (productDetails.get(PARAM_LANGUAGE) != null) {
                setProductLanguage(productDetails.get(PARAM_LANGUAGE));
            }
            if (productDetails.get(PARAM_NAME) != null) {
                name.setText(productDetails.get(PARAM_NAME));
            }
            if (productDetails.get(PARAM_QUANTITY) != null) {
                String qty = productDetails.get(PARAM_QUANTITY);
                // Splits the quantity into value and unit. Example: "250g" into "250" and "g"
                String part[] = qty.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                quantity.setText(part[0]);
                if (part.length > 1) {
                    // quantity has the unit part
                    for (int i = 0; i < UNIT.length; i++) {
                        // find index where the UNIT array has the identified unit
                        if (UNIT[i].equals(part[1])) {
                            quantityUnit.setSelection(i);
                        }
                    }
                }
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
                .where(CountryNameDao.Properties.LanguageCode.eq(languageCode))
                .orderDesc(CountryNameDao.Properties.Name).build());
        asyncSessionLabels.queryList(labelNameDao.queryBuilder()
                .where(LabelNameDao.Properties.LanguageCode.eq(languageCode))
                .orderDesc(LabelNameDao.Properties.Name).build());
        asyncSessionCategories.queryList(categoryNameDao.queryBuilder()
                .where(CategoryNameDao.Properties.LanguageCode.eq(languageCode))
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
        language.setText("Product language : ");
        language.append(WordUtils.capitalize(current.getDisplayName(current)));
        if (activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).addToMap(PARAM_LANGUAGE, languageCode);
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
            bundle.putString("imageurl", "file://" + mImageUrl);
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

    @OnClick(R.id.btn_other_pictures)
    void addOtherImage() {
        frontImage = false;
        if (ContextCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            EasyImage.openCamera(this, 0);
        }
    }

    public void getDetails() {
        chipifyAllUnterminatedTokens();
        if (activity instanceof AddProductActivity) {
            if (!code.isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_BARCODE, code);
            }
            if (!languageCode.isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_LANGUAGE, languageCode);
            }
            if (!name.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_NAME, name.getText().toString());
            }
            if (!quantity.getText().toString().isEmpty()) {
                String qty = quantity.getText().toString() + UNIT[quantityUnit.getSelectedItemPosition()];
                ((AddProductActivity) activity).addToMap(PARAM_QUANTITY, qty);
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
                .content("Examples: EMB 53062, FR 62.448.034 CE, 84 R 20, 33 RECOLTANT 522, FSSL 10013011001409")
                .positiveText(R.string.ok_button)
                .show();
    }

    @OnClick(R.id.hint_link)
    void searchProductLink() {
        String url = "https://www.google.com/search?q=" + code;
        if (!brand.getText().toString().isEmpty()) {
            url = url + " " + brand.getText().toString();
        }
        if (!name.getText().toString().isEmpty()) {
            url = url + " " + name.getText().toString();
        }
        url = url + " " + "official website";
        CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(activity.getBaseContext(), null);
        CustomTabActivityHelper.openCustomTab(activity, customTabsIntent, Uri.parse(url), new WebViewFallback());
    }

    @OnClick(R.id.hint_link_2)
    void scanProductLink() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setRequestCode(INTENT_INTEGRATOR_REQUEST_CODE);
        integrator.setPrompt("Scan QR Code for product website");
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
                    setProductLanguage(finalLocalValues.get(which));
                    loadAutoSuggestions();
                    if (activity instanceof AddProductActivity) {
                        ((AddProductActivity) activity).loadAutoSuggestion();
                    }
                    return true;
                })
                .positiveText(R.string.ok_button)
                .show();
    }

    public boolean areRequiredFieldsEmpty() {
        if (mImageUrl == null || mImageUrl.equals("")) {
            Toast.makeText(activity, "Please add at least one picture of this product before proceeding", Toast.LENGTH_SHORT).show();
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
            otherImageProgressText.setText("Image uploaded successfully");
        }
    }
}
