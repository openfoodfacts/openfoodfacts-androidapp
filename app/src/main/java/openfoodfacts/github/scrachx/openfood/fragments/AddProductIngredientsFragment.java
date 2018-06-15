package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;

public class AddProductIngredientsFragment extends BaseFragment {

    private static final String PARAM_INGREDIENTS = "ingredients_text";
    private static final String PARAM_TRACES = "traces";
    @BindView(R.id.ingredients_list)
    EditText ingredients;
    @BindView(R.id.traces)
    EditText traces;
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product_ingredients, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @OnClick(R.id.btn_next)
    void next() {
        Activity activity = getActivity();
        if (activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).proceed();
        }
    }

    public void getDetails() {
        if (activity instanceof AddProductActivity) {
            if (!ingredients.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_INGREDIENTS, ingredients.getText().toString());
            }
            if (!traces.getText().toString().isEmpty()) {
                ((AddProductActivity) activity).addToMap(PARAM_TRACES, traces.getText().toString());
            }
        }
    }
}
