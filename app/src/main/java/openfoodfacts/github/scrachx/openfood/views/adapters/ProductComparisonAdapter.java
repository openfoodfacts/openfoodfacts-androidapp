package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import static android.Manifest.permission.CAMERA;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.FRONT;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevels;
import openfoodfacts.github.scrachx.openfood.models.NutrimentLevel;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.WikidataApiClient;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.ImageUploadListener;
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import com.mikepenz.materialize.util.UIUtils;

import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductComparisonActivity;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.views.listeners.RecyclerItemClickListener;
import openfoodfacts.github.scrachx.openfood.views.product.ProductActivity;
import openfoodfacts.github.scrachx.openfood.views.product.summary.SummaryProductFragment;
import pl.aprilapps.easyphotopicker.EasyImage;

public class ProductComparisonAdapter extends RecyclerView.Adapter<ProductComparisonAdapter.ProductComparisonViewHolder> implements ImageUploadListener {

    private ArrayList<Product> productsToCompare;
    private Context context;
    private boolean isLowBatteryMode = false;
    private IProductRepository repository = ProductRepository.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();
    private String languageCode = Locale.getDefault().getLanguage();
    private Button addProductButton = null;
    private OpenFoodAPIClient api;
    private ArrayList<ProductComparisonViewHolder> viewHolders = new ArrayList<>();
    public static Integer ON_PHOTO_RETURN_POSITION;

    public static class ProductComparisonViewHolder extends RecyclerView.ViewHolder {
        public NestedScrollView listItemLayout;
        public CardView productComparisonDetailsCv;
        public TextView productNameTextView;
        public TextView productQuantityTextView;
        public TextView productBrandTextView;
        public TextView productComparisonNutrientText;
        public RecyclerView nutrientsRecyclerView;
        public CardView productComparisonNutrientCv;
        public ImageButton productComparisonImage;
        public TextView productComparisonLabel;
        public ImageView productComparisonImageGrade;
        public ImageView productComparisonNovaGroup;
        public CardView productComparisonAdditiveCv;
        public TextView productComparisonAdditiveText;
        public Button fullProductButton;
        public ImageView productComparisonCo2Icon;

        public ProductComparisonViewHolder(View view) {
            super(view);
            listItemLayout = (NestedScrollView) view.findViewById(R.id.product_comparison_list_item_layout);
            productComparisonDetailsCv = (CardView) view.findViewById(R.id.product_comparison_details_cv);
            productNameTextView = (TextView) view.findViewById(R.id.product_comparison_name);
            productQuantityTextView = (TextView) view.findViewById(R.id.product_comparison_quantity);
            productBrandTextView = (TextView) view.findViewById(R.id.product_comparison_brand);
            productComparisonNutrientText = (TextView) view.findViewById(R.id.product_comparison_textNutrientTxt);
            nutrientsRecyclerView = (RecyclerView) view.findViewById(R.id.product_comparison_listNutrientLevels);
            productComparisonNutrientCv = (CardView) view.findViewById(R.id.product_comparison_nutrient_cv);
            productComparisonImage = (ImageButton) view.findViewById(R.id.product_comparison_image);
            productComparisonLabel = (TextView) view.findViewById(R.id.product_comparison_label);
            productComparisonImageGrade = (ImageView) view.findViewById(R.id.product_comparison_imageGrade);
            productComparisonNovaGroup = (ImageView) view.findViewById(R.id.product_comparison_nova_group);
            productComparisonAdditiveCv = (CardView) view.findViewById(R.id.product_comparison_additive);
            productComparisonAdditiveText = (TextView) view.findViewById(R.id.product_comparison_additive_text);
            fullProductButton = (Button) view.findViewById(R.id.full_product_button);
            productComparisonCo2Icon = (ImageView) view.findViewById(R.id.product_comparison_co2_icon);
        }
    }

    public ProductComparisonAdapter(ArrayList<Product> productsToCompare, Context context) {
        this.productsToCompare = productsToCompare;
        this.context = context;
        this.addProductButton = ((Activity) context).findViewById(R.id.product_comparison_button);
        api = new OpenFoodAPIClient((Activity) context);
    }

    @NonNull
    @Override
    public ProductComparisonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.product_comparison_list_item, parent, false);
        ProductComparisonViewHolder viewHolder = new ProductComparisonViewHolder(v);
        viewHolders.add(viewHolder);
        return viewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ProductComparisonViewHolder holder, int position) {
        if (!productsToCompare.isEmpty()) {

            //support synchronous scrolling
            holder.listItemLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    for (ProductComparisonViewHolder viewHolder : viewHolders) {
                        viewHolder.listItemLayout.setScrollX(i);
                        viewHolder.listItemLayout.setScrollY(i1);
                    }
                }
            });

            Product product = productsToCompare.get(position);

            //set the visibility of UI components
            holder.productNameTextView.setVisibility(View.VISIBLE);
            holder.productQuantityTextView.setVisibility(View.VISIBLE);
            holder.productBrandTextView.setVisibility(View.VISIBLE);

            //Modify the text on the button for adding products
            if (this.addProductButton != null) {
                addProductButton.setText(R.string.add_another_product);
            }

            holder.productComparisonImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (product.getImageUrl() != null) {
                        Intent intent = new Intent(context, FullScreenImage.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("imageurl", product.getImageUrl());
                        intent.putExtras(bundle);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ActivityOptionsCompat options = ActivityOptionsCompat.
                                    makeSceneTransitionAnimation((Activity) context, (View) holder.productComparisonImage,
                                            ((Activity) context).getString(R.string.product_transition));
                            context.startActivity(intent, options.toBundle());
                        } else {
                            context.startActivity(intent);
                        }
                    } else {
                        // take a picture
                        if (ContextCompat.checkSelfPermission((Activity) context, CAMERA) != PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                        } else {
                            ON_PHOTO_RETURN_POSITION = position;
                            if (Utils.isHardwareCameraInstalled(context)) {
                                EasyImage.openCamera(((Activity) context), 0);
                            } else {
                                EasyImage.openGallery(((Activity) context), 0, false);
                            }
                        }
                    }
                }
            });

            if (isNotBlank(product.getImageUrl())) {
                holder.productComparisonLabel.setVisibility(View.INVISIBLE);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                Utils.DISABLE_IMAGE_LOAD = preferences.getBoolean("disableImageLoad", false);
                if (Utils.DISABLE_IMAGE_LOAD && Utils.getBatteryLevel(context)) {
                    isLowBatteryMode = true;
                }
                // Load Image if isLowBatteryMode is false
                if (!isLowBatteryMode) {
                    Picasso.with(context)
                            .load(product.getImageUrl())
                            .into(holder.productComparisonImage);
                } else {
                    holder.productComparisonImage.setVisibility(View.GONE);
                }
            }

            if (isNotBlank(product.getProductName())) {
                holder.productNameTextView.setText(product.getProductName());
            } else {
                //product name placeholder text goes here
            }

            if (isNotBlank(product.getQuantity())) {
                holder.productQuantityTextView.setText(bold("Quantity :"));
                holder.productQuantityTextView.append(' ' + product.getQuantity());
            } else {
                //product quantity placeholder goes here
            }

            if (isNotBlank(product.getBrands())) {
                holder.productBrandTextView.setText(bold("Brands :"));
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
            if (!(fat == null && salt == null && saturatedFat == null && sugars == null)) {

                if (product.getNutritionGradeFr() != null) {
                    holder.productComparisonImageGrade.setImageDrawable(ContextCompat.getDrawable(context, Utils.getImageGrade(product.getNutritionGradeFr())));
                } else {
                    holder.productComparisonImageGrade.setImageDrawable(null);
                }

                if (nutriments!=null) {
                    holder.nutrientsRecyclerView.setVisibility(View.VISIBLE);
                    holder.productComparisonNutrientText.setText(context.getString(R.string.txtNutrientLevel100g));
                    holder.nutrientsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                    holder.nutrientsRecyclerView.setAdapter(new NutrientLevelListAdapter(context, loadLevelItems(product)));
                }
            } else {
                holder.productComparisonImageGrade.setVisibility(View.INVISIBLE);
            }

            if (product.getNovaGroups() != null) {
                holder.productComparisonNovaGroup.setImageResource(Utils.getNovaGroupDrawable(product.getNovaGroups()));
            } else {
                holder.productComparisonNovaGroup.setVisibility(View.INVISIBLE);
            }

            if(product.getEnvironmentImpactLevelTags()!=null) {
                List<String> tags=product.getEnvironmentImpactLevelTags();
                String tag=tags.get(0).replace("\"","");
                holder.productComparisonCo2Icon.setVisibility(View.VISIBLE);
                if(tag.equals("en:high")){
                    holder.productComparisonCo2Icon.setImageResource(R.drawable.ic_co2_high_24dp);
                } else if(tag.equals("en:low")){
                    holder.productComparisonCo2Icon.setImageResource(R.drawable.ic_co2_low_24dp);
                } else if(tag.equals("en:medium")){
                    holder.productComparisonCo2Icon.setImageResource(R.drawable.ic_co2_medium_24dp);
                } else {
                    holder.productComparisonCo2Icon.setVisibility(View.GONE);
                }
            }

            List<String> additivesTags = product.getAdditivesTags();
            if (additivesTags != null && !additivesTags.isEmpty()) {
                loadAdditives(product, holder.productComparisonAdditiveText);
            }

            holder.fullProductButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (product != null) {
                        String barcode = product.getCode();
                        if (Utils.isNetworkConnected(context)) {
                            api.getProduct(barcode, (Activity) context);
                            try {
                                View view1 = ((Activity) context).getCurrentFocus();
                                if (view != null) {
                                    InputMethodManager imm = (InputMethodManager) ((Activity) context).getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        } else {
                            new MaterialDialog.Builder(context)
                                    .title(R.string.device_offline_dialog_title)
                                    .content(R.string.connectivity_check)
                                    .positiveText(R.string.txt_try_again)
                                    .negativeText(R.string.dismiss)
                                    .onPositive((dialog, which) -> {
                                        if (Utils.isNetworkConnected(context))
                                            api.getProduct(barcode, (Activity) context);
                                        else
                                            Toast.makeText(context, R.string.device_offline_dialog_title, Toast.LENGTH_SHORT).show();
                                    })
                                    .show();
                        }
                    }
                }
            });

        } else {
            holder.listItemLayout.setVisibility(View.GONE);
        }
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

        if (!(fat == null && salt == null && saturatedFat == null && sugars == null)) {

            if(nutriments!=null)
            {

                Nutriments.Nutriment fatNutriment = nutriments.get(Nutriments.FAT);
                if (fat != null && fatNutriment != null) {
                    String fatNutrimentLevel = fat.getLocalize(context);
                    levelItem.add(new NutrientLevelItem("Fat",fatNutriment.getDisplayStringFor100g(),
                            fatNutrimentLevel,
                            fat.getImageLevel()));
                }

                Nutriments.Nutriment saturatedFatNutriment = nutriments.get(Nutriments.SATURATED_FAT);
                if (saturatedFat != null && saturatedFatNutriment != null) {
                    String saturatedFatLocalize = saturatedFat.getLocalize(context);
                    levelItem.add(new NutrientLevelItem("Saturated fat",saturatedFatNutriment.getDisplayStringFor100g(),
                            saturatedFatLocalize,
                            saturatedFat.getImageLevel()));
                }

                Nutriments.Nutriment sugarsNutriment = nutriments.get(Nutriments.SUGARS);
                if (sugars != null && sugarsNutriment  != null) {
                    String sugarsLocalize = sugars.getLocalize(context);
                    levelItem.add(new NutrientLevelItem("Sugars",sugarsNutriment.getDisplayStringFor100g(),
                            sugarsLocalize,
                            sugars.getImageLevel()));
                }

                Nutriments.Nutriment saltNutriment = nutriments.get(Nutriments.SALT);
                if (salt != null && saltNutriment != null) {
                    String saltLocalize = salt.getLocalize(context);
                    levelItem.add(new NutrientLevelItem("Salt",saltNutriment.getDisplayStringFor100g(),
                            saltLocalize,
                            salt.getImageLevel()));
                }
            }

        }
        return levelItem;
    }

    private void loadAdditives(Product product, View v) {
        StringBuilder additivesBuilder = new StringBuilder();
        List<String> additivesTags = product.getAdditivesTags();
        if (additivesTags != null && !additivesTags.isEmpty()) {
            disposable.add(
                    Observable.fromArray(additivesTags.toArray(new String[additivesTags.size()]))
                            .flatMapSingle(tag -> repository.getAdditiveByTagAndLanguageCode(tag, languageCode)
                                    .flatMap(categoryName -> {
                                        if (categoryName.isNull()) {
                                            return repository.getAdditiveByTagAndDefaultLanguageCode(tag);
                                        } else {
                                            return Single.just(categoryName);
                                        }
                                    }))
                            .filter(additiveName -> additiveName.isNotNull())
                            .toList()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe(d -> {})
                            .subscribe(additives -> {
                                if (!additives.isEmpty()) {
                                    additivesBuilder.append(bold("Additives :"));
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
                            }, e -> {
                                e.printStackTrace();
                            })
            );
        }
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
        for (ProductComparisonViewHolder current: viewHolders) {
            current.productComparisonDetailsCv.setMinimumHeight(Collections.max(productDetailsHeight));
            current.productComparisonNutrientCv.setMinimumHeight(Collections.max(productNutrientsHeight));
            current.productComparisonAdditiveText.setHeight(dpsToPixel(Collections.max(productAdditivesHeight)));
        }
    }

    public void setImageOnPhotoReturn(File file) {
        ProductComparisonViewHolder holder = viewHolders.get(ON_PHOTO_RETURN_POSITION);
        Product product = productsToCompare.get(ON_PHOTO_RETURN_POSITION);
        ProductImage image = new ProductImage(product.getCode(), FRONT, file);
        image.setFilePath(file.getAbsolutePath());
        api.postImg(context, image, this);
        String mUrlImage = file.getAbsolutePath();
        product.setImageUrl(mUrlImage);
        ON_PHOTO_RETURN_POSITION = null;
        notifyDataSetChanged();
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure(String message) {

    }

    //helper method
    private int dpsToPixel(int dps) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dps+100,
                r.getDisplayMetrics()
        );
        return (int) px;
    }
}
