package openfoodfacts.github.scrachx.openfood.views.category.fragment;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.dagger.component.FragmentComponent;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentCategoryListBinding;
import openfoodfacts.github.scrachx.openfood.fragments.MvvmFragment;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.viewmodel.category.CategoryFragmentViewModel;

public class CategoryListFragment extends MvvmFragment<CategoryFragmentViewModel, FragmentComponent> {

    @Inject
    CategoryFragmentViewModel viewModel;

    private FragmentCategoryListBinding binding;

    public CategoryListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding = DataBindingUtil.bind(this.getView());

        binding.recycler.setHasFixedSize(true);
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        binding.setViewModel(getViewModel());
    }

    @Override
    protected CategoryFragmentViewModel getViewModel() {
        return viewModel;
    }

    @NonNull
    @Override
    protected FragmentComponent createComponent() {
        return ((BaseActivity)getActivity()).getActivityComponent().plusFragmentComponent();
    }

    @Override
    protected void inject() {
        component().inject(this);
    }

    @Override
    protected void bindProperties(CompositeDisposable compositeDisposable) {
        //Not used here
    }
}
