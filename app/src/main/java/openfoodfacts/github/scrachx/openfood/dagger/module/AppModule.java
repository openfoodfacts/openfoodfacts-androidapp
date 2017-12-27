package openfoodfacts.github.scrachx.openfood.dagger.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import openfoodfacts.github.scrachx.openfood.dagger.Qualifiers;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

@Module
public class AppModule {
    private OFFApplication application;

    public AppModule(OFFApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    OFFApplication provideTrainLineApplication() {
        return application;
    }

    @Provides
    @Qualifiers.ForApplication
    @Singleton
    Context provideApplicationContext() {
        return application;
    }
}