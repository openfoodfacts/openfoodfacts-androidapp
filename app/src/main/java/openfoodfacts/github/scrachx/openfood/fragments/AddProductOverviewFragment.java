package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.text.WordUtils;
import org.greenrobot.greendao.async.AsyncSession;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.CategoryNameDao;
import openfoodfacts.github.scrachx.openfood.models.CountryName;
import openfoodfacts.github.scrachx.openfood.models.CountryNameDao;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.LabelNameDao;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;

public class AddProductOverviewFragment extends BaseFragment {

    private static final String PARAM_NAME = "product_name";
    private static final String PARAM_BARCODE = "code";
    private static final String PARAM_QUANTITY = "quantity";
    private static final String UNIT[] = {"", "g", "mg", "kg", "l", "ml", "cl", "fl oz"};
    private static final String PARAM_BRAND = "brands";
    private static final String PARAM_LANGUAGE = "lang";
    private static final String PARAM_PACKAGING = "packaging";
    private static final String PARAM_CATEGORIES = "categories";
    private static final String PARAM_LABELS = "labels";
    private static final String PARAM_ORIGIN = "origins";
    private static final String PARAM_MANUFACTURING_PLACE = "manufacturing_places";
    private static final String PARAM_EMB_CODE = "emb_codes";
    private static final String PARAM_LINK = "link";
    private static final String PARAM_PURCHASE = "purchase_places";
    private static final String PARAM_STORE = "stores";
    private static final String PARAM_COUNTRIES = "countries";

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
    EditText brand;
    @BindView(R.id.packaging)
    EditText packaging;
    @BindView(R.id.categories)
    AutoCompleteTextView categories;
    @BindView(R.id.label)
    AutoCompleteTextView label;
    @BindView(R.id.origin_of_ingredients)
    AutoCompleteTextView originOfIngredients;
    @BindView(R.id.manufacturing_place)
    EditText manufacturingPlace;
    @BindView(R.id.emb_code)
    EditText embCode;
    @BindView(R.id.link)
    EditText link;
    @BindView(R.id.country_where_purchased)
    AutoCompleteTextView countryWherePurchased;
    @BindView(R.id.stores)
    EditText stores;
    @BindView(R.id.countries_where_sold)
    AutoCompleteTextView countriesWhereSold;
    private String languageCode;
    private Activity activity;
    private Product mProduct;
    private String code;
    private String mImageUrl;
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
            mProduct = (Product) b.getSerializable("product");
            String currentLang = LocaleHelper.getLanguage(activity);
            setProductLanguage(currentLang);
            barcode.setText(R.string.txtBarcode);
            code = mProduct.getCode();
            barcode.append(" " + code);
        } else {
            Toast.makeText(activity, "Something went wrong while trying to add product details", Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        loadAutoSuggestions();
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
            originOfIngredients.setAdapter(adapter);
            countryWherePurchased.setAdapter(adapter);
            countriesWhereSold.setAdapter(adapter);
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
    }

    private void setProductLanguage(String lang) {
        languageCode = lang;
        Locale current = LocaleHelper.getLocale(lang);
        language.setText("Product language : ");
        language.append(WordUtils.capitalize(current.getDisplayName(current)));
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
        if (ContextCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            EasyImage.openCamera(this, 0);
        }
    }

    public void getDetails() {
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
            if (!brand.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_BRAND, brand.getText().toString());
            }
            if (!packaging.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_PACKAGING, packaging.getText().toString());
            }
            if (!categories.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_CATEGORIES, categories.getText().toString());
            }
            if (!label.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_LABELS, label.getText().toString());
            }
            if (!originOfIngredients.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_ORIGIN, originOfIngredients.getText().toString());
            }
            if (!manufacturingPlace.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_MANUFACTURING_PLACE, manufacturingPlace.getText().toString());
            }
            if (!embCode.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_EMB_CODE, embCode.getText().toString());
            }
            if (!link.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_LINK, link.getText().toString());
            }
            if (!countryWherePurchased.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_PURCHASE, countryWherePurchased.getText().toString());
            }
            if (!stores.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_STORE, stores.getText().toString());
            }
            if (!countriesWhereSold.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_COUNTRIES, countriesWhereSold.getText().toString());
            }
        }
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
                    return true;
                })
                .positiveText(R.string.ok_button)
                .show();
    }

    public boolean areRequiredFieldsEmpty() {
        if (mImageUrl == null || mImageUrl.equals("")) {
            Toast.makeText(activity, R.string.txtPictureNeededDialogContent, Toast.LENGTH_SHORT).show();
            scrollView.fullScroll(View.FOCUS_UP);
        } else if (name.getText().toString().isEmpty()) {
            name.setError("This field is required");
            name.requestFocus();
        } else if (quantity.getText().toString().isEmpty()) {
            quantity.setError("This field is required");
            quantity.requestFocus();
        } else if (brand.getText().toString().isEmpty()) {
            brand.setError("This field is required");
            brand.requestFocus();
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                photoFile = new File((resultUri.getPath()));
                ProductImage image = new ProductImage(code, FRONT, photoFile);
                image.setFilePath(resultUri.getPath());
                mImageUrl = photoFile.getAbsolutePath();
                if (activity instanceof AddProductActivity) {
                    ((AddProductActivity) activity).addToPhotoMap(image, 0);
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
                CropImage.activity(Uri.fromFile(imageFiles.get(0))).setAllowFlipping(false)
                        .setOutputUri(Utils.getOutputPicUri(getContext())).start(activity.getApplicationContext(), AddProductOverviewFragment.this);
            }
        });
    }

    public void showImageProgress() {
        imageProgress.setVisibility(View.VISIBLE);
        imageProgressText.setVisibility(View.VISIBLE);
        imageFront.setVisibility(View.INVISIBLE);

    }

    public void hideImageProgress(boolean errorInUploading) {
        imageProgress.setVisibility(View.GONE);
        imageProgressText.setVisibility(View.GONE);
        imageFront.setVisibility(View.VISIBLE);
        if (!errorInUploading) {
            Picasso.with(activity)
                    .load(photoFile)
                    .into(imageFront);
            Toast.makeText(activity, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "You seem offline, images will be uploaded when network is available", Toast.LENGTH_SHORT).show();
        }
    }
}
