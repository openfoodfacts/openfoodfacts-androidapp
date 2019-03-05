package openfoodfacts.github.scrachx.openfood.views.category.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.customtabs.CustomTabsIntent;
import android.view.View;
import android.widget.Button;
import com.afollestad.materialdialogs.MaterialDialog;
import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.utils.ShakeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.ContinuousScanActivity;

public class CategoryActivity extends BaseActivity {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private boolean scanOnShake;
    private Button game_button;
    @BindView(R.id.buttonScan)
    FloatingActionButton mButtonScan;

    public static Intent getIntent(Context context) {
        return new Intent(context, CategoryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_category);
        setSupportActionBar(findViewById(R.id.toolbar));
        setTitle(R.string.category_drawer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        game_button = findViewById(R.id.game_button);

        SharedPreferences shakePreference = PreferenceManager.getDefaultSharedPreferences(this);
        scanOnShake = shakePreference.getBoolean("shakeScanMode", false);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();

        mShakeDetector.setOnShakeListener(count -> {
            if (scanOnShake) {
                Utils.scan(CategoryActivity.this);
            }
        });

        // chrome custom tab for category hunger game
        game_button.setOnClickListener(v -> {
            String url = getString(R.string.hunger_game_url);
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(getBaseContext(), Uri.parse(url));
        });


    }


    @OnClick (R.id.buttonScan)
    protected void onButtonScanClick() {
        if (Utils.isHardwareCameraInstalled(this)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    new MaterialDialog.Builder(this)
                            .title(R.string.action_about)
                            .content(R.string.permission_camera)
                            .neutralText(R.string.txtOk)
                            .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA))
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
                }
            } else {
                Intent intent = new Intent(this, ContinuousScanActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
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
