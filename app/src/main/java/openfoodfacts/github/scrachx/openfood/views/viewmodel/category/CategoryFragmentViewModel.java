package openfoodfacts.github.scrachx.openfood.views.viewmodel.category;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.category.CategoryRepository;
import openfoodfacts.github.scrachx.openfood.category.model.Category;
import openfoodfacts.github.scrachx.openfood.views.viewmodel.ViewModel;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */

public class CategoryFragmentViewModel extends ViewModel {
    private final CategoryRepository repository;
    private final ObservableField<List<Category>> categories;
    private final ObservableInt showProgress;

    public CategoryFragmentViewModel(CategoryRepository repository) {
        this.repository = repository;
        this.categories = new ObservableField<>(Collections.emptyList());
        this.showProgress = new ObservableInt(View.VISIBLE);
    }

    public ObservableField<List<Category>> getCategories() {
        return categories;
    }

    public ObservableInt getShowProgress() {
        return showProgress;
    }

    @Override
    protected void subscribe(@NonNull CompositeDisposable subscriptions) {
        subscriptions.add(repository.retrieveAll().subscribe(categoryList -> {
                    categories.set(categoryList);
                    showProgress.set(View.GONE);
                },
                throwable -> Log.e(CategoryFragmentViewModel.class.getCanonicalName(), "Error loading categories", throwable)));
    }
}
