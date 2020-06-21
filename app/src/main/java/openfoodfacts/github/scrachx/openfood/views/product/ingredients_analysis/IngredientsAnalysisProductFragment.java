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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentIngredientsAnalysisProductBinding;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.FragmentUtils;
import openfoodfacts.github.scrachx.openfood.views.product.ProductActivity;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis.adapter.IngredientAnalysisRecyclerAdapter;

public class IngredientsAnalysisProductFragment extends BaseFragment {
    private FragmentIngredientsAnalysisProductBinding binding;
    private OpenFoodAPIClient api;
    private Product product;
    private IngredientAnalysisRecyclerAdapter adapter;
    private final CompositeDisposable disp = new CompositeDisposable();

    @Override
    public void onDestroy() {
        disp.dispose();
        binding = null;
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        State state = FragmentUtils.requireStateFromArguments(this);
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

        disp.add(api.getIngredients(product.getCode()).observeOn(AndroidSchedulers.mainThread()).subscribe(
            ingredients -> {
                adapter = new IngredientAnalysisRecyclerAdapter(ingredients, requireActivity());
                binding.ingredientAnalysisRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
                binding.ingredientAnalysisRecyclerView.setAdapter(adapter);
            },
            throwable -> Toast.makeText(getActivity(), requireActivity().getString(R.string.errorWeb), Toast.LENGTH_LONG).show())
        );

        Intent intent = requireActivity().getIntent();
        if (intent != null && intent.getExtras() != null) {
            refreshView((State) intent.getExtras().getSerializable(ProductActivity.STATE_KEY));
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
