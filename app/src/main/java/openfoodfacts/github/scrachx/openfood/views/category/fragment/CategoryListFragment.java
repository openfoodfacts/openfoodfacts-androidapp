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
import android.view.ViewTreeObserver;
import android.widget.Toast;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.FastScroller;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.dagger.component.FragmentComponent;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentCategoryListBinding;
import openfoodfacts.github.scrachx.openfood.fragments.MvvmFragment;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_category_list, container, false);
        fastScroller = (FastScroller)rootView.findViewById(R.id.fast_scroller);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding = DataBindingUtil.bind(this.getView());
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        binding.setViewModel(getViewModel());
        fastScroller.setRecyclerView(binding.recycler);
        binding.recycler.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(binding.getViewModel().getCategories().get().isEmpty()){
                    fastScroller.setVisibility(View.GONE);
                }
                else {
                    fastScroller.setVisibility(View.VISIBLE);
                    // check for an empty item in the start of the list
                    if(viewModel.getCategories().get().get(0).getName().isEmpty()){
                        viewModel.getCategories().get().remove(0);
                        binding.recycler.getAdapter().notifyItemRemoved(0);
                        binding.recycler.getAdapter().notifyItemRangeChanged(0,binding.recycler.getAdapter().getItemCount());
                    }
                }
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
