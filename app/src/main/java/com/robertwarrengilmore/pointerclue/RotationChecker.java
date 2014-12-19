package com.robertwarrengilmore.pointerclue;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Robert Warren Gilmore on 2014-09-30.
 */
public class RotationChecker {

    private final SensorManager sensorManager;
    private float zOrientation = 0;
    private Date lastUpdated;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            // get the angle around the z-axis rotated
            zOrientation = sensorEvent.values[2];
            lastUpdated = new Date();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    public RotationChecker(Context context) {
        sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
    }

    public void start() {
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    public float getZOrientation() {
        return zOrientation;
    }

    public boolean isStale() {
        Calendar staleCalendar = GregorianCalendar.getInstance();
        staleCalendar.add(Calendar.SECOND, -1);
        return (lastUpdated == null) || (lastUpdated.before(staleCalendar.getTime()));
    }
}
