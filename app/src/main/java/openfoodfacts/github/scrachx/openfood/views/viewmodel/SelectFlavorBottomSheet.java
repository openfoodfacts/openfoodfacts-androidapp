package openfoodfacts.github.scrachx.openfood.views.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.views.ContinuousScanActivity;

public class SelectFlavorBottomSheet extends BottomSheetDialogFragment {

    RelativeLayout rlFoodFact;
    RelativeLayout rlBeautyFact;
    RelativeLayout rlPetFact;
    RelativeLayout rlOpenProductsFact;

    private Context context;
    private View view;
    private productTypeListener productTypeListener;

    private int OPEN_FOOD_FACT = 5;
    private int OPEN_BEAUTY_FACTS = 6;
    private int OPEN_PET_FOOD_FACTS = 7;
    private int OPEN_PRODUCTS_FACTS = 8;

    public SelectFlavorBottomSheet() {
    }


    public interface productTypeListener {
        void chosenProductType(int productType);
    }

    public void setProductTypeListener(productTypeListener productTypeListener){
        this.productTypeListener = productTypeListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_select_flavor_bottom_sheet, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initComponents();
    }

    private void initComponents(){

        rlFoodFact = view.findViewById(R.id.rlFoodFact);
        rlBeautyFact = view.findViewById(R.id.rlBeautyFact);
        rlPetFact = view.findViewById(R.id.rlPetFact);
        rlOpenProductsFact = view.findViewById(R.id.rlOpenProductsFact);


        rlFoodFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productTypeListener.chosenProductType(OPEN_FOOD_FACT);
                dismiss();
            }
        });

        rlBeautyFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productTypeListener.chosenProductType(OPEN_BEAUTY_FACTS);
                dismiss();
            }
        });

        rlPetFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productTypeListener.chosenProductType(OPEN_PET_FOOD_FACTS);
                dismiss();
            }
        });

        rlOpenProductsFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productTypeListener.chosenProductType(OPEN_PRODUCTS_FACTS);
                dismiss();
            }
        });
    }
}
