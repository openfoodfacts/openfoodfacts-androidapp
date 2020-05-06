package openfoodfacts.github.scrachx.openfood.views.product.summary;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentSummaryProductBinding;
import openfoodfacts.github.scrachx.openfood.fragments.AdditiveFragmentHelper;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.fragments.CategoryProductHelper;
import openfoodfacts.github.scrachx.openfood.images.PhotoReceiver;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.jobs.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.AllergenHelper;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagConfig;
import openfoodfacts.github.scrachx.openfood.models.BottomScreenCommon;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.InsightAnnotationResponse;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevels;
import openfoodfacts.github.scrachx.openfood.models.NutrimentLevel;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.models.ProductLists;
import openfoodfacts.github.scrachx.openfood.models.ProductListsDao;
import openfoodfacts.github.scrachx.openfood.models.Question;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.models.Tag;
import openfoodfacts.github.scrachx.openfood.models.TagDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.WikidataApiClient;
import openfoodfacts.github.scrachx.openfood.utils.ImageUploadListener;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState;
import openfoodfacts.github.scrachx.openfood.utils.ProductUtils;
import openfoodfacts.github.scrachx.openfood.utils.QuestionActionListeners;
import openfoodfacts.github.scrachx.openfood.utils.QuestionDialog;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.FullScreenActivityOpener;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductComparisonActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductImageManagementActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductListsActivity;
import openfoodfacts.github.scrachx.openfood.views.YourListedProducts;
import openfoodfacts.github.scrachx.openfood.views.adapters.DialogAddToListAdapter;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrientLevelListAdapter;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.views.product.ProductActivity;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis.IngredientsWithTagDialogFragment;

public class SummaryProductFragment extends BaseFragment implements CustomTabActivityHelper.ConnectionCallback, ISummaryProductPresenter.View, ImageUploadListener, PhotoReceiver {
    private static final int EDIT_PRODUCT_AFTER_LOGIN = 1;
    private static final int EDIT_PRODUCT_NUTRITION_AFTER_LOGIN = 3;
    private static final int EDIT_REQUEST_CODE = 2;
    private OpenFoodAPIClient api;
    private WikidataApiClient apiClientForWikiData;
    private String barcode;
    private FragmentSummaryProductBinding binding;
    private CustomTabActivityHelper customTabActivityHelper;
    private CustomTabsIntent customTabsIntent;
    private boolean hasCategoryInsightQuestion = false;
    //boolean to determine if image should be loaded or not
    private boolean isLowBatteryMode = false;
    private TagDao mTagDao;
    private String mUrlImage;
    private Uri nutritionScoreUri;
    private PhotoReceiverHandler photoReceiverHandler;
    private ISummaryProductPresenter.Actions presenter;
    private Product product;
    private Question productQuestion = null;
    private boolean sendOther = false;
    //boolean to determine if category prompt should be shown
    private boolean showCategoryPrompt = false;
    //boolean to determine if nutrient prompt should be shown
    private boolean showNutrientPrompt = false;
    private State state;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());

        presenter = new SummaryProductPresenter(product, this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        api = new OpenFoodAPIClient(getActivity());
        apiClientForWikiData = new WikidataApiClient();
        binding = FragmentSummaryProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //done here for android 4 compatibility.
        //a better solution could be to use https://developer.android.com/jetpack/androidx/releases/ but weird issue with it..
        binding.addNutriscorePrompt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_box_blue_18dp, 0, 0, 0);
        binding.buttonMorePictures.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_a_photo_blue_18dp, 0, 0, 0);
        photoReceiverHandler = new PhotoReceiverHandler(this);

        binding.imageViewFront.setOnClickListener(v -> openFullScreen());
        binding.buttonMorePictures.setOnClickListener(v -> takeMorePicture());
        binding.actionAddToListButton.setOnClickListener(v -> onBookmarkProductButtonClick());
        binding.actionEditButton.setOnClickListener(v -> onEditProductButtonClick());
        binding.actionShareButton.setOnClickListener(v -> onShareProductButtonClick());
        binding.actionCompareButton.setOnClickListener(v -> onCompareProductButtonClick());
        binding.addNutriscorePrompt.setOnClickListener(v -> onAddNutriScorePromptClick());
        binding.productQuestionDismiss.setOnClickListener(v -> productQuestionDismiss());
        binding.productQuestionLayout.setOnClickListener(v -> onProductQuestionClick());

        state = getStateFromActivityIntent();
        refreshView(state);
    }

    @Override
    public void refreshView(State state) {
        // No state -> we can't display anything.
        if (state == null) {
            return;
        }

        super.refreshView(state);
        this.state = state;
        product = state.getProduct();
        presenter = new SummaryProductPresenter(product, this);
        binding.textCategoryProduct.setText(Utils.bold(getString(R.string.txtCategories)));
        binding.textLabelProduct.setText(Utils.bold(getString(R.string.txtLabels)));

        //refresh visibility of UI components
        binding.textLabelProduct.setVisibility(View.VISIBLE);
        binding.textBrandProduct.setVisibility(View.VISIBLE);
        binding.textQuantityProduct.setVisibility(View.VISIBLE);
        binding.textEmbCode.setVisibility(View.VISIBLE);
        binding.textNameProduct.setVisibility(View.VISIBLE);

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        if (Utils.isDisableImageLoad(getContext()) && Utils.getBatteryLevel(getContext())) {
            isLowBatteryMode = true;
        }

        //checks the product states_tags to determine which prompt to be shown
        refreshNutriscorePrompt();

        presenter.loadAllergens(null);
        presenter.loadCategories();
        presenter.loadLabels();
        presenter.loadProductQuestion();
        binding.textAdditiveProduct.setText(Utils.bold(getString(R.string.txtAdditives)));
        presenter.loadAdditives();
        presenter.loadAnalysisTags();

        mTagDao = Utils.getAppDaoSession(getActivity()).getTagDao();
        barcode = product.getCode();
        String langCode = LocaleHelper.getLanguage(getContext());

        final String imageUrl = product.getImageUrl(langCode);
        if (StringUtils.isNotBlank(imageUrl)) {
            binding.addPhotoLabel.setVisibility(View.GONE);

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Picasso.get()
                    .load(imageUrl)
                    .into(binding.imageViewFront);
            } else {
                binding.imageViewFront.setVisibility(View.GONE);
            }

            mUrlImage = imageUrl;
        }

        //TODO use OpenFoodApiService to fetch product by packaging, brands, categories etc

        if (product.getProductName(langCode) != null) {
            binding.textNameProduct.setText(product.getProductName(langCode));
        } else {
            binding.textNameProduct.setVisibility(View.GONE);
        }

        if (StringUtils.isNotBlank(product.getQuantity())) {
            binding.textQuantityProduct.setText(product.getQuantity());
        } else {
            binding.textQuantityProduct.setVisibility(View.GONE);
        }

        if (StringUtils.isNotBlank(product.getBrands())) {
            binding.textBrandProduct.setClickable(true);
            binding.textBrandProduct.setMovementMethod(LinkMovementMethod.getInstance());
            binding.textBrandProduct.setText("");

            String[] brands = product.getBrands().split(",");
            for (int i = 0; i < brands.length; i++) {
                if (i > 0) {
                    binding.textBrandProduct.append(", ");
                }
                binding.textBrandProduct.append(Utils.getClickableText(brands[i].trim(), "", SearchType.BRAND, getActivity(), customTabsIntent));
            }
        } else {
            binding.textBrandProduct.setVisibility(View.GONE);
        }

        if (product.getEmbTags() != null && !product.getEmbTags().toString().trim().equals("[]")) {
            binding.textEmbCode.setMovementMethod(LinkMovementMethod.getInstance());
            binding.textEmbCode.setText(Utils.bold(getString(R.string.txtEMB)));
            binding.textEmbCode.append(" ");

            String[] embTags = product.getEmbTags().toString().replace("[", "").replace("]", "").split(", ");
            for (int i = 0; i < embTags.length; i++) {
                if (i > 0) {
                    binding.textEmbCode.append(", ");
                }
                String embTag = embTags[i];
                binding.textEmbCode.append(Utils.getClickableText(getEmbCode(embTag).trim(), getEmbUrl(embTag), SearchType.EMB, getActivity(), customTabsIntent));
            }
        } else {
            binding.textEmbCode.setVisibility(View.GONE);
        }

        // if the device does not have a camera, hide the button
        try {
            if (!Utils.isHardwareCameraInstalled(getContext())) {
                binding.buttonMorePictures.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            if (BuildConfig.DEBUG) {
                Log.i(getClass().getSimpleName(), e.toString());
            }
        }

        if (BuildConfig.FLAVOR.equals("off")) {
            binding.scoresLayout.setVisibility(View.VISIBLE);
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

            final boolean inVolume = ProductUtils.isPerServingInLiter(product);
            binding.textNutrientTxt.setText(inVolume ? R.string.txtNutrientLevel100ml : R.string.txtNutrientLevel100g);

            if (!(fat == null && salt == null && saturatedFat == null && sugars == null)) {
                // prefetch the uri
                // currently only available in french translations
                nutritionScoreUri = Uri.parse(getString(R.string.nutriscore_uri));
                customTabActivityHelper.mayLaunchUrl(nutritionScoreUri, null, null);
                Context context = this.getContext();

                if (nutriments != null) {
                    binding.cvNutritionLights.setVisibility(View.VISIBLE);
                    Nutriments.Nutriment fatNutriment = nutriments.get(Nutriments.FAT);
                    if (fat != null && fatNutriment != null) {
                        String fatNutrimentLevel = fat.getLocalize(context);
                        levelItem.add(new NutrientLevelItem(getString(R.string.txtFat),
                            fatNutriment.getDisplayStringFor100g(),
                            fatNutrimentLevel,
                            fat.getImageLevel()));
                    }

                    Nutriments.Nutriment saturatedFatNutriment = nutriments.get(Nutriments.SATURATED_FAT);
                    if (saturatedFat != null && saturatedFatNutriment != null) {
                        String saturatedFatLocalize = saturatedFat.getLocalize(context);
                        levelItem.add(new NutrientLevelItem(getString(R.string.txtSaturatedFat), saturatedFatNutriment.getDisplayStringFor100g(),
                            saturatedFatLocalize,
                            saturatedFat.getImageLevel()));
                    }

                    Nutriments.Nutriment sugarsNutriment = nutriments.get(Nutriments.SUGARS);
                    if (sugars != null && sugarsNutriment != null) {
                        String sugarsLocalize = sugars.getLocalize(context);
                        levelItem.add(new NutrientLevelItem(getString(R.string.txtSugars),
                            sugarsNutriment.getDisplayStringFor100g(),
                            sugarsLocalize,
                            sugars.getImageLevel()));
                    }

                    Nutriments.Nutriment saltNutriment = nutriments.get(Nutriments.SALT);
                    if (salt != null && saltNutriment != null) {
                        String saltLocalize = salt.getLocalize(context);
                        levelItem.add(new NutrientLevelItem(getString(R.string.txtSalt),
                            saltNutriment.getDisplayStringFor100g(),
                            saltLocalize,
                            salt.getImageLevel()));
                    }
                }
            } else {
                binding.cvNutritionLights.setVisibility(View.GONE);
            }

            binding.listNutrientLevels.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.listNutrientLevels.setAdapter(new NutrientLevelListAdapter(getContext(), levelItem));

            refreshNutriscore();
            refreshNovaIcon();
            refreshCo2Icon();
            refreshScoresLayout();
        } else {
            binding.scoresLayout.setVisibility(View.GONE);
        }
        //to be sure that top of the product view is visible at start
        binding.textNameProduct.requestFocus();
        binding.textNameProduct.clearFocus();
    }

    private void refreshScoresLayout() {
        if (binding.novaGroup.getVisibility() == View.GONE &&
            binding.co2Icon.getVisibility() == View.GONE &&
            binding.imageGrade.getVisibility() == View.GONE &&
            binding.addNutriscorePrompt.getVisibility() == View.GONE) {
            binding.scoresLayout.setVisibility(View.GONE);
        } else {
            binding.scoresLayout.setVisibility(View.VISIBLE);
        }
    }

    private void refreshNutriscore() {
        int nutritionGradeResource = Utils.getImageGrade(product);
        if (nutritionGradeResource != Utils.NO_DRAWABLE_RESOURCE) {
            binding.imageGrade.setVisibility(View.VISIBLE);
            binding.imageGrade.setImageResource(nutritionGradeResource);
            binding.imageGrade.setOnClickListener(view1 -> {
                CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
                CustomTabActivityHelper.openCustomTab(SummaryProductFragment.this.getActivity(), customTabsIntent, nutritionScoreUri, new WebViewFallback());
            });
        } else {
            binding.imageGrade.setVisibility(View.GONE);
        }
    }

    private void refreshNovaIcon() {
        if (product.getNovaGroups() != null) {
            binding.novaGroup.setVisibility(View.VISIBLE);
            binding.novaGroup.setImageResource(Utils.getNovaGroupDrawable(product.getNovaGroups()));
            binding.novaGroup.setOnClickListener(view1 -> {
                Uri uri = Uri.parse(getString(R.string.url_nova_groups));
                CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
                CustomTabActivityHelper.openCustomTab(SummaryProductFragment.this.getActivity(), customTabsIntent, uri, new WebViewFallback());
            });
        } else {
            binding.novaGroup.setVisibility(View.GONE);
            binding.novaGroup.setImageResource(0);
        }
    }

    private void refreshCo2Icon() {
        int environmentImpactResource = Utils.getImageEnvironmentImpact(product);
        if (environmentImpactResource != Utils.NO_DRAWABLE_RESOURCE) {
            binding.co2Icon.setVisibility(View.VISIBLE);
            binding.co2Icon.setImageResource(environmentImpactResource);
        } else {
            binding.co2Icon.setVisibility(View.GONE);
        }
    }

    private void refreshNutriscorePrompt() {
        //checks the product states_tags to determine which prompt to be shown
        List<String> statesTags = product.getStatesTags();
        showCategoryPrompt = (statesTags.contains("en:categories-to-be-completed") &&
            !hasCategoryInsightQuestion);
        Log.e("showCat", String.valueOf(showCategoryPrompt));

        if (product.getNoNutritionData() != null && product.getNoNutritionData().equals("on")) {
            showNutrientPrompt = false;
        } else {
            if (statesTags.contains("en:nutrition-facts-to-be-completed")) {
                showNutrientPrompt = true;
            }
        }

        if (showNutrientPrompt || showCategoryPrompt) {
            binding.addNutriscorePrompt.setVisibility(View.VISIBLE);
            if (showNutrientPrompt && showCategoryPrompt) {
                // Both true
                binding.addNutriscorePrompt.setText(getString(R.string.add_nutrient_category_prompt_text));
            } else if (showNutrientPrompt) {
                // showNutrientPrompt true
                binding.addNutriscorePrompt.setText(getString(R.string.add_nutrient_prompt_text));
            } else {
                // showCategoryPrompt true
                binding.addNutriscorePrompt.setText(getString(R.string.add_category_prompt_text));
            }
        } else {
            binding.addNutriscorePrompt.setVisibility(View.GONE);
        }
    }

    @Override
    public void showAdditives(List<AdditiveName> additives) {
        AdditiveFragmentHelper.showAdditives(additives, binding.textAdditiveProduct, apiClientForWikiData, this);
    }

    @Override
    public void showAdditivesState(String state) {
        getActivity().runOnUiThread(() -> {
            if (ProductInfoState.LOADING.equals(state)) {
                binding.textAdditiveProduct.append(getString(R.string.txtLoading));
                binding.textAdditiveProduct.setVisibility(View.VISIBLE);
            } else if (ProductInfoState.EMPTY.equals(state)) {
                binding.textAdditiveProduct.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void showAnalysisTags(List<AnalysisTagConfig> analysisTags) {
        getActivity().runOnUiThread(() -> {
            binding.analysisContainer.setVisibility(View.VISIBLE);
            IngredientAnalysisTagsAdapter adapter = new IngredientAnalysisTagsAdapter(getContext(), analysisTags);
            adapter.setOnItemClickListener((view, position) -> {
                IngredientsWithTagDialogFragment fragment = IngredientsWithTagDialogFragment
                    .newInstance(product, (AnalysisTagConfig) view.getTag(R.id.analysis_tag_config));
                fragment.show(getChildFragmentManager(), "fragment_ingredients_with_tag");

                fragment.setOnDismissListener(dialog -> adapter.filterVisibleTags());
            });

            binding.analysisTags.setAdapter(adapter);
        });
    }

    @Override
    public void showAllergens(List<AllergenName> allergens) {
        final AllergenHelper.Data data = AllergenHelper.computeUserAllergen(product, allergens);
        if (data.isEmpty()) {
            return;
        }

        if (data.isIncomplete()) {
            binding.productAllergenAlertText.setText(R.string.product_incomplete_message);
            binding.productAllergenAlertLayout.setVisibility(View.VISIBLE);
            return;
        }

        String text = String.format("%s\n", getResources().getString(R.string.product_allergen_prompt)) +
            StringUtils.join(data.getAllergens(), ", ");
        binding.productAllergenAlertText.setText(text);
        binding.productAllergenAlertLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void showCategories(List<CategoryName> categories) {
        CategoryProductHelper categoryProductHelper = new CategoryProductHelper(binding.textCategoryProduct, categories, this, apiClientForWikiData);
        categoryProductHelper.showCategories();

        if (categoryProductHelper.getContainsAlcohol()) {
            categoryProductHelper.showAlcoholAlert(binding.textCategoryAlcoholAlert);
        }
    }

    @Override
    public void showProductQuestion(Question question) {
        if (Utils.isUserLoggedIn(getContext()) && question != null && !question.isEmpty()) {
            productQuestion = question;
            binding.productQuestionText.setText(String.format("%s%n%s",
                question.getQuestion(), question.getValue()));
            binding.productQuestionLayout.setVisibility(View.VISIBLE);
            hasCategoryInsightQuestion = question.getInsightType().equals("category");
        } else {
            binding.productQuestionLayout.setVisibility(View.GONE);
            productQuestion = null;
        }
        refreshNutriscorePrompt();
        refreshScoresLayout();
    }

    private void onProductQuestionClick() {
        if (productQuestion == null && !Utils.isUserLoggedIn(getContext())) {
            return;
        }
        new QuestionDialog(getActivity())
            .setBackgroundColor(R.color.colorPrimaryDark)
            .setQuestion(productQuestion.getQuestion())
            .setValue(productQuestion.getValue())
            .setOnReviewClickListener(new QuestionActionListeners() {
                @Override
                public void onPositiveFeedback(QuestionDialog dialog) {
                    //init POST request
                    sendProductInsights(productQuestion.getInsightId(), 1);
                    dialog.dismiss();
                }

                @Override
                public void onNegativeFeedback(QuestionDialog dialog) {
                    sendProductInsights(productQuestion.getInsightId(), 0);
                    dialog.dismiss();
                }

                @Override
                public void onAmbiguityFeedback(QuestionDialog dialog) {
                    sendProductInsights(productQuestion.getInsightId(), -1);
                    dialog.dismiss();
                }

                @Override
                public void onCancelListener(DialogInterface dialog) {
                    dialog.dismiss();
                }
            })
            .show();
    }

    public void sendProductInsights(String insightId, int annotation) {
        Log.d("SummaryProductFragment",
            String.format("Annotation %d received for insight %s", annotation, insightId));
        presenter.annotateInsight(insightId, annotation);
        binding.productQuestionLayout.setVisibility(View.GONE);
        productQuestion = null;
    }

    public void showAnnotatedInsightToast(InsightAnnotationResponse response) {
        if (response.getStatus().equals("updated") && getActivity() != null) {
            Toast toast = Toast.makeText(getActivity(), R.string.product_question_submit_message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 500);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void productQuestionDismiss() {
        binding.productQuestionLayout.setVisibility(View.GONE);
    }

    @Override
    public void showLabels(List<LabelName> labels) {
        binding.textLabelProduct.setText(Utils.bold(getString(R.string.txtLabels)));
        binding.textLabelProduct.setClickable(true);
        binding.textLabelProduct.setMovementMethod(LinkMovementMethod.getInstance());
        binding.textLabelProduct.append(" ");

        for (int i = 0; i < labels.size() - 1; i++) {
            binding.textLabelProduct.append(getLabelTag(labels.get(i)));
            binding.textLabelProduct.append(", ");
        }

        binding.textLabelProduct.append(getLabelTag(labels.get(labels.size() - 1)));
    }

    @Override
    public void showCategoriesState(String state) {
        getActivity().runOnUiThread(() -> {
            if (ProductInfoState.LOADING.equals(state)) {
                if (getContext() != null) {
                    binding.textCategoryProduct.append(getString(R.string.txtLoading));
                }
            } else if (ProductInfoState.EMPTY.equals(state)) {
                binding.textCategoryProduct.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void showLabelsState(String state) {
        getActivity().runOnUiThread(() -> {
            if (ProductInfoState.LOADING.equals(state)) {
                binding.textLabelProduct.append(getString(R.string.txtLoading));
            } else if (ProductInfoState.EMPTY.equals(state)) {
                binding.textLabelProduct.setVisibility(View.GONE);
            }
        });
    }

    private String getEmbUrl(String embTag) {
        Tag tag = mTagDao.queryBuilder().where(TagDao.Properties.Id.eq(embTag)).unique();
        if (tag != null) {
            return tag.getName();
        }
        return null;
    }

    private String getEmbCode(String embTag) {
        Tag tag = mTagDao.queryBuilder().where(TagDao.Properties.Id.eq(embTag)).unique();
        if (tag != null) {
            return tag.getName();
        }
        return embTag;
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

        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableStringBuilder;
    }

    private void onAddNutriScorePromptClick() {
        if (BuildConfig.FLAVOR.equals("off")) {
            if (isUserNotLoggedIn()) {
                startLoginToEditAnd(EDIT_PRODUCT_NUTRITION_AFTER_LOGIN);
            } else {
                editProductNutriscore();
            }
        }
    }

    private void editProductNutriscore() {
        Intent intent = new Intent(getActivity(), AddProductActivity.class);
        intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, product);
        //adds the information about the prompt when navigating the user to the edit the product
        intent.putExtra(AddProductActivity.MODIFY_CATEGORY_PROMPT, showCategoryPrompt);
        intent.putExtra(AddProductActivity.MODIFY_NUTRITION_PROMPT, showNutrientPrompt);
        startActivity(intent);
    }

    private void onCompareProductButtonClick() {
        Intent intent = new Intent(getActivity(), ProductComparisonActivity.class);
        intent.putExtra("product_found", true);
        ArrayList<Product> productsToCompare = new ArrayList<>();
        productsToCompare.add(product);
        intent.putExtra("products_to_compare", productsToCompare);
        startActivity(intent);
    }

    private void onShareProductButtonClick() {
        String shareUrl = " " + getString(R.string.website_product) + product.getCode();
        Intent sharingIntent = new Intent();
        sharingIntent.setAction(Intent.ACTION_SEND);
        sharingIntent.setType(OpenFoodAPIClient.TEXT_PLAIN);
        String shareBody = getResources().getString(R.string.msg_share) + shareUrl;
        String shareSub = "\n\n";
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    private void onEditProductButtonClick() {
        if (isUserNotLoggedIn()) {
            startLoginToEditAnd(EDIT_PRODUCT_AFTER_LOGIN);
        } else {
            editProduct();
        }
    }

    private void editProduct() {
        Intent intent = new Intent(getActivity(), AddProductActivity.class);
        intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, product);
        startActivityForResult(intent, EDIT_REQUEST_CODE);
    }

    private void onBookmarkProductButtonClick() {
        Activity activity = getActivity();

        String productBarcode = product.getCode();
        String productName = product.getProductName();
        String imageUrl = product.getImageSmallUrl(LocaleHelper.getLanguage(getContext()));
        String productDetails = YourListedProducts.getProductBrandsQuantityDetails(product);

        MaterialDialog.Builder addToListBuilder = new MaterialDialog.Builder(activity)
            .title(R.string.add_to_product_lists)
            .customView(R.layout.dialog_add_to_list, true);
        MaterialDialog addToListDialog = addToListBuilder.build();
        addToListDialog.show();
        View addToListView = addToListDialog.getCustomView();
        if (addToListView != null) {
            ProductListsDao productListsDao = ProductListsActivity.getProducListsDaoWithDefaultList(this.getContext());
            List<ProductLists> productLists = productListsDao.loadAll();

            RecyclerView addToListRecyclerView =
                addToListView.findViewById(R.id.rv_dialogAddToList);
            DialogAddToListAdapter addToListAdapter =
                new DialogAddToListAdapter(activity, productLists, productBarcode, productName, productDetails, imageUrl);
            addToListRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            addToListRecyclerView.setAdapter(addToListAdapter);
            TextView tvAddToList = addToListView.findViewById(R.id.tvAddToNewList);
            tvAddToList.setOnClickListener(view -> {
                Intent intent = new Intent(activity, ProductListsActivity.class);
                intent.putExtra("product", product);
                activity.startActivity(intent);
            });
        }
    }

    @Override
    public void onCustomTabsConnected() {
        binding.imageGrade.setClickable(true);
    }

    @Override
    public void onCustomTabsDisconnected() {
        binding.imageGrade.setClickable(false);
    }

    private void takeMorePicture() {
        sendOther = true;
        doChooseOrTakePhotos(getString(R.string.take_more_pictures));
    }

    private void openFullScreen() {
        if (mUrlImage != null) {
            FullScreenActivityOpener.openForUrl(this, product, ProductImageField.FRONT, mUrlImage, binding.imageViewFront);
        } else {
            // take a picture
            newFrontImage();
        }
    }

    void newFrontImage() {
        // add front image.
        sendOther = false;
        doChooseOrTakePhotos(getString(R.string.set_img_front));
    }

    private void loadPhoto(File photoFile) {
        ProductImage image = new ProductImage(barcode, ProductImageField.FRONT, photoFile);
        image.setFilePath(photoFile.getAbsolutePath());
        api.postImg(getContext(), image, this);
        binding.addPhotoLabel.setVisibility(View.GONE);
        mUrlImage = photoFile.getAbsolutePath();

        Picasso.get()
            .load(photoFile)
            .fit()
            .into(binding.imageViewFront);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data);
        boolean shouldRefresh = (requestCode == EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data.getBooleanExtra(AddProductActivity.UPLOADED_TO_SERVER, false));
        if (ProductImageManagementActivity.isImageModified(requestCode, resultCode)) {
            shouldRefresh = true;
        }
        if (shouldRefresh && getActivity() instanceof ProductActivity) {
            ((ProductActivity) getActivity()).onRefresh();
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == EDIT_PRODUCT_AFTER_LOGIN && isUserLoggedIn()) {
                editProduct();
            }
            if (requestCode == EDIT_PRODUCT_NUTRITION_AFTER_LOGIN && isUserLoggedIn()) {
                editProductNutriscore();
            }
        }
    }

    public void onPhotoReturned(File newPhotoFile) {
        URI resultUri = newPhotoFile.toURI();
        //the booleans are checked to determine if the picture uploaded was due to a prompt click
        //the pictures are uploaded with the correct path
        if (!sendOther) {
            loadPhoto(new File(resultUri.getPath()));
        } else {
            ProductImage image = new ProductImage(barcode, ProductImageField.OTHER, newPhotoFile);
            image.setFilePath(resultUri.getPath());
            showOtherImageProgress();
            api.postImg(getContext(), image, this);
        }
    }

    @Override
    protected void doOnPhotosPermissionGranted() {
        if (sendOther) {
            takeMorePicture();
        } else {
            newFrontImage();
        }
    }

    @Override
    public void onDestroyView() {
        presenter.dispose();
        super.onDestroyView();
        binding = null;
    }

    public void showOtherImageProgress() {
        binding.uploadingImageProgress.setVisibility(View.VISIBLE);
        binding.uploadingImageProgressText.setVisibility(View.VISIBLE);
        binding.uploadingImageProgressText.setText(R.string.toastSending);
    }

    @Override
    public void onSuccess() {
        binding.uploadingImageProgress.setVisibility(View.GONE);
        binding.uploadingImageProgressText.setText(R.string.image_uploaded_successfully);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onFailure(String message) {
        binding.uploadingImageProgress.setVisibility(View.GONE);
        binding.uploadingImageProgressText.setVisibility(View.GONE);
        Context context = getContext();
        if (context == null) {
            context = OFFApplication.getInstance();
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void resetScroll() {
        binding.scrollView.scrollTo(0, 0);

        if (binding.analysisTags.getAdapter() != null) {
            ((IngredientAnalysisTagsAdapter) binding.analysisTags.getAdapter()).filterVisibleTags();
        }
    }
}
