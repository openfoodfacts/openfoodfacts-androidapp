package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.validator.ChipifyingNachoValidator;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.lang3.StringUtils;
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
import butterknife.OnTextChanged;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.AllergenNameDao;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.hootsuite.nachos.terminator.ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.INGREDIENTS;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;

public class AddProductIngredientsFragment extends BaseFragment {

    private static final String PARAM_INGREDIENTS = "ingredients_text";
    private static final String PARAM_TRACES = "add_traces";
    private static final String PARAM_LANGUAGE = "lang";
    @BindView(R.id.btnAddImageIngredients)
    ImageView imageIngredients;
    @BindView(R.id.imageProgress)
    ProgressBar imageProgress;
    @BindView(R.id.imageProgressText)
    TextView imageProgressText;
    @BindView(R.id.ingredients_list)
    EditText ingredients;
    @BindView(R.id.btn_extract_ingredients)
    Button extractIngredients;
    @BindView(R.id.ocr_progress)
    ProgressBar ocrProgress;
    @BindView(R.id.ocr_progress_text)
    TextView ocrProgressText;
    @BindView(R.id.ingredients_list_verified)
    ImageView ingredientsVerifiedTick;
    @BindView(R.id.btn_looks_good)
    Button btnLooksGood;
    @BindView(R.id.btn_skip_ingredients)
    Button btnSkipIngredients;
    @BindView(R.id.traces)
    NachoTextView traces;
    @BindView(R.id.section_traces)
    TextView tracesHeader;
    @BindView(R.id.hint_traces)
    TextView tracesHint;
    @BindView(R.id.grey_line2)
    View greyLine2;
    AllergenNameDao mAllergenNameDao;
    private Activity activity;
    private File photoFile;
    private String code;
    private List<String> allergens = new ArrayList<>();
    private OfflineSavedProduct mOfflineSavedProduct;
    private HashMap<String, String> productDetails = new HashMap<>();
    private String imagePath;
    private boolean edit_product;
    private Product product;
    private boolean newImageSelected;
    private String appLanguageCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product_ingredients, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getActivity().getIntent().getBooleanExtra("modify_nutrition_prompt", false)) {
            if (!getActivity().getIntent().getBooleanExtra("modify_category_prompt", false)) {
                ((AddProductActivity) getActivity()).proceed();
            }
        }
        Bundle b = getArguments();
        if (b != null) {
            mAllergenNameDao = Utils.getAppDaoSession(activity).getAllergenNameDao();
            appLanguageCode = Locale.getDefault().getLanguage();
            product = (Product) b.getSerializable("product");
            mOfflineSavedProduct = (OfflineSavedProduct) b.getSerializable("edit_offline_product");
            edit_product = b.getBoolean("edit_product");
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
                //addition
                if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("fastAdditionMode", false)) {
                    enableFastAdditionMode(true);
                } else {
                    enableFastAdditionMode(false);
                }
            }
            if(b.getBoolean("perform_ocr")) {
                extractIngredients();
            }
            if (b.getBoolean("send_updated")) {
                newIngredientsImage();
            }
        } else {
            Toast.makeText(activity, R.string.error_adding_ingredients, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        if (ingredients.getText().toString().isEmpty() && productDetails.get("image_ingredients") != null && !productDetails.get("image_ingredients").isEmpty()) {
            extractIngredients.setVisibility(View.VISIBLE);
            imagePath = productDetails.get("image_ingredients");
        } else if (edit_product && ingredients.getText().toString().isEmpty() && product.getImageIngredientsUrl() != null && !product.getImageIngredientsUrl().isEmpty()) {
            extractIngredients.setVisibility(View.VISIBLE);
        }
        loadAutoSuggestions();
    }

    /**
     * Pre fill the fields of the product which are already present on the server.
     */
    private void preFillProductValues() {
        if (product.getImageIngredientsUrl() != null && !product.getImageIngredientsUrl().isEmpty()) {
            imageProgress.setVisibility(View.VISIBLE);
            imagePath = product.getImageIngredientsUrl();
            Picasso.with(getContext())
                    .load(product.getImageIngredientsUrl())
                    .resize(dpsToPixels(), dpsToPixels())
                    .centerInside()
                    .into(imageIngredients, new Callback() {
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
        if (product.getIngredientsText() != null && !product.getIngredientsText().isEmpty()) {
            ingredients.setText(product.getIngredientsText());
        }
        if (product.getTracesTags() != null && !product.getTracesTags().isEmpty()) {
            List<String> tracesTags = product.getTracesTags();
            final List<String> chipValues = new ArrayList<>();
            for (String tag : tracesTags) {
                chipValues.add(getTracesName(appLanguageCode, tag));
            }
            traces.setText(chipValues);
        }
    }

    private String getTracesName(String languageCode, String tag) {
        AllergenName allergenName = mAllergenNameDao.queryBuilder().where(AllergenNameDao.Properties.AllergenTag.eq(tag), AllergenNameDao.Properties.LanguageCode.eq(languageCode)).unique();
        if (allergenName != null) return allergenName.getName();
        return tag;
    }

    /**
     * To enable fast addition mode
     *
     * @param isEnabled
     */
    private void enableFastAdditionMode(boolean isEnabled) {
        if (isEnabled) {
            traces.setVisibility(View.GONE);
            tracesHeader.setVisibility(View.GONE);
            tracesHint.setVisibility(View.GONE);
            greyLine2.setVisibility(View.GONE);
        } else {
            traces.setVisibility(View.VISIBLE);
            tracesHeader.setVisibility(View.VISIBLE);
            tracesHint.setVisibility(View.VISIBLE);
            greyLine2.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Pre fill the fields if the product is already present in SavedProductOffline db.
     */
    private void preFillValues() {
        productDetails = mOfflineSavedProduct.getProductDetailsMap();
        if (productDetails != null) {
            if (productDetails.get("image_ingredients") != null) {
                imageProgress.setVisibility(View.VISIBLE);
                Picasso.with(getContext())
                        .load("file://" + productDetails.get("image_ingredients"))
                        .resize(dpsToPixels(), dpsToPixels())
                        .centerInside()
                        .into(imageIngredients, new Callback() {
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
            String lc = productDetails.get(PARAM_LANGUAGE) != null ? productDetails.get(PARAM_LANGUAGE) : "en";
            if (productDetails.get(PARAM_INGREDIENTS + "_" + lc) != null) {
                ingredients.setText(productDetails.get(PARAM_INGREDIENTS + "_" + lc));
            } else if (productDetails.get(PARAM_INGREDIENTS + "_" + "en") != null) {
                ingredients.setText(productDetails.get(PARAM_INGREDIENTS + "_" + "en"));
            }
            if (productDetails.get(PARAM_TRACES) != null) {
                List<String> chipValues = Arrays.asList(productDetails.get(PARAM_TRACES).split("\\s*,\\s*"));
                traces.setText(chipValues);
            }
        }
    }

    private void loadAutoSuggestions() {
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        AsyncSession asyncSessionAllergens = daoSession.startAsyncSession();
        AllergenNameDao allergenNameDao = daoSession.getAllergenNameDao();

        asyncSessionAllergens.queryList(allergenNameDao.queryBuilder()
                .where(AllergenNameDao.Properties.LanguageCode.eq(appLanguageCode))
                .orderDesc(AllergenNameDao.Properties.Name).build());

        asyncSessionAllergens.setListenerMainThread(operation -> {
            @SuppressWarnings("unchecked")
            List<AllergenName> allergenNames = (List<AllergenName>) operation.getResult();
            allergens.clear();
            for (int i = 0; i < allergenNames.size(); i++) {
                allergens.add(allergenNames.get(i).getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                    android.R.layout.simple_dropdown_item_1line, allergens);
            traces.addChipTerminator(',', BEHAVIOR_CHIPIFY_CURRENT_TOKEN);
            traces.setNachoValidator(new ChipifyingNachoValidator());
            traces.enableEditChipOnTouch(false, true);
            traces.setAdapter(adapter);
        });
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

    @OnClick(R.id.btnAddImageIngredients)
    void addIngredientsImage() {
        if (imagePath != null) {
            // ingredients image is already added. Open full screen image.
            Intent intent = new Intent(getActivity(), FullScreenImage.class);
            Bundle bundle = new Bundle();
            if (edit_product && !newImageSelected) {
                bundle.putString("imageurl", imagePath);
            } else {
                bundle.putString("imageurl", "file://" + imagePath);
            }
            intent.putExtras(bundle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(activity, imageIngredients,
                                activity.getString(R.string.product_transition));
                startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }
        } else {
            // add ingredients image.
            if (ContextCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                EasyImage.openCamera(this, 0);
            }
        }
    }

    @OnLongClick(R.id.btnAddImageIngredients)
    boolean newIngredientsImage() {
        if (ContextCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            EasyImage.openCamera(this, 0);
        }
        return true;
    }

    @OnClick(R.id.btn_next)
    void next() {
        Activity activity = getActivity();
        if (activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).proceed();
        }
    }

    @OnClick(R.id.btn_looks_good)
    void ingredientsVerified() {
        ingredientsVerifiedTick.setVisibility(View.VISIBLE);
        traces.requestFocus();
        btnLooksGood.setVisibility(View.GONE);
        btnSkipIngredients.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_skip_ingredients)
    void skipIngredients() {
        ingredients.setText(null);
        btnSkipIngredients.setVisibility(View.GONE);
        btnLooksGood.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_extract_ingredients)
    void extractIngredients() {
        if (activity instanceof AddProductActivity) {
            if (imagePath != null && (!edit_product || newImageSelected)) {
                photoFile = new File(imagePath);
                ProductImage image = new ProductImage(code, INGREDIENTS, photoFile);
                image.setFilePath(imagePath);
                ((AddProductActivity) activity).addToPhotoMap(image, 1);
            } else if (imagePath != null) {
                ((AddProductActivity) activity).performOCR(code, "ingredients_" + ((AddProductActivity) activity).getProductLanguage());
            }
        }
    }

    @OnTextChanged(value = R.id.ingredients_list, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void toggleExtractIngredientsButtonVisibility() {
        if (ingredients.getText().toString().isEmpty()) {
            extractIngredients.setVisibility(View.VISIBLE);
        } else {
            extractIngredients.setVisibility(View.GONE);
        }
    }

    /**
     * adds all the fields to the query map even those which are null or empty.
     */
    public void getAllDetails() {
        traces.chipifyAllUnterminatedTokens();
        if (activity instanceof AddProductActivity) {
            String languageCode = ((AddProductActivity) activity).getProductLanguage();
            String lc = (!languageCode.isEmpty()) ? languageCode : "en";
            ((AddProductActivity) activity).addToMap(PARAM_INGREDIENTS + "_" + lc, ingredients.getText().toString());
            List<String> list = traces.getChipValues();
            String string = StringUtils.join(list, ',');
            ((AddProductActivity) activity).addToMap(PARAM_TRACES.substring(4), string);
        }
    }

    /**
     * adds only those fields to the query map which are not empty.
     */
    public void getDetails() {
        traces.chipifyAllUnterminatedTokens();
        if (activity instanceof AddProductActivity) {
            if (!ingredients.getText().toString().isEmpty()) {
                String languageCode = ((AddProductActivity) activity).getProductLanguage();
                String lc = (!languageCode.isEmpty()) ? languageCode : "en";
                ((AddProductActivity) activity).addToMap(PARAM_INGREDIENTS + "_" + lc, ingredients.getText().toString());
            }
            if (!traces.getChipValues().isEmpty()) {
                List<String> list = traces.getChipValues();
                String string = StringUtils.join(list, ',');
                ((AddProductActivity) activity).addToMap(PARAM_TRACES, string);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imagePath = resultUri.getPath();
                newImageSelected = true;
                photoFile = new File((resultUri.getPath()));
                ProductImage image = new ProductImage(code, INGREDIENTS, photoFile);
                image.setFilePath(resultUri.getPath());
                if (activity instanceof AddProductActivity) {
                    ((AddProductActivity) activity).addToPhotoMap(image, 1);
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
                CropImage.activity(Uri.fromFile(imageFiles.get(0)))
                        .setAllowFlipping(false)
                        .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
                        .setOutputUri(Utils.getOutputPicUri(getContext()))
                        .start(activity.getApplicationContext(), AddProductIngredientsFragment.this);
            }
        });
    }

    public void showImageProgress() {
        imageProgress.setVisibility(View.VISIBLE);
        imageProgressText.setVisibility(View.VISIBLE);
        imageProgressText.setText(R.string.toastSending);
        imageIngredients.setVisibility(View.INVISIBLE);
    }

    public void hideImageProgress(boolean errorInUploading, String message) {
        imageProgress.setVisibility(View.INVISIBLE);
        imageProgressText.setVisibility(View.GONE);
        imageIngredients.setVisibility(View.VISIBLE);
        if (!errorInUploading) {
            Picasso.with(activity)
                    .load(photoFile)
                    .resize(dpsToPixels(), dpsToPixels())
                    .centerInside()
                    .into(imageIngredients);
            imageProgressText.setText(message);
            imageProgressText.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        }
    }

    public void setIngredients(String status, String ocrResult) {
        switch (status) {
            case "set":
                ingredients.setText(ocrResult);
                break;
            case "0":
                ingredients.setText(ocrResult);
                btnLooksGood.setVisibility(View.VISIBLE);
                btnSkipIngredients.setVisibility(View.VISIBLE);
                break;
            default:
                Toast.makeText(activity, R.string.unable_to_extract_ingredients, Toast.LENGTH_SHORT).show();
                break;
        }

    }

    public void showOCRProgress() {
        extractIngredients.setVisibility(View.GONE);
        ingredients.setText(null);
        ocrProgress.setVisibility(View.VISIBLE);
        ocrProgressText.setVisibility(View.VISIBLE);
    }

    public void hideOCRProgress() {
        ocrProgress.setVisibility(View.GONE);
        ocrProgressText.setVisibility(View.GONE);
    }

    private int dpsToPixels() {
        // converts 50dp to equivalent pixels.
        final float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (50 * scale + 0.5f);
    }
}
