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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.Objects;

import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityLoginBinding;
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI;
import openfoodfacts.github.scrachx.openfood.utils.ShakeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG;

/**
 * A login screen that offers login via login/password.
 * This Activity connect to the Chrome Custom Tabs Service on startup to prefetch the url.
 */
public class LoginActivity extends BaseActivity implements CustomTabActivityHelper.ConnectionCallback {
    private ActivityLoginBinding binding;
    private ProductsAPI apiClient;
    private CustomTabActivityHelper customTabActivityHelper;
    private Uri userLoginUri;
    private Uri resetPasswordUri;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    // boolean to determine if scan on shake feature should be enabled
    private boolean scanOnShake;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup view listeners
        binding.btnLogin.setOnClickListener(v -> doAttemptLogin());
        binding.btnCreateAccount.setOnClickListener(v -> doRegister());
        binding.btnForgotPass.setOnClickListener(v -> doForgotPassword());

        setTitle(getString(R.string.txtSignIn));
        setSupportActionBar(binding.toolbarLayout.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        userLoginUri = Uri.parse(getString(R.string.website) + "cgi/user.pl");
        resetPasswordUri = Uri.parse(getString(R.string.website) + "cgi/reset_password.pl");

        // prefetch the uri
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
        customTabActivityHelper.mayLaunchUrl(userLoginUri, null, null);
        binding.btnCreateAccount.setEnabled(true);

        final SharedPreferences settings = getSharedPreferences("login", 0);
        String loginS = settings.getString(getResources().getString(R.string.user), getResources().getString(R.string.txt_anonymous));
        if (loginS.equals(getResources().getString(R.string.user))) {
            new MaterialDialog.Builder(this)
                .title(R.string.log_in)
                .content(R.string.login_true)
                .neutralText(R.string.ok_button)
                .show();
        }

        apiClient = new Retrofit.Builder()
            .baseUrl(BuildConfig.HOST)
            .client(Utils.httpClientBuilder())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(ProductsAPI.class);

        // Get the user preference for scan on shake feature and open ContinuousScanActivity if the user has enabled the feature
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = Objects.requireNonNull(mSensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();

        SharedPreferences shakePreference = PreferenceManager.getDefaultSharedPreferences(this);
        scanOnShake = shakePreference.getBoolean("shakeScanMode", false);

        mShakeDetector.setOnShakeListener(count -> {
            if (scanOnShake) {
                Utils.scan(LoginActivity.this);
            }
        });
    }

    protected void doAttemptLogin() {
        Utils.hideKeyboard(this);
        String login = binding.loginInput.getText().toString();
        String password = binding.passInput.getText().toString();
        if (TextUtils.isEmpty(login)) {
            binding.loginInput.setError(getString(R.string.error_field_required));
            binding.loginInput.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.passInput.setError(getString(R.string.error_field_required));
            binding.passInput.requestFocus();
            return;
        }
        if (password.length() < 6) {
            binding.passInput.setError(getText(R.string.error_invalid_password));
            binding.passInput.requestFocus();
            return;
        }

        Snackbar snackbar = Snackbar.make(binding.loginLinearlayout, R.string.toast_retrieving, LENGTH_LONG);
        snackbar.show();

        binding.btnLogin.setClickable(false);

        final Activity context = this;
        apiClient.signIn(login, password, "Sign-in").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(context, context.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                    return;
                }

                String htmlNoParsed = null;
                try {
                    htmlNoParsed = response.body().string();
                } catch (IOException e) {
                    Log.e("LOGIN", "Unable to parse the login response page", e);
                }

                SharedPreferences.Editor editor = context.getSharedPreferences("login", 0).edit();

                if (htmlNoParsed == null
                    || htmlNoParsed.contains("Incorrect user name or password.")
                    || htmlNoParsed.contains("See you soon!")) {

                    Toast.makeText(context, context.getString(R.string.errorLogin), Toast.LENGTH_LONG).show();
                    binding.passInput.setText("");

                    binding.txtInfoLogin.setTextColor(getResources().getColor(R.color.red));
                    binding.txtInfoLogin.setText(R.string.txtInfoLoginNo);

                    snackbar.dismiss();
                } else {
                    // store the user session id (user_session and user_id)
                    for (HttpCookie httpCookie : HttpCookie.parse(response.headers().get("set-cookie"))) {
                        // Example format of set-cookie: session=user_session&S0MeR@nD0MSECRETk3Y&user_id&testuser; domain=.openfoodfacts.org; path=/
                        if (BuildConfig.HOST.contains(httpCookie.getDomain()) && httpCookie.getPath().equals("/")) {
                            String[] cookieValues = httpCookie.getValue().split("&");
                            for (int i = 0; i < cookieValues.length; i++) {
                                editor.putString(cookieValues[i], cookieValues[++i]);
                            }
                            break;
                        }
                    }
                    Snackbar snackbar = Snackbar.make(binding.loginLinearlayout, R.string.connection, LENGTH_LONG);

                    snackbar.show();

                    Toast.makeText(context, context.getResources().getText(R.string.txtToastSaved), Toast.LENGTH_LONG).show();
                    editor.putString("user", login);
                    editor.putString("pass", password);
                    editor.apply();

                    binding.txtInfoLogin.setTextColor(getResources().getColor(R.color.green_500));
                    binding.txtInfoLogin.setText(R.string.txtInfoLoginOk);

                    setResult(RESULT_OK, new Intent());
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(context, context.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                Log.e(getClass().getSimpleName(), "onFailure", t);
            }
        });

        binding.btnLogin.setClickable(true);
    }

    protected void doRegister() {
        CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getBaseContext(), customTabActivityHelper.getSession());

        CustomTabActivityHelper.openCustomTab(this, customTabsIntent, userLoginUri, new WebViewFallback());
    }

    public void doForgotPassword() {
        CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getBaseContext(), customTabActivityHelper.getSession());
        CustomTabActivityHelper.openCustomTab(this, customTabsIntent, resetPasswordUri, new WebViewFallback());
    }

    @Override
    public void onCustomTabsConnected() {
        binding.btnCreateAccount.setEnabled(true);
    }

    @Override
    public void onCustomTabsDisconnected() {
        //TODO find out what do do with it
        binding.btnCreateAccount.setEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        customTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        customTabActivityHelper.unbindCustomTabsService(this);
        binding.btnCreateAccount.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customTabActivityHelper.setConnectionCallback(null);
        binding = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (scanOnShake) {
            // unregister the listener
            mSensorManager.unregisterListener(mShakeDetector, mAccelerometer);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (scanOnShake) {
            //register the listener
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }
}
