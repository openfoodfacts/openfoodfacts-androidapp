package openfoodfacts.github.scrachx.openfood.dagger.module

import dagger.Module
import dagger.Provides
import openfoodfacts.github.scrachx.openfood.dagger.FragmentScope
import openfoodfacts.github.scrachx.openfood.features.viewmodel.category.CategoryFragmentViewModel

@Module
class FragmentModule {
    @FragmentScope
    @Provides
    fun provideCategoryFragmentViewModel(): CategoryFragmentViewModel = CategoryFragmentViewModel()
}