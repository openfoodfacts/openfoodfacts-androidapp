package org.openfoodfacts.scanner.dagger.component;

import dagger.Subcomponent;
import org.openfoodfacts.scanner.dagger.FragmentScope;
import org.openfoodfacts.scanner.dagger.module.FragmentModule;
import org.openfoodfacts.scanner.views.category.fragment.CategoryListFragment;

@Subcomponent(modules = {FragmentModule.class})
@FragmentScope
public interface FragmentComponent {
    void inject(CategoryListFragment categoryListFragment);
}