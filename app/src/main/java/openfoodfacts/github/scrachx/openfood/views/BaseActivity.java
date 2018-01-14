package openfoodfacts.github.scrachx.openfood.views;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import butterknife.ButterKnife;
import openfoodfacts.github.scrachx.openfood.dagger.component.ActivityComponent;
import openfoodfacts.github.scrachx.openfood.dagger.module.ActivityModule;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;

public abstract class BaseActivity extends AppCompatActivity {

    private ActivityComponent activityComponent;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent = OFFApplication.getAppComponent().plusActivityComponent(new ActivityModule(this));
        activityComponent.inject(this);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        LocaleHelper.onCreate(this);
    }

    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }
}