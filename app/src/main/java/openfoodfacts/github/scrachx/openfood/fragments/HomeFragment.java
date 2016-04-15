package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.FoodUserClient;

/**
 * Created by scotscriven on 04/05/15.
 */
public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_home,container, false);

        final SharedPreferences settings = rootView.getContext().getSharedPreferences("login", 0);
        String loginS = settings.getString("user", "");
        String passS = settings.getString("pass", "");

        if(!loginS.isEmpty() && !passS.isEmpty()) {
            RequestParams params = new RequestParams();
            params.put("user_id", loginS);
            params.put("password", passS);
            params.put(".submit", "Sign-in");

            FoodUserClient.post("/cgi/session.pl", params, new AsyncHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                    SharedPreferences.Editor editor = settings.edit();
                    String htmlNoParsed = new String(responseBody);
                    if (htmlNoParsed.contains("Incorrect user name or password.") || htmlNoParsed.contains("See you soon!")) {
                        editor.putString("user", "");
                        editor.putString("pass", "");
                        editor.commit();
                        new MaterialDialog.Builder(rootView.getContext())
                                .title(R.string.alert_dialog_warning_title)
                                .content(R.string.alert_dialog_warning_msg_user)
                                .positiveText(R.string.txtOk)
                                .show();
                    }
                }

                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

                }

                @Override
                public void onFinish(){
                    super.onFinish();
                }
            });
        }

        return rootView;
    }
}
