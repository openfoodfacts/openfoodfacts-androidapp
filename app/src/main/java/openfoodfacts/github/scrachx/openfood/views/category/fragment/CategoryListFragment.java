package openfoodfacts.github.scrachx.openfood.views.category.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import javax.inject.Inject;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.FastScroller;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.dagger.component.FragmentComponent;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentCategoryListBinding;
import openfoodfacts.github.scrachx.openfood.fragments.MvvmFragment;
import openfoodfacts.github.scrachx.openfood.utils.SearchSuggestionProvider;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.viewmodel.category.CategoryFragmentViewModel;

public class CategoryListFragment extends MvvmFragment<CategoryFragmentViewModel, FragmentComponent> {
    FastScroller fastScroller;
    @Inject
    CategoryFragmentViewModel viewModel;
    private FragmentCategoryListBinding binding;

    public CategoryListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryListBinding.inflate(getLayoutInflater(), container, false);
        fastScroller = binding.fastScroller;
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
        binding.setViewModel(getViewModel());
        fastScroller.setRecyclerView(binding.recycler);
        binding.recycler.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (binding.getViewModel().getFilteredCategories().get().isEmpty()) {
                fastScroller.setVisibility(View.GONE);
            } else {
                fastScroller.setVisibility(View.VISIBLE);
                // check for an empty item in the start of the list
                if (viewModel.getFilteredCategories().get().get(0).getName().isEmpty()) {
                    viewModel.getFilteredCategories().get().remove(0);
                    binding.recycler.getAdapter().notifyItemRemoved(0);
                    binding.recycler.getAdapter().notifyItemRangeChanged(0, binding.recycler.getAdapter().getItemCount());
                }
            }
        });
        binding.offlineView.findViewById(R.id.buttonToRefresh).setOnClickListener(v -> viewModel.loadCategories());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint("Search for a food category");
        if (searchManager.getSearchableInfo(requireActivity().getComponentName()) == null) {
            return;
        }
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getContext(),
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                suggestions.saveRecentQuery(query, null);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.searchCategories(newText.toLowerCase());
                return false;
            }
        });
    }

    @Override
    protected CategoryFragmentViewModel getViewModel() {
        return viewModel;
    }

    @NonNull
    @Override
    protected FragmentComponent createComponent() {
        return ((BaseActivity) requireActivity()).getActivityComponent().plusFragmentComponent();
    }

    @Override
    protected void inject() {
        component().inject(this);
    }

    @Override
    protected void bindProperties(CompositeDisposable compositeDisposable) {
        // Not used here
    }
}
