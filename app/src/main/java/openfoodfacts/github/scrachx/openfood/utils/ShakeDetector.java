package openfoodfacts.github.scrachx.openfood.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;

/**
 * Created by prajwalm on 31/03/18.
 */

public class ShakeDetector implements SensorEventListener {

    private static final float G_FORCE_THRESHOLD = 2.7F;
    private static final int SHAKE_COUNT_IGNORE_INTERVAL = 3000;
    private static final int SHAKE_NOT_CONSIDERABLE = 500;
    private OnShakeDetected onShakeDetected;
    private int mShakeCount;
    private long mShakeTimeStamp;

    private long nowTime;


    public void setOnShakeListener(OnShakeDetected mListener) {

        this.onShakeDetected = mListener;

    }

    public interface OnShakeDetected {
        void onShake(int count);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (onShakeDetected != null) {
            float x = sensorEvent.values[0] / SensorManager.GRAVITY_EARTH;
            float y = sensorEvent.values[1] / SensorManager.GRAVITY_EARTH;
            float z = sensorEvent.values[2] / SensorManager.GRAVITY_EARTH;

            float gForce = (float) Math.sqrt(x * x + y * y + z * z);

            if (gForce > G_FORCE_THRESHOLD) {

                nowTime = System.currentTimeMillis();

                // If the time between shakes is less than return
                if (mShakeTimeStamp + SHAKE_NOT_CONSIDERABLE > nowTime) {

                    return;

                }

                // If no shake occurs for 3 seconds then set mShakeCount to zero
                if (mShakeTimeStamp + SHAKE_COUNT_IGNORE_INTERVAL < nowTime) {

                    mShakeCount = 0;

                }

                mShakeTimeStamp = nowTime;
                mShakeCount++;

                onShakeDetected.onShake(mShakeCount);


            }


        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
