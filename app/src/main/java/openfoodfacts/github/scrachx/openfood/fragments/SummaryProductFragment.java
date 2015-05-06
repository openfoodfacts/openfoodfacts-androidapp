package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.State;

/**
 * Created by scotscriven on 04/05/15.
 */
public class SummaryProductFragment extends Fragment {

    ImageView imgProduct;
    TextView nameProduct, barCodeProduct, quantityProduct, packagingProduct, brandProduct, manufacturingProduct,
            cityProduct, storeProduct, countryProduct, categoryProduct;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary_product,container,false);

        String imgUrl;

        imgProduct = (ImageView) rootView.findViewById(R.id.imageViewProduct);
        nameProduct = (TextView) rootView.findViewById(R.id.textNameProduct);
        barCodeProduct = (TextView) rootView.findViewById(R.id.textBarcodeProduct);
        quantityProduct = (TextView) rootView.findViewById(R.id.textQuantityProduct);
        packagingProduct = (TextView) rootView.findViewById(R.id.textPackagingProduct);
        brandProduct = (TextView) rootView.findViewById(R.id.textBrandProduct);
        manufacturingProduct = (TextView) rootView.findViewById(R.id.textManufacturingProduct);
        categoryProduct = (TextView) rootView.findViewById(R.id.textCategoryProduct);
        cityProduct = (TextView) rootView.findViewById(R.id.textCityProduct);
        storeProduct = (TextView) rootView.findViewById(R.id.textStoreProduct);
        countryProduct = (TextView) rootView.findViewById(R.id.textCountryProduct);

        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

        if(state.getProduct().getImageUrl() == null){
            imgUrl = state.getProduct().getImageSmallUrl();
        }else{
            imgUrl = state.getProduct().getImageUrl();
        }
        setImageView(imgUrl);
        nameProduct.setText(state.getProduct().getProductName());
        barCodeProduct.setText(getString(R.string.txtBarcode) + ' ' + state.getProduct().getCode());
        quantityProduct.setText(getString(R.string.txtQuantity) + ' ' + state.getProduct().getQuantity());
        packagingProduct.setText(getString(R.string.txtPackaging) + ' ' + state.getProduct().getPackaging());
        brandProduct.setText(getString(R.string.txtBrands) + ' ' + state.getProduct().getBrands());
        manufacturingProduct.setText(getString(R.string.txtManufacturing) + ' ' + state.getProduct().getManufacturingPlaces());
        categoryProduct.setText(getString(R.string.txtCategories) + ' ' + state.getProduct().getCategories());
        cityProduct.setText(getString(R.string.txtCity) + ' ' + state.getProduct().getCitiesTags().toString());
        storeProduct.setText(getString(R.string.txtStores) + ' ' + state.getProduct().getStores());
        countryProduct.setText(getString(R.string.txtCountries) + ' ' + state.getProduct().getCountries());

        return rootView;
    }

    public void setImageView(String imgUrl){
        Ion.with(imgProduct)
                .placeholder(R.drawable.placeholder_thumb)
                .error(R.drawable.error_image)
                .load(imgUrl);
    }
}
