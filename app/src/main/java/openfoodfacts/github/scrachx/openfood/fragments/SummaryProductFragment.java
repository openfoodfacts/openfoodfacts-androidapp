package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.CountryName;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevels;
import openfoodfacts.github.scrachx.openfood.models.NutrimentLevel;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.models.Tag;
import openfoodfacts.github.scrachx.openfood.models.TagDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.OTHER;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SummaryProductFragment extends BaseFragment implements CustomTabActivityHelper.ConnectionCallback {

    @BindView(R.id.product_incomplete_warning_view_container)
    CardView productIncompleteView;
    @BindView(R.id.textNameProduct)
    TextView nameProduct;
    @BindView(R.id.textGenericNameProduct)
    TextView genericNameProduct;
    @BindView(R.id.textBarcodeProduct)
    TextView barCodeProduct;
    @BindView(R.id.textQuantityProduct)
    TextView quantityProduct;
    @BindView(R.id.textPackagingProduct)
    TextView packagingProduct;
    @BindView(R.id.textBrandProduct)
    TextView brandProduct;
    @BindView(R.id.textManufacturingProduct)
    TextView manufacturingProduct;
    @BindView(R.id.textIngredientsOriginProduct)
    TextView ingredientsOrigin;
    @BindView(R.id.textEmbCode)
    TextView embCode;
    @BindView(R.id.textManufactureUrl)
    TextView manufactureUlrProduct;
    @BindView(R.id.textStoreProduct)
    TextView storeProduct;
    @BindView(R.id.textCountryProduct)
    TextView countryProduct;
    @BindView(R.id.textCategoryProduct)
    TextView categoryProduct;
    @BindView(R.id.textLabelProduct)
    TextView labelProduct;
    @BindView(R.id.imageViewFront)
    ImageView mImageFront;
    @BindView(R.id.addPhotoLabel)
    TextView addPhotoLabel;
    @BindView(R.id.buttonMorePictures)
    Button addMorePicture;
    @BindView(R.id.imageGrade)
    ImageView img;
    private OpenFoodAPIClient api;
    private String mUrlImage;
    private String barcode;
    private boolean sendOther = false;
    private CustomTabsIntent customTabsIntent;
    private CustomTabActivityHelper customTabActivityHelper;
    private Uri nutritionScoreUri;
    private Uri embCodeUri;
    private TagDao mTagDao;
    private SummaryProductFragment mFragment;
    private IProductRepository productRepository;
    private Uri manufactureUri;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
        productRepository = ProductRepository.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        api = new OpenFoodAPIClient(getActivity());
        mFragment = this;
        return createView(inflater, container, R.layout.fragment_summary_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        final State state = (State) intent.getExtras().getSerializable("state");

        final Product product = state.getProduct();

        List<AllergenName> mAllergens = productRepository.getAllergensByEnabledAndLanguageCode(true, Locale.getDefault().getLanguage());

        List<String> allergens = product.getAllergensHierarchy();
        List<String> traces = product.getTracesTags();
        allergens.addAll(traces);
        if (!mAllergens.isEmpty() && product.getStatesTags().get(0).contains("to-be-completed")) {
            productIncompleteView.setVisibility(View.VISIBLE);
        }

        List<String> matchAll = new ArrayList<>();
        for (int a = 0; a < mAllergens.size(); a++) {
            for (int i = 0; i < allergens.size(); i++) {

                if (allergens.get(i).trim().equals(mAllergens.get(a).getAllergenTag().trim())) {
                    matchAll.add(mAllergens.get(a).getName());
                }
            }
        }

        /**
         * shows the dialog if allergen is found.
         */
        if (matchAll.size() > 0) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.warning_allergens)
                    .items(matchAll)
                    .neutralText(R.string.txtOk)
                    .titleColorRes(R.color.red_500)
                    .dividerColorRes(R.color.indigo_900)
                    .icon(new IconicsDrawable(getActivity())
                            .icon(GoogleMaterial.Icon.gmd_warning)
                            .color(Color.RED)
                            .sizeDp(24))
                    .show();
        }


        mTagDao = Utils.getAppDaoSession(getActivity()).getTagDao();
        barcode = product.getCode();

        if (isNotBlank(product.getImageUrl())) {
            addPhotoLabel.setVisibility(View.GONE);

            Picasso.with(view.getContext())
                    .load(product.getImageUrl())
                    .into(mImageFront);

            mUrlImage = product.getImageUrl();
        }

        //TODO use OpenFoodApiService to fetch product by packaging, brands, categories etc

        if (isNotBlank(product.getProductName())) {
            nameProduct.setText(product.getProductName());
        } else {
            nameProduct.setVisibility(View.GONE);
        }
        if (isNotBlank(product.getGenericName())) {
            genericNameProduct.setText(bold(getString(R.string.txtGenericName)));
            genericNameProduct.append(' ' + product.getGenericName());
        } else {
            genericNameProduct.setVisibility(View.GONE);
        }
        if (isNotBlank(barcode)) {
            barCodeProduct.setText(bold(getString(R.string.txtBarcode)));
            barCodeProduct.append(' ' + barcode);
        } else {
            barCodeProduct.setVisibility(View.GONE);
        }
        if (isNotBlank(product.getQuantity())) {
            quantityProduct.setText(bold(getString(R.string.txtQuantity)));
            quantityProduct.append(' ' + product.getQuantity());
        } else {
            quantityProduct.setVisibility(View.GONE);
        }
        if (isNotBlank(product.getPackaging())) {
            packagingProduct.setClickable(true);
            packagingProduct.setMovementMethod(LinkMovementMethod.getInstance());
            packagingProduct.setText(bold(getString(R.string.txtPackaging)));
            packagingProduct.append(" ");
            String[] packagings = product.getPackaging().split(",");

            for (int i = 0; i < packagings.length - 1; i++) {
                packagingProduct.append(Utils.getClickableText(packagings[i], "", SearchType.PACKAGING, getActivity(), customTabsIntent));
                packagingProduct.append(", ");
            }

            packagingProduct.append(Utils.getClickableText(packagings[packagings.length - 1], "", SearchType.PACKAGING, getActivity(), customTabsIntent));
        } else {
            packagingProduct.setVisibility(View.GONE);
        }
        if (isNotBlank(product.getBrands())) {
            brandProduct.setClickable(true);
            brandProduct.setMovementMethod(LinkMovementMethod.getInstance());
            brandProduct.setText(bold(getString(R.string.txtBrands)));
            brandProduct.append(" ");

            String[] brands = product.getBrands().split(",");
            for (int i = 0; i < brands.length - 1; i++) {
                brandProduct.append(Utils.getClickableText(brands[i], "", SearchType.BRAND, getActivity(), customTabsIntent));
                brandProduct.append(", ");
            }
            brandProduct.append(Utils.getClickableText(brands[brands.length - 1], "", SearchType.BRAND, getActivity(), customTabsIntent));
        } else {
            brandProduct.setVisibility(View.GONE);
        }
        if (isNotBlank(product.getManufacturingPlaces())) {
            manufacturingProduct.setText(bold(getString(R.string.txtManufacturing)));
            manufacturingProduct.append(' ' + product.getManufacturingPlaces());
        } else {
            manufacturingProduct.setVisibility(View.GONE);
        }

        if (isBlank(product.getOrigins())) {
            ingredientsOrigin.setVisibility(View.GONE);
        } else {
            ingredientsOrigin.setText(bold(getString(R.string.txtIngredientsOrigins)));
            ingredientsOrigin.append(' ' + product.getOrigins());
        }

        List<String> tags = product.getCategoriesTags();
        if (tags != null && !tags.isEmpty()) {
            categoryProduct.setMovementMethod(LinkMovementMethod.getInstance());
            categoryProduct.setText(bold(getString(R.string.txtCategories)));
            categoryProduct.append(" ");
            categoryProduct.setClickable(true);
            categoryProduct.setMovementMethod(LinkMovementMethod.getInstance());
            CategoryName categoryName;
            String languageCode = Locale.getDefault().getLanguage();
            List<String> categories = new ArrayList<>();
            for (String tag : product.getCategoriesTags()) {
                categoryName = productRepository.getCategoryByTagAndLanguageCode(tag, languageCode);
                if (categoryName == null) {
                    categoryName = productRepository.getCategoryByTagAndDefaultLanguageCode(tag);
                }

                if (categoryName != null) {
                    categories.add(categoryName.getName());
                }
            }

            if (categories.isEmpty()) {
                categoryProduct.setVisibility(View.GONE);
            } else {
                String category;
                for (int i = 0; i < categories.size() - 1; i++) {
                    category = categories.get(i);
                    categoryProduct.append(Utils.getClickableText(category, category, SearchType.CATEGORY, getActivity(), customTabsIntent));
                    categoryProduct.append(", ");
                }

                category = categories.get(categories.size() - 1);
                categoryProduct.append(Utils.getClickableText(category, category, SearchType.CATEGORY, getActivity(), customTabsIntent));
            }
        } else {
            categoryProduct.setVisibility(View.GONE);
        }

        List<String> labelsTags = product.getLabelsTags();
        if (labelsTags != null && !labelsTags.isEmpty()) {
            labelProduct.append(bold(getString(R.string.txtLabels)));
            labelProduct.setClickable(true);
            labelProduct.setMovementMethod(LinkMovementMethod.getInstance());
            labelProduct.append(" ");

            String labelTag;
            String languageCode = Locale.getDefault().getLanguage();
            List<String> labels = new ArrayList<>();
            for (int i = 0; i < labelsTags.size(); i++) {
                labelTag = labelsTags.get(i);
                LabelName label = productRepository.getLabelByTagAndLanguageCode(labelTag, languageCode);
                if (label == null) {
                    label = productRepository.getLabelByTagAndDefaultLanguageCode(labelTag);
                    if (label == null) {
                        if (product.getLabelsHierarchy().size() > i) {
                            label = new LabelName(product.getLabelsHierarchy().get(i).replaceAll("(en:|fr:)", ""));
                        }
                    }
                }

                if (label != null) {
                    labels.add(label.getName());
                }
            }

            String label;
            for (int i = 0; i < labels.size() - 1; i++) {
                label = labels.get(i);
                labelProduct.append(Utils.getClickableText(label, label, SearchType.LABEL, getActivity(), customTabsIntent));
                labelProduct.append(", ");
            }

            label = labels.get(labels.size() - 1);
            labelProduct.append(Utils.getClickableText(label, label, SearchType.LABEL, getActivity(), customTabsIntent));
        } else {
            labelProduct.setVisibility(View.GONE);
        }

        if (product.getEmbTags() != null && !product.getEmbTags().toString().trim().equals("[]")) {
            embCode.setMovementMethod(LinkMovementMethod.getInstance());
            embCode.setText(bold(getString(R.string.txtEMB)));
            embCode.append(" ");

            String embTag;
            String[] embTags = product.getEmbTags().toString().replace("[", "").replace("]", "").split(", ");
            for (int i = 0; i < embTags.length - 1; i++) {
                embTag = embTags[i];
                embCode.append(Utils.getClickableText(getEmbCode(embTag), getEmbUrl(embTag), SearchType.EMB, getActivity(), customTabsIntent));
                embCode.append(", ");
            }

            embTag = embTags[embTags.length - 1];
            embCode.append(Utils.getClickableText(getEmbCode(embTag), getEmbUrl(embTag), SearchType.EMB, getActivity(), customTabsIntent));
        } else {
            embCode.setVisibility(View.GONE);
        }

        if (isNotBlank(product.getStores())) {
            storeProduct.setMovementMethod(LinkMovementMethod.getInstance());
            storeProduct.setText(bold(getString(R.string.txtStores)));
            storeProduct.append(" ");

            String store;
            String stores[] = product.getStores().split(",");
            for (int i = 0; i < stores.length - 1; i++) {
                store = stores[i];
                storeProduct.append(Utils.getClickableText(store, store, SearchType.STORE, getActivity(), customTabsIntent));
                storeProduct.append(", ");
            }

            store = stores[stores.length - 1];
            storeProduct.append(Utils.getClickableText(store, store, SearchType.STORE, getActivity(), customTabsIntent));
        } else {
            storeProduct.setVisibility(View.GONE);
        }
        if (isNotBlank(product.getManufactureUrl())) {
            manufactureUri = Uri.parse(product.getManufactureUrl());
            if (manufactureUri.getScheme() == null)
                manufactureUri = Uri.parse("http://" + product.getManufactureUrl());
            customTabActivityHelper.mayLaunchUrl(manufactureUri, null, null);

            String manufactureUrlTitle = getString(R.string.txtManufactureUrl);
            SpannableString spannableText = new SpannableString(manufactureUrlTitle + "\n" + product.getManufactureUrl());

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    CustomTabActivityHelper.openCustomTab(getActivity(), customTabsIntent, manufactureUri, new WebViewFallback());
                }
            };

            spannableText.setSpan(clickableSpan, manufactureUrlTitle.length() + 1, spannableText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new StyleSpan(Typeface.BOLD), 0, manufactureUrlTitle.length(), 0);

            manufactureUlrProduct.setText(spannableText);
            manufactureUlrProduct.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            manufactureUlrProduct.setVisibility(View.GONE);
        }

        List<String> countryTags = product.getCountriesTags();
        if (countryTags != null && !countryTags.isEmpty()) {
            countryProduct.setText(bold(getString(R.string.txtCountries)));
            countryProduct.setClickable(true);
            countryProduct.setMovementMethod(LinkMovementMethod.getInstance());
            countryProduct.append(" ");

            String countryTag;
            String languageCode = Locale.getDefault().getLanguage();
            String defaultCountries[] = product.getCountries().replaceAll("(en:|fr:)", "").split(",");
            List<String> countries = new ArrayList<>();
            for (int i = 0; i < countryTags.size(); i++) {
                countryTag = countryTags.get(i);
                CountryName countryName = productRepository.getCountryByTagAndLanguageCode(countryTag, languageCode);
                if (countryName == null) {
                    countryName = productRepository.getCountryByTagAndDefaultLanguageCode(countryTag);
                    if (countryName == null) {
                        if (defaultCountries.length > i) {
                            countryName = new CountryName(defaultCountries[i]);
                        }
                    }
                }

                if (countryName != null) {
                    countries.add(countryName.getName());
                }
            }

            for (int i = 0; i < countries.size() - 1; i++) {
                countryProduct.append(Utils.getClickableText(StringUtils.capitalize(countries.get(i)), "", SearchType.COUNTRY, getActivity(), customTabsIntent));
                countryProduct.append(", ");
            }

            countryProduct.append(Utils.getClickableText(StringUtils.capitalize(countries.get(countries.size() - 1)), "", SearchType.COUNTRY, getActivity(), customTabsIntent));
            ;
        } else {
            countryProduct.setVisibility(View.GONE);
        }

        // if the device does not have a camera, hide the button
        try {
            if (!Utils.isHardwareCameraInstalled(getContext())) {
                addMorePicture.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), e.toString());
        }

        if (BuildConfig.FLAVOR.equals("off")) {
            List<NutrientLevelItem> levelItem = new ArrayList<>();
            Nutriments nutriments = product.getNutriments();

            NutrientLevels nutrientLevels = product.getNutrientLevels();
            NutrimentLevel fat = null;
            NutrimentLevel saturatedFat = null;
            NutrimentLevel sugars = null;
            NutrimentLevel salt = null;
            if (nutrientLevels != null) {
                fat = nutrientLevels.getFat();
                saturatedFat = nutrientLevels.getSaturatedFat();
                sugars = nutrientLevels.getSugars();
                salt = nutrientLevels.getSalt();
            }

            if (fat == null && salt == null && saturatedFat == null && sugars == null) {
                levelItem.add(new NutrientLevelItem(getString(R.string.txtNoData), "", "", R.drawable.error_image));
            } else {
                // prefetch the uri
                // currently only available in french translations
                nutritionScoreUri = Uri.parse("https://fr.openfoodfacts.org/score-nutritionnel-france");
                customTabActivityHelper.mayLaunchUrl(nutritionScoreUri, null, null);

                Context context = this.getContext();

                if (fat != null) {
                    String fatNutrimentLevel = fat.getLocalize(context);
                    Nutriments.Nutriment nutriment = nutriments.get(Nutriments.FAT);
                    levelItem.add(new NutrientLevelItem(getString(R.string.txtFat), getRoundNumber(nutriment.getFor100g()) + " " + nutriment.getUnit(), fatNutrimentLevel, fat.getImageLevel()));
                }

                if (saturatedFat != null) {
                    String saturatedFatLocalize = saturatedFat.getLocalize(context);
                    Nutriments.Nutriment nutriment = nutriments.get(Nutriments.SATURATED_FAT);
                    String saturatedFatValue = getRoundNumber(nutriment.getFor100g()) + " " + nutriment.getUnit();
                    levelItem.add(new NutrientLevelItem(getString(R.string.txtSaturatedFat), saturatedFatValue, saturatedFatLocalize, saturatedFat.getImageLevel()));
                }

                if (sugars != null) {
                    String sugarsLocalize = sugars.getLocalize(context);
                    Nutriments.Nutriment nutriment = nutriments.get(Nutriments.SUGARS);
                    String sugarsValue = getRoundNumber(nutriment.getFor100g()) + " " + nutriment.getUnit();
                    levelItem.add(new NutrientLevelItem(getString(R.string.txtSugars), sugarsValue, sugarsLocalize, sugars.getImageLevel()));
                }

                if (salt != null) {
                    String saltLocalize = salt.getLocalize(context);
                    Nutriments.Nutriment nutriment = nutriments.get(Nutriments.SALT);
                    String saltValue = getRoundNumber(nutriment.getFor100g()) + " " + nutriment.getUnit();
                    levelItem.add(new NutrientLevelItem(getString(R.string.txtSalt), saltValue, saltLocalize, salt.getImageLevel()));
                }

                img.setImageDrawable(ContextCompat.getDrawable(context, Utils.getImageGrade(product.getNutritionGradeFr())));
                img.setOnClickListener(view1 -> {
                    CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());

                    CustomTabActivityHelper.openCustomTab(SummaryProductFragment.this.getActivity(), customTabsIntent, nutritionScoreUri, new WebViewFallback());
                });
            }
        }

    }

    private String getEmbUrl(String embTag) {
        Tag tag = mTagDao.queryBuilder().where(TagDao.Properties.Id.eq(embTag)).unique();
        if (tag != null) return tag.getName();
        return null;
    }

    private String getEmbCode(String embTag) {
        Tag tag = mTagDao.queryBuilder().where(TagDao.Properties.Id.eq(embTag)).unique();
        if (tag != null) return tag.getName();
        return embTag;
    }

    @OnClick(R.id.product_incomplete_message_dismiss_icon)
    public void onDismissProductIncompleteMsgClicked() {
        productIncompleteView.setVisibility(View.GONE);
    }

    // Implements CustomTabActivityHelper.ConnectionCallback
    @Override
    public void onCustomTabsConnected() {
        img.setClickable(true);
    }

    // Implements CustomTabActivityHelper.ConnectionCallback
    @Override
    public void onCustomTabsDisconnected() {
        img.setClickable(false);
    }

    @OnClick(R.id.buttonMorePictures)
    public void takeMorePicture() {
        try {
            if (Utils.isHardwareCameraInstalled(getContext())) {
                if (ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                } else {
                    sendOther = true;
                    EasyImage.openCamera(this, 0);
                }
            } else {
                if (ContextCompat.checkSelfPermission(this.getContext(), READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this.getContext(), WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this.getActivity(), new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE);
                } else {
                    sendOther = true;
                    EasyImage.openGallery(this, 0, false);
                }
            }

            if (ContextCompat.checkSelfPermission(this.getContext(), READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this.getContext(), WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), READ_EXTERNAL_STORAGE)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), WRITE_EXTERNAL_STORAGE)) {
                    new MaterialDialog.Builder(this.getContext())
                            .title(R.string.action_about)
                            .content(R.string.permission_storage)
                            .neutralText(R.string.txtOk)
                            .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(this.getActivity(), new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE))
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this.getActivity(), new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE);
                }
            }
        } catch (NullPointerException e) {
            Log.i(getClass().getSimpleName(), e.toString());
        }
    }

    @OnClick(R.id.imageViewFront)
    public void openFullScreen(View v) {
        if (mUrlImage != null) {
            Intent intent = new Intent(v.getContext(), FullScreenImage.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageurl", mUrlImage);
            intent.putExtras(bundle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), (View) mImageFront,
                                getActivity().getString(R.string.product_transition));
                startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }
        } else {
            // take a picture
            if (ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                sendOther = false;
                if (Utils.isHardwareCameraInstalled(getContext())) {
                    EasyImage.openCamera(this, 0);
                } else {
                    EasyImage.openGallery(getActivity(), 0, false);
                }
            }
        }
    }

    private void onPhotoReturned(File photoFile) {
        ProductImage image = new ProductImage(barcode, FRONT, photoFile);
        image.setFilePath(photoFile.getAbsolutePath());
        api.postImg(getContext(), image);
        addPhotoLabel.setVisibility(View.GONE);
        mUrlImage = photoFile.getAbsolutePath();

        Picasso.with(getContext())
                .load(photoFile)
                .fit()
                .into(mImageFront);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                if (!sendOther) {
                    onPhotoReturned(new File(resultUri.getPath()));
                } else {
                    ProductImage image = new ProductImage(barcode, OTHER, new File(resultUri.getPath()));
                    image.setFilePath(resultUri.getPath());
                    api.postImg(getContext(), image);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                CropImage.activity(Uri.fromFile(imageFiles.get(0))).setAllowFlipping(false)
                        .start(getContext(), mFragment);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(getContext());
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length <= 0 || grantResults[0] != PERMISSION_GRANTED) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.permission_title)
                            .content(R.string.permission_denied)
                            .negativeText(R.string.txtNo)
                            .positiveText(R.string.txtYes)
                            .onPositive((dialog, which) -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .show();
                } else {
                    sendOther = false;
                    EasyImage.openCamera(this, 0);
                }
            }
        }
    }
}
