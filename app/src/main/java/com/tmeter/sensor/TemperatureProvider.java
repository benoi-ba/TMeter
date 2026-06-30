package com.tmeter.sensor;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;

public class TemperatureProvider implements SensorEventListener {

    private final Context appContext;
    private final SensorManager sensorManager;
    private final Sensor ambientTempSensor;
    private Float lastKnownAmbientTemp = null;
    private boolean isListening = false;
    
    public interface OnTemperatureChangeListener {
        void onTemperatureChanged(float temperature, String source);
    }
    
    private OnTemperatureChangeListener listener;

    public TemperatureProvider(Context context) {
        this.appContext = context.getApplicationContext();
        sensorManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            ambientTempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        } else {
            ambientTempSensor = null;
        }
    }

    public boolean hasAmbientSensor() {
        return ambientTempSensor != null;
    }

    public void startListening(OnTemperatureChangeListener listener) {
        this.listener = listener;
        if (ambientTempSensor != null && !isListening) {
            sensorManager.registerListener(this, ambientTempSensor, SensorManager.SENSOR_DELAY_NORMAL);
            isListening = true;
        }
        // Immediately push the current reading
        triggerImmediateReading();
    }

    public void stopListening() {
        if (isListening) {
            sensorManager.unregisterListener(this);
            isListening = false;
        }
        this.listener = null;
    }

    public void triggerImmediateReading() {
        if (listener == null) return;
        
        StringBuilder sourceBuilder = new StringBuilder();
        float temp = getCurrentReading(sourceBuilder);
        listener.onTemperatureChanged(temp, sourceBuilder.toString());
    }

    /**
     * Reads the current temperature.
     * Uses ambient sensor if available and active, otherwise falls back to battery temperature.
     */
    public float getCurrentReading(StringBuilder sourceOut) {
        if (ambientTempSensor != null && lastKnownAmbientTemp != null) {
            if (sourceOut != null) {
                sourceOut.setLength(0);
                sourceOut.append("SENSOR");
            }
            return lastKnownAmbientTemp;
        }

        if (sourceOut != null) {
            sourceOut.setLength(0);
            sourceOut.append("BATTERY");
        }
        return getBatteryTemperature();
    }

    private float getBatteryTemperature() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = appContext.registerReceiver(null, filter);
            if (batteryStatus != null) {
                int tempTenths = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                return tempTenths / 10.0f;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0f; // default fallback if error or unavailable
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            lastKnownAmbientTemp = event.values[0];
            if (listener != null) {
                listener.onTemperatureChanged(lastKnownAmbientTemp, "SENSOR");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}
