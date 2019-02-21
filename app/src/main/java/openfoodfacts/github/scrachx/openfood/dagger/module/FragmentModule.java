package openfoodfacts.github.scrachx.openfood.dagger.module;

import dagger.Module;
import dagger.Provides;
import openfoodfacts.github.scrachx.openfood.dagger.FragmentScope;
import openfoodfacts.github.scrachx.openfood.views.viewmodel.category.CategoryFragmentViewModel;

@Module
public class FragmentModule {
    @FragmentScope
    @Provides
    CategoryFragmentViewModel provideCategoryFragmentViewModel() {
        return new CategoryFragmentViewModel();
    }
}
