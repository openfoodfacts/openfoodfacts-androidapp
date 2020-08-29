package openfoodfacts.github.scrachx.openfood.dagger.component;

import javax.inject.Singleton;

import dagger.Component;
import openfoodfacts.github.scrachx.openfood.dagger.module.ActivityModule;
import openfoodfacts.github.scrachx.openfood.dagger.module.AppModule;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.scan.ContinuousScanActivity;

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {

    ActivityComponent plusActivityComponent(ActivityModule activityModule);

    void inject(OFFApplication application);

    void inject(ContinuousScanActivity activity);

    void inject(openfoodfacts.github.scrachx.openfood.views.AddProductActivity activity);

    final class Initializer {

        private Initializer() {
            //empty
        }

        public static synchronized AppComponent init(AppModule appModule) {
            return DaggerAppComponent.builder()
                    .appModule(appModule)
                    .build();
        }
    }
}
