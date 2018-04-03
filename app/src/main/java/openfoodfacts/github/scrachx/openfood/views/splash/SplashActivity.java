package openfoodfacts.github.scrachx.openfood.views.splash;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import net.steamcrafted.loadtoast.LoadToast;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.WelcomeActivity;
import pl.aprilapps.easyphotopicker.EasyImage;

public class SplashActivity extends BaseActivity implements ISplashPresenter.View {

    private ISplashPresenter.Actions presenter;
    private LoadToast toast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        toast = new LoadToast(this);

        presenter = new SplashPresenter(getSharedPreferences("prefs", 0), this);
        presenter.refreshData();
    }

    @Override
    public void navigateToMainActivity() {
        EasyImage.configuration(this)
                .setImagesFolderName("OFF_Images")
                .saveInAppExternalFilesDir()
                .setCopyExistingPicturesToPublicLocation(true);
        Intent mainIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void showLoading() {
        toast.setText(SplashActivity.this.getString(R.string.toast_retrieving));
        toast.setBackgroundColor(ContextCompat.getColor(SplashActivity.this, R.color.blue));
        toast.setTextColor(ContextCompat.getColor(SplashActivity.this, R.color.white));
        toast.show();
    }

    @Override
    public void hideLoading(boolean isError) {
        if (isError)
            toast.error();
        else
            toast.success();
    }

    @Override
    public AssetManager getAssetManager() {
        return getAssets();
    }
}