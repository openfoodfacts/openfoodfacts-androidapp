package org.openfoodfacts.scanner.dagger.module;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import dagger.Module;
import dagger.Provides;
import org.openfoodfacts.scanner.dagger.ActivityScope;
import org.openfoodfacts.scanner.dagger.Qualifiers;

@Module
public class ActivityModule {
    private AppCompatActivity activity;

    public ActivityModule(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Qualifiers.ForActivity
    @ActivityScope
    Context provideActivityContext() {
        return activity;
    }

}
