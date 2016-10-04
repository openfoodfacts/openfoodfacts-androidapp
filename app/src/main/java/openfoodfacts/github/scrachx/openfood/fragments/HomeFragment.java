package openfoodfacts.github.scrachx.openfood.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import butterknife.BindView;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.FoodAPIRestClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.ScannerFragmentActivity;

public class HomeFragment extends BaseFragment {

    @BindView(R.id.buttonScan) Button mButtonScan;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkUserCredentials();
    }

    @OnClick(R.id.buttonScan)
    protected void OnScan() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.action_about)
                        .content(R.string.permission_camera)
                        .neutralText(R.string.txtOk)
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
            Intent intent = new Intent(getActivity(), ScannerFragmentActivity.class);
            startActivity(intent);
        }
    }

    private void checkUserCredentials() {
        final SharedPreferences settings = getActivity().getSharedPreferences("login", 0);
        String loginS = settings.getString("user", "");
        String passS = settings.getString("pass", "");

        if (!loginS.isEmpty() && !passS.isEmpty()) {
            RequestParams params = new RequestParams();
            params.put("user_id", loginS);
            params.put("password", passS);
            params.put(".submit", "Sign-in");

            FoodAPIRestClient.post(getString(R.string.openfoodUrl) + "/cgi/session.pl", params, new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                    SharedPreferences.Editor editor = settings.edit();
                    String htmlNoParsed = new String(responseBody);
                    if (htmlNoParsed.contains("Incorrect user name or password.") || htmlNoParsed.contains("See you soon!")) {
                        editor.putString("user", "");
                        editor.putString("pass", "");
                        editor.apply();
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.alert_dialog_warning_title)
                                .content(R.string.alert_dialog_warning_msg_user)
                                .positiveText(R.string.txtOk)
                                .show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                }
            });
        }
    }
}
