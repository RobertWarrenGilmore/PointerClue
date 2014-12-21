package com.robertwarrengilmore.pointerclue;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.GregorianCalendar;
import java.util.LinkedList;

/**
 * Created by Robert Warren Gilmore on 2014-09-30.
 */
public class RotationChecker {

    private final SensorManager sensorManager;
    private Deque<Float> recentZOrientations = new LinkedList<>();
    private float zOrientation = 0;
    private Date lastUpdated;
    private final int SMOOTHING_CONSTANT = 10;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            // Normalise the sensor data for the different kinds of sensors that could produce it.
            float normalisedZ;
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                normalisedZ = sensorEvent.values[0] % 360;
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                normalisedZ = (sensorEvent.values[2] * -180) % 360;
            } else {
                throw new IllegalStateException("Neither Orientation nor Rotation Vector sensors are present.");
            }

            // Take the moving average of the last up-to-SMOOTHING_CONSTANT sensor readings.
            recentZOrientations.addLast(sensorEvent.values[0]);
            while (recentZOrientations.size() > SMOOTHING_CONSTANT) {
                recentZOrientations.removeFirst();
            }
            // If none of the values are between 90 and 270, rotate everything 180 before and after taking the mean.
            // Do any of the compass readings point southish?
            boolean pointingSouthish = false;
            for (float z : recentZOrientations) {
                if (z < 225 && z >= 135) {
                    pointingSouthish = true;
                    break;
                }
            }
            float mean = 0;
            if (!pointingSouthish) {
                for (float z : recentZOrientations) {
                    mean += (z + 180) % 360;
                }
                mean /= recentZOrientations.size();
                mean += 180;
                mean %= 360;
            } else {
                for (float z : recentZOrientations) {
                    mean += z;
                }
                mean /= recentZOrientations.size();
            }
            zOrientation = mean;
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
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (sensor == null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(sensorEventListener);
        recentZOrientations.clear();
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
