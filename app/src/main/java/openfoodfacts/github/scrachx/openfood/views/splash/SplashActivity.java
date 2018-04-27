package openfoodfacts.github.scrachx.openfood.views.splash;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import net.steamcrafted.loadtoast.LoadToast;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.WelcomeActivity;
import pl.aprilapps.easyphotopicker.EasyImage;

public class SplashActivity extends BaseActivity implements ISplashPresenter.View {

    @BindView(R.id.tagline)
    TextView tagline;
    int i = 0;
    private ISplashPresenter.Actions presenter;
    private LoadToast toast;
    private String[] taglines;

    /*
    To show different slogans below the logo while content is being downloaded.
     */
    Runnable changeTagline = new Runnable() {
        @Override
        public void run() {
            i++;
            if (i > taglines.length - 1)
                i = 0;
            tagline.setText(taglines[i]);
            tagline.postDelayed(changeTagline, 1500);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_splash);
        taglines = getResources().getStringArray(R.array.taglines_array);
        tagline.post(changeTagline);

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