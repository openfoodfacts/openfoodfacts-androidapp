package openfoodfacts.github.scrachx.openfood.dagger.component;

import dagger.Subcomponent;
import openfoodfacts.github.scrachx.openfood.dagger.FragmentScope;
import openfoodfacts.github.scrachx.openfood.dagger.module.FragmentModule;

@Subcomponent(modules = {FragmentModule.class})
@FragmentScope
public interface FragmentComponent {
}