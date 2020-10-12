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

package openfoodfacts.github.scrachx.openfood.views.product.ingredients;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import openfoodfacts.github.scrachx.openfood.AppFlavors;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentIngredientsProductBinding;
import openfoodfacts.github.scrachx.openfood.fragments.AdditiveFragmentHelper;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.images.PhotoReceiver;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.AllergenNameDao;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.WikiDataApiClient;
import openfoodfacts.github.scrachx.openfood.utils.BottomScreenCommon;
import openfoodfacts.github.scrachx.openfood.utils.FileUtils;
import openfoodfacts.github.scrachx.openfood.utils.FragmentUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.FullScreenActivityOpener;
import openfoodfacts.github.scrachx.openfood.views.LoginActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductImageManagementActivity;

import static android.app.Activity.RESULT_OK;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.INGREDIENTS;
import static openfoodfacts.github.scrachx.openfood.utils.ProductInfoState.EMPTY;
import static openfoodfacts.github.scrachx.openfood.utils.ProductInfoState.LOADING;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class IngredientsProductFragment extends BaseFragment implements IIngredientsProductPresenter.View, PhotoReceiver {
    public static final Pattern INGREDIENT_PATTERN = Pattern.compile("[\\p{L}\\p{Nd}(),.-]+");
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;
    private static final int EDIT_REQUEST_CODE = 2;
    private FragmentIngredientsProductBinding binding;
    private AllergenNameDao mAllergenNameDao;
    private OpenFoodAPIClient client;
    private String mUrlImage;
    private State activityState;
    private String barcode;
    private SendProduct mSendProduct;
    private WikiDataApiClient wikidataClient;
    private CustomTabActivityHelper customTabActivityHelper;
    private CustomTabsIntent customTabsIntent;
    private IIngredientsProductPresenter.Actions presenter;
    private boolean extractIngredients = false;
    private boolean sendUpdatedIngredientsImage = false;
    /**
     * boolean to determine if image should be loaded or not
     **/
    private boolean isLowBatteryMode = false;
    private PhotoReceiverHandler photoReceiverHandler;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());

        activityState = FragmentUtils.requireStateFromArguments(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        client = new OpenFoodAPIClient(requireContext());
        wikidataClient = new WikiDataApiClient();
        binding = FragmentIngredientsProductBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activityState = FragmentUtils.getStateFromArguments(this);
        binding.extractIngredientsPrompt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_box_blue_18dp, 0, 0, 0);
        binding.changeIngImg.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_a_photo_blue_18dp, 0, 0, 0);

        binding.changeIngImg.setOnClickListener(v -> changeIngImage());
        binding.novaMethodLink.setOnClickListener(v -> novaMethodLinkDisplay());
        binding.extractIngredientsPrompt.setOnClickListener(v -> extractIngredients());
        binding.imageViewIngredients.setOnClickListener(v -> openFullScreen());

        photoReceiverHandler = new PhotoReceiverHandler(this);
        refreshView(activityState);
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);
        activityState = state;
        String langCode = LocaleHelper.getLanguage(getContext());
        if (getArguments() != null) {
            mSendProduct = (SendProduct) getArguments().getSerializable("sendProduct");
        }

        mAllergenNameDao = Utils.getDaoSession().getAllergenNameDao();

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        if (Utils.isDisableImageLoad(requireContext()) && Utils.isBatteryLevelLow(requireContext())) {
            isLowBatteryMode = true;
        }

        final Product product = activityState.getProduct();
        presenter = new IngredientsProductPresenter(product, this);
        barcode = product.getCode();
        List<String> vitaminTagsList = product.getVitaminTags();
        List<String> aminoAcidTagsList = product.getAminoAcidTags();
        List<String> mineralTags = product.getMineralTags();
        List<String> otherNutritionTags = product.getOtherNutritionTags();

        if (!vitaminTagsList.isEmpty()) {
            binding.cvVitaminsTagsText.setVisibility(View.VISIBLE);
            binding.vitaminsTagsText.setText(bold(getString(R.string.vitamin_tags_text)));
            binding.vitaminsTagsText.append(buildStringBuilder(vitaminTagsList, Utils.SPACE));
        }

        if (!aminoAcidTagsList.isEmpty()) {
            binding.cvAminoAcidTagsText.setVisibility(View.VISIBLE);
            binding.aminoAcidTagsText.setText(bold(getString(R.string.amino_acid_tags_text)));
            binding.aminoAcidTagsText.append(buildStringBuilder(aminoAcidTagsList, Utils.SPACE));
        }

        if (!mineralTags.isEmpty()) {
            binding.cvMineralTagsText.setVisibility(View.VISIBLE);
            binding.mineralTagsText.setText(bold(getString(R.string.mineral_tags_text)));
            binding.mineralTagsText.append(buildStringBuilder(mineralTags, Utils.SPACE));
        }

        if (!otherNutritionTags.isEmpty()) {
            binding.otherNutritionTags.setVisibility(View.VISIBLE);
            binding.otherNutritionTags.setText(bold(getString(R.string.other_tags_text)));
            binding.otherNutritionTags.append(buildStringBuilder(otherNutritionTags, Utils.SPACE));
        }

        binding.textAdditiveProduct.setText(bold(getString(R.string.txtAdditives)));
        presenter.loadAdditives();

        if (isNotBlank(product.getImageIngredientsUrl(langCode))) {
            binding.addPhotoLabel.setVisibility(View.GONE);
            binding.changeIngImg.setVisibility(View.VISIBLE);

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Picasso.get()
                    .load(product.getImageIngredientsUrl(langCode))
                    .into(binding.imageViewIngredients);
            } else {
                binding.imageViewIngredients.setVisibility(View.GONE);
            }
            mUrlImage = product.getImageIngredientsUrl(langCode);
        }

        //useful when this fragment is used in offline saving
        if (mSendProduct != null && isNotBlank(mSendProduct.getImgupload_ingredients())) {
            binding.addPhotoLabel.setVisibility(View.GONE);
            mUrlImage = mSendProduct.getImgupload_ingredients();
            Picasso.get().load(FileUtils.LOCALE_FILE_SCHEME + mUrlImage).config(Bitmap.Config.RGB_565).into(binding.imageViewIngredients);
        }

        List<String> allergens = getAllergens();

        if (activityState != null && StringUtils.isNotEmpty(product.getIngredientsText(langCode))) {
            binding.cvTextIngredientProduct.setVisibility(View.VISIBLE);
            SpannableStringBuilder txtIngredients = new SpannableStringBuilder(product.getIngredientsText(langCode).replace("_", ""));
            txtIngredients = setSpanBoldBetweenTokens(txtIngredients, allergens);
            if (TextUtils.isEmpty(product.getIngredientsText(langCode))) {
                binding.extractIngredientsPrompt.setVisibility(View.VISIBLE);
            }
            int ingredientsListAt = Math.max(0, txtIngredients.toString().indexOf(":"));
            if (!txtIngredients.toString().substring(ingredientsListAt).trim().isEmpty()) {
                binding.textIngredientProduct.setText(txtIngredients);
            }
        } else {
            binding.cvTextIngredientProduct.setVisibility(View.GONE);
            if (isNotBlank(product.getImageIngredientsUrl(langCode))) {
                binding.extractIngredientsPrompt.setVisibility(View.VISIBLE);
            }
        }
        presenter.loadAllergens();

        if (!StringUtils.isBlank(product.getTraces())) {
            String language = LocaleHelper.getLanguage(getContext());
            binding.cvTextTraceProduct.setVisibility(View.VISIBLE);
            binding.textTraceProduct.setMovementMethod(LinkMovementMethod.getInstance());
            binding.textTraceProduct.setText(bold(getString(R.string.txtTraces)));
            binding.textTraceProduct.append(" ");

            String[] traces = product.getTraces().split(",");
            for (int i = 0; i < traces.length; i++) {
                String trace = traces[i];
                if (i > 0) {
                    binding.textTraceProduct.append(", ");
                }
                binding.textTraceProduct.append(Utils.getClickableText(getTracesName(language, trace), trace, SearchType.TRACE, getActivity(), customTabsIntent));
            }
        } else {
            binding.cvTextTraceProduct.setVisibility(View.GONE);
        }

        if (product.getNovaGroups() != null) {
            binding.novaLayout.setVisibility(View.VISIBLE);
            binding.novaExplanation.setText(Utils.getNovaGroupExplanation(product.getNovaGroups(), requireContext()));
            binding.novaGroup.setImageResource(Utils.getNovaGroupDrawable(product));
            binding.novaGroup.setOnClickListener((View v) -> {
                Uri uri = Uri.parse(getString(R.string.url_nova_groups));
                CustomTabsIntent tabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
                CustomTabActivityHelper.openCustomTab(IngredientsProductFragment.this.requireActivity(), tabsIntent, uri, new WebViewFallback());
            });
        } else {
            binding.novaLayout.setVisibility(View.GONE);
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

    private CharSequence getAllergensTag(AllergenName allergen) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                if (allergen.getIsWikiDataIdPresent()) {
                    wikidataClient.doSomeThing(
                        allergen.getWikiDataId(),
                        result -> {
                            if (result != null) {
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
        AdditiveFragmentHelper.showAdditives(additives, binding.textAdditiveProduct, wikidataClient, this);
    }

    @Override
    public void showAdditivesState(String state) {
        switch (state) {
            case LOADING:
                binding.cvTextAdditiveProduct.setVisibility(View.VISIBLE);
                binding.textAdditiveProduct.append(getString(R.string.txtLoading));
                break;

            case EMPTY:
                binding.cvTextAdditiveProduct.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void showAllergens(List<AllergenName> allergens) {
        binding.textSubstanceProduct.setMovementMethod(LinkMovementMethod.getInstance());
        binding.textSubstanceProduct.setText(bold(getString(R.string.txtSubstances)));
        binding.textSubstanceProduct.append(" ");

        for (int i = 0, lastIdx = allergens.size() - 1; i <= lastIdx; i++) {
            AllergenName allergen = allergens.get(i);
            binding.textSubstanceProduct.append(getAllergensTag(allergen));
            // Add comma if not the last item
            if (i != lastIdx) {
                binding.textSubstanceProduct.append(", ");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    public void changeIngImage() {
        sendUpdatedIngredientsImage = true;

        if (getActivity() == null) {
            return;
        }
        final ViewPager2 viewPager = getActivity().findViewById(R.id.pager);
        if (Utils.isFlavor(AppFlavors.OFF)) {
            final SharedPreferences settings = getActivity().getSharedPreferences("login", 0);
            final String login = settings.getString("user", "");
            if (login.isEmpty()) {
                showSignInDialog();
            } else {
                activityState = FragmentUtils.getStateFromArguments(this);
                if (activityState != null) {
                    Intent intent = new Intent(getContext(), AddProductActivity.class);
                    intent.putExtra("send_updated", sendUpdatedIngredientsImage);
                    intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, activityState.getProduct());
                    startActivityForResult(intent, EDIT_REQUEST_CODE);
                }
            }
        }
        if (Utils.isFlavor(AppFlavors.OPFF)) {
            viewPager.setCurrentItem(4);
        }

        if (Utils.isFlavor(AppFlavors.OBF)) {
            viewPager.setCurrentItem(1);
        }

        if (Utils.isFlavor(AppFlavors.OPF)) {
            viewPager.setCurrentItem(0);
        }
    }

    @Override
    public void showAllergensState(String state) {
        switch (state) {
            case LOADING:
                binding.textSubstanceProduct.setVisibility(View.VISIBLE);
                binding.textSubstanceProduct.append(getString(R.string.txtLoading));
                break;

            case EMPTY:
                binding.textSubstanceProduct.setVisibility(View.GONE);
                break;
        }
    }

    private List<String> getAllergens() {
        List<String> allergens = activityState.getProduct().getAllergensTags();
        if (activityState.getProduct() == null || allergens == null || allergens.isEmpty()) {
            return Collections.emptyList();
        } else {
            return allergens;
        }
    }

    void novaMethodLinkDisplay() {
        if (activityState != null && activityState.getProduct() != null && activityState.getProduct().getNovaGroups() != null) {
            Uri uri = Uri.parse(getString(R.string.url_nova_groups));
            CustomTabsIntent tabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
            CustomTabActivityHelper.openCustomTab(requireActivity(), tabsIntent, uri, new WebViewFallback());
        }
    }

    public void extractIngredients() {
        extractIngredients = true;
        final SharedPreferences settings = requireActivity().getSharedPreferences("login", 0);
        final String login = settings.getString("user", "");
        if (login.isEmpty()) {
            showSignInDialog();
        } else {
            activityState = FragmentUtils.requireStateFromArguments(this);
            Intent intent = new Intent(getContext(), AddProductActivity.class);
            intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, activityState.getProduct());
            intent.putExtra("perform_ocr", extractIngredients);
            startActivityForResult(intent, EDIT_REQUEST_CODE);
        }
    }

    private void showSignInDialog() {
        new MaterialDialog.Builder(requireContext())
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
    }

    private void openFullScreen() {
        if (mUrlImage != null && activityState != null && activityState.getProduct() != null) {
            FullScreenActivityOpener.openForUrl(this, activityState.getProduct(), INGREDIENTS, mUrlImage, binding.imageViewIngredients);
        } else {
            newIngredientImage();
        }
    }

    public void newIngredientImage() {
        doChooseOrTakePhotos(getString(R.string.ingredients_picture));
    }

    @Override
    protected void doOnPhotosPermissionGranted() {
        newIngredientImage();
    }

    public void onPhotoReturned(File newPhotoFile) {
        ProductImage image = new ProductImage(barcode, INGREDIENTS, newPhotoFile);
        image.setFilePath(newPhotoFile.getAbsolutePath());
        client.postImg(image, null);
        binding.addPhotoLabel.setVisibility(View.GONE);
        mUrlImage = newPhotoFile.getAbsolutePath();

        Picasso.get()
            .load(newPhotoFile)
            .fit()
            .into(binding.imageViewIngredients);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //added case for sending updated ingredients image
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(getContext(), AddProductActivity.class);
            intent.putExtra("send_updated", sendUpdatedIngredientsImage);
            intent.putExtra("perform_ocr", extractIngredients);
            intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, activityState.getProduct());
            startActivity(intent);
        }
        if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            onRefresh();
        }
        if (ProductImageManagementActivity.isImageModified(requestCode, resultCode)) {
            onRefresh();
        }

        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data);
    }

    public String getIngredients() {
        return mUrlImage;
    }

    @Override
    public void onDestroyView() {
        if (presenter != null) {
            presenter.dispose();
        }
        super.onDestroyView();
    }
}
