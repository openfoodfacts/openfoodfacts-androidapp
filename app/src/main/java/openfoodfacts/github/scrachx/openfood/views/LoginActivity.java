package openfoodfacts.github.scrachx.openfood.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResponse;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.CredentialsOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

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
public class LoginActivity extends BaseActivity implements CustomTabActivityHelper.ConnectionCallback,GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

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
    @BindView(R.id.buttonCreateAccount)
    Button signup;
    @BindView(R.id.login_linearlayout)
    LinearLayout linearLayout;

    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String TAG = LoginActivity.class.getSimpleName();
    private boolean mIsResolving = false;
    private OpenFoodAPIService apiClient;
    private CustomTabActivityHelper customTabActivityHelper;
    private Uri userLoginUri;
    private Uri resetPasswordUri;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    // boolean to determine if scan on shake feature should be enabled
    private boolean scanOnShake;
    private CredentialRequest mCredentialRequest;
    private CredentialsClient credentialsClient;

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

        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
        }

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
        signup.setEnabled(true);

        final SharedPreferences settings = getSharedPreferences("login", 0);
        String loginS = settings.getString(getResources().getString(R.string.user), getResources().getString(R.string.txt_anonymous));
        if (loginS.equals(getResources().getString(R.string.user))) {
            new MaterialDialog.Builder(this)
                    .title(R.string.log_in)
                    .content(R.string.login_true)
                    .neutralText(R.string.ok_button)
                    .show();
        }

        if(isGooglePlayServicesAvailable(this)){
            requestCredentials();
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
        if (!(password.length() >= 6)) {
            passwordView.setError(getString(R.string.error_invalid_password));
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
                            if(isGooglePlayServicesAvailable(context)){
                                storeCredentials();
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

    @OnClick(R.id.buttonCreateAccount)
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
        signup.setEnabled(true);
    }

    @Override
    public void onCustomTabsDisconnected() {
        signup.setEnabled(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        customTabActivityHelper.unbindCustomTabsService(this);
        signup.setEnabled(false);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void requestCredentials(){
        credentialsClient = Credentials.getClient(this);

        mCredentialRequest = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build();

        credentialsClient.request(mCredentialRequest).addOnCompleteListener(new OnCompleteListener<CredentialRequestResponse>() {
            @Override
            public void onComplete(@NonNull Task<CredentialRequestResponse> task) {
                if (task.isSuccessful()) {
                    onCredentialRetrieved(task.getResult().getCredential());
                    return;
                }
                Exception e = task.getException();
                if (e instanceof ResolvableApiException) {
                    // This is most likely the case where the user has multiple saved
                    // credentials and needs to pick one. This requires showing UI to
                    // resolve the read request.
                    ResolvableApiException rae = (ResolvableApiException) e;
                    resolveResult(rae, 2);
                } else if (e instanceof ApiException) {
                    // The user must create an account or sign in manually.
                    ApiException ae = (ApiException) e;
                    if (ae.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                        // This means only a hint is available, but we are handling that
                        // elsewhere so no need to act here.
                    } else {
                        Log.w(TAG, "Unexpected status code: " + ae.getStatusCode());
                    }
                }
            }
        });

    }

    private void onCredentialRetrieved(Credential credential) {
        String accountType = credential.getAccountType();
        if (accountType == null) {
            // Sign the user in with information from the Credential.
            loginView.setText(credential.getId());
            passwordView.setText(credential.getPassword());
        }
    }

    private void resolveResult(ResolvableApiException rae, int requestCode) {
        try {
            rae.startResolutionForResult(LoginActivity.this, requestCode);
            mIsResolving = true;
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Failed to send resolution.", e);
        }
    }

    private void storeCredentials(){
        Credential credential = new Credential.Builder(loginView.getText().toString())
                .setPassword(passwordView.getText().toString())
                .build();
        Snackbar snackbar = Snackbar
                .make(linearLayout, R.string.toast_retrieving, Snackbar.LENGTH_LONG);
        snackbar.show();
        CredentialsOptions options = new CredentialsOptions.Builder()
                .forceEnableSaveDialog()
                .build();
        credentialsClient = Credentials.getClient(this, options);
        credentialsClient.save(credential).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.w(TAG,"Credential Saved");
                            snackbar.dismiss();
                            return;
                        }

                        Exception e = task.getException();
                        if (e instanceof ResolvableApiException) {
                            // The first time a credential is saved, the user is shown UI
                            // to confirm the action. This requires resolution.
                            snackbar.dismiss();
                            ResolvableApiException rae = (ResolvableApiException) e;
                            resolveResult(rae, 3);
                        } else {
                            // Save failure cannot be resolved.
                            Log.w(TAG, "Save failed.", e);
                            snackbar.dismiss();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                onCredentialRetrieved(credential);
            } else {
                Log.e(TAG, "Credential Read Failed");
            }
        }

        if (requestCode == 3) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Credentials Stored Successfully");
            }
        }

    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }
}
