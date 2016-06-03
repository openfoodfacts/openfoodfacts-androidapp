package openbeautyfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hkm.slider.Animations.DescriptionAnimation;
import com.hkm.slider.Indicators.PagerIndicator;
import com.hkm.slider.SliderLayout;
import com.hkm.slider.SliderTypes.AdjustableSlide;
import com.hkm.slider.SliderTypes.BaseSliderView;
import com.hkm.slider.SliderTypes.DefaultSliderView;

import java.util.ArrayList;

import butterknife.Bind;
import openbeautyfacts.github.scrachx.openfood.R;
import openbeautyfacts.github.scrachx.openfood.models.State;

public class SummaryProductFragment extends BaseFragment {

    @Bind(R.id.textNameProduct) TextView nameProduct;
    @Bind(R.id.textBarcodeProduct) TextView barCodeProduct;
    @Bind(R.id.textQuantityProduct) TextView quantityProduct;
    @Bind(R.id.textPackagingProduct) TextView packagingProduct;
    @Bind(R.id.textBrandProduct) TextView brandProduct;
    @Bind(R.id.textManufacturingProduct) TextView manufacturingProduct;
    @Bind(R.id.textCityProduct) TextView cityProduct;
    @Bind(R.id.textStoreProduct) TextView storeProduct;
    @Bind(R.id.textCountryProduct) TextView countryProduct;
    @Bind(R.id.textCategoryProduct) TextView categoryProduct;
    @Bind(R.id.slider) SliderLayout sliderImages;
    @Bind(R.id.custom_indicator) PagerIndicator pagerIndicator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_summary_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

        ArrayList<String> urlsImages = new ArrayList<>();
        if (state.getProduct().getImageUrl() != null) {
            urlsImages.add(state.getProduct().getImageUrl());
        }
        if (state.getProduct().getImageIngredientsUrl() != null) {
            urlsImages.add(state.getProduct().getImageIngredientsUrl());
        }
        if (state.getProduct().getImageNutritionUrl() != null) {
            urlsImages.add(state.getProduct().getImageNutritionUrl());
        }
        ArrayList<AdjustableSlide> list = new ArrayList<>();
        for (int h = 0; h < urlsImages.size(); h++) {
            AdjustableSlide textSliderView = new AdjustableSlide(view.getContext());
            textSliderView
                    .image(urlsImages.get(h))
                    .setScaleType(BaseSliderView.ScaleType.FitCenterCrop);
            list.add(textSliderView);
        }
        sliderImages.loadSliderList(list);
        sliderImages.setCustomAnimation(new DescriptionAnimation());
        sliderImages.setSliderTransformDuration(1000, new LinearOutSlowInInterpolator());
        sliderImages.setCustomIndicator(pagerIndicator);
        sliderImages.setDuration(5500);
        sliderImages.startAutoCycle();

        nameProduct.setText(state.getProduct().getProductName());
        barCodeProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtBarcode) + "</b>" + ' ' + state.getProduct().getCode()));
        quantityProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtQuantity) + "</b>" + ' ' + state.getProduct().getQuantity()));
        packagingProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtPackaging) + "</b>" + ' ' + state.getProduct().getPackaging()));
        brandProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtBrands) + "</b>" + ' ' + state.getProduct().getBrands()));
        manufacturingProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtManufacturing) + "</b>" + ' ' + state.getProduct().getManufacturingPlaces()));
        String categ;
        if (state.getProduct().getCategories() == null) {
            categ = state.getProduct().getCategories();
        } else {
            categ = state.getProduct().getCategories().replace(",", ", ");
        }
        categoryProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtCategories) + "</b>" + ' ' + categ));
        cityProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtCity) + "</b>" + ' ' + state.getProduct().getCitiesTags().toString().replace("[", "").replace("]", "")));
        storeProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtStores) + "</b>" + ' ' + state.getProduct().getStores()));
        countryProduct.setText(Html.fromHtml("<b>" + getString(R.string.txtCountries) + "</b>" + ' ' + state.getProduct().getCountries()));
    }

    @Override
    public void onStop() {
        sliderImages.stopAutoCycle();
        super.onStop();
    }
}
