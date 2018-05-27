package org.openfoodfacts.scanner.dagger.component;

import dagger.Subcomponent;
import org.openfoodfacts.scanner.dagger.ActivityScope;
import org.openfoodfacts.scanner.dagger.module.ActivityModule;
import org.openfoodfacts.scanner.views.BaseActivity;

@Subcomponent(modules = {ActivityModule.class})
@ActivityScope
public interface ActivityComponent {

    FragmentComponent plusFragmentComponent();

    void inject(BaseActivity baseActivity);
}
