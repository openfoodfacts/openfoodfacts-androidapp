package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Additive;
import openfoodfacts.github.scrachx.openfood.models.AdditiveDao;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.SaveProductOfflineActivity;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.INGREDIENTS;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class IngredientsProductFragment extends BaseFragment {

    public static final Pattern INGREDIENT_PATTERN = Pattern.compile("[\\p{L}\\p{Nd}(),.-]+");
    public static final Pattern ALLERGEN_PATTERN = Pattern.compile("[\\p{L}\\p{Nd}]+");
    @BindView(R.id.textIngredientProduct)
    TextView ingredientsProduct;
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
    @BindView(R.id.imageViewIngredients)
    ImageView mImageIngredients;
    @BindView(R.id.addPhotoLabel)
    TextView addPhotoLabel;

    private OpenFoodAPIClient api;
    private String mUrlImage;
    private State mState;
    private String barcode;
    private AdditiveDao mAdditiveDao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        api = new OpenFoodAPIClient(getActivity());

        return createView(inflater, container, R.layout.fragment_ingredients_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        mState = (State) intent.getExtras().getSerializable("state");
        mAdditiveDao = Utils.getAppDaoSession(getActivity()).getAdditiveDao();

        final Product product = mState.getProduct();
        barcode = product.getCode();

        if (isNotBlank(product.getImageIngredientsUrl())) {
            addPhotoLabel.setVisibility(View.GONE);

            Picasso.with(view.getContext())
                    .load(product.getImageIngredientsUrl())
                    .into(mImageIngredients);

            mUrlImage = product.getImageIngredientsUrl();
        }

        List<String> allergens = getAllergens();

        if (mState != null && product.getIngredientsText() != null) {
            SpannableStringBuilder txtIngredients = new SpannableStringBuilder(product.getIngredientsText().replace("_", ""));
            txtIngredients = setSpanBoldBetweenTokens(txtIngredients, allergens);
            int ingredientsListAt = Math.max(0, txtIngredients.toString().indexOf(":"));
            if (!txtIngredients.toString().substring(ingredientsListAt).trim().isEmpty()) {
                ingredientsProduct.setText(txtIngredients);
            } else {
                ingredientsProduct.setVisibility(View.GONE);
            }
        }

        if (!allergens.isEmpty()) {
            substanceProduct.append(bold(getString(R.string.txtSubstances)));
            substanceProduct.append(" ");
            String delim = "";
            for (String allergen : allergens) {
                substanceProduct.append(delim);
                substanceProduct.append(allergen);
                delim = ", ";
            }
        } else {
            substanceProduct.setVisibility(View.GONE);
        }

        String traces;
        if (product.getTraces() == null) {
            traceProduct.setVisibility(View.GONE);
        } else {
            traces = product.getTraces().replace(",", ", ");
            if (traces.isEmpty()) {
                traceProduct.setVisibility(View.GONE);
            } else {
                traceProduct.append(bold(getString(R.string.txtTraces)));
                traceProduct.append(" ");
                traceProduct.append(traces);
            }
        }

        if (!product.getAdditivesTags().isEmpty()) {
            additiveProduct.setMovementMethod(LinkMovementMethod.getInstance());
            additiveProduct.append(bold(getString(R.string.txtAdditives)));
            additiveProduct.append(" ");
            additiveProduct.append("\n");

            for (String tag : product.getAdditivesTags()) {
                String tagWithoutLocale = tag.replaceAll("(en:|fr:)", "").toUpperCase(Locale.getDefault());
                final List<Additive> la = mAdditiveDao.queryBuilder().where(AdditiveDao.Properties.Code.eq(tagWithoutLocale.toUpperCase())).list();
                additiveProduct.append(getSpanTag(tagWithoutLocale, view));
                //Display additives list with full name
                if (la.size() >= 1) {
                    final Additive additive = la.get(0);
                    additiveProduct.append(" - ");
                    additiveProduct.append(additive.getName().split(",")[0]);
                    additiveProduct.append("\n");
                }
            }
        } else {
            additiveProduct.setVisibility(View.GONE);
        }

        if (product.getIngredientsFromPalmOilN() == 0 && product.getIngredientsFromOrThatMayBeFromPalmOilN() == 0) {
            palmOilProduct.setVisibility(View.GONE);
            mayBeFromPalmOilProduct.setVisibility(View.GONE);
        } else {
            if (!product.getIngredientsFromPalmOilTags().isEmpty()) {
                palmOilProduct.append(bold(getString(R.string.txtPalmOilProduct)));
                palmOilProduct.append(" ");
                palmOilProduct.append(product.getIngredientsFromPalmOilTags().toString().replaceAll("[\\[,\\]]", ""));
            } else {
                palmOilProduct.setVisibility(View.GONE);
            }
            if (!product.getIngredientsThatMayBeFromPalmOilTags().isEmpty()) {
                mayBeFromPalmOilProduct.append(bold(getString(R.string.txtMayBeFromPalmOilProduct)));
                mayBeFromPalmOilProduct.append(" ");
                mayBeFromPalmOilProduct.append(product.getIngredientsThatMayBeFromPalmOilTags().toString().replaceAll("[\\[,\\]]", ""));
            } else {
                mayBeFromPalmOilProduct.setVisibility(View.GONE);
            }
        }
    }

    private CharSequence getSpanTag(String tag, final View view) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder();

        final List<Additive> la = mAdditiveDao.queryBuilder().where(AdditiveDao.Properties.Code.eq(tag.toUpperCase(Locale.getDefault()))).list();
        if (la.size() >= 1) {
            final Additive additive = la.get(0);
            //disabled popup temporarily
          /*ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(view.getContext())
                            .title(additive.getCode() + " : " + additive.getName())
                            .content(additive.getRisk().toUpperCase(Locale.getDefault()))
                            .positiveText(R.string.txtOk)
                            .show();
                }
            };*/
            ssb.append(tag);
            // ssb.setSpan(clickableSpan, 0, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(" ");
        }
        return ssb;
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

    private List<String> getAllergens() {
        if (mState.getProduct() == null || mState.getProduct().getAllergens() == null) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>();
        Matcher m = ALLERGEN_PATTERN.matcher(mState.getProduct().getAllergens().replace(",", ""));
        while (m.find()) {
            final String tma = m.group();
            boolean canAdd = true;

            for (String allergen : list) {
                if (tma.equalsIgnoreCase(allergen)) {
                    canAdd = false;
                    break;
                }
            }

            if (canAdd) {
                list.add(tma);
            }
        }
        return list;
    }

    @OnClick(R.id.imageViewIngredients)
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
                EasyImage.openCamera(this, 0);
            }
        }
    }

    private void onPhotoReturned(File photoFile) {
        ProductImage image = new ProductImage(barcode, INGREDIENTS, photoFile);
        image.setFilePath(photoFile.getAbsolutePath());
        api.postImg(getContext(), image);
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
                    EasyImage.openCamera(this, 0);
                }
            }
        }
    }
}
