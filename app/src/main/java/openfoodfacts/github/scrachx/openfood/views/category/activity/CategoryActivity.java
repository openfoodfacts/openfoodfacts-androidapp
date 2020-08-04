package openfoodfacts.github.scrachx.openfood.views.category.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityCategoryBinding;
import openfoodfacts.github.scrachx.openfood.utils.ShakeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.category.fragment.CategoryListFragment;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;

public class CategoryActivity extends BaseActivity {
    private ActivityCategoryBinding binding;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private boolean scanOnShake;

    public static void start(Context context) {
        Intent starter = new Intent(context, CategoryActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        binding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarInclude.toolbar);
        setTitle(R.string.category_drawer);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        SharedPreferences shakePreference = PreferenceManager.getDefaultSharedPreferences(this);
        scanOnShake = shakePreference.getBoolean("shakeScanMode", false);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mShakeDetector = new ShakeDetector();

            mShakeDetector.setOnShakeListener(count -> {
                if (scanOnShake) {
                    Utils.scan(CategoryActivity.this);
                }
            });
        }

        // chrome custom tab for category hunger game
        binding.gameButton.setOnClickListener(v -> openHungerGame());

        // set fragment container view
        getSupportFragmentManager().beginTransaction().add(R.id.fragment, new CategoryListFragment()).commitNow();

        BottomNavigationListenerInstaller.selectNavigationItem(binding.bottomNavigationInclude.bottomNavigation, 0);
        BottomNavigationListenerInstaller.install(binding.bottomNavigationInclude.bottomNavigation, this);
    }

    private void openHungerGame() {
        final String url = getString(R.string.hunger_game_url);
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(CategoryActivity.this, Uri.parse(url));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (scanOnShake) {
            mSensorManager.unregisterListener(mShakeDetector, mAccelerometer);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (scanOnShake) {
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }
}
