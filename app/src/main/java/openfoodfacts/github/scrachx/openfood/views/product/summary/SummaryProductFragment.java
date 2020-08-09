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

package openfoodfacts.github.scrachx.openfood.views.product.summary;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import androidx.activity.result.contract.ActivityResultContracts;
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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import openfoodfacts.github.scrachx.openfood.AppFlavors;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentSummaryProductBinding;
import openfoodfacts.github.scrachx.openfood.fragments.AdditiveFragmentHelper;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.fragments.CategoryProductHelper;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.AnnotationAnswer;
import openfoodfacts.github.scrachx.openfood.models.AnnotationResponse;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevels;
import openfoodfacts.github.scrachx.openfood.models.NutrimentLevel;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.models.ProductState;
import openfoodfacts.github.scrachx.openfood.models.Question;
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists;
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenHelper;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig;
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelName;
import openfoodfacts.github.scrachx.openfood.models.entities.tag.Tag;
import openfoodfacts.github.scrachx.openfood.models.entities.tag.TagDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.WikiDataApiClient;
import openfoodfacts.github.scrachx.openfood.utils.BottomScreenCommon;
import openfoodfacts.github.scrachx.openfood.utils.FragmentUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState;
import openfoodfacts.github.scrachx.openfood.utils.ProductUtils;
import openfoodfacts.github.scrachx.openfood.utils.QuestionActionListeners;
import openfoodfacts.github.scrachx.openfood.utils.QuestionDialog;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.FullScreenActivityOpener;
import openfoodfacts.github.scrachx.openfood.views.LoginActivity;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductComparisonActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductImageManagementActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductListsActivity;
import openfoodfacts.github.scrachx.openfood.views.YourListedProductsActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.DialogAddToListAdapter;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrientLevelListAdapter;
import openfoodfacts.github.scrachx.openfood.views.product.ProductActivity;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis.IngredientsWithTagDialogFragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static openfoodfacts.github.scrachx.openfood.AppFlavors.OFF;

public class SummaryProductFragment extends BaseFragment implements CustomTabActivityHelper.ConnectionCallback, ISummaryProductPresenter.View {
    private static final int EDIT_PRODUCT_AFTER_LOGIN = 1;
    private static final int EDIT_PRODUCT_NUTRITION_AFTER_LOGIN = 3;
    private static final int EDIT_REQUEST_CODE = 2;
    private OpenFoodAPIClient api;
    private WikiDataApiClient apiClientForWikiData;
    private String barcode;
    private FragmentSummaryProductBinding binding;
    private CustomTabActivityHelper customTabActivityHelper;
    private CustomTabsIntent customTabsIntent;
    private CompositeDisposable disp;
    private boolean hasCategoryInsightQuestion = false;

    private void onImageListenerComplete() {
        binding.uploadingImageProgress.setVisibility(GONE);
        binding.uploadingImageProgressText.setText(R.string.image_uploaded_successfully);
    }

    private void onImageListenerError(Throwable error) {
        binding.uploadingImageProgress.setVisibility(GONE);
        binding.uploadingImageProgressText.setVisibility(GONE);
        Context context = getContext();
        if (context == null) {
            context = OFFApplication.getInstance();
        }
        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
    }

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
    private ProductState productState;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper.getSession());

        presenter = new SummaryProductPresenter(product, this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        disp = new CompositeDisposable();
        api = new OpenFoodAPIClient(requireActivity());
        apiClientForWikiData = new WikiDataApiClient();
        binding = FragmentSummaryProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //done here for android 4 compatibility.
        //a better solution could be to use https://developer.android.com/jetpack/androidx/releases/ but weird issue with it..
        binding.addNutriscorePrompt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_box_blue_18dp, 0, 0, 0);
        photoReceiverHandler = new PhotoReceiverHandler(newPhotoFile -> {
            URI resultUri = newPhotoFile.toURI();
            //the booleans are checked to determine if the picture uploaded was due to a prompt click
            //the pictures are uploaded with the correct path
            if (!sendOther) {
                loadPhoto(new File(resultUri.getPath()));
            } else {
                ProductImage image = new ProductImage(barcode, ProductImageField.OTHER, newPhotoFile);
                image.setFilePath(resultUri.getPath());
                showOtherImageProgress();

                disp.add(api.postImg(image).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        onImageListenerComplete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        onImageListenerError(error);
                    }
                }));
            }
        });

        binding.imageViewFront.setOnClickListener(v -> openFrontImageFullscreen());
        binding.buttonMorePictures.setOnClickListener(v -> takeMorePicture());
        binding.actionAddToListButton.setOnClickListener(v -> onBookmarkProductButtonClick());
        binding.actionEditButton.setOnClickListener(v -> onEditProductButtonClick());
        binding.actionShareButton.setOnClickListener(v -> onShareProductButtonClick());
        binding.actionCompareButton.setOnClickListener(v -> onCompareProductButtonClick());
        binding.addNutriscorePrompt.setOnClickListener(v -> onAddNutriScorePromptClick());
        binding.productQuestionDismiss.setOnClickListener(v -> productQuestionDismiss());
        binding.productQuestionLayout.setOnClickListener(v -> onProductQuestionClick());

        productState = FragmentUtils.requireStateFromArguments(this);
        refreshView(productState);
    }

    @Override
    public void refreshView(ProductState productState) {
        // No state -> we can't display anything.
        if (productState == null) {
            return;
        }

        super.refreshView(productState);
        this.productState = productState;
        product = productState.getProduct();
        presenter = new SummaryProductPresenter(product, this);
        binding.categoriesText.setText(Utils.bold(getString(R.string.txtCategories)));
        binding.labelsText.setText(Utils.bold(getString(R.string.txtLabels)));

        //refresh visibility of UI components
        binding.textBrandProduct.setVisibility(VISIBLE);
        binding.textQuantityProduct.setVisibility(VISIBLE);
        binding.textNameProduct.setVisibility(VISIBLE);

        binding.embText.setVisibility(VISIBLE);
        binding.embIcon.setVisibility(VISIBLE);

        binding.labelsText.setVisibility(VISIBLE);
        binding.labelsIcon.setVisibility(VISIBLE);

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        if (Utils.isDisableImageLoad(requireActivity()) && Utils.isBatteryLevelLow(requireContext())) {
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

        mTagDao = Utils.getDaoSession().getTagDao();
        barcode = product.getCode();
        String langCode = LocaleHelper.getLanguage(getContext());

        final String imageUrl = product.getImageUrl(langCode);
        if (StringUtils.isNotBlank(imageUrl)) {
            binding.addPhotoLabel.setVisibility(GONE);

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Picasso.get()
                    .load(imageUrl)
                    .into(binding.imageViewFront);
            } else {
                binding.imageViewFront.setVisibility(GONE);
            }

            mUrlImage = imageUrl;
        }

        //TODO use OpenFoodApiService to fetch product by packaging, brands, categories etc

        if (product.getProductName(langCode) != null) {
            binding.textNameProduct.setText(product.getProductName(langCode));
        } else {
            binding.textNameProduct.setVisibility(GONE);
        }

        if (StringUtils.isNotBlank(product.getQuantity())) {
            binding.textQuantityProduct.setText(product.getQuantity());
        } else {
            binding.textQuantityProduct.setVisibility(GONE);
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
            binding.textBrandProduct.setVisibility(GONE);
        }

        if (product.getEmbTags() != null && !product.getEmbTags().toString().trim().equals("[]")) {
            binding.embText.setMovementMethod(LinkMovementMethod.getInstance());
            binding.embText.setText(Utils.bold(getString(R.string.txtEMB)));
            binding.embText.append(" ");

            String[] embTags = product.getEmbTags().toString().replace("[", "").replace("]", "").split(", ");
            for (int i = 0; i < embTags.length; i++) {
                if (i > 0) {
                    binding.embText.append(", ");
                }
                String embTag = embTags[i];
                binding.embText.append(Utils.getClickableText(getEmbCode(embTag).trim(), getEmbUrl(embTag), SearchType.EMB, getActivity(), customTabsIntent));
            }
        } else {
            binding.embText.setVisibility(GONE);
            binding.embIcon.setVisibility(GONE);
        }

        // if the device does not have a camera, hide the button
        try {
            if (!Utils.isHardwareCameraInstalled(requireActivity())) {
                binding.buttonMorePictures.setVisibility(GONE);
            }
        } catch (NullPointerException e) {
            Log.d(getClass().getSimpleName(), e.toString());
        }

        if (AppFlavors.isFlavors(OFF)) {
            binding.scoresLayout.setVisibility(VISIBLE);
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
                // prefetch the URL
                nutritionScoreUri = Uri.parse(getString(R.string.nutriscore_uri));
                customTabActivityHelper.mayLaunchUrl(nutritionScoreUri, null, null);
                Context context = this.getContext();

                if (nutriments != null) {
                    binding.cvNutritionLights.setVisibility(VISIBLE);
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
                binding.cvNutritionLights.setVisibility(GONE);
            }

            binding.listNutrientLevels.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.listNutrientLevels.setAdapter(new NutrientLevelListAdapter(getContext(), levelItem));

            refreshNutriScore();
            refreshNovaIcon();
            refreshCo2Icon();
            refreshScoresLayout();
        } else {
            binding.scoresLayout.setVisibility(GONE);
        }

        //to be sure that top of the product view is visible at start
        binding.textNameProduct.requestFocus();
        binding.textNameProduct.clearFocus();
    }

    private void refreshScoresLayout() {
        if (binding.novaGroup.getVisibility() == GONE &&
            binding.co2Icon.getVisibility() == GONE &&
            binding.imageGrade.getVisibility() == GONE &&
            binding.addNutriscorePrompt.getVisibility() == GONE) {
            binding.scoresLayout.setVisibility(GONE);
        } else {
            binding.scoresLayout.setVisibility(VISIBLE);
        }
    }

    private void refreshNutriScore() {
        final Drawable nutritionGradeResource = Utils.getImageGradeDrawable(requireContext(), product);
        if (nutritionGradeResource != null) {
            binding.imageGrade.setVisibility(VISIBLE);
            binding.imageGrade.setImageDrawable(nutritionGradeResource);
            binding.imageGrade.setOnClickListener(view1 -> {
                CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
                CustomTabActivityHelper.openCustomTab(SummaryProductFragment.this.requireActivity(), customTabsIntent, nutritionScoreUri, new WebViewFallback());
            });
        } else {
            binding.imageGrade.setVisibility(GONE);
        }
    }

    private void refreshNovaIcon() {
        if (product.getNovaGroups() != null) {
            binding.novaGroup.setVisibility(VISIBLE);
            binding.novaGroup.setImageResource(Utils.getNovaGroupDrawable(product.getNovaGroups()));
            binding.novaGroup.setOnClickListener(view1 -> {
                Uri uri = Uri.parse(getString(R.string.url_nova_groups));
                CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper.getSession());
                CustomTabActivityHelper.openCustomTab(SummaryProductFragment.this.requireActivity(), customTabsIntent, uri, new WebViewFallback());
            });
        } else {
            binding.novaGroup.setVisibility(GONE);
            binding.novaGroup.setImageResource(0);
        }
    }

    private void refreshCo2Icon() {
        int environmentImpactResource = Utils.getImageEnvironmentImpact(product);
        if (environmentImpactResource != Utils.NO_DRAWABLE_RESOURCE) {
            binding.co2Icon.setVisibility(VISIBLE);
            binding.co2Icon.setImageResource(environmentImpactResource);
        } else {
            binding.co2Icon.setVisibility(GONE);
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
            binding.addNutriscorePrompt.setVisibility(VISIBLE);
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
            binding.addNutriscorePrompt.setVisibility(GONE);
        }
    }

    @Override
    public void showAdditives(List<AdditiveName> additives) {
        AdditiveFragmentHelper.showAdditives(additives, binding.textAdditiveProduct, apiClientForWikiData, this);
    }

    @Override
    public void showAdditivesState(ProductInfoState state) {
        requireActivity().runOnUiThread(() -> {
            switch (state) {
                case LOADING:
                    binding.textAdditiveProduct.append(getString(R.string.txtLoading));
                    binding.textAdditiveProduct.setVisibility(VISIBLE);
                    break;
                case EMPTY:
                    binding.textAdditiveProduct.setVisibility(GONE);
                    break;
            }
        });
    }

    @Override
    public void showAnalysisTags(List<AnalysisTagConfig> analysisTags) {
        requireActivity().runOnUiThread(() -> {
            binding.analysisContainer.setVisibility(VISIBLE);
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
            binding.productAllergenAlertLayout.setVisibility(VISIBLE);
            return;
        }

        String text = String.format("%s\n", getResources().getString(R.string.product_allergen_prompt)) +
            StringUtils.join(data.getAllergens(), ", ");
        binding.productAllergenAlertText.setText(text);
        binding.productAllergenAlertLayout.setVisibility(VISIBLE);
    }

    @Override
    public void showCategories(List<CategoryName> categories) {
        if (categories.isEmpty()) {
            binding.categoriesLayout.setVisibility(GONE);
        }
        CategoryProductHelper categoryProductHelper = new CategoryProductHelper(binding.categoriesText, categories, this, apiClientForWikiData);
        categoryProductHelper.showCategories();

        if (categoryProductHelper.getContainsAlcohol()) {
            categoryProductHelper.showAlcoholAlert(binding.textCategoryAlcoholAlert);
        }
    }

    @Override
    public void showProductQuestion(Question question) {
        if (question != null && !question.isEmpty()) {
            productQuestion = question;
            binding.productQuestionText.setText(String.format("%s%n%s",
                question.getQuestion(), question.getValue()));
            binding.productQuestionLayout.setVisibility(VISIBLE);
            hasCategoryInsightQuestion = question.getInsightType().equals("category");
        } else {
            binding.productQuestionLayout.setVisibility(GONE);
            productQuestion = null;
        }
        if (AppFlavors.isFlavors(OFF)) {
            refreshNutriscorePrompt();
            refreshScoresLayout();
        }
    }

    private void onProductQuestionClick() {
        if (productQuestion == null) {
            return;
        }
        new QuestionDialog(requireActivity())
            .setBackgroundColor(R.color.colorPrimaryDark)
            .setQuestion(productQuestion.getQuestion())
            .setValue(productQuestion.getValue())
            .setOnReviewClickListener(new QuestionActionListeners() {
                @Override
                public void onPositiveFeedback(QuestionDialog dialog) {
                    //init POST request
                    sendProductInsights(productQuestion.getInsightId(), AnnotationAnswer.POSITIVE);
                    dialog.dismiss();
                }

                @Override
                public void onNegativeFeedback(QuestionDialog dialog) {
                    sendProductInsights(productQuestion.getInsightId(), AnnotationAnswer.NEGATIVE);
                    dialog.dismiss();
                }

                @Override
                public void onAmbiguityFeedback(QuestionDialog dialog) {
                    sendProductInsights(productQuestion.getInsightId(), AnnotationAnswer.AMBIGUITY);
                    dialog.dismiss();
                }

                @Override
                public void onCancelListener(DialogInterface dialog) {
                    dialog.dismiss();
                }
            })
            .show();
    }

    public void sendProductInsights(String insightId, AnnotationAnswer annotation) {
        if (!Utils.isUserLoggedIn(requireActivity())) {
            new MaterialDialog.Builder(requireActivity())
                .title(getString(R.string.sign_in_to_answer))
                .positiveText(getString(R.string.sign_in_or_register))
                .onPositive((dialog, which) ->
                    registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                dialog.dismiss();
                                processInsight(insightId, annotation);
                            }
                        }).launch(new Intent(getActivity(), LoginActivity.class)))
                .neutralText(R.string.dialog_cancel)
                .onNeutral((dialog, which) -> dialog.dismiss())
                .show();
        } else {
            processInsight(insightId, annotation);
        }
    }

    private void processInsight(String insightId, AnnotationAnswer annotation) {
        presenter.annotateInsight(insightId, annotation);
        Log.d("SummaryProductFragment", String.format("Annotation %s received for insight %s", annotation, insightId));
        binding.productQuestionLayout.setVisibility(GONE);
        productQuestion = null;
    }

    public void showAnnotatedInsightToast(@NonNull AnnotationResponse response) {
        if (response.getStatus().equals("updated") && getActivity() != null) {
            Toast toast = Toast.makeText(getActivity(), R.string.product_question_submit_message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 500);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void productQuestionDismiss() {
        binding.productQuestionLayout.setVisibility(GONE);
    }

    @Override
    public void showLabels(List<LabelName> labels) {
        binding.labelsText.setText(Utils.bold(getString(R.string.txtLabels)));
        binding.labelsText.setClickable(true);
        binding.labelsText.setMovementMethod(LinkMovementMethod.getInstance());
        binding.labelsText.append(" ");

        for (int i = 0; i < labels.size() - 1; i++) {
            binding.labelsText.append(getLabelTag(labels.get(i)));
            binding.labelsText.append(", ");
        }

        binding.labelsText.append(getLabelTag(labels.get(labels.size() - 1)));
    }

    @Override
    public void showCategoriesState(ProductInfoState state) {
        requireActivity().runOnUiThread(() -> {
            switch (state) {
                case LOADING:
                    if (getContext() != null) {
                        binding.categoriesText.append(getString(R.string.txtLoading));
                    }
                    break;
                case EMPTY:
                    binding.categoriesText.setVisibility(GONE);
                    binding.categoriesIcon.setVisibility(GONE);
                    break;
            }
        });
    }

    @Override
    public void showLabelsState(ProductInfoState state) {
        requireActivity().runOnUiThread(() -> {
            switch (state) {
                case LOADING:
                    binding.labelsText.append(getString(R.string.txtLoading));
                    break;
                case EMPTY:
                    binding.labelsText.setVisibility(GONE);
                    binding.labelsIcon.setVisibility(GONE);
                    break;
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
            public void onClick(@NonNull View view) {

                if (label.getIsWikiDataIdPresent()) {
                    apiClientForWikiData.doSomeThing(label.getWikiDataId(), result -> {
                        if (result != null) {
                            FragmentActivity activity = getActivity();
                            if (activity != null && !activity.isFinishing()) {
                                BottomScreenCommon.showBottomSheet(result, label,
                                    activity.getSupportFragmentManager());
                            }
                        } else {
                            ProductBrowsingListActivity.start(getContext(),
                                label.getLabelTag(),
                                label.getName(),
                                SearchType.LABEL);
                        }
                    });
                } else {
                    ProductBrowsingListActivity.start(getContext(),
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
        if (AppFlavors.isFlavors(OFF)) {
            if (isUserNotLoggedIn()) {
                Utils.startLoginToEditAnd(EDIT_PRODUCT_NUTRITION_AFTER_LOGIN, requireActivity());
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
        sharingIntent.setType(OpenFoodAPIClient.MIME_TEXT);
        String shareBody = getResources().getString(R.string.msg_share) + shareUrl;
        String shareSub = "\n\n";
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    private void onEditProductButtonClick() {
        if (isUserNotLoggedIn()) {
            Utils.startLoginToEditAnd(EDIT_PRODUCT_AFTER_LOGIN, requireActivity());
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
        final Activity activity = requireActivity();
        final List<ProductLists> productLists = ProductListsActivity.getProductListsDaoWithDefaultList(activity).loadAll();

        final String productBarcode = product.getCode();
        final String productName = product.getProductName();
        final String imageUrl = product.getImageSmallUrl(LocaleHelper.getLanguage(activity));
        final String productDetails = YourListedProductsActivity.getProductBrandsQuantityDetails(product);

        final MaterialDialog addToListDialog = new MaterialDialog.Builder(activity)
            .title(R.string.add_to_product_lists)
            .customView(R.layout.dialog_add_to_list, true)
            .build();
        addToListDialog.show();

        final View dialogView = addToListDialog.getCustomView();
        if (dialogView == null) {
            return;
        }

        // Set recycler view
        final RecyclerView addToListRecyclerView = dialogView.findViewById(R.id.rv_dialogAddToList);
        DialogAddToListAdapter addToListAdapter = new DialogAddToListAdapter(activity, productLists, productBarcode, productName, productDetails, imageUrl);
        addToListRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        addToListRecyclerView.setAdapter(addToListAdapter);

        // Add listener to text view
        final TextView tvAddToList = dialogView.findViewById(R.id.tvAddToNewList);
        tvAddToList.setOnClickListener(view -> {
            Intent intent = new Intent(activity, ProductListsActivity.class);
            intent.putExtra("product", product);
            activity.startActivity(intent);
        });
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

    private void openFrontImageFullscreen() {
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
        disp.add(api.postImg(image).subscribeWith(new DisposableCompletableObserver() {
            @Override
            public void onComplete() {
                onImageListenerComplete();
            }

            @Override
            public void onError(Throwable error) {
                onImageListenerError(error);
            }
        }));
        binding.addPhotoLabel.setVisibility(GONE);
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
        boolean shouldRefresh = (requestCode == EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK)
            || ProductImageManagementActivity.isImageModified(requestCode, resultCode);

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
        binding.uploadingImageProgress.setVisibility(VISIBLE);
        binding.uploadingImageProgressText.setVisibility(VISIBLE);
        binding.uploadingImageProgressText.setText(R.string.toastSending);
    }

    public void resetScroll() {
        binding.scrollView.scrollTo(0, 0);

        if (binding.analysisTags.getAdapter() != null) {
            ((IngredientAnalysisTagsAdapter) binding.analysisTags.getAdapter()).filterVisibleTags();
        }
    }
}
