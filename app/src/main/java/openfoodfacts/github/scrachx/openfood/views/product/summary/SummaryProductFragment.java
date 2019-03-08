package openfoodfacts.github.scrachx.openfood.views.product.summary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.BottomScreenCommon;
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
import openfoodfacts.github.scrachx.openfood.models.YourListedProduct;
import openfoodfacts.github.scrachx.openfood.models.YourListedProductDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.WikidataApiClient;
import openfoodfacts.github.scrachx.openfood.utils.ImageUploadListener;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductComparisonActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrientLevelListAdapter;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.views.product.ProductFragment;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.INGREDIENTS;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.NUTRITION;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.OTHER;
import static openfoodfacts.github.scrachx.openfood.utils.ProductInfoState.EMPTY;
import static openfoodfacts.github.scrachx.openfood.utils.ProductInfoState.LOADING;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.getColor;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SummaryProductFragment extends BaseFragment implements CustomTabActivityHelper.ConnectionCallback, ISummaryProductPresenter.View, ImageUploadListener {

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
    @BindView(R.id.textOtherInfo)
    TextView otherInfo;
    @BindView(R.id.textConservationCond)
    TextView conservationCond;
    @BindView(R.id.textRecyclingInstructionToRecycle)
    TextView recyclingInstructionToRecycle;
    @BindView(R.id.textRecyclingInstructionToDiscard)
    TextView recyclingInstructionToDiscard;
    @BindView(R.id.front_picture_layout)
    LinearLayout frontPictureLayout;
    @BindView(R.id.imageViewFront)
    ImageView mImageFront;
    @BindView(R.id.addPhotoLabel)
    TextView addPhotoLabel;
    @BindView(R.id.uploadingImageProgress)
    ProgressBar uploadingImageProgress;
    @BindView(R.id.uploadingImageProgressText)
    TextView uploadingImageProgressText;
    @BindView(R.id.buttonMorePictures)
    Button addMorePicture;
    @BindView(R.id.add_nutriscore_prompt)
    Button addNutriScorePrompt;
    @BindView(R.id.imageGrade)
    ImageView img;
    @BindView(R.id.nova_group)
    ImageView novaGroup;
    @BindView(R.id.scores_layout)
    ConstraintLayout scoresLayout;
    @BindView(R.id.ingredient_image_prompt_layout)
    LinearLayout ingredientImagePromptLayout;
    @BindView(R.id.imageViewIngredients)
    ImageView imageViewIngredients;
    @BindView(R.id.add_ingredient_photo_label)
    TextView ingredientPhotoLabel;
    @BindView(R.id.nutrition_image_prompt_layout)
    LinearLayout nutritionImagePromptLayout;
    @BindView(R.id.imageViewNutrition)
    ImageView imageViewNutrition;
    @BindView(R.id.add_nutrition_photo_label)
    TextView nutritionPhotoLabel;
    @BindView(R.id.listNutrientLevels)
    RecyclerView rv;
    @BindView(R.id.textNutrientTxt)
    TextView textNutrientTxt;
    @BindView(R.id.cvNutritionLights)
    CardView nutritionLightsCardView;
    @BindView(R.id.textAdditiveProduct)
    TextView additiveProduct;
    @BindView(R.id.cvTextAdditiveProduct)
    CardView textAdditiveProductCardView;
    @BindView(R.id.textWarning)
    TextView warning;
    @BindView(R.id.textCustomerService)
    TextView customerService;
    @BindView(R.id.listname)
    TextView listName;
    @BindView(R.id.compare_product_button)
    Button compareProductButton;
    private State state;
    private Product product;
    private OpenFoodAPIClient api;
    private WikidataApiClient apiClientForWikiData;
    private String mUrlImage;
    private String barcode;
    private boolean sendOther = false;
    private CustomTabsIntent customTabsIntent;
    private CustomTabActivityHelper customTabActivityHelper;
    private Uri nutritionScoreUri;
    private Uri embCodeUri;
    private TagDao mTagDao;
    private SummaryProductFragment mFragment;
    private ISummaryProductPresenter.Actions presenter;
    private Uri manufactureUri;
    //boolean to determine if image should be loaded or not
    private boolean isLowBatteryMode = false;
    //boolean to determine if nutrient prompt should be shown
    private boolean showNutrientPrompt = false;
    //boolean to determine if category prompt should be shown
    private boolean showCategoryPrompt = false;
    //boolean to indicate if the image clicked was that of ingredients
    private boolean addingIngredientsImage = false;
    //boolean to indicate if the image clicked was that of nutrition
    private boolean addingNutritionImage = false;
    //boolean to determine if nutrition data should be shown
    private boolean showNutritionData=true;

    private YourListedProductDao yourListedProductDao;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());

        Intent intent = getActivity().getIntent();
        if (intent.getExtras() != null && intent.getExtras().getSerializable("state") != null) {
            state = (State) intent.getExtras().getSerializable("state");
        } else {
            state = ProductFragment.mState;
        }

        presenter = new SummaryProductPresenter(product, this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        api = new OpenFoodAPIClient(getActivity());
        apiClientForWikiData = new WikidataApiClient();
        mFragment = this;
        return createView(inflater, container, R.layout.fragment_summary_product);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        refreshView(state);
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);
        product = state.getProduct();
        presenter = new SummaryProductPresenter(product, this);
        categoryProduct.setText(bold(getString(R.string.txtCategories)));
        labelProduct.setText(bold(getString(R.string.txtLabels)));
        countryProduct.setText(bold(getString(R.string.txtCountries)));

        //refresh visibilty of UI components
        labelProduct.setVisibility(View.VISIBLE);
        brandProduct.setVisibility(View.VISIBLE);
        quantityProduct.setVisibility(View.VISIBLE);
        packagingProduct.setVisibility(View.VISIBLE);
        countryProduct.setVisibility(View.VISIBLE);
        storeProduct.setVisibility(View.VISIBLE);
        embCode.setVisibility(View.VISIBLE);
        manufactureUlrProduct.setVisibility(View.VISIBLE);
        manufacturingProduct.setVisibility(View.VISIBLE);
        ingredientsOrigin.setVisibility(View.VISIBLE);
        barCodeProduct.setVisibility(View.VISIBLE);
        nameProduct.setVisibility(View.VISIBLE);
        genericNameProduct.setVisibility(View.VISIBLE);

        yourListedProductDao = Utils.getAppDaoSession(getContext()).getYourListedProductDao();
        List<YourListedProduct> searchResult = yourListedProductDao.queryBuilder()
                .where(YourListedProductDao.Properties.Barcode.eq(product.getCode())).list();
        if (searchResult.size() > 0) {
            listName.setVisibility(View.VISIBLE);
            listName.setText(getString(R.string.list_text));
            int i = 0;
            for (; i < searchResult.size() - 1; i++) {
                listName.append(searchResult.get(i).getListName() + ", ");
            }
            listName.append(searchResult.get(i).getListName());
        } else {
            listName.setVisibility(View.GONE);
        }

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Utils.DISABLE_IMAGE_LOAD = preferences.getBoolean("disableImageLoad", false);
        if (Utils.DISABLE_IMAGE_LOAD && Utils.getBatteryLevel(getContext())) {
            isLowBatteryMode = true;
        }

        //checks the product states_tags to determine which prompt to be shown
        List<String> statesTags = product.getStatesTags();
        if (statesTags.contains("en:categories-to-be-completed")) {
            showCategoryPrompt = true;
        }
        if (product.getNoNutritionData() != null && product.getNoNutritionData().equals("on")) {
            showNutrientPrompt = false;
            showNutritionData= false;
        } else {
            if (statesTags.contains("en:nutrition-facts-to-be-completed")) {
                showNutrientPrompt = true;
            }
        }

        if (showNutrientPrompt || showCategoryPrompt) {
            addNutriScorePrompt.setVisibility(View.VISIBLE);
            if (showNutrientPrompt && showCategoryPrompt) {
                addNutriScorePrompt.setText(getString(R.string.add_nutrient_category_prompt_text));
            } else if (showNutrientPrompt) {
                addNutriScorePrompt.setText(getString(R.string.add_nutrient_prompt_text));
            } else if (showCategoryPrompt) {
                addNutriScorePrompt.setText(getString(R.string.add_category_prompt_text));
            }
        }

        if (!showNutritionData) {
            nutritionImagePromptLayout.setVisibility(View.GONE);
        }

        presenter.loadAllergens();
        presenter.loadCategories();
        presenter.loadLabels();
        presenter.loadCountries();
        additiveProduct.setText(bold(getString(R.string.txtAdditives)));
        presenter.loadAdditives();

        mTagDao = Utils.getAppDaoSession(getActivity()).getTagDao();
        barcode = product.getCode();

        if (isNotBlank(product.getImageUrl())) {
            addPhotoLabel.setVisibility(View.GONE);

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Picasso.with(getContext())
                        .load(product.getImageUrl())
                        .into(mImageFront);
            } else {
                mImageFront.setVisibility(View.GONE);

            }

            mUrlImage = product.getImageUrl();
        }

        //the following checks whether the ingredient and nutrition images are already uploaded for the product
        if (isBlank(product.getImageIngredientsUrl())) {
            ingredientImagePromptLayout.setVisibility(View.VISIBLE);
        }

        if(!BuildConfig.FLAVOR.equals( "obf" ) && !BuildConfig.FLAVOR.equals( "opf" )) {
            if (isBlank(product.getImageNutritionUrl()) && showNutritionData) {
                nutritionImagePromptLayout.setVisibility(View.VISIBLE);
            }
        }

        //TODO use OpenFoodApiService to fetch product by packaging, brands, categories etc

        if (isNotBlank(product.getOtherInformation())) {
            otherInfo.setText(bold(getString(R.string.txtOtherInfo)));
            otherInfo.append(' ' + product.getOtherInformation());
        } else {
            otherInfo.setVisibility(View.GONE);
        }
        if (isNotBlank(product.getConservationConditions())) {
            conservationCond.setText(bold(getString(R.string.txtConservationCond)));
            conservationCond.append(' ' + product.getConservationConditions());
        } else {
            conservationCond.setVisibility(View.GONE);
        }
        if (isNotBlank(product.getRecyclingInstructionsToDiscard())) {
            recyclingInstructionToDiscard.setText(bold(getString(R.string.txtRecyclingInstructionToDiscard)));
            recyclingInstructionToDiscard.append(' ' + product.getRecyclingInstructionsToDiscard());
        } else {
            recyclingInstructionToDiscard.setVisibility(View.GONE);
        }
        if (isNotBlank(product.getRecyclingInstructionsToRecycle())) {
            recyclingInstructionToRecycle.setText(bold(getString(R.string.txtRecyclingInstructionToRecycle)));
            recyclingInstructionToRecycle.append(' ' + product.getRecyclingInstructionsToRecycle());
        } else {
            recyclingInstructionToRecycle.setVisibility(View.GONE);
        }
        if (isNotBlank(product.getProductName())) {
            nameProduct.setText(product.getProductName());
        } else {
            nameProduct.setVisibility(View.GONE);
        }

        if (LocaleHelper.getLanguage(getContext()) != null) {
            String lang = LocaleHelper.getLanguage(getContext());
            //removes country specific code in the language code eg: nl-BE
            if (lang.contains("-")) {
                String langSplit[] = lang.split("-");
                lang = langSplit[0];
            }
            String langCode = lang;

            if (product.getProductName(langCode) != null) {
                nameProduct.setText(product.getProductName(langCode));
            } else if (product.getProductName("en") != null) {
                nameProduct.setText(product.getProductName("en"));
            } else if (product.getProductName() != null) {
                nameProduct.setText(product.getProductName());
            } else {
                nameProduct.setVisibility(View.GONE);
            }
        }
        if (isNotBlank(product.getGenericName())) {
            genericNameProduct.setText(product.getGenericName());
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
                packagingProduct.append(Utils.getClickableText(packagings[i].trim(), "", SearchType.PACKAGING, getActivity(), customTabsIntent));
                packagingProduct.append(", ");
            }

            packagingProduct.append(Utils.getClickableText(packagings[packagings.length - 1].trim(), "", SearchType.PACKAGING, getActivity(), customTabsIntent));
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
                brandProduct.append(Utils.getClickableText(brands[i].trim(), "", SearchType.BRAND, getActivity(), customTabsIntent));
                brandProduct.append(", ");
            }
            brandProduct.append(Utils.getClickableText(brands[brands.length - 1].trim(), "", SearchType.BRAND, getActivity(), customTabsIntent));
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

        if (product.getEmbTags() != null && !product.getEmbTags().toString().trim().equals("[]")) {
            embCode.setMovementMethod(LinkMovementMethod.getInstance());
            embCode.setText(bold(getString(R.string.txtEMB)));
            embCode.append(" ");

            String embTag;
            String[] embTags = product.getEmbTags().toString().replace("[", "").replace("]", "").split(", ");
            for (int i = 0; i < embTags.length - 1; i++) {
                embTag = embTags[i];
                embCode.append(Utils.getClickableText(getEmbCode(embTag).trim(), getEmbUrl(embTag), SearchType.EMB, getActivity(), customTabsIntent));
                embCode.append(", ");
            }

            embTag = embTags[embTags.length - 1];
            embCode.append(Utils.getClickableText(getEmbCode(embTag).trim(), getEmbUrl(embTag), SearchType.EMB, getActivity(), customTabsIntent));
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
                storeProduct.append(Utils.getClickableText(store.trim(), store, SearchType.STORE, getActivity(), customTabsIntent));
                storeProduct.append(", ");
            }

            store = stores[stores.length - 1];
            storeProduct.append(Utils.getClickableText(store.trim(), store, SearchType.STORE, getActivity(), customTabsIntent));
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

            spannableText.setSpan(clickableSpan, manufactureUrlTitle.length() + 1, spannableText.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new StyleSpan(Typeface.BOLD), 0, manufactureUrlTitle.length(), 0);

            manufactureUlrProduct.setText(spannableText);
            manufactureUlrProduct.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            manufactureUlrProduct.setVisibility(View.GONE);
        }


        if (isNotBlank(product.getWarning())) {
            warning.setText(bold(getString(R.string.warning)));
            warning.append(product.getWarning());
        } else {
            warning.setVisibility(View.GONE);
        }

        if (isNotBlank(product.getCustomerService())) {
            customerService.setText(bold(getString(R.string.customer_service)));
            customerService.append(product.getCustomerService());
        } else {
            customerService.setVisibility(View.GONE);
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
            scoresLayout.setVisibility(View.VISIBLE);
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

            if (!(fat == null && salt == null && saturatedFat == null && sugars == null)) {
                // prefetch the uri
                // currently only available in french translations
                nutritionScoreUri = Uri.parse(getString(R.string.nutriscore_uri));
                customTabActivityHelper.mayLaunchUrl(nutritionScoreUri, null, null);
                Context context = this.getContext();
                if (Utils.getImageGrade(product.getNutritionGradeFr()) != 0) {
                    img.setImageDrawable(ContextCompat.getDrawable(context, Utils.getImageGrade(product.getNutritionGradeFr())));
                } else {
                    img.setVisibility(View.GONE);
                }
                img.setOnClickListener(view1 -> {
                    CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
                    CustomTabActivityHelper.openCustomTab(SummaryProductFragment.this.getActivity(), customTabsIntent, nutritionScoreUri, new WebViewFallback());
                });

                if (nutriments != null) {
                    nutritionLightsCardView.setVisibility(View.VISIBLE);
                    Nutriments.Nutriment fatNutriment = nutriments.get(Nutriments.FAT);
                    if (fat != null && fatNutriment != null) {
                        String fatNutrimentLevel = fat.getLocalize(context);
                        String modifier = nutriments.getModifier(Nutriments.FAT);
                        levelItem.add(new NutrientLevelItem(getString(R.string.txtFat),
                                (modifier == null ? "" : modifier)
                                        + getRoundNumber(fatNutriment.getFor100g())
                                        + " " + fatNutriment.getUnit(),
                                fatNutrimentLevel,
                                fat.getImageLevel()));
                    }

                    Nutriments.Nutriment saturatedFatNutriment = nutriments.get(Nutriments.SATURATED_FAT);
                    if (saturatedFat != null && saturatedFatNutriment != null) {
                        String saturatedFatLocalize = saturatedFat.getLocalize(context);
                        String saturatedFatValue = getRoundNumber(saturatedFatNutriment.getFor100g()) + " " + saturatedFatNutriment.getUnit();
                        String modifier = nutriments.getModifier(Nutriments.SATURATED_FAT);
                        levelItem.add(new NutrientLevelItem(getString(R.string.txtSaturatedFat),
                                (modifier == null ? "" : modifier) + saturatedFatValue,
                                saturatedFatLocalize,
                                saturatedFat.getImageLevel()));
                    }

                    Nutriments.Nutriment sugarsNutriment = nutriments.get(Nutriments.SUGARS);
                    if (sugars != null && sugarsNutriment != null) {
                        String sugarsLocalize = sugars.getLocalize(context);
                        String sugarsValue = getRoundNumber(sugarsNutriment.getFor100g()) + " " + sugarsNutriment.getUnit();
                        String modifier = nutriments.getModifier(Nutriments.SUGARS);
                        levelItem.add(new NutrientLevelItem(getString(R.string.txtSugars),
                                (modifier == null ? "" : modifier) + sugarsValue,
                                sugarsLocalize,
                                sugars.getImageLevel()));
                    }

                    Nutriments.Nutriment saltNutriment = nutriments.get(Nutriments.SALT);
                    if (salt != null && saltNutriment != null) {
                        String saltLocalize = salt.getLocalize(context);
                        String saltValue = getRoundNumber(saltNutriment.getFor100g()) + " " + saltNutriment.getUnit();
                        String modifier = nutriments.getModifier(Nutriments.SALT);
                        levelItem.add(new NutrientLevelItem(getString(R.string.txtSalt),
                                (modifier == null ? "" : modifier) + saltValue,
                                saltLocalize,
                                salt.getImageLevel()));
                    }
                }

            } else {
                nutritionLightsCardView.setVisibility(View.GONE);
            }

            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            rv.setAdapter(new NutrientLevelListAdapter(getContext(), levelItem));


            if (product.getNovaGroups() != null) {
                novaGroup.setImageResource(Utils.getNovaGroupDrawable(product.getNovaGroups()));
                novaGroup.setOnClickListener(view1 -> {
                    Uri uri = Uri.parse(getString(R.string.url_nova_groups));
                    CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
                    CustomTabActivityHelper.openCustomTab(SummaryProductFragment.this.getActivity(), customTabsIntent, uri, new WebViewFallback());
                });
            } else {
                novaGroup.setImageResource(0);
            }
            if (product.getNovaGroups() == null && product.getNutritionGradeFr() == null) {
                img.setVisibility(View.GONE);
                novaGroup.setVisibility(View.GONE);
            }

        } else {
            scoresLayout.setVisibility(View.GONE);
        }
    }

    private CharSequence getAdditiveTag(AdditiveName additive) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (additive.getIsWikiDataIdPresent()) {
                    apiClientForWikiData.doSomeThing(additive.getWikiDataId(), (value, result) -> {
                        FragmentActivity activity = getActivity();
                        if (value) {
                            if (activity != null && !activity.isFinishing()) {
                                BottomScreenCommon.showBottomScreen(result, additive,
                                        activity.getSupportFragmentManager());
                            }
                        } else {
                            if (additive.hasOverexposureData()) {
                                if (activity != null && !activity.isFinishing()) {
                                    BottomScreenCommon.showBottomScreen(result, additive,
                                            activity.getSupportFragmentManager());
                                }
                            } else {
                                ProductBrowsingListActivity.startActivity(getContext(), additive.getAdditiveTag(), additive.getName(), SearchType.ADDITIVE);
                            }
                        }
                    });
                } else {
                    FragmentActivity activity = getActivity();
                    if (additive.hasOverexposureData()) {
                        if (activity != null && !activity.isFinishing()) {
                            BottomScreenCommon.showBottomScreen(null, additive,
                                    activity.getSupportFragmentManager());
                        }
                    } else {
                        ProductBrowsingListActivity.startActivity(getContext(), additive.getAdditiveTag(), additive.getName(), SearchType.ADDITIVE);
                    }
                }
            }
        };

        spannableStringBuilder.append(additive.getName());
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        // if the additive has an overexposure risk ("high" or "moderate") then append the warning message to it
        if (additive.hasOverexposureData()) {
            boolean isHighRisk = "high".equalsIgnoreCase(additive.getOverexposureRisk());
            Drawable riskIcon;
            String riskWarningStr;
            int riskWarningColor;
            if (isHighRisk) {
                riskIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_additive_high_risk);
                riskWarningStr = getString(R.string.overexposure_high);
                riskWarningColor = getColor(getContext(), R.color.overexposure_high);
            } else {
                riskIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_additive_moderate_risk);
                riskWarningStr = getString(R.string.overexposure_moderate);
                riskWarningColor = getColor(getContext(), R.color.overexposure_moderate);
            }
            riskIcon.setBounds(0, 0, riskIcon.getIntrinsicWidth(), riskIcon.getIntrinsicHeight());
            ImageSpan iconSpan = new ImageSpan(riskIcon, ImageSpan.ALIGN_BOTTOM);

            spannableStringBuilder.append(" - "); // this will be replaced with the risk icon
            spannableStringBuilder.setSpan(iconSpan, spannableStringBuilder.length() - 2, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

            spannableStringBuilder.append(riskWarningStr);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(riskWarningColor), spannableStringBuilder.length() - riskWarningStr.length(), spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableStringBuilder;
    }

    @Override
    public void showAdditives(List<AdditiveName> additives) {
        additiveProduct.setText(bold(getString(R.string.txtAdditives)));
        additiveProduct.setMovementMethod(LinkMovementMethod.getInstance());
        additiveProduct.append(" ");
        additiveProduct.append("\n");
        additiveProduct.setClickable(true);
        additiveProduct.setMovementMethod(LinkMovementMethod.getInstance());

        for (int i = 0; i < additives.size() - 1; i++) {
            additiveProduct.append(getAdditiveTag(additives.get(i)));
            additiveProduct.append("\n");
        }

        additiveProduct.append(getAdditiveTag((additives.get(additives.size() - 1))));
    }

    @Override
    public void showAdditivesState(String state) {
        switch (state) {
            case LOADING: {
                textAdditiveProductCardView.setVisibility(View.VISIBLE);
                additiveProduct.append(getString(R.string.txtLoading));
                break;
            }
            case EMPTY: {
                textAdditiveProductCardView.setVisibility(View.GONE);
                break;
            }
        }
    }

    @Override
    public void showAllergens(List<AllergenName> allergens) {
        List<String> productAllergens = product.getAllergensHierarchy();
        List<String> traces = product.getTracesTags();
        productAllergens.addAll(traces);
        if (!allergens.isEmpty() && product.getStatesTags().get(0).contains("to-be-completed")) {
            productIncompleteView.setVisibility(View.VISIBLE);
        }

        List<String> matchAll = new ArrayList<>();
        for (int a = 0; a < allergens.size(); a++) {
            for (int i = 0; i < productAllergens.size(); i++) {

                if (productAllergens.get(i).trim().equals(allergens.get(a).getAllergenTag().trim())) {
                    matchAll.add(allergens.get(a).getName());
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
    }

    @Override
    public void showCategories(List<CategoryName> categories) {
        categoryProduct.setText(bold(getString(R.string.txtCategories)));
        categoryProduct.setMovementMethod(LinkMovementMethod.getInstance());
        categoryProduct.append(" ");
        categoryProduct.setClickable(true);
        categoryProduct.setMovementMethod(LinkMovementMethod.getInstance());

        if (categories.isEmpty()) {
            categoryProduct.setVisibility(View.GONE);
        } else {
            categoryProduct.setVisibility(View.VISIBLE);
            // Add all the categories to text view and link them to wikidata is possible
            for (int i = 0, lastIndex = categories.size() - 1; i <= lastIndex; i++) {
                CategoryName category = categories.get(i);
                CharSequence categoryName = getCategoriesTag(category);
                if (categoryName != null) {
                    // Add category name to text view
                    categoryProduct.append(categoryName);
                    // Add a comma if not the last item
                    if (i != lastIndex) {
                        categoryProduct.append(", ");
                    }
                }
            }
        }
    }

    @Override
    public void showLabels(List<LabelName> labels) {
        labelProduct.setText(bold(getString(R.string.txtLabels)));
        labelProduct.setClickable(true);
        labelProduct.setMovementMethod(LinkMovementMethod.getInstance());
        labelProduct.append(" ");

        for (int i = 0; i < labels.size() - 1; i++) {
            labelProduct.append(getLabelTag(labels.get(i)));
            labelProduct.append(", ");
        }

        labelProduct.append(getLabelTag(labels.get(labels.size() - 1)));
    }

    @Override
    public void showCountries(List<CountryName> countries) {
        countryProduct.setText(bold(getString(R.string.txtCountries)));
        countryProduct.setClickable(true);
        countryProduct.setMovementMethod(LinkMovementMethod.getInstance());
        countryProduct.append(" ");

        for (int i = 0; i < countries.size() - 1; i++) {
            countryProduct.append(Utils.getClickableText(StringUtils.capitalize(countries.get(i).getName()).trim(), "", SearchType.COUNTRY, getActivity(), customTabsIntent));
            countryProduct.append(", ");
        }

        countryProduct.append(Utils.getClickableText(StringUtils.capitalize(countries.get(countries.size() - 1).getName()).trim(), "", SearchType.COUNTRY, getActivity(), customTabsIntent));

    }

    @Override
    public void showCategoriesState(String state) {
        switch (state) {
            case LOADING: {
                categoryProduct.append(getString(R.string.txtLoading));
                break;
            }
            case EMPTY: {
                categoryProduct.setVisibility(View.GONE);
                break;
            }
        }
    }

    @Override
    public void showLabelsState(String state) {
        switch (state) {
            case LOADING: {
                labelProduct.append(getString(R.string.txtLoading));
                break;
            }
            case EMPTY: {
                labelProduct.setVisibility(View.GONE);
                break;
            }
        }
    }

    @Override
    public void showCountriesState(String state) {
        switch (state) {
            case LOADING: {
                countryProduct.append(getString(R.string.txtLoading));
                break;
            }
            case EMPTY: {
                countryProduct.setVisibility(View.GONE);
                break;
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

    private CharSequence getCategoriesTag(CategoryName category) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (category.getIsWikiDataIdPresent()) {
                    apiClientForWikiData.doSomeThing(category.getWikiDataId(), (value, result) -> {
                        if (value) {
                            FragmentActivity activity = getActivity();

                            if (activity != null && !activity.isFinishing()) {
                                BottomScreenCommon.showBottomScreen(result, category,
                                        activity.getSupportFragmentManager());
                            }
                        } else {
                            ProductBrowsingListActivity.startActivity(getContext(),
                                    category.getCategoryTag(),
                                    category.getName(),
                                    SearchType.CATEGORY);
                        }
                    });
                } else {
                    ProductBrowsingListActivity.startActivity(getContext(),
                            category.getCategoryTag(),
                            category.getName(),
                            SearchType.CATEGORY);
                }

            }
        };
        spannableStringBuilder.append(category.getName());
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        if (!category.isNotNull()) {
            StyleSpan iss = new StyleSpan(android.graphics.Typeface.ITALIC); //Span to make text italic
            spannableStringBuilder.setSpan(iss, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableStringBuilder;
    }

    private CharSequence getLabelTag(LabelName label) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {

                if (label.getIsWikiDataIdPresent()) {
                    apiClientForWikiData.doSomeThing(label.getWikiDataId(), (value, result) -> {
                        if (value) {
                            FragmentActivity activity = getActivity();
                            if (activity != null && !activity.isFinishing()) {
                                BottomScreenCommon.showBottomScreen(result, label,
                                        activity.getSupportFragmentManager());
                            }
                        } else {
                            ProductBrowsingListActivity.startActivity(getContext(),
                                    label.getLabelTag(),
                                    label.getName(),
                                    SearchType.LABEL);
                        }
                    });

                } else {
                    ProductBrowsingListActivity.startActivity(getContext(),
                            label.getLabelTag(),
                            label.getName(),
                            SearchType.LABEL);
                }

            }

        };


        spannableStringBuilder.append(label.getName());

        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableStringBuilder;
    }

    @OnClick(R.id.product_incomplete_message_dismiss_icon)
    public void onDismissProductIncompleteMsgClicked() {
        productIncompleteView.setVisibility(View.GONE);
    }

    @OnClick(R.id.add_nutriscore_prompt)
    public void onAddNutriScorePromptClick() {
        Intent intent = new Intent(getActivity(), AddProductActivity.class);
        intent.putExtra("edit_product", product);
        //adds the information about the prompt when navigating the user to the edit the product
        intent.putExtra("modify_category_prompt", showCategoryPrompt);
        intent.putExtra("modify_nutrition_prompt", showNutrientPrompt);
        startActivity(intent);
    }

    @OnClick(R.id.compare_product_button)
    public void onCompareProductButtonClick() {
        Intent intent = new Intent(getContext(), ProductComparisonActivity.class);
        intent.putExtra("product_found", true);
        ArrayList<Product> productsToCompare = new ArrayList<>();
        productsToCompare.add(product);
        intent.putExtra("products_to_compare", productsToCompare);
        startActivity(intent);
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

    //when the prompt for the images are selected, the camera is initialized and corresponding booleans flipped
    @OnClick(R.id.imageViewIngredients)
    public void addIngredientImage() {
        addingIngredientsImage = true;
        if (ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            if (Utils.isHardwareCameraInstalled(getContext())) {
                EasyImage.openCamera(this, 0);
            } else {
                EasyImage.openGallery(getActivity(), 0, false);
            }
        }
    }

    @OnClick(R.id.imageViewNutrition)
    public void addNutritionImage() {
        addingNutritionImage = true;
        if (ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            if (Utils.isHardwareCameraInstalled(getContext())) {
                EasyImage.openCamera(this, 0);
            } else {
                EasyImage.openGallery(getActivity(), 0, false);
            }
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
        api.postImg(getContext(), image, this);
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

                //the booleans are checked to determine if the picture uploaded was due to a prompt click
                //the pictures are uploaded with the correct path
                if (addingIngredientsImage) {
                    ProductImage image = new ProductImage(barcode, INGREDIENTS, new File(resultUri.getPath()));
                    image.setFilePath(resultUri.getPath());
                    showOtherImageProgress();
                    api.postImg(getContext(), image, this);
                    ingredientImagePromptLayout.setVisibility(View.GONE);
                    addingIngredientsImage = false;
                }

                if (addingNutritionImage) {
                    ProductImage image = new ProductImage(barcode, NUTRITION, new File(resultUri.getPath()));
                    image.setFilePath(resultUri.getPath());
                    showOtherImageProgress();
                    api.postImg(getContext(), image, this);
                    nutritionImagePromptLayout.setVisibility(View.GONE);
                    addingNutritionImage = false;
                }

                if (!sendOther) {
                    onPhotoReturned(new File(resultUri.getPath()));
                } else {
                    ProductImage image = new ProductImage(barcode, OTHER, new File(resultUri.getPath()));
                    image.setFilePath(resultUri.getPath());
                    showOtherImageProgress();
                    api.postImg(getContext(), image, this);
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
                CropImage.activity(Uri.fromFile(imageFiles.get(0)))
                        .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
                        .setAllowFlipping(false)
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

    @Override
    public void onDestroyView() {
        presenter.dispose();
        super.onDestroyView();
    }

    public void showOtherImageProgress() {
        uploadingImageProgress.setVisibility(View.VISIBLE);
        uploadingImageProgressText.setVisibility(View.VISIBLE);
        uploadingImageProgressText.setText(R.string.toastSending);
    }

    @Override
    public void onSuccess() {
        uploadingImageProgress.setVisibility(View.GONE);
        uploadingImageProgressText.setText(R.string.image_uploaded_successfully);

    }

    @Override
    public void onFailure(String message) {
        uploadingImageProgress.setVisibility(View.GONE);
        uploadingImageProgressText.setVisibility(View.GONE);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}