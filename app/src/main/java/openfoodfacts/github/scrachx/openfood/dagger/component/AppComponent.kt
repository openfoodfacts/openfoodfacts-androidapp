package openfoodfacts.github.scrachx.openfood.dagger.component

import dagger.Component
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.dagger.module.ActivityModule
import openfoodfacts.github.scrachx.openfood.dagger.module.AppModule
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import javax.inject.Singleton

@Component(modules = [AppModule::class])
@Singleton
interface AppComponent {
    fun plusActivityComponent(activityModule: ActivityModule?): ActivityComponent?
    fun inject(application: OFFApplication?)
    fun inject(activity: ContinuousScanActivity?)
    fun inject(activity: ProductEditActivity?)

    object Initializer {
        @JvmStatic
        @Synchronized
        fun init(appModule: AppModule?): AppComponent = DaggerAppComponent.builder()
                .appModule(appModule)
                .build()
    }
}