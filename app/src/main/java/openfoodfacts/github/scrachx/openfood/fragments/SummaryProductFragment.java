package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hkm.slider.Indicators.PagerIndicator;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class SummaryProductFragment extends BaseFragment {

    @BindView(R.id.textNameProduct) TextView nameProduct;
    @BindView(R.id.textBarcodeProduct) TextView barCodeProduct;
    @BindView(R.id.textQuantityProduct) TextView quantityProduct;
    @BindView(R.id.textPackagingProduct) TextView packagingProduct;
    @BindView(R.id.textBrandProduct) TextView brandProduct;
    @BindView(R.id.textManufacturingProduct) TextView manufacturingProduct;
    @BindView(R.id.textIngredientsOriginProduct) TextView ingredientsOrigin;
    @BindView(R.id.textCityProduct) TextView cityProduct;
    @BindView(R.id.textStoreProduct) TextView storeProduct;
    @BindView(R.id.textCountryProduct) TextView countryProduct;
    @BindView(R.id.textCategoryProduct) TextView categoryProduct;
    @BindView(R.id.textLabelProduct) TextView labelProduct;
    @BindView(R.id.custom_indicator) PagerIndicator pagerIndicator;
    @BindView(R.id.imageViewNutritionFullSum) ImageView mImageNutritionFullSum;

    private String mUrlImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_summary_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        final State state = (State) intent.getExtras().getSerializable("state");

        final Product product = state.getProduct();

        if (isNotEmpty(product.getImageUrl())) {
            Picasso.with(view.getContext())
                    .load(product.getImageUrl())
                    .into(mImageNutritionFullSum);
            mUrlImage = product.getImageUrl();
        }

        //TODO use OpenFoodApiService to fetch product by packaging, brands, categories etc

        if(product.getProductName() != null && !product.getProductName().trim().isEmpty()) {
            nameProduct.setText(product.getProductName());
        } else {
            nameProduct.setVisibility(View.GONE);
        }
        if(product.getCode() != null && !product.getCode().trim().isEmpty()) {
            barCodeProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtBarcode) + "</b>" + ' ' + product.getCode()));
        } else {
            barCodeProduct.setVisibility(View.GONE);
        }
        if(product.getQuantity() != null && !product.getQuantity().trim().isEmpty()) {
            quantityProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtQuantity) + "</b>" + ' ' + product.getQuantity()));
        } else {
            quantityProduct.setVisibility(View.GONE);
        }
        if(product.getPackaging() != null && !product.getPackaging().trim().isEmpty()) {
            packagingProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtPackaging) + "</b>" + ' ' + product.getPackaging()));
        } else {
            packagingProduct.setVisibility(View.GONE);
        }
        if(product.getBrands() != null && !product.getBrands().trim().isEmpty()) {
            brandProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtBrands) + "</b>" + ' ' + product.getBrands()));
        } else {
            brandProduct.setVisibility(View.GONE);
        }
        if(product.getManufacturingPlaces() != null && !product.getManufacturingPlaces().trim().isEmpty()) {
            manufacturingProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtManufacturing) + "</b>" + ' ' + product.getManufacturingPlaces()));
        } else {
            manufacturingProduct.setVisibility(View.GONE);
        }

        if (product.getOrigins() == null) {
            ingredientsOrigin.setVisibility(View.GONE);
        } else {
            ingredientsOrigin.setText(Html.fromHtml("<b>" + getString(R.string.txtIngredientsOrigins) + "</b>" + ' ' + product.getOrigins()));
        }

        String categ;
        if (product.getCategories() != null && !product.getCategories().trim().isEmpty()) {
            categ = product.getCategories().replace(",", ", ");
            categoryProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtCategories) + "</b>" + ' ' + categ));
        } else {
            categoryProduct.setVisibility(View.GONE);
        }

        String labels = product.getLabels();
        if (isNotEmpty(labels)) {
            labelProduct.append(bold(getString(R.string.txtLabels)));
            labelProduct.append(" ");
            for (String label : labels.split(",")) {
                labelProduct.append(label.trim());
                labelProduct.append(", ");
            }
        } else {
            labelProduct.setVisibility(View.GONE);
        }

        if(product.getCitiesTags() != null && !product.getCitiesTags().toString().trim().equals("[]")) {
            cityProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtCity) + "</b>" + ' ' + product.getCitiesTags().toString().replace("[", "").replace("]", "")));
        } else {
            cityProduct.setVisibility(View.GONE);
        }
        if(product.getStores() != null && !product.getStores().trim().isEmpty()) {
            storeProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtStores) + "</b>" + ' ' + product.getStores()));
        } else {
            storeProduct.setVisibility(View.GONE);
        }
        if(product.getCountries() != null && !product.getCountries().trim().isEmpty()) {
            countryProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtCountries) + "</b>" + ' ' + product.getCountries()));
        } else {
            countryProduct.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.imageViewNutritionFullSum)
    public void openFullScreen(View v) {
        Intent intent = new Intent(v.getContext(), FullScreenImage.class);
        Bundle bundle = new Bundle();
        bundle.putString("imageurl", mUrlImage);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
