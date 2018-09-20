package openfoodfacts.github.scrachx.openfood.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.ContinuousScanActivity;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_HOME;

public class HomeFragment extends NavigationBaseFragment {

    @BindView(R.id.buttonScan)
    FloatingActionButton mButtonScan;

    @BindView(R.id.textHome)
    TextView textHome;

    private OpenFoodAPIService apiClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiClient = new OpenFoodAPIClient(getActivity()).getAPIService();
        checkUserCredentials();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getContext()!=null){
            if(Utils.isNetworkConnected(getContext())){
                Call<Search> call =  apiClient.getTotalProductCount();
                call.enqueue(new Callback<Search>() {
                    @Override
                    public void onResponse(Call<Search> call, Response<Search> response) {
                        textHome.setText("Open Food Facts is an open database of more than "+response.body().getCount()+" food products (plus the ones you'll contribute) with ingredients, allergens, nutrition facts and all the tidbits of information we can find on product labels.");
                    }

                    @Override
                    public void onFailure(Call<Search> call, Throwable t) {
                        textHome.setText(R.string.txtHome);
                    }
                });
            }
            else {
                textHome.setText(R.string.txtHome);
            }
        }
    }

    @OnClick(R.id.buttonScan)
    protected void OnScan() {
        if (Utils.isHardwareCameraInstalled(getContext())) {
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
                Intent intent = new Intent(getActivity(), ContinuousScanActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        } else {
            ((MainActivity) getContext()).moveToBarcodeEntry();
        }
    }

    @Override
    @NavigationDrawerType
    public int getNavigationDrawerType() {
        return ITEM_HOME;
    }

    private void checkUserCredentials() {
        final SharedPreferences settings = getActivity().getSharedPreferences("login", 0);
        String login = settings.getString("user", "");
        String password = settings.getString("pass", "");

        if (!login.isEmpty() && !password.isEmpty()) {
            apiClient.signIn(login, password, "Sign-in").enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
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
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e(HomeFragment.class.getName(), "Unable to Sign-in");
                }
            });
        }
    }


    public void onResume() {

        super.onResume();

        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.home_drawer));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }
}
