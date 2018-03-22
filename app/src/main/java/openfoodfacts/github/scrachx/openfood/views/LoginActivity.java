package openfoodfacts.github.scrachx.openfood.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
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
    @BindView(R.id.buttonCreateAccount)
    Button signup;
    @BindView(R.id.login_linearlayout)  
    LinearLayout linearLayout;

    private OpenFoodAPIService apiClient;
    private CustomTabActivityHelper customTabActivityHelper;
    private Uri userLoginUri;
    private Uri resetPasswordUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_login);

        setTitle(getString(R.string.txtSignIn));
        setSupportActionBar(toolbar);


        userLoginUri = Uri.parse(getString(R.string.website) + "cgi/user.pl");
        resetPasswordUri = Uri.parse(getString(R.string.website) + "cgi/reset_password.pl");

        // prefetch the uri
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
        customTabActivityHelper.mayLaunchUrl(userLoginUri, null, null);

        signup.setEnabled(false);

        final SharedPreferences settings = getSharedPreferences("login", 0);
        String loginS = settings.getString("user", getResources().getString(R.string.txt_anonymous));
        if (!loginS.equals(getResources().getString(R.string.txt_anonymous))) {
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
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
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
                    loginView.setText("");
                    passwordView.setText("");
                    editor.putString("user", "");
                    editor.putString("pass", "");
                    editor.apply();
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
                            .make(linearLayout,R.string.connection, Snackbar.LENGTH_LONG);

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
            public void onFailure(Call<ResponseBody> call, Throwable t) {
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
    protected void onStart() {
        super.onStart();
        customTabActivityHelper.bindCustomTabsService(this);
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
}
