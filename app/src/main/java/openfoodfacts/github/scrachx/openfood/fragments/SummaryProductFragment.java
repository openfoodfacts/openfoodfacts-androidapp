package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
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

        if(state.getProduct().getImageUrl().isEmpty()){
            imgUrl = state.getProduct().getImageSmallUrl();
        }else{
            imgUrl = state.getProduct().getImageUrl();
        }
        setImageView(imgUrl);
        nameProduct.setText(state.getProduct().getProductName());
        barCodeProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtBarcode) + "</b>" + ' ' + state.getProduct().getCode()));
        quantityProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtQuantity) + "</b>" + ' ' + state.getProduct().getQuantity()));
        packagingProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtPackaging) + "</b>" + ' ' + state.getProduct().getPackaging()));
        brandProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtBrands) + "</b>" + ' ' + state.getProduct().getBrands()));
        manufacturingProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtManufacturing) + "</b>" + ' ' + state.getProduct().getManufacturingPlaces()));
        categoryProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtCategories) + "</b>" + ' ' + state.getProduct().getCategories().replace(",",", ")));
        cityProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtCity) + "</b>" + ' ' + state.getProduct().getCitiesTags().toString().replace("[","").replace("]","")));
        storeProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtStores) + "</b>" + ' ' + state.getProduct().getStores()));
        countryProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtCountries) + "</b>" + ' ' + state.getProduct().getCountries()));

        return rootView;
    }

    public void setImageView(String imgUrl){
        Ion.with(imgProduct)
                .placeholder(R.drawable.placeholder_thumb)
                .error(R.drawable.error_image)
                .resizeWidth(600)
                .load(imgUrl);
    }
}
