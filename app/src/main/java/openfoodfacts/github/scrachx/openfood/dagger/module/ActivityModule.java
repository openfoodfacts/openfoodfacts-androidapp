package openfoodfacts.github.scrachx.openfood.dagger.module;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import dagger.Module;
import dagger.Provides;
import openfoodfacts.github.scrachx.openfood.dagger.ActivityScope;
import openfoodfacts.github.scrachx.openfood.dagger.Qualifiers.ForActivity;

@Module
public class ActivityModule {
    private final AppCompatActivity activity;

    public ActivityModule(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Provides
    @ForActivity
    @ActivityScope
    Context provideActivityContext() {
        return activity;
    }

}
