package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
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
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.SaveProductOfflineActivity;
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
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.OTHER;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SummaryProductFragment extends BaseFragment implements CustomTabActivityHelper.ConnectionCallback {

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        api = new OpenFoodAPIClient(getActivity());

        return createView(inflater, container, R.layout.fragment_summary_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        final State state = (State) intent.getExtras().getSerializable("state");

        final Product product = state.getProduct();
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
            packagingProduct.setText(bold(getString(R.string.txtPackaging)));
            packagingProduct.append(' ' + product.getPackaging());
        } else {
            packagingProduct.setVisibility(View.GONE);
        }
        if (isNotBlank(product.getBrands())) {
            brandProduct.setText(bold(getString(R.string.txtBrands)));
            brandProduct.append(' ' + product.getBrands());
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

        String categ;
        if (isNotBlank(product.getCategories())) {
            categ = product.getCategories().replace(",", ", ");
            categoryProduct.setText(bold(getString(R.string.txtCategories)));
            categoryProduct.append(' ' + categ);
        } else {
            categoryProduct.setVisibility(View.GONE);
        }

        String labels = product.getLabels();
        if (isNotBlank(labels)) {
            labelProduct.append(bold(getString(R.string.txtLabels)));
            labelProduct.append(" ");
            String[] label = labels.split(",");
            int labelCount = label.length;
            if (labelCount > 1) {
                for (int i = 0; i < (labelCount - 1); i++) {
                    labelProduct.append(label[i].trim());
                    labelProduct.append(", ");
                }
                labelProduct.append(label[labelCount - 1].trim());
            } else {
                labelProduct.append(label[0].trim());
            }
        } else {
            labelProduct.setVisibility(View.GONE);
        }

        if (product.getEmbTags() != null && !product.getEmbTags().toString().trim().equals("[]")) {
            embCode.setMovementMethod(LinkMovementMethod.getInstance());
            embCode.setText(bold(getString(R.string.txtEMB)));
            embCode.append(" ");
            String[] embTags = product.getEmbTags().toString().replace("[", "").replace("]", "").split(", ");
            for (String embTag : embTags) {
                embCode.append(getSpanTag(getEmbCode(embTag), getEmbUrl(embTag)));
            }

        } else {
            embCode.setVisibility(View.GONE);
        }
        if (isNotBlank(product.getStores())) {
            storeProduct.setText(bold(getString(R.string.txtStores)));
            storeProduct.append(' ' + product.getStores());
        } else {
            storeProduct.setVisibility(View.GONE);
        }
        if (isNotBlank(product.getManufactureUrl())) {
            Uri manufactureUri = Uri.parse(product.getManufactureUrl());
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
        if (isNotBlank(product.getCountries())) {
            countryProduct.setText(bold(getString(R.string.txtCountries)));
            countryProduct.append(' ' + product.getCountries());
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

    private CharSequence getSpanTag(String embCode, String embUrl) {
        final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
                embCodeUri = Uri.parse("https://world.openfoodfacts.org/packager-code/" + embUrl);
                CustomTabActivityHelper.openCustomTab(SummaryProductFragment.this.getActivity(), customTabsIntent, embCodeUri, new WebViewFallback());
            }
        };
        spannableStringBuilder.append(embCode);
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append(" ");
        return spannableStringBuilder;
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
            startActivity(intent);
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
                    ProductImage image = new ProductImage(barcode, OTHER, new  File(resultUri.getPath()));
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
                        .start(getActivity());
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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
