package openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis.adapter.IngredientAnalysisRecyclerAdapter;

public class IngredientsAnalysisProductFragment extends BaseFragment {
    @BindView(R.id.ingredient_analysis_recycler_view)
    RecyclerView mRecyclerView;

    private OpenFoodAPIClient api;
    private Product product;
    private IngredientAnalysisRecyclerAdapter adapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");
        product = state.getProduct();
        api = new OpenFoodAPIClient(getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mRecyclerView=new RecyclerView(getContext());
        api.getIngredients(product.getCode(),((value, ingredients) -> {
            if(value) {
                adapter= new IngredientAnalysisRecyclerAdapter(getContext(),ingredients,getActivity());
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(getActivity(), getActivity().getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
            }

        }));
        return createView(inflater,container,R.layout.fragment_ingredients_analysis_product);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        refreshView((State) intent.getExtras().getSerializable("state"));
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);
        this.product = state.getProduct();

        if(adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }
}
