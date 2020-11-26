package openfoodfacts.github.scrachx.openfood.dagger.component

import dagger.Subcomponent
import openfoodfacts.github.scrachx.openfood.dagger.ActivityScope
import openfoodfacts.github.scrachx.openfood.dagger.module.ActivityModule
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity

@Subcomponent(modules = [ActivityModule::class])
@ActivityScope
interface ActivityComponent {
    fun plusFragmentComponent(): FragmentComponent?
    fun inject(baseActivity: BaseActivity?)
}