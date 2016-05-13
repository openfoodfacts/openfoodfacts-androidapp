package openfoodfacts.github.scrachx.openfood.fragments;

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
import com.loopj.android.http.RequestParams;

import butterknife.Bind;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.FoodUserClientUsage;
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

        SharedPreferences settings = getContext().getSharedPreferences("login", 0);

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
    }

    @OnClick(R.id.buttonSave)
    protected void onSaveUser() {
        SharedPreferences settings = getContext().getSharedPreferences("login", 0);
        RequestParams params = new RequestParams();
        params.put("user_id", login.getText().toString());
        params.put("password", pass.getText().toString());
        params.put(".submit", "Sign-in");
        FoodUserClientUsage api = new FoodUserClientUsage();
        api.getLoggedIn(params, getContext(), getActivity(), settings, save, login, pass, infoLogin);
    }

    @OnClick(R.id.buttonCreateAccount)
    protected void onCreateUser() {
        Intent browser = new Intent(Intent.ACTION_VIEW);
        browser.setData(Uri.parse(Utils.getUriByCurrentLanguage() + "cgi/user.pl"));
        startActivity(browser);
    }


}
