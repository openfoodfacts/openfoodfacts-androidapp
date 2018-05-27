package org.openfoodfacts.scanner.dagger.component;

import javax.inject.Singleton;

import dagger.Component;
import org.openfoodfacts.scanner.dagger.module.ActivityModule;
import org.openfoodfacts.scanner.dagger.module.AppModule;
import org.openfoodfacts.scanner.views.OFFApplication;

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {

    ActivityComponent plusActivityComponent(ActivityModule activityModule);

    void inject(OFFApplication application);

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
