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

package openfoodfacts.github.scrachx.openfood.features;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Callback;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityFullScreenImageBinding;
import openfoodfacts.github.scrachx.openfood.features.adapters.LanguageDataAdapter;
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity;
import openfoodfacts.github.scrachx.openfood.images.ImageKeyHelper;
import openfoodfacts.github.scrachx.openfood.images.ImageSize;
import openfoodfacts.github.scrachx.openfood.images.ImageTransformationUtils;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.FileDownloader;
import openfoodfacts.github.scrachx.openfood.utils.FileUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.utils.SwipeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import pl.aprilapps.easyphotopicker.EasyImage;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;

import static android.Manifest.permission.CAMERA;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Activity to display/edit product images
 */
public class ImagesManageActivity extends BaseActivity {
    private static final int RESULTCODE_MODIFIED = 1;
    private static final int REQUEST_EDIT_IMAGE_AFTER_LOGIN = 1;
    private static final int REQUEST_ADD_IMAGE_AFTER_LOGIN = 2;
    private static final int REQUEST_CHOOSE_IMAGE_AFTER_LOGIN = 3;
    private static final int REQUEST_UNSELECT_IMAGE_AFTER_LOGIN = 4;
    public static final int REQUEST_EDIT_IMAGE = 1000;
    private static final int REQUEST_CHOOSE_IMAGE = 1001;
    private static final List<ProductImageField> TYPE_IMAGE = Arrays.asList(ProductImageField.FRONT, ProductImageField.INGREDIENTS, ProductImageField.NUTRITION);
    private ActivityFullScreenImageBinding binding;
    private OpenFoodAPIClient client;
    private File lastViewedImage;
    private PhotoViewAttacher attacher;
    private SharedPreferences settings;
    private CompositeDisposable disp;

    public static boolean isImageModified(int requestCode, int resultCode) {
        return requestCode == REQUEST_EDIT_IMAGE && resultCode == ImagesManageActivity.RESULTCODE_MODIFIED;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disp.dispose();
        binding = null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disp = new CompositeDisposable();
        client = new OpenFoodAPIClient(this);
        binding = ActivityFullScreenImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup onclick listeners
        binding.btnDone.setOnClickListener(v -> onExit());
        binding.btnUnselectImage.setOnClickListener(v -> unSelectImage());
        binding.btnChooseImage.setOnClickListener(v -> onChooseImage());
        binding.btnAddImage.setOnClickListener(v -> onAddImage());
        binding.btnChooseDefaultLanguage.setOnClickListener(v -> onSelectDefaultLanguage());
        binding.btnEditImage.setOnClickListener(v -> onStartEditExistingImage());

        binding.comboLanguages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onLanguageChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        binding.comboImageType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onImageTypeChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        settings = getSharedPreferences("prefs", 0);
        if (settings.getBoolean(getString(R.string.check_first_time), true)) {
            startShowCase(getString(R.string.title_image_type), getString(R.string.content_image_type), R.id.comboImageType, 1);
        }

        Intent intent = getIntent();

        Product product = (Product) intent.getSerializableExtra(ImageKeyHelper.PRODUCT);
        boolean canEdit = product != null;
        binding.btnEditImage.setVisibility(canEdit ? View.VISIBLE : View.INVISIBLE);
        binding.btnUnselectImage.setVisibility(binding.btnEditImage.getVisibility());

        attacher = new PhotoViewAttacher(binding.imageViewFullScreen);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //delaying the transition until the view has been laid out
            postponeEnterTransition();
        }
        new SwipeDetector(binding.imageViewFullScreen).setOnSwipeListener((v, swipeType) -> {
            if (swipeType == SwipeDetector.SwipeTypeEnum.LEFT_TO_RIGHT) {
                incrementImageType(-1);
            } else if (swipeType == SwipeDetector.SwipeTypeEnum.RIGHT_TO_LEFT) {
                incrementImageType(1);
            } else if (swipeType == SwipeDetector.SwipeTypeEnum.TOP_TO_BOTTOM) {
                onRefresh(true);
            } else {
                stopRefresh();
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item_white, generateImageTypeNames());
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        binding.comboImageType.setAdapter(adapter);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadLanguage();

        binding.comboImageType.setSelection(TYPE_IMAGE.indexOf(getSelectedType()));
        updateProductImagesInfo(null);
        onRefresh(false);
    }

    private void startShowCase(String title, String content, int viewId, final int type) {
        new GuideView.Builder(this)
            .setTitle(title)
            .setContentText(content)
            .setTargetView(findViewById(viewId))
            .setContentTextSize(12)
            .setTitleTextSize(16)
            .setDismissType(GuideView.DismissType.outside)
            .setGuideListener(view -> {
                switch (type) {
                    case 1:
                        startShowCase(getString(R.string.title_choose_language), getString(R.string.content_choose_language), R.id.comboLanguages, 2);
                        break;
                    case 2:
                        startShowCase(getString(R.string.title_add_photo), getString(R.string.content_add_photo), R.id.btnAddImage, 3);
                        break;
                    case 3:
                        startShowCase(getString(R.string.title_choose_photo), getString(R.string.content_choose_photo), R.id.btnChooseImage, 4);
                        break;
                    case 4:
                        startShowCase(getString(R.string.title_edit_photo), getString(R.string.content_edit_photo), R.id.btnEditImage, 5);
                        break;
                    case 5:
                        startShowCase(getString(R.string.title_unselect_photo), getString(R.string.content_unselect_photo), R.id.btnUnselectImage, 6);
                        break;
                    case 6:
                        startShowCase(getString(R.string.title_exit), getString(R.string.content_exit), R.id.btn_done, 7);
                        break;
                    case 7:
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(getString(R.string.check_first_time), false);
                        editor.apply();
                        break;
                }
            })
            .build()
            .show();
    }

    @NonNull
    private List<String> generateImageTypeNames() {
        List<String> images = new ArrayList<>();
        for (ProductImageField type : TYPE_IMAGE) {
            images.add(getResources().getString(ImageKeyHelper.getResourceId(type)));
        }
        return images;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void incrementImageType(int inc) {
        stopRefresh();
        int newPosition = binding.comboImageType.getSelectedItemPosition() + inc;
        final int count = binding.comboImageType.getAdapter().getCount();
        if (newPosition < 0) {
            newPosition = count - 1;
        } else {
            newPosition = newPosition % count;
        }
        binding.comboImageType.setSelection(newPosition, true);
    }

    private void loadLanguage() {
        Product product = getProduct();
        if (product == null) {
            return;
        }
        //we load all available languages for product/type
        String currentLanguage = getCurrentLanguage();
        final ProductImageField productImageField = getSelectedType();
        final Set<String> addedLanguages = new HashSet<>(product.getAvailableLanguageForImage(productImageField, ImageSize.DISPLAY));
        final List<LocaleHelper.LanguageData> languageForImage = LocaleHelper.getLanguageData(addedLanguages, true);
        int selectedIndex = LocaleHelper.find(languageForImage, currentLanguage);
        if (selectedIndex < 0) {
            addedLanguages.add(currentLanguage);
            languageForImage.add(LocaleHelper.getLanguageData(currentLanguage, false));
        }
        String[] localeValues = getResources().getStringArray(R.array.languages_array);
        List<String> otherNotSupportedCode = new ArrayList<>();
        for (String local : localeValues) {
            if (!addedLanguages.contains(local)) {
                otherNotSupportedCode.add(local);
            }
        }
        languageForImage.addAll(LocaleHelper.getLanguageData(otherNotSupportedCode, false));
        LanguageDataAdapter adapter = new LanguageDataAdapter(this, R.layout.simple_spinner_item_white, languageForImage);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        binding.comboLanguages.setAdapter(adapter);
        selectedIndex = LocaleHelper.find(languageForImage, currentLanguage);
        if (selectedIndex >= 0) {
            binding.comboLanguages.setSelection(selectedIndex);
        }
        updateLanguageStatus();
        updateSelectDefaultLanguageAction();
    }

    /**
     * Use to warn the user that there is no image for the selected image.
     */
    private boolean updateLanguageStatus() {
        final ProductImageField serializableExtra = getSelectedType();
        String imageUrl = getCurrentImageUrl();
        String languageUsedByImage = ImageKeyHelper.getLanguageCodeFromUrl(serializableExtra, imageUrl);
        String language = getCurrentLanguage();
        //if the language of the displayed image is not the same that the language in this activity
        //we use the language of the image
        boolean languageSupported = language.equals(languageUsedByImage);
        if (languageSupported) {
            binding.textInfo.setText(null);
            binding.textInfo.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            binding.textInfo.setText(R.string.image_not_defined_for_language);
            binding.textInfo.setTextColor(ContextCompat.getColor(this, R.color.orange));
        }
        binding.btnEditImage.setVisibility(languageSupported ? View.VISIBLE : View.GONE);
        binding.btnUnselectImage.setVisibility(binding.btnEditImage.getVisibility());
        return languageSupported;
    }

    private String getCurrentLanguage() {
        final String language = getIntent().getStringExtra(ImageKeyHelper.LANGUAGE);
        if (language == null) {
            return LocaleHelper.getLanguage(getBaseContext());
        }
        return language;
    }

    private void updateToolbarTitle(Product product) {
        if (product != null) {
            binding.toolbar.setTitle(String.format("%s / %s",
                StringUtils.defaultString(product.getLocalProductName(this)),
                binding.comboImageType.getSelectedItem().toString()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateToolbarTitle(getProduct());
    }

    private void onRefresh(boolean reloadProduct) {
        String imageUrl = getCurrentImageUrl();
        if (reloadProduct || imageUrl == null) {
            reloadProduct();
        } else {
            loadImage(imageUrl);
        }
    }

    private void loadImage(String imageUrl) {
        if (isNotEmpty(imageUrl)) {
            String url = imageUrl;
            if (FileUtils.isAbsolute(url)) {
                url = "file://" + url;
            }
            startRefresh(getString(R.string.txtLoading));
            Utils.picassoBuilder(this)
                .load(url)
                .into(binding.imageViewFullScreen, new Callback() {
                    @Override
                    public void onSuccess() {
                        attacher.update();
                        scheduleStartPostponedTransition(binding.imageViewFullScreen);
                        binding.imageViewFullScreen.setVisibility(View.VISIBLE);
                        stopRefresh();
                    }

                    @Override
                    public void onError(Exception ex) {
                        binding.imageViewFullScreen.setVisibility(View.VISIBLE);
                        Toast.makeText(ImagesManageActivity.this, getResources().getString(R.string.txtConnectionError), Toast.LENGTH_LONG).show();
                        stopRefresh();
                    }
                });
        } else {
            binding.imageViewFullScreen.setImageDrawable(null);
            stopRefresh();
        }
    }

    /**
     * Reloads product images from the server. Updates images and the language.
     */
    private void reloadProduct() {
        if (isFinishing()) {
            return;
        }
        Product product = getProduct();
        if (product != null) {
            startRefresh(getString(R.string.loading_product,
                StringUtils.defaultString(product.getLocalProductName(this) + "...")));
            client.getProductImages(product.getCode(), newState -> {
                final Product newStateProduct = newState.getProduct();
                boolean imageReloaded = false;
                if (newStateProduct != null) {
                    updateToolbarTitle(newStateProduct);
                    String imageUrl = getCurrentImageUrl();
                    getIntent().putExtra(ImageKeyHelper.PRODUCT, newStateProduct);
                    final String newImageUrl = getImageUrlToDisplay(newStateProduct);
                    loadLanguage();
                    if (imageUrl == null || !imageUrl.equals(newImageUrl)) {
                        getIntent().putExtra(ImageKeyHelper.IMAGE_URL, newImageUrl);
                        loadImage(newImageUrl);
                        imageReloaded = true;
                    }
                } else {
                    if (StringUtils.isNotBlank(newState.getStatusVerbose())) {
                        Toast.makeText(ImagesManageActivity.this, newState.getStatusVerbose(), Toast.LENGTH_LONG).show();
                    }
                }
                if (!imageReloaded) {
                    stopRefresh();
                }
            });
        }
    }

    /**
     * The additional field "images" is not loaded by default by OFF as it's only used to edit an image.
     * So we load the product images in background.
     * Could be improved by loading only the field "images".
     */
    private void updateProductImagesInfo(Runnable toDoAfter) {
        Product product = getProduct();
        if (product != null) {
            client.getProductImages(product.getCode(), newState -> {
                final Product newStateProduct = newState.getProduct();
                if (newStateProduct != null) {
                    getIntent().putExtra(ImageKeyHelper.PRODUCT, newStateProduct);
                }
                if (toDoAfter != null) {
                    toDoAfter.run();
                }
            });
        }
    }

    private String getImageUrlToDisplay(@NonNull Product product) {
        return product.getSelectedImage(getCurrentLanguage(), getSelectedType(),
            ImageSize.DISPLAY);
    }

    private String getCurrentImageUrl() {
        return getIntent().getStringExtra(ImageKeyHelper.IMAGE_URL);
    }

    /**
     * @see #startRefresh(String)
     */
    private void stopRefresh() {
        binding.progressBar.setVisibility(View.GONE);
        updateLanguageStatus();
    }

    private boolean isRefreshing() {
        return binding.progressBar.getVisibility() == View.VISIBLE;
    }

    /**
     * @param text
     * @see #stopRefresh()
     */
    private void startRefresh(@Nullable String text) {
        binding.progressBar.setVisibility(View.VISIBLE);
        if (text != null) {
            binding.textInfo.setTextColor(ContextCompat.getColor(this, R.color.white));
            binding.textInfo.setText(text);
        }
    }

    void onSelectDefaultLanguage() {
        String lang = LocaleHelper.getLocale(getProduct().getLang()).getLanguage();
        LocaleHelper.getLanguageData(lang, true);
        final int position = ((LanguageDataAdapter) binding.comboLanguages.getAdapter()).getPosition(lang);
        if (position >= 0) {
            binding.comboLanguages.setSelection(position, true);
        }
    }

    void onExit() {
        setResult(RESULT_OK);
        finish();
    }

    private void unSelectImage() {
        if (cannotEdit(REQUEST_UNSELECT_IMAGE_AFTER_LOGIN)) {
            return;
        }
        startRefresh(getString(R.string.unselect_image));
        client.unSelectImage(getProduct().getCode(), getSelectedType(), getCurrentLanguage(), (value, response) -> {
            if (value) {
                setResult(RESULTCODE_MODIFIED);
            }
            reloadProduct();
        });
    }

    private void onChooseImage() {
        if (cannotEdit(REQUEST_CHOOSE_IMAGE_AFTER_LOGIN)) {
            return;
        }
        final Intent intent = new Intent(this, ImagesSelectActivity.class);
        intent.putExtra(ImageKeyHelper.PRODUCT_BARCODE, getProduct().getCode());
        intent.putExtra(ImagesSelectActivity.TOOLBAR_TITLE, binding.toolbar.getTitle());
        startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
    }

    /**
     * Check if user is able to edit or not.
     *
     * @param loginRequestCode request code to pass to {@link #startActivityForResult(Intent, int)}.
     * @return true if user <strong>cannot edit</strong>, false otherwise.
     */
    private boolean cannotEdit(int loginRequestCode) {
        if (isRefreshing()) {
            Toast.makeText(this, R.string.cant_modify_if_refreshing, Toast.LENGTH_SHORT).show();
            return true;
        }
        //if user not logged in, we force to log
        if (!isUserLoggedIn()) {
            startActivityForResult(new Intent(this, LoginActivity.class), loginRequestCode);
            return true;
        }
        return false;
    }

    private void onAddImage() {
        if (cannotEdit(REQUEST_ADD_IMAGE_AFTER_LOGIN)) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            EasyImage.openCamera(this, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA && Utils.isAllGranted(grantResults)) {
            onAddImage();
        }
    }

    private void updateSelectDefaultLanguageAction() {
        boolean isDefault = getProduct().getLang() != null && getCurrentLanguage().equals(LocaleHelper.getLocale(getProduct().getLang()).getLanguage());
        binding.btnChooseDefaultLanguage.setVisibility(isDefault ? View.INVISIBLE : View.VISIBLE);
    }

    private void onStartEditExistingImage() {
        if (cannotEdit(REQUEST_EDIT_IMAGE_AFTER_LOGIN)) {
            return;
        }
        Product product = getProduct();
        final ProductImageField productImageField = getSelectedType();
        String language = getCurrentLanguage();
        //the rotation/crop set on the server
        ImageTransformationUtils transformation = ImageTransformationUtils.getScreenTransformation(product, productImageField, language);
        //the first time, the images properties are not loaded...
        if (transformation.isEmpty()) {
            updateProductImagesInfo(() -> editPhoto(productImageField, ImageTransformationUtils.getScreenTransformation(product, productImageField, language)));
        }
        editPhoto(productImageField, transformation);
    }

    private void editPhoto(ProductImageField productImageField, @NonNull ImageTransformationUtils transformation) {
        if (transformation.isNotEmpty()) {
            disp.add(FileDownloader.download(this, transformation.getInitImageUrl())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(file -> {
                    //to delete the file after:
                    lastViewedImage = file;
                    cropRotateExistingImageOnServer(file, getString(ImageKeyHelper.getResourceIdForEditAction(productImageField)), transformation);
                }));
        }
    }

    private Product getProduct() {
        return (Product) getIntent().getSerializableExtra(ImageKeyHelper.PRODUCT);
    }

    private void onLanguageChanged() {
        LocaleHelper.LanguageData data = (LocaleHelper.LanguageData) binding.comboLanguages.getSelectedItem();
        Product product = getProduct();
        if (!data.getCode().equals(getCurrentLanguage())) {
            getIntent().putExtra(ImageKeyHelper.LANGUAGE, data.getCode());
            getIntent().putExtra(ImageKeyHelper.IMAGE_URL, getImageUrlToDisplay(product));
            updateToolbarTitle(product);
            onRefresh(false);
        }
        updateSelectDefaultLanguageAction();
    }

    private ProductImageField getSelectedType() {
        return (ProductImageField) getIntent().getSerializableExtra(ImageKeyHelper.IMAGE_TYPE);
    }

    private void onImageTypeChanged() {
        if (getProduct() == null) {
            return;
        }
        ProductImageField newTypeSelected = TYPE_IMAGE.get(binding.comboImageType.getSelectedItemPosition());
        final ProductImageField selectedType = getSelectedType();
        if (newTypeSelected.equals(selectedType)) {
            return;
        }
        getIntent().putExtra(ImageKeyHelper.IMAGE_TYPE, newTypeSelected);
        getIntent().putExtra(ImageKeyHelper.IMAGE_URL, getImageUrlToDisplay(getProduct()));
        onRefresh(false);
        loadLanguage();
        updateToolbarTitle(getProduct());
    }

    private void cropRotateExistingImageOnServer(File image, String title, ImageTransformationUtils transformation) {
        Uri uri = Uri.fromFile(image);
        final CropImage.ActivityBuilder activityBuilder = CropImage.activity(uri)
            .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
            .setAllowFlipping(false)
            //we just want crop size/rotation
            .setNoOutputImage(true)
            .setAllowRotation(true)
            .setAllowCounterRotation(true)
            .setAutoZoomEnabled(false)
            .setInitialRotation(transformation.getRotationInDegree())

            .setActivityTitle(title);
        if (transformation.getCropRectangle() != null) {
            activityBuilder.setInitialCropWindowRectangle(transformation.getCropRectangle());
        } else {
            activityBuilder.setInitialCropWindowPaddingRatio(0);
        }
        startActivityForResult(activityBuilder.getIntent(this, CropImageActivity.class), REQUEST_EDIT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // do nothing
        switch (requestCode) {
            case REQUEST_EDIT_IMAGE_AFTER_LOGIN:
                if (resultCode == RESULT_OK) {
                    onStartEditExistingImage();
                }
                break;
            case REQUEST_ADD_IMAGE_AFTER_LOGIN:
                if (resultCode == RESULT_OK) {
                    onAddImage();
                }
                break;
            case REQUEST_CHOOSE_IMAGE_AFTER_LOGIN:
                if (resultCode == RESULT_OK) {
                    onChooseImage();
                }
                break;
            case REQUEST_UNSELECT_IMAGE_AFTER_LOGIN:
                if (resultCode == RESULT_OK) {
                    unSelectImage();
                }
                break;
            case REQUEST_EDIT_IMAGE:
                applyEditExistingImage(resultCode, data);
                break;
            case REQUEST_CHOOSE_IMAGE:
                if (resultCode == RESULT_OK && data != null) {
                    File file = (File) data.getSerializableExtra(ImageKeyHelper.IMAGE_FILE);
                    String imgId = data.getStringExtra(ImageKeyHelper.IMG_ID);
                    //photo choosed from gallery
                    if (file != null) {
                        onPhotoReturned(file);
                    } else if (StringUtils.isNotBlank(imgId)) {
                        HashMap<String, String> imgMap = new HashMap<>();
                        imgMap.put(ImageKeyHelper.IMG_ID, imgId);
                        postEditImage(imgMap);
                    }
                }
                break;
            default:
                new PhotoReceiverHandler(this::onPhotoReturned).onActivityResult(this, requestCode, resultCode, data);
                break;
        }
    }

    /**
     * @param resultCode should
     * @param dataFromCropActivity from the crop activity. If not, action is ignored
     */
    private void applyEditExistingImage(int resultCode, @Nullable Intent dataFromCropActivity) {
        // Delete downloaded local file
        deleteLocalFiles();
        // if the selected language is not the same than current image we can't modify: only add
        if (!isUserLoggedIn() || !updateLanguageStatus() || dataFromCropActivity == null) {
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            startRefresh(StringUtils.EMPTY);
            CropImage.ActivityResult result = CropImage.getActivityResult(dataFromCropActivity);
            final Product product = getProduct();
            ImageTransformationUtils currentServerTransformation = ImageTransformationUtils.getInitialServerTransformation(product, getSelectedType(), getCurrentLanguage());
            ImageTransformationUtils newServerTransformation = ImageTransformationUtils
                .toServerTransformation(new ImageTransformationUtils(result.getRotation(), result.getCropRect()), product, getSelectedType(), getCurrentLanguage());
            boolean isModified = !currentServerTransformation.equals(newServerTransformation);
            if (isModified) {
                startRefresh(getString(R.string.toastSending));
                HashMap<String, String> imgMap = new HashMap<>();
                imgMap.put(ImageKeyHelper.IMG_ID, newServerTransformation.getInitImageId());
                ImageTransformationUtils.addTransformToMap(newServerTransformation, imgMap);
                postEditImage(imgMap);
            } else {
                stopRefresh();
            }
        }
    }

    private void postEditImage(@NonNull HashMap<String, String> imgMap) {
        final String code = getProduct().getCode();
        imgMap.put(ImageKeyHelper.PRODUCT_BARCODE, code);
        imgMap.put(ImageKeyHelper.IMAGE_STRING_ID, ImageKeyHelper.getImageStringKey(getSelectedType(), getCurrentLanguage()));
        binding.imageViewFullScreen.setVisibility(View.INVISIBLE);
        client.editImage(code, imgMap, (value, response) -> {
            if (value) {
                setResult(RESULTCODE_MODIFIED);
            }
            reloadProduct();
        });
    }

    private void deleteLocalFiles() {
        if (lastViewedImage != null) {
            boolean deleted = lastViewedImage.delete();
            if (!deleted) {
                Log.w(ImagesManageActivity.class.getSimpleName(),
                    String.format("Cannot delete file %s.", lastViewedImage.getAbsolutePath()));
            } else {
                lastViewedImage = null;
            }
        }
    }

    /**
     * For scheduling a postponed transition after the proper measures of the view are done
     * and the view has been properly laid out in the View hierarchy
     */
    private void scheduleStartPostponedTransition(@NonNull final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
            new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startPostponedEnterTransition();
                    }
                    return true;
                }
            });
    }

    /**
     * @param newPhotoFile photo selected by the user to be sent to the server.
     */
    public void onPhotoReturned(File newPhotoFile) {
        startRefresh(getString(R.string.uploading_image));
        ProductImage image = new ProductImage(getProduct().getCode(), getSelectedType(), newPhotoFile, getCurrentLanguage());
        image.setFilePath(newPhotoFile.getAbsolutePath());
        disp.add(client.postImg(image, true).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
            reloadProduct();
            setResult(RESULTCODE_MODIFIED);
        }, throwable -> {
            Toast.makeText(ImagesManageActivity.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(ImagesManageActivity.class.getSimpleName(), throwable.getMessage(), throwable);
            stopRefresh();
        }));
    }
}
