package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.steamcrafted.loadtoast.LoadToast;

import butterknife.Bind;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.FoodUserClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

public class UserFragment extends BaseFragment {

    @Bind(R.id.editTextLogin) EditText login;
    @Bind(R.id.editTextPass) EditText pass;
    @Bind(R.id.textInfoLogin) TextView infoLogin;
    @Bind(R.id.buttonSave) Button save;
    @Bind(R.id.buttonCreateAccount) Button signup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_user);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SharedPreferences settings = getContext().getSharedPreferences("login", 0);

        login.setSelected(false);
        pass.setSelected(false);

        String loginS = settings.getString("user", "");
        String passS = settings.getString("pass", "");

        if (loginS.isEmpty() && passS.isEmpty()) {
            infoLogin.setText(R.string.txtInfoLoginNo);
        } else {
            infoLogin.setText(R.string.txtInfoLoginOk);
        }

        login.setText(loginS);
        pass.setText(passS);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browser = new Intent(Intent.ACTION_VIEW);
                browser.setData(Uri.parse(Utils.getUriByCurrentLanguage() + "cgi/user.pl"));
                startActivity(browser);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams params = new RequestParams();
                params.put("user_id", login.getText().toString());
                params.put("password", pass.getText().toString());
                params.put(".submit", "Sign-in");
                getLoggedIn(params, getContext(), settings);
            }
        });
    }

    public void getLoggedIn(RequestParams params, final Context context, final SharedPreferences shpref) {
        FoodUserClient.post("/cgi/session.pl", params, new AsyncHttpResponseHandler() {

            LoadToast lt = new LoadToast(context);

            @Override
            public void onStart() {
                super.onStart();
                save.setClickable(false);
                lt.setText(context.getString(R.string.toast_retrieving));
                lt.setBackgroundColor(getResources().getColor(R.color.indigo_600));
                lt.setTextColor(getResources().getColor(R.color.white));
                lt.show();
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                SharedPreferences.Editor editor = shpref.edit();
                String htmlNoParsed = new String(responseBody);
                if (htmlNoParsed.contains("Incorrect user name or password.") || htmlNoParsed.contains("See you soon!")) {
                    lt.error();
                    Toast.makeText(context, context.getString(R.string.errorLogin), Toast.LENGTH_LONG).show();
                    login.setText("");
                    pass.setText("");
                    editor.putString("user", "");
                    editor.putString("pass", "");
                    editor.commit();
                    infoLogin.setText(R.string.txtInfoLoginNo);
                } else {
                    lt.success();
                    Toast.makeText(context, getResources().getText(R.string.txtToastSaved), Toast.LENGTH_LONG).show();
                    editor.putString("user", login.getText().toString());
                    editor.putString("pass", pass.getText().toString());
                    editor.commit();
                    infoLogin.setText(R.string.txtInfoLoginOk);
                }
                Utils.hideKeyboard(getActivity());
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, context.getString(R.string.errorWeb), Toast.LENGTH_LONG).show();
                lt.error();
                Utils.hideKeyboard(getActivity());
            }

            @Override
            public void onFinish() {
                super.onFinish();
                save.setClickable(true);
            }
        });
    }
}
