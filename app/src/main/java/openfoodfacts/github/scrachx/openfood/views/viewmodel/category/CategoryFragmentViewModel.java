package openfoodfacts.github.scrachx.openfood.views.viewmodel.category;

import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.util.Log;

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

    public CategoryFragmentViewModel(CategoryRepository repository) {
        this.repository = repository;
        this.categories = new ObservableField<>(Collections.emptyList());
    }

    public ObservableField<List<Category>> getCategories() {
        return categories;
    }

    @Override
    protected void subscribe(@NonNull CompositeDisposable subscriptions) {
        subscriptions.add(repository.retrieveAll().subscribe(categories::set,
                throwable -> Log.e(CategoryFragmentViewModel.class.getCanonicalName(), "Error loading categories", throwable)));
    }
}
