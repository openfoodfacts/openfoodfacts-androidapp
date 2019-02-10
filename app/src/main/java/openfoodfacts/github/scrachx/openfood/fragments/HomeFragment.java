package openfoodfacts.github.scrachx.openfood.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.models.TaglineLanguageModel;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.ContinuousScanActivity;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_HOME;

public class HomeFragment extends NavigationBaseFragment implements CustomTabActivityHelper.ConnectionCallback {

    @BindView(R.id.buttonScan)
    FloatingActionButton mButtonScan;

    @BindView(R.id.tvDailyFoodFact)
    TextView tvDailyFoodFact;

    @BindView(R.id.textHome)
    TextView textHome;

    private OpenFoodAPIService apiClient;
    private OpenFoodAPIService apiCacheClient;
    private SharedPreferences sp;
    private String taglineURL;
    private Cache cache;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiClient = new OpenFoodAPIClient(getActivity()).getAPIService();
        checkUserCredentials();
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        File httpCacheDirectory = new File(getContext().getCacheDir(), "offlineCache");
        //Allocates 10 MB for cache storage
        cache = new Cache(httpCacheDirectory, 10 * 1024 * 1024);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
            if (getContext() instanceof MainActivity) {
                ((MainActivity) getContext()).moveToBarcodeEntry();
            }
        }
    }

    @OnClick(R.id.tvDailyFoodFact)
    protected void setDailyFoodFact(){
        // chrome custom tab init
        CustomTabsIntent customTabsIntent;
        CustomTabActivityHelper customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
        Uri dailyFoodFactUri = Uri.parse(taglineURL);
        customTabActivityHelper.mayLaunchUrl(dailyFoodFactUri, null, null);

        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(),
                customTabActivityHelper.getSession());
        CustomTabActivityHelper.openCustomTab(getActivity(),
                customTabsIntent, dailyFoodFactUri, new WebViewFallback());
    }

    @Override
    @NavigationDrawerType
    public int getNavigationDrawerType() {
        return ITEM_HOME;
    }

    private void checkUserCredentials() {
        final SharedPreferences settings = OFFApplication.getInstance().getSharedPreferences("login", 0);
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

        String txtHomeOnline = OFFApplication.getInstance().getResources().getString(R.string.txtHomeOnline);
        int productCount = sp.getInt("productCount", 0);
        apiClient.getTotalProductCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Search>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (productCount != 0) {
                            textHome.setText(String.format(txtHomeOnline, productCount));
                        } else {
                            textHome.setText(R.string.txtHome);
                        }
                    }

                    @Override
                    public void onSuccess(Search search) {
                        SharedPreferences.Editor editor;
                        int totalProductCount = Integer.parseInt(search.getCount());
                        textHome.setText(String.format(txtHomeOnline, totalProductCount));
                        editor = sp.edit();
                        editor.putInt("productCount", totalProductCount);
                        editor.apply();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (productCount != 0) {
                            textHome.setText(String.format(txtHomeOnline, productCount));
                        } else {
                            textHome.setText(R.string.txtHome);
                        }
                    }
                });

        if (BuildConfig.FLAVOR.equals("off")){
            getTagline();
        }

        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.home_drawer);
            }
        }

    }

    @Override
    public void onCustomTabsConnected() {

    }

    @Override
    public void onCustomTabsDisconnected() {

    }

    private void getTagline(){

       apiCacheClient = new OpenFoodAPIClient(getActivity(), cache).getAPIService();
       Call<ArrayList<TaglineLanguageModel>> call = apiCacheClient.getTagline();
       call.enqueue(new Callback<ArrayList<TaglineLanguageModel>>() {
           @Override
           public void onResponse(Call<ArrayList<TaglineLanguageModel>> call, Response<ArrayList<TaglineLanguageModel>> response) {
               if(response.isSuccessful()){
                   String locale = String.valueOf(Resources.getSystem().getConfiguration().locale);
                   boolean isLanguageFound = false;
                   for (int i = 0; i < response.body().size(); i++){
                       if (response.body().get(i).getLanguage().equals(locale)){
                           taglineURL = response.body().get(i).getTaglineModel().getUrl();
                           tvDailyFoodFact.setText(response.body().get(i).getTaglineModel().getMessage());
                           tvDailyFoodFact.setVisibility(View.VISIBLE);
                           isLanguageFound = true;
                       }
                   }
                   if (!isLanguageFound){
                       taglineURL = response.body().get(response.body().size() -1).getTaglineModel().getUrl();
                       tvDailyFoodFact.setText(response.body().get(response.body().size() -1).getTaglineModel().getMessage());
                       tvDailyFoodFact.setVisibility(View.VISIBLE);
                   }
               }
           }

           @Override
           public void onFailure(Call<ArrayList<TaglineLanguageModel>> call, Throwable t) { }
       });
    }
}