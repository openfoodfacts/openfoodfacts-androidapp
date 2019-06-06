package openfoodfacts.github.scrachx.openfood.views.product.nutrition;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.BindView;
import butterknife.OnClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.jobs.PhotoReceiver;
import openfoodfacts.github.scrachx.openfood.jobs.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.models.*;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.ProductUtils;
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrientLevelListAdapter;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrimentsRecyclerViewAdapter;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.views.product.CalculateDetails;
import pl.aprilapps.easyphotopicker.EasyImage;

import java.io.File;
import java.util.*;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.*;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.NUTRITION;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class NutritionProductFragment extends BaseFragment implements CustomTabActivityHelper.ConnectionCallback, PhotoReceiver {
    private static final int EDIT_PRODUCT_AFTER_LOGIN_REQUEST_CODE = 1;
    @BindView(R.id.imageGrade)
    ImageView img;
    @BindView(R.id.imageGradeLayout)
    LinearLayout imageGradeLayout;
    @BindView(R.id.nutriscoreLink)
    TextView nutriscoreLink;
    private PhotoReceiverHandler photoReceiverHandler;
    @BindView(R.id.listNutrientLevels)
    RecyclerView rv;
    @BindView(R.id.textServingSize)
    TextView serving;
    @BindView(R.id.serving_size_card_view)
    CardView servingSizeCardView;
    @BindView(R.id.textCarbonFootprint)
    TextView carbonFootprint;
    @BindView(R.id.textNutrientTxt)
    TextView textNutrientTxt;
    @BindView(R.id.get_nutriscore_prompt)
    Button nutriscorePrompt;
    @BindView(R.id.textPerPortion)
    TextView mTextPerPortion;
    @BindView(R.id.imageViewNutrition)
    ImageView mImageNutrition;
    @BindView(R.id.addPhotoLabel)
    TextView addPhotoLabel;
    @BindView(R.id.nutriments_recycler_view)
    RecyclerView nutrimentsRecyclerView;
    @BindView(R.id.textNutriScoreInfo)
    TextView textNutriScoreInfo;
    @BindView(R.id.nutrient_levels_card_view)
    CardView nutrientLevelsCardView;
    @BindView(R.id.newAdd)
    Button img1;
    @BindView(R.id.calculateNutritionFacts)
    Button calculateNutritionFacts;
    @BindView(R.id.nutrimentsCardView)
    CardView nutrimentsCardView;
    @BindView(R.id.textNoNutritionData)
    TextView textNoNutritionData;
    private String mUrlImage;
    private String barcode;
    private OpenFoodAPIClient api;
    //boolean to determine if image should be loaded or not
    private boolean isLowBatteryMode = false;
    private SendProduct mSendProduct;
    private CustomTabActivityHelper customTabActivityHelper;
    private Uri nutritionScoreUri;
    //the following booleans indicate whether the prompts are to be made visible
    private boolean showNutritionPrompt = false;
    private boolean showCategoryPrompt = false;
    //boolean to determine if nutrition data should be shown
    private boolean showNutritionData = true;
    private Product product;
    private State activityState;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        api = new OpenFoodAPIClient(getActivity());
        return createView(inflater, container, R.layout.fragment_nutrition_product);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photoReceiverHandler=new PhotoReceiverHandler(this);
        // use VERTICAL divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(nutrimentsRecyclerView.getContext(), VERTICAL);
        nutrimentsRecyclerView.addItemDecoration(dividerItemDecoration);
        nutriscorePrompt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_box_blue_18dp, 0, 0, 0);
        img1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_a_photo_black_18dp, 0, 0, 0);
        refreshView(getStateFromActivityIntent());
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);
        activityState = state;
        product = state.getProduct();
        String langCode = LocaleHelper.getLanguage(getContext());
        //checks the product states_tags to determine which prompt to be shown
        List<String> statesTags = product.getStatesTags();
        if (statesTags.contains("en:categories-to-be-completed")) {
            showCategoryPrompt = true;
        }
        if (product.getNoNutritionData() != null && product.getNoNutritionData().equals("on")) {
            showNutritionPrompt = false;
            showNutritionData = false;
        } else {
            if (statesTags.contains("en:nutrition-facts-to-be-completed")) {
                showNutritionPrompt = true;
            }
        }

        if (showNutritionPrompt || showCategoryPrompt) {
            nutriscorePrompt.setVisibility(View.VISIBLE);
            if (showNutritionPrompt && showCategoryPrompt) {
                nutriscorePrompt.setText(getString(R.string.add_nutrient_category_prompt_text));
            } else if (showNutritionPrompt) {
                nutriscorePrompt.setText(getString(R.string.add_nutrient_prompt_text));
            } else if (showCategoryPrompt) {
                nutriscorePrompt.setText(getString(R.string.add_category_prompt_text));
            }
        }

        if (!showNutritionData) {
            mImageNutrition.setVisibility(View.GONE);
            addPhotoLabel.setVisibility(View.GONE);
            imageGradeLayout.setVisibility(View.GONE);
            calculateNutritionFacts.setVisibility(View.GONE);
            nutrimentsCardView.setVisibility(View.GONE);
            textNoNutritionData.setVisibility(View.VISIBLE);
        }

        List<NutrientLevelItem> levelItem = new ArrayList<>();

        SharedPreferences settingsPreference = getActivity().getSharedPreferences("prefs", 0);

        Nutriments nutriments = product.getNutriments();

        if (nutriments != null && !nutriments.contains(Nutriments.CARBON_FOOTPRINT)) {
            carbonFootprint.setVisibility(View.GONE);
        }

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
            nutrientLevelsCardView.setVisibility(View.GONE);
            levelItem.add(new NutrientLevelItem("", "", "", 0));
            img.setVisibility(View.GONE);
        } else {
            // prefetch the uri
            customTabActivityHelper = new CustomTabActivityHelper();
            customTabActivityHelper.setConnectionCallback(this);
            // currently only available in french translations
            nutritionScoreUri = Uri.parse("https://fr.openfoodfacts.org/score-nutritionnel-france");
            customTabActivityHelper.mayLaunchUrl(nutritionScoreUri, null, null);

            Context context = this.getContext();
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
                levelItem.add(new NutrientLevelItem(getString(R.string.txtSugars), sugarsNutriment.getDisplayStringFor100g(),
                    sugarsLocalize,
                    sugars.getImageLevel()));
            }

            Nutriments.Nutriment saltNutriment = nutriments.get(Nutriments.SALT);
            if (salt != null && saltNutriment != null) {
                String saltLocalize = salt.getLocalize(context);
                levelItem.add(new NutrientLevelItem(getString(R.string.txtSalt), saltNutriment.getDisplayStringFor100g(),
                    saltLocalize,
                    salt.getImageLevel()));
            }

            int nutritionGrade = Utils.getImageGrade(product);
            if (nutritionGrade != Utils.NO_DRAWABLE_RESOURCE) {
                imageGradeLayout.setVisibility(View.VISIBLE);
                img.setImageResource(nutritionGrade);
                img.setOnClickListener(view1 -> {
                    CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
                    CustomTabActivityHelper.openCustomTab(NutritionProductFragment.this.getActivity(), customTabsIntent, nutritionScoreUri, new WebViewFallback());
                });
            } else {
                imageGradeLayout.setVisibility(View.GONE);
            }
        }

        //checks the flags and accordingly sets the text of the prompt
        if (showNutritionPrompt || showCategoryPrompt) {
            nutriscorePrompt.setVisibility(View.VISIBLE);
            if (showNutritionPrompt && showCategoryPrompt) {
                nutriscorePrompt.setText(getString(R.string.add_nutrient_category_prompt_text));
            } else if (showNutritionPrompt) {
                nutriscorePrompt.setText(getString(R.string.add_nutrient_prompt_text));
            } else if (showCategoryPrompt) {
                nutriscorePrompt.setText(getString(R.string.add_category_prompt_text));
            }
        }

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new NutrientLevelListAdapter(getContext(), levelItem));

        textNutriScoreInfo.setClickable(true);
        textNutriScoreInfo.setMovementMethod(LinkMovementMethod.getInstance());
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
                customTabsIntent.intent.putExtra("android.intent.extra.REFERRER", Uri.parse("android-app://" + getActivity().getPackageName()));
                CustomTabActivityHelper.openCustomTab(getActivity(), customTabsIntent, Uri.parse(getString(R.string.url_nutrient_values)), new WebViewFallback());
            }
        };
        spannableStringBuilder.append(getString(R.string.txtNutriScoreInfo));
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textNutriScoreInfo.setText(spannableStringBuilder);

        if (TextUtils.isEmpty(product.getServingSize())) {
            serving.setVisibility(View.GONE);
            servingSizeCardView.setVisibility(View.GONE);
        } else {
            String servingSize = product.getServingSize();
            if (settingsPreference.getString("volumeUnitPreference", "l").equals("oz")) {
                servingSize = Utils.getServingInOz(servingSize);
            } else if (servingSize.toLowerCase().contains("oz") && settingsPreference.getString("volumeUnitPreference", "l").equals("l")) {
                servingSize = Utils.getServingInL(servingSize);
            }

            serving.setText(bold(getString(R.string.txtServingSize)));
            serving.append(" ");
            serving.append(servingSize);
        }

        if (Utils.isDisableImageLoad(getContext()) && Utils.getBatteryLevel(getContext())) {
            isLowBatteryMode = true;
        }

        if (getArguments() != null) {
            mSendProduct = (SendProduct) getArguments().getSerializable("sendProduct");
        }

        barcode = product.getCode();
        List<NutrimentItem> nutrimentItems = new ArrayList<>();

        final boolean inVolume= ProductUtils.isPerServingInLiter(product);
        textNutrientTxt.setText(inVolume ? R.string.txtNutrientLevel100ml : R.string.txtNutrientLevel100g);
        if (isNotBlank(product.getServingSize())) {
            mTextPerPortion.setText(getString(R.string.nutriment_serving_size) + " " + product.getServingSize());
        } else {
            mTextPerPortion.setVisibility(View.GONE);
        }

        if (isNotBlank(product.getImageNutritionUrl(langCode))) {
            addPhotoLabel.setVisibility(View.GONE);
            img1.setVisibility(View.VISIBLE);

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Picasso.with(getContext())
                    .load(product.getImageNutritionUrl(langCode))
                    .into(mImageNutrition);
            } else {

                mImageNutrition.setVisibility(View.GONE);
            }
            Picasso.with(getContext())
                .load(product.getImageNutritionUrl(langCode))
                .into(mImageNutrition);

            mUrlImage = product.getImageNutritionUrl(langCode);
        }

        //useful when this fragment is used in offline saving
        if (mSendProduct != null && isNotBlank(mSendProduct.getImgupload_nutrition())) {
            addPhotoLabel.setVisibility(View.GONE);
            mUrlImage = mSendProduct.getImgupload_nutrition();
            Picasso.with(getContext()).load("file://" + mUrlImage).config(Bitmap.Config.RGB_565).into(mImageNutrition);
        }

        if (nutriments == null) {
            return;
        }

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        nutrimentsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        nutrimentsRecyclerView.setLayoutManager(mLayoutManager);

        nutrimentsRecyclerView.setNestedScrollingEnabled(false);

        // Header hack
        nutrimentItems.add(new NutrimentItem(inVolume));

        // Energy
        Nutriments.Nutriment energy = nutriments.get(ENERGY);
        if (energy != null && UnitUtils.ENERGY_KCAL.equalsIgnoreCase(settingsPreference.getString("energyUnitPreference", UnitUtils.ENERGY_KCAL))) {
            nutrimentItems.add(
                new NutrimentItem(getString(R.string.nutrition_energy_short_name),
                    Utils.getEnergy(energy.getFor100gInUnits()),
                    Utils.getEnergy(energy.getForServingInUnits()),
                    UnitUtils.ENERGY_KCAL,
                    nutriments.getModifier(ENERGY)));
        } else if (energy != null && UnitUtils.ENERGY_KJ.equalsIgnoreCase(settingsPreference.getString("energyUnitPreference", UnitUtils.ENERGY_KCAL))) {
            nutrimentItems.add(
                new NutrimentItem(getString(R.string.nutrition_energy_short_name),
                    energy.getFor100gInUnits(),
                    energy.getForServingInUnits(),
                    UnitUtils.ENERGY_KJ.toLowerCase(),
                    nutriments.getModifier(ENERGY)));
        }

        // Fat
        Nutriments.Nutriment fat2 = nutriments.get(FAT);
        if (fat2 != null) {
            String modifier = nutriments.getModifier(FAT);
            nutrimentItems.add(new HeaderNutrimentItem(getString(R.string.nutrition_fat),
                fat2.getFor100gInUnits(),
                fat2.getForServingInUnits(),
                fat2.getUnit(),
                modifier == null ? "" : modifier));

            nutrimentItems.addAll(getNutrimentItems(nutriments, FAT_MAP));
        }

        // Carbohydrates
        Nutriments.Nutriment carbohydrates = nutriments.get(CARBOHYDRATES);
        if (carbohydrates != null) {
            String modifier = nutriments.getModifier(CARBOHYDRATES);
            nutrimentItems.add(new HeaderNutrimentItem(getString(R.string.nutrition_carbohydrate),
                carbohydrates.getFor100gInUnits(),
                carbohydrates.getForServingInUnits(),
                carbohydrates.getUnit(),
                modifier == null ? "" : modifier));

            nutrimentItems.addAll(getNutrimentItems(nutriments, CARBO_MAP));
        }

        // fiber
        nutrimentItems.addAll(getNutrimentItems(nutriments, Collections.singletonMap(Nutriments.FIBER, R.string.nutrition_fiber)));

        // Proteins
        Nutriments.Nutriment proteins = nutriments.get(PROTEINS);
        if (proteins != null) {
            String modifier = nutriments.getModifier(PROTEINS);
            nutrimentItems.add(new HeaderNutrimentItem(getString(R.string.nutrition_proteins),
                proteins.getFor100gInUnits(),
                proteins.getForServingInUnits(),
                proteins.getUnit(),
                modifier == null ? "" : modifier));

            nutrimentItems.addAll(getNutrimentItems(nutriments, PROT_MAP));
        }

        // salt and alcohol
        Map<String, Integer> map = new HashMap<>();
        map.put(Nutriments.SALT, R.string.nutrition_salt);
        map.put(Nutriments.SODIUM, R.string.nutrition_sodium);
        map.put(Nutriments.ALCOHOL, R.string.nutrition_alcohol);
        nutrimentItems.addAll(getNutrimentItems(nutriments, map));

        // Vitamins
        if (nutriments.hasVitamins()) {
            nutrimentItems.add(new HeaderNutrimentItem(getString(R.string.nutrition_vitamins)));

            nutrimentItems.addAll(getNutrimentItems(nutriments, VITAMINS_MAP));
        }

        // Minerals
        if (nutriments.hasMinerals()) {
            nutrimentItems.add(new HeaderNutrimentItem(getString(R.string.nutrition_minerals)));

            nutrimentItems.addAll(getNutrimentItems(nutriments, MINERALS_MAP));
        }

        RecyclerView.Adapter adapter = new NutrimentsRecyclerViewAdapter(nutrimentItems);
        nutrimentsRecyclerView.setAdapter(adapter);
    }

    private List<NutrimentItem> getNutrimentItems(Nutriments nutriments, Map<String, Integer> nutrimentMap) {
        List<NutrimentItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : nutrimentMap.entrySet()) {
            Nutriments.Nutriment nutriment = nutriments.get(entry.getKey());
            if (nutriment != null) {
                items.add(new NutrimentItem(getString(entry.getValue()),
                    nutriment.getFor100gInUnits(),
                    nutriment.getForServingInUnits(),
                    entry.getValue().equals(R.string.ph) ? "" : nutriment.getUnit(),
                    nutriments.getModifier(entry.getKey())));
            }
        }

        return items;
    }

    @OnClick(R.id.nutriscoreLink)
    void nutriscoreLinkDisplay() {
        if (product.getNutritionGradeFr() != null) {
            CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
            CustomTabActivityHelper.openCustomTab(NutritionProductFragment.this.getActivity(), customTabsIntent, nutritionScoreUri, new WebViewFallback());
        }
    }

    @OnClick(R.id.imageViewNutrition)
    public void openFullScreen(View v) {
        if (mUrlImage != null) {
            Intent intent = new Intent(v.getContext(), FullScreenImage.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageurl", mUrlImage);
            intent.putExtras(bundle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(getActivity(), mImageNutrition,
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

    @OnClick(R.id.calculateNutritionFacts)
    public void calculateNutritionFacts(View v) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
            .title(R.string.calculate_nutrition_facts)
            .customView(R.layout.dialog_calculate_calories, false)
            .dismissListener(dialogInterface -> Utils.hideKeyboard(getActivity()));
        MaterialDialog dialog = builder.build();
        dialog.show();
        View view = dialog.getCustomView();
        if (view != null) {
            EditText etWeight = view.findViewById(R.id.edit_text_weight);
            Spinner spinner = view.findViewById(R.id.spinner_weight);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Button btn = (Button) dialog.findViewById(R.id.txt_calories_result);
                    btn.setOnClickListener(v1 -> {
                        if (!TextUtils.isEmpty(etWeight.getText().toString())) {

                            String spinnerValue = (String) spinner.getSelectedItem();
                            String weight = etWeight.getText().toString();
                            Product p = activityState.getProduct();
                            Intent intent = new Intent(getContext(), CalculateDetails.class);
                            intent.putExtra("sampleObject", p);
                            intent.putExtra("spinnervalue", spinnerValue);
                            intent.putExtra("weight", weight);
                            startActivity(intent);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), getResources().getString(R.string.please_enter_weight), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
    }

    public void onPhotoReturned(File photoFile) {
        ProductImage image = new ProductImage(barcode, NUTRITION, photoFile);
        image.setFilePath(photoFile.getAbsolutePath());
        api.postImg(getContext(), image, null);
        addPhotoLabel.setVisibility(View.GONE);
        mUrlImage = photoFile.getAbsolutePath();

        Picasso.with(getContext())
            .load(photoFile)
            .fit()
            .into(mImageNutrition);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        photoReceiverHandler.onActivityResult(this,requestCode,resultCode,data);
        if (requestCode == EDIT_PRODUCT_AFTER_LOGIN_REQUEST_CODE && resultCode == RESULT_OK && isUserLoggedIn()) {
                startEditProduct();
        }
    }

    @OnClick(R.id.newAdd)
    public void newNutritionImage() {
        doChooseOrTakePhotos(getString(R.string.nutrition_facts_picture));
    }

    @Override
    protected void doOnPhotosPermissionGranted() {
        newNutritionImage();
    }

    public String getNutrients() {
        return mUrlImage;
    }

    @Override
    public void onCustomTabsConnected() {
        img.setClickable(true);
    }

    @Override
    public void onCustomTabsDisconnected() {
        img.setClickable(false);
    }

    @OnClick(R.id.get_nutriscore_prompt)
    public void onNutriscoreButtonClick() {
        if (BuildConfig.FLAVOR.equals("off")) {
            if (isUserNotLoggedIn()) {
                startLoginToEditAnd(EDIT_PRODUCT_AFTER_LOGIN_REQUEST_CODE);
            } else {
                startEditProduct();
            }
        }
    }

    private void startEditProduct() {
        Intent intent = new Intent(getActivity(), AddProductActivity.class);
        intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, product);
        //adds the information about the prompt when navigating the user to the edit the product
        intent.putExtra(AddProductActivity.MODIFY_CATEGORY_PROMPT, showCategoryPrompt);
        intent.putExtra(AddProductActivity.MODIFY_NUTRITION_PROMPT, showNutritionPrompt);
        startActivity(intent);
    }
}
