package openfoodfacts.github.scrachx.openfood.views.product.ingredients;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.models.*;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.WikidataApiClient;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.LoginActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.INGREDIENTS;
import static openfoodfacts.github.scrachx.openfood.utils.ProductInfoState.EMPTY;
import static openfoodfacts.github.scrachx.openfood.utils.ProductInfoState.LOADING;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.jsoup.helper.StringUtil.isBlank;

public class IngredientsProductFragment extends BaseFragment implements IIngredientsProductPresenter.View {
    public static final Pattern INGREDIENT_PATTERN = Pattern.compile("[\\p{L}\\p{Nd}(),.-]+");
    public static final Pattern ALLERGEN_PATTERN = Pattern.compile("[\\p{L}\\p{Nd}]+[\\p{L}\\p{Nd}\\p{Z}\\p{P}&&[^,]]*");
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;
    private static final int EDIT_REQUEST_CODE = 2;
    @BindView(R.id.textIngredientProduct)
    TextView ingredientsProduct;
    private AllergenNameDao mAllergenNameDao;
    @BindView(R.id.textSubstanceProduct)
    TextView substanceProduct;
    @BindView(R.id.textTraceProduct)
    TextView traceProduct;
    @BindView(R.id.textAdditiveProduct)
    TextView additiveProduct;
    @BindView(R.id.textPalmOilProduct)
    TextView palmOilProduct;
    @BindView(R.id.textMayBeFromPalmOilProduct)
    TextView mayBeFromPalmOilProduct;
    @BindView(R.id.novaLayout)
    LinearLayout novaLayout;
    @BindView(R.id.nova_group)
    ImageView novaGroup;
    @BindView(R.id.novaExplanation)
    TextView novaExplanation;
    @BindView(R.id.novaMethodLink)
    TextView novaMethodLink;
    @BindView(R.id.imageViewIngredients)
    ImageView mImageIngredients;
    @BindView(R.id.addPhotoLabel)
    TextView addPhotoLabel;
    @BindView(R.id.vitaminsTagsText)
    TextView vitaminTagsTextView;
    @BindView(R.id.mineralTagsText)
    TextView mineralTagsTextView;
    @BindView(R.id.aminoAcidTagsText)
    TextView aminoAcidTagsTextView;
    @BindView(R.id.otherNutritionTags)
    TextView otherNutritionTagTextView;
    @BindView(R.id.cvTextIngredientProduct)
    CardView textIngredientProductCardView;
    @BindView(R.id.cvTextTraceProduct)
    CardView textTraceProductCardView;
    @BindView(R.id.cvTextAdditiveProduct)
    CardView textAdditiveProductCardView;
    @BindView(R.id.cvTextPalmOilProduct)
    CardView textPalmOilProductCardView;
    @BindView(R.id.cvTextMayBePalmOilProduct)
    CardView textMayBePalmOilProductCardView;
    @BindView(R.id.cvVitaminsTagsText)
    CardView vitaminsTagsTextCardView;
    @BindView(R.id.cvAminoAcidTagsText)
    CardView aminoAcidTagsTextCardView;
    @BindView(R.id.cvMineralTagsText)
    CardView mineralTagsTextCardView;
    @BindView(R.id.cvOtherNutritionTags)
    CardView otherNutritionTagsCardView;
    @BindView(R.id.extract_ingredients_prompt)
    Button extractIngredientsPrompt;
    @BindView(R.id.change_ing_img)
    Button updateImageBtn;

    private OpenFoodAPIClient api;
    private String mUrlImage;
    private State mState;
    private String barcode;
    private IngredientsProductFragment mFragment;
    private SendProduct mSendProduct;
    private WikidataApiClient apiClientForWikiData;
    private CustomTabActivityHelper customTabActivityHelper;
    private CustomTabsIntent customTabsIntent;
    private IIngredientsProductPresenter.Actions presenter;
    private boolean extractIngredients = false;
    private boolean sendUpdatedIngredientsImage = false;
    //boolean to determine if image should be loaded or not
    private boolean isLowBatteryMode = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());

        mState=getStateFromActivityIntent();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        api = new OpenFoodAPIClient(getActivity());
        apiClientForWikiData = new WikidataApiClient();
        mFragment = this;

        return createView(inflater, container, R.layout.fragment_ingredients_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mState = getStateFromActivityIntent();
        extractIngredientsPrompt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_box_blue_18dp, 0, 0, 0);
        updateImageBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_a_photo_blue_18dp, 0, 0, 0);
        refreshView(mState);
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);
        mState = state;
        String langCode = LocaleHelper.getLanguage(getContext());
        if (getArguments() != null) {
            mSendProduct = (SendProduct) getArguments().getSerializable("sendProduct");
        }

        mAllergenNameDao = Utils.getAppDaoSession(getActivity()).getAllergenNameDao();

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Utils.DISABLE_IMAGE_LOAD = preferences.getBoolean("disableImageLoad", false);
        if (Utils.DISABLE_IMAGE_LOAD && Utils.getBatteryLevel(getContext())) {
            isLowBatteryMode = true;
        }

        final Product product = mState.getProduct();
        presenter = new IngredientsProductPresenter(product, this);
        barcode = product.getCode();
        List<String> vitaminTagsList = product.getVitaminTags();
        List<String> aminoAcidTagsList = product.getAminoAcidTags();
        List<String> mineralTags = product.getMineralTags();
        List<String> otherNutritionTags = product.getOtherNutritionTags();

        if (!vitaminTagsList.isEmpty()) {
            vitaminsTagsTextCardView.setVisibility(View.VISIBLE);
            vitaminTagsTextView.setText(bold(getString(R.string.vitamin_tags_text)));
            vitaminTagsTextView.append(buildStringBuilder(vitaminTagsList, StringUtils.SPACE));
        }

        if (!aminoAcidTagsList.isEmpty()) {
            aminoAcidTagsTextCardView.setVisibility(View.VISIBLE);
            aminoAcidTagsTextView.setText(bold(getString(R.string.amino_acid_tags_text)));
            aminoAcidTagsTextView.append(buildStringBuilder(aminoAcidTagsList, StringUtils.SPACE));
        }

        if (!mineralTags.isEmpty()) {
            mineralTagsTextCardView.setVisibility(View.VISIBLE);
            mineralTagsTextView.setText(bold(getString(R.string.mineral_tags_text)));
            mineralTagsTextView.append(buildStringBuilder(mineralTags, StringUtils.SPACE));
        }

        if (!otherNutritionTags.isEmpty()) {
            otherNutritionTagTextView.setVisibility(View.VISIBLE);
            otherNutritionTagTextView.setText(bold(getString(R.string.other_tags_text)));
            otherNutritionTagTextView.append(buildStringBuilder(otherNutritionTags, StringUtils.SPACE));
        }

        additiveProduct.setText(bold(getString(R.string.txtAdditives)));
        presenter.loadAdditives();

        if (isNotBlank(product.getImageIngredientsUrl(langCode))) {
            addPhotoLabel.setVisibility(View.GONE);
            updateImageBtn.setVisibility(View.VISIBLE);

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Picasso.with(getContext())
                    .load(product.getImageIngredientsUrl(langCode))
                    .into(mImageIngredients);
            } else {
                mImageIngredients.setVisibility(View.GONE);
            }

            mUrlImage = product.getImageIngredientsUrl(langCode);
        }

        //useful when this fragment is used in offline saving
        if (mSendProduct != null && isNotBlank(mSendProduct.getImgupload_ingredients())) {
            addPhotoLabel.setVisibility(View.GONE);
            mUrlImage = mSendProduct.getImgupload_ingredients();
            Picasso.with(getContext()).load("file://" + mUrlImage).config(Bitmap.Config.RGB_565).into(mImageIngredients);
        }

        List<String> allergens = getAllergens();

        if (mState != null && StringUtils.isNotEmpty(product.getIngredientsText(langCode))) {
            textIngredientProductCardView.setVisibility(View.VISIBLE);
            SpannableStringBuilder txtIngredients = new SpannableStringBuilder(product.getIngredientsText(langCode).replace("_", ""));
            txtIngredients = setSpanBoldBetweenTokens(txtIngredients, allergens);
            if (TextUtils.isEmpty(product.getIngredientsText(langCode))) {
                extractIngredientsPrompt.setVisibility(View.VISIBLE);
            }
            int ingredientsListAt = Math.max(0, txtIngredients.toString().indexOf(":"));
            if (!txtIngredients.toString().substring(ingredientsListAt).trim().isEmpty()) {
                ingredientsProduct.setText(txtIngredients);
            }
        } else {
            textIngredientProductCardView.setVisibility(View.GONE);
            if (isNotBlank(product.getImageIngredientsUrl(langCode))) {
                extractIngredientsPrompt.setVisibility(View.VISIBLE);
            }
        }
        presenter.loadAllergens();

        if (!isBlank(product.getTraces())) {
            String language = LocaleHelper.getLanguage(getContext());
            textTraceProductCardView.setVisibility(View.VISIBLE);
            traceProduct.setMovementMethod(LinkMovementMethod.getInstance());
            traceProduct.setText(bold(getString(R.string.txtTraces)));
            traceProduct.append(" ");

            String[] traces = product.getTraces().split(",");
            for (int i = 0; i < traces.length; i++) {
                String trace = traces[i];
                if (i > 0) {
                    traceProduct.append(", ");
                }
                traceProduct.append(Utils.getClickableText(getTracesName(language, trace), trace, SearchType.TRACE, getActivity(), customTabsIntent));
            }
        } else {
            textTraceProductCardView.setVisibility(View.GONE);
        }

        textPalmOilProductCardView.setVisibility(View.GONE);
        palmOilProduct.setVisibility(View.GONE);
        if (CollectionUtils.isNotEmpty(product.getIngredientsFromPalmOilTags())) {
            textPalmOilProductCardView.setVisibility(View.VISIBLE);
            palmOilProduct.setVisibility(View.VISIBLE);
            palmOilProduct.setText(bold(getString(R.string.txtPalmOilProduct)));
            palmOilProduct.append(" ");
            palmOilProduct.append(product.getIngredientsFromPalmOilTags().toString().replaceAll("[\\[,\\]]", ""));
        }
        textMayBePalmOilProductCardView.setVisibility(View.GONE);
        mayBeFromPalmOilProduct.setVisibility(View.GONE);
        if (CollectionUtils.isNotEmpty(product.getIngredientsThatMayBeFromPalmOilTags())) {
            textMayBePalmOilProductCardView.setVisibility(View.VISIBLE);
            mayBeFromPalmOilProduct.setVisibility(View.VISIBLE);
            mayBeFromPalmOilProduct.setText(bold(getString(R.string.txtMayBeFromPalmOilProduct)));
            mayBeFromPalmOilProduct.append(" ");
            mayBeFromPalmOilProduct.append(product.getIngredientsThatMayBeFromPalmOilTags().toString().replaceAll("[\\[,\\]]", ""));
        }

        if (product.getNovaGroups() != null) {
            novaLayout.setVisibility(View.VISIBLE);
            novaExplanation.setText(Utils.getNovaGroupExplanation(product.getNovaGroups(), getContext()));
            novaGroup.setImageResource(Utils.getNovaGroupDrawable(product));
            novaGroup.setOnClickListener((View v) -> {
                Uri uri = Uri.parse(getString(R.string.url_nova_groups));
                CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
                CustomTabActivityHelper.openCustomTab(IngredientsProductFragment.this.getActivity(), customTabsIntent, uri, new WebViewFallback());
            });
        } else {
            novaLayout.setVisibility(View.GONE);
        }
    }

    private String getTracesName(String languageCode, String tag) {
        AllergenName allergenName = mAllergenNameDao.queryBuilder().where(AllergenNameDao.Properties.AllergenTag.eq(tag), AllergenNameDao.Properties.LanguageCode.eq(languageCode))
            .unique();
        if (allergenName != null) {
            return allergenName.getName();
        }
        return tag;
    }

    private StringBuilder buildStringBuilder(List<String> stringList, String prefix) {
        StringBuilder otherNutritionStringBuilder = new StringBuilder();
        for (String otherSubstance : stringList) {
            otherNutritionStringBuilder.append(prefix);
            prefix = ", ";
            otherNutritionStringBuilder.append(trimLanguagePartFromString(otherSubstance));
        }
        return otherNutritionStringBuilder;
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
            spannableStringBuilder.setSpan(new ForegroundColorSpan(riskWarningColor), spannableStringBuilder.length() - riskWarningStr.length(), spannableStringBuilder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableStringBuilder;
    }

    private CharSequence getAllergensTag(AllergenName allergen) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (allergen.getIsWikiDataIdPresent()) {
                    apiClientForWikiData.doSomeThing(
                        allergen.getWikiDataId(),
                        (value, result) -> {
                            if (value) {
                                FragmentActivity activity = getActivity();
                                if (activity != null && !activity.isFinishing()) {
                                    BottomScreenCommon.showBottomScreen(result, allergen,
                                        activity.getSupportFragmentManager());
                                }
                            } else {
                                ProductBrowsingListActivity.startActivity(getContext(),
                                    allergen.getAllergenTag(),
                                    allergen.getName(),
                                    SearchType.ALLERGEN);
                            }
                        });
                } else {
                    ProductBrowsingListActivity.startActivity(getContext(),
                        allergen.getAllergenTag(),
                        allergen.getName(),
                        SearchType.ALLERGEN);
                }
            }
        };

        ssb.append(allergen.getName());
        ssb.setSpan(clickableSpan, 0, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        // If allergen is not in the taxonomy list then italicize it
        if (!allergen.isNotNull()) {
            StyleSpan iss =
                new StyleSpan(android.graphics.Typeface.ITALIC); //Span to make text italic
            ssb.setSpan(iss, 0, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ssb;
    }

    /**
     * @return the string after trimming the language code from the tags
     *     like it returns folic-acid for en:folic-acid
     */
    private String trimLanguagePartFromString(String string) {
        return string.substring(3);
    }

    private SpannableStringBuilder setSpanBoldBetweenTokens(CharSequence text, List<String> allergens) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        Matcher m = INGREDIENT_PATTERN.matcher(ssb);
        while (m.find()) {
            final String tm = m.group();
            final String allergenValue = tm.replaceAll("[(),.-]+", "");

            for (String allergen : allergens) {
                if (allergen.equalsIgnoreCase(allergenValue)) {
                    int start = m.start();
                    int end = m.end();

                    if (tm.contains("(")) {
                        start += 1;
                    } else if (tm.contains(")")) {
                        end -= 1;
                    }

                    ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        ssb.insert(0, Utils.bold(getString(R.string.txtIngredients) + ' '));
        return ssb;
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
        substanceProduct.setMovementMethod(LinkMovementMethod.getInstance());
        substanceProduct.setText(bold(getString(R.string.txtSubstances)));
        substanceProduct.append(" ");

        for (int i = 0, lastIdx = allergens.size() - 1; i <= lastIdx; i++) {
            AllergenName allergen = allergens.get(i);
            substanceProduct.append(getAllergensTag(allergen));
            // Add comma if not the last item
            if (i != lastIdx) {
                substanceProduct.append(", ");
            }
        }
    }

    @OnClick(R.id.change_ing_img)
    public void change_ing_image(View v) {
        sendUpdatedIngredientsImage = true;

        if (getActivity() == null) {
            return;
        }
        ViewPager viewPager = (ViewPager) getActivity().findViewById(
            R.id.pager);
        if (BuildConfig.FLAVOR.equals("off")) {
            final SharedPreferences settings = getActivity().getSharedPreferences("login", 0);
            final String login = settings.getString("user", "");
            if (login.isEmpty()) {
                new MaterialDialog.Builder(getContext())
                    .title(R.string.sign_in_to_edit)
                    .positiveText(R.string.txtSignIn)
                    .negativeText(R.string.dialog_cancel)
                    .onPositive((dialog, which) -> {
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivityForResult(intent, LOGIN_ACTIVITY_REQUEST_CODE);
                        dialog.dismiss();
                    })
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .build().show();
            } else {
                mState = getStateFromActivityIntent();
                if (mState != null) {
                    Intent intent = new Intent(getContext(), AddProductActivity.class);
                    intent.putExtra("send_updated", sendUpdatedIngredientsImage);
                    intent.putExtra("edit_product", mState.getProduct());
                    startActivityForResult(intent, EDIT_REQUEST_CODE);
                }
            }
        }
        if (BuildConfig.FLAVOR.equals("opff")) {
            viewPager.setCurrentItem(4);
        }

        if (BuildConfig.FLAVOR.equals("obf")) {
            viewPager.setCurrentItem(1);
        }

        if (BuildConfig.FLAVOR.equals("opf")) {
            viewPager.setCurrentItem(0);
        }
    }

    @Override
    public void showAllergensState(String state) {
        switch (state) {
            case LOADING: {
                substanceProduct.setVisibility(View.VISIBLE);
                substanceProduct.append(getString(R.string.txtLoading));
                break;
            }
            case EMPTY: {
                substanceProduct.setVisibility(View.GONE);
                break;
            }
        }
    }

    private List<String> getAllergens() {
        List<String> allergens = mState.getProduct().getAllergensTags();
        if (mState.getProduct() == null || allergens == null || allergens.isEmpty()) {
            return Collections.emptyList();
        } else {
            return allergens;
        }
    }

    @OnClick(R.id.novaMethodLink)
    void novaMethodLinkDisplay() {
        if(mState!=null && mState.getProduct()!=null &&mState.getProduct().getNovaGroups() != null) {
            Uri uri = Uri.parse(getString(R.string.url_nova_groups));
            CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
            CustomTabActivityHelper.openCustomTab(IngredientsProductFragment.this.getActivity(), customTabsIntent, uri, new WebViewFallback());
        }
    }

    @OnClick(R.id.extract_ingredients_prompt)
    public void extractIngredients() {
        extractIngredients = true;
        final SharedPreferences settings = getActivity().getSharedPreferences("login", 0);
        final String login = settings.getString("user", "");
        if (login.isEmpty()) {
            new MaterialDialog.Builder(getContext())
                .title(R.string.sign_in_to_edit)
                .positiveText(R.string.txtSignIn)
                .negativeText(R.string.dialog_cancel)
                .onPositive((dialog, which) -> {
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivityForResult(intent, LOGIN_ACTIVITY_REQUEST_CODE);
                    dialog.dismiss();
                })
                .onNegative((dialog, which) -> dialog.dismiss())
                .build().show();
        } else {
            mState = getStateFromActivityIntent();
            Intent intent = new Intent(getContext(), AddProductActivity.class);
            intent.putExtra("edit_product", mState.getProduct());
            intent.putExtra("perform_ocr", extractIngredients);
            startActivityForResult(intent, EDIT_REQUEST_CODE);
        }
    }

    @OnClick(R.id.imageViewIngredients)
    public void openFullScreen(View v) {
        if (mUrlImage != null) {
            Intent intent = new Intent(v.getContext(), FullScreenImage.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageurl", mUrlImage);
            intent.putExtras(bundle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(getActivity(), (View) mImageIngredients,
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
                EasyImage.openCamera(this, 0);
            }
        }
    }

    private void onPhotoReturned(File photoFile) {
        ProductImage image = new ProductImage(barcode, INGREDIENTS, photoFile);
        image.setFilePath(photoFile.getAbsolutePath());
        api.postImg(getContext(), image, null);
        addPhotoLabel.setVisibility(View.GONE);
        mUrlImage = photoFile.getAbsolutePath();

        Picasso.with(getContext())
            .load(photoFile)
            .fit()
            .into(mImageIngredients);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //added case for sending updated ingredients image
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(getContext(), AddProductActivity.class);
            intent.putExtra("send_updated", sendUpdatedIngredientsImage);
            intent.putExtra("perform_ocr", extractIngredients);
            intent.putExtra("edit_product", mState.getProduct());
            startActivity(intent);
        }
        if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            onRefresh();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                onPhotoReturned(new File(resultUri.getPath()));
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
                    .setOutputUri(Utils.getOutputPicUri(getContext()))
                    .start(getContext(), mFragment);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(getContext());
                    if (photoFile != null) {
                        photoFile.delete();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                    EasyImage.openCamera(this, 0);
                }
            }
        }
    }

    public String getIngredients() {
        return mUrlImage;
    }

    @Override
    public void onDestroyView() {
        if(presenter!=null) {
            presenter.dispose();
        }
        super.onDestroyView();
    }
}
