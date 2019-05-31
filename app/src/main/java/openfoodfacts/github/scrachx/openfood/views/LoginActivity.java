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
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.IOException;
import java.net.HttpCookie;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.ShakeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A login screen that offers login via login/password.
 * This Activity connect to the Chrome Custom Tabs Service on startup to prefetch the url.
 */
public class LoginActivity extends BaseActivity implements CustomTabActivityHelper.ConnectionCallback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.editTextLogin)
    EditText loginView;
    @BindView(R.id.editTextPass)
    EditText passwordView;
    @BindView(R.id.textInfoLogin)
    TextView infoLogin;
    @BindView(R.id.buttonSave)
    Button save;
    @BindView(R.id.createaccount)
    TextView createAccount;
    @BindView(R.id.login_linearlayout)
    LinearLayout linearLayout;

    private OpenFoodAPIService apiClient;
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
        setContentView(R.layout.activity_login);

        setTitle(getString(R.string.txtSignIn));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        userLoginUri = Uri.parse(getString(R.string.website) + "cgi/user.pl");
        resetPasswordUri = Uri.parse(getString(R.string.website) + "cgi/reset_password.pl");

        // prefetch the uri
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
        customTabActivityHelper.mayLaunchUrl(userLoginUri, null, null);
        createAccount.setEnabled(true);

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
                .client(Utils.HttpClientBuilder())
                .build()
                .create(OpenFoodAPIService.class);

        // Get the user preference for scan on shake feature and open ContinuousScanActivity if the user has enabled the feature
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();

        SharedPreferences shakePreference = PreferenceManager.getDefaultSharedPreferences(this);
        scanOnShake = shakePreference.getBoolean("shakeScanMode", false);

        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeDetected() {
            @Override
            public void onShake(int count) {
                if (scanOnShake) {
                    Utils.scan(LoginActivity.this);
                }
            }
        });

    }

    @OnClick(R.id.buttonSave)
    protected void attemptLogin() {
        String login = loginView.getText().toString();
        String password = passwordView.getText().toString();
        if (TextUtils.isEmpty(login)) {
            loginView.setError(getString(R.string.error_field_required));
            loginView.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            passwordView.requestFocus();
            return;
        }
        if (!(password.length() >= 6)) {
            Toast.makeText(this,getText(R.string.error_invalid_password),Toast.LENGTH_SHORT).show();
            passwordView.requestFocus();
            return;
        }

        Snackbar snackbar = Snackbar
                .make(linearLayout, R.string.toast_retrieving, Snackbar.LENGTH_LONG);
        snackbar.show();

        final LoadToast lt = new LoadToast(this);
        save.setClickable(false);
        lt.setText(getString(R.string.toast_retrieving));
        lt.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
        lt.setTextColor(ContextCompat.getColor(this, R.color.white));
        lt.show();

        final Activity context = this;
        apiClient.signIn(login, password, "Sign-in").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(context, context.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();

                    Utils.hideKeyboard(context);
                    return;
                }

                String htmlNoParsed = null;
                try {
                    htmlNoParsed = response.body().string();
                } catch (IOException e) {
                    Log.e("LOGIN", "Unable to parse the login response page", e);
                }

                SharedPreferences.Editor editor = context.getSharedPreferences("login", 0).edit();

                if (htmlNoParsed == null || htmlNoParsed.contains("Incorrect user name or password.") || htmlNoParsed.contains("See you soon!")) {

                    Toast.makeText(context, context.getString(R.string.errorLogin), Toast.LENGTH_LONG).show();
                    passwordView.setText("");
                    loginView.setText("");
                    infoLogin.setText(R.string.txtInfoLoginNo);
                    lt.hide();
                } else {
                    // store the user session id (user_session and user_id)
                    for (HttpCookie httpCookie : HttpCookie.parse(response.headers().get("set-cookie"))) {
                        if (httpCookie.getDomain().equals(".openbeautyfacts.org") && httpCookie.getPath().equals("/")) {
                            String[] cookieValues = httpCookie.getValue().split("&");
                            for (int i = 0; i < cookieValues.length; i++) {
                                editor.putString(cookieValues[i], cookieValues[++i]);
                            }

                            break;
                        }
                    }
                    Snackbar snackbar = Snackbar
                            .make(linearLayout, R.string.connection, Snackbar.LENGTH_LONG);

                    snackbar.show();

                    Toast.makeText(context, context.getResources().getText(R.string.txtToastSaved), Toast.LENGTH_LONG).show();
                    editor.putString("user", login);
                    editor.putString("pass", password);
                    editor.apply();
                    infoLogin.setText(R.string.txtInfoLoginOk);

                    setResult(RESULT_OK, new Intent());
                    finish();
                }
                Utils.hideKeyboard(context);

            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(context, context.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();

                Utils.hideKeyboard(context);
                t.printStackTrace();
            }
        });

        save.setClickable(true);
    }

    @OnClick(R.id.createaccount)
    protected void onCreateUser() {
        CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getBaseContext(), customTabActivityHelper.getSession());

        CustomTabActivityHelper.openCustomTab(this, customTabsIntent, userLoginUri, new WebViewFallback());
    }

    @OnClick(R.id.forgotpassword)
    public void forgotpassword() {
        CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getBaseContext(), customTabActivityHelper.getSession());
        CustomTabActivityHelper.openCustomTab(this, customTabsIntent, resetPasswordUri, new WebViewFallback());
    }

    @Override
    public void onCustomTabsConnected() {
        createAccount.setEnabled(true);
    }

    @Override
    public void onCustomTabsDisconnected() {
        //TODO find out what do do with it
        createAccount.setEnabled(false);
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
        createAccount.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customTabActivityHelper.setConnectionCallback(null);
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
