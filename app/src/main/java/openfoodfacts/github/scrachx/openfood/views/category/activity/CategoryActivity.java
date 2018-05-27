package org.openfoodfacts.scanner.views.category.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import org.openfoodfacts.scanner.R;
import org.openfoodfacts.scanner.utils.ShakeDetector;
import org.openfoodfacts.scanner.utils.Utils;
import org.openfoodfacts.scanner.views.BaseActivity;

public class CategoryActivity extends BaseActivity {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private boolean scanOnShake;

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

        SharedPreferences shakePreference = PreferenceManager.getDefaultSharedPreferences(this);
        scanOnShake = shakePreference.getBoolean("shakeScanMode", false);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();

        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeDetected() {
            @Override
            public void onShake(int count) {
                if (scanOnShake) {
                    Utils.scan(CategoryActivity.this);
                }
            }
        });
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
