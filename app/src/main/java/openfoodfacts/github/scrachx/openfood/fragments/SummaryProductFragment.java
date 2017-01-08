package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
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
            barCodeProduct.setText(Utils.bold(getString(R.string.txtBarcode)));
            barCodeProduct.append(' ' + product.getCode());
        } else {
            barCodeProduct.setVisibility(View.GONE);
        }
        if(product.getQuantity() != null && !product.getQuantity().trim().isEmpty()) {
            quantityProduct.setText(Utils.bold(getString(R.string.txtQuantity)));
            quantityProduct.append(' ' + product.getQuantity());
        } else {
            quantityProduct.setVisibility(View.GONE);
        }
        if(product.getPackaging() != null && !product.getPackaging().trim().isEmpty()) {
            packagingProduct.setText(Utils.bold(getString(R.string.txtPackaging)));
            packagingProduct.append(' ' + product.getPackaging());
        } else {
            packagingProduct.setVisibility(View.GONE);
        }
        if(product.getBrands() != null && !product.getBrands().trim().isEmpty()) {
            brandProduct.setText(Utils.bold(getString(R.string.txtBrands)));
            brandProduct.append(' ' + product.getBrands());
        } else {
            brandProduct.setVisibility(View.GONE);
        }
        if(product.getManufacturingPlaces() != null && !product.getManufacturingPlaces().trim().isEmpty()) {
            manufacturingProduct.setText(Utils.bold(getString(R.string.txtManufacturing)));
            manufacturingProduct.append(' ' + product.getManufacturingPlaces());
        } else {
            manufacturingProduct.setVisibility(View.GONE);
        }

        if (product.getOrigins() == null) {
            ingredientsOrigin.setVisibility(View.GONE);
        } else {
            ingredientsOrigin.setText(Utils.bold(getString(R.string.txtIngredientsOrigins)));
            ingredientsOrigin.append(' ' + product.getOrigins());
        }

        String categ;
        if (product.getCategories() != null && !product.getCategories().trim().isEmpty()) {
            categ = product.getCategories().replace(",", ", ");
            categoryProduct.setText(Utils.bold(getString(R.string.txtCategories)));
            categoryProduct.append(' ' + categ);
        } else {
            categoryProduct.setVisibility(View.GONE);
        }

        String labels = product.getLabels();
        if (isNotBlank(labels)) {
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
            cityProduct.setText(Utils.bold(getString(R.string.txtCity)));
            cityProduct.append(' ' + product.getCitiesTags().toString().replace("[", "").replace("]", ""));
        } else {
            cityProduct.setVisibility(View.GONE);
        }
        if(product.getStores() != null && !product.getStores().trim().isEmpty()) {
            storeProduct.setText(Utils.bold(getString(R.string.txtStores)));
            storeProduct.append(' ' + product.getStores());
        } else {
            storeProduct.setVisibility(View.GONE);
        }
        if(product.getCountries() != null && !product.getCountries().trim().isEmpty()) {
            countryProduct.setText(Utils.bold(getString(R.string.txtCountries)));
            countryProduct.append(' ' + product.getCountries());
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
