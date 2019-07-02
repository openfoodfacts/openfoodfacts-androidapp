package openfoodfacts.github.scrachx.openfood.views;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import butterknife.ButterKnife;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.dagger.component.ActivityComponent;
import openfoodfacts.github.scrachx.openfood.dagger.module.ActivityModule;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import org.apache.commons.lang.StringUtils;

public abstract class BaseActivity extends AppCompatActivity {
    private ActivityComponent activityComponent;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
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

    boolean isUserLoggedIn() {
        return isUserLoggedIn(this);
    }

    boolean isUserNotLoggedIn() {
        return !isUserLoggedIn();
    }

    String getUserLogin() {
        SharedPreferences preferences = getLoginPreferences();
        return preferences == null ? null : preferences.getString("user", null);
    }

    //Helper Function
    protected int dpsToPixel(int dps) {
        return dpsToPixel(dps,this);
    }

    public  static  int dpsToPixel(int dps,Activity activity) {
        if(activity==null){
            return 0;
        }
        final float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    private SharedPreferences getLoginPreferences() {
        return getLoginPreferences(this);
    }

    private static SharedPreferences getLoginPreferences(Activity activity) {
        if (activity == null) {
            return null;
        }
        return activity.getSharedPreferences("login", 0);
    }

    String getUserSession() {
        SharedPreferences preferences = getLoginPreferences();
        return preferences.getString("user_session", null);
    }

    public static boolean isUserLoggedIn(Activity activity) {
        if (activity == null) {
            return false;
        }
        final String login = getLoginPreferences(activity).getString("user", "");
        return StringUtils.isNotBlank(login);
    }
}
