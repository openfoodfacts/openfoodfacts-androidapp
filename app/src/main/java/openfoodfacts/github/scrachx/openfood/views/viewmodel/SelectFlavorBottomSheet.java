package openfoodfacts.github.scrachx.openfood.views.viewmodel;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import openfoodfacts.github.scrachx.openfood.R;

public class SelectFlavorBottomSheet extends BottomSheetDialogFragment {

    RelativeLayout rlFoodFacts;
    RelativeLayout rlBeautyFacts;
    RelativeLayout rlPetFacts;
    RelativeLayout rlOpenProductsFacts;

    private Context context;
    private View view;
    private productTypeListener productTypeListener;

    private int OPEN_FOOD_FACTS = 5;
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

        rlFoodFacts = view.findViewById(R.id.rlFoodFact);
        rlBeautyFacts = view.findViewById(R.id.rlBeautyFact);
        rlPetFacts = view.findViewById(R.id.rlPetFact);
        rlOpenProductsFacts = view.findViewById(R.id.rlOpenProductsFact);


        rlFoodFacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productTypeListener.chosenProductType(OPEN_FOOD_FACTS);
                dismiss();
            }
        });

        rlBeautyFacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productTypeListener.chosenProductType(OPEN_BEAUTY_FACTS);
                dismiss();
            }
        });

        rlPetFacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productTypeListener.chosenProductType(OPEN_PET_FOOD_FACTS);
                dismiss();
            }
        });

        rlOpenProductsFacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productTypeListener.chosenProductType(OPEN_PRODUCTS_FACTS);
                dismiss();
            }
        });
    }
}
