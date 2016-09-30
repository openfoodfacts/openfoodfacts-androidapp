package openfoodfacts.github.scrachx.openfood.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.steamcrafted.loadtoast.LoadToast;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.FoodAPIRestClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

/**
 * A login screen that offers login via login/password.
 */
public class LoginActivity extends BaseActivity {

    @BindView(R.id.editTextLogin) EditText loginView;
    @BindView(R.id.editTextPass) EditText passwordView;
    @BindView(R.id.textInfoLogin) TextView infoLogin;
    @BindView(R.id.buttonSave) Button save;
    @BindView(R.id.buttonCreateAccount) Button signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final SharedPreferences settings = getSharedPreferences("login", 0);
        String loginS = settings.getString("user", getResources().getString(R.string.txt_anonymous));
        if(!loginS.equals(getResources().getString(R.string.txt_anonymous))) {
            new MaterialDialog.Builder(this)
                    .title("Login")
                    .content("Already logged in!")
                    .neutralText(R.string.ok_button)
                    .show();
        }
    }

    @OnClick(R.id.buttonCreateAccount)
    protected void onCreateUser() {
        Intent browser = new Intent(Intent.ACTION_VIEW);
        browser.setData(Uri.parse(Utils.getUriByCurrentLanguage() + "cgi/user.pl"));
        startActivity(browser);
    }

    @OnClick(R.id.buttonSave)
    protected void attemptLogin() {
        String login = this.loginView.getText().toString();
        String password = passwordView.getText().toString();

        if (!(password.length() > 6)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            passwordView.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(login)) {
            loginView.setError(getString(R.string.error_field_required));
            loginView.requestFocus();
            return;
        }

        RequestParams requestParams = new RequestParams();
        requestParams.put("user_id", login);
        requestParams.put("password", password);
        requestParams.put(".submit", "Sign-in");

        final Activity context = this;

        FoodAPIRestClient.post(getString(R.string.openfoodUrl) + "/cgi/session.pl", requestParams, new AsyncHttpResponseHandler() {

            LoadToast lt = new LoadToast(context);

            @Override
            public void onStart() {
                super.onStart();
                save.setClickable(false);
                lt.setText(context.getString(R.string.toast_retrieving));
                lt.setBackgroundColor(context.getResources().getColor(R.color.indigo_600));
                lt.setTextColor(context.getResources().getColor(R.color.white));
                lt.show();
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                SharedPreferences settings = context.getSharedPreferences("login", 0);
                SharedPreferences.Editor editor = settings.edit();
                String htmlNoParsed = new String(responseBody);

                if (htmlNoParsed.contains("Incorrect user name or password.") || htmlNoParsed.contains("See you soon!")) {
                    lt.error();
                    Toast.makeText(context, context.getString(R.string.errorLogin), Toast.LENGTH_LONG).show();
                    loginView.setText("");
                    passwordView.setText("");
                    editor.putString("user", "");
                    editor.putString("pass", "");
                    editor.apply();
                    infoLogin.setText(R.string.txtInfoLoginNo);
                } else {
                    lt.success();
                    Toast.makeText(context, context.getResources().getText(R.string.txtToastSaved), Toast.LENGTH_LONG).show();
                    editor.putString("user", loginView.getText().toString());
                    editor.putString("pass", passwordView.getText().toString());
                    editor.apply();
                    infoLogin.setText(R.string.txtInfoLoginOk);

                    setResult(RESULT_OK, new Intent());
                    finish();
                }
                Utils.hideKeyboard(context);
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, context.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                lt.error();
                Utils.hideKeyboard(context);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                save.setClickable(true);
            }
        });
    }
}
