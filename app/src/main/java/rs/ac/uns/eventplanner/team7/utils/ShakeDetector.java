package rs.ac.uns.eventplanner.team7.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import rs.ac.uns.eventplanner.team7.model.interfaces.Shakeable;

public final class ShakeDetector implements SensorEventListener {
    private static final float SHAKE_THRESHOLD = 12.0f;
    private static final int SHAKE_SLOP_TIME_MS = 2000;
    private long lastShakeTime;
    private final Shakeable listener;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;

    public ShakeDetector(Context context, Shakeable listener) {
        this.listener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void start() {
        if (accelerometer == null) return;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float acceleration = (float) Math.sqrt(x*x + y*y + z*z) - SensorManager.GRAVITY_EARTH;
        if (acceleration > SHAKE_THRESHOLD) {
            long now = System.currentTimeMillis();
            if (now - lastShakeTime > SHAKE_SLOP_TIME_MS) {
                lastShakeTime = now;
                listener.onShakeDetected();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for shake detection
    }
}

