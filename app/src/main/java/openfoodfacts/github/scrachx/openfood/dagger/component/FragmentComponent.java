package openfoodfacts.github.scrachx.openfood.dagger.component;

import dagger.Subcomponent;
import openfoodfacts.github.scrachx.openfood.dagger.FragmentScope;
import openfoodfacts.github.scrachx.openfood.dagger.module.FragmentModule;
import openfoodfacts.github.scrachx.openfood.views.category.fragment.CategoryListFragment;

@Subcomponent(modules = {FragmentModule.class})
@FragmentScope
public interface FragmentComponent {
    void inject(CategoryListFragment categoryListFragment);
}