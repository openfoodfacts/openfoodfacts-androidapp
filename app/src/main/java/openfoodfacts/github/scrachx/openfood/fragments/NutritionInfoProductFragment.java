package openfoodfacts.github.scrachx.openfood.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.HeaderNutrimentItem;
import openfoodfacts.github.scrachx.openfood.models.NutrimentItem;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Nutriments.Nutriment;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrimentsRecyclerViewAdapter;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;
import static android.text.TextUtils.isEmpty;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.CARBOHYDRATES;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.CARBO_MAP;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.ENERGY;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.FAT;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.FAT_MAP;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.MINERALS_MAP;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.PROTEINS;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.PROT_MAP;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.VITAMINS_MAP;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.NUTRITION;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class NutritionInfoProductFragment extends BaseFragment {

    @BindView(R.id.textPerPortion)
    TextView mTextPerPortion;
    @BindView(R.id.imageViewNutrition)
    ImageView mImageNutrition;
    @BindView(R.id.addPhotoLabel)
    TextView addPhotoLabel;
    @BindView(R.id.nutriments_recycler_view)
    RecyclerView nutrimentsRecyclerView;

    private String mUrlImage;
    private String barcode;
    private OpenFoodAPIClient api;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        api = new OpenFoodAPIClient(getActivity());

        return createView(inflater, container, R.layout.fragment_nutrition_info_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

        final Product product = state.getProduct();
        barcode = product.getCode();
        Nutriments nutriments = product.getNutriments();
        List<NutrimentItem> nutrimentItems = new ArrayList<>();

        if (isNotBlank(product.getServingSize())) {
            mTextPerPortion.setText(getString(R.string.nutriment_serving_size) + " " + product.getServingSize());
        } else {
            mTextPerPortion.setVisibility(View.GONE);
        }

        if (isNotBlank(product.getImageNutritionUrl())) {
            addPhotoLabel.setVisibility(View.GONE);

            Picasso.with(view.getContext())
                    .load(product.getImageNutritionUrl())
                    .into(mImageNutrition);

            mUrlImage = product.getImageNutritionUrl();
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

        // use VERTICAL divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(nutrimentsRecyclerView.getContext(), VERTICAL);
        nutrimentsRecyclerView.addItemDecoration(dividerItemDecoration);

        // Header hack
        nutrimentItems.add(new NutrimentItem(null, null, null, null));

        // Energy
        Nutriment energy = nutriments.get(ENERGY);
        if (energy != null) {
            nutrimentItems.add(new NutrimentItem(getString(R.string.nutrition_energy_short_name), getEnergy(energy.getFor100g()), getEnergy(energy
                    .getForServing()), "kcal"));
        }

        // Fat        
        Nutriment fat = nutriments.get(FAT);
        if (fat != null) {
            nutrimentItems.add(new HeaderNutrimentItem(getString(R.string.nutrition_fat), fat.getFor100g(), fat.getForServing(), fat.getUnit()));

            nutrimentItems.addAll(getNutrimentItems(nutriments, FAT_MAP));
        }

        // Carbohydrates
        Nutriment carbohydrates = nutriments.get(CARBOHYDRATES);
        if (carbohydrates != null) {
            nutrimentItems.add(new HeaderNutrimentItem(getString(R.string.nutrition_carbohydrate),
                    carbohydrates.getFor100g(),
                    carbohydrates.getForServing(),
                    carbohydrates.getUnit()));

            nutrimentItems.addAll(getNutrimentItems(nutriments, CARBO_MAP));
        }

        // fiber
        nutrimentItems.addAll(getNutrimentItems(nutriments, Collections.singletonMap(Nutriments.FIBER, R.string.nutrition_fiber)));

        // Proteins
        Nutriment proteins = nutriments.get(PROTEINS);
        if (proteins != null) {
            nutrimentItems.add(new HeaderNutrimentItem(getString(R.string.nutrition_proteins),
                    proteins.getFor100g(),
                    proteins.getForServing(),
                    proteins.getUnit()));

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
            Nutriment nutriment = nutriments.get(entry.getKey());
            if (nutriment != null) {
                items.add(new NutrimentItem(getString(entry.getValue()),
                        nutriment.getFor100g(), nutriment.getForServing(), nutriment.getUnit()));
            }
        }

        return items;
    }

    private String getEnergy(String value) {
        String defaultValue = "0";
        if (defaultValue.equals(value) || isEmpty(value)) {
            return defaultValue;
        }

        try {
            int energyKcal = convertKjToKcal(Integer.parseInt(value));
            return String.valueOf(energyKcal);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int convertKjToKcal(int kj) {
        return kj != 0 ? Double.valueOf(((double) kj) / 4.1868d).intValue() : -1;
    }

    @OnClick(R.id.imageViewNutrition)
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
        ProductImage image = new ProductImage(barcode, NUTRITION, photoFile);
        image.setFilePath(photoFile.getAbsolutePath());
        api.postImg(getContext(), image);
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
