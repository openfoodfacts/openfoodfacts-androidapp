/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.views;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.apache.commons.lang.StringUtils;

import openfoodfacts.github.scrachx.openfood.R;
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
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        activityComponent = OFFApplication.getAppComponent().plusActivityComponent(new ActivityModule(this));
        activityComponent.inject(this);
    }

    @Override
    public void setContentView(final View view) {
        super.setContentView(view);
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
