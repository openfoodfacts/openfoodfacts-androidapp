package openfoodfacts.github.scrachx.openfood.views.splash;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.android.material.snackbar.Snackbar;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivitySplashBinding;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.welcome.WelcomeActivity;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG;

public class SplashActivity extends BaseActivity implements ISplashActivity.View {
    private ActivitySplashBinding binding;
    private String[] taglines;
    /*
    To show different slogans below the logo while content is being downloaded.
     */
    private final Runnable changeTagline = new Runnable() {
        int i = 0;

        @Override
        public void run() {
            i++;
            if (i > taglines.length - 1) {
                i = 0;
            }
            if (binding != null) {
                binding.tagline.setText(taglines[i]);
                binding.tagline.postDelayed(changeTagline, 1500);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        taglines = getResources().getStringArray(R.array.taglines_array);
        binding.tagline.post(changeTagline);

        ISplashActivity.Controller presenter = new SplashController(getSharedPreferences("prefs", 0), this, this);
        presenter.refreshData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void navigateToMainActivity() {
        EasyImage.configuration(this)
            .setImagesFolderName("OFF_Images")
            .saveInAppExternalFilesDir()
            .setCopyExistingPicturesToPublicLocation(true);
        WelcomeActivity.start(this);
        finish();
    }

    @Override
    public void showLoading() {
    }

    @Override
    public void hideLoading(boolean isError) {
        if (isError) {
            new Handler(Looper.getMainLooper()).post(() -> Snackbar.make(binding.getRoot(), R.string.errorWeb, LENGTH_LONG).show());
        }
    }
}
