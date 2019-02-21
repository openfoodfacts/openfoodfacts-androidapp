package openfoodfacts.github.scrachx.openfood.dagger.component;

import dagger.Subcomponent;
import openfoodfacts.github.scrachx.openfood.dagger.ActivityScope;
import openfoodfacts.github.scrachx.openfood.dagger.module.ActivityModule;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;

@Subcomponent(modules = {ActivityModule.class})
@ActivityScope
public interface ActivityComponent {

    FragmentComponent plusFragmentComponent();

    void inject(BaseActivity baseActivity);
}
