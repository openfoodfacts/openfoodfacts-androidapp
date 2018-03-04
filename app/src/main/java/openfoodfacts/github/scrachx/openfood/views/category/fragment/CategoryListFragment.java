package openfoodfacts.github.scrachx.openfood.views.category.fragment;


import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
<<<<<<< HEAD
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Delayed;
=======
>>>>>>> 8c4b7829eb6fc0c80686a0ff3623b6b84c2887e1

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.FastScroller;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.category.mapper.CategoryMapper;
import openfoodfacts.github.scrachx.openfood.dagger.component.FragmentComponent;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentCategoryListBinding;
import openfoodfacts.github.scrachx.openfood.fragments.MvvmFragment;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.category.adapter.CategoryListRecyclerAdapter;
import openfoodfacts.github.scrachx.openfood.views.viewmodel.category.CategoryFragmentViewModel;

<<<<<<< HEAD
public class CategoryListFragment extends MvvmFragment<CategoryFragmentViewModel, FragmentComponent>  {
    ProgressBar progressBar;
    TextView emptyView;
    TextView loadText;
=======
public class CategoryListFragment extends MvvmFragment<CategoryFragmentViewModel, FragmentComponent> {

    FastScroller fastScroller;

>>>>>>> 8c4b7829eb6fc0c80686a0ff3623b6b84c2887e1
    @Inject
    CategoryFragmentViewModel viewModel;
    private FragmentCategoryListBinding binding;

    public CategoryListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
<<<<<<< HEAD

        View rootView =  inflater.inflate(R.layout.fragment_category_list, container, false);
        progressBar = (ProgressBar)rootView.findViewById(R.id.prog_bar);
        emptyView = (TextView)rootView.findViewById(R.id.empty_view);
        loadText = (TextView)rootView.findViewById(R.id.load_text);
        return  rootView;
=======
        View rootView =  inflater.inflate(R.layout.fragment_category_list, container, false);
        fastScroller = (FastScroller)rootView.findViewById(R.id.fast_scroller);
        return rootView;
>>>>>>> 8c4b7829eb6fc0c80686a0ff3623b6b84c2887e1
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding = DataBindingUtil.bind(this.getView());
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        binding.setViewModel(getViewModel());
<<<<<<< HEAD
        binding.recycler.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                RunCheck();
            }
        });


=======
        fastScroller.setRecyclerView(binding.recycler);
        binding.recycler.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(binding.getViewModel().getCategories().get().isEmpty()){
                    fastScroller.setVisibility(View.GONE);

                }
                else {
                    fastScroller.setVisibility(View.VISIBLE);
                }
            }
        });
>>>>>>> 8c4b7829eb6fc0c80686a0ff3623b6b84c2887e1
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

    public void RunCheck(){
      if (viewModel.getCategories().get().isEmpty()){
            binding.recycler.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            loadText.setVisibility(View.GONE);
        }
        else {
            emptyView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            binding.recycler.setVisibility(View.VISIBLE);
            loadText.setVisibility(View.VISIBLE);
        }
    }

}
