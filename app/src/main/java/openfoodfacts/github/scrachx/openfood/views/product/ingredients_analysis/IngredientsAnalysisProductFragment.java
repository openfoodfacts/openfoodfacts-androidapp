package openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentIngredientsAnalysisProductBinding;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis.adapter.IngredientAnalysisRecyclerAdapter;

public class IngredientsAnalysisProductFragment extends BaseFragment {
    private FragmentIngredientsAnalysisProductBinding binding;
    private OpenFoodAPIClient api;
    private Product product;
    private IngredientAnalysisRecyclerAdapter adapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        State state = FragmentUtils.getStateFromActivityIntent();
        product = state.getProduct();
        api = new OpenFoodAPIClient(requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentIngredientsAnalysisProductBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        api.getIngredients(product.getCode(), ((value, ingredients) -> {
            if (value) {
                adapter = new IngredientAnalysisRecyclerAdapter(ingredients, requireActivity());
                binding.ingredientAnalysisRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
                binding.ingredientAnalysisRecyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(getActivity(), getActivity().getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
            }
        }));

        Intent intent = requireActivity().getIntent();
        if (intent != null && intent.getExtras() != null) {
            refreshView((State) intent.getExtras().getSerializable("state"));
        }
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);
        this.product = state.getProduct();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
