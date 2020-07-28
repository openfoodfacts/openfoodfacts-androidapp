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

package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.AppFlavors;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevels;
import openfoodfacts.github.scrachx.openfood.models.NutrimentLevel;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.CompatibiltyUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.FullScreenActivityOpener;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class ProductComparisonAdapter extends RecyclerView.Adapter<ProductComparisonAdapter.ProductComparisonViewHolder> {
    private final Button addProductButton;
    private final OpenFoodAPIClient api;
    private boolean isLowBatteryMode = false;
    private final Context context;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private final List<Product> productsToCompare;
    private final ProductRepository repository = ProductRepository.getInstance();
    private final ArrayList<ProductComparisonViewHolder> viewHolders = new ArrayList<>();
    private Integer onPhotoReturnPosition;

    public ProductComparisonAdapter(List<Product> productsToCompare, Context context) {
        this.productsToCompare = productsToCompare;
        this.context = context;
        this.addProductButton = ((Activity) context).findViewById(R.id.product_comparison_button);
        api = new OpenFoodAPIClient(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ProductComparisonViewHolder holder, int position) {
        if (productsToCompare.isEmpty()) {
            holder.listItemLayout.setVisibility(View.GONE);
            return;
        }

        // Support synchronous scrolling
        if (CompatibiltyUtils.isOnScrollChangeListenerAvailable()) {
            holder.listItemLayout.setOnScrollChangeListener((View.OnScrollChangeListener) (view, i, i1, i2, i3) -> {
                for (ProductComparisonViewHolder viewHolder : viewHolders) {
                    viewHolder.listItemLayout.setScrollX(i);
                    viewHolder.listItemLayout.setScrollY(i1);
                }
            });
        }

        Product product = productsToCompare.get(position);

        // Set the visibility of UI components
        holder.productNameTextView.setVisibility(View.VISIBLE);
        holder.productQuantityTextView.setVisibility(View.VISIBLE);
        holder.productBrandTextView.setVisibility(View.VISIBLE);

        // Modify the text on the button for adding products
        if (this.addProductButton != null) {
            addProductButton.setText(R.string.add_another_product);
        }

        // Image
        final String imageUrl = product.getImageUrl(LocaleHelper.getLanguage(context));
        holder.productComparisonImage.setOnClickListener(view -> {
            if (imageUrl != null) {
                FullScreenActivityOpener.openForUrl((Activity) context, product, FRONT, imageUrl, holder.productComparisonImage);
            } else {
                // take a picture
                if (ContextCompat.checkSelfPermission(context, CAMERA) != PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                } else {
                    onPhotoReturnPosition = position;
                    if (Utils.isHardwareCameraInstalled(context)) {
                        EasyImage.openCamera(((Activity) context), 0);
                    } else {
                        EasyImage.openGallery(((Activity) context), 0, false);
                    }
                }
            }
        });

        if (isNotBlank(imageUrl)) {
            holder.productComparisonLabel.setVisibility(View.INVISIBLE);

            if (Utils.isDisableImageLoad(context) && Utils.isBatteryLevelLow(context)) {
                isLowBatteryMode = true;
            }
            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Picasso.get()
                    .load(imageUrl)
                    .into(holder.productComparisonImage);
            } else {
                holder.productComparisonImage.setVisibility(View.GONE);
            }
        }

        // Name
        if (isNotBlank(product.getProductName())) {
            holder.productNameTextView.setText(product.getProductName());
        } else {
            //product name placeholder text goes here
        }

        // Quantity
        if (isNotBlank(product.getQuantity())) {
            holder.productQuantityTextView.setText(bold(
                context.getString(R.string.compare_quantity)
            ));
            holder.productQuantityTextView.append(' ' + product.getQuantity());
        } else {
            //product quantity placeholder goes here
        }

        // Brands
        if (isNotBlank(product.getBrands())) {
            holder.productBrandTextView.setText(bold(context.getString(R.string.compare_brands)));
            holder.productBrandTextView.append(" ");

            String[] brands = product.getBrands().split(",");
            for (int i = 0; i < brands.length - 1; i++) {
                holder.productBrandTextView.append(brands[i].trim());
                holder.productBrandTextView.append(", ");
            }
            holder.productBrandTextView.append(brands[brands.length - 1].trim());
        } else {
            //product brand placeholder goes here
        }

        // Open Food Facts specific
        if (Utils.isFlavor(AppFlavors.OFF)) {
            // NutriScore
            int nutritionGradeResource = Utils.getImageGrade(product);
            if (nutritionGradeResource != Utils.NO_DRAWABLE_RESOURCE) {
                holder.productComparisonImageGrade.setVisibility(View.VISIBLE);
                holder.productComparisonImageGrade.setImageResource(nutritionGradeResource);
            } else {
                holder.productComparisonImageGrade.setVisibility(View.INVISIBLE);
            }

            // Nova group
            if (product.getNovaGroups() != null) {
                holder.productComparisonNovaGroup.setImageResource(Utils.getNovaGroupDrawable(product.getNovaGroups()));
            } else {
                holder.productComparisonNovaGroup.setVisibility(View.INVISIBLE);
            }

            // Environment impact
            int environmentImpactResource = Utils.getImageEnvironmentImpact(product);
            if (environmentImpactResource != Utils.NO_DRAWABLE_RESOURCE) {
                holder.productComparisonCo2Icon.setVisibility(View.VISIBLE);
                holder.productComparisonCo2Icon.setImageResource(environmentImpactResource);
            } else {
                holder.productComparisonCo2Icon.setVisibility(View.GONE);
            }

            // Nutriments
            if (product.getNutriments() != null) {
                holder.nutrientsRecyclerView.setVisibility(View.VISIBLE);
                holder.productComparisonNutrientText.setText(context.getString(R.string.txtNutrientLevel100g));
                holder.nutrientsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                holder.nutrientsRecyclerView.setAdapter(new NutrientLevelListAdapter(context, loadLevelItems(product)));
            }
        } else {
            holder.productComparisonScoresLayout.setVisibility(View.GONE);
            holder.productComparisonNutrientCv.setVisibility(View.GONE);
        }

        // Additives
        List<String> additivesTags = product.getAdditivesTags();
        if (additivesTags != null && !additivesTags.isEmpty()) {
            loadAdditives(product, holder.productComparisonAdditiveText);
        }

        // Full product button
        holder.fullProductButton.setOnClickListener(view -> {
            if (product != null) {
                String barcode = product.getCode();
                if (Utils.isNetworkConnected(context)) {
                    api.openProduct(barcode, (Activity) context);
                    try {
                        View view1 = ((Activity) context).getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                        }
                    } catch (NullPointerException e) {
                        Log.e(ProductComparisonAdapter.class.getSimpleName(), "setOnClickListener", e);
                    }
                } else {
                    new MaterialDialog.Builder(context)
                        .title(R.string.device_offline_dialog_title)
                        .content(R.string.connectivity_check)
                        .positiveText(R.string.txt_try_again)
                        .negativeText(R.string.dismiss)
                        .onPositive((dialog, which) -> {
                            if (Utils.isNetworkConnected(context)) {
                                api.openProduct(barcode, (Activity) context);
                            } else {
                                Toast.makeText(context, R.string.device_offline_dialog_title, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
                }
            }
        });
    }

    @NonNull
    @Override
    public ProductComparisonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_comparison_list_item, parent, false);
        ProductComparisonViewHolder viewHolder = new ProductComparisonViewHolder(v);
        viewHolders.add(viewHolder);
        return viewHolder;
    }

    private void loadAdditives(Product product, View v) {
        StringBuilder additivesBuilder = new StringBuilder();
        List<String> additivesTags = product.getAdditivesTags();
        if (additivesTags == null || additivesTags.isEmpty()) {
            return;
        }
        final String languageCode = LocaleHelper.getLanguage(v.getContext());
        disposable.add(
            Observable.fromArray(additivesTags.toArray(new String[0]))
                .flatMapSingle(tag -> repository.getAdditiveByTagAndLanguageCode(tag, languageCode)
                    .flatMap(categoryName -> {
                        if (categoryName.isNull()) {
                            return repository.getAdditiveByTagAndDefaultLanguageCode(tag);
                        } else {
                            return Single.just(categoryName);
                        }
                    }))
                .filter(AdditiveName::isNotNull)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(additives -> {
                    if (!additives.isEmpty()) {
                        additivesBuilder.append(bold(context.getString(R.string.compare_additives)));
                        additivesBuilder.append(" ");
                        additivesBuilder.append("\n");

                        for (int i = 0; i < additives.size() - 1; i++) {
                            additivesBuilder.append(additives.get(i).getName());
                            additivesBuilder.append("\n");
                        }

                        additivesBuilder.append(additives.get(additives.size() - 1).getName());
                        ((TextView) v).setText(additivesBuilder.toString());
                        setMaxCardHeight();
                    }
                }, e -> Log.e(ProductComparisonAdapter.class.getSimpleName(), "loadAdditives", e))
        );
    }

    @Override
    public int getItemCount() {
        return productsToCompare.size();
    }

    private List<NutrientLevelItem> loadLevelItems(Product product) {
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

        if (nutriments != null && !(fat == null && salt == null && saturatedFat == null && sugars == null)) {

            Nutriments.Nutriment fatNutriment = nutriments.get(Nutriments.FAT);
            if (fat != null && fatNutriment != null) {
                String fatNutrimentLevel = fat.getLocalize(context);
                levelItem.add(new NutrientLevelItem(
                    context.getString(R.string.compare_fat),
                    fatNutriment.getDisplayStringFor100g(),
                    fatNutrimentLevel,
                    fat.getImageLevel()));
            }

            Nutriments.Nutriment saturatedFatNutriment = nutriments.get(Nutriments.SATURATED_FAT);
            if (saturatedFat != null && saturatedFatNutriment != null) {
                String saturatedFatLocalize = saturatedFat.getLocalize(context);
                levelItem.add(new NutrientLevelItem(
                    context.getString(R.string.compare_saturated_fat),
                    saturatedFatNutriment.getDisplayStringFor100g(),
                    saturatedFatLocalize,
                    saturatedFat.getImageLevel()));
            }

            Nutriments.Nutriment sugarsNutriment = nutriments.get(Nutriments.SUGARS);
            if (sugars != null && sugarsNutriment != null) {
                String sugarsLocalize = sugars.getLocalize(context);
                levelItem.add(new NutrientLevelItem(
                    context.getString(R.string.compare_sugars),
                    sugarsNutriment.getDisplayStringFor100g(),
                    sugarsLocalize,
                    sugars.getImageLevel()));
            }

            Nutriments.Nutriment saltNutriment = nutriments.get(Nutriments.SALT);
            if (salt != null && saltNutriment != null) {
                String saltLocalize = salt.getLocalize(context);
                levelItem.add(new NutrientLevelItem(
                    context.getString(R.string.compare_salt),
                    saltNutriment.getDisplayStringFor100g(),
                    saltLocalize,
                    salt.getImageLevel()
                ));
            }
        }
        return levelItem;
    }

    public void setImageOnPhotoReturn(File file) {
        Product product = productsToCompare.get(onPhotoReturnPosition);
        ProductImage image = new ProductImage(product.getCode(), FRONT, file);
        image.setFilePath(file.getAbsolutePath());
        api.postImg(image, null);
        String mUrlImage = file.getAbsolutePath();
        product.setImageUrl(mUrlImage);
        onPhotoReturnPosition = null;
        notifyDataSetChanged();
    }

    private void setMaxCardHeight() {
        //getting all the heights of CardViews
        ArrayList<Integer> productDetailsHeight = new ArrayList<>();
        ArrayList<Integer> productNutrientsHeight = new ArrayList<>();
        ArrayList<Integer> productAdditivesHeight = new ArrayList<>();
        for (ProductComparisonViewHolder current : viewHolders) {
            productDetailsHeight.add(current.productComparisonDetailsCv.getHeight());
            productNutrientsHeight.add(current.productComparisonNutrientCv.getHeight());
            productAdditivesHeight.add(current.productComparisonAdditiveText.getHeight());
        }

        //setting all the heights to be the maximum
        for (ProductComparisonViewHolder current : viewHolders) {
            current.productComparisonDetailsCv.setMinimumHeight(Collections.max(productDetailsHeight));
            current.productComparisonNutrientCv.setMinimumHeight(Collections.max(productNutrientsHeight));
            current.productComparisonAdditiveText.setHeight(dpsToPixel(Collections.max(productAdditivesHeight)));
        }
    }

    public static class ProductComparisonViewHolder extends RecyclerView.ViewHolder {
        final Button fullProductButton;
        final NestedScrollView listItemLayout;
        final RecyclerView nutrientsRecyclerView;
        final TextView productBrandTextView;
        final CardView productComparisonAdditiveCv;
        final TextView productComparisonAdditiveText;
        final ImageView productComparisonCo2Icon;
        final CardView productComparisonDetailsCv;
        final ImageButton productComparisonImage;
        final ImageView productComparisonImageGrade;
        final TextView productComparisonLabel;
        final ImageView productComparisonNovaGroup;
        final CardView productComparisonNutrientCv;
        final TextView productComparisonNutrientText;
        final RelativeLayout productComparisonScoresLayout;
        final TextView productNameTextView;
        final TextView productQuantityTextView;

        public ProductComparisonViewHolder(View view) {
            super(view);
            listItemLayout = view.findViewById(R.id.product_comparison_list_item_layout);
            productComparisonDetailsCv = view.findViewById(R.id.product_comparison_details_cv);
            productNameTextView = view.findViewById(R.id.product_comparison_name);
            productQuantityTextView = view.findViewById(R.id.product_comparison_quantity);
            productBrandTextView = view.findViewById(R.id.product_comparison_brand);
            productComparisonNutrientText = view.findViewById(R.id.product_comparison_textNutrientTxt);
            nutrientsRecyclerView = view.findViewById(R.id.product_comparison_listNutrientLevels);
            productComparisonNutrientCv = view.findViewById(R.id.product_comparison_nutrient_cv);
            productComparisonImage = view.findViewById(R.id.product_comparison_image);
            productComparisonLabel = view.findViewById(R.id.product_comparison_label);
            productComparisonImageGrade = view.findViewById(R.id.product_comparison_imageGrade);
            productComparisonNovaGroup = view.findViewById(R.id.product_comparison_nova_group);
            productComparisonAdditiveCv = view.findViewById(R.id.product_comparison_additive);
            productComparisonAdditiveText = view.findViewById(R.id.product_comparison_additive_text);
            fullProductButton = view.findViewById(R.id.full_product_button);
            fullProductButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fullscreen_blue_18dp, 0, 0, 0);
            productComparisonCo2Icon = view.findViewById(R.id.product_comparison_co2_icon);
            productComparisonScoresLayout = view.findViewById(R.id.product_comparison_scores_layout);
        }
    }

    //helper method
    private int dpsToPixel(int dps) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps + 100f, r.getDisplayMetrics());
    }
}
