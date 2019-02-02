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

public class SelectFlavorBottomSheet extends BottomSheetDialogFragment {

    RelativeLayout rlFoodFact;
    RelativeLayout rlBeautyFact;
    RelativeLayout rlPetFact;
    RelativeLayout rlOpenProductsFact;

    private Context context;
    private View view;
    private Activity activity;

    public SelectFlavorBottomSheet() {
    }

    public void setParentActivity(Activity activity){
        this.activity = activity;
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
        // Inflate the layout for this fragment

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
                Toast.makeText(context, "Food Clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("categorySelected", "food");
                activity.setResult(26, intent);
            }
        });

        rlBeautyFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Beauty Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        rlPetFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Pet Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        rlOpenProductsFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Others Clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
