package org.openfoodfacts.scanner.dagger.module;

import dagger.Module;
import dagger.Provides;
import org.openfoodfacts.scanner.dagger.FragmentScope;
import org.openfoodfacts.scanner.views.viewmodel.category.CategoryFragmentViewModel;

@Module
public class FragmentModule {
    @FragmentScope
    @Provides
    CategoryFragmentViewModel provideCategoryFragmentViewModel() {
        return new CategoryFragmentViewModel();
    }
}
