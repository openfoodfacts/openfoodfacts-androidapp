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


import com.hkm.slider.Indicators.PagerIndicator;
import com.hkm.slider.SliderLayout;
import com.hkm.slider.SliderTypes.DefaultSliderView;
import com.hkm.slider.TransformerL;
import com.koushikdutta.ion.Ion;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.State;

/**
 * Created by scotscriven on 04/05/15.
 */
public class SummaryProductFragment extends Fragment {

    TextView nameProduct, barCodeProduct, quantityProduct, packagingProduct, brandProduct, manufacturingProduct,
            cityProduct, storeProduct, countryProduct, categoryProduct;
    SliderLayout sliderImages;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary_product,container,false);

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
        sliderImages = (SliderLayout) rootView.findViewById(R.id.slider);

        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

        DefaultSliderView sliderViewImageDefault;
        if(state.getProduct().getImageUrl() != null) {
            sliderViewImageDefault = new DefaultSliderView(getActivity());
            sliderViewImageDefault
                    .description("Default")
                    .image(state.getProduct().getImageUrl());
            sliderImages.addSlider(sliderViewImageDefault);
        }
        if(state.getProduct().getImageIngredientsUrl() != null) {
            sliderViewImageDefault = new DefaultSliderView(getActivity());
            sliderViewImageDefault
                    .description("Ingredients")
                    .image(state.getProduct().getImageIngredientsUrl());
            sliderImages.addSlider(sliderViewImageDefault);
        }
        if(state.getProduct().getImageNutritionUrl() != null) {
            sliderViewImageDefault = new DefaultSliderView(getActivity());
            sliderViewImageDefault
                    .description("Nutrition")
                    .image(state.getProduct().getImageNutritionUrl());

            sliderImages.addSlider(sliderViewImageDefault);
        }
        sliderImages.setCustomIndicator((PagerIndicator) rootView.findViewById(R.id.custom_indicator));
        sliderImages.setDuration(5000);
        sliderImages.startAutoCycle();

        nameProduct.setText(state.getProduct().getProductName());
        barCodeProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtBarcode) + "</b>" + ' ' + state.getProduct().getCode()));
        quantityProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtQuantity) + "</b>" + ' ' + state.getProduct().getQuantity()));
        packagingProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtPackaging) + "</b>" + ' ' + state.getProduct().getPackaging()));
        brandProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtBrands) + "</b>" + ' ' + state.getProduct().getBrands()));
        manufacturingProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtManufacturing) + "</b>" + ' ' + state.getProduct().getManufacturingPlaces()));
        String categ;
        if(state.getProduct().getCategories() == null){
            categ = state.getProduct().getCategories();
        }else{
            categ = state.getProduct().getCategories().replace(",",", ");
        }
        categoryProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtCategories) + "</b>" + ' ' + categ));
        cityProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtCity) + "</b>" + ' ' + state.getProduct().getCitiesTags().toString().replace("[","").replace("]","")));
        storeProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtStores) + "</b>" + ' ' + state.getProduct().getStores()));
        countryProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtCountries) + "</b>" + ' ' + state.getProduct().getCountries()));

        return rootView;
    }

    @Override
    public void onStop() {
        sliderImages.stopAutoCycle();
        super.onStop();
    }
}
