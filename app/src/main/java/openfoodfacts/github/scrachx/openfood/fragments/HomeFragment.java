package openfoodfacts.github.scrachx.openfood.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.ScannerFragmentActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends BaseFragment {

    @BindView(R.id.buttonScan)
    FloatingActionButton mButtonScan;

    private OpenFoodAPIService apiClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiClient = new OpenFoodAPIClient(getActivity()).getAPIService();
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
                        .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA))
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
        String login = settings.getString("user", "");
        String password = settings.getString("pass", "");

        if (!login.isEmpty() && !password.isEmpty()) {
            apiClient.signIn(login, password, "Sign-in").enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    String htmlNoParsed = null;
                    try {
                        htmlNoParsed = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (htmlNoParsed != null && (htmlNoParsed.contains("Incorrect user name or password.")
                            || htmlNoParsed.contains("See you soon!"))) {
                        settings.edit()
                                .putString("user", "")
                                .putString("pass", "")
                                .apply();

                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.alert_dialog_warning_title)
                                .content(R.string.alert_dialog_warning_msg_user)
                                .positiveText(R.string.txtOk)
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(HomeFragment.class.getName(), "Unable to Sign-in");
                }
            });
        }
    }
}
